package com.vaticle.force.graph.benchmark;

import com.vaticle.force.graph.api.Edge;
import com.vaticle.force.graph.api.Simulation;
import com.vaticle.force.graph.api.Vertex;
import com.vaticle.force.graph.force.LinkForce;
import com.vaticle.force.graph.impl.BasicEdge;
import com.vaticle.force.graph.impl.BasicSimulation;
import com.vaticle.force.graph.impl.BasicVertex;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BasicSimulationBenchmark {
    @Test
    public void star_graph() {
        final List<Vertex> vertices = new ArrayList<>();
        final List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < 10000; i++) vertices.add(new BasicVertex(0.0, 0.0));
        for (int i = 1; i < 10000; i++) edges.add(new BasicEdge(vertices.get(0), vertices.get(i)));
        final Simulation simulation = new BasicSimulation();
        simulation.alphaMin(0.01);
        simulation.placeVertices(vertices);
        simulation.forces().addCollideForce(vertices, 80.0);
        simulation.forces().addManyBodyForce(vertices, -500.0);
        simulation.forces().addLinkForce(vertices, edges, 100, 1);
        int iteration = 0;
        System.out.println("-- STAR GRAPH ---\n");
        final Instant simulationStart = Instant.now();
        while (simulation.alpha() > simulation.alphaMin()) {
            final Instant tickStart = Instant.now();
            simulation.tick();
            System.out.printf("star_graph iteration %d: alpha = %.3f, alphaMin = %.3f, vertices[1729].x = %.3f, execution time = %dms%n", iteration, simulation.alpha(), simulation.alphaMin(), vertices.get(1729).x(), Duration.between(tickStart, Instant.now()).toMillis());
            iteration++;
        }
        System.out.println();
        System.out.printf("star_graph total runtime: %dms%n", Duration.between(simulationStart, Instant.now()).toMillis());
    }

    @Test
    public void incremental_star_graph() {
        final List<Vertex> vertices = new ArrayList<>();
        final List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < 2000; i++) vertices.add(new BasicVertex(0.0, 0.0));
        for (int i = 1; i < 2000; i++) edges.add(new BasicEdge(vertices.get(0), vertices.get(i)));
        final Simulation simulation = new BasicSimulation();
        simulation.alphaMin(0.01);
        simulation.placeVertices(vertices);
        simulation.forces().addCollideForce(vertices, 80.0);
        simulation.forces().addManyBodyForce(vertices, -500.0);
        LinkForce linkForce = simulation.forces().addLinkForce(vertices, edges, 100, 1);
        int iteration = 0;
        System.out.println("-- INCREMENTAL STAR GRAPH ---\n");
        final Instant simulationStart = Instant.now();
        while (simulation.alpha() > simulation.alphaMin()) {
            final Instant tickStart = Instant.now();
            simulation.tick();
            System.out.printf("incremental_star_graph iteration %d: total vertices = %d, alpha = %.3f, alphaMin = %.3f, vertices[1729].x = %.3f, execution time = %dms%n", iteration, vertices.size(), simulation.alpha(), simulation.alphaMin(), vertices.get(1729).x(), Duration.between(tickStart, Instant.now()).toMillis());
            iteration++;
            if (iteration < 9) {
                final List<Vertex> newVertices = new ArrayList<>();
                for (int i = 0; i < 1000; i++) {
                    final Vertex vertex = new BasicVertex(0.0, 0.0);
                    newVertices.add(vertex);
                    edges.add(new BasicEdge(vertices.get(0), vertex));
                }
                vertices.addAll(newVertices);
                simulation.placeVertices(newVertices);
                simulation.forces().remove(linkForce);
                linkForce = simulation.forces().addLinkForce(vertices, edges, 100, 1);
            }
        }
        System.out.println();
        System.out.printf("incremental_star_graph total runtime: %dms%n", Duration.between(simulationStart, Instant.now()).toMillis());
    }
}
