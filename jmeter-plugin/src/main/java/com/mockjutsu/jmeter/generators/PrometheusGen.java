package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class PrometheusGen {
    private PrometheusGen() {}

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "prometheus_metrics"   -> prometheusMetrics(rng);
            case "openmetrics_snapshot" -> openMetrics(rng);
            default -> "ERROR: Unknown prometheus type '" + type + "'";
        };
    }

    private static String prometheusMetrics(ThreadLocalRandom rng) {
        double cpuUsage  = rng.nextDouble(100);
        double memUsage  = rng.nextDouble(100);
        long   requests  = rng.nextLong(1000, 9999999);
        double p99lat    = rng.nextDouble(0.001, 2.0);
        long   ts        = System.currentTimeMillis();
        return "# HELP http_requests_total Total HTTP requests\n" +
               "# TYPE http_requests_total counter\n" +
               "http_requests_total{method=\"GET\",status=\"200\"} " + requests + " " + ts + "\n" +
               "# HELP process_cpu_usage CPU usage fraction\n" +
               "# TYPE process_cpu_usage gauge\n" +
               "process_cpu_usage " + String.format("%.4f", cpuUsage/100) + "\n" +
               "# HELP jvm_memory_used_bytes JVM memory used\n" +
               "# TYPE jvm_memory_used_bytes gauge\n" +
               "jvm_memory_used_bytes{area=\"heap\"} " + (long)(memUsage * 1024 * 1024) + "\n" +
               "# HELP http_request_duration_seconds HTTP request latency\n" +
               "# TYPE http_request_duration_seconds summary\n" +
               "http_request_duration_seconds{quantile=\"0.99\"} " + String.format("%.6f", p99lat);
    }

    private static String openMetrics(ThreadLocalRandom rng) {
        return "# TYPE cpu_usage gauge\n# UNIT cpu_usage ratio\n" +
               "cpu_usage " + String.format("%.4f", rng.nextDouble()) + "\n" +
               "# TYPE memory_used_bytes gauge\n# UNIT memory_used_bytes bytes\n" +
               "memory_used_bytes " + rng.nextLong(100000000L, 2000000000L) + "\n" +
               "# EOF";
    }
}
