package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class AiVectorGen {
    private AiVectorGen() {}

    public static String generate(String type, String locale) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "ai_embedding"     -> embedding(rng, 768);
            case "ai_vector"        -> embedding(rng, 1536);
            case "ai_sparse_vector" -> sparseVector(rng);
            default -> "ERROR: Unknown AI vector type '" + type + "'";
        };
    }

    private static String embedding(ThreadLocalRandom rng, int dim) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < dim; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%.6f", -1.0 + rng.nextDouble(2)));
        }
        return sb.append("]").toString();
    }

    private static String sparseVector(ThreadLocalRandom rng) {
        int nnz = rng.nextInt(10, 50);
        StringBuilder sb = new StringBuilder("{\"indices\":[");
        java.util.TreeSet<Integer> indices = new java.util.TreeSet<>();
        while (indices.size() < nnz) indices.add(rng.nextInt(0, 30000));
        boolean first = true;
        for (int idx : indices) { if (!first) sb.append(","); sb.append(idx); first = false; }
        sb.append("],\"values\":[");
        first = true;
        for (int i = 0; i < nnz; i++) { if (!first) sb.append(","); sb.append(String.format("%.4f", rng.nextDouble())); first = false; }
        sb.append("]}");
        return sb.toString();
    }
}
