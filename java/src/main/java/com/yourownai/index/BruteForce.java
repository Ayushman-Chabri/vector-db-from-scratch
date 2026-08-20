package com.yourownai.index;

import com.yourownai.model.VectorItem;
import com.yourownai.distance.DistFn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BruteForce {

    private final List<VectorItem> items = new ArrayList<>();

    public void insert(VectorItem v) {
        items.add(v);
    }

    public List<SearchResult> knn(List<Float> query, int k, DistFn dist) {
        List<SearchResult> results = new ArrayList<>(items.size());

        for (VectorItem v : items) {
            double d = dist.distance(query, v.vec());
            results.add(new SearchResult(v.id(), d));
        }

        Collections.sort(results);

        if (results.size() > k) {
            results = results.subList(0, k);
        }

        return results;
    }

    public void remove(int id) {
        items.removeIf(v -> v.id() == id);
    }
}