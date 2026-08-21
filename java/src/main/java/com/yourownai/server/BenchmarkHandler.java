package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.BenchmarkOutcome;
import com.yourownai.db.VectorDB;
import com.yourownai.json.JsonParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BenchmarkHandler implements HttpHandler {

    private final VectorDB db;

    public BenchmarkHandler(VectorDB db) {
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
        if (!"GET".equals(method)) {
            HttpJson.send(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }

        Map<String, String> params = QueryParams.parse(exchange.getRequestURI().getRawQuery());
        List<Float> query = JsonParser.parseVector(params.getOrDefault("v", ""));

        if (query.size() != db.dims) {
            HttpJson.send(exchange, 400, "{\"error\":\"need " + db.dims + "D vector\"}");
            return;
        }

        int k = 5;
        try {
            k = Integer.parseInt(params.getOrDefault("k", "5"));
        } catch (NumberFormatException ignored) {
        }
        String metric = params.getOrDefault("metric", "cosine");

        BenchmarkOutcome b = db.benchmark(query, k, metric);

        String json = "{\"bruteforceUs\":" + b.bruteForceMicros()
                + ",\"kdtreeUs\":" + b.kdTreeMicros()
                + ",\"hnswUs\":" + b.hnswMicros()
                + ",\"itemCount\":" + b.itemCount()
                + "}";

        HttpJson.send(exchange, 200, json);
    }
}