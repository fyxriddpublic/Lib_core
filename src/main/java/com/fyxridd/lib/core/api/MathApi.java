package com.fyxridd.lib.core.api;

import org.apache.commons.math3.random.RandomDataGenerator;

public class MathApi {
    public static final org.apache.commons.math3.random.RandomDataGenerator RandomDataGenerator = new RandomDataGenerator();

    public static int nextInt(int lower, int upper) {
        return RandomDataGenerator.nextInt(lower, upper);
    }

    public static long nextLong(long lower, long upper) {
        return RandomDataGenerator.nextLong(lower, upper);
    }
}
