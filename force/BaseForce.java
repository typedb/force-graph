package com.vaticle.force.graph.force;

import java.util.Collection;

public abstract class BaseForce implements Force {

    private final Collection<Node> nodes;

    BaseForce(Collection<Node> nodes) {
        this.nodes = nodes;
    }

    protected Collection<Node> nodes() {
        return nodes;
    }
}
