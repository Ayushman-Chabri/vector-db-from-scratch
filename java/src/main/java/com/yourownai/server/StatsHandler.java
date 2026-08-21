package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.DocumentDB;
import com.yourownai.db.VectorDB;
import com.yourownai.ollama.OllamaClient;

import java.io.IOException;

public class StatsHandler implements HttpHandler {

    private final VectorDB db;
    private final DocumentDB docDB;
    private final OllamaClient ollama;

    public StatsHandler(VectorDB db, DocumentDB docDB, OllamaClient ollama) {
        this.db = db;
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
        if (!"GET".equals(method)) {
            HttpJson.send(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }

        String json = "{\"vectorCount\":" + db.size()
                + ",\"docCount\":" + docDB.size()
                + ",\"dims\":" + db.dims
                + ",\"ollamaOnline\":" + ollama.isAvailable()
                + "}";

        HttpJson.send(exchange, 200, json);
    }
}