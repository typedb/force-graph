package com.vaticle.force.graph.force;

import java.util.Collection;
import java.util.function.Supplier;

import static com.vaticle.force.graph.force.StandardFunctions.constant;

public class XForce extends BaseForce {
    double strength;
    Supplier<Double> x;

    public XForce(Collection<Node> nodes, double x) {
        this(nodes, x, 1);
    }

    public XForce(Collection<Node> nodes, double x, double strength) {
        this(nodes, constant(x), strength);
    }

    public XForce(Collection<Node> nodes, Supplier<Double> x, double strength) {
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
            node.vx(node.vx() + (x.get() - node.x()) * strength * alpha);
        }
    }
}
