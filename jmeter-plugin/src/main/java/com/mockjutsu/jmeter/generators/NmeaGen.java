package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** NMEA — GPGGA, GPRMC with correct NMEA checksum. Mirrors nmea.py. */
public final class NmeaGen {
    private NmeaGen() {}

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "nmea_gpgga" -> gpgga(rng);
            case "nmea_gprmc" -> gprmc(rng);
            default -> "ERROR: Unknown NMEA type '" + type + "'";
        };
    }

    // ── GPGGA ─────────────────────────────────────────────────────────────────

    private static String gpgga(ThreadLocalRandom rng) {
        String time   = String.format("%02d%02d%05.2f", rng.nextInt(0,24), rng.nextInt(0,60), rng.nextDouble(0,60));
        String lat    = nmeaLat(rng);
        char   latH   = rng.nextBoolean() ? 'N' : 'S';
        String lon    = nmeaLon(rng);
        char   lonH   = rng.nextBoolean() ? 'E' : 'W';
        int    fix    = rng.nextInt(1,7);
        int    sats   = rng.nextInt(4,12);
        double hdop   = 0.8 + rng.nextDouble(3);
        double alt    = rng.nextDouble(500);
        String body   = String.format("GPGGA,%s,%s,%c,%s,%c,%d,%02d,%.1f,%.1f,M,0.0,M,,",
            time, lat, latH, lon, lonH, fix, sats, hdop, alt);
        return "$" + body + "*" + nmeaChecksum(body);
    }

    // ── GPRMC ─────────────────────────────────────────────────────────────────

    private static String gprmc(ThreadLocalRandom rng) {
        String time   = String.format("%02d%02d%05.2f", rng.nextInt(0,24), rng.nextInt(0,60), rng.nextDouble(0,60));
        String date   = String.format("%02d%02d%02d", rng.nextInt(1,29), rng.nextInt(1,13), rng.nextInt(24,30));
        String lat    = nmeaLat(rng);
        char   latH   = rng.nextBoolean() ? 'N' : 'S';
        String lon    = nmeaLon(rng);
        char   lonH   = rng.nextBoolean() ? 'E' : 'W';
        double speed  = rng.nextDouble(100);
        double course = rng.nextDouble(360);
        String body   = String.format("GPRMC,%s,A,%s,%c,%s,%c,%.1f,%.1f,%s,,,A",
            time, lat, latH, lon, lonH, speed, course, date);
        return "$" + body + "*" + nmeaChecksum(body);
    }

    // ── NMEA checksum: XOR of all bytes between $ and * ──────────────────────

    static String nmeaChecksum(String sentence) {
        int cs = 0;
        for (char c : sentence.toCharArray()) cs ^= c;
        return String.format("%02X", cs);
    }

    private static String nmeaLat(ThreadLocalRandom rng) {
        int deg   = rng.nextInt(0,90);
        double min = rng.nextDouble(60);
        return String.format("%02d%07.4f", deg, min);
    }

    private static String nmeaLon(ThreadLocalRandom rng) {
        int deg   = rng.nextInt(0,180);
        double min = rng.nextDouble(60);
        return String.format("%03d%07.4f", deg, min);
    }
}
