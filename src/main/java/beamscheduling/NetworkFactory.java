package beamscheduling;

import org.apache.commons.collections15.Factory;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

class NetworkFactory implements Factory<Network> {
    double width;
    double height;

    public NetworkFactory(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public Network create() {
        return new Network(this.width, this.height);
    }
}
