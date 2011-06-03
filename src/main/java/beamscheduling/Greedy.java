package beamscheduling;

import java.util.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

class Greedy {

    Network network;

    public Greedy(Network<Vertex, Edge> network) {
        this.network = network;
    }

    public void solve() {
        HashMap firstLoop = new HashMap();
        int numVertices = network.getVertexCount();

        // Loop 1: Find the closest relay
        // Mark each subscriber with their preferred relay
        for (int i = 0; i < network.subList.length; i++) {
            Vertex s = network.subList[i];
            double minDist = 1000000.0;
            double maxThroughput = 0.0;
            Vertex bestRelay = null;
            for (int j = 0; j < network.relayList.length; j++) {
                Vertex r = network.relayList[j];
                double dist = s.location.distance(r.location);
                if (dist < minDist) {
                    minDist = dist;
                    maxThroughput = s.calculateThroughput(20, r);
                    bestRelay = r;
                    s.preferredRelay = r;
                }
            }
            // Do something with the relay
            Edge e = new Edge();
            e.type = 1;
            e.length = Point.roundTwoDecimals(s.location.distance(bestRelay.location));
            network.addEdge(e, s, bestRelay);
            firstLoop.put(s, bestRelay);
        }

        // Loop 2: Choose the beam sets for each relay
        for (int i = 0; i < network.relayList.length; i++) {
            Vertex r = network.relayList[i];
            for (int k = 0; k < network.thetaSet.length; k++) {
                int theta = network.thetaSet[k];
                double bestReward = 0.0;
                for (int l = 0; l < network.beamSet[i][k].length; l++) {
                    double reward = 0.0;
                    HashSet<Vertex> beamSet = network.beamSet[i][k][l];
                    for (Vertex v : beamSet) {
                        if (v.preferredRelay == r) {
                            reward += r.calculateThroughput(theta, v);
                        }
                    }
                    System.out.println("r: " + r + " k: " + k + " l: " + l + "  reward = " + reward);
                    if (reward >= bestReward) {
                        bestReward = reward;
                        r.bestK = k;
                        r.bestL = l;
                    }
                }
            }
            System.out.println("relay " + r + ": best theta " + network.thetaSet[r.bestK] + " best l " + r.bestL);
            
            // center beams:
            HashSet<Vertex> bestBeamSet = network.beamSet[i][r.bestK][r.bestL];
            double min = 360.0;
            double max = 0.0;
            for (Vertex v : bestBeamSet) {
                min = Math.min(min, r.getBearing(v));
                max = Math.max(max, r.getBearing(v));
            }
            if (max - min <= network.thetaSet[r.bestK]) {
                r.bestBearing = (max + min) / 2.0;
            } else {
                min += 360.0;
                double b = (max + min) / 2.0;
                if (b > 360.0)
                    b -= 360.0;
                r.bestBearing = b;
            }
        }


        // Loop 3: Find a better Relay if there is one
        // and it's beam is covering this node
        double overallThroughput = 0.0;
        
        for (int i = 0; i < network.subList.length; i++) {
            Vertex s = network.subList[i];
            double maxThroughput = 0.0;
            Vertex bestRelay = null;
            for (int j = 0; j < network.relayList.length; j++) {
                // Check to see if the beam intersects this subscriber
                // Otherwise none of this matters.
                Vertex r = network.relayList[j];
                HashSet<Vertex> bestBeamSet = network.beamSet[j][r.bestK][r.bestL];                
                double throughput = r.calculateThroughput(network.thetaSet[r.bestK], s);
                if (bestBeamSet.contains(s) && throughput > maxThroughput) {
                    maxThroughput = throughput;
                    bestRelay = r;
                }
            }
            overallThroughput += maxThroughput;
//            // Do something with the relay
//            if (bestRelay != null) {
//                Edge e = new Edge();
//                e.type = 3;
//                e.length = Point.roundTwoDecimals(s.location.distance(bestRelay.location));
//                network.addEdge(e, s, bestRelay);
//                firstLoop.put(s, bestRelay);
//            }
        }

        System.out.println("Solved the greedy problem, overall throughput = " + overallThroughput);
    }
}
