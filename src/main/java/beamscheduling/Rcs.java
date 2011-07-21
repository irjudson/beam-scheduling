package beamscheduling;

import java.util.*;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.PrimMinimumSpanningTree;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.graph.Graph;

public class Rcs {

    static Logger logger = Logger.getLogger("RoutingChannelSelection");

    public static void dfsPath(Graph network, Vertex src, Vertex dst,
                              String prefix, Vector paths, 
                              ArrayList<Edge> path) {
        for(Object o: network.getNeighbors(src)) {
            Vertex v = (Vertex)o;
            Edge e = (Edge) network.findEdge(src, v);
            if (e == null || path.contains(e)) {     // Bad node or edge
                continue;  
            } else if (v == dst) {           // Found the destination
                path.add(e);
                paths.add(path.clone());
                path.remove(e);
            } else {                                 // Still looking...
                path.add(e);
                dfsPath(network, v, dst, prefix + "|", paths, path);
                path.remove(e);
            }
        }
    }

    public static Boolean inPath(List<Edge> path, Vertex vertex, 
                                 Graph network) {
        //System.out.println("Checking for " + vertex + " in " + path);
        for(Edge e: path) {
            Pair<Vertex> ends = network.getEndpoints(e);
            if ((Vertex)ends.getFirst() == vertex || 
                (Vertex)ends.getSecond() == vertex) { 
                //System.out.println("Found " + vertex + " in " + path);
                return true; 
            }
        }
        return false;
    }

    public static List<Edge> rcsPath(Graph network, Vertex src,
                                     Vertex dst, int consider) {
        ChannelSelection cs = new ChannelSelection((Network)network);
        int uremove = 0, vremove = 0;

        // Initialize all paths
        for (Object o : network.getVertices()) {
            Vertex v = (Vertex) o;
            if (v != src) {
                v.rcsPaths = new TreeMap();
                for(int i = 0; i < consider; i++) {
                    v.rcsPaths.put(0.0d, new ArrayList<Edge>());
                }
            } else {
                v.rcsPaths = new TreeMap();
                v.rcsPaths.put(0.0d, new ArrayList<Edge>());
            }
        }

        // As long as no paths are getting better...keep cycling edges & paths
        Boolean updating = true;
        while(updating) {
            // Try to extend all the paths
            Vector newpaths = new Vector();
            for(Object o: network.getEdges()) {
                Edge e = (Edge)o;
                Pair<Vertex> ends = network.getEndpoints(e);
                Vertex u = (Vertex)ends.getFirst();
                Vertex v = (Vertex)ends.getSecond();

                for(Object c: u.rcsPaths.keySet()) {
                    double othpt = (Double)c;
                    List<Edge> path = (List<Edge>)u.rcsPaths.get(c);
                    if (! path.contains(e) && (!inPath(path, u, network) 
                                               || !inPath(path, v, network))){
                        path.add(e);
                        double cthpt = cs.selectChannels((List<Edge>)path);
                        // If the throughput of the extended path is better
                        if (cthpt > othpt) {
                            // Remove the old one
                            uremove += 1;
                            //u.rcsPaths.remove(c);
                            // Add the extended path to S(u)
                            u.rcsPaths.put(cthpt, path);
                            System.out.println("Updating " + e.id + " (" + u 
                                               + ","+ v +") [" 
                                               + cthpt + "] = " + path);
                            // Add the extended path to S(v)
                            v.rcsPaths.put(cthpt, path);
                            // Remove the worst path from S(v)
                            vremove += 1;
                            //v.rcsPaths.remove(v.rcsPaths.firstKey());
                            // Mark things to continue again
                            updating = true;
                        } else {
                            // Mark things to quit
                            updating = false;
                        }
                    }
                }

                // Cleanup to make sure we don't break indexing anymore
                while(uremove > 0) {
                    u.rcsPaths.remove(u.rcsPaths.firstKey());
                    uremove--;
                }

                while(vremove > 0) {
                    v.rcsPaths.remove(v.rcsPaths.firstKey());
                    vremove--;
                }
            }
        }
        return((List<Edge>)dst.rcsPaths.get(dst.rcsPaths.lastKey()));
    }
    
    public static void main(String[] args) {
        HashMap subscribers;
        NetworkGenerator networkGenerator;
        Network network;
        RcsOptions options = new RcsOptions();
        CmdLineParser parser = new CmdLineParser(options);
        Draw drawing = null;
        ChannelSelection cs = null;
        int[] sources, destinations;
        double[] primThpt, primThptGdyCS;
        double[] dijkstraThpt, dijkstraThptGdyCS;
        
        parser.setUsageWidth(80);

        BasicConfigurator.configure();
        logger.setLevel(Level.DEBUG);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            logger.error("Failed to parse command line arguments.");
            logger.error(e.getMessage());
            parser.printUsage(System.err);
            System.exit(1);
        }

        sources = new int[options.iter];
        destinations = new int[options.iter];
        primThpt = new double[options.iter];
        primThptGdyCS = new double[options.iter];
        dijkstraThpt = new double[options.iter];
        dijkstraThptGdyCS = new double[options.iter];

        // Handle options that matter
        if (options.verbose) {
            System.out.println("Random Seed: " + options.seed);
        }
        networkGenerator = Network.getGenerator(options.relays,
                                                options.subscribers,
                                                options.width, options.height,
                                                options.seed, options.channels);
        network = networkGenerator.create();

        Transformer<Edge, Double> wtTransformer = new Transformer<Edge, Double>() {

            public Double transform(Edge e) {
                if (e.capacity > 0.0) {
                    return e.length;
                } else {
                    return Double.MAX_VALUE;
                }
            }
        };

        Transformer<Edge, Double> pTransformer = new Transformer<Edge, Double>() {

            public Double transform(Edge e) {
                return e.bottleNeckWeight();
            }
        };

        int count = 0;
        while(count < options.iter) {
            Vertex source = network.randomRelay();
            Vertex destination = network.randomRelay();
            while (source == destination) {
                destination = network.randomRelay();
            }

            source.type = 3;
            destination.type = 4;

            if (options.verbose) {
                System.out.println("Source: " + source
                                   + " Destination: " + destination);
            }

            // Find dmax and dmin
            double dmin = Double.MAX_VALUE, dmax = Double.MIN_VALUE;
            for (Object e1 : network.getEdges()) {
                Pair<Object> ends = network.getEndpoints(e1);
                Vertex a = (Vertex) ends.getFirst();
                Vertex b = (Vertex) ends.getSecond();
                double ad = (a.distanceTo(source) + b.distanceTo(destination)) / 2.0;
                if (ad < dmin) {
                    dmin = ad;
                }
                if (ad > dmax) {
                    dmax = ad;
                }
            }

            // Compute weights for the edges
            for (Object e1 : network.getEdges()) {
                Edge e = (Edge) e1;
                Pair<Object> ends = network.getEndpoints(e1);
                Vertex a = (Vertex) ends.getFirst();
                Vertex b = (Vertex) ends.getSecond();
                double d = (a.distanceTo(source) + b.distanceTo(destination)) / 2.0;
                e.weight = (1.0 + (dmax - d) / (dmax - dmin)) / 2.0;
                if (options.verbose) {
                    System.out.println("Edge: " + e.id + " W: " + e.weight);
                }
            }

            if (options.verbose) {
                System.out.println("S: " + source + " D: " + destination);
            }

            DijkstraShortestPath<Vertex, Edge> dsp = new DijkstraShortestPath(network, wtTransformer, false);
            List<Edge> dpath = dsp.getPath(source, destination);
            if (dpath.size() == 0) {
                continue;
            } else {
                System.out.println("["+count+"] Dijkstra Path: " + dpath.toString());
                for (Edge e : dpath) {
                    e.type = 1;
                }
            
                try {
                    cs = new ChannelSelection(network);
                    dijkstraThpt[count] = cs.selectChannels(dpath);
                    dijkstraThptGdyCS[count] = cs.greedySelectChannels(dpath);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("S: " + source + " D: " + destination);
                }

                PrimMinimumSpanningTree psp = new PrimMinimumSpanningTree(networkGenerator.networkFactory, pTransformer);
                Graph primTree = psp.transform(network);
                if (options.verbose) {
                    System.out.println("Prim Tree: "+ primTree.toString());
                }

                for (Object e : primTree.getEdges()) {
                    ((Edge) e).type = 2;
                }

                // Clear out markings
                for (Object v : primTree.getVertices()) {
                    ((Vertex) v).isMarked = false;
                }
                for (Object e : primTree.getEdges()) {
                    ((Edge) e).isMarked = false;
                }

                DijkstraShortestPath<Vertex, Edge> dsp2 = new DijkstraShortestPath(primTree, wtTransformer, false);
                List<Edge> primpath = dsp2.getPath(source, destination);

                for (Edge e : primpath) { e.type = 4; }
                System.out.println("["+count+"] Prim Path: " 
                                   + primpath.toString());
                
                // RCS
                List<Edge> rcsPath = rcsPath(network, source, destination, 
                                             options.consider);
                if (rcsPath == null) { rcsPath = new ArrayList<Edge>(); }
                System.out.println("["+count+"] RCS Path: " 
                                   + rcsPath.toString());

                for(Edge e: rcsPath) { e.type = 5; }

                primThpt[count] = cs.selectChannels(primpath);
                primThptGdyCS[count] = cs.greedySelectChannels(primpath);

                sources[count] = source.id;
                destinations[count] = destination.id;
                count += 1;
            }
        }

        if (options.display) {
            drawing = new Draw(network, 1024, 768, 
                               "Routing and Channel Selection Application");
            drawing.draw();
        }

        System.out.println("Seed, Iter, Width, Height, Nodes, Users, Channels, Source, Destination, Dijkstra, Prim, DijkstraGdyCS, PrimGdyCS");
        for(int i = 0; i < options.iter; i++) {
            System.out.println(options.seed + ", " + i + ", " + options.width + ", " + options.height + ", " + options.relays + ", " + options.subscribers + ", " + options.channels + ", " + sources[i] + ", " + destinations[i] + ", " + dijkstraThpt[i] + ", " + primThpt[i] + ", " + dijkstraThptGdyCS[i] + ", " + primThptGdyCS[i]);
        }

        if (options.display) {
            drawing.repaint();
        }
    }
}
