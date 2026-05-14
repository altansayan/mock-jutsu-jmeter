package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

/** TLE — Two-Line Element set (satellite orbital data). Mirrors tle.py. */
public final class TleGen {
    private TleGen() {}

    public static String generate(String type, String locale) {
        if (!"tle_satellite".equals(type)) return "ERROR: Unknown TLE type '" + type + "'";
        return tleSatellite(ThreadLocalRandom.current());
    }

    private static String tleSatellite(ThreadLocalRandom rng) {
        // Name line
        String name = "MOCK-SAT-" + String.format("%04d", rng.nextInt(1, 9999));

        // Line 1: satellite number, epoch, drag, etc.
        int    satNum    = rng.nextInt(10000, 99999);
        int    epochYear = rng.nextInt(24, 30);
        double epochDay  = 1 + rng.nextDouble(365);
        double meanMotion2 = rng.nextDouble(-0.00001, 0.00001);
        int    elsetNum  = rng.nextInt(1, 999);

        String line1 = String.format("1 %05dU %02d%03d%-3s %02d%012.8f %+.8f  00000-0  00000-0 0 %4d",
            satNum, epochYear, rng.nextInt(1,365), "A  ",
            epochYear, epochDay, meanMotion2, elsetNum);
        // Append checksum
        line1 = line1.substring(0,68) + tleChecksum(line1.substring(0,68));

        // Line 2: inclination, RAAN, eccentricity, arg perigee, mean anomaly, mean motion
        double incl  = rng.nextDouble(0, 180);
        double raan  = rng.nextDouble(0, 360);
        double ecc   = rng.nextDouble(0, 0.3);
        double argP  = rng.nextDouble(0, 360);
        double anom  = rng.nextDouble(0, 360);
        double mm    = rng.nextDouble(1, 17);
        int    revNo = rng.nextInt(1, 99999);

        String eccStr = String.format("%.7f", ecc).substring(2); // remove "0."
        String line2 = String.format("2 %05d %8.4f %8.4f %s %8.4f %8.4f %11.8f%5d",
            satNum, incl, raan, eccStr, argP, anom, mm, revNo);
        // Append checksum
        line2 = line2.substring(0,68) + tleChecksum(line2.substring(0,68));

        return name + "\n" + line1 + "\n" + line2;
    }

    static int tleChecksum(String line) {
        int sum = 0;
        for (char c : line.toCharArray()) {
            if (Character.isDigit(c)) sum += c - '0';
            else if (c == '-') sum += 1;
        }
        return sum % 10;
    }
}
