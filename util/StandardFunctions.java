package com.typedb.force.graph.util;

import java.util.function.Supplier;

public class StandardFunctions {
    public static Supplier<Double> constant(double value) {
        return () -> value;
    }
}
