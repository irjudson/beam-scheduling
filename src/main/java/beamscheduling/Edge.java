package beamscheduling;

public class Edge {
    public Edge(int id) {
        this.id = id;
    }
        
    public Edge() {
        this.id = -1;
    }

    public int     id;                          
    public double  length;
    public double  capacity;
    public int     type;

    public String toString() {
        return Double.toString(length);
    }
}
