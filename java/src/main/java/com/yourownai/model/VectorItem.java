package com.yourownai.model;

import java.util.List;

public record VectorItem(int id, List<Float> vec, String label) {
}