package beamscheduling;

import java.util.*;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

/**
 * Beam Scheduling Code.
 *
 */
public class App {
    static Logger logger = Logger.getLogger("BeamScheduling");

    public static void main( String[] args )
    {
        HashMap subscribers;
        NetworkGenerator networkGenerator;
        Network network;
        CmdLineOptions options = new CmdLineOptions();
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
        networkGenerator = Network.getGenerator(options.nodes, options.clients, options.sectors, 
                                                options.width, options.height, options.seed);
        network = networkGenerator.createCenteredRadialTree();

        System.out.println(network);
        network.draw(1024, 768, "Beam Scheduling Application");

        int step = 1;
        int theta = 20;
        Greedy greedy = new Greedy(network);
        greedy.solve(step, theta);

        LPRound lpround = new LPRound(network);
        lpround.solve(step, theta);

    }
}
