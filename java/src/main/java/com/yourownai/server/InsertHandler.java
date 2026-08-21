package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.VectorDB;
import com.yourownai.distance.DistFnFactory;
import com.yourownai.json.JsonParser;

import java.io.IOException;
import java.util.List;

public class InsertHandler implements HttpHandler {

    private final VectorDB db;

    public InsertHandler(VectorDB db) {
        this.db = db;
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
        JsonParser.ParsedInsertBody parsed = JsonParser.parseInsertBody(body);
        List<Float> emb = parsed.embedding();

        if (parsed.label().isEmpty() || emb.size() != db.dims) {
            HttpJson.send(exchange, 400, "{\"error\":\"invalid body\"}");
            return;
        }

        int id = db.insert(parsed.label(), emb, DistFnFactory.getDistFn("cosine"));
        HttpJson.send(exchange, 200, "{\"id\":" + id + "}");
    }
}