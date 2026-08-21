package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class StatusHandler implements HttpHandler {

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

        HttpJson.send(exchange, 200, "{\"status\":\"ok\"}");
    }
}