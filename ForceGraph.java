package com.vaticle.force.graph;

import com.vaticle.force.graph.api.Simulation;
import com.vaticle.force.graph.impl.BasicSimulation;

public class ForceGraph {
    public static Simulation newSimulation() {
        return new BasicSimulation();
    }
}
