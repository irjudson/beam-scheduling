package beamscheduling;

import java.awt.geom.Point2D;

import java.util.*;
import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.generators.GraphGenerator;
import edu.uci.ics.jung.graph.Graph;

public class NetworkGenerator<V,E> implements GraphGenerator<V,E> {
    public int numRelays;
    public int numSubscribers;
    public double width;
    public double height;
    public Random random;
    public Factory<Network<V,E>> networkFactory;
    public Factory<V> vertexFactory;
    public Factory<E> edgeFactory;

    /**
     * Creates an instance with the specified factories and specifications.
     * @param networkFactory the factory to use to generate the graph
     * @param vertexFactory the factory to use to create vertices
     * @param edgeFactory the factory to use to create edges
     * @param numRelays the number of vertices for the generated graph
     * @param width the width of the area the network covers
     * @param height the height of the are the network covers
     */
    public NetworkGenerator(Factory<Network<V,E>> networkFactory,
                            Factory<V> vertexFactory, Factory<E> edgeFactory, 
                            int numRelays, int numSubscribers, 
                            double width, double height) {
    	this.networkFactory = networkFactory;
    	this.vertexFactory = vertexFactory;
    	this.edgeFactory = edgeFactory;
        this.numRelays = numRelays;
        this.numSubscribers = numSubscribers;
        this.width = width;
        this.height = height;
    }

    public Network<V,E> create() {
        Network<V,E> network = null;
        network = this.networkFactory.create();
        for(int i=0; i<numRelays; i++) {
            network.addVertex(vertexFactory.create());
        }

        for(V v1: network.getVertices()) {
            for(V v2: network.getVertices()) {
                if (v1 != v2 && network.findEdge(v1,v2) == null) {
                    double dist = Point.roundTwoDecimals(((Vertex)v1).location.distance(((Vertex)v2).location));
                    // Check for connectivity & throughput
                    E edge = edgeFactory.create();
                    ((Edge)edge).length = dist;
                    network.addEdge(edge, v1, v2);
                }
            }
        }

        return network;
    }

    public Network<V,E> createCenteredRadialTree() {
        Network<V,E> network = null;
        network = this.networkFactory.create();
        network.relays = new HashSet(numRelays);
        network.subscribers = new HashSet(numSubscribers);

        // Create the root at the center
        V root = vertexFactory.create();
        network.addVertex(root);
        Vertex center = (Vertex)root;
        network.gateway = center;
        center.type = 0;
        center.location.setLocation(network.width/2, network.height/2);
        double max_radius = center.calculateRange(center.sectors);

        System.out.println("Center: " + center.location.getX() + "," + center.location.getY());
        System.out.println("Maximum Distance: " + max_radius);

        // Create the rest of the nodes
        network.relayList = new Vertex[numRelays];
        for(int i=0; i<numRelays; i++) {
            V node = vertexFactory.create();
            Vertex n = (Vertex)node;
            n.type = 1;
            double theta = (i * 360.0 / numRelays * Math.PI ) / 180.0; // changed by Brendan so that 0 and numRelays-1 don't overlap
            double radius = random.nextDouble() * max_radius;
            n.location.setLocation(center.location.getX() + (radius * Math.cos(theta)), 
                                   center.location.getY() + (radius * Math.sin(theta)));
            System.out.println("Node: " + n.id + " (" + n.location.getX() + ","+ n.location.getY() + ")");
            network.addVertex(node);
            network.relays.add(n);
            network.relayList[i] = n;
        }

        // wire up the rest of the network
        for(V vertex: network.getVertices()) {
            if (vertex != root && network.findEdge(root,vertex) == null) {
                double dist = Point.roundTwoDecimals(((Vertex)root).location.distance(((Vertex)vertex).location));
                // Check for connectivity & throughput
                E edge = edgeFactory.create();
                ((Edge)edge).type = 0;
                ((Edge)edge).length = dist;
                network.addEdge(edge, root, vertex);
            }
        }

        network.subList = new Vertex[numSubscribers];
        for(int i = 0; i < numSubscribers; i++) {
            V node = vertexFactory.create();
            Vertex v = (Vertex)node;
            v.type = 2;
            // Mark this vertex as a client.
            network.addVertex(node);
            network.subscribers.add(v);
            network.subList[i] = v;
        }
        return network;
    }

    /**
     * Sets the seed for the random number generator.
     * @param seed input to the random number generator.
     */
    public void setSeed(long seed) {
        random = new Random(seed);
        ((VertexFactory)vertexFactory).setSeed(seed);
    }
}
