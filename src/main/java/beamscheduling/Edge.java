package beamscheduling;

public class Edge {
    public Edge(int id) {
        this.id = id;
    }
        
    public int     id;                          
    public double  length;
    public double  capacity;

    public String toString() {
        //        StringBuilder result = new StringBuilder();
        //        result.append("Vertex: " + id);
        //        result.append(" X: " + location.getX());
        //        result.append(" Y: " + location.getY());
        //        return result.toString();
        return Double.toString(length);
    }
}
