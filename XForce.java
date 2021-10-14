package com.vaticle.force.graph;

import java.util.Collection;

public class XForce extends BaseForce {

    double x;
    double strength;

    public XForce(Collection<Node> nodes, double x) {
        this(nodes, x, 1);
    }

    public XForce(Collection<Node> nodes, double x, double strength) {
        super(nodes);
        this.x = x;
        this.strength = strength;
    }

    @Override
    public void init() {
    }

    @Override
    public void apply(double alpha) {
        for (Node node : nodes()) {
            node.vx += (x - node.x()) * strength * alpha;
        }
    }
}
