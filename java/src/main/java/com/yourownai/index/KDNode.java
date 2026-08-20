package com.yourownai.index;

import com.yourownai.model.VectorItem;

class KDNode {
    VectorItem item;
    KDNode left;
    KDNode right;

    KDNode(VectorItem item) {
        this.item = item;
    }
}