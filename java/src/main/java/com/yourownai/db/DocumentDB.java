package com.yourownai.db;

import com.yourownai.model.VectorItem;
import com.yourownai.distance.DistFn;
import com.yourownai.distance.DistFnFactory;
import com.yourownai.index.HNSW;
import com.yourownai.index.BruteForce;
import com.yourownai.index.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentDB {

    private final Map<Integer, DocItem> store = new HashMap<>();
    private final HNSW hnsw = new HNSW(16, 200);
    private final BruteForce bf = new BruteForce();
    private final DistFn cosine = DistFnFactory.getDistFn("cosine");
    private int nextId = 1;
    private int dims = 0;

    public synchronized int insert(String title, String text, List<Float> embedding) {
        if (dims == 0) {
            dims = embedding.size();
        }

        DocItem item = new DocItem(nextId++, title, text, embedding);
        store.put(item.id(), item);

        VectorItem vi = new VectorItem(item.id(), embedding, title);
        hnsw.insert(vi, cosine);
        bf.insert(vi);

        return item.id();
    }

    public synchronized List<DocSearchResult> search(List<Float> query, int k, double maxDistance) {
        if (store.isEmpty()) {
            return new ArrayList<>();
        }

        List<SearchResult> raw = (store.size() < 10)
                ? bf.knn(query, k, cosine)
                : hnsw.knn(query, k, 50, cosine);

        List<DocSearchResult> out = new ArrayList<>();
        for (SearchResult r : raw) {
            DocItem doc = store.get(r.id());
            if (doc != null && r.distance() <= maxDistance) {
                out.add(new DocSearchResult(r.distance(), doc));
            }
        }
        return out;
    }

    public synchronized List<DocSearchResult> search(List<Float> query, int k) {
        return search(query, k, 0.7);
    }

    public synchronized boolean remove(int id) {
        if (!store.containsKey(id)) {
            return false;
        }
        store.remove(id);
        hnsw.remove(id);
        bf.remove(id);
        return true;
    }

    public synchronized List<DocItem> all() {
        return new ArrayList<>(store.values());
    }

    public synchronized int size() {
        return store.size();
    }

    public int getDims() {
        return dims;
    }
}