package com.vaticle.force.graph.force.impl;

import com.vaticle.force.graph.force.Link;
import com.vaticle.force.graph.force.Node;

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
