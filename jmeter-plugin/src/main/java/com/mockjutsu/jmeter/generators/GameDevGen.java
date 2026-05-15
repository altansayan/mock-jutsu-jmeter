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
        // Uniform rotation distribution via Gaussian sampling (Shoemake 1992)
        // nextDouble(-1,1) produces non-uniform rotations; Gaussian normalisation is correct.
        double[] q = randomQuaternion(rng);
        return String.format("{\"x\":%.6f,\"y\":%.6f,\"z\":%.6f,\"w\":%.6f}", q[0], q[1], q[2], q[3]);
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
        int nodeCount = rng.nextInt(3, 10);
        StringBuilder sb = new StringBuilder("{\"nodes\":[");
        for (int i = 0; i < nodeCount; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"x\":%.2f,\"y\":0.0,\"z\":%.2f}", rng.nextDouble(-100,100), rng.nextDouble(-100,100)));
        }
        sb.append("],\"totalLength\":").append(String.format("%.2f", rng.nextDouble(10,500)));
        sb.append("}");
        return sb.toString();
    }
}
