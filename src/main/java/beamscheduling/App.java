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

    public static void main(String[] args) {
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
        //System.out.println("Random Seed: " + options.seed);
        networkGenerator = Network.getGenerator(options.nodes, options.clients, options.sectors,
                options.width, options.height, options.seed, options.theta, options.meanq, options.slotLength);
        network = networkGenerator.createCenteredRadialTree();

        //System.out.println(network);


        network.calculateBeamSets();

        ILPSolve ilpSolve = new ILPSolve(network);
        double ilpThpt = ilpSolve.solve();


        LPRound lpround = new LPRound(network);
        double lprThpt = lpround.solve();


        Greedy greedy = new Greedy(network);
        double grdyThpt = greedy.solve();

        //network.draw(1024, 768, "Beam Scheduling Application");
        
        System.out.println("Seed, Width, Height, Theta, Relays, Subscribers, Slot Length, MeanQ, ILP, LPR, GDY");
        System.out.println(options.seed + ", " + options.width + ", " + options.height + ", " + options.theta + ", " +
                + options.nodes + ", " + options.clients + ", " + options.slotLength + ", " + options.meanq + ", "
                + ilpThpt + ", " + lprThpt + ", " + grdyThpt);
    }
}
