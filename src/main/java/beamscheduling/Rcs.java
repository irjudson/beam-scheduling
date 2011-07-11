package beamscheduling;

import java.util.*;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.Pair;

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
                return e.length;
            }
        };

        DijkstraShortestPath<Vertex, Edge> dsp = new DijkstraShortestPath(network, wtTransformer);
        //DijkstraShortestPath<Vertex, Edge> dsp = new DijkstraShortestPath(network);
        Vertex source = network.randomRelay();
        Vertex destination = network.randomRelay();
        while (source == destination) {
            destination = network.randomRelay();
        }

        System.out.println("Source: " + source);
        System.out.println("Destination: " + destination);
        source.type = 3;
        destination.type = 4;

        List<Edge> dpath = dsp.getPath(source, destination);
        double totalTP = 0.0;
        double totalLength = 0.0;
        for(Edge e: dpath) {
            Pair<Vertex> ends = network.getEndpoints(e);
            e.type = 1;
            totalTP += e.capacity;
            totalLength += e.length;
            System.out.println(ends.getFirst() + " -> " + ends.getSecond());
        }

        System.out.println("Dijkstra: (cap) " + totalTP + " (length) " + totalLength);
        network.draw(1024, 768, "Routing and Channel Selection Application");

        //        System.out.println("Seed, Width, Height, Nodes, Users, Channels, Dijkstra");
        //        System.out.println(options.seed + ", " + options.width + ", " + options.height + ", " + options.relays + ", " + options.subscribers + ", " + options.channels + ", " + totalTP);
    }
}
