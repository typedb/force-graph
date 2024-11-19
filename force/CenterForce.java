package com.typedb.force.graph.force;

import com.typedb.force.graph.api.Vertex;

import java.util.Collection;

public class CenterForce extends BaseForce {
    double x;
    double y;
    double strength;

    public CenterForce(Collection<Vertex> vertices, double x, double y) {
        this(vertices, x, y, 1);
    }

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
            sx += vertex.getX(); sy += vertex.getY();
        }
        int n = vertices().size();
        sx = (sx / n - x) * strength;
        sy = (sy / n - y) * strength;
        for (Vertex vertex : vertexPartition) {
            vertex.setX(vertex.getX() - sx); vertex.setY(vertex.getY() - sy);
        }
    }
}
