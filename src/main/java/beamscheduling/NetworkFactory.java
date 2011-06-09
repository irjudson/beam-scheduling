package beamscheduling;

import org.apache.commons.collections15.Factory;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

class NetworkFactory implements Factory<Network> {
    double width;
    double height;
    int theta;

    public NetworkFactory(double width, double height, int theta) {
        this.width = width;
        this.height = height;
        this.theta = theta;
    }

    public Network create() {
        return new Network(this.width, this.height, this.theta);
    }
}
