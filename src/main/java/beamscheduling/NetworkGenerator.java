package beamscheduling;

import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.generators.GraphGenerator;
import edu.uci.ics.jung.graph.Graph;

public class NetworkGenerator<V,E> implements GraphGenerator<V,E> {
    private int mNumVertices;
    private double mWidth;
    private double mHeight;
    private Random mRandom;
    private Factory<Network<V,E>> networkFactory;
    private Factory<V> vertexFactory;
    private Factory<E> edgeFactory;

    /**
     * Creates an instance with the specified factories and specifications.
     * @param networkFactory the factory to use to generate the graph
     * @param vertexFactory the factory to use to create vertices
     * @param edgeFactory the factory to use to create edges
     * @param numVertices the number of vertices for the generated graph
     * @param width the width of the area the network covers
     * @param height the height of the are the network covers
     */
    public NetworkGenerator(Factory<Network<V,E>> networkFactory,
    		Factory<V> vertexFactory, Factory<E> edgeFactory, 
                int numVertices, double width, double height) {
    	this.networkFactory = networkFactory;
    	this.vertexFactory = vertexFactory;
    	this.edgeFactory = edgeFactory;
        mNumVertices = numVertices;
        mWidth = width;
        mHeight = height;
    }

    public Network<V,E> create() {
        Network<V,E> network = null;
        network = this.networkFactory.create();
        for(int i=0; i<mNumVertices; i++) {
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

    /**
     * Sets the seed for the random number generator.
     * @param seed input to the random number generator.
     */
    public void setSeed(long seed) {
        mRandom = new Random(seed);
        ((VertexFactory)vertexFactory).setSeed(seed);
    }
}
