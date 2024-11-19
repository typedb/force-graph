package com.typedb.force.graph.api;

import java.util.Collection;

public interface Force {

    void apply(double alpha);

    void apply(Collection<Vertex> vertexPartition, double alpha);

    Collection<Vertex> vertices();
}
