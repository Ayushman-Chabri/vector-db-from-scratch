package com.yourownai.server;

public record CompareResult(String model, String answer, long latencyMs, boolean skipped) {
}