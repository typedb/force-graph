package com.vaticle.force.graph.api;

public interface Force {
    void apply(double alpha);

    default void onNodesChanged() {}
}
