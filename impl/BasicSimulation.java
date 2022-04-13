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

import com.vaticle.force.graph.api.Edge;
import com.vaticle.force.graph.api.Vertex;
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
import java.util.List;
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
    private final ConcurrentMap<Vertex, Integer> verticesIndexed;
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
        verticesIndexed = new ConcurrentHashMap<>();
        nextNodeID = new AtomicInteger();
    }

    @Override
    public Collection<Vertex> vertices() {
        return verticesIndexed.keySet();
    }

    @Override
    public Simulation.Forces forces() {
        return forces;
    }

    @Override
    public synchronized void tick() {
        alpha += (alphaTarget - alpha) * alphaDecay;

        forces.applyAll();

        for (Vertex vertex : vertices()) {
            if (vertex.isXFixed()) vertex.vx(0);
            else {
                vertex.vx(vertex.vx() * velocityDecay);
                vertex.x(vertex.x() + vertex.vx());
            }
            if (vertex.isYFixed()) vertex.vy(0);
            else {
                vertex.vy(vertex.vy() * velocityDecay);
                vertex.y(vertex.y() + vertex.vy());
            }
        }
    }

    @Override
    public synchronized void placeVertices(Collection<Vertex> vertices) {
        vertices.forEach(this::placeNode);
        forces.onVerticesChanged();
    }

    protected void placeNode(Vertex vertex) {
        int id = nextNodeID.getAndIncrement();
        double radius = INITIAL_PLACEMENT_RADIUS * Math.sqrt(0.5 + id);
        double angle = id * INITIAL_PLACEMENT_ANGLE;
        vertex.x(vertex.x() + (vertex.isXFixed() ? 0 : radius * Math.cos(angle)));
        vertex.y(vertex.y() + (vertex.isYFixed() ? 0 : radius * Math.sin(angle)));
        verticesIndexed.put(vertex, id);
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
        verticesIndexed.clear();
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

        void onVerticesChanged() {
            forces.forEach(Force::onVerticesChanged);
        }

        void add(Force force) {
            forces.add(requireNonNull(force));
            force.onVerticesChanged();
        }

        @Override
        public CenterForce addCenterForce(Collection<Vertex> vertices, double x, double y) {
            return addCenterForce(vertices, x, y, DEFAULT_FORCE_STRENGTH);
        }

        @Override
        public CenterForce addCenterForce(Collection<Vertex> vertices, double x, double y, double strength) {
            CenterForce force = new CenterForce(vertices, x, y, strength);
            add(force);
            return force;
        }

        @Override
        public CollideForce addCollideForce(List<Vertex> vertices, double radius) {
            return addCollideForce(vertices, radius, DEFAULT_FORCE_STRENGTH);
        }

        @Override
        public CollideForce addCollideForce(List<Vertex> vertices, double radius, double strength) {
            CollideForce force = new CollideForce(vertices, radius, strength);
            add(force);
            return force;
        }

        @Override
        public LinkForce addLinkForce(Collection<Vertex> vertices, Collection<Edge> edges, double distance) {
            return addLinkForce(vertices, edges, distance, DEFAULT_FORCE_STRENGTH);
        }

        @Override
        public LinkForce addLinkForce(Collection<Vertex> vertices, Collection<Edge> edges, double distance, double strength) {
            LinkForce force = new LinkForce(vertices, edges, distance, strength);
            add(force);
            return force;
        }

        @Override
        public ManyBodyForce addManyBodyForce(Collection<Vertex> vertices, double strength) {
            return addManyBodyForce(vertices, strength, Math.sqrt(Double.MAX_VALUE));
        }

        @Override
        public ManyBodyForce addManyBodyForce(Collection<Vertex> vertices, double strength, double distanceMax) {
            ManyBodyForce force = new ManyBodyForce(vertices, strength, distanceMax);
            add(force);
            return force;
        }

        @Override
        public XForce addXForce(Collection<Vertex> vertices, double x) {
            return addXForce(vertices, x, DEFAULT_FORCE_STRENGTH);
        }

        @Override
        public XForce addXForce(Collection<Vertex> vertices, double x, double strength) {
            return addXForce(vertices, constant(x), strength);
        }

        @Override
        public XForce addXForce(Collection<Vertex> vertices, Supplier<Double> x, double strength) {
            XForce force = new XForce(vertices, x, strength);
            add(force);
            return force;
        }

        @Override
        public YForce addYForce(Collection<Vertex> vertices, double y) {
            return addYForce(vertices, y, DEFAULT_FORCE_STRENGTH);
        }

        @Override
        public YForce addYForce(Collection<Vertex> vertices, double y, double strength) {
            return addYForce(vertices, constant(y), strength);
        }

        @Override
        public YForce addYForce(Collection<Vertex> vertices, Supplier<Double> y, double strength) {
            YForce force = new YForce(vertices, y, strength);
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
