package beamscheduling;

import java.util.*;
import edu.uci.ics.jung.graph.Graph;

class LPRound {
    Network network;

    public LPRound(Network<Vertex,Edge> network) {
        this.network = network;
    }

    public void solve() {
        System.out.println("Solved the lpround problem.");
    }
}
