package ch.kaiki.nn.data;

import java.util.ArrayList;
import java.util.List;

public class Graph implements IGraph {

    private String name;
    boolean bidirectional = true;
    private List<IVertice> vertices = new ArrayList<>();
    private List<IEdge> edges = new ArrayList<>();

    public Graph() {
    }

    public Graph(String name) {
        this.name = name;
    }

    @Override
    public void addVertice(IVertice vertice) {
        this.vertices.add(vertice);
    }

    @Override
    public void addVertices(List<IVertice> vertices) {
        this.vertices.addAll(vertices);
    }

    @Override
    public void addEdge(IVertice a, IVertice b) {
        Edge edge = new Edge(a, b);
        edges.add(edge);
         if (bidirectional) {
            new Edge(b, a);
        }
    }

    @Override
    public void addEdge(IVertice a, IVertice b, double weight) {
        new Edge(a, b, weight);
        if (bidirectional) {
            new Edge(b, a, weight);
        }
    }

    @Override
    public List<IVertice> getVertices() {
        return new ArrayList<>(vertices);
    }

    @Override
    public List<IEdge> getEdges() {
        return new ArrayList<>(edges);
    }

    public String getName() {
        return name;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }
}
