package com.mockjutsu.jmeter;

import java.security.SecureRandom;

/**
 * Shared random sources — instantiated once per JVM, thread-safe.
 *
 * Previously each generator class declared its own {@code static final SecureRandom SEC},
 * resulting in 9 separate instances that all seeded from the OS entropy source at class-load
 * time and competed for /dev/urandom under concurrent JMeter threads. A single shared
 * instance eliminates redundant seeding cost and reduces memory overhead.
 *
 * {@link SecureRandom} is documented as thread-safe; sharing across generators is safe.
 */
public final class Randoms {

    private Randoms() {}

    /** Cryptographically strong random — shared across all generators. Thread-safe. */
    public static final SecureRandom SECURE = new SecureRandom();
}
