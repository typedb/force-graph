package com.vaticle.force.graph;

public interface SimulationObserver {

    void onTick(ForceSimulation simulation);

    void onEnd(ForceSimulation simulation);
}
