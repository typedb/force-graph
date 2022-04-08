package com.vaticle.force.graph.force;

import java.util.function.Supplier;

public class StandardFunctions {
    public static Supplier<Double> constant(double value) {
        return () -> value;
    }
}
