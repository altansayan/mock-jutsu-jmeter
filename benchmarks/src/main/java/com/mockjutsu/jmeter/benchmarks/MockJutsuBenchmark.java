package com.mockjutsu.jmeter.benchmarks;

import com.mockjutsu.jmeter.MockJutsuRegistry;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * JMH throughput baseline for mock-jutsu-jmeter generator types.
 *
 * Run:
 *   cd jmeter-plugin && mvn install -DskipTests
 *   cd ../benchmarks && mvn package -DskipTests
 *   java -jar target/benchmarks.jar
 *
 * To run a single group:
 *   java -jar target/benchmarks.jar ".*Identity.*"
 *
 * Performance guardrail thresholds (established 2026-07-25, Java 17, i5-1135G7):
 *   Identity / Banking: should be < 5 µs/op
 *   Crypto (wallet, OIDC): < 500 µs/op
 *   JWT attack / fuzz: < 200 µs/op
 *
 * If a type regresses past 2× its baseline, investigate before merging.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class MockJutsuBenchmark {

    // ── Identity ──────────────────────────────────────────────────────────────

    @Benchmark
    public String tckn() {
        return MockJutsuRegistry.generate("tckn", "TR");
    }

    @Benchmark
    public String ssnUS() {
        return MockJutsuRegistry.generate("ssn", "US");
    }

    @Benchmark
    public String uuid() {
        return MockJutsuRegistry.generate("uuid", "TR");
    }

    // ── Financial ─────────────────────────────────────────────────────────────

    @Benchmark
    public String iban() {
        return MockJutsuRegistry.generate("iban", "TR");
    }

    @Benchmark
    public String creditCard() {
        return MockJutsuRegistry.generate("credit_card", "TR");
    }

    @Benchmark
    public String routingNumber() {
        return MockJutsuRegistry.generate("routing_number", "US");
    }

    // ── Crypto — hot path ─────────────────────────────────────────────────────

    @Benchmark
    public String ethWallet() {
        return MockJutsuRegistry.generate("eth_wallet", "TR");
    }

    @Benchmark
    public String btcWallet() {
        return MockJutsuRegistry.generate("btc_wallet", "TR");
    }

    @Benchmark
    public String solWallet() {
        return MockJutsuRegistry.generate("sol_wallet", "TR");
    }

    // ── OIDC / JWT ────────────────────────────────────────────────────────────

    @Benchmark
    public String oidcToken() {
        return MockJutsuRegistry.generate("oidc_token", "TR");
    }

    @Benchmark
    public String jwtAttack() {
        return MockJutsuRegistry.generate("jwt_attack", "TR");
    }

    // ── Compliance ────────────────────────────────────────────────────────────

    @Benchmark
    public String policyNumber() {
        return MockJutsuRegistry.generate("policy_number", "TR");
    }

    @Benchmark
    public String consentId() {
        return MockJutsuRegistry.generate("consent_id", "TR");
    }

    // ── Masking ───────────────────────────────────────────────────────────────

    @Benchmark
    public String tcknMasked() {
        return MockJutsuRegistry.generate("tckn_masked", "TR");
    }

    @Benchmark
    public String accountNumberMasked() {
        return MockJutsuRegistry.generate("account_number_masked", "TR");
    }

    // ── Entry point for IDE / manual run ────────────────────────────────────

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
            .include(MockJutsuBenchmark.class.getSimpleName())
            .forks(1)
            .build();
        new Runner(opt).run();
    }
}
