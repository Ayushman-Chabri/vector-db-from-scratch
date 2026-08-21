package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.DocItem;
import com.yourownai.db.DocumentDB;
import com.yourownai.json.JsonWriter;

import java.io.IOException;
import java.util.List;

public class DocListHandler implements HttpHandler {

    private final DocumentDB docDB;

    public DocListHandler(DocumentDB docDB) {
        this.docDB = docDB;
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

        List<DocItem> docs = docDB.all();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < docs.size(); i++) {
            if (i > 0)
                sb.append(',');
            DocItem d = docs.get(i);
            String preview = d.text().length() > 120 ? d.text().substring(0, 120) + "…" : d.text();
            int words = d.text().isBlank() ? 0 : d.text().trim().split("\\s+").length;

            sb.append("{\"id\":").append(d.id())
                    .append(",\"title\":").append(JsonWriter.jsonString(d.title()))
                    .append(",\"preview\":").append(JsonWriter.jsonString(preview))
                    .append(",\"words\":").append(words)
                    .append('}');
        }
        sb.append(']');

        HttpJson.send(exchange, 200, sb.toString());
    }
}