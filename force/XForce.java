package com.typedb.force.graph.force;

import com.typedb.force.graph.api.Vertex;

import java.util.Collection;
import java.util.function.Supplier;

import static com.typedb.force.graph.util.StandardFunctions.constant;

public class XForce extends BaseForce {
    double strength;
    Supplier<Double> x;

    public XForce(Collection<Vertex> vertices, double x) {
        this(vertices, constant(x), 1);
    }

    public XForce(Collection<Vertex> vertices, double x, double strength) {
        this(vertices, constant(x), strength);
    }

    public XForce(Collection<Vertex> vertices, Supplier<Double> x, double strength) {
        super(vertices);
        this.x = x;
        this.strength = strength;
    }

    @Override
    public void apply(Collection<Vertex> vertexPartition, double alpha) {
        for (Vertex vertex : vertexPartition) {
            vertex.setVX(vertex.getVX() + (x.get() - vertex.getX()) * strength * alpha);
        }
    }
}
