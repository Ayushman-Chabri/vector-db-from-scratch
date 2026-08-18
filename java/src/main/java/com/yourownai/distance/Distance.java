package com.yourownai.distance;

import java.util.List;

public final class Distance {

    private Distance() {} // no instances — pure utility class

    public static double euclidean(List<Float> a, List<Float> b) {
        double sum = 0.0;
        for (int i = 0; i < a.size(); i++) {
            double diff = a.get(i) - b.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    public static double manhattan(List<Float> a, List<Float> b) {
        double sum = 0.0;
        for (int i = 0; i < a.size(); i++) {
            sum += Math.abs(a.get(i) - b.get(i));
        }
        return sum;
    }

    public static double cosine(List<Float> a, List<Float> b) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        // cosine distance = 1 - cosine similarity
        return 1.0 - (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}