package com.liys.ai_assistant.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 定长字符分块，带重叠。
 */
public final class TextSplitter {

    private TextSplitter() {
    }

    public static List<String> split(String text, int chunkSize, int chunkOverlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        if (chunkSize <= 0 || chunkOverlap < 0 || chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException("chunkSize 必须大于 0，且 chunkOverlap 必须在 0 到 chunkSize 之间");
        }
        int step = chunkSize - chunkOverlap;
        for (int start = 0; start < text.length(); start += step) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            if (end == text.length()) {
                break;
            }
        }
        return chunks;
    }
}
