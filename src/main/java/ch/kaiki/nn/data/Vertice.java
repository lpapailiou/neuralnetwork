package ch.kaiki.nn.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Vertice implements IVertice {

    private double x;
    private double y;
    private double z;
    private String name;
    private List<IEdge> edges = new ArrayList<>();
    private boolean visited;
    private boolean highlighted;

    public Vertice(double x, double y, double z, String name) {
        this(x, y, z);
        this.name = name;
    }

    public Vertice(double x, double y, String name) {
        this(x, y);
        this.name = name;
    }

    public Vertice(double x, double y, double z) {
        this(x, y);
        this.z = z;
    }

    public Vertice(double x, double y) {
        this.x = x;
        this.y = y;
    }

    private Vertice() {}

    @Override
    public void addEdge(IEdge edge) {
        edges.add(edge);
    }

    public void addEdges(List<IEdge> edges) {
        this.edges.addAll(edges);
    }

    public void setEdges(List<IEdge> edges) {
        if (edges != null) {
            this.edges = edges;
        } else {
            this.edges.clear();
        }
    }
    @Override
    public void clearEdges() {
        this.edges.clear();
    }
    @Override
    public List<IEdge> getEdges() {
        return new ArrayList<>(edges);
    }
    @Override
    public double getX() {
        return x;
    }
    @Override
    public double getY() {
        return y;
    }
    @Override
    public double getZ() {
        return z;
    }
    @Override
    public double[] getCoordinates() {
        return new double[]{x, y, z};
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public boolean isVisited() {
        return visited;
    }
    @Override
    public boolean isHighlighted() {
        return highlighted;
    }
    @Override
    public void setVisited(boolean visited) {
        this.visited = visited;
    }
    @Override
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }


}
