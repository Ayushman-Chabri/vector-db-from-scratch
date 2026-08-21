package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.DocSearchResult;
import com.yourownai.db.DocumentDB;
import com.yourownai.json.JsonParser;
import com.yourownai.json.JsonWriter;
import com.yourownai.ollama.OllamaClient;

import java.io.IOException;
import java.util.List;

public class DocAskHandler implements HttpHandler {

    private final DocumentDB docDB;
    private final OllamaClient ollama;

    public DocAskHandler(DocumentDB docDB, OllamaClient ollama) {
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

        List<Float> qEmb = ollama.embed(question);
        if (qEmb.isEmpty()) {
            HttpJson.send(exchange, 502, "{\"error\":\"Ollama unavailable\"}");
            return;
        }

        List<DocSearchResult> hits = docDB.search(qEmb, k);

        StringBuilder context = new StringBuilder();
        for (DocSearchResult hit : hits) {
            context.append(hit.doc().text()).append("\n\n");
        }

        String prompt = "Answer the question based only on the following context:\n\n"
                + context + "\nQuestion: " + question;

        String answer = ollama.generate(prompt);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"answer\":").append(JsonWriter.jsonString(answer))
                .append(",\"contexts\":[");
        for (int i = 0; i < hits.size(); i++) {
            if (i > 0)
                sb.append(',');
            DocSearchResult h = hits.get(i);
            sb.append("{\"id\":").append(h.doc().id())
                    .append(",\"title\":").append(JsonWriter.jsonString(h.doc().title()))
                    .append(",\"distance\":").append(h.distance())
                    .append('}');
        }
        sb.append("]}");

        HttpJson.send(exchange, 200, sb.toString());
    }
}