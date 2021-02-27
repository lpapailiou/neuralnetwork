package ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import neuralnet.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class NNVisualizer {

    private GraphicsContext context;
    private double width;
    private double height;
    private double radius;
    private double textLineWidth = 0.7;
    private NeuralNetwork neuralNetwork;
    private int[] activeInputNodes;
    private String[] outputNodeLabels;
    private NNColorPalette colors;
    private List<List<GraphNode>> graph = new ArrayList<>();

    public NNVisualizer(GraphicsContext context, double nodeRadius, double textLineWidth, NeuralNetwork neuralNetwork, int[] activeInputNodes, String[] outputNodeLabels, NNColorPalette colorPalette) {
        this.context = context;
        width = context.getCanvas().getWidth();
        height = context.getCanvas().getHeight();
        this.radius = nodeRadius;
        this.textLineWidth = textLineWidth;
        this.colors = colorPalette;
        this.neuralNetwork = neuralNetwork;
        this.activeInputNodes = activeInputNodes;
        this.outputNodeLabels = outputNodeLabels;
        initializeNetwork();
    }

    private void initializeNetwork() {
        graph.clear();
        int w = (int) width / neuralNetwork.getLayerCount();
        for (int i = 0; i < neuralNetwork.getLayerCount(); i++) {
            List<GraphNode> layer = new ArrayList<>();
            int h = (int) height / neuralNetwork.getConfiguration()[i];
            int hOffset = ((int) height - ((neuralNetwork.getConfiguration()[i] - 1) * h) - 20) / 2;
            for (int j = 0; j < neuralNetwork.getConfiguration()[i]; j++) {
                int offset = 0;
                GraphNode node = new GraphNode((w * i) + offset, (h * j) + hOffset);
                layer.add(node);
                if (i == 0) {
                    if (activeInputNodes[i] != 1) {
                        node.active = false;
                    }
                }
            }
            graph.add(layer);
        }
    }

    public void paintNetwork() {
        paintBackground();
        paintLines();
        paintDots();
    }

    private void paintLines() {
        for (int i = 1; i < graph.size(); i++) {
            cross(graph.get(i), graph.get(i - 1));
        }
    }

    private void cross(List<GraphNode> layer1, List<GraphNode> layer2) {
        for (GraphNode a : layer1) {
            for (GraphNode b : layer2) {
                if (a.active && b.active) {
                    paintLine(a, b);
                }
            }
        }
    }

    public void flashOutput(int flash) {
        for (int i = 0; i < graph.get(graph.size() - 1).size(); i++) {
            GraphNode node = graph.get(graph.size() - 1).get(i);
            if (i == flash) {
                paintDot(node.x, node.y, radius, colors.getFlashedNodeColor());
            } else {
                paintDot(node.x, node.y, radius);
            }
        }
    }

    private void paintDots() {
        for (int i = 0; i < graph.size(); i++) {
            for (int j = 0; j < graph.get(i).size(); j++) {
                GraphNode node = graph.get(i).get(j);
                if (node.active) {
                    paintDot(node.x, node.y, radius);
                    if (i == graph.size() - 1) {
                        context.setLineWidth(0.7);
                        context.strokeText(outputNodeLabels[i], node.x + radius * 0.5,
                                node.y - radius * 0.5);
                    }
                }
            }
        }
    }

    private void paintDot(int x, int y, double radius) {
        context.setFill(colors.getNodeColor());
        context.fillOval(x, y, radius, radius);
    }

    private void paintDot(int x, int y, double radius, Color color) {
        context.setFill(color);
        context.fillOval(x, y, radius, radius);
    }

    private void paintLine(GraphNode a, GraphNode b) {
        context.setStroke(colors.getLineColor());
        context.setLineWidth(2);
        context
                .strokeLine(a.x + (radius / 2), a.y + (radius / 2),
                        b.x + (radius / 2), b.y + (radius / 2));
    }

    private void paintBackground() {
        context.clearRect(0, 0, width, height);
        context.setFill(colors.getBackgroundColor());
        context.fillRect(0, 0, width, height);
    }


    public void setColorPalette(NNColorPalette colorPalette) {
        this.colors = colorPalette;
    }


    private static class GraphNode {
        int x;
        int y;
        boolean active = true;

        GraphNode(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
