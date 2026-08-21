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
        OllamaClient ollama = new OllamaClient();

        Demo.load(db);

        boolean ollamaUp = ollama.isAvailable();

        System.out.println("=== VectorDB Engine (Java) ===");
        System.out.println("http://localhost:8080");
        System.out.println(db.size() + " demo vectors | " + Demo.DIMS + " dims | HNSW+KD-Tree+BruteForce");
        System.out.println("Ollama: " + (ollamaUp ? "ONLINE" : "OFFLINE (install from ollama.com)"));
        if (ollamaUp) {
            System.out.println("  embed model: " + ollama.embedModel + "  gen model: " + ollama.genModel);
        }

        VectorDbServer server = new VectorDbServer(db, docDB, ollama);
        server.start(8080);
    }
}