package com.mockjutsu.jmeter.generators;

import java.util.concurrent.ThreadLocalRandom;

public final class AiVectorGen {
    private AiVectorGen() {}

    public static String generate(String type, String locale) {
        return generate(type, locale, "");
    }

    public static String generate(String type, String locale, String qualifier) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        return switch (type) {
            case "ai_embedding" -> {
                int dim = 1536;
                if (!qualifier.isEmpty()) try { dim = Math.max(1, Integer.parseInt(qualifier)); } catch (NumberFormatException ignored) {}
                yield embedding(rng, dim);
            }
            case "ai_vector" -> {
                int dim = 384;
                if (!qualifier.isEmpty()) try { dim = Math.max(1, Integer.parseInt(qualifier)); } catch (NumberFormatException ignored) {}
                yield embedding(rng, dim);
            }
            case "ai_sparse_vector" -> {
                int dims = 30000, nnz = -1;
                if (!qualifier.isEmpty()) {
                    String[] parts = qualifier.split("\\|", 2);
                    try { dims = Math.max(1, Integer.parseInt(parts[0])); } catch (NumberFormatException ignored) {}
                    if (parts.length > 1) try { nnz = Math.max(1, Integer.parseInt(parts[1])); } catch (NumberFormatException ignored) {}
                }
                yield sparseVector(rng, dims, nnz);
            }
            default -> "ERROR: Unknown AI vector type '" + type + "'";
        };
    }

    private static String embedding(ThreadLocalRandom rng, int dim) {
        double[] values = new double[dim];
        double norm = 0.0;
        for (int i = 0; i < dim; i++) {
            values[i] = -1.0 + rng.nextDouble(2.0);
            norm += values[i] * values[i];
        }
        norm = Math.sqrt(norm);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < dim; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%.6f", values[i] / norm));
        }
        return sb.append("]").toString();
    }

    private static String sparseVector(ThreadLocalRandom rng, int dims, int nnzOverride) {
        int nnz = nnzOverride > 0 ? nnzOverride : rng.nextInt(10, 50);
        nnz = Math.min(nnz, dims);
        StringBuilder sb = new StringBuilder("{\"indices\":[");
        java.util.TreeSet<Integer> indices = new java.util.TreeSet<>();
        while (indices.size() < nnz) indices.add(rng.nextInt(0, dims));
        boolean first = true;
        for (int idx : indices) { if (!first) sb.append(","); sb.append(idx); first = false; }
        sb.append("],\"values\":[");
        first = true;
        for (int i = 0; i < nnz; i++) { if (!first) sb.append(","); sb.append(String.format("%.4f", rng.nextDouble())); first = false; }
        sb.append("]}");
        return sb.toString();
    }
}
