package com.typedb.force.graph;

import com.typedb.force.graph.api.Simulation;
import com.typedb.force.graph.impl.BasicSimulation;

public class ForceGraph {
    public static Simulation newSimulation() {
        return new BasicSimulation();
    }
}
