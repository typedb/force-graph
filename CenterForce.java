package com.vaticle.force.graph;

import java.util.Collection;

public class CenterForce extends BaseForce {

    double x;
    double y;
    double strength;

    public CenterForce(Collection<Node> nodes, double x, double y) {
        this(nodes, x, y, 1);
    }

    public CenterForce(Collection<Node> nodes, double x, double y, double strength) {
        super(nodes);
        this.x = x;
        this.y = y;
        this.strength = strength;
    }

    @Override
    public void init() {
    }

    @Override
    public void apply(double alpha) {
        double sx = 0, sy = 0;
        for (Node node : nodes()) {
            sx += node.x(); sy += node.y();
        }
        int n = nodes().size();
        sx = (sx / n - x) * strength;
        sy = (sy / n - y) * strength;
        for (Node node : nodes()) {
            node.x(node.x() - sx); node.y(node.y() - sy);
        }
    }
}
