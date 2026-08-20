package com.yourownai.db;

import java.util.List;

public record SearchOutcome(List<SearchHit> hits, long micros, String algo, String metric) {
}