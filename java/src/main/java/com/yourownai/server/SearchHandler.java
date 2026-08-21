package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.SearchHit;
import com.yourownai.db.SearchOutcome;
import com.yourownai.db.VectorDB;
import com.yourownai.json.JsonParser;
import com.yourownai.json.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SearchHandler implements HttpHandler {

    private final VectorDB db;

    public SearchHandler(VectorDB db) {
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
        String algo = params.getOrDefault("algo", "hnsw");

        SearchOutcome out = db.search(query, k, metric, algo);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"results\":[");
        List<SearchHit> hits = out.hits();
        for (int i = 0; i < hits.size(); i++) {
            if (i > 0)
                sb.append(',');
            SearchHit h = hits.get(i);
            sb.append("{\"id\":").append(h.id())
                    .append(",\"label\":").append(JsonWriter.jsonString(h.label()))
                    .append(",\"distance\":").append(h.distance())
                    .append(",\"embedding\":").append(JsonWriter.jsonVector(h.vec()))
                    .append('}');
        }
        sb.append("],\"latencyUs\":").append(out.micros())
                .append(",\"algo\":").append(JsonWriter.jsonString(out.algo()))
                .append(",\"metric\":").append(JsonWriter.jsonString(out.metric()))
                .append('}');

        HttpJson.send(exchange, 200, sb.toString());
    }
}