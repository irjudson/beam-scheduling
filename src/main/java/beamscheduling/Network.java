package beamscheduling;

import java.util.*;

import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
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
    public double width;
    public double height;

    public Vertex gateway;
    public HashSet relays;
    public HashSet subscribers;
    public Vertex[] relayList;
    public Vertex[] subList;

    public Network(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns a {@code Generator} that creates an instance of this graph type.
     * @param <V> the vertex type for the graph factory
     * @param <E> the edge type for the graph factory
     */
    public static NetworkGenerator getGenerator(int numRelays, int numSubscribers, int sectors, double width, double height, long seed) 
    { 
        NetworkGenerator gen = new NetworkGenerator(new NetworkFactory(width, height),
                                                    new VertexFactory(width, height, sectors),
                                                    new EdgeFactory(), numRelays, numSubscribers, width, height);
        gen.setSeed(seed);
        return(gen);
    }
   
    void draw(int width, int height, String name) {
        Transformer<Edge, Stroke> edgeDraw = new Transformer<Edge, Stroke>() {
            public Stroke transform(Edge e) {
                if (e.type == 0) {
                    return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
                } else if (e.type == 1) {
                    float dash[] = {5.0f};
                    return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                } else if (e.type == 2) {
                    float dash[] = {10.0f};
                    return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                } else if (e.type == 3) {
                    float dash[] = {15.0f};
                    return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                } else {
                    float dash[] = {20.0f};
                    return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                }
            }
        };

        Transformer<Vertex,Paint> vertexPaint = new Transformer<Vertex,Paint>() {
            public Paint transform(Vertex v) {
                if (v.type == 0) {
                    return Color.RED;
                } else if (v.type == 1) {
                    return Color.BLUE;
                } else if (v.type == 2) {
                    return Color.GREEN;
                } else {
                    return Color.YELLOW;
                }
            }
        };  

        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Beam Scheduling");
        JFrame jf = new JFrame(name);
        Dimension layoutSize = new Dimension(width, height);
        Layout layout = new StaticLayout(this, new NetworkTransformer(layoutSize, this.width, this.height), layoutSize);
        VisualizationViewer vv = new VisualizationViewer(layout, layoutSize);
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeStrokeTransformer(edgeDraw);
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
        jf.getContentPane().add(vv);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);
    }

    public void calculateBeamSet(int step, int theta) {
        SortedMap beamSet = new TreeMap();
        Iterator itr = this.relays.iterator();

        // First we organize things for calculations
        while(itr.hasNext()) {
            Vertex relay = (Vertex)itr.next();
            SortedMap subscribers = new TreeMap();

            // Calculate what SS's are even in range, then the bearing to them
            Iterator ss = this.subscribers.iterator();
            while(ss.hasNext()) {
                Vertex subscriber = (Vertex)ss.next();
                double throughput = relay.calculateThroughput(theta, subscriber);
                if (throughput > 0) {
                 double bearing = relay.getBearing(subscriber);
                 ArrayList d = new ArrayList();
                 d.add(0, throughput);
                 d.add(1, subscriber);
                 subscribers.put(bearing, d);
                }
            }
            beamSet.put(relay, subscribers);
        }
        // Now we can calcuate beamsets sanely
        // Looping through beamSet which is organized like:
        // beamSet[relay] => subscribers[bearing] => [throughput, ss]
        itr = beamSet.keySet().iterator();
        int bestBearing = 0;
        double bestThroughput = 0.0;
        HashSet bestSet = null;
        while(itr.hasNext()) {
            Vertex relay = (Vertex)itr.next();
            SortedMap subscribers = (SortedMap)beamSet.get(relay);
            SortedMap beamContains = new TreeMap();
            
            bestThroughput = 0.0;
            for(int i = 0; i < 360; i++) {
                int halfTheta = theta/2;
                int upperBound = i + halfTheta;
                if (upperBound > 360) {
                    upperBound -= 360;
                }
                int lowerBound = i - halfTheta;
                if (lowerBound < 0) {
                    lowerBound += 360;
                }
             
                Iterator brng = subscribers.keySet().iterator();
                while(brng.hasNext()) {
                    double bearing = Double.valueOf(brng.next().toString()).doubleValue();
                    if (bearing <= upperBound && bearing >= lowerBound) {
                        ArrayList entry = (ArrayList)subscribers.get(bearing);
                        beamContains.put(bearing, entry);
                    }
                    // Remove things that fall out of the back of the beam
                    try {
                        while (Double.valueOf(beamContains.firstKey().toString()).doubleValue() < lowerBound) {
                            beamContains.remove(beamContains.firstKey());
                        }
                    } catch (NoSuchElementException ex) {
                        // There's nothing to try to remove
                    }
                }
                if (beamContains.keySet().size() > 0 ) {
                    // Print the beam set for this degree position
                    Iterator bci = beamContains.keySet().iterator();
                    double throughput = 0.0;
                    HashSet subs = new HashSet();
                    while(bci.hasNext()) {
                        double bearing = Double.valueOf(bci.next().toString()).doubleValue();
                        ArrayList entry = (ArrayList)beamContains.get(bearing);
                        throughput += Double.valueOf(entry.get(0).toString()).doubleValue();
                        subs.add(entry.get(1));
                    }
                    if(throughput >= bestThroughput) {
                        bestThroughput = throughput;
                        bestBearing = i;
                        bestSet = subs;
                    }
                }
            }
            System.out.println("Relay: " + relay + " Bearing: " + bestBearing + " Throughput: " + bestThroughput);
            Iterator sitr = bestSet.iterator();
            while(sitr.hasNext()) {
                Vertex sub = (Vertex)sitr.next();
                Edge e = new Edge();
                e.type = 2;
                e.length = Point.roundTwoDecimals(sub.location.distance(relay.location));
                this.addEdge(((E)e), ((V)sub), ((V)relay));
            }
        }
    }
}
