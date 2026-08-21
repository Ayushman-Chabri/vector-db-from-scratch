package com.yourownai.server;

import com.sun.net.httpserver.HttpExchange;

public final class PathId {

    private PathId() {
    }

    // Extracts an integer id from either a trailing path segment
    // (e.g. "/delete/5" with contextPath "/delete") or a query param
    // (e.g. "/delete?id=5") — supports both the original C++/index.html
    // frontend's path style and our own query-param style from Phase 10.
    public static Integer extract(HttpExchange exchange, String contextPath) {
        String path = exchange.getRequestURI().getPath();

        if (path.length() > contextPath.length() + 1
                && path.startsWith(contextPath + "/")) {
            String suffix = path.substring(contextPath.length() + 1);
            try {
                return Integer.parseInt(suffix);
            } catch (NumberFormatException ignored) {
                // fall through to query param
            }
        }

        String rawQuery = exchange.getRequestURI().getRawQuery();
        String idParam = QueryParams.parse(rawQuery).get("id");
        if (idParam != null) {
            try {
                return Integer.parseInt(idParam);
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }
}