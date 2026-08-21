package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class RequestBody {

    private RequestBody() {
    }

    public static String readAsString(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            is.transferTo(buffer);
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}