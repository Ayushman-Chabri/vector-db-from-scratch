package com.yourownai.index;

import java.util.ArrayList;
import java.util.List;

public class GraphInfo {
    public int topLayer;
    public int nodeCount;
    public List<Integer> nodesPerLayer = new ArrayList<>();
    public List<Integer> edgesPerLayer = new ArrayList<>();
    public List<NodeView> nodes = new ArrayList<>();
    public List<EdgeView> edges = new ArrayList<>();

    public record NodeView(int id, String label, int maxLyr) {
    }

    public record EdgeView(int src, int dst, int layer) {
    }
}