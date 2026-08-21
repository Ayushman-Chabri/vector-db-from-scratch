package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yourownai.db.VectorDB;
import com.yourownai.index.GraphInfo;
import com.yourownai.json.JsonWriter;

import java.io.IOException;

public class HnswInfoHandler implements HttpHandler {

    private final VectorDB db;

    public HnswInfoHandler(VectorDB db) {
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

        GraphInfo gi = db.hnswInfo();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"topLayer\":").append(gi.topLayer)
                .append(",\"nodeCount\":").append(gi.nodeCount)
                .append(",\"nodesPerLayer\":[");
        for (int i = 0; i < gi.nodesPerLayer.size(); i++) {
            if (i > 0)
                sb.append(',');
            sb.append(gi.nodesPerLayer.get(i));
        }
        sb.append("],\"edgesPerLayer\":[");
        for (int i = 0; i < gi.edgesPerLayer.size(); i++) {
            if (i > 0)
                sb.append(',');
            sb.append(gi.edgesPerLayer.get(i));
        }
        sb.append("],\"nodes\":[");
        for (int i = 0; i < gi.nodes.size(); i++) {
            if (i > 0)
                sb.append(',');
            GraphInfo.NodeView n = gi.nodes.get(i);
            sb.append("{\"id\":").append(n.id())
                    .append(",\"label\":").append(JsonWriter.jsonString(n.label()))
                    .append(",\"maxLyr\":").append(n.maxLyr())
                    .append('}');
        }
        sb.append("],\"edges\":[");
        for (int i = 0; i < gi.edges.size(); i++) {
            if (i > 0)
                sb.append(',');
            GraphInfo.EdgeView e = gi.edges.get(i);
            sb.append("{\"src\":").append(e.src())
                    .append(",\"dst\":").append(e.dst())
                    .append(",\"lyr\":").append(e.layer())
                    .append('}');
        }
        sb.append("]}");

        HttpJson.send(exchange, 200, sb.toString());
    }
}