package com.yourownai.index;

public record SearchResult(int id, double distance) implements Comparable<SearchResult> {

    @Override
    public int compareTo(SearchResult other) {
        int cmp = Double.compare(this.distance, other.distance);
        if (cmp != 0)
            return cmp;
        return Integer.compare(this.id, other.id);
    }
}