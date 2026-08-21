package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler implements HttpHandler {

    // Resolved relative to the working directory the server is started
    // from — "java/" for local `mvn exec:java` runs, "/app" inside the
    // Docker image (per WORKDIR + COPY frontend ./frontend), so the
    // same relative path works in both places without special-casing.
    private final Path indexFile = Path.of("frontend", "index.html");

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

        if (!Files.exists(indexFile)) {
            HttpJson.send(exchange, 404,
                    "{\"error\":\"frontend/index.html not found — check the server's working directory\"}");
            return;
        }

        byte[] bytes = Files.readAllBytes(indexFile);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}