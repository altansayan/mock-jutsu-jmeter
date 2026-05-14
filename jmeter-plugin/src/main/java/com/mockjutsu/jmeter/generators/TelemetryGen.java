package com.mockjutsu.jmeter.generators;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TelemetryGen {
    private TelemetryGen() {}

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "fdr_record"      -> fdrRecord(rng);
            case "drone_telemetry" -> droneTelemetry(rng);
            default -> "ERROR: Unknown telemetry type '" + type + "'";
        };
    }

    private static String fdrRecord(ThreadLocalRandom rng) {
        double alt     = 30000 + rng.nextDouble(10000);
        double speed   = 400 + rng.nextDouble(300);
        double heading = rng.nextDouble(360);
        double vspeed  = rng.nextDouble(-500, 500);
        double lat     = rng.nextDouble(-90, 90);
        double lon     = rng.nextDouble(-180, 180);
        return String.format("{\"timestamp\":\"%s\",\"altitude_ft\":%.1f,\"airspeed_kts\":%.1f,\"heading_deg\":%.1f," +
               "\"vertical_speed_fpm\":%.1f,\"lat\":%.6f,\"lon\":%.6f,\"engine1_n1\":%.1f,\"engine2_n1\":%.1f}",
            java.time.Instant.now(), alt, speed, heading, vspeed, lat, lon,
            80 + rng.nextDouble(20), 80 + rng.nextDouble(20));
    }

    private static String droneTelemetry(ThreadLocalRandom rng) {
        String droneId = "UAV-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
        return String.format("{\"droneId\":\"%s\",\"timestamp\":\"%s\",\"lat\":%.6f,\"lon\":%.6f," +
               "\"alt_m\":%.1f,\"speed_ms\":%.2f,\"heading_deg\":%.1f,\"battery_pct\":%d," +
               "\"gps_sats\":%d,\"mode\":\"%s\"}",
            droneId, java.time.Instant.now(),
            rng.nextDouble(-90,90), rng.nextDouble(-180,180),
            rng.nextDouble(200), rng.nextDouble(20), rng.nextDouble(360),
            rng.nextInt(10,100), rng.nextInt(4,16),
            new String[]{"GUIDED","AUTO","LOITER","RTL"}[rng.nextInt(4)]);
    }
}
