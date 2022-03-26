package ch.kaiki.nn.data;

public class Edge implements IEdge {

    private String name;
    private IVertice from;
    private IVertice to;
    private double weight;
    private boolean visited;
    private boolean highlighted;


    public Edge(IVertice a, IVertice b, String name) {
        this(a, b);
        this.name = name;
    }

    public Edge(IVertice a, IVertice b, double weight) {
        this(a, b);
        this.weight = weight;
    }

    public Edge(IVertice a, IVertice b, double weight, String name) {
        this(a, b);
        this.weight = weight;
        this.name = name;
    }

    public Edge(IVertice a, IVertice b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("An Edge must connect two vertices!");
        }
        this.from = a;
        this.to = b;
        from.addEdge(this);
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
    @Override
    public double getWeight() {
        return weight;
    }
    @Override
    public IVertice getFrom() {
        return from;
    }
    @Override
    public IVertice getTo() {
        return to;
    }
}
