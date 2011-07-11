package beamscheduling;

import java.util.*;

import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.decorators.*;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;

public class Network<V, E>
    extends UndirectedSparseGraph<V, E>
    implements Graph<V, E> {

    final public double width;
    final public double height;
    public Vertex gateway;
    public HashSet relays;
    public HashSet subscribers;
    public Vertex[] relayList;
    public Vertex[] subList;
    public static double meanQueueLength;
    public static double timeslotLength;
    public static int[] thetaSet = new int[1]; // Brendan added, for now just two theta kept
    public HashSet<Vertex>[][][] beamSet; // beamSet[i][k][l] = lth beam set for relay i, theta k
    public static int numChannels;
    public Random random;

    public Network(double width, double height, int theta, int channels) {
        this.width = width;
        this.height = height;
        this.thetaSet[0] = theta;
    }

    /**
     * Returns a {@code Generator} that creates an instance of this graph type.
     * @param <V> the vertex type for the graph factory
     * @param <E> the edge type for the graph factory
     */
    public static NetworkGenerator getGenerator(int numRelays, int numSubscribers, int sectors, double width, double height, long seed, int theta, double meanq, double slotlen, int channels) {
        NetworkGenerator gen = new NetworkGenerator(new NetworkFactory(width, height, theta, channels),
                                                    new VertexFactory(width, height, sectors, meanq),
                                                    new EdgeFactory(), numRelays, numSubscribers, width, height);
        gen.setSeed(seed);
        thetaSet[0] = theta;
        meanQueueLength = meanq;
        timeslotLength = slotlen;
        numChannels = channels;
        return (gen);
    }

    public static NetworkGenerator getGenerator(int relays, int subscribers, 
                                                double width, double height, 
                                                long seed, int channels) {
        NetworkGenerator gen = new NetworkGenerator(new NetworkFactory(width, 
                                                                       height, 
                                                                       channels),
                                                    new VertexFactory(width, height),
                                                    new EdgeFactory(), relays, subscribers, width, height);
        gen.setSeed(seed);
        numChannels = channels;
        return (gen);
    }

    void draw(int width, int height, String name) {
        Transformer<Edge, Stroke> edgeDraw = new Transformer<Edge, Stroke>() {

            public Stroke transform(Edge e) {
                if (e.type == 0) {
                    return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
                } else if (e.type == 1) {
                    float dash[] = {5.0f};
                    return new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                } else if (e.type == 2) {
                    float dash[] = {10.0f};
                    return new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                } else if (e.type == 3) {
                    float dash[] = {15.0f};
                    return new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                } else {
                    float dash[] = {20.0f};
                    return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                }
            }
        };

        Transformer<Vertex, Paint> vertexPaint = new Transformer<Vertex, Paint>() {

            public Paint transform(Vertex v) {
                if (v.type == 0) {
                    return Color.YELLOW;
                } else if (v.type == 1) {
                    return Color.CYAN;
                } else if (v.type == 2) {
                    return Color.GREEN;
                } else if (v.type == 3) {
                    return Color.RED;
                } else {
                    return Color.YELLOW;
                }
            }
        };

        Transformer<Edge, Paint> edgePaint = new Transformer<Edge, Paint>() {
            public Paint transform(Edge e) {
                if (e.type == 0) {
                    return Color.BLACK;
                } else {
                    return Color.RED;
                }
            }
        };

        System.setProperty("com.apple.mrj.application.apple.menu.about.name", 
                           "Wireless Mesh Network Simulation");
        JFrame jf = new JFrame(name);
        final Dimension layoutSize = new Dimension(width, height);
        final Layout layout = new StaticLayout(this, new NetworkTransformer(layoutSize, this.width, this.height), layoutSize);
        final VisualizationViewer vv = new VisualizationViewer(layout, layoutSize);

        final float scale_x = (float) ((layoutSize.getWidth() * 0.95) / this.width);
        final float scale_y = (float) ((layoutSize.getHeight() * 0.95) / this.height);

        vv.addPostRenderPaintable(new VisualizationViewer.Paintable() {

                public boolean useTransform() {
                    return true;
                }

                public void paint(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;

                    // Draw the beams
                    if (relays != null) {
                        Iterator itr = relays.iterator();
                        while (itr.hasNext()) {
                            Vertex relay = (Vertex) itr.next();
                            int theta = thetaSet[relay.bestK];
                            int half_theta = theta / 2;


                            double length = relay.calculateRange(360 / theta);
                            double b = (relay.bestBearing * Math.PI) / 180.0;
                            double bm = ((relay.bestBearing - half_theta) * Math.PI) / 180.0;
                            double bp = ((relay.bestBearing + half_theta) * Math.PI) / 180.0;
                            float old_x = (float) relay.location.getX() * scale_x;
                            float old_y = (float) relay.location.getY() * scale_y;
                            float u_x = ((float) relay.location.getX() + ((float) (length * Math.sin(bp)))) * scale_x;
                            float u_y = ((float) relay.location.getY() + ((float) (length * Math.cos(bp)))) * scale_y;
                            float l_x = ((float) relay.location.getX() + ((float) (length * Math.sin(bm)))) * scale_x;
                            float l_y = ((float) relay.location.getY() + ((float) (length * Math.cos(bm)))) * scale_y;

                            GeneralPath beamShape = new GeneralPath();
                            beamShape.moveTo(old_x, old_y);
                            beamShape.lineTo(u_x, u_y);
                            beamShape.lineTo(l_x, l_y);
                            beamShape.lineTo(old_x, old_y);

                            Color old = g2.getColor();

                            g2.setColor(Color.RED);
                            g2.draw(new Line2D.Double(old_x, old_y, u_x, u_y));
                            g2.draw(new Line2D.Double(old_x, old_y, l_x, l_y));

                            g2.setColor(old);
                        }
                    }
                }
            });

        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
        vv.getRenderContext().setEdgeStrokeTransformer(edgeDraw);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

        jf.getContentPane().add(vv);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);

    }

    public Vertex randomSub() {
        return this.subList[this.random.nextInt(this.subList.length)];
    }

    public void calculateBeamSets() {
        int numRelays = relayList.length;
        int numSubs = subList.length;
        this.beamSet = new HashSet[numRelays][this.thetaSet.length][];

        for (int i = 0; i < relayList.length; i++) {
            Vertex relay = relayList[i];

            //System.out.println("computing beam sets for relay " + i);

            for (int k = 0; k < this.thetaSet.length; k++) {
                BearingSub[] sortedSubs = new BearingSub[numSubs];
                int ns = 0;
                for (int j = 0; j < subList.length; j++) {
                    Vertex sub = subList[j];
                    double bearing = relay.getBearing(sub);
                    if (relay.calculateThroughput(this.thetaSet[k], sub) > 0) {
                        sortedSubs[ns++] = new BearingSub(bearing, sub);
                    }
                }
                sortedSubs = Arrays.copyOf(sortedSubs, ns);
                Arrays.sort(sortedSubs);

                this.beamSet[i][k] = new HashSet[0];
                ArrayList<HashSet<Vertex>> tmp = new ArrayList();
                for (int start = 0; start < sortedSubs.length; start++) {
                    HashSet<Vertex> nextSet = new HashSet<Vertex>();
                    int end;
                    end = start;
                    double endBearing = sortedSubs[end].bearing;
                    //System.out.println("start bearing: " + sortedSubs[start].bearing);
                    while (endBearing - sortedSubs[start].bearing <= this.thetaSet[k]) {
                        //System.out.println("end bearing: " + sortedSubs[end].bearing);
                        nextSet.add(sortedSubs[end].sub);
                        end = (end + 1) % sortedSubs.length;
                        if (end == start)
                            break;
                        endBearing = sortedSubs[end].bearing;
                        if (endBearing < sortedSubs[start].bearing) {
                            endBearing += 360.0;
                        }
                    }
                    if (tmp.isEmpty() || !tmp.get(tmp.size() - 1).containsAll(nextSet)) {
                        tmp.add(nextSet);
                        //System.out.println("beamSet[" + i + "][" + k + "] adding " + nextSet);
                    }
                }
                this.beamSet[i][k] = tmp.toArray(this.beamSet[i][k]);
            }
        }
    }
}

class BearingSub implements Comparable {

    double bearing;
    Vertex sub;

    public BearingSub(double b, Vertex s) {
        this.bearing = b;
        this.sub = s;
    }

    public int compareTo(Object other) {
        BearingSub otherBS = (BearingSub) other;
        return Double.compare(this.bearing, otherBS.bearing);
    }
}
