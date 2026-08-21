package com.yourownai.db;

import java.util.List;

public record DocItem(int id, String title, String text, List<Float> embedding) {
}