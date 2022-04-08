package com.vaticle.force.graph.force;

import com.vaticle.force.graph.api.Node;
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
    private Map<Quadtree<Node>.Node, QuadData> quads;
    Random random;

    public ManyBodyForce(Collection<Node> nodes, double strength) {
        this(nodes, strength, Math.sqrt(Double.MAX_VALUE));
    }

    public ManyBodyForce(Collection<Node> nodes, double strength, double distanceMax) {
        super(nodes);
        this.strength = strength;
        this.distanceMin2 = 1.0;
        this.distanceMax2 = distanceMax * distanceMax;
        this.theta2 = 0.81;
    }

    @Override
    public void init() {
        random = new Random();
    }

    @Override
    public void apply(double alpha) {
        Quadtree<Node> tree = new Quadtree<>(nodes(), Node::x, Node::y);
        quads = new HashMap<>();
        tree.visitAfter(this::accumulate);

        for (Node node : nodes()) {
            tree.visit(quad -> {
                QuadData q = quads.get(quad.node);
                if (q == null || q.value == 0.0) return true;

                double x = q.x - node.x();
                double y = q.y - node.y();
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
                        node.vx(node.vx() + x * q.value * alpha / len);
                        node.vy(node.vy() + y * q.value * alpha / len);
                    }
                    return true;
                }

                // Otherwise, process points directly
                if (!quad.node.children.isEmpty() || len >= distanceMax2) return false;

                // Limit forces for very close nodes; randomise direction if coincident
                if (!quad.node.data.equals(node) || quad.node.next != null) {
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

                Quadtree<Node>.Node n = quad.node;
                do {
                    if (!quad.node.data.equals(node)) {
                        double u = strength * alpha / len;
                        node.vx(node.vx() + x * u);
                        node.vy(node.vy() + y * u);
                    }
                    n = n.next;
                } while (n != null);

                return false;
            });
        }
    }

    private void accumulate(Quadtree<Node>.Quad quad) {
        double strength = 0.0;

        if (!quad.node.children.isEmpty()) {
            // For internal nodes, accumulate forces from child quadrants
            double weight = 0.0;
            double x = 0.0, y = 0.0;
            for (int i = 0; i < 4; i++) {
                Quadtree<Node>.Node qi = quad.node.children.get(i);
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
            Quadtree<Node>.Node n = quad.node;
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
