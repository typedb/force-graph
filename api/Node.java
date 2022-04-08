package com.vaticle.force.graph.api;

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
}
