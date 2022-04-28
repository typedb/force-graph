package com.vaticle.force.graph.force;

import com.vaticle.force.graph.api.Vertex;

import java.util.Collection;
import java.util.function.Supplier;

import static com.vaticle.force.graph.util.StandardFunctions.constant;

public class YForce extends BaseForce {
    Supplier<Double> y;
    double strength;

    public YForce(Collection<Vertex> vertices, double y) {
        this(vertices, constant(y), 1);
    }

    public YForce(Collection<Vertex> vertices, double y, double strength) {
        this(vertices, constant(y), strength);
    }

    public YForce(Collection<Vertex> vertices, Supplier<Double> y, double strength) {
        super(vertices);
        this.y = y;
        this.strength = strength;
    }

    @Override
    public void apply(Collection<Vertex> vertexPartition, double alpha) {
        for (Vertex vertex : vertexPartition) {
            vertex.vy(vertex.vy() + (y.get() - vertex.y()) * strength * alpha);
        }
    }
}
