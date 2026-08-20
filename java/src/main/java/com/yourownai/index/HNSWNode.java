package com.yourownai.index;

import com.yourownai.model.VectorItem;

import java.util.List;

class HNSWNode {
    VectorItem item;
    int maxLyr;
    List<List<Integer>> nbrs;

    HNSWNode(VectorItem item, int maxLyr, List<List<Integer>> nbrs) {
        this.item = item;
        this.maxLyr = maxLyr;
        this.nbrs = nbrs;
    }
}