package com.mockjutsu.jmeter;

import java.security.SecureRandom;

/**
 * Shared random sources for all generators.
 *
 * {@code SECURE} is a {@link ThreadLocal} so each JMeter thread owns its own
 * {@link SecureRandom} instance. This eliminates the internal lock contention that
 * occurs when multiple threads share a single {@code SecureRandom} — every
 * {@code nextBytes()} call on a shared instance acquires a monitor, forming a queue
 * under high concurrency. Per-thread instances have zero contention while retaining
 * full cryptographic strength (each is independently seeded from OS entropy).
 */
public final class Randoms {

    private Randoms() {}

    /** Cryptographically strong random — one instance per JMeter thread, zero contention. */
    public static final ThreadLocal<SecureRandom> SECURE =
        ThreadLocal.withInitial(SecureRandom::new);
}
