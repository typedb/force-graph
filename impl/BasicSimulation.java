/*
 * Copyright (C) 2021 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.vaticle.force.graph.impl;

import com.vaticle.force.graph.api.Link;
import com.vaticle.force.graph.api.Node;
import com.vaticle.force.graph.api.Simulation;
import com.vaticle.force.graph.api.Force;
import com.vaticle.force.graph.force.CenterForce;
import com.vaticle.force.graph.force.CollideForce;
import com.vaticle.force.graph.force.LinkForce;
import com.vaticle.force.graph.force.ManyBodyForce;
import com.vaticle.force.graph.force.XForce;
import com.vaticle.force.graph.force.YForce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.vaticle.force.graph.util.StandardFunctions.constant;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public class BasicSimulation implements Simulation {
    private double alpha;
    private double alphaMin;
    private double alphaDecay;
    private double alphaTarget;
    private double velocityDecay;
    private final Forces forces;
    private final ConcurrentMap<Node, Integer> nodesIndexed;
    private final AtomicInteger nextNodeID;

    private static final int INITIAL_PLACEMENT_RADIUS = 10;
    private static final double INITIAL_PLACEMENT_ANGLE = Math.PI * (3 - Math.sqrt(5));
    private static final double DEFAULT_FORCE_STRENGTH = 1;

    public BasicSimulation() {
        alpha = 1;
        alphaMin = 0.001;
        alphaDecay = 1 - Math.pow(alphaMin, 1.0 / 300);
        alphaTarget = 0;
        velocityDecay = 0.6;
        forces = new Forces();
        nodesIndexed = new ConcurrentHashMap<>();
        nextNodeID = new AtomicInteger();
    }

    @Override
    public Collection<Node> nodes() {
        return nodesIndexed.keySet();
    }

    @Override
    public Simulation.Forces forces() {
        return forces;
    }

    @Override
    public synchronized void tick() {
        alpha += (alphaTarget - alpha) * alphaDecay;

        forces.applyAll();

        for (Node node : nodes()) {
            if (node.isXFixed()) node.vx(0);
            else {
                node.vx(node.vx() * velocityDecay);
                node.x(node.x() + node.vx());
            }
            if (node.isYFixed()) node.vy(0);
            else {
                node.vy(node.vy() * velocityDecay);
                node.y(node.y() + node.vy());
            }
        }
    }

    @Override
    public void placeNodes(Collection<Node> nodes) {
        nodes.forEach(this::placeNode);
        forces.onNodesChanged();
    }

    protected void placeNode(Node node) {
        int id = nextNodeID.getAndIncrement();
        double radius = INITIAL_PLACEMENT_RADIUS * Math.sqrt(0.5 + id);
        double angle = id * INITIAL_PLACEMENT_ANGLE;
        node.x(node.x() + (node.isXFixed() ? 0 : radius * Math.cos(angle)));
        node.y(node.y() + (node.isYFixed() ? 0 : radius * Math.sin(angle)));
        nodesIndexed.put(node, id);
    }

    @Override
    public double alpha() {
        return alpha;
    }

    @Override
    public BasicSimulation alpha(double value) {
        alpha = value;
        return this;
    }

    @Override
    public double alphaMin() {
        return alphaMin;
    }

    @Override
    public BasicSimulation alphaMin(double value) {
        alphaMin = value;
        return this;
    }

    @Override
    public double alphaDecay() {
        return alphaDecay;
    }

    @Override
    public BasicSimulation alphaDecay(double value) {
        alphaDecay = value;
        return this;
    }

    @Override
    public double alphaTarget() {
        return alphaTarget;
    }

    @Override
    public BasicSimulation alphaTarget(double value) {
        alphaTarget = value;
        return this;
    }

    @Override
    public double velocityDecay() {
        return velocityDecay;
    }

    @Override
    public BasicSimulation velocityDecay(double value) {
        velocityDecay = value;
        return this;
    }

    @Override
    public synchronized void clear() {
        forces.clear();
        nodesIndexed.clear();
        nextNodeID.set(0);
    }

    public class Forces implements Simulation.Forces {
        final Collection<Force> forces;

        Forces() {
            forces = new ArrayList<>();
        }

        void applyAll() {
            forces.forEach(force -> force.apply(alpha));
        }

        void onNodesChanged() {
            forces.forEach(Force::onNodesChanged);
        }

        void add(Force force) {
            forces.add(requireNonNull(force));
            force.onNodesChanged();
        }

        @Override
        public CenterForce addCenterForce(Collection<Node> nodes, double x, double y) {
            return addCenterForce(nodes, x, y, DEFAULT_FORCE_STRENGTH);
        }

        @Override
        public CenterForce addCenterForce(Collection<Node> nodes, double x, double y, double strength) {
            CenterForce force = new CenterForce(nodes, x, y, strength);
            add(force);
            return force;
        }

        @Override
        public CollideForce<Integer> addCollideForce(Collection<Node> nodes, double radius) {
            return addCollideForce(nodes, radius, DEFAULT_FORCE_STRENGTH);
        }

        @Override
        public CollideForce<Integer> addCollideForce(Collection<Node> nodes, double radius, double strength) {
            CollideForce<Integer> force = new CollideForce<>(nodes.stream().collect(toMap(x -> x, nodesIndexed::get)), radius, strength);
            add(force);
            return force;
        }

        @Override
        public LinkForce addLinkForce(Collection<Node> nodes, Collection<Link> links, double distance) {
            return addLinkForce(nodes, links, distance, DEFAULT_FORCE_STRENGTH);
        }

        @Override
        public LinkForce addLinkForce(Collection<Node> nodes, Collection<Link> links, double distance, double strength) {
            LinkForce force = new LinkForce(nodes, links, distance, strength);
            add(force);
            return force;
        }

        @Override
        public ManyBodyForce addManyBodyForce(Collection<Node> nodes, double strength) {
            return addManyBodyForce(nodes, strength, Math.sqrt(Double.MAX_VALUE));
        }

        @Override
        public ManyBodyForce addManyBodyForce(Collection<Node> nodes, double strength, double distanceMax) {
            ManyBodyForce force = new ManyBodyForce(nodes, strength, distanceMax);
            add(force);
            return force;
        }

        @Override
        public XForce addXForce(Collection<Node> nodes, double x) {
            return addXForce(nodes, x, DEFAULT_FORCE_STRENGTH);
        }

        @Override
        public XForce addXForce(Collection<Node> nodes, double x, double strength) {
            return addXForce(nodes, constant(x), strength);
        }

        @Override
        public XForce addXForce(Collection<Node> nodes, Supplier<Double> x, double strength) {
            XForce force = new XForce(nodes, x, strength);
            add(force);
            return force;
        }

        @Override
        public YForce addYForce(Collection<Node> nodes, double y) {
            return addYForce(nodes, y, DEFAULT_FORCE_STRENGTH);
        }

        @Override
        public YForce addYForce(Collection<Node> nodes, double y, double strength) {
            return addYForce(nodes, constant(y), strength);
        }

        @Override
        public YForce addYForce(Collection<Node> nodes, Supplier<Double> y, double strength) {
            YForce force = new YForce(nodes, y, strength);
            add(force);
            return force;
        }

        @Override
        public boolean remove(Force force) {
            return forces.remove(force);
        }

        @Override
        public void clear() {
            forces.clear();
        }
    }
}
