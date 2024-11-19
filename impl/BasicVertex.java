package com.typedb.force.graph.impl;

import com.typedb.force.graph.api.Vertex;

public class BasicVertex implements Vertex {
    private double x;
    private double y;
    public double vx;
    public double vy;

    private boolean isXFixed;
    private boolean isYFixed;

    public BasicVertex(double x, double y) {
        this(x, y, false, false);
    }

    public BasicVertex(double x, double y, boolean isXFixed, boolean isYFixed) {
        this.x = x;
        this.y = y;
        this.isXFixed = isXFixed;
        this.isYFixed = isYFixed;
        this.vx = this.vy = 0;
    }

    @Override
    public boolean isXFixed() {
        return isXFixed;
    }

    @Override
    public boolean isYFixed() {
        return isYFixed;
    }

    @Override
    public void setXFixed(boolean value) {
        isXFixed = value;
    }

    @Override
    public void setYFixed(boolean value) {
        isYFixed = value;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setX(double value) {
        x = value;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setY(double value) {
        y = value;
    }

    @Override
    public double getVX() {
        return vx;
    }

    @Override
    public void setVX(double value) {
        vx = value;
    }

    @Override
    public double getVY() {
        return vy;
    }

    @Override
    public void setVY(double value) {
        vy = value;
    }
}
