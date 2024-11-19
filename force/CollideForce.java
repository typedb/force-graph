package com.typedb.force.graph.force;

import com.typedb.force.graph.api.Force;
import com.typedb.force.graph.api.Vertex;
import com.typedb.force.graph.quadtree.Quadtree;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import static com.typedb.force.graph.util.RandomEffects.jiggle;

public class CollideForce implements Force {
    // TODO: just make Force.vertices() be a List<Vertex> so we can extend BaseForce
    private final List<Vertex> vertexList;
    private final double radius;
    private Map<Quadtree<Vertex>.Node, Double> quadRadii;
    private Quadtree<Vertex> tree;
    private final Function<Vertex, Double> x;
    private final Function<Vertex, Double> y;
    double strength;
    Random random;

    public CollideForce(List<Vertex> vertexList, double radius) {
        this(vertexList, radius, 1);
    }

    public CollideForce(List<Vertex> vertexList, double radius, double strength) {
        this.vertexList = vertexList;
        this.radius = radius;
        this.strength = strength;
        x = node -> node.getX() + node.getVX();
        y = node -> node.getY() + node.getVY();
        random = new Random();
    }

    public void buildQuadtree() {
        tree = new Quadtree<>(vertexList, x, y);
        quadRadii = new HashMap<>();
        tree.visitAfter(this::prepare);
    }

    @Override
    public Collection<Vertex> vertices() {
        return vertexList;
    }

    @Override
    public void apply(double alpha) {
        buildQuadtree();
        apply(vertexList, alpha);
    }

    @Override
    public void apply(Collection<Vertex> vertexPartition, double alpha) {
        Map<Vertex, Integer> vertexIndices = new HashMap<>();
        for (int i = 0; i < vertexList.size(); i++) {
            vertexIndices.put(vertexList.get(i), i);
        }

        for (Vertex vertex : vertexPartition) {
            double ri = radius; double ri2 = ri * ri;
            double xi = vertex.getX() + vertex.getVX();
            double yi = vertex.getY() + vertex.getVY();
            tree.visit(quad -> {
                Vertex data = quad.node.data;
                double rj = quadRadii.get(quad.node);
                double r = ri + rj;
                if (data != null) {
                    if (vertexIndices.get(data).compareTo(vertexIndices.get(vertex)) > 0) {
                        double x = xi - data.getX() - data.getVX();
                        double y = yi - data.getY() - data.getVY();
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
                            vertex.setVX(vertex.getVX() + x * r);
                            vertex.setVY(vertex.getVY() + y * r);
                            r = 1 - r;
                            data.setVX(data.getVX() - x * r);
                            data.setVY(data.getVY() - y * r);
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
