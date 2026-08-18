package com.yourownai.distance;

public final class DistFnFactory {

    private DistFnFactory() {
    }

    public static DistFn getDistFn(String metric) {
        return switch (metric) {
            case "euclidean" -> Distance::euclidean;
            case "cosine" -> Distance::cosine;
            case "manhattan" -> Distance::manhattan;
            default -> throw new IllegalArgumentException("unknown metric: " + metric);
        };
    }
}