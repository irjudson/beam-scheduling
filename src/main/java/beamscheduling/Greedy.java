package beamscheduling;

import java.util.*;
import edu.uci.ics.jung.graph.Graph;

class Greedy {
    Network network;

    public Greedy(Network<Vertex,Edge> network) {
        this.network = network;
    }

    public void solve() {
        int numVertices = network.getVertexCount();

        // Loop 1: 
        for(int i = 1; i < numVertices; i++) {
        }

        // Loop 2: Calculate the greedy reward
        

        // Loop 3: 
        System.out.println("Solved the greedy problem.");
    }
}
