package ch.kaiki.nn.data;

import java.util.List;

public interface IVertice {

    void addEdge(IEdge edge);
    void addEdges(List<IEdge> edges);
    void setEdges(List<IEdge> edges);
    void clearEdges();
    List<IEdge> getEdges();
    double getX();
    double getY();
    double getZ();
    double[] getCoordinates();
    String getName();
    boolean isVisited();
    boolean isHighlighted();
    void setVisited(boolean visited);
    void setHighlighted(boolean highlighted);
}
