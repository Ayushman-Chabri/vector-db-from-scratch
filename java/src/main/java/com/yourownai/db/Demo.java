package com.yourownai.db;

import com.yourownai.distance.DistFn;
import com.yourownai.distance.DistFnFactory;

import java.util.List;

public final class Demo {

    public static final int DIMS = 16;

    private Demo() {
    }

    public static void load(VectorDB db) {
        DistFn cosine = DistFnFactory.getDistFn("cosine");

        db.insert("[cs] Linked List: nodes connected by pointers", List.of(
                0.90f, 0.85f, 0.72f, 0.68f, 0.12f, 0.08f, 0.15f, 0.10f, 0.05f, 0.08f, 0.06f, 0.09f, 0.07f, 0.11f, 0.08f,
                0.06f), cosine);
        db.insert("[cs] Binary Search Tree: O(log n) search and insert", List.of(
                0.88f, 0.82f, 0.78f, 0.74f, 0.15f, 0.10f, 0.08f, 0.12f, 0.06f, 0.07f, 0.08f, 0.05f, 0.09f, 0.06f, 0.07f,
                0.10f), cosine);
        db.insert("[cs] Dynamic Programming: memoization overlapping subproblems", List.of(
                0.82f, 0.76f, 0.88f, 0.80f, 0.20f, 0.18f, 0.12f, 0.09f, 0.07f, 0.06f, 0.08f, 0.07f, 0.08f, 0.09f, 0.06f,
                0.07f), cosine);
        db.insert("[cs] Graph BFS and DFS: breadth and depth first traversal", List.of(
                0.85f, 0.80f, 0.75f, 0.82f, 0.18f, 0.14f, 0.10f, 0.08f, 0.06f, 0.09f, 0.07f, 0.06f, 0.10f, 0.08f, 0.09f,
                0.07f), cosine);
        db.insert("[cs] Hash Table: O(1) lookup with collision chaining", List.of(
                0.87f, 0.78f, 0.70f, 0.76f, 0.13f, 0.11f, 0.09f, 0.14f, 0.08f, 0.07f, 0.06f, 0.08f, 0.07f, 0.10f, 0.08f,
                0.09f), cosine);

        db.insert("[math] Calculus: derivatives integrals and limits", List.of(
                0.12f, 0.15f, 0.18f, 0.10f, 0.91f, 0.86f, 0.78f, 0.72f, 0.08f, 0.06f, 0.07f, 0.09f, 0.07f, 0.08f, 0.06f,
                0.10f), cosine);
        db.insert("[math] Linear Algebra: matrices eigenvalues eigenvectors", List.of(
                0.20f, 0.18f, 0.15f, 0.12f, 0.88f, 0.90f, 0.82f, 0.76f, 0.09f, 0.07f, 0.08f, 0.06f, 0.10f, 0.07f, 0.08f,
                0.09f), cosine);
        db.insert("[math] Probability: distributions random variables Bayes theorem", List.of(
                0.15f, 0.12f, 0.20f, 0.18f, 0.84f, 0.80f, 0.88f, 0.82f, 0.07f, 0.08f, 0.06f, 0.10f, 0.09f, 0.06f, 0.09f,
                0.08f), cosine);
        db.insert("[math] Number Theory: primes modular arithmetic RSA cryptography", List.of(
                0.22f, 0.16f, 0.14f, 0.20f, 0.80f, 0.85f, 0.76f, 0.90f, 0.08f, 0.09f, 0.07f, 0.06f, 0.08f, 0.10f, 0.07f,
                0.06f), cosine);
        db.insert("[math] Combinatorics: permutations combinations generating functions", List.of(
                0.18f, 0.20f, 0.16f, 0.14f, 0.86f, 0.78f, 0.84f, 0.80f, 0.06f, 0.07f, 0.09f, 0.08f, 0.06f, 0.09f, 0.10f,
                0.07f), cosine);

        db.insert("[food] Neapolitan Pizza: wood-fired dough San Marzano tomatoes", List.of(
                0.08f, 0.06f, 0.09f, 0.07f, 0.07f, 0.08f, 0.06f, 0.09f, 0.90f, 0.86f, 0.78f, 0.72f, 0.08f, 0.06f, 0.09f,
                0.07f), cosine);
        db.insert("[food] Sushi: vinegared rice raw fish and nori rolls", List.of(
                0.06f, 0.08f, 0.07f, 0.09f, 0.09f, 0.06f, 0.08f, 0.07f, 0.86f, 0.90f, 0.82f, 0.76f, 0.07f, 0.09f, 0.06f,
                0.08f), cosine);
        db.insert("[food] Ramen: noodle soup with chashu pork and soft-boiled eggs", List.of(
                0.09f, 0.07f, 0.06f, 0.08f, 0.08f, 0.09f, 0.07f, 0.06f, 0.82f, 0.78f, 0.90f, 0.84f, 0.09f, 0.07f, 0.08f,
                0.06f), cosine);
        db.insert("[food] Tacos: corn tortillas with carnitas salsa and cilantro", List.of(
                0.07f, 0.09f, 0.08f, 0.06f, 0.06f, 0.07f, 0.09f, 0.08f, 0.78f, 0.82f, 0.86f, 0.90f, 0.06f, 0.08f, 0.07f,
                0.09f), cosine);
        db.insert("[food] Croissant: laminated pastry with buttery flaky layers", List.of(
                0.06f, 0.07f, 0.10f, 0.09f, 0.10f, 0.06f, 0.07f, 0.10f, 0.85f, 0.80f, 0.76f, 0.82f, 0.09f, 0.07f, 0.10f,
                0.06f), cosine);

        db.insert("[sports] Basketball: fast-paced shooting dribbling slam dunks", List.of(
                0.09f, 0.07f, 0.08f, 0.10f, 0.08f, 0.09f, 0.07f, 0.06f, 0.08f, 0.07f, 0.09f, 0.06f, 0.91f, 0.85f, 0.78f,
                0.72f), cosine);
        db.insert("[sports] Football: tackles touchdowns field goals and strategy", List.of(
                0.07f, 0.09f, 0.06f, 0.08f, 0.09f, 0.07f, 0.10f, 0.08f, 0.07f, 0.09f, 0.08f, 0.07f, 0.87f, 0.89f, 0.82f,
                0.76f), cosine);
        db.insert("[sports] Tennis: racket volleys groundstrokes and Wimbledon serves", List.of(
                0.08f, 0.06f, 0.09f, 0.07f, 0.07f, 0.08f, 0.06f, 0.09f, 0.09f, 0.06f, 0.07f, 0.08f, 0.83f, 0.80f, 0.88f,
                0.82f), cosine);
        db.insert("[sports] Chess: openings endgames tactics strategic board game", List.of(
                0.25f, 0.20f, 0.22f, 0.18f, 0.22f, 0.18f, 0.20f, 0.15f, 0.06f, 0.08f, 0.07f, 0.09f, 0.80f, 0.84f, 0.78f,
                0.90f), cosine);
        db.insert("[sports] Swimming: butterfly freestyle backstroke Olympic competition", List.of(
                0.06f, 0.08f, 0.07f, 0.09f, 0.08f, 0.06f, 0.09f, 0.07f, 0.10f, 0.08f, 0.06f, 0.07f, 0.85f, 0.82f, 0.86f,
                0.80f), cosine);
    }
}