package com.yourownai.llm;

import com.yourownai.json.JsonParser;
import com.yourownai.json.JsonWriter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class GenericLlmClient {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private GenericLlmClient() {
    }

    // Works with any provider that speaks the OpenAI-compatible chat
    // completions schema — covers OpenAI itself, Groq, Together, Mistral,
    // DeepSeek, Fireworks, OpenRouter (which can route to Anthropic/Google
    // models too), and most others. Never throws — errors come back as a
    // "ERROR: ..." string, same convention as OllamaClient.
    public static String ask(String endpoint, String apiKey, String model, String prompt) {
        if (endpoint == null || endpoint.isBlank())
            return "ERROR: no endpoint URL provided";
        if (apiKey == null || apiKey.isBlank())
            return "ERROR: no API key provided";
        if (model == null || model.isBlank())
            return "ERROR: no model name provided";

        String body = "{\"model\":" + JsonWriter.jsonString(model)
                + ",\"messages\":[{\"role\":\"user\",\"content\":"
                + JsonWriter.jsonString(prompt) + "}]}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "ERROR: " + endpoint + " returned HTTP " + response.statusCode();
            }
            String content = JsonParser.extractString(response.body(), "content");
            return content.isEmpty() ? "ERROR: could not parse response from " + endpoint : content;
        } catch (IOException | InterruptedException e) {
            return "ERROR: could not reach " + endpoint + " (" + e.getMessage() + ")";
        } catch (IllegalArgumentException e) {
            return "ERROR: invalid endpoint URL";
        }
    }
}