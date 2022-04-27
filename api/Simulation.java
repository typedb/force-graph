package com.vaticle.force.graph.api;

import com.vaticle.force.graph.force.CenterForce;
import com.vaticle.force.graph.force.CollideForce;
import com.vaticle.force.graph.force.LinkForce;
import com.vaticle.force.graph.force.ManyBodyForce;
import com.vaticle.force.graph.force.XForce;
import com.vaticle.force.graph.force.YForce;
import com.vaticle.force.graph.impl.BasicSimulation;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface Simulation {
    Collection<Vertex> vertices();

    Forces forces();

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
        CenterForce addCenterForce(Collection<Vertex> vertices, double x, double y);

        CenterForce addCenterForce(Collection<Vertex> vertices, double x, double y, double strength);

        CollideForce addCollideForce(List<Vertex> vertices, double radius);

        CollideForce addCollideForce(List<Vertex> vertices, double radius, double strength);

        LinkForce addLinkForce(Collection<Vertex> vertices, Collection<Edge> edges, double distance);

        LinkForce addLinkForce(Collection<Vertex> vertices, Collection<Edge> edges, double distance, double strength);

        ManyBodyForce addManyBodyForce(Collection<Vertex> vertices, double strength);

        ManyBodyForce addManyBodyForce(Collection<Vertex> vertices, double strength, double distanceMax);

        XForce addXForce(Collection<Vertex> vertices, double x);

        XForce addXForce(Collection<Vertex> vertices, double x, double strength);

        XForce addXForce(Collection<Vertex> vertices, Supplier<Double> x, double strength);

        YForce addYForce(Collection<Vertex> vertices, double y);

        YForce addYForce(Collection<Vertex> vertices, double y, double strength);

        YForce addYForce(Collection<Vertex> vertices, Supplier<Double> y, double strength);

        boolean remove(Force force);

        void clear();
    }
}
