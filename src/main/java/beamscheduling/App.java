package beamscheduling;

import java.util.*;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

///

import java.awt.Dimension;
import java.awt.geom.Point2D;

import javax.swing.JFrame;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.*;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

///

/**
 * Beam Scheduling Code.
 *
 */
public class App {
    static Logger logger = Logger.getLogger("BeamScheduling");

    public static void main( String[] args )
    {
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

        System.out.println("Random Seed: " + options.seed);
        networkGenerator = Network.getGenerator(options.nodes, options.sectors, options.width, options.height, options.seed);
        network = networkGenerator.create();

        for(Vertex v: (Collection<Vertex>)network.getVertices()) {
            v.computeConnectivity(network);
        }

        System.out.print(network);

        network.draw(800, 600, "Beam Scheduling Application");
    }

}
