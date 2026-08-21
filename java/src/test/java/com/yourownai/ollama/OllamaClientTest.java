package com.yourownai.ollama;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class OllamaClientTest {

    private OllamaClient ollama;

    @BeforeEach
    void setUp() {
        ollama = new OllamaClient();
    }

    @Test
    void isAvailableReturnsFalseForUnreachableHost() {
        OllamaClient unreachable = new OllamaClient("127.0.0.1", 59999);
        assertFalse(unreachable.isAvailable());
    }

    @Test
    void embedReturnsNonEmptyVector() {
        assumeTrue(ollama.isAvailable(), "Ollama not running locally — skipping");
        List<Float> emb = ollama.embed("hello world");
        assertFalse(emb.isEmpty());
    }

    @Test
    void embedIsConsistentDimensionAcrossCalls() {
        assumeTrue(ollama.isAvailable(), "Ollama not running locally — skipping");
        List<Float> emb1 = ollama.embed("the quick brown fox");
        List<Float> emb2 = ollama.embed("a completely different sentence");
        assertEquals(emb1.size(), emb2.size());
    }

    @Test
    void generateReturnsNonErrorResponse() {
        assumeTrue(ollama.isAvailable(), "Ollama not running locally — skipping");
        String response = ollama.generate("Say the word 'test' and nothing else.");
        assertFalse(response.startsWith("ERROR"));
        assertFalse(response.isBlank());
    }
}