package ch.kaiki.nn.data;

import java.util.List;

public interface IGraph {

    void addVertice(IVertice vertice);
    void addVertices(List<IVertice> vertices);
    void addEdge(IVertice a, IVertice b);
    void addEdge(IVertice a, IVertice b, double weight);
    List<IVertice> getVertices();
    List<IEdge> getEdges();
}
