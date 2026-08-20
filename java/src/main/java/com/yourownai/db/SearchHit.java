package com.yourownai.db;

import java.util.List;

public record SearchHit(int id, String label, List<Float> vec, double distance) {
}