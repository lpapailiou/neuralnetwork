package ch.kaiki.nn.data;

public interface IEdge {

    String getName();
    boolean isVisited();
    boolean isHighlighted();
    void setVisited(boolean visited);
    void setHighlighted(boolean highlighted);
    double getWeight();
    IVertice getFrom();
    IVertice getTo();
}
