package com.yourownai.db;

import com.yourownai.model.VectorItem;
import com.yourownai.distance.DistFn;
import com.yourownai.distance.DistFnFactory;
import com.yourownai.index.BruteForce;
import com.yourownai.index.KDTree;
import com.yourownai.index.HNSW;
import com.yourownai.index.GraphInfo;
import com.yourownai.index.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VectorDB {

    private final Map<Integer, VectorItem> store = new HashMap<>();
    private final BruteForce bf = new BruteForce();
    private final KDTree kdt;
    private final HNSW hnsw = new HNSW(16, 200);
    private int nextId = 1;

    public final int dims;

    public VectorDB(int dims) {
        this.dims = dims;
        this.kdt = new KDTree(dims);
    }

    public synchronized int insert(String label, List<Float> emb, DistFn dist) {
        VectorItem v = new VectorItem(nextId++, emb, label);
        store.put(v.id(), v);
        bf.insert(v);
        kdt.insert(v);
        hnsw.insert(v, dist);
        return v.id();
    }

    public synchronized boolean remove(int id) {
        if (!store.containsKey(id)) {
            return false;
        }
        store.remove(id);
        bf.remove(id);
        hnsw.remove(id);
        kdt.rebuild(new ArrayList<>(store.values()));
        return true;
    }

    public synchronized SearchOutcome search(List<Float> query, int k, String metric, String algo) {
        DistFn dfn = DistFnFactory.getDistFn(metric);

        long start = System.nanoTime();
        List<SearchResult> raw;
        if ("bruteforce".equals(algo)) {
            raw = bf.knn(query, k, dfn);
        } else if ("kdtree".equals(algo)) {
            raw = kdt.knn(query, k, dfn);
        } else {
            raw = hnsw.knn(query, k, 50, dfn);
        }
        long micros = (System.nanoTime() - start) / 1000;

        List<SearchHit> hits = new ArrayList<>();
        for (SearchResult r : raw) {
            VectorItem v = store.get(r.id());
            if (v != null) {
                hits.add(new SearchHit(v.id(), v.label(), v.vec(), r.distance()));
            }
        }

        return new SearchOutcome(hits, micros, algo, metric);
    }

    public synchronized BenchmarkOutcome benchmark(List<Float> query, int k, String metric) {
        DistFn dfn = DistFnFactory.getDistFn(metric);

        long bfStart = System.nanoTime();
        bf.knn(query, k, dfn);
        long bfMicros = (System.nanoTime() - bfStart) / 1000;

        long kdStart = System.nanoTime();
        kdt.knn(query, k, dfn);
        long kdMicros = (System.nanoTime() - kdStart) / 1000;

        long hnswStart = System.nanoTime();
        hnsw.knn(query, k, 50, dfn);
        long hnswMicros = (System.nanoTime() - hnswStart) / 1000;

        return new BenchmarkOutcome(bfMicros, kdMicros, hnswMicros, store.size());
    }

    public synchronized List<VectorItem> all() {
        return new ArrayList<>(store.values());
    }

    public synchronized GraphInfo hnswInfo() {
        return hnsw.getInfo();
    }

    public synchronized int size() {
        return store.size();
    }
}