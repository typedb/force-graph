package com.vaticle.force.graph.api;

import com.vaticle.force.graph.impl.BasicSimulation;

import java.util.Collection;

public interface Simulation {
    Collection<Vertex> getVertices();

    Forces getForces();

    Forces getLocalForces();

    void placeVertices(Collection<Vertex> vertices);

    void placeVertex(Vertex vertex);

    void tick();

    double getAlpha();

    BasicSimulation setAlpha(double value);

    double getAlphaMin();

    BasicSimulation setAlphaMin(double value);

    double getAlphaDecay();

    BasicSimulation setAlphaDecay(double value);

    double getAlphaTarget();

    BasicSimulation setAlphaTarget(double value);

    double getVelocityDecay();

    BasicSimulation setVelocityDecay(double value);

    void clear();

    interface Forces {
        <FORCE extends Force> FORCE add(FORCE force);

        boolean remove(Force force);

        void clear();
    }
}
