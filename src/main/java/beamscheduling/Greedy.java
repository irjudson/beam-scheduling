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
        HashMap firstLoop = new HashMap();
        int numVertices = network.getVertexCount();

        // Loop 1: Find the closest relay
        for(int i = 0; i < network.subList.length; i++) {
            Vertex s = network.subList[i];
            double minDist = 1000000.0;
            double maxThroughput = 0.0;
            Vertex bestRelay = null;
            for(int j = 0; j < network.relayList.length; j++) {
                Vertex r = network.relayList[j];
                double dist = s.location.distance(r.location);
                if (dist < minDist) {
                    minDist = dist;
                    maxThroughput = s.calculateThroughput(20, r);
                    bestRelay = r;
                }
            }
            // Do something with the relay
            Edge e = new Edge();
            e.type = 1;
            e.length = Point.roundTwoDecimals(s.location.distance(bestRelay.location));
            network.addEdge(e, s, bestRelay);
            firstLoop.put(s, bestRelay);
        }

        // Loop 2: Calculate the greedy reward
        network.calculateBeamSet(1, 20);

        // Loop 3: Find a better Relay if there is one
        for(int i = 0; i < network.subList.length; i++) {
            Vertex s = network.subList[i];
            double maxThroughput = 0.0;
            Vertex bestRelay = null;
            for(int j = 0; j < network.relayList.length; j++) {
                Vertex r = network.relayList[j];
                double throughput = s.calculateThroughput(20, r);
                if (throughput > maxThroughput) {
                    maxThroughput = throughput;
                    bestRelay = r;
                }
            }
            // Do something with the relay
            Edge e = new Edge();
            e.type = 3;
            e.length = Point.roundTwoDecimals(s.location.distance(bestRelay.location));
            network.addEdge(e, s, bestRelay);
            firstLoop.put(s, bestRelay);
        }

        System.out.println("Solved the greedy problem.");
    }
}
