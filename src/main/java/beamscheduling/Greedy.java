package beamscheduling;

import java.util.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

class Greedy {
    Network network;

    public Greedy(Network<Vertex,Edge> network) {
        this.network = network;
    }

    public void solve() {
        HashSet firstLoop = new HashSet();
        int numVertices = network.getVertexCount();
        network.calculateBeamSet(1, 20);

        // Loop 1: 
        for(int i = 0; i < network.subList.length; i++) {
            Vertex s = network.subList[i];
            double maxThroughput = 0.0;
            Vertex bestRelay = null;
            for(int j = 0; j < network.relayList.length; j++) {
                Vertex r = network.relayList[j];
                double throughput = s.calculateThroughput(20, r);
                if (throughput >= maxThroughput) {
                    maxThroughput = throughput;
                    bestRelay = r;
                }
            }
            // Do something with the relay
            firstLoop.put(s, bestRelay);
        }

        // Loop 2: Calculate the greedy reward
        

        // Loop 3: 
        System.out.println("Solved the greedy problem.");
    }
}
