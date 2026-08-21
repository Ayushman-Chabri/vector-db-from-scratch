package com.yourownai;

import com.yourownai.db.Demo;
import com.yourownai.db.DocumentDB;
import com.yourownai.db.VectorDB;
import com.yourownai.ollama.OllamaClient;
import com.yourownai.server.VectorDbServer;

public class Main {
    public static void main(String[] args) throws Exception {
        VectorDB db = new VectorDB(Demo.DIMS);
        DocumentDB docDB = new DocumentDB();

        // Defaults to 127.0.0.1:11434 for local runs. Inside Docker,
        // set OLLAMA_HOST=host.docker.internal so the container can
        // reach Ollama running on the host machine (127.0.0.1 inside
        // a container refers to the container itself, not the host).
        String ollamaHost = System.getenv().getOrDefault("OLLAMA_HOST", "127.0.0.1");
        int ollamaPort = parsePort(System.getenv("OLLAMA_PORT"), 11434);
        OllamaClient ollama = new OllamaClient(ollamaHost, ollamaPort);

        Demo.load(db);

        boolean ollamaUp = ollama.isAvailable();

        System.out.println("=== VectorDB Engine (Java) ===");
        System.out.println("http://localhost:8080");
        System.out.println(db.size() + " demo vectors | " + Demo.DIMS + " dims | HNSW+KD-Tree+BruteForce");
        System.out.println("Ollama: " + (ollamaUp ? "ONLINE" : "OFFLINE") + " @ " + ollamaHost + ":" + ollamaPort);
        if (ollamaUp) {
            System.out.println("  embed model: " + ollama.embedModel + "  gen model: " + ollama.genModel);
        }

        VectorDbServer server = new VectorDbServer(db, docDB, ollama);
        server.start(8080);
    }

    private static int parsePort(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}