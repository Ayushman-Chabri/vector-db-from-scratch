package com.yourownai.db;

import com.yourownai.model.VectorItem;
import com.yourownai.distance.DistFn;
import com.yourownai.distance.DistFnFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class VectorDBTest {

    private VectorDB db;
    private DistFn euclidean;

    @BeforeEach
    void setUp() {
        db = new VectorDB(2);
        euclidean = DistFnFactory.getDistFn("euclidean");

        db.insert("a", List.of(0.0f, 0.0f), euclidean);
        db.insert("b", List.of(1.0f, 0.0f), euclidean);
        db.insert("c", List.of(5.0f, 5.0f), euclidean);
        db.insert("d", List.of(0.0f, 1.0f), euclidean);
        db.insert("e", List.of(9.0f, 9.0f), euclidean);
    }

    @Test
    void allThreeAlgorithmsAgreeOnSameQuery() {
        List<Float> query = List.of(0.0f, 0.0f);

        SearchOutcome bfOut = db.search(query, 3, "euclidean", "bruteforce");
        SearchOutcome kdOut = db.search(query, 3, "euclidean", "kdtree");
        SearchOutcome hnswOut = db.search(query, 3, "euclidean", "hnsw");

        Set<Integer> bfIds = bfOut.hits().stream().map(SearchHit::id).collect(Collectors.toSet());
        Set<Integer> kdIds = kdOut.hits().stream().map(SearchHit::id).collect(Collectors.toSet());
        Set<Integer> hnswIds = hnswOut.hits().stream().map(SearchHit::id).collect(Collectors.toSet());

        assertEquals(bfIds, kdIds);
        assertEquals(bfIds, hnswIds);
    }

    @Test
    void insertAssignsSequentialIds() {
        int id = db.insert("f", List.of(2.0f, 2.0f), euclidean);
        assertEquals(6, id);
        assertEquals(6, db.size());
    }

    @Test
    void removeExcludesItemFromAllAlgorithms() {
        boolean removed = db.remove(1); // "a" at origin
        assertTrue(removed);
        assertEquals(4, db.size());

        SearchOutcome bfOut = db.search(List.of(0.0f, 0.0f), 5, "euclidean", "bruteforce");
        assertTrue(bfOut.hits().stream().noneMatch(h -> h.id() == 1));
    }

    @Test
    void removeReturnsFalseForUnknownId() {
        assertFalse(db.remove(999));
    }

    @Test
    void benchmarkReportsCorrectItemCount() {
        BenchmarkOutcome out = db.benchmark(List.of(0.0f, 0.0f), 3, "euclidean");

        assertEquals(5, out.itemCount());
        assertTrue(out.bruteForceMicros() >= 0);
        assertTrue(out.kdTreeMicros() >= 0);
        assertTrue(out.hnswMicros() >= 0);
    }

    @Test
    void allReturnsEveryStoredItem() {
        List<VectorItem> items = db.all();
        assertEquals(5, items.size());
    }
}