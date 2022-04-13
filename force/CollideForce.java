package com.vaticle.force.graph.force;

import com.vaticle.force.graph.api.Force;
import com.vaticle.force.graph.api.Vertex;
import com.vaticle.force.graph.quadtree.Quadtree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static com.vaticle.force.graph.util.RandomEffects.jiggle;

public class CollideForce implements Force {
    private final List<Vertex> vertexList;
    private final double radius;
    private Map<Quadtree<Vertex>.Node, Double> quadRadii;
    private final Function<Vertex, Double> x;
    private final Function<Vertex, Double> y;
    double strength;
    Random random;

    public CollideForce(List<Vertex> vertexList, double radius, double strength) {
        this.vertexList = vertexList;
        this.radius = radius;
        this.strength = strength;
        x = node -> node.x() + node.vx();
        y = node -> node.y() + node.vy();
        random = new Random();
    }

    @Override
    public void apply(double alpha) {
        Quadtree<Vertex> tree = new Quadtree<>(vertexList, x, y);
        quadRadii = new HashMap<>();
        tree.visitAfter(this::prepare);
        Map<Vertex, Integer> vertexIndices = new HashMap<>();
        for (int i = 0; i < vertexList.size(); i++) {
            vertexIndices.put(vertexList.get(i), i);
        }

        for (int i = 0; i < vertexList.size(); i++) {
            final int index = i;
            Vertex vertex = vertexList.get(i);
            double ri = radius; double ri2 = ri * ri;
            double xi = vertex.x() + vertex.vx();
            double yi = vertex.y() + vertex.vy();
            tree.visit(quad -> {
                Vertex data = quad.node.data;
                double rj = quadRadii.get(quad.node);
                double r = ri + rj;
                if (data != null) {
                    if (vertexIndices.get(data).compareTo(index) > 0) {
                        double x = xi - data.x() - data.vx();
                        double y = yi - data.y() - data.vy();
                        double len = x*x + y*y;
                        if (len < r*r) {
                            if (x == 0) {
                                x = jiggle(random::nextDouble);
                                len += x*x;
                            }
                            if (y == 0) {
                                y = jiggle(random::nextDouble);
                                len += y*y;
                            }
                            len = Math.sqrt(len);
                            len = (r - len) / len * strength;
                            x *= len; y *= len; rj *= rj;
                            r = rj / (ri2 + rj);
                            vertex.vx(vertex.vx() + x * r);
                            vertex.vy(vertex.vy() + y * r);
                            r = 1 - r;
                            data.vx(data.vx() - x * r);
                            data.vy(data.vy() - y * r);
                        }
                    }
                    return false;
                }
                return quad.x0 > xi + r || quad.x1 < xi - r || quad.y0 > yi + r || quad.y1 < yi - r;
            });
        }
    }

    private void prepare(Quadtree<Vertex>.Quad quad) {
        if (quad.node.data != null) {
            quadRadii.put(quad.node, radius);
            return;
        }
        double quadRadius = quadRadii.getOrDefault(quad.node, 0.0);
        for (int i = 0; i < 4; i++) {
            Quadtree<Vertex>.Node qi = quad.node.children.get(i);
            if (qi == null) continue;
            Double qiRadius = quadRadii.get(qi);
            if (qiRadius == null) continue;
            if (qiRadius > quadRadius) quadRadii.put(quad.node, qiRadius);
        }
    }
}
