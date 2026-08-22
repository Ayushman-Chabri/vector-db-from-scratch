package com.yourownai.server;

// accuracyScore is a cosine-similarity average against reference facts,
// in [0,1] — or -1 if no facts were supplied for this question, meaning
// "not scored" rather than "scored zero."
public record CompareResult(String model, String answer, long latencyMs, boolean skipped, double accuracyScore) {
}