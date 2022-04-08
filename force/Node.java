package com.vaticle.force.graph.force;

public interface Node {
    boolean isXFixed();

    boolean isYFixed();

    double x();

    void x(double value);

    double y();

    void y(double value);

    double vx();

    void vx(double value);

    double vy();

    void vy(double value);

    // TODO: refactor to ID and document why (D3.js relies on dynamic typing to add 'index' to nodes, but we can't)
    int index();
}
