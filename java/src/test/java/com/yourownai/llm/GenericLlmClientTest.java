package com.yourownai.llm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenericLlmClientTest {

    @Test
    void blankEndpointReturnsClearError() {
        String result = GenericLlmClient.ask("", "key123", "some-model", "hello");
        assertEquals("ERROR: no endpoint URL provided", result);
    }

    @Test
    void nullApiKeyReturnsClearError() {
        String result = GenericLlmClient.ask("https://example.com/v1/chat/completions", null, "some-model", "hello");
        assertEquals("ERROR: no API key provided", result);
    }

    @Test
    void blankModelReturnsClearError() {
        String result = GenericLlmClient.ask("https://example.com/v1/chat/completions", "key123", "  ", "hello");
        assertEquals("ERROR: no model name provided", result);
    }
}