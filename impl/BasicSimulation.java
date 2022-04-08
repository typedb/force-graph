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

import com.vaticle.force.graph.api.Node;
import com.vaticle.force.graph.api.Simulation;
import com.vaticle.force.graph.api.Force;
import com.vaticle.force.graph.force.CollideForce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public class BasicSimulation implements Simulation {
    private double alpha;
    private double alphaMin;
    private double alphaDecay;
    private double alphaTarget;
    private double velocityDecay;
    protected final Collection<Force> forces;
    private final ConcurrentMap<Node, Integer> nodesIndexed;
    private final AtomicInteger nextNodeID;

    private static final int INITIAL_RADIUS = 10;
    private static final double INITIAL_ANGLE = Math.PI * (3 - Math.sqrt(5));

    public BasicSimulation() {
        alpha = 1;
        alphaMin = 0.001;
        alphaDecay = 1 - Math.pow(alphaMin, 1.0 / 300);
        alphaTarget = 0;
        velocityDecay = 0.6;
        forces = new ArrayList<>();
        nodesIndexed = new ConcurrentHashMap<>();
        nextNodeID = new AtomicInteger();
    }

    @Override
    public synchronized void tick() {
        alpha += (alphaTarget - alpha) * alphaDecay;

        forces.forEach(force -> force.apply(alpha));

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

    // TODO: I think we can get rid of 'synchronized' here since we're using ConcurrentHashMap
    public synchronized void placeNodes(Collection<Node> nodes) {
        nodes.forEach(this::placeNode);
        forces.forEach(Force::onNodesChanged);
    }

    protected void placeNode(Node node) {
        int id = nextNodeID.getAndIncrement();
        double radius = INITIAL_RADIUS * Math.sqrt(0.5 + id);
        double angle = id * INITIAL_ANGLE;
        node.x(node.x() + (node.isXFixed() ? 0 : radius * Math.cos(angle)));
        node.y(node.y() + (node.isYFixed() ? 0 : radius * Math.sin(angle)));
        nodesIndexed.put(node, id);
    }

    public Collection<Node> nodes() {
        return nodesIndexed.keySet();
    }

    public double alpha() {
        return alpha;
    }

    public BasicSimulation alpha(double value) {
        alpha = value;
        return this;
    }

    public double alphaMin() {
        return alphaMin;
    }

    public BasicSimulation alphaMin(double value) {
        alphaMin = value;
        return this;
    }

    public double alphaDecay() {
        return alphaDecay;
    }

    public BasicSimulation alphaDecay(double value) {
        alphaDecay = value;
        return this;
    }

    public double alphaTarget() {
        return alphaTarget;
    }

    public BasicSimulation alphaTarget(double value) {
        alphaTarget = value;
        return this;
    }

    public double velocityDecay() {
        return velocityDecay;
    }

    public BasicSimulation velocityDecay(double value) {
        velocityDecay = value;
        return this;
    }

    @Override
    public void addForce(Force force) {
        forces.add(requireNonNull(force));
        force.onNodesChanged();
    }

    @Override
    public boolean removeForce(Force force) {
        return forces.remove(force);
    }

    public void addCollideForce(Collection<Node> nodes, double radius) {
        forces.add(new CollideForce<>(nodes.stream().collect(toMap(x -> x, nodesIndexed::get)), radius));
    }

    public synchronized void clear() {
        forces.clear();
        nodesIndexed.clear();
        nextNodeID.set(0);
    }
}
