package com.vaticle.force.graph.api;

public interface Vertex {
    boolean isXFixed();

    boolean isYFixed();

    void setXFixed(boolean value);

    void setYFixed(boolean value);

    double getX();

    void setX(double value);

    double getY();

    void setY(double value);

    double getVX();

    void setVX(double value);

    double getVY();

    void setVY(double value);
}
