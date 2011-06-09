package beamscheduling;

import java.util.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

class Greedy {

    Network network;

    public Greedy(Network<Vertex, Edge> network) {
        this.network = network;
    }

    public double solve() {
        HashMap firstLoop = new HashMap();
        int numVertices = network.getVertexCount();

        // Loop 1: Find the closest relay
        // Mark each subscriber with their preferred relay
        for (int i = 0; i < network.subList.length; i++) {
            Vertex s = network.subList[i];
            double minDist = Double.MAX_VALUE;
            for (int j = 0; j < network.relayList.length; j++) {
                Vertex r = network.relayList[j];
                double dist = s.location.distance(r.location);
                //System.out.println("distance from " + s + " to " + r + " = " + dist);
                if (dist < minDist) {
                    minDist = dist;
                    s.preferredRelay = r;
                }
            }
            //System.out.println("preferred relay for " + s + " = " + s.preferredRelay);
            // Do something with the relay
//            Edge e = new Edge();
//            e.type = 1;
//            e.length = Point.roundTwoDecimals(s.location.distance(bestRelay.location));
//            network.addEdge(e, s, bestRelay);
//            firstLoop.put(s, bestRelay);
        }

        // Loop 2: Choose the beam sets for each relay
        for (int i = 0; i < network.relayList.length; i++) {
            Vertex r = network.relayList[i];
            for (int k = 0; k < Network.thetaSet.length; k++) {
                int theta = Network.thetaSet[k];
                double bestReward = 0.0;
                for (int l = 0; l < network.beamSet[i][k].length; l++) {
                    double reward = 0.0;
                    HashSet<Vertex> beamSet = network.beamSet[i][k][l];
                    for (Vertex v : beamSet) {
                        if (v.preferredRelay == r) {
                            reward += v.queueLength * Math.min(v.queueLength, r.calculateThroughput(theta, v) * network.timeslotLength);
                        }
                    }
                    //System.out.println("r: " + r + " k: " + k + " l: " + l + "  reward = " + reward);
                    if (reward >= bestReward) {
                        bestReward = reward;
                        r.bestK = k;
                        r.bestL = l;
                    }
                }
            }
            //System.out.println("relay " + r + ": best theta " + network.thetaSet[r.bestK] + " best l " + r.bestL);

        }


        // Loop 3: Find a better Relay if there is one
        // and it's beam is covering this node
        double objectiveVal = 0.0;

        for (int i = 0; i < network.subList.length; i++) {
            Vertex s = network.subList[i];
            double maxThroughput = 0.0;
            s.preferredRelay = null;
            for (int j = 0; j < network.relayList.length; j++) {
                // Check to see if the beam intersects this subscriber
                // Otherwise none of this matters.
                Vertex r = network.relayList[j];
                HashSet<Vertex> bestBeamSet = network.beamSet[j][r.bestK][r.bestL];
                double throughput = r.calculateThroughput(network.thetaSet[r.bestK], s) * network.timeslotLength;
                if (bestBeamSet.contains(s) && throughput > maxThroughput) {
                    maxThroughput = throughput;
                    s.preferredRelay = r;
                }
            }
            objectiveVal += s.queueLength * Math.min(s.queueLength, maxThroughput);
            // Do something with the relay
            if (s.preferredRelay != null) {
                Edge e = new Edge();
                e.type = 1;
                e.length = Point.roundTwoDecimals(s.preferredRelay.calculateThroughput(network.thetaSet[s.preferredRelay.bestK], s));
                network.addEdge(e, s, s.preferredRelay);
                firstLoop.put(s, s.preferredRelay);
            }
        }

        // center beams:
        for (int i = 0; i < network.relayList.length; i++) {
            Vertex r = network.relayList[i];
            double min = 360.0;
            double max = 0.0;
            for (int j = 0; j < network.subList.length; j++) {
                Vertex s = network.subList[j];
                if (s.preferredRelay == r) {
                    min = Math.min(min, r.getBearing(s));
                    max = Math.max(max, r.getBearing(s));
                }
            }
            if (max - min <= network.thetaSet[r.bestK]) {
                r.bestBearing = (max + min) / 2.0;
            } else {
                min += 360.0;
                double b = (max + min) / 2.0;
                if (b > 360.0) {
                    b -= 360.0;
                }
                r.bestBearing = b;
            }
        }

        //System.out.println("Solved the greedy problem, overall throughput = " + overallThroughput);
        return objectiveVal;
    }
}
