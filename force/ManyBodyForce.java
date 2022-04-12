package com.vaticle.force.graph.force;

import com.vaticle.force.graph.api.Vertex;
import com.vaticle.force.graph.quadtree.Quadtree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.vaticle.force.graph.util.RandomEffects.jiggle;

public class ManyBodyForce extends BaseForce {
    double strength;
    double distanceMin2;
    double distanceMax2;
    double theta2;
    private Map<Quadtree<Vertex>.Node, QuadData> quads;
    Random random;

    public ManyBodyForce(Collection<Vertex> vertices, double strength, double distanceMax) {
        super(vertices);
        this.strength = strength;
        this.distanceMin2 = 1.0;
        this.distanceMax2 = distanceMax * distanceMax;
        this.theta2 = 0.81;
        random = new Random();
    }

    @Override
    public void apply(double alpha) {
        Quadtree<Vertex> tree = new Quadtree<>(vertices(), Vertex::x, Vertex::y);
        quads = new HashMap<>();
        tree.visitAfter(this::accumulate);

        for (Vertex vertex : vertices()) {
            tree.visit(quad -> {
                QuadData q = quads.get(quad.node);
                if (q == null || q.value == 0.0) return true;

                double x = q.x - vertex.x();
                double y = q.y - vertex.y();
                double w = quad.x1 - quad.x0;
                double len = x*x + y*y;

                // Apply the Barnes-Hut approximation if possible.
                // Limit forces for very close nodes; randomise direction if coincident
                if (w*w / theta2 < len) {
                    if (len < distanceMax2) {
                        if (x == 0) {
                            x = jiggle(random::nextDouble);
                            len += x*x;
                        }
                        if (y == 0) {
                            y = jiggle(random::nextDouble);
                            len += y*y;
                        }
                        if (len < distanceMin2) len = Math.sqrt(distanceMin2 * len);
                        vertex.vx(vertex.vx() + x * q.value * alpha / len);
                        vertex.vy(vertex.vy() + y * q.value * alpha / len);
                    }
                    return true;
                }

                // Otherwise, process points directly
                if (!quad.node.children.isEmpty() || len >= distanceMax2) return false;

                // Limit forces for very close nodes; randomise direction if coincident
                if (!quad.node.data.equals(vertex) || quad.node.next != null) {
                    if (x == 0) {
                        x = jiggle(random::nextDouble);
                        len += x*x;
                    }
                    if (y == 0) {
                        y = jiggle(random::nextDouble);
                        len += y*y;
                    }
                    if (len < distanceMin2) len = Math.sqrt(distanceMin2 * len);
                }

                Quadtree<Vertex>.Node n = quad.node;
                do {
                    if (!quad.node.data.equals(vertex)) {
                        double u = strength * alpha / len;
                        vertex.vx(vertex.vx() + x * u);
                        vertex.vy(vertex.vy() + y * u);
                    }
                    n = n.next;
                } while (n != null);

                return false;
            });
        }
    }

    private void accumulate(Quadtree<Vertex>.Quad quad) {
        double strength = 0.0;

        if (!quad.node.children.isEmpty()) {
            // For internal nodes, accumulate forces from child quadrants
            double weight = 0.0;
            double x = 0.0, y = 0.0;
            for (int i = 0; i < 4; i++) {
                Quadtree<Vertex>.Node qi = quad.node.children.get(i);
                if (qi == null) continue;
                QuadData qiData = quads.get(qi);
                if (qiData == null || qiData.value == 0.0) continue;
                double c = Math.abs(qiData.value);
                strength += qiData.value;
                weight += c;
                x += c * qiData.x; y += c * qiData.y;
            }
            quads.put(quad.node, new QuadData(x / weight, y / weight, strength));
        } else {
            // For leaf nodes, accumulate forces from coincident quadrants
            Quadtree<Vertex>.Node n = quad.node;
            quads.put(n, new QuadData(n.data.x(), n.data.y(), 0.0));
            do {
                strength += this.strength;
                n = n.next;
            } while (n != null);
            quads.get(quad.node).value = strength;
        }
    }

    private static class QuadData {
        final double x;
        final double y;
        double value;

        private QuadData(double x, double y, double value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }
}
