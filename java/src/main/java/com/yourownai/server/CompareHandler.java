package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.DocSearchResult;
import com.yourownai.db.DocumentDB;
import com.yourownai.json.JsonParser;
import com.yourownai.json.JsonWriter;
import com.yourownai.llm.GenericLlmClient;
import com.yourownai.ollama.OllamaClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompareHandler implements HttpHandler {

    private final DocumentDB docDB;
    private final OllamaClient ollama;

    public CompareHandler(DocumentDB docDB, OllamaClient ollama) {
        this.docDB = docDB;
        this.ollama = ollama;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CorsSupport.apply(exchange);
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if (!"POST".equals(method)) {
            HttpJson.send(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }

        String body = RequestBody.readAsString(exchange);
        String question = JsonParser.extractString(body, "question");
        int k = JsonParser.extractInt(body, "k", 3);

        if (question.isEmpty()) {
            HttpJson.send(exchange, 400, "{\"error\":\"need question\"}");
            return;
        }

        List<String> providerJsons = JsonParser.extractObjectArray(body, "providers");

        List<CompareResult> results = new ArrayList<>();
        results.add(runLocalRag(question, k));

        for (String pJson : providerJsons) {
            String name = JsonParser.extractString(pJson, "name");
            String endpoint = JsonParser.extractString(pJson, "endpoint");
            String apiKey = JsonParser.extractString(pJson, "apiKey");
            String model = JsonParser.extractString(pJson, "model");
            if (name.isEmpty())
                name = endpoint;

            long start = System.currentTimeMillis();
            String answer = GenericLlmClient.ask(endpoint, apiKey, model, question);
            long latency = System.currentTimeMillis() - start;
            results.add(new CompareResult(name, answer, latency, false));
        }

        StringBuilder sb = new StringBuilder("{\"results\":[");
        for (int i = 0; i < results.size(); i++) {
            if (i > 0)
                sb.append(',');
            CompareResult r = results.get(i);
            sb.append("{\"model\":").append(JsonWriter.jsonString(r.model()))
                    .append(",\"answer\":").append(JsonWriter.jsonString(r.answer()))
                    .append(",\"latencyMs\":").append(r.latencyMs())
                    .append(",\"skipped\":").append(r.skipped())
                    .append('}');
        }
        sb.append("]}");

        HttpJson.send(exchange, 200, sb.toString());
    }

    private CompareResult runLocalRag(String question, int k) {
        long start = System.currentTimeMillis();

        List<Float> qEmb = ollama.embed(question);
        if (qEmb.isEmpty()) {
            return new CompareResult("Local RAG (HNSW + llama3.2)",
                    "ERROR: Ollama unavailable or no embedding returned",
                    System.currentTimeMillis() - start, false);
        }

        List<DocSearchResult> hits = docDB.search(qEmb, k);
        StringBuilder context = new StringBuilder();
        for (DocSearchResult hit : hits) {
            context.append(hit.doc().text()).append("\n\n");
        }

        String prompt = "Answer the question based only on the following context:\n\n"
                + context + "\nQuestion: " + question;
        String answer = ollama.generate(prompt);

        long latency = System.currentTimeMillis() - start;
        return new CompareResult("Local RAG (HNSW + llama3.2)", answer, latency, false);
    }
}