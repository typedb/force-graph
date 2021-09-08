package com.vaticle.force.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static com.vaticle.force.graph.RandomEffects.jiggle;

public class LinkForce extends BaseForce {

    final Collection<Link> links;
    final double baseStrength;
    double distance;
    Map<Node, Integer> count;
    Map<Link, Double> bias;
    Map<Link, Double> strengths;
    Random random;

    public LinkForce(Collection<Node> nodes, Collection<Link> links, double distance) {
        this(nodes, links, distance, 1.0);
    }

    public LinkForce(Collection<Node> nodes, Collection<Link> links, double distance, double strength) {
        super(nodes);
        this.links = links;
        this.baseStrength = strength;
        this.distance = distance;
    }

    @Override
    public void init() {
        if (nodes().isEmpty()) return;

        count = new HashMap<>();
        for (Link link : links) {
            count.putIfAbsent(link.source(), 0);
            count.put(link.source(), count.get(link.source()) + 1);
            count.putIfAbsent(link.target(), 0);
            count.put(link.target(), count.get(link.target()) + 1);
        }

        bias = new HashMap<>();
        for (Link link : links) {
            bias.put(link, (double) count.get(link.source()) / (count.get(link.source()) + count.get(link.target())));
        }

        initStrength();
        random = new Random();
    }

    protected void initStrength() {
        strengths = new HashMap<>();
        for (Link link : links) {
            strengths.put(link, baseStrength / Math.min(count.get(link.source()), count.get(link.target())));
        }
    }

    @Override
    public void apply(double alpha) {
        for (Link link : links) {
            double deltaX = link.target().x() + link.target().vx - link.source().x() - link.source().vx;
            double x = deltaX != 0 ? deltaX : jiggle(random::nextDouble);
            double deltaY = link.target().y() + link.target().vy - link.source().y() - link.source().vy;
            double y = deltaY != 0 ? deltaY : jiggle(random::nextDouble);
            double length = Math.sqrt(x*x + y*y);
            double l = (length - distance) / length * alpha * strengths.get(link);
            x *= l; y *= l;
            double targetBias = bias.get(link);
            double sourceBias = 1 - targetBias;
            link.target().vx -= x * targetBias; link.target().vy -= y * targetBias;
            link.source().vx += x * sourceBias; link.source().vy += y * sourceBias;
        }
    }
}
