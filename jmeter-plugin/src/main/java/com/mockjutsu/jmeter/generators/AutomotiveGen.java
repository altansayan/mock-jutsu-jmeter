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
        boolean extended = rng.nextBoolean();
        int arbId  = extended ? rng.nextInt(0x001, 0x1FFFFFFF) : rng.nextInt(0x001, 0x7FF);
        int dlc    = rng.nextInt(1, 9);
        StringBuilder data = new StringBuilder();
        StringBuilder dataHex = new StringBuilder();
        for (int i = 0; i < dlc; i++) {
            int b = rng.nextInt(0, 256);
            if (i > 0) { data.append(' '); dataHex.append(' '); }
            data.append(String.format("%02X", b));
            dataHex.append(String.format("%02X", b));
        }
        // CRC15 stub (mirrors automotive.py)
        int crc15 = arbId ^ dlc ^ rng.nextInt(0x7FFF);
        String frameType = extended ? "extended" : "standard";
        String canIdStr = extended ? String.format("0x%08X", arbId) : String.format("0x%03X", arbId);
        String socketcan = String.format("%s#%s", canIdStr.substring(2), dataHex.toString().replace(" ", ""));
        return String.format(
            "{\"frame_type\":\"%s\",\"can_id\":\"%s\",\"can_id_int\":%d,\"dlc\":%d," +
            "\"data\":\"%s\",\"data_hex\":\"%s\",\"crc15\":%d,\"crc15_hex\":\"0x%04X\",\"socketcan\":\"%s\"}",
            frameType, canIdStr, arbId, dlc, data, dataHex, crc15, crc15, socketcan);
    }

    private static String obd2Response(ThreadLocalRandom rng) {
        // Full snapshot mirroring automotive.py
        String ecuId = "7E8";
        double rpm    = 600 + rng.nextDouble(5400);
        double speed  = rng.nextDouble(200);
        double cool   = 60 + rng.nextDouble(60);
        double throt  = rng.nextDouble(100);
        double load   = rng.nextDouble(100);
        double fuel   = rng.nextDouble(100);
        // Build pids array
        String pids = String.format(java.util.Locale.US,
            "[{\"pid\":\"0C\",\"name\":\"Engine RPM\",\"value\":%.1f,\"unit\":\"rpm\"}," +
            "{\"pid\":\"0D\",\"name\":\"Vehicle Speed\",\"value\":%.1f,\"unit\":\"km/h\"}," +
            "{\"pid\":\"05\",\"name\":\"Coolant Temp\",\"value\":%.1f,\"unit\":\"C\"}]",
            rpm, speed, cool);
        String dtcs = "[]";
        return String.format(java.util.Locale.US,
            "{\"ecu_id\":\"%s\",\"mode\":\"01\",\"pids\":%s,\"dtcs\":%s,\"dtc_count\":0," +
            "\"rpm\":%.1f,\"speed_kmh\":%.1f,\"coolant_temp_c\":%.1f," +
            "\"throttle_pct\":%.1f,\"engine_load_pct\":%.1f,\"fuel_level_pct\":%.1f}",
            ecuId, pids, dtcs, rpm, speed, cool, throt, load, fuel);
    }
}
