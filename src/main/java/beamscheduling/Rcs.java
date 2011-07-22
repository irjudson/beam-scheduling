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

    public static List<Edge> rcsPath(Graph network, Vertex src,
                                     Vertex dst, int consider, int sweeps) {
        ChannelSelection cs = new ChannelSelection((Network)network);

        // Initialize all paths
        for (Object o : network.getVertices()) {
            Vertex v = (Vertex) o;
            if (v != src) {
                if (v.rcsPaths == null) {
                    v.rcsPaths = new TreeMap();
                }
            } else {
                v.rcsPaths = new TreeMap();
                v.rcsPaths.put(0.0d, new ArrayList<Edge>());
                for(Object v0: network.getNeighbors(v)) {
                    List<Edge> p = new ArrayList<Edge>();
                    Vertex v01 = (Vertex)v0;
                    Edge e = (Edge)network.findEdge(v, v0);
                    p.add(e);
                    double th = cs.selectChannels(p);
                    if (v01.rcsPaths == null) {
                        v01.rcsPaths = new TreeMap();
                    }
                    v01.rcsPaths.put(th, p);
                }
            }
        }

        for(int i = 0; i < sweeps; i++) {
            // System.out.println("RCS Sweep: " + i + "/" + sweeps);
            // For each edge see if we can extend the existing paths with it
            for(Object o: network.getEdges()) {
                Edge e = (Edge)o;
                Pair<Vertex> ends = network.getEndpoints(e);
                Vertex u = (Vertex)ends.getFirst();
                Vertex v = (Vertex)ends.getSecond();
                ArrayList<Edge> path = null;
                // First direction
                if (v != src) {
                    for(Object c: u.rcsPaths.keySet()) {
                        path = (ArrayList<Edge>)((ArrayList<Edge>)u.rcsPaths.get(c)).clone();
                        path.add(e);
                        v.rcsPaths.put(cs.selectChannels((List<Edge>)path), 
                                       path);
                        // If we added one and we're over, take one out
                        if(v.rcsPaths.keySet().size() > consider) {
                            v.rcsPaths.remove(v.rcsPaths.firstKey());
                        }
                    }
                }

                // Second direction
                if (u != src) {
                    for(Object c: v.rcsPaths.keySet()) {
                        double othpt = (Double)c;
                        path = (ArrayList<Edge>)((ArrayList<Edge>)v.rcsPaths.get(c)).clone();
                        path.add(e);
                        u.rcsPaths.put(cs.selectChannels((List<Edge>)path),
                                       path);
                        // If we added one and we're over, take one out
                        if(u.rcsPaths.keySet().size() > consider) {
                            u.rcsPaths.remove(u.rcsPaths.firstKey());
                        }
                    }
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
        double[] primThpt, primThptGdyCS, rcsThpt;
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
        rcsThpt = new double[options.iter];

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
                                             options.consider, 
                                             network.getVertexCount());
                if (rcsPath == null) { rcsPath = new ArrayList<Edge>(); }
                System.out.println("["+count+"] RCS Path: " 
                                   + rcsPath.toString());

                for(Edge e: rcsPath) { e.type = 5; }

                rcsThpt[count] = cs.selectChannels(rcsPath);

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

        System.out.println("Seed, Iter, Width, Height, Nodes, Users, Channels, Source, Destination, Dijkstra, Prim, DijkstraGdyCS, PrimGdyCS, RCS");
        for(int i = 0; i < options.iter; i++) {
            System.out.println(options.seed + ", " + i + ", " 
                               + options.width + ", " + options.height + ", " 
                               + options.relays + ", " + options.subscribers 
                               + ", " + options.channels + ", " + sources[i] 
                               + ", " + destinations[i] + ", " 
                               + dijkstraThpt[i] + ", " + primThpt[i] + ", " 
                               + dijkstraThptGdyCS[i] + ", " 
                               + primThptGdyCS[i] + ", " + rcsThpt[i]);
        }

        if (options.display) {
            drawing.repaint();
        }
    }
}
