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

// for dijkstra the weights can just be the physical link distance 
// (assuming there is at least one available channel on the link). 
// basically we should assign random channels to each link, 
// then place primary users (removing some channels), any link 
// that still has >= 1 channel survives, then we run routing algorithms 
// to get paths, then channel selection on each path to determine the 
// end-to-end quality

public class Rcs {

    static Logger logger = Logger.getLogger("RoutingChannelSelection");

    public static void main(String[] args) {
        HashMap subscribers;
        NetworkGenerator networkGenerator;
        Network network;
        RcsOptions options = new RcsOptions();
        CmdLineParser parser = new CmdLineParser(options);
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

        // Handle options that matter
        System.out.println("Random Seed: " + options.seed);
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

        Transformer<Edge, Double>pTransformer = new Transformer<Edge, Double>() {
            public Double transform(Edge e) {
                return e.bottleNeckWeight();                
            }
        };

        Vertex source = network.randomRelay();
        Vertex destination = network.randomRelay();
        while (source == destination) {
            destination = network.randomRelay();
        }

        // System.out.println("Source: " + source);
        // System.out.println("Destination: " + destination);
        source.type = 3;
        destination.type = 4;

        DijkstraShortestPath<Vertex, Edge> dsp = new DijkstraShortestPath(network, wtTransformer);
        List<Edge> dpath = dsp.getPath(source, destination);
        System.out.println("Dijkstra Path");
        System.out.println(dpath.toString());
        for(Edge e: dpath) { e.type = 1; }

        PrimMinimumSpanningTree psp = new PrimMinimumSpanningTree(networkGenerator.networkFactory, pTransformer);
        Graph primTree = psp.transform(network);
        System.out.println("Prim Tree");
        System.out.print(primTree.toString());

        DijkstraShortestPath<Vertex, Edge> dsp2 = new DijkstraShortestPath(primTree, wtTransformer);
        List<Edge> ppath = dsp2.getPath(source, destination);
        for(Edge e: ppath) { e.type = 4; }
        System.out.println("Prim Path");
        System.out.println(ppath.toString());

        //((Network)primTree).draw(1024, 768, "FOo");
        network.draw(1024, 768, "Routing and Channel Selection Application");
        System.out.println("Seed, Width, Height, Nodes, Users, Channels, Dijkstra, Prim");
        System.out.println(options.seed + ", " + options.width + ", " + options.height + ", " + options.relays + ", " + options.subscribers + ", " + options.channels + ", 0.0, 0.0");
    }
}
