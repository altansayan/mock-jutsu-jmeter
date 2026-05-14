package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class AutomotiveGen {
    private AutomotiveGen() {}

    // Common OBD-II PIDs
    private static final int[][] OBD_PIDS = {
        {0x0C, 2}, // Engine RPM
        {0x0D, 1}, // Vehicle speed
        {0x05, 1}, // Coolant temp
        {0x04, 1}, // Engine load
        {0x11, 1}, // Throttle position
    };
    private static final String[] OBD_NAMES = {"EngineRPM","VehicleSpeed","CoolantTemp","EngineLoad","ThrottlePos"};

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "can_frame"     -> canFrame(rng);
            case "obd2_response" -> obd2Response(rng);
            default -> "ERROR: Unknown automotive type '" + type + "'";
        };
    }

    private static String canFrame(ThreadLocalRandom rng) {
        int arbId  = rng.nextInt(0x001, 0x7FF);  // 11-bit standard ID
        int dlc    = rng.nextInt(1, 9);            // Data Length Code
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < dlc; i++) {
            if (i > 0) data.append(' ');
            data.append(String.format("%02X", rng.nextInt(0, 256)));
        }
        return String.format("{\"id\":\"0x%03X\",\"dlc\":%d,\"data\":\"%s\",\"ts\":%d}", arbId, dlc, data, System.currentTimeMillis());
    }

    private static String obd2Response(ThreadLocalRandom rng) {
        int idx  = rng.nextInt(OBD_PIDS.length);
        int pid  = OBD_PIDS[idx][0];
        String name = OBD_NAMES[idx];
        double val;
        String unit;
        switch (pid) {
            case 0x0C -> { val = rng.nextDouble(500, 6000); unit = "rpm"; }
            case 0x0D -> { val = rng.nextDouble(0, 200);    unit = "km/h"; }
            case 0x05 -> { val = rng.nextDouble(60, 120);   unit = "°C"; }
            case 0x04 -> { val = rng.nextDouble(0, 100);    unit = "%"; }
            default   -> { val = rng.nextDouble(0, 100);    unit = "%"; }
        }
        return String.format("{\"pid\":\"0x%02X\",\"name\":\"%s\",\"value\":%.1f,\"unit\":\"%s\"}", pid, name, val, unit);
    }
}
