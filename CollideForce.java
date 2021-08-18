package com.vaticle.force.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static com.vaticle.force.graph.RandomEffects.jiggle;

public class CollideForce extends BaseForce {

    private final double radius;
    private Map<Quadtree<Node>.Node, Double> quadRadii;
    private final Function<Node, Double> x;
    private final Function<Node, Double> y;
    double strength;
    Random random;

    public CollideForce(Collection<Node> nodes, double radius) {
        this(nodes, radius, 1);
    }

    public CollideForce(Collection<Node> nodes, double radius, double strength) {
        super(nodes);
        this.radius = radius;
        this.strength = strength;
        x = node -> node.x() + node.vx;
        y = node -> node.y() + node.vy;
    }

    @Override
    public void init() {
        random = new Random();
    }

    @Override
    public void apply(double alpha) {
        Quadtree<Node> tree = new Quadtree<>(nodes(), x, y);
        quadRadii = new HashMap<>();
        tree.visitAfter(this::prepare);

        for (Node node : nodes()) {
            double ri = radius; double ri2 = ri * ri;
            double xi = node.x() + node.vx;
            double yi = node.y() + node.vy;
            tree.visit(quad -> {
                Node data = quad.node.data;
                double rj = quadRadii.get(quad.node);
                double r = ri + rj;
                if (data != null) {
                    if (data.index() > node.index()) {
                        double x = xi - data.x() - data.vx;
                        double y = yi - data.y() - data.vy;
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
                            node.vx += x * r;
                            node.vy += y * r;
                            r = 1 - r;
                            data.vx -= x * r;
                            data.vy -= y * r;
                        }
                    }
                    return false;
                }
                return quad.x0 > xi + r || quad.x1 < xi - r || quad.y0 > yi + r || quad.y1 < yi - r;
            });
        }
    }

    private void prepare(Quadtree<Node>.Quad quad) {
        if (quad.node.data != null) {
            quadRadii.put(quad.node, radius);
            return;
        }
        double quadRadius = quadRadii.getOrDefault(quad.node, 0.0);
        for (int i = 0; i < 4; i++) {
            Quadtree<Node>.Node qi = quad.node.children.get(i);
            if (qi == null) continue;
            Double qiRadius = quadRadii.get(qi);
            if (qiRadius == null) continue;
            if (qiRadius > quadRadius) quadRadii.put(quad.node, qiRadius);
        }
    }
}
