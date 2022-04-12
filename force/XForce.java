package com.vaticle.force.graph.force;

import com.vaticle.force.graph.api.Node;

import java.util.Collection;
import java.util.function.Supplier;

public class XForce extends BaseForce {
    double strength;
    Supplier<Double> x;

    public XForce(Collection<Node> nodes, Supplier<Double> x, double strength) {
        super(nodes);
        this.x = x;
        this.strength = strength;
    }

    @Override
    public void apply(double alpha) {
        for (Node node : nodes()) {
            node.vx(node.vx() + (x.get() - node.x()) * strength * alpha);
        }
    }
}
