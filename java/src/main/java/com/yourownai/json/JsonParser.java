package com.yourownai.json;

import java.util.ArrayList;
import java.util.List;

public final class JsonParser {

    private JsonParser() {
    }

    // Parses a comma-separated string like "0.1,0.2,0.3" into a float list.
    public static List<Float> parseVector(String s) {
        List<Float> v = new ArrayList<>();
        for (String token : s.split(",")) {
            try {
                v.add(Float.parseFloat(token.trim()));
            } catch (NumberFormatException ignored) {
                // matches C++'s try/catch-and-skip on bad tokens
            }
        }
        return v;
    }

    // Extracts a JSON string field value, handling basic escape sequences.
    public static String extractString(String body, String key) {
        int p = body.indexOf('"' + key + '"');
        if (p == -1)
            return "";

        p = body.indexOf(':', p) + 1;
        while (p < body.length() && (body.charAt(p) == ' ' || body.charAt(p) == '\t'))
            p++;
        if (p >= body.length() || body.charAt(p) != '"')
            return "";
        p++;

        StringBuilder result = new StringBuilder();
        while (p < body.length()) {
            char c = body.charAt(p);
            if (c == '"')
                break;
            if (c == '\\' && p + 1 < body.length()) {
                p++;
                char esc = body.charAt(p);
                switch (esc) {
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    default -> result.append(esc);
                }
            } else {
                result.append(c);
            }
            p++;
        }
        return result.toString();
    }

    // Extracts a JSON integer field value.
    public static int extractInt(String body, String key, int defaultValue) {
        int p = body.indexOf('"' + key + '"');
        if (p == -1)
            return defaultValue;

        p = body.indexOf(':', p) + 1;
        while (p < body.length() && (body.charAt(p) == ' ' || body.charAt(p) == '\t'))
            p++;

        int end = p;
        while (end < body.length() && (Character.isDigit(body.charAt(end)) || body.charAt(end) == '-'))
            end++;

        try {
            return Integer.parseInt(body.substring(p, end));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Extracts a JSON float array field value, e.g. "embedding":[0.1,0.2,0.3]
    public static List<Float> extractFloatArray(String body, String key) {
        int p = body.indexOf('"' + key + '"');
        if (p == -1)
            return new ArrayList<>();

        p = body.indexOf('[', p);
        if (p == -1)
            return new ArrayList<>();

        int e = body.indexOf(']', p);
        if (e == -1)
            return new ArrayList<>();

        return parseVector(body.substring(p + 1, e));
    }

    // Extracts a JSON array of objects, e.g. "providers":[{"a":1},{"b":2}],
    // returning each object as its own raw JSON substring (so callers can
    // run extractString/extractInt on each one individually). Handles
    // nested braces and quoted strings (including escaped quotes) so a
    // brace inside a string value doesn't throw off the bracket counting.
    public static List<String> extractObjectArray(String body, String key) {
        List<String> result = new ArrayList<>();
        int p = body.indexOf('"' + key + '"');
        if (p == -1)
            return result;

        p = body.indexOf('[', p);
        if (p == -1)
            return result;

        int i = p + 1;
        while (i < body.length()) {
            while (i < body.length() && (body.charAt(i) == ',' || Character.isWhitespace(body.charAt(i))))
                i++;
            if (i >= body.length() || body.charAt(i) == ']')
                break;
            if (body.charAt(i) != '{')
                break;

            int start = i;
            int depth = 0;
            boolean inString = false;
            while (i < body.length()) {
                char c = body.charAt(i);
                if (inString) {
                    if (c == '\\') {
                        i++;
                    } else if (c == '"') {
                        inString = false;
                    }
                } else {
                    if (c == '"') {
                        inString = true;
                    } else if (c == '{') {
                        depth++;
                    } else if (c == '}') {
                        depth--;
                        if (depth == 0) {
                            i++;
                            break;
                        }
                    }
                }
                i++;
            }
            result.add(body.substring(start, i));
        }
        return result;
    }

    public record ParsedInsertBody(String label, List<Float> embedding) {
    }

    // Port of C++'s parseBody — adapted for our single-`label` VectorItem
    // instead of C++'s separate metadata/category fields.
    public static ParsedInsertBody parseInsertBody(String body) {
        String label = extractString(body, "label");
        List<Float> embedding = extractFloatArray(body, "embedding");
        return new ParsedInsertBody(label, embedding);
    }
}