package com.typedb.force.graph.force;

import com.typedb.force.graph.api.Force;
import com.typedb.force.graph.api.Vertex;

import java.util.Collection;

public abstract class BaseForce implements Force {
    private final Collection<Vertex> vertices;

    BaseForce(Collection<Vertex> vertices) {
        this.vertices = vertices;
    }

    public Collection<Vertex> vertices() {
        return vertices;
    }

    @Override
    public void apply(double alpha) {
        apply(vertices(), alpha);
    }
}
