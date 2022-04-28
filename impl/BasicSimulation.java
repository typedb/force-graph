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

import com.vaticle.force.graph.api.Vertex;
import com.vaticle.force.graph.api.Simulation;
import com.vaticle.force.graph.api.Force;
import com.vaticle.force.graph.force.CollideForce;
import com.vaticle.force.graph.force.ManyBodyForce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

public class BasicSimulation implements Simulation {
    private double alpha;
    private double alphaMin;
    private double alphaDecay;
    private double alphaTarget;
    private double velocityDecay;
    private final Forces forces;
    private final Forces localForces;
    private final List<Vertex> vertices;
    private final AtomicInteger nextNodeID;

    private static final int INITIAL_PLACEMENT_RADIUS = 10;
    private static final double INITIAL_PLACEMENT_ANGLE = Math.PI * (3 - Math.sqrt(5));

    public BasicSimulation() {
        this(Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
    }

    public BasicSimulation(int parallelism) {
        alpha = 1;
        alphaMin = 0.001;
        alphaDecay = 1 - Math.pow(alphaMin, 1.0 / 300);
        alphaTarget = 0;
        velocityDecay = 0.6;
        vertices = Collections.synchronizedList(new ArrayList<>());
        forces = Forces.global(vertices, parallelism);
        localForces = Forces.local();
        nextNodeID = new AtomicInteger();
    }

    @Override
    public Collection<Vertex> vertices() {
        return vertices;
    }

    @Override
    public Simulation.Forces forces() {
        return forces;
    }

    @Override
    public Simulation.Forces localForces() {
        return localForces;
    }

    @Override
    public synchronized void tick() {
        alpha += (alphaTarget - alpha) * alphaDecay;

        forces.applyAll(alpha);
        localForces.applyAll(alpha);

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
        forces.onGraphChanged();
        localForces.onGraphChanged();
    }

    protected void placeNode(Vertex vertex) {
        int id = nextNodeID.getAndIncrement();
        double radius = INITIAL_PLACEMENT_RADIUS * Math.sqrt(0.5 + id);
        double angle = id * INITIAL_PLACEMENT_ANGLE;
        vertex.x(vertex.x() + (vertex.isXFixed() ? 0 : radius * Math.cos(angle)));
        vertex.y(vertex.y() + (vertex.isYFixed() ? 0 : radius * Math.sin(angle)));
        vertices.add(vertex);
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
        vertices.clear();
        nextNodeID.set(0);
    }

    public static class Forces implements Simulation.Forces {
        final Collection<Force> forces;
        final List<Vertex> vertices;
        private final boolean isLocal;
        private final int threadCount;
        private final ExecutorService executor;

        private Forces(List<Vertex> vertices, boolean isLocal, int parallelism) {
            forces = new ArrayList<>();
            this.vertices = vertices;
            this.isLocal = isLocal;
            threadCount = parallelism;
            executor = isLocal ? null : Executors.newFixedThreadPool(threadCount);
        }

        static Forces global(List<Vertex> vertices, int parallelism) {
            return new Forces(vertices, false, parallelism);
        }

        static Forces local() {
            return new Forces(null, true, 1);
        }

        void applyAll(double alpha) {
            if (isLocal) applyAllSerial(alpha);
            else applyAllParallel(alpha);
        }

        private void applyAllSerial(double alpha) {
            forces.forEach(force -> force.apply(alpha));
        }

        private void applyAllParallel(double alpha) {
            buildQuadtrees();
            applyInterBodyForcesParallel(alpha);
            applySingleBodyForces(alpha);
        }

        private void buildQuadtrees() {
            for (Force force : forces) {
                if (force instanceof CollideForce) ((CollideForce) force).buildQuadtree();
                else if (force instanceof ManyBodyForce) ((ManyBodyForce) force).buildQuadtree();
            }
        }

        private void applyInterBodyForcesParallel(double alpha) {
            int taskCount = 8 * threadCount; // We make more tasks than threads because some tasks may need more time to compute.
            ArrayList<Future<?>> futures = new ArrayList<>();
            for (int t = taskCount; t > 0; t--) {
                int from = (int) Math.floor(vertices.size() * (t - 1) / taskCount);
                int to = (int) Math.floor(vertices.size() * t / taskCount);
                Future<?> future = executor.submit(() -> {
                    final List<Vertex> vertexPartition = vertices.subList(from, to);
                    for (Force force : forces) {
                        if (force instanceof ManyBodyForce || force instanceof CollideForce) force.apply(vertexPartition, alpha);
                    }
                });
                futures.add(future);
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void applySingleBodyForces(double alpha) {
            for (Force force : forces) {
                if (!(force instanceof ManyBodyForce) && !(force instanceof CollideForce)) {
                    force.apply(alpha);
                }
            }
        }

        void onGraphChanged() {
            forces.forEach(Force::onGraphChanged);
        }

        public <FORCE extends Force> FORCE add(FORCE force) {
            forces.add(requireNonNull(force));
            force.onGraphChanged();
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
