package com.yourownai.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TextChunker {

    private TextChunker() {
    }

    public static List<String> chunkText(String text) {
        return chunkText(text, 250, 30);
    }

    public static List<String> chunkText(String text, int chunkWords, int overlapWords) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> words = Arrays.asList(trimmed.split("\\s+"));
        if (words.size() <= chunkWords) {
            return List.of(text);
        }

        List<String> chunks = new ArrayList<>();
        int step = chunkWords - overlapWords;
        for (int i = 0; i < words.size(); i += step) {
            int end = Math.min(i + chunkWords, words.size());
            chunks.add(String.join(" ", words.subList(i, end)));
            if (end == words.size()) {
                break;
            }
        }
        return chunks;
    }
}