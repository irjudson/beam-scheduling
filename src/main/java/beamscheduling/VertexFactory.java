package beamscheduling;

import java.util.*;
import org.apache.commons.collections15.Factory;

class VertexFactory implements Factory<Vertex> {
    int count;
    double width;
    double height;
    int sectors;
    Random generator;

    public VertexFactory(double width, double height, int sectors) {
        this.width = width;
        this.height = height;
        this.sectors = sectors;
        generator = new Random();
    }
                             
    public Vertex create() {
        double x = Point.roundTwoDecimals(generator.nextDouble());
        double y = Point.roundTwoDecimals(generator.nextDouble());
        return new Vertex(count++, this.sectors, x * this.width, y * this.height);
    }

    public long setSeed(long seed) {
        generator = new Random(seed);
        return seed;
    }
}

 
