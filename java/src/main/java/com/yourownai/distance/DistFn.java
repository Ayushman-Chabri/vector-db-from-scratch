package com.yourownai.distance;

import java.util.List;

@FunctionalInterface
public interface DistFn {
    double distance(List<Float> a, List<Float> b);
}