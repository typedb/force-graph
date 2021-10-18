package com.vaticle.force.graph;

import java.util.Random;
import java.util.function.Supplier;

public class RandomEffects {

    public static double jiggle() {
        Random random = new Random();
        return jiggle(random::nextDouble);
    }

    public static double jiggle(Supplier<Double> randomSource) {
        return (randomSource.get() - 0.5) * 1e-6;
    }
}
