package ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import neuralnet.NeuralNetwork;
import ui.color.NNGraphColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static javafx.scene.paint.Color.*;

/**
 * With this class, the NeuralNetwork from this library can be visualized with the javafx framework.
 * It is not only a representation as graph of a specific NeuralNetwork instance, as it is able to display the
 * node values of the last prediction and the layer weights by different colors. There are three colors each
 * to support this functionality (below negative threshold, above positive threshold and in between).
 * Thresholds can be chosen individually.
 * The graph will be triggered automatically as soon as the linked NeuralNetwork will make a prediction.
 * Further parameters concerning graphical representation (color palette, width offset, input and output node
 * labels, etc.) may be set by according setters. Chaining is supported.
 */

public class NNGraph {

    private GraphicsContext context;
    private double initialWidth;
    private double initialHeight;
    private double width;
    private double height;
    private double wOffsetLeft;
    private double hOffsetTop;
    private boolean dynamicGrowth = true;
    private double radius = 16;
    private double lineWidth = 2;
    private double lowerNodeThreshold = 0.3;
    private double uppweNodeThreshold = 0.7;
    private double lowerWeightThreshold = -0.2;
    private double upperWeightThreshold = 0.2;
    private NeuralNetwork neuralNetwork;
    private int[] activeInputNodes;
    private String[] inputNodeLabels;
    private String[] outputNodeLabels;
    private NNGraphColor colors = new NNGraphColor(WHITESMOKE, BLACK, BLACK, ROYALBLUE.brighter(), GAINSBORO, STEELBLUE, INDIANRED, STEELBLUE.darker(), INDIANRED.darker());
    private List<List<GraphNode>> graph = new ArrayList<>();
    private List<List<Double>> nodeValues = new ArrayList<>();

    /**
     * The constructor will prepare the graph. Initially, the display will be blank. It will update as soon
     * as a neural network instance is set.
     * @param context the javafx GraphicsContext of a Canvas instance.
     */
    public NNGraph(GraphicsContext context) {
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null!");
        }
        this.context = context;
        initialWidth = context.getCanvas().getWidth();
        width = initialWidth;
        initialHeight = context.getCanvas().getHeight();
        height = initialHeight;
    }

    private void buildGraph() {
        graph.clear();
        if (neuralNetwork == null) {
            paintBackground();
            return;
        }
        int[] configuration = neuralNetwork.getConfiguration();
        double calcW = width / (configuration.length - (dynamicGrowth ? 0 : 1));
        for (int i = 0; i < configuration.length; i++) {
            List<GraphNode> layer = new ArrayList<>();
            int indicatorLayerSize = (i == 0) ? activeInputNodes.length : configuration[i];
            double h = height / indicatorLayerSize;
            double hOffset = (height - ((indicatorLayerSize - 1) * h) - 20) / 2;

            for (int j = 0; j < indicatorLayerSize; j++) {
                GraphNode node = new GraphNode(((calcW * i) + wOffsetLeft), (h * j) + hOffset + hOffsetTop);
                layer.add(node);
                if (i == 0) {
                    if (activeInputNodes[j] != 1) {
                        node.active = false;
                    }
                }
            }
            graph.add(layer);
        }
        paintNetwork();
    }

    private void paintNetwork() {
        paintBackground();
        paintLines();
        paintDots();
    }

    private void paintLines() {
        for (int i = 1; i < graph.size(); i++) {
            cross(graph.get(i), graph.get(i - 1), i);
        }
    }

    private void cross(List<GraphNode> layer1, List<GraphNode> layer2, int layerIndex) {
        for (int i = 0; i < layer1.size(); i++) {
            int nodesSkipped = 0;
            double[][] layerWeights = neuralNetwork.getWeights(layerIndex-1);
            for (int j = 0; j < layer2.size(); j++) {
                GraphNode a = layer1.get(i);
                GraphNode b = layer2.get(j);
                if (a.active && b.active) {
                    paintLine(a, b, layerWeights[i][j-nodesSkipped]);
                } else {
                    nodesSkipped++;
                }
            }
        }
    }

    private void paintDots() {
        for (int i = 0; i < graph.size(); i++) {
            int skippedNodes = 0;
            for (int j = 0; j < graph.get(i).size(); j++) {
                GraphNode node = graph.get(i).get(j);
                Color color = colors.getNodeColor();
                context.setFill(colors.getLineColor());

                if (i == 0) {
                    context.setTextAlign(TextAlignment.RIGHT);
                    context.fillText(inputNodeLabels[j], node.x + radius * 0.5,node.y - radius * 0.5);
                }

                if (node.active) {
                    if (!nodeValues.isEmpty()) {
                        color = evaluateColor(nodeValues.get(i).get(j-skippedNodes), lowerNodeThreshold, uppweNodeThreshold, color, true);
                    }

                    if (i == graph.size() - 1) {
                        if (!nodeValues.isEmpty()) {
                            double max = Collections.max(nodeValues.get(i));
                            if (nodeValues.get(i).get(j - skippedNodes) == max) {
                                Color flashColor = colors.getFlashedNodeColor();
                                if (flashColor != TRANSPARENT) {
                                    color = flashColor;
                                }
                            }
                        }
                        context.setTextAlign(TextAlignment.LEFT);
                        context.fillText(outputNodeLabels[j], node.x + radius * 0.5,node.y - radius * 0.5);
                    }
                } else {
                    color = colors.getInactiveInputNodeColor();
                    skippedNodes++;
                }
                paintDot(node.x, node.y, radius, color);
            }
        }
    }

    private void paintDot(double x, double y, double radius, Color color) {
        context.setFill(color);
        context.fillOval(x, y, radius, radius);
    }

    private void paintLine(GraphNode a, GraphNode b, double value) {
        Color color = evaluateColor(value, lowerWeightThreshold, upperWeightThreshold, colors.getLineColor(), false);
        context.setStroke(color);
        context.setLineWidth(lineWidth);
        context.strokeLine(a.x + (radius / 2), a.y + (radius / 2), b.x + (radius / 2), b.y + (radius / 2));
    }

    private void paintBackground() {
        context.clearRect(0, 0, initialWidth, initialHeight);
        context.setFill(colors.getBackgroundColor());
        context.fillRect(0, 0, initialWidth, initialHeight);
    }

    private Color evaluateColor(double value, double negativeThreshold, double positiveThreshold, Color color, boolean isNode) {
        if (value < negativeThreshold) {
            color = isNode ? colors.getLowerAccentNodeColor() : colors.getLowerAccentWeightColor();
        } else if (value >= positiveThreshold) {
            color = isNode ? colors.getUpperAccentNodeColor() : colors.getUpperAccentWeightColor();
        }
        return color;
    }

    /**
     * This method will update the visualization (i.e. clear the cached node values).
     * It may be used during a training of the neural network or other actions, where the preceding node values
     * are not relevant anymore.
     */
    public void refresh() {
        nodeValues.clear();
        paintNetwork();
    }

    /**
     * Method to set a new neural network instance to be represented visually.
     * @param neuralNetwork the neural network instance to be visualized.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setNeuralNetwork(NeuralNetwork neuralNetwork) {
        nodeValues.clear();
        if (neuralNetwork == null) {
            throw new NullPointerException("NeuralNetwork must not be null!");
        }
        this.neuralNetwork = neuralNetwork;
        int[] configuration = neuralNetwork.getConfiguration();
        int[] inNodes = new int[configuration[0]];
        Arrays.fill(inNodes, 1);
        int activeNodes = (!graph.isEmpty()) ? (int) graph.get(0).stream().filter(n -> (n.active)).count() : 0;
        if (activeInputNodes == null || activeNodes != inNodes.length) {
            this.activeInputNodes = inNodes;
        }

        int outNodes = configuration[configuration.length-1];
        if (inputNodeLabels == null || (!graph.isEmpty() && (int) graph.get(0).stream().filter(n -> n.active).count() != inNodes.length)) {
            inputNodeLabels = new String[inNodes.length];
        }
        if (outputNodeLabels == null || outNodes != outputNodeLabels.length) {
            outputNodeLabels = new String[outNodes];
        }
        neuralNetwork.addListener(e -> {
            nodeValues = neuralNetwork.getCachedNodeValues();
            paintNetwork();
        });

        buildGraph();
        return this;
    }

    /**
     * Setter for the color palette.
     * @param colorPalette the color palette to be applied to the graph.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setColorPalette(NNGraphColor colorPalette) {
        if (colorPalette == null) {
            throw new NullPointerException("The color palette must not be null!");
        }
        this.colors = colorPalette;
        paintNetwork();
        return this;
    }

    /**
     * With this method, the threshold for the node color switch can be set. It will have effect on which colors
     * from the color palette are chosen to display the nodes depending on their value.
     * @param lowerBound the lower threshold value.
     * @param upperBound the upper threshold value.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setNodeColorThreshold(double lowerBound, double upperBound) {
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("Lower bound must not be larger than upper bound!");
        }
        this.lowerNodeThreshold = lowerBound;
        this.uppweNodeThreshold = upperBound;
        paintNetwork();
        return this;
    }

    /**
     * With this method, the threshold for the line color switch can be set. It will have effect on which colors
     * from the color palette are chosen to display the lines depending on their value.
     * @param lowerBound the lower threshold value.
     * @param upperBound the upper threshold value.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setWeightColorThreshold(double lowerBound, double upperBound) {
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("Lower bound must not be larger than upper bound!");
        }
        this.lowerWeightThreshold = lowerBound;
        this.upperWeightThreshold = upperBound;
        paintNetwork();
        return this;
    }

    /**
     * This method will set a new width offset on the left hand of the graphics. It may be used to free up some
     * space if the input nodes are labeled.
     * @param top the top offset.
     * @param right the right offset.
     * @param bottom the bottom offset.
     * @param left the left offset.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setPadding(double top, double right, double bottom, double left) {
        if (top < 0 || right < 0 || bottom < 0 || left < 0) {
            throw new IllegalArgumentException("Padding offset must be greater than 0!");
        }
        this.width = initialWidth;
        this.height = initialHeight;

        this.width = width - left - right;
        this.height = height - top - bottom;
        this.wOffsetLeft = left;
        this.hOffsetTop = top;
        buildGraph();
        return this;
    }

    /**
     * This method defines if the neural network graph will grow horizontally to the right if
     * the layer count will be expanded.
     * @param growth the growth indicator.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setDynamicGrowth(boolean growth) {
        this.dynamicGrowth = growth;
        return this;
    }

    /**
     * This setter allows to set input node labels. Be aware, that by default the text will align from right
     * to left, so an additional width offset may be set.
     * @param inputNodeLabels the input node labels.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setInputNodeLabels(String[] inputNodeLabels) {
        int inputLayerSize = graph.get(0).size();
        if (inputNodeLabels == null || inputNodeLabels.length == inputLayerSize) {
            if (inputNodeLabels == null) {
                String[] labels = new String[inputLayerSize];
                Arrays.fill(labels, "");
                this.inputNodeLabels = labels;
            } else {
                this.inputNodeLabels = inputNodeLabels;
            }
            paintNetwork();
        } else {
            throw new IllegalArgumentException("Label count does not match input node count of neural network graph!");
        }
        return this;
    }

    /**
     * This setter allows to set output node labels.
     * @param outputNodeLabels the output node labels.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setOutputNodeLabels(String[] outputNodeLabels) {
        int[] configuration = neuralNetwork.getConfiguration();
        int outputLayerSize = configuration[configuration.length-1];
        if (outputNodeLabels == null || outputNodeLabels.length == outputLayerSize) {
            if (outputNodeLabels == null) {
                String[] labels = new String[outputLayerSize];
                Arrays.fill(labels, "");
                this.outputNodeLabels = labels;
            } else {
                this.outputNodeLabels = outputNodeLabels;
            }
            paintNetwork();
        } else {
            throw new IllegalArgumentException("Label count does not match output node count of neural network!");
        }
        return this;
    }

    /**
     * Setter for the radius of the nodes / vertices.
     * @param radius the node/vertex radius.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setNodeRadius(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius must be greater than 0!");
        }
        this.radius = radius;
        paintNetwork();
        return this;
    }

    /**
     * Setter for the line width. It will be applied to the edges of the neural network graph.
     * @param lineWidth the line width of the graph edges.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setLineWidth(double lineWidth) {
        if (lineWidth < 0) {
            throw new IllegalArgumentException("Line with must be greater than 0!");
        }
        this.lineWidth = lineWidth;
        paintNetwork();
        return this;
    }

    /**
     * Setter for the line width of the font.
     * @param bold can either be true or false.
     * @param italic can either be true or false.
     * @param fontSize the fontsize to set, default is 12.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setFontProperties(boolean bold, boolean italic, double fontSize) {
        if (fontSize < 0) {
            throw new IllegalArgumentException("Font size must be greater than 0!");
        }
        context.setFont(Font.font(null, bold ? FontWeight.BOLD : FontWeight.NORMAL, italic ? FontPosture.ITALIC : FontPosture.REGULAR, fontSize));
        paintNetwork();
        return this;
    }

    /**
     * This methods covers the edge case, where in general there would be a fixed count of input nodes,
     * but some of them can be switched off optionally.
     * By every switch, a new neural network has to be created, because in the background it has to be
     * fully connected. On the visual representation side, we can graphically add 'unconnected input nodes'.
     * Like this, the visualization may appear smoother.
     * @param nodeCount the total count of input nodes to be displayed.
     * @param inactiveNodeIndexes the indexes of the input nodes to appear 'switched off'.
     * @return this NNGraph (for chaining).
     */
    public NNGraph setGraphInputNodeCount(int nodeCount, int... inactiveNodeIndexes) {
        if (nodeCount < neuralNetwork.getConfiguration()[0]) {
            throw new IllegalArgumentException("Node count must be greater or equal to the actual input node count of the neural network!");
        } else if (nodeCount != neuralNetwork.getConfiguration()[0] + ((inactiveNodeIndexes == null) ? 0 : inactiveNodeIndexes.length)) {
            throw new IllegalArgumentException("Node count and inactive node indexes do not match the current neural network!");
        }
        int[] inNodes = new int[nodeCount];
        Arrays.fill(inNodes, 1);
        if (inactiveNodeIndexes != null) {
            for (int i = 0; i < inactiveNodeIndexes.length; i++) {
                inNodes[inactiveNodeIndexes[i]] = 0;
            }
        }
        if (inputNodeLabels == null || nodeCount != inputNodeLabels.length) {
            inputNodeLabels = new String[nodeCount];
        }
        this.activeInputNodes = inNodes;
        buildGraph();
        return this;
    }

    private static class GraphNode {
        double x;
        double y;
        boolean active = true;

        GraphNode(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
