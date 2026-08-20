package com.yourownai.index;

import com.yourownai.model.VectorItem;
import com.yourownai.distance.DistFn;
import com.yourownai.distance.DistFnFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BruteForceTest {

    private BruteForce bf;
    private DistFn euclidean;

    @BeforeEach
    void setUp() {
        bf = new BruteForce();
        euclidean = DistFnFactory.getDistFn("euclidean");

        bf.insert(new VectorItem(1, List.of(0.0f), "a"));
        bf.insert(new VectorItem(2, List.of(1.0f), "b"));
        bf.insert(new VectorItem(3, List.of(2.0f), "c"));
        bf.insert(new VectorItem(4, List.of(1.0f), "d")); // ties with id=2
    }

    @Test
    void knnReturnsResultsSortedByDistance() {
        List<SearchResult> results = bf.knn(List.of(0.0f), 4, euclidean);

        assertEquals(4, results.size());
        assertEquals(1, results.get(0).id());
        assertEquals(0.0, results.get(0).distance(), 1e-6);
        assertEquals(3, results.get(3).id());
        assertEquals(2.0, results.get(3).distance(), 1e-6);
    }

    @Test
    void knnBreaksTiesByAscendingId() {
        List<SearchResult> results = bf.knn(List.of(0.0f), 4, euclidean);

        // id=2 and id=4 are both distance 1.0 — id=2 must come first
        assertEquals(2, results.get(1).id());
        assertEquals(4, results.get(2).id());
    }

    @Test
    void knnRespectsKLimit() {
        List<SearchResult> results = bf.knn(List.of(0.0f), 2, euclidean);

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).id());
        assertEquals(2, results.get(1).id());
    }

    @Test
    void removeExcludesItemFromFutureSearches() {
        bf.remove(1); // remove the exact-match item

        List<SearchResult> results = bf.knn(List.of(0.0f), 4, euclidean);

        assertEquals(3, results.size());
        assertTrue(results.stream().noneMatch(r -> r.id() == 1));
        // new closest should now be id=2 (or id=4, both at distance 1.0 — id=2 wins the
        // tie)
        assertEquals(2, results.get(0).id());
    }
}