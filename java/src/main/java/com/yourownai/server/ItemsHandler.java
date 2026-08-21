package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.VectorDB;
import com.yourownai.json.JsonWriter;
import com.yourownai.model.VectorItem;

import java.io.IOException;
import java.util.List;

public class ItemsHandler implements HttpHandler {

    private final VectorDB db;

    public ItemsHandler(VectorDB db) {
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

        List<VectorItem> items = db.all();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0)
                sb.append(',');
            VectorItem v = items.get(i);
            sb.append("{\"id\":").append(v.id())
                    .append(",\"label\":").append(JsonWriter.jsonString(v.label()))
                    .append(",\"embedding\":").append(JsonWriter.jsonVector(v.vec()))
                    .append('}');
        }
        sb.append(']');

        HttpJson.send(exchange, 200, sb.toString());
    }
}