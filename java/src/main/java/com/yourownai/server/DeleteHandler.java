package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.VectorDB;

import java.io.IOException;
import java.util.Map;

public class DeleteHandler implements HttpHandler {

    private final VectorDB db;

    public DeleteHandler(VectorDB db) {
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
        if (!"DELETE".equals(method)) {
            HttpJson.send(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }

        Integer id = PathId.extract(exchange, "/delete");
        if (id == null) {
            HttpJson.send(exchange, 400, "{\"error\":\"invalid id\"}");
            return;
        }

        boolean ok = db.remove(id);
        HttpJson.send(exchange, 200, "{\"ok\":" + ok + "}");
    }
}