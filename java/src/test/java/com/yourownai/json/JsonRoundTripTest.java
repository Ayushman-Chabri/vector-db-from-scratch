package com.yourownai.json;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonRoundTripTest {

    @Test
    void jsonStringEscapesSpecialCharacters() {
        String result = JsonWriter.jsonString("hello \"world\"\nnew line");
        assertEquals("\"hello \\\"world\\\"\\nnew line\"", result);
    }

    @Test
    void jsonVectorFormatsWithFourDecimals() {
        String result = JsonWriter.jsonVector(List.of(1.0f, 0.5f, -2.25f));
        assertEquals("[1.0000,0.5000,-2.2500]", result);
    }

    @Test
    void parseVectorHandlesCommaSeparatedFloats() {
        List<Float> result = JsonParser.parseVector("0.1,0.2,0.3");
        assertEquals(List.of(0.1f, 0.2f, 0.3f), result);
    }

    @Test
    void parseVectorSkipsMalformedTokens() {
        List<Float> result = JsonParser.parseVector("0.1,garbage,0.3");
        assertEquals(List.of(0.1f, 0.3f), result);
    }

    @Test
    void extractStringFindsFieldValue() {
        String body = "{\"label\":\"hello world\",\"other\":1}";
        assertEquals("hello world", JsonParser.extractString(body, "label"));
    }

    @Test
    void extractStringReturnsEmptyForMissingKey() {
        String body = "{\"other\":1}";
        assertEquals("", JsonParser.extractString(body, "label"));
    }

    @Test
    void extractIntFindsFieldValue() {
        String body = "{\"k\":5,\"other\":\"x\"}";
        assertEquals(5, JsonParser.extractInt(body, "k", 0));
    }

    @Test
    void extractIntReturnsDefaultForMissingKey() {
        String body = "{\"other\":1}";
        assertEquals(3, JsonParser.extractInt(body, "k", 3));
    }

    @Test
    void extractFloatArrayFindsEmbedding() {
        String body = "{\"embedding\":[0.1,0.2,0.3],\"label\":\"x\"}";
        List<Float> result = JsonParser.extractFloatArray(body, "embedding");
        assertEquals(List.of(0.1f, 0.2f, 0.3f), result);
    }

    @Test
    void parseInsertBodyExtractsBothFields() {
        String body = "{\"label\":\"cat photo\",\"embedding\":[1.0,2.0]}";
        JsonParser.ParsedInsertBody parsed = JsonParser.parseInsertBody(body);
        assertEquals("cat photo", parsed.label());
        assertEquals(List.of(1.0f, 2.0f), parsed.embedding());
    }

    @Test
    void writeThenParseRoundTripsAVector() {
        List<Float> original = List.of(1.5f, 2.25f, -3.75f);
        String json = JsonWriter.jsonVector(original);
        // strip the [ and ] to reuse parseVector on the inner content
        String inner = json.substring(1, json.length() - 1);
        List<Float> parsed = JsonParser.parseVector(inner);
        assertEquals(original, parsed);
    }
}