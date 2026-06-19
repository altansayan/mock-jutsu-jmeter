package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class GameDevGen {
    private GameDevGen() {}

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "quaternion"   -> quaternion(rng);
            case "navmesh_path" -> navmeshPath(rng);
            default -> "ERROR: Unknown gamedev type '" + type + "'";
        };
    }

    private static String quaternion(ThreadLocalRandom rng) {
        // Mirrors gamedev.py: adds magnitude (always ~1.0) and euler_degrees
        double[] q = randomQuaternion(rng);
        double mag = Math.sqrt(q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]);
        // Convert quaternion to Euler angles (yaw/pitch/roll)
        double sinrCosp = 2 * (q[3]*q[0] + q[1]*q[2]);
        double cosrCosp = 1 - 2 * (q[0]*q[0] + q[1]*q[1]);
        double roll  = Math.toDegrees(Math.atan2(sinrCosp, cosrCosp));
        double sinp  = 2 * (q[3]*q[1] - q[2]*q[0]);
        double pitch = Math.toDegrees(Math.abs(sinp) >= 1 ? Math.copySign(Math.PI/2, sinp) : Math.asin(sinp));
        double sinyCosp = 2 * (q[3]*q[2] + q[0]*q[1]);
        double cosyCosp = 1 - 2 * (q[1]*q[1] + q[2]*q[2]);
        double yaw   = Math.toDegrees(Math.atan2(sinyCosp, cosyCosp));
        return String.format(java.util.Locale.US,
            "{\"x\":%.6f,\"y\":%.6f,\"z\":%.6f,\"w\":%.6f,\"magnitude\":%.6f," +
            "\"euler_degrees\":{\"roll\":%.4f,\"pitch\":%.4f,\"yaw\":%.4f}}",
            q[0], q[1], q[2], q[3], mag, roll, pitch, yaw);
    }

    private static double[] randomQuaternion(ThreadLocalRandom rng) {
        // Gaussian sampling for uniform rotation distribution (Shoemake 1992)
        double x, y, z, w, norm;
        do {
            x = rng.nextGaussian();
            y = rng.nextGaussian();
            z = rng.nextGaussian();
            w = rng.nextGaussian();
            norm = Math.sqrt(x*x + y*y + z*z + w*w);
        } while (norm == 0);
        return new double[]{x/norm, y/norm, z/norm, w/norm};
    }

    private static String navmeshPath(ThreadLocalRandom rng) {
        // Mirrors gamedev.py: {waypoints, start, end, total_distance, waypoint_count}
        int nodeCount = rng.nextInt(3, 10);
        double startX = rng.nextDouble(-100, 100);
        double startY = rng.nextDouble(-10, 10);
        double startZ = rng.nextDouble(-100, 100);
        double endX   = rng.nextDouble(-100, 100);
        double endY   = rng.nextDouble(-10, 10);
        double endZ   = rng.nextDouble(-100, 100);
        StringBuilder waypointsSb = new StringBuilder("[");
        double totalDist = 0;
        double prevX = startX, prevZ = startZ;
        for (int i = 0; i < nodeCount; i++) {
            if (i > 0) waypointsSb.append(",");
            double wx = i == 0 ? startX : (i == nodeCount-1 ? endX : rng.nextDouble(-100,100));
            double wz = i == 0 ? startZ : (i == nodeCount-1 ? endZ : rng.nextDouble(-100,100));
            waypointsSb.append(String.format(java.util.Locale.US, "{\"x\":%.3f,\"y\":%.3f,\"z\":%.3f}", wx, rng.nextDouble(-10,10), wz));
            totalDist += Math.sqrt((wx-prevX)*(wx-prevX) + (wz-prevZ)*(wz-prevZ));
            prevX = wx; prevZ = wz;
        }
        waypointsSb.append("]");
        return String.format(java.util.Locale.US,
            "{\"waypoints\":%s,\"start\":{\"x\":%.3f,\"y\":%.3f,\"z\":%.3f}," +
            "\"end\":{\"x\":%.3f,\"y\":%.3f,\"z\":%.3f}," +
            "\"total_distance\":%.2f,\"waypoint_count\":%d}",
            waypointsSb, startX, startY, startZ, endX, endY, endZ, totalDist, nodeCount);
    }
}
