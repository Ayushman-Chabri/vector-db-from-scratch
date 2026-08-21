package com.yourownai.json;

import java.util.List;

public final class JsonWriter {

    private JsonWriter() {
    }

    // Escapes a string for safe embedding inside a JSON string literal.
    // Jackson equivalent: new ObjectMapper().writeValueAsString(s)
    public static String jsonString(String s) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.append('"').toString();
    }

    // Serializes a float vector as a JSON array, 4 decimal places.
    public static String jsonVector(List<Float> v) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < v.size(); i++) {
            if (i > 0)
                out.append(',');
            out.append(String.format("%.4f", v.get(i)));
        }
        return out.append(']').toString();
    }
}