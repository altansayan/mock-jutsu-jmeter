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
        // Unit quaternion: x,y,z,w with |q|=1
        double x = rng.nextDouble(-1, 1);
        double y = rng.nextDouble(-1, 1);
        double z = rng.nextDouble(-1, 1);
        double w = Math.sqrt(Math.max(0, 1 - x*x - y*y - z*z));
        return String.format("{\"x\":%.6f,\"y\":%.6f,\"z\":%.6f,\"w\":%.6f}", x, y, z, w);
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
