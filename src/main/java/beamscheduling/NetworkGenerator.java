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
        network.relays = new HashSet(numRelays);
        network.subscribers = new HashSet(numSubscribers);

        network.relayList = new Vertex[numRelays];
        for(int i=0; i<numRelays; i++) {
            V node = vertexFactory.create();
            Vertex v = (Vertex)node;
            network.addVertex(node);
            network.relays.add(v);
            network.relayList[i] = v;
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

        for(Vertex v1: network.relayList) {
            for(Vertex v2: network.relayList) {
                if (v1 != v2 && network.findEdge((V)v1, (V)v2) == null) {
                    double tp = v1.calculateThroughput(v2);
                    if (tp > 0.0) {
                        double dist = Point.roundTwoDecimals(v1.distanceTo(v2));
                        E e = edgeFactory.create();
                        Edge edge = (Edge)e;
                        edge.capacity = tp;
                        edge.length = dist;
                        edge.channels = new Double[network.numChannels * 3];
                        // -1 means never initialized for use
                        // 0 means zero'd for interference
                        // > 0 means active (1) or effective throughput
                        for(int i = 0; i < network.numChannels * 3; i++ ) {
                            edge.channels[i] = -1.0d;
                        }

                        // Randomly assign k channels from 3*k choices
                        int n = 0;
                        while(n < network.numChannels) {
                            int nextIdx = random.nextInt(network.numChannels*3);
                            if (edge.channels[nextIdx] == -1.0d) {
                                edge.channels[nextIdx] = 1.0d;
                                n += 1;
                            }
                        }

                        // Calculate throughput for each active channel
                        int freq = -1;
                        for (int i = 0; i < network.numChannels*3; i++) {
                            if(i % network.numChannels == 0) {
                                if (freq == -1) {
                                    freq = 700;
                                } else if (freq == 700) {
                                    freq = 2400;
                                } else if (freq == 2400) {
                                    freq = 5800;
                                }
                            }

                            if (edge.channels[i] == 1.0f) {
                                edge.channels[i] = edge.lookupThroughput(freq);
                            }
                        }

                        network.addEdge(e, (V)v1, (V)v2);
                    }
                }
            }
        }

        // Calculate interference and zero out channels
        for(Vertex v1: network.subList) {
            v1.interferenceChannel = random.nextInt(network.numChannels*3);
            double range = 0.0d;
            if (v1.interferenceChannel > 0 
                && v1.interferenceChannel < network.numChannels) {
                range = 30.8;
            } else if (v1.interferenceChannel > 0 + network.numChannels &&
                       v1.interferenceChannel < 2 * network.numChannels) {
                range = 9.0;
            } else if (v1.interferenceChannel > 0 + 2 * network.numChannels &&
                       v1.interferenceChannel < 3 * network.numChannels) {
                range = 3.6;
            }
            for (Vertex v2: network.relayList) {
                if (v1.distanceTo(v1) < range) {
                    for(E e: network.getIncidentEdges((V)v2)) {
                        ((Edge)e).channels[v1.interferenceChannel] = 0.0d;
                    }
                }
            }
        }

        Vector junk = new Vector();
        for(Vertex v: network.relayList) {
            for(E e: network.getIncidentEdges((V)v)) {
                Boolean remove = true;
                for(int i = 0; i < network.numChannels; i++) {
                    if (((Edge)e).channels[i] > 0.0) {
                        remove = false;
                    }
                }
                if(remove) {
                    junk.add(e);
                }
            }
        }
        
        Iterator itr = junk.iterator();
        while(itr.hasNext()) {
            E e = (E)itr.next();
            network.removeEdge(e);
        }

        network.random = random;
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

        // Create the rest of the nodes
        network.relayList = new Vertex[numRelays];
        for(int i=0; i<numRelays; i++) {
            V node = vertexFactory.create();
            Vertex n = (Vertex)node;
            n.type = 1;
            // changed by Brendan so that 0 and numRelays-1 don't overlap
            double theta = (i * 360.0 / numRelays * Math.PI ) / 180.0; 
            double radius = random.nextDouble() * max_radius;
            n.location.setLocation(center.location.getX() + (radius * Math.cos(theta)), 
                                   center.location.getY() + (radius * Math.sin(theta)));
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
        network.random = random;
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
