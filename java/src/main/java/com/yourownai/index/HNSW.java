package com.yourownai.index;

import com.yourownai.model.VectorItem;
import com.yourownai.distance.DistFn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class HNSW {

    private final Map<Integer, HNSWNode> G = new HashMap<>();
    private final int M;
    private final int M0;
    private final int efBuild;
    private final double mL;
    private int topLayer = -1;
    private int entryPt = -1;
    private final Random rng;

    public HNSW(int m, int efBuild) {
        this.M = m;
        this.M0 = 2 * m;
        this.efBuild = efBuild;
        this.mL = 1.0 / Math.log(m);
        this.rng = new Random(42);
    }

    public HNSW() {
        this(16, 200);
    }

    private int randLevel() {
        double u = 1.0 - rng.nextDouble(); // (0,1], avoids log(0)
        return (int) Math.floor(-Math.log(u) * mL);
    }

    private List<SearchResult> searchLayer(List<Float> q, int ep, int ef, int lyr, DistFn dist) {
        Set<Integer> visited = new HashSet<>();
        PriorityQueue<SearchResult> candidates = new PriorityQueue<>();
        PriorityQueue<SearchResult> found = new PriorityQueue<>(Collections.reverseOrder());

        double d0 = dist.distance(q, G.get(ep).item.vec());
        visited.add(ep);
        candidates.offer(new SearchResult(ep, d0));
        found.offer(new SearchResult(ep, d0));

        while (!candidates.isEmpty()) {
            SearchResult c = candidates.poll();
            if (found.size() >= ef && c.distance() > found.peek().distance()) {
                break;
            }

            HNSWNode cNode = G.get(c.id());
            if (lyr >= cNode.nbrs.size())
                continue;

            for (int nid : cNode.nbrs.get(lyr)) {
                if (visited.contains(nid) || !G.containsKey(nid))
                    continue;
                visited.add(nid);
                double nd = dist.distance(q, G.get(nid).item.vec());
                if (found.size() < ef || nd < found.peek().distance()) {
                    candidates.offer(new SearchResult(nid, nd));
                    found.offer(new SearchResult(nid, nd));
                    if (found.size() > ef) {
                        found.poll();
                    }
                }
            }
        }

        List<SearchResult> results = new ArrayList<>(found);
        Collections.sort(results);
        return results;
    }

    private List<Integer> selectNbrs(List<SearchResult> candidates, int maxM) {
        List<Integer> result = new ArrayList<>();
        int limit = Math.min(candidates.size(), maxM);
        for (int i = 0; i < limit; i++) {
            result.add(candidates.get(i).id());
        }
        return result;
    }

    public void insert(VectorItem item, DistFn dist) {
        int id = item.id();
        int lvl = randLevel();

        List<List<Integer>> nbrs = new ArrayList<>();
        for (int i = 0; i <= lvl; i++) {
            nbrs.add(new ArrayList<>());
        }
        G.put(id, new HNSWNode(item, lvl, nbrs));

        if (entryPt == -1) {
            entryPt = id;
            topLayer = lvl;
            return;
        }

        int ep = entryPt;
        for (int lc = topLayer; lc > lvl; lc--) {
            HNSWNode epNode = G.get(ep);
            if (lc < epNode.nbrs.size()) {
                List<SearchResult> w = searchLayer(item.vec(), ep, 1, lc, dist);
                if (!w.isEmpty()) {
                    ep = w.get(0).id();
                }
            }
        }

        for (int lc = Math.min(topLayer, lvl); lc >= 0; lc--) {
            List<SearchResult> w = searchLayer(item.vec(), ep, efBuild, lc, dist);
            int maxM = (lc == 0) ? M0 : M;
            List<Integer> sel = selectNbrs(w, maxM);
            G.get(id).nbrs.set(lc, sel);

            for (int nid : sel) {
                HNSWNode neighbor = G.get(nid);
                if (neighbor == null)
                    continue;

                while (neighbor.nbrs.size() <= lc) {
                    neighbor.nbrs.add(new ArrayList<>());
                }
                List<Integer> conn = neighbor.nbrs.get(lc);
                conn.add(id);

                if (conn.size() > maxM) {
                    List<SearchResult> ds = new ArrayList<>();
                    for (int c : conn) {
                        if (G.containsKey(c)) {
                            double d = dist.distance(neighbor.item.vec(), G.get(c).item.vec());
                            ds.add(new SearchResult(c, d));
                        }
                    }
                    Collections.sort(ds);
                    conn.clear();
                    for (int i = 0; i < maxM && i < ds.size(); i++) {
                        conn.add(ds.get(i).id());
                    }
                }
            }

            if (!w.isEmpty()) {
                ep = w.get(0).id();
            }
        }

        if (lvl > topLayer) {
            topLayer = lvl;
            entryPt = id;
        }
    }

    public List<SearchResult> knn(List<Float> query, int k, int ef, DistFn dist) {
        if (entryPt == -1) {
            return new ArrayList<>();
        }

        int ep = entryPt;
        for (int lc = topLayer; lc > 0; lc--) {
            HNSWNode epNode = G.get(ep);
            if (lc < epNode.nbrs.size()) {
                List<SearchResult> w = searchLayer(query, ep, 1, lc, dist);
                if (!w.isEmpty()) {
                    ep = w.get(0).id();
                }
            }
        }

        List<SearchResult> w = searchLayer(query, ep, Math.max(ef, k), 0, dist);
        if (w.size() > k) {
            w = w.subList(0, k);
        }
        return w;
    }

    public void remove(int id) {
        if (!G.containsKey(id))
            return;

        for (HNSWNode node : G.values()) {
            for (List<Integer> layer : node.nbrs) {
                layer.removeIf(n -> n == id);
            }
        }

        if (entryPt == id) {
            entryPt = -1;
            for (int nid : G.keySet()) {
                if (nid != id) {
                    entryPt = nid;
                    break;
                }
            }
        }

        G.remove(id);
    }

    public GraphInfo getInfo() {
        GraphInfo gi = new GraphInfo();
        gi.topLayer = topLayer;
        gi.nodeCount = G.size();

        int maxL = Math.max(topLayer + 1, 1);
        for (int i = 0; i < maxL; i++) {
            gi.nodesPerLayer.add(0);
            gi.edgesPerLayer.add(0);
        }

        for (Map.Entry<Integer, HNSWNode> entry : G.entrySet()) {
            int id = entry.getKey();
            HNSWNode node = entry.getValue();
            gi.nodes.add(new GraphInfo.NodeView(id, node.item.label(), node.maxLyr));

            for (int lc = 0; lc <= node.maxLyr && lc < maxL; lc++) {
                gi.nodesPerLayer.set(lc, gi.nodesPerLayer.get(lc) + 1);
                if (lc < node.nbrs.size()) {
                    for (int nid : node.nbrs.get(lc)) {
                        if (id < nid) {
                            gi.edgesPerLayer.set(lc, gi.edgesPerLayer.get(lc) + 1);
                            gi.edges.add(new GraphInfo.EdgeView(id, nid, lc));
                        }
                    }
                }
            }
        }

        return gi;
    }

    public int size() {
        return G.size();
    }
}