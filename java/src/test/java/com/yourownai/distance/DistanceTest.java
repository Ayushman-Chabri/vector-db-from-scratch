package com.yourownai.distance;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DistanceTest {

    @Test
    void euclideanKnownTriangle() {
        // classic 3-4-5 right triangle
        List<Float> a = List.of(0f, 0f);
        List<Float> b = List.of(3f, 4f);
        assertEquals(5.0, Distance.euclidean(a, b), 1e-9);
    }

    @Test
    void manhattanSimpleCase() {
        List<Float> a = List.of(0f, 0f);
        List<Float> b = List.of(3f, 4f);
        assertEquals(7.0, Distance.manhattan(a, b), 1e-9);
    }

    @Test
    void cosineIdenticalVectorsIsZeroDistance() {
        List<Float> a = List.of(1f, 2f, 3f);
        List<Float> b = List.of(1f, 2f, 3f);
        assertEquals(0.0, Distance.cosine(a, b), 1e-9);
    }

    @Test
    void cosineOrthogonalVectorsIsOne() {
        List<Float> a = List.of(1f, 0f);
        List<Float> b = List.of(0f, 1f);
        assertEquals(1.0, Distance.cosine(a, b), 1e-9);
    }
}