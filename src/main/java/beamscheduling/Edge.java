package beamscheduling;

import java.text.DecimalFormat;


public class Edge {
    public int     id;                          
    public double  length;
    public double  capacity;
    public int     type;

    public Edge(int id) {
        this.id = id;
    }
        
    public Edge() {
        this.id = -1;
    }

    public String toString() {
        DecimalFormat dec = new DecimalFormat("##.#");
        return(Integer.toString(id) + " " + dec.format(length) + " km\n " + Double.toString(capacity) + " kbps");
    }
}
