package com.yourownai.index;

import com.yourownai.model.VectorItem;
import com.yourownai.distance.DistFn;
import com.yourownai.distance.DistFnFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HNSWTest {

    private final DistFn euclidean = DistFnFactory.getDistFn("euclidean");

    @Test
    void knnFindsExactNearestOnSmallLinearDataset() {
        HNSW hnsw = new HNSW(16, 200);
        for (int i = 0; i < 20; i++) {
            hnsw.insert(new VectorItem(i, List.of((float) i), "p" + i), euclidean);
        }

        List<SearchResult> results = hnsw.knn(List.of(10.0f), 3, 50, euclidean);

        assertEquals(3, results.size());
        assertEquals(10, results.get(0).id());
        assertEquals(0.0, results.get(0).distance(), 1e-6);
        assertEquals(9, results.get(1).id());
        assertEquals(11, results.get(2).id());
    }

    @Test
    void agreesWithBruteForceOnSmallGrid() {
        List<VectorItem> items = new ArrayList<>();
        int id = 1;
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                items.add(new VectorItem(id++, List.of((float) x, (float) y), "grid"));
            }
        }

        HNSW hnsw = new HNSW(16, 200);
        BruteForce bf = new BruteForce();
        for (VectorItem v : items) {
            hnsw.insert(v, euclidean);
            bf.insert(v);
        }

        List<Float> query = List.of(2.0f, 2.0f);
        List<SearchResult> hnswResults = hnsw.knn(query, 5, 50, euclidean);
        List<SearchResult> bfResults = bf.knn(query, 5, euclidean);

        assertEquals(bfResults.size(), hnswResults.size());
        for (int i = 0; i < bfResults.size(); i++) {
            assertEquals(bfResults.get(i).id(), hnswResults.get(i).id());
        }
    }

    @Test
    void removeExcludesItemFromFutureSearches() {
        HNSW hnsw = new HNSW(16, 200);
        for (int i = 0; i < 10; i++) {
            hnsw.insert(new VectorItem(i, List.of((float) i), "p" + i), euclidean);
        }

        hnsw.remove(5);

        assertEquals(9, hnsw.size());
        List<SearchResult> results = hnsw.knn(List.of(5.0f), 10, 50, euclidean);
        assertTrue(results.stream().noneMatch(r -> r.id() == 5));
    }

    @Test
    void getInfoReportsConsistentNodeCounts() {
        HNSW hnsw = new HNSW(16, 200);
        for (int i = 0; i < 15; i++) {
            hnsw.insert(new VectorItem(i, List.of((float) i), "p" + i), euclidean);
        }

        GraphInfo info = hnsw.getInfo();

        assertEquals(15, info.nodeCount);
        assertEquals(15, info.nodesPerLayer.get(0));
        assertEquals(info.nodeCount, info.nodes.size());
    }
}