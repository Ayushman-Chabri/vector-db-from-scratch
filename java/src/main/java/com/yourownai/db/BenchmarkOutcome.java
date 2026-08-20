package com.yourownai.db;

public record BenchmarkOutcome(long bruteForceMicros, long kdTreeMicros, long hnswMicros, int itemCount) {
}