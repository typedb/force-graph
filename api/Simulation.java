package com.vaticle.force.graph.api;

public interface Simulation {
    void tick();

    void addForce(Force force);

    boolean removeForce(Force force);
}
