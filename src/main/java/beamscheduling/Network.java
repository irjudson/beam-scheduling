package beamscheduling;

import java.util.*;

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

public class Network<V,E>
    extends UndirectedSparseGraph<V,E>
    implements Graph<V,E> 
{

    double width;
    double height;

    public Network(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns a {@code Generator} that creates an instance of this graph type.
     * @param <V> the vertex type for the graph factory
     * @param <E> the edge type for the graph factory
     */
    public static NetworkGenerator getGenerator(int numVertices, int sectors, double width, double height, long seed) 
    { 
        NetworkGenerator gen = new NetworkGenerator(new NetworkFactory(width, height),
                                                    new VertexFactory(width, height, sectors),
                                                    new EdgeFactory(), numVertices, width, height);
        gen.setSeed(seed);
        return(gen);
    }
   
    void draw(int width, int height, String name) {
        JFrame jf = new JFrame(name);
        Dimension layoutSize = new Dimension(width, height);
        Layout layout = new StaticLayout(this, new NetworkTransformer(layoutSize, this.width, this.height), layoutSize);
        VisualizationViewer vv = new VisualizationViewer(layout, layoutSize);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
        jf.getContentPane().add(vv);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);
    }
}
