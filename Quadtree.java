package com.vaticle.force.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

public class Quadtree<DATA> {

    final Function<DATA, Double> x;
    final Function<DATA, Double> y;
    double x0;
    double y0;
    double x1;
    double y1;
    Node root;

    public Quadtree(Collection<DATA> data, Function<DATA, Double> x, Function<DATA, Double> y) {
        this.x = x;
        this.y = y;
        this.x0 = this.y0 = this.x1 = this.y1 = Double.NaN;
        this.addAll(data);
    }

    public void addAll(Collection<DATA> data) {
        Map<DATA, Double> xz = new HashMap<>();
        Map<DATA, Double> yz = new HashMap<>();
        double x0 = Double.MAX_VALUE; double y0 = x0; double x1 = -x0; double y1 = x1;

        // Compute the points and their extent
        for (DATA d : data) {
            Double x = this.x.apply(d); Double y = this.y.apply(d);
            if (x == null || y == null) continue;
            xz.put(d, x);
            yz.put(d, y);
            if (x < x0) x0 = x;
            if (x > x1) x1 = x;
            if (y < y0) y0 = y;
            if (y > y1) y1 = y;
        }

        // If there were no (valid) points, abort
        if (x0 > x1 || y0 > y1) return;

        // Expand the tree to cover the new points
        cover(x0, y0);
        cover(x1, y1);

        // Add the new points
        for (DATA d : data) {
            add(xz.get(d), yz.get(d), d);
        }
    }

    void cover(double x, double y) {
        // If the quadtree has no extent, initialise them. Integer extents are necessary so that if we later
        // double the extent, the existing quadrant boundaries don't change due to floating-point errors
        if (Double.isNaN(x0)) {
            x0 = Math.floor(x); x1 = x0 + 1;
            y0 = Math.floor(y); y1 = y0 + 1;
            return;
        }

        // Otherwise, double repeatedly to cover
        double z = x1 - x0;
        Node node = root;
        int i;

        while (x0 > x || x >= x1 || y0 > y || y >= y1) {
            if (y >= y0) i = x >= x0 ? 0 : 1;
            else i = x >= x0 ? 2 : 3;
            Node parent = new Node();
            parent.children.put(i, node);
            node = parent;
            z *= 2;

            switch (i) {
                case 0: x1 = x0 + z; y1 = y0 + z; break;
                case 1: x0 = x1 - z; y1 = y0 + z; break;
                case 2: x1 = x0 + z; y0 = y1 - z; break;
                case 3: x0 = x1 - z; y0 = y1 - z; break;
            }
        }

        if (root != null && !root.children.isEmpty()) root = node;
    }

    void add(Double x, Double y, DATA d) {
        if (x == null || y == null) return; // ignore invalid points

        Node parent = null;
        Node node = root;
        Node leaf = new Node(d);
        double _x0 = x0, _y0 = y0, _x1 = x1, _y1 = y1;
        double xm, ym, xp, yp;
        boolean right, bottom;
        int i = -1, j;

        // If the tree is empty, initialise the root as a leaf
        if (node == null) {
            root = leaf;
            return;
        }

        // Find the existing leaf for the new point, or add it
        while (!node.children.isEmpty()) {
            xm = (_x0 + _x1) / 2; ym = (_y0 + _y1) / 2;
            right = x >= xm; bottom = y >= ym;
            if (right) _x0 = xm; else _x1 = xm;
            if (bottom) _y0 = ym; else _y1 = ym;
            parent = node;
            i = childIndex(bottom, right);
            node = node.children.get(i);
            if (node == null) {
                parent.children.put(i, leaf);
                return;
            }
        }

        // Is the new point exactly coincident with the existing point?
        xp = this.x.apply(node.data);
        yp = this.y.apply(node.data);
        if (x == xp && y == yp) {
            leaf.next = node;
            if (parent != null) {
                assert i != -1;
                parent.children.put(i, leaf);
            } else {
                root = leaf;
            }
            return;
        }

        // Otherwise, split the leaf node until the old and new points are separated
        do {
            if (parent == null) {
                root = new Node();
                parent = root;
            } else {
                Node newParent = new Node();
                parent.children.put(i, newParent);
                parent = newParent;
            }
            xm = (_x0 + _x1) / 2; ym = (_y0 + _y1) / 2;
            right = x >= xm; bottom = y >= ym;
            if (right) _x0 = xm; else _x1 = xm;
            if (bottom) _y0 = ym; else _y1 = ym;
            i = childIndex(bottom, right);
            if (yp < ym) j = xp < xm ? 0 : 1;
            else j = xp < xm ? 2 : 3;
        } while (i == j);

        parent.children.put(j, node);
        parent.children.put(i, leaf);
    }

    int childIndex(boolean bottom, boolean right) {
        if (!bottom) return !right ? 0 : 1;
        else return !right ? 2 : 3;
    }

    public void visit(Function<Quad, Boolean> callback) {
        Stack<Quad> quads = new Stack<>();
        if (root != null) quads.push(new Quad(root, x0, y0, x1, y1));
        while (!quads.isEmpty()) {
            Quad q = quads.pop();
            if (!callback.apply(q) && !q.node.children.isEmpty()) {
                double xm = (q.x0 + q.x1) / 2; double ym = (q.y0 + q.y1) / 2;
                if (q.node.children.containsKey(3)) quads.push(new Quad(q.node.children.get(3), xm, ym, q.x1, q.y1));
                if (q.node.children.containsKey(2)) quads.push(new Quad(q.node.children.get(2), q.x0, ym, xm, q.y1));
                if (q.node.children.containsKey(1)) quads.push(new Quad(q.node.children.get(1), xm, q.y0, q.x1, ym));
                if (q.node.children.containsKey(0)) quads.push(new Quad(q.node.children.get(0), q.x0, q.y0, xm, ym));
            }
        }
    }

    public void visitAfter(Consumer<Quad> callback) {
        Stack<Quad> quads = new Stack<>();
        Stack<Quad> next = new Stack<>();
        if (root != null) quads.push(new Quad(root, x0, y0, x1, y1));
        while (!quads.isEmpty()) {
            Quad q = quads.pop();
            if (!q.node.children.isEmpty()) {
                double xm = (q.x0 + q.x1) / 2; double ym = (q.y0 + q.y1) / 2;
                if (q.node.children.containsKey(0)) quads.push(new Quad(q.node.children.get(0), q.x0, q.y0, xm, ym));
                if (q.node.children.containsKey(1)) quads.push(new Quad(q.node.children.get(1), xm, q.y0, q.x1, ym));
                if (q.node.children.containsKey(2)) quads.push(new Quad(q.node.children.get(2), q.x0, ym, xm, q.y1));
                if (q.node.children.containsKey(3)) quads.push(new Quad(q.node.children.get(3), xm, ym, q.x1, q.y1));
            }
            next.push(q);
        }
        while (!next.isEmpty()) {
            Quad q = next.pop();
            callback.accept(q);
        }
    }

    class Node {

        DATA data;
        Map<Integer, Node> children; // map representation of sparse array
        Node next;

        Node() {
            this(null);
        }

        Node(DATA data) {
            this.data = data;
            this.children = new HashMap<>();
        }
    }

    class Quad {

        final Node node;
        final double x0;
        final double y0;
        final double x1;
        final double y1;

        Quad(Node node, double x0, double y0, double x1, double y1) {
            this.node = node;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }
    }
}
