package com.yourownai.db;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextChunkerTest {

    @Test
    void emptyTextReturnsNoChunks() {
        assertTrue(TextChunker.chunkText("").isEmpty());
        assertTrue(TextChunker.chunkText("   ").isEmpty());
    }

    @Test
    void shortTextReturnsSingleChunkUnmodified() {
        String text = "the quick brown fox jumps";
        List<String> chunks = TextChunker.chunkText(text, 250, 30);

        assertEquals(1, chunks.size());
        assertEquals(text, chunks.get(0));
    }

    @Test
    void longTextSplitsIntoOverlappingChunks() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            if (i > 0)
                sb.append(' ');
            sb.append("w").append(i);
        }

        List<String> chunks = TextChunker.chunkText(sb.toString(), 10, 3);

        assertEquals(4, chunks.size());
        assertEquals("w0 w1 w2 w3 w4 w5 w6 w7 w8 w9", chunks.get(0));
        assertEquals("w7 w8 w9 w10 w11 w12 w13 w14 w15 w16", chunks.get(1));
        assertEquals("w14 w15 w16 w17 w18 w19 w20 w21 w22 w23", chunks.get(2));
        assertEquals("w21 w22 w23 w24", chunks.get(3));
    }

    @Test
    void consecutiveChunksOverlapByRequestedWordCount() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            if (i > 0)
                sb.append(' ');
            sb.append("w").append(i);
        }

        List<String> chunks = TextChunker.chunkText(sb.toString(), 10, 3);

        String[] chunk0Words = chunks.get(0).split(" ");
        String[] chunk1Words = chunks.get(1).split(" ");
        assertEquals(chunk0Words[7], chunk1Words[0]);
        assertEquals(chunk0Words[8], chunk1Words[1]);
        assertEquals(chunk0Words[9], chunk1Words[2]);
    }
}