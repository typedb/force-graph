package com.vaticle.force.graph.api;

import com.vaticle.force.graph.impl.BasicSimulation;

import java.util.Collection;

public interface Simulation {
    Collection<Vertex> vertices();

    Forces forces();

    Forces localForces();

    void placeVertices(Collection<Vertex> vertices);

    void tick();

    double alpha();

    BasicSimulation alpha(double value);

    double alphaMin();

    BasicSimulation alphaMin(double value);

    double alphaDecay();

    BasicSimulation alphaDecay(double value);

    double alphaTarget();

    BasicSimulation alphaTarget(double value);

    double velocityDecay();

    BasicSimulation velocityDecay(double value);

    void clear();

    interface Forces {
        <FORCE extends Force> FORCE add(FORCE force);

        boolean remove(Force force);

        void clear();
    }
}
