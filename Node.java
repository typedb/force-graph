package com.vaticle.force.graph;

public class Node implements Location2D {

    private final int index;
    private double x;
    private double y;
    public double vx;
    public double vy;

    private boolean isXFixed;
    private boolean isYFixed;

    Node(int index, double x, double y) {
        this(index, x, y, false, false);
    }

    Node(int index, double x, double y, boolean isXFixed, boolean isYFixed) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.isXFixed = isXFixed;
        this.isYFixed = isYFixed;
        this.vx = this.vy = 0;
    }

    public boolean isXFixed() { return isXFixed; }

    public void setXFixed(boolean value) { isXFixed = value; }

    public boolean isYFixed() { return isYFixed; }

    public void setYFixed(boolean value) { isYFixed = value; }

    @Override
    public double x() {
        return this.x;
    }

    @Override
    public double y() {
        return this.y;
    }

    public int index() {
        return this.index;
    }

    public void x(double value) {
        this.x = value;
    }

    public void y(double value) {
        this.y = value;
    }
}
