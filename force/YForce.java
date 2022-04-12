package com.vaticle.force.graph.force;

import com.vaticle.force.graph.api.Node;

import java.util.Collection;
import java.util.function.Supplier;

public class YForce extends BaseForce {
    Supplier<Double> y;
    double strength;

    public YForce(Collection<Node> nodes, Supplier<Double> y, double strength) {
        super(nodes);
        this.y = y;
        this.strength = strength;
    }

    @Override
    public void apply(double alpha) {
        for (Node node : nodes()) {
            node.vy(node.vy() + (y.get() - node.y()) * strength * alpha);
        }
    }
}
