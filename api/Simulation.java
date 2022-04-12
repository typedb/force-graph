package com.vaticle.force.graph.api;

import com.vaticle.force.graph.force.CenterForce;
import com.vaticle.force.graph.force.CollideForce;
import com.vaticle.force.graph.force.LinkForce;
import com.vaticle.force.graph.force.ManyBodyForce;
import com.vaticle.force.graph.force.XForce;
import com.vaticle.force.graph.force.YForce;
import com.vaticle.force.graph.impl.BasicSimulation;

import java.util.Collection;
import java.util.function.Supplier;

public interface Simulation {
    Collection<Node> nodes();

    Forces forces();

    void placeNodes(Collection<Node> nodes);

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
        CenterForce addCenterForce(Collection<Node> nodes, double x, double y);

        CenterForce addCenterForce(Collection<Node> nodes, double x, double y, double strength);

        CollideForce<Integer> addCollideForce(Collection<Node> nodes, double radius);

        CollideForce<Integer> addCollideForce(Collection<Node> nodes, double radius, double strength);

        LinkForce addLinkForce(Collection<Node> nodes, Collection<Link> links, double distance);

        LinkForce addLinkForce(Collection<Node> nodes, Collection<Link> links, double distance, double strength);

        ManyBodyForce addManyBodyForce(Collection<Node> nodes, double strength);

        ManyBodyForce addManyBodyForce(Collection<Node> nodes, double strength, double distanceMax);

        XForce addXForce(Collection<Node> nodes, double x);

        XForce addXForce(Collection<Node> nodes, double x, double strength);

        XForce addXForce(Collection<Node> nodes, Supplier<Double> x, double strength);

        YForce addYForce(Collection<Node> nodes, double y);

        YForce addYForce(Collection<Node> nodes, double y, double strength);

        YForce addYForce(Collection<Node> nodes, Supplier<Double> y, double strength);

        boolean remove(Force force);

        void clear();
    }
}
