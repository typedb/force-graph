package com.vaticle.force.graph.force;

import com.vaticle.force.graph.api.Edge;
import com.vaticle.force.graph.api.Vertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.vaticle.force.graph.util.RandomEffects.jiggle;

public class LinkForce extends BaseForce {
    final Collection<Edge> edges;
    final double baseStrength;
    double distance;
    Map<Vertex, Integer> edgeCounts;
    Map<Edge, Double> bias;
    Map<Edge, Double> strengths;
    Random random;

    public LinkForce(Collection<Vertex> vertices, Collection<Edge> edges, double distance, double strength) {
        super(vertices);
        this.edges = edges;
        this.baseStrength = strength;
        this.distance = distance;
        random = new Random();
        onGraphChanged();
    }

    @Override
    public void onGraphChanged() {
        edgeCounts = new HashMap<>();
        bias = new HashMap<>();
        strengths = new HashMap<>();

        if (vertices().isEmpty()) return;

        for (Edge edge : edges) {
            edgeCounts.merge(edge.source(), 1, Integer::sum);
            edgeCounts.merge(edge.target(), 1, Integer::sum);
        }
        for (Edge edge : edges) {
            bias.put(edge, (double) edgeCounts.get(edge.source()) / (edgeCounts.get(edge.source()) + edgeCounts.get(edge.target())));
            strengths.put(edge, baseStrength / Math.min(edgeCounts.get(edge.source()), edgeCounts.get(edge.target())));
        }
    }

    @Override
    public void apply(double alpha) {
        apply(vertices(), alpha);
    }

    @Override
    public void apply(Collection<Vertex> vertexPartition, double alpha) {
        for (Edge edge : edges) {
            double deltaX = edge.target().x() + edge.target().vx() - edge.source().x() - edge.source().vx();
            double x = deltaX != 0 ? deltaX : jiggle(random::nextDouble);
            double deltaY = edge.target().y() + edge.target().vy() - edge.source().y() - edge.source().vy();
            double y = deltaY != 0 ? deltaY : jiggle(random::nextDouble);
            double length = Math.sqrt(x*x + y*y);
            double l = (length - distance) / length * alpha * strengths.get(edge);
            x *= l; y *= l;
            double targetBias = bias.get(edge);
            double sourceBias = 1 - targetBias;
            edge.target().vx(edge.target().vx() - x * targetBias);
            edge.target().vy(edge.target().vy() - y * targetBias);
            edge.source().vx(edge.source().vx() + x * sourceBias);
            edge.source().vy(edge.source().vy() + y * sourceBias);
        }
    }
}
