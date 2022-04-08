package com.vaticle.force.graph.impl;

import com.vaticle.force.graph.api.Node;

public class BasicNode implements Node {
    private double x;
    private double y;
    public double vx;
    public double vy;

    private final boolean isXFixed;
    private final boolean isYFixed;

    public BasicNode(double x, double y) {
        this(x, y, false, false);
    }

    public BasicNode(double x, double y, boolean isXFixed, boolean isYFixed) {
        this.x = x;
        this.y = y;
        this.isXFixed = isXFixed;
        this.isYFixed = isYFixed;
        this.vx = this.vy = 0;
    }

    @Override
    public boolean isXFixed() { return isXFixed; }

    @Override
    public boolean isYFixed() { return isYFixed; }

    @Override
    public double x() {
        return x;
    }

    @Override
    public void x(double value) {
        x = value;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public void y(double value) {
        y = value;
    }

    @Override
    public double vx() {
        return vx;
    }

    @Override
    public void vx(double value) {
        vx = value;
    }

    @Override
    public double vy() {
        return vy;
    }

    @Override
    public void vy(double value) {
        vy = value;
    }
}
