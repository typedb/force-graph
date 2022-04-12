package com.vaticle.force.graph.force;

import com.vaticle.force.graph.api.Force;
import com.vaticle.force.graph.api.Vertex;

import java.util.Collection;

public abstract class BaseForce implements Force {
    private final Collection<Vertex> vertices;

    BaseForce(Collection<Vertex> vertices) {
        this.vertices = vertices;
    }

    protected Collection<Vertex> nodes() {
        return vertices;
    }
}
