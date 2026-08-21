package com.yourownai.ollama;

import com.yourownai.json.JsonParser;
import com.yourownai.json.JsonWriter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class OllamaClient {

    private final String host;
    private final int port;
    private final HttpClient client;

    public String embedModel = "nomic-embed-text";
    public String genModel = "llama3.2";

    public OllamaClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public OllamaClient() {
        this("127.0.0.1", 11434);
    }

    private String baseUrl() {
        return "http://" + host + ":" + port;
    }

    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/api/tags"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    // Returns an empty list if Ollama is unreachable or the model isn't found.
    public List<Float> embed(String text) {
        String body = "{\"model\":" + JsonWriter.jsonString(embedModel)
                + ",\"prompt\":" + JsonWriter.jsonString(text) + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/api/embeddings"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return List.of();
            }
            return JsonParser.extractFloatArray(response.body(), "embedding");
        } catch (IOException | InterruptedException e) {
            return List.of();
        }
    }

    // Returns an error string (not an exception) if Ollama is unavailable —
    // matches the C++ version's behavior of returning a user-facing message.
    public String generate(String prompt) {
        String body = "{\"model\":" + JsonWriter.jsonString(genModel)
                + ",\"prompt\":" + JsonWriter.jsonString(prompt)
                + ",\"stream\":false}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/api/generate"))
                    .timeout(Duration.ofSeconds(180)) // LLMs can be slow
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return "ERROR: Ollama unavailable. Run: ollama serve";
            }
            return JsonParser.extractString(response.body(), "response");
        } catch (IOException | InterruptedException e) {
            return "ERROR: Ollama unavailable. Run: ollama serve";
        }
    }
}