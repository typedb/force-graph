package com.vaticle.force.graph;

import java.util.Collection;
import java.util.function.Supplier;

import static com.vaticle.force.graph.StandardFunctions.constant;

public class YForce extends BaseForce {

    Supplier<Double> y;
    double strength;

    public YForce(Collection<Node> nodes, double y) {
        this(nodes, y, 1);
    }

    public YForce(Collection<Node> nodes, double y, double strength) {
        this(nodes, constant(y), strength);
    }

    public YForce(Collection<Node> nodes, Supplier<Double> y, double strength) {
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
            node.vy += (y.get() - node.y()) * strength * alpha;
        }
    }
}
