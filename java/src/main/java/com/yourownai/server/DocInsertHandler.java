package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.DocumentDB;
import com.yourownai.db.TextChunker;
import com.yourownai.json.JsonParser;
import com.yourownai.ollama.OllamaClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocInsertHandler implements HttpHandler {

    private final DocumentDB docDB;
    private final OllamaClient ollama;

    public DocInsertHandler(DocumentDB docDB, OllamaClient ollama) {
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
        String title = JsonParser.extractString(body, "title");
        String text = JsonParser.extractString(body, "text");

        if (title.isEmpty() || text.isEmpty()) {
            HttpJson.send(exchange, 400, "{\"error\":\"need title and text\"}");
            return;
        }

        List<String> chunks = TextChunker.chunkText(text);
        List<Integer> ids = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            List<Float> emb = ollama.embed(chunks.get(i));
            if (emb.isEmpty()) {
                HttpJson.send(exchange, 502,
                        "{\"error\":\"Ollama unavailable. Install from https://ollama.com then run: "
                                + "ollama pull nomic-embed-text && ollama pull llama3.2\"}");
                return;
            }
            String chunkTitle = chunks.size() > 1
                    ? title + " [" + (i + 1) + "/" + chunks.size() + "]"
                    : title;
            ids.add(docDB.insert(chunkTitle, chunks.get(i), emb));
        }

        StringBuilder sb = new StringBuilder("{\"ids\":[");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0)
                sb.append(',');
            sb.append(ids.get(i));
        }
        sb.append("],\"chunks\":").append(chunks.size())
                .append(",\"dims\":").append(docDB.getDims())
                .append('}');

        HttpJson.send(exchange, 200, sb.toString());
    }
}