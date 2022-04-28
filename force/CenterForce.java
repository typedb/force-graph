package com.vaticle.force.graph.force;

import com.vaticle.force.graph.api.Vertex;

import java.util.Collection;

public class CenterForce extends BaseForce {
    double x;
    double y;
    double strength;

    public CenterForce(Collection<Vertex> vertices, double x, double y, double strength) {
        super(vertices);
        this.x = x;
        this.y = y;
        this.strength = strength;
    }

    @Override
    public void apply(Collection<Vertex> vertexPartition, double alpha) {
        double sx = 0, sy = 0;
        for (Vertex vertex : vertices()) {
            sx += vertex.x(); sy += vertex.y();
        }
        int n = vertices().size();
        sx = (sx / n - x) * strength;
        sy = (sy / n - y) * strength;
        for (Vertex vertex : vertexPartition) {
            vertex.x(vertex.x() - sx); vertex.y(vertex.y() - sy);
        }
    }
}
