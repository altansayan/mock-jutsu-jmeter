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
        String expo = "# HELP http_requests_total Total HTTP requests " +
               "# TYPE http_requests_total counter " +
               "http_requests_total{method=\\\"GET\\\",status=\\\"200\\\"} " + requests + " " + ts + " " +
               "# HELP process_cpu_usage CPU usage fraction " +
               "# TYPE process_cpu_usage gauge " +
               "process_cpu_usage " + String.format(java.util.Locale.US, "%.4f", cpuUsage/100) + " " +
               "# HELP jvm_memory_used_bytes JVM memory used " +
               "# TYPE jvm_memory_used_bytes gauge " +
               "jvm_memory_used_bytes{area=\\\"heap\\\"} " + (long)(memUsage * 1024 * 1024) + " " +
               "# HELP http_request_duration_seconds HTTP request latency " +
               "# TYPE http_request_duration_seconds summary " +
               "http_request_duration_seconds{quantile=\\\"0.99\\\"} " + String.format(java.util.Locale.US, "%.6f", p99lat);
        return "{\"exposition\":\"" + expo + "\",\"format\":\"prometheus\",\"metric_families\":4,\"total_samples\":4}";
    }

    private static String openMetrics(ThreadLocalRandom rng) {
        String expo = "# TYPE cpu_usage gauge # UNIT cpu_usage ratio " +
               "cpu_usage " + String.format(java.util.Locale.US, "%.4f", rng.nextDouble()) + " " +
               "# TYPE memory_used_bytes gauge # UNIT memory_used_bytes bytes " +
               "memory_used_bytes " + rng.nextLong(100000000L, 2000000000L) + " # EOF";
        return "{\"exposition\":\"" + expo + "\",\"format\":\"openmetrics\",\"metric_families\":2,\"total_samples\":2}";
    }
}
