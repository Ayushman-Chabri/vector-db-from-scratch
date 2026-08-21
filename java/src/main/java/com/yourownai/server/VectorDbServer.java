package com.yourownai.server;

import com.sun.net.httpserver.HttpServer;
import com.yourownai.db.DocumentDB;
import com.yourownai.db.VectorDB;
import com.yourownai.ollama.OllamaClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class VectorDbServer {

    private final VectorDB db;
    private final DocumentDB docDB;
    private final OllamaClient ollama;
    private HttpServer server;

    public VectorDbServer(VectorDB db, DocumentDB docDB, OllamaClient ollama) {
        this.db = db;
        this.docDB = docDB;
        this.ollama = ollama;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/status", new StatusHandler());
        server.createContext("/items", new ItemsHandler(db));
        server.createContext("/stats", new StatsHandler(db, docDB, ollama));
        server.createContext("/search", new SearchHandler(db));
        server.createContext("/benchmark", new BenchmarkHandler(db));
        server.createContext("/hnsw-info", new HnswInfoHandler(db));
        server.createContext("/insert", new InsertHandler(db));
        server.createContext("/delete", new DeleteHandler(db));
        server.createContext("/doc/insert", new DocInsertHandler(docDB, ollama));
        server.createContext("/doc/list", new DocListHandler(docDB));
        server.createContext("/doc/delete", new DocDeleteHandler(docDB));
        server.createContext("/doc/ask", new DocAskHandler(docDB, ollama));
        server.createContext("/compare", new CompareHandler(docDB, ollama));
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}