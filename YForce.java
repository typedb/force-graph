package com.vaticle.force.graph;

import java.util.Collection;

public class YForce extends BaseForce {

    double y;
    double strength;

    public YForce(Collection<Node> nodes, double y) {
        this(nodes, y, 1);
    }

    public YForce(Collection<Node> nodes, double y, double strength) {
        super(nodes);
        this.y = y;
        this.strength = strength;
    }

    @Override
    public void init() {
    }

    @Override
    public void apply(double alpha) {
        for (Node node : nodes()) {
            node.vy += (y - node.y()) * strength * alpha;
        }
    }
}
