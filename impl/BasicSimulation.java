/*
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    }

    @Override
    public Collection<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public Simulation.Forces getForces() {
        return forces;
    }

    @Override
    public Simulation.Forces getLocalForces() {
        return localForces;
    }

    @Override
    public synchronized void tick() {
        alpha += (alphaTarget - alpha) * alphaDecay;

        forces.applyAll(alpha);
        localForces.applyAll(alpha);

        Collection<Vertex> allVertices = Stream.concat(
                getVertices().stream(),
                localForces.forces.stream().flatMap(force -> force.vertices().stream())
        ).collect(Collectors.toSet());

        for (Vertex vertex : allVertices) {
            if (vertex.isXFixed()) vertex.setVX(0);
            else {
                vertex.setVX(vertex.getVX() * velocityDecay);
                vertex.setX(vertex.getX() + vertex.getVX());
            }
            if (vertex.isYFixed()) vertex.setVY(0);
            else {
                vertex.setVY(vertex.getVY() * velocityDecay);
                vertex.setY(vertex.getY() + vertex.getVY());
            }
        }
    }

    @Override
    public void placeVertices(Collection<Vertex> vertices) {
        vertices.forEach(this::placeVertex);
    }

    @Override
    public synchronized void placeVertex(Vertex vertex) {
        int id = vertices.size();
        double radius = INITIAL_PLACEMENT_RADIUS * Math.sqrt(0.5 + id);
        double angle = id * INITIAL_PLACEMENT_ANGLE;
        vertex.setX(vertex.getX() + (vertex.isXFixed() ? 0 : radius * Math.cos(angle)));
        vertex.setY(vertex.getY() + (vertex.isYFixed() ? 0 : radius * Math.sin(angle)));
        vertices.add(vertex);
    }

    @Override
    public double getAlpha() {
        return alpha;
    }

    @Override
    public BasicSimulation setAlpha(double value) {
        alpha = value;
        return this;
    }

    @Override
    public double getAlphaMin() {
        return alphaMin;
    }

    @Override
    public BasicSimulation setAlphaMin(double value) {
        alphaMin = value;
        return this;
    }

    @Override
    public double getAlphaDecay() {
        return alphaDecay;
    }

    @Override
    public BasicSimulation setAlphaDecay(double value) {
        alphaDecay = value;
        return this;
    }

    @Override
    public double getAlphaTarget() {
        return alphaTarget;
    }

    @Override
    public BasicSimulation setAlphaTarget(double value) {
        alphaTarget = value;
        return this;
    }

    @Override
    public double getVelocityDecay() {
        return velocityDecay;
    }

    @Override
    public BasicSimulation setVelocityDecay(double value) {
        velocityDecay = value;
        return this;
    }

    @Override
    public synchronized void clear() {
        forces.clear();
        vertices.clear();
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

        public <FORCE extends Force> FORCE add(FORCE force) {
            forces.add(requireNonNull(force));
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
