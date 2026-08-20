package com.yourownai.index;

import com.yourownai.model.VectorItem;
import com.yourownai.distance.DistFn;
import com.yourownai.distance.DistFnFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KDTreeTest {

    private final DistFn euclidean = DistFnFactory.getDistFn("euclidean");

    @Test
    void knnFindsNearestNeighborsIn2D() {
        KDTree tree = new KDTree(2);
        tree.insert(new VectorItem(1, List.of(2.0f, 3.0f), "a"));
        tree.insert(new VectorItem(2, List.of(5.0f, 4.0f), "b"));
        tree.insert(new VectorItem(3, List.of(9.0f, 6.0f), "c"));
        tree.insert(new VectorItem(4, List.of(4.0f, 7.0f), "d"));
        tree.insert(new VectorItem(5, List.of(8.0f, 1.0f), "e"));
        tree.insert(new VectorItem(6, List.of(7.0f, 2.0f), "f"));

        List<SearchResult> results = tree.knn(List.of(9.0f, 2.0f), 3, euclidean);

        assertEquals(3, results.size());
        assertEquals(5, results.get(0).id());
        assertEquals(6, results.get(1).id());
        assertEquals(3, results.get(2).id());
    }

    @Test
    void knnBreaksTiesByAscendingId() {
        KDTree tree = new KDTree(2);
        tree.insert(new VectorItem(5, List.of(0.0f, 0.0f), "origin"));
        tree.insert(new VectorItem(10, List.of(1.0f, 0.0f), "x"));
        tree.insert(new VectorItem(20, List.of(0.0f, 1.0f), "y"));

        List<SearchResult> results = tree.knn(List.of(0.0f, 0.0f), 3, euclidean);

        assertEquals(5, results.get(0).id());
        assertEquals(10, results.get(1).id());
        assertEquals(20, results.get(2).id());
    }

    @Test
    void rebuildReplacesAllPreviousData() {
        KDTree tree = new KDTree(1);
        tree.insert(new VectorItem(1, List.of(0.0f), "old"));
        tree.insert(new VectorItem(2, List.of(1.0f), "old"));

        tree.rebuild(List.of(
                new VectorItem(3, List.of(0.0f), "new"),
                new VectorItem(4, List.of(1.0f), "new")));

        List<SearchResult> results = tree.knn(List.of(0.0f), 4, euclidean);

        assertEquals(2, results.size());
        assertEquals(3, results.get(0).id());
        assertEquals(4, results.get(1).id());
    }

    @Test
    void agreesWithBruteForceOnSameDataset() {
        List<VectorItem> items = List.of(
                new VectorItem(1, List.of(2.0f, 3.0f), "a"),
                new VectorItem(2, List.of(5.0f, 4.0f), "b"),
                new VectorItem(3, List.of(9.0f, 6.0f), "c"),
                new VectorItem(4, List.of(4.0f, 7.0f), "d"),
                new VectorItem(5, List.of(8.0f, 1.0f), "e"),
                new VectorItem(6, List.of(7.0f, 2.0f), "f"));

        KDTree tree = new KDTree(2);
        BruteForce bf = new BruteForce();
        for (VectorItem v : items) {
            tree.insert(v);
            bf.insert(v);
        }

        List<Float> query = List.of(6.0f, 3.0f);
        List<SearchResult> kdResults = tree.knn(query, 4, euclidean);
        List<SearchResult> bfResults = bf.knn(query, 4, euclidean);

        assertEquals(bfResults.size(), kdResults.size());
        for (int i = 0; i < bfResults.size(); i++) {
            assertEquals(bfResults.get(i).id(), kdResults.get(i).id());
            assertEquals(bfResults.get(i).distance(), kdResults.get(i).distance(), 1e-6);
        }
    }
}