package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;

public final class CorsSupport {

    private CorsSupport() {
    }

    // Port of the C++ cors() helper — same three headers, same values.
    public static void apply(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
}