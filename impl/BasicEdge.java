package com.vaticle.force.graph.impl;

import com.vaticle.force.graph.api.Edge;
import com.vaticle.force.graph.api.Vertex;

public class BasicEdge implements Edge {
    private final Vertex source;
    private final Vertex target;

    public BasicEdge(Vertex source, Vertex target) {
        this.source = source;
        this.target = target;
    }

    public Vertex source() {
        return source;
    }

    public Vertex target() {
        return target;
    }
}
