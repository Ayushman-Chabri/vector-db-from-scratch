package com.yourownai.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentDBTest {

    private DocumentDB docDB;

    @BeforeEach
    void setUp() {
        docDB = new DocumentDB();
    }

    @Test
    void insertTracksDimsFromFirstEmbedding() {
        docDB.insert("doc1", "some text", List.of(1.0f, 0.0f, 0.0f));
        assertEquals(3, docDB.getDims());
    }

    @Test
    void searchFindsMostSimilarDocumentByCosine() {
        docDB.insert("about cats", "cats are great pets", List.of(1.0f, 0.0f, 0.0f));
        docDB.insert("about dogs", "dogs are loyal companions", List.of(0.9f, 0.1f, 0.0f));
        docDB.insert("about cars", "cars need regular maintenance", List.of(0.0f, 0.0f, 1.0f));

        List<DocSearchResult> results = docDB.search(List.of(1.0f, 0.0f, 0.0f), 2, 1.0);

        assertFalse(results.isEmpty());
        assertEquals("about cats", results.get(0).doc().title());
    }

    @Test
    void searchRespectsMaxDistanceThreshold() {
        docDB.insert("close", "similar text", List.of(1.0f, 0.0f, 0.0f));
        docDB.insert("far", "unrelated text", List.of(0.0f, 1.0f, 0.0f));

        List<DocSearchResult> results = docDB.search(List.of(1.0f, 0.0f, 0.0f), 5, 0.01);

        assertEquals(1, results.size());
        assertEquals("close", results.get(0).doc().title());
    }

    @Test
    void removeExcludesDocumentFromFutureSearches() {
        int id = docDB.insert("doc1", "text one", List.of(1.0f, 0.0f, 0.0f));
        docDB.insert("doc2", "text two", List.of(0.9f, 0.1f, 0.0f));

        boolean removed = docDB.remove(id);
        assertTrue(removed);

        List<DocSearchResult> results = docDB.search(List.of(1.0f, 0.0f, 0.0f), 5, 1.0);
        assertTrue(results.stream().noneMatch(r -> r.doc().id() == id));
    }

    @Test
    void sizeReflectsInsertedAndRemovedDocuments() {
        docDB.insert("a", "text a", List.of(1.0f, 0.0f, 0.0f));
        docDB.insert("b", "text b", List.of(0.0f, 1.0f, 0.0f));
        assertEquals(2, docDB.size());

        docDB.remove(1);
        assertEquals(1, docDB.size());
    }
}