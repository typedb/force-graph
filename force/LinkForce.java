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
    Map<Vertex, Integer> vertexOrders;
    Map<Edge, Double> bias;
    Map<Edge, Double> strengths;
    Random random;

    public LinkForce(Collection<Vertex> vertices, Collection<Edge> edges, double distance, double strength) {
        super(vertices);
        this.edges = edges;
        this.baseStrength = strength;
        this.distance = distance;
        random = new Random();
        refreshVertexOrders();
    }

    public void refreshVertexOrders() {
        vertexOrders = new HashMap<>();
        bias = new HashMap<>();
        strengths = new HashMap<>();

        if (vertices().isEmpty()) return;

        for (Edge edge : edges) {
            vertexOrders.merge(edge.source(), 1, Integer::sum);
            vertexOrders.merge(edge.target(), 1, Integer::sum);
        }
        for (Edge edge : edges) {
            bias.put(edge, (double) vertexOrders.get(edge.source()) / (vertexOrders.get(edge.source()) + vertexOrders.get(edge.target())));
            strengths.put(edge, baseStrength / Math.min(vertexOrders.get(edge.source()), vertexOrders.get(edge.target())));
        }
    }

    @Override
    public void apply(double alpha) {
        apply(vertices(), alpha);
    }

    @Override
    public void apply(Collection<Vertex> vertexPartition, double alpha) {
        refreshVertexOrders(); // TODO: only refresh if graph has changed since last refresh
        for (Edge edge : edges) {
            double deltaX = edge.target().getX() + edge.target().getVX() - edge.source().getX() - edge.source().getVX();
            double x = deltaX != 0 ? deltaX : jiggle(random::nextDouble);
            double deltaY = edge.target().getY() + edge.target().getVY() - edge.source().getY() - edge.source().getVY();
            double y = deltaY != 0 ? deltaY : jiggle(random::nextDouble);
            double length = Math.sqrt(x*x + y*y);
            double l = (length - distance) / length * alpha * strengths.get(edge);
            x *= l; y *= l;
            double targetBias = bias.get(edge);
            double sourceBias = 1 - targetBias;
            edge.target().setVX(edge.target().getVX() - x * targetBias);
            edge.target().setVY(edge.target().getVY() - y * targetBias);
            edge.source().setVX(edge.source().getVX() + x * sourceBias);
            edge.source().setVY(edge.source().getVY() + y * sourceBias);
        }
    }
}
