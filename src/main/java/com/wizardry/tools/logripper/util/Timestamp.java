package com.wizardry.tools.logripper.util;

import java.time.Duration;
import java.time.Instant;

/**
 * Simple Timestamp Record. Can be instantiated with or without a pre-defined Instant.
 * At any point in time, the total Duration or total time elapsed in milliseconds can be retrieved.
 * @param start
 */
public record Timestamp (Instant start) {

    public Timestamp() {
        this(Instant.now());
    }

    public static Timestamp now() {
        return new Timestamp(Instant.now());
    }

    public Duration getDuration() {
        return Duration.between(start, Instant.now());
    }

    public long toMillis() {
        return getDuration().toMillis();
    }
}
