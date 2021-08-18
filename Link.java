package com.vaticle.force.graph;

public class Link {

    private final Node source;
    private final Node target;

    public Link(Node source, Node target) {
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
