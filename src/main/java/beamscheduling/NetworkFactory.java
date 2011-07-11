package beamscheduling;

import org.apache.commons.collections15.Factory;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

class NetworkFactory implements Factory<Network> {
    double width;
    double height;
    int theta;
    int channels;

    public NetworkFactory(double width, double height, int theta, int channels)
        {
            this.width = width;
            this.height = height;
            this.theta = theta;
            this.channels = channels;
        }

    public NetworkFactory(double width, double height, int channels)
        {
            this.width = width;
            this.height = height;
            this.channels = channels;
            this.theta = 1;
        }

    public Network create() {
        return new Network(this.width, this.height, this.theta, this.channels);
    }
}
