package com.yourownai.server;

import com.yourownai.db.Demo;
import com.yourownai.db.DocumentDB;
import com.yourownai.db.VectorDB;
import com.yourownai.ollama.OllamaClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class VectorDbServerTest {

    private VectorDbServer server;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        VectorDB db = new VectorDB(Demo.DIMS);
        Demo.load(db);
        DocumentDB docDB = new DocumentDB();
        OllamaClient ollama = new OllamaClient();

        server = new VectorDbServer(db, docDB, ollama);
        server.start(0);
        baseUrl = "http://localhost:" + server.getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void statusEndpointReturnsOk() throws Exception {
        HttpResponse<String> response = get("/status");
        assertEquals(200, response.statusCode());
        assertEquals("{\"status\":\"ok\"}", response.body());
    }

    @Test
    void itemsEndpointReturnsAllTwentyDemoVectors() throws Exception {
        HttpResponse<String> response = get("/items");
        assertEquals(200, response.statusCode());
        long count = response.body().split("\"id\":").length - 1;
        assertEquals(20, count);
    }

    @Test
    void statsEndpointReportsVectorCount() throws Exception {
        HttpResponse<String> response = get("/stats");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"vectorCount\":20"));
    }

    @Test
    void searchEndpointReturnsResultsForValidVector() throws Exception {
        String query = "0.9,0.85,0.72,0.68,0.12,0.08,0.15,0.10,0.05,0.08,0.06,0.09,0.07,0.11,0.08,0.06";
        HttpResponse<String> response = get("/search?v=" + query + "&k=3");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"results\""));
        assertTrue(response.body().contains("\"algo\":\"hnsw\""));
    }

    @Test
    void searchEndpointRejectsWrongDimensionVector() throws Exception {
        HttpResponse<String> response = get("/search?v=1.0,2.0&k=3");
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("error"));
    }

    @Test
    void benchmarkEndpointReturnsAllThreeTimings() throws Exception {
        String query = "0.9,0.85,0.72,0.68,0.12,0.08,0.15,0.10,0.05,0.08,0.06,0.09,0.07,0.11,0.08,0.06";
        HttpResponse<String> response = get("/benchmark?v=" + query + "&k=3");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("bruteforceUs"));
        assertTrue(response.body().contains("kdtreeUs"));
        assertTrue(response.body().contains("hnswUs"));
    }

    @Test
    void hnswInfoEndpointReportsGraphStructure() throws Exception {
        HttpResponse<String> response = get("/hnsw-info");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"nodeCount\":20"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void insertAddsNewVectorAndItAppearsInItems() throws Exception {
        String body = "{\"label\":\"test item\",\"embedding\":"
                + "[0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1]}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/insert"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"id\""));

        HttpResponse<String> items = get("/items");
        assertTrue(items.body().contains("test item"));
    }

    @Test
    void insertRejectsWrongDimensionEmbedding() throws Exception {
        String body = "{\"label\":\"bad\",\"embedding\":[1.0,2.0]}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/insert"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void deleteRemovesExistingItem() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/delete?id=1"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"ok\":true"));
    }

    @Test
    void deleteAcceptsPathStyleIdForFrontendCompatibility() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/delete/2"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"ok\":true"));
    }

    @Test
    void docListStartsEmpty() throws Exception {
        HttpResponse<String> response = get("/doc/list");
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void deleteRejectsNonNumericId() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/delete/abc"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("error"));
    }

    @Test
    void docDeleteReturnsFalseForUnknownId() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/doc/delete/999"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"ok\":false"));
    }

    @Test
    void compareRejectsEmptyQuestion() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/compare"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"providers\":[]}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("error"));
    }

    @Test
    void rootServesFrontendHtml() throws Exception {
        HttpResponse<String> response = get("/");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("<!DOCTYPE html>"));
    }
}