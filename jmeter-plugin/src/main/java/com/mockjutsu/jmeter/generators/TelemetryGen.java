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

    private static final String[] AIRCRAFT_TYPES = {"A320","B737","B777","A380","A350","B787"};

    private static String fdrRecord(ThreadLocalRandom rng) {
        // Mirrors telemetry.py: time-series with {flight_id, aircraft, recording_start, interval_ms, samples}
        String flightId = UUID.randomUUID().toString();
        String aircraft = AIRCRAFT_TYPES[rng.nextInt(AIRCRAFT_TYPES.length)];
        String recStart = java.time.Instant.now().toString();
        int intervalMs  = 100 + rng.nextInt(900);
        int sampleCount = 5 + rng.nextInt(11);
        StringBuilder samples = new StringBuilder("[");
        for (int i = 0; i < sampleCount; i++) {
            if (i > 0) samples.append(",");
            samples.append(String.format(java.util.Locale.US,
                "{\"altitude_ft\":%.1f,\"airspeed_kts\":%.1f,\"heading_deg\":%.1f," +
                "\"vertical_speed_fpm\":%.1f,\"lat\":%.6f,\"lon\":%.6f}",
                30000 + rng.nextDouble(10000), 400 + rng.nextDouble(300),
                rng.nextDouble(360), rng.nextDouble(-500, 500),
                rng.nextDouble(-90, 90), rng.nextDouble(-180, 180)));
        }
        samples.append("]");
        return "{\"flight_id\":\"" + flightId + "\",\"aircraft\":\"" + aircraft + "\"," +
               "\"recording_start\":\"" + recStart + "\",\"interval_ms\":" + intervalMs + "," +
               "\"samples\":" + samples + "}";
    }

    private static final String[] DRONE_TYPES = {"PARROT-ANAFI","DJI-MINI","MAVIC-AIR","PHANTOM-4","YUNEEC-H520"};

    private static String droneTelemetry(ThreadLocalRandom rng) {
        // Mirrors telemetry.py: time-series with {drone_id, mission_id, recording_start, interval_ms, samples}
        String droneId   = DRONE_TYPES[rng.nextInt(DRONE_TYPES.length)] + "-" + String.format("%04d", rng.nextInt(10000));
        String missionId = UUID.randomUUID().toString();
        String recStart  = java.time.Instant.now().toString();
        int intervalMs   = 50 + rng.nextInt(450);
        int sampleCount  = 5 + rng.nextInt(11);
        StringBuilder samples = new StringBuilder("[");
        for (int i = 0; i < sampleCount; i++) {
            if (i > 0) samples.append(",");
            samples.append(String.format(java.util.Locale.US,
                "{\"lat\":%.6f,\"lon\":%.6f,\"alt_m\":%.1f,\"speed_ms\":%.2f," +
                "\"heading_deg\":%.1f,\"battery_pct\":%d,\"gps_sats\":%d}",
                rng.nextDouble(-90,90), rng.nextDouble(-180,180), rng.nextDouble(200),
                rng.nextDouble(20), rng.nextDouble(360), rng.nextInt(10,100), rng.nextInt(4,16)));
        }
        samples.append("]");
        return "{\"drone_id\":\"" + droneId + "\",\"mission_id\":\"" + missionId + "\"," +
               "\"recording_start\":\"" + recStart + "\",\"interval_ms\":" + intervalMs + "," +
               "\"samples\":" + samples + "}";
    }
}
