package com.vaticle.force.graph.impl;

import com.vaticle.force.graph.api.Link;
import com.vaticle.force.graph.api.Node;

public class BasicLink implements Link {
    private final Node source;
    private final Node target;

    public BasicLink(Node source, Node target) {
        this.source = source;
        this.target = target;
    }

    public Node source() {
        return source;
    }

    public Node target() {
        return target;
    }
}
