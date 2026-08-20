package com.yourownai.index;

import com.yourownai.model.VectorItem;
import com.yourownai.distance.DistFn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class KDTree {

    private KDNode root;
    private final int dims;

    public KDTree(int dims) {
        this.dims = dims;
    }

    public void insert(VectorItem v) {
        root = insert(root, v, 0);
    }

    private KDNode insert(KDNode n, VectorItem v, int depth) {
        if (n == null) {
            return new KDNode(v);
        }
        int axis = depth % dims;
        if (v.vec().get(axis) < n.item.vec().get(axis)) {
            n.left = insert(n.left, v, depth + 1);
        } else {
            n.right = insert(n.right, v, depth + 1);
        }
        return n;
    }

    public List<SearchResult> knn(List<Float> query, int k, DistFn dist) {
        PriorityQueue<SearchResult> heap = new PriorityQueue<>(Collections.reverseOrder());
        knn(root, query, k, 0, dist, heap);

        List<SearchResult> results = new ArrayList<>(heap);
        Collections.sort(results);
        return results;
    }

    private void knn(KDNode n, List<Float> query, int k, int depth, DistFn dist,
            PriorityQueue<SearchResult> heap) {
        if (n == null) {
            return;
        }

        double dn = dist.distance(query, n.item.vec());
        if (heap.size() < k || dn < heap.peek().distance()) {
            heap.offer(new SearchResult(n.item.id(), dn));
            if (heap.size() > k) {
                heap.poll();
            }
        }

        int axis = depth % dims;
        double diff = query.get(axis) - n.item.vec().get(axis);
        KDNode closer = diff < 0 ? n.left : n.right;
        KDNode farther = diff < 0 ? n.right : n.left;

        knn(closer, query, k, depth + 1, dist, heap);

        if (heap.size() < k || Math.abs(diff) < heap.peek().distance()) {
            knn(farther, query, k, depth + 1, dist, heap);
        }
    }

    public void rebuild(List<VectorItem> items) {
        root = null;
        for (VectorItem v : items) {
            insert(v);
        }
    }
}