package com.yourownai.distance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DistFnFactoryTest {

    @Test
    void euclideanMetricReturnsWorkingFunction() {
        DistFn fn = DistFnFactory.getDistFn("euclidean");
        assertEquals(3.0, fn.distance(List.of(0.0f), List.of(3.0f)), 1e-6);
    }

    @Test
    void manhattanMetricReturnsWorkingFunction() {
        DistFn fn = DistFnFactory.getDistFn("manhattan");
        assertEquals(3.0, fn.distance(List.of(0.0f), List.of(3.0f)), 1e-6);
    }

    @Test
    void cosineMetricReturnsWorkingFunction() {
        DistFn fn = DistFnFactory.getDistFn("cosine");
        assertEquals(0.0, fn.distance(List.of(1.0f, 0.0f), List.of(1.0f, 0.0f)), 1e-6);
    }

    @Test
    void unknownMetricThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> DistFnFactory.getDistFn("bogus"));
    }
}