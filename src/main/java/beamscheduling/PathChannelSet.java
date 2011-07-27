package beamscheduling;

import java.util.*;

public class PathChannelSet {
    public ArrayList<EdgeChannelSet> path;
    public ChannelSelection.PathCS pcs;

    public PathChannelSet(ArrayList<Edge> p) {
        this.path = new ArrayList<EdgeChannelSet>();
        for(Object o: p) {
            this.path.add(new EdgeChannelSet((Edge)o, new Vector()));
        }
    }

    public String toString() {
        String out = "";
        for(Object o: path) {
            out += ((EdgeChannelSet)o).toString();
        }
        return(out);
    }
    
    public ArrayList<Edge> getPath() {
        ArrayList<Edge> path = new ArrayList<Edge>();
        for(Object o: path) {
            path.add(((EdgeChannelSet)o).edge);
        }
        return(path);
    }
}

