package io.github.izzcj.gamemaster.ingest;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 文本分块服务。
 */
@Service
public class ChunkService {

    private static final int CHUNK_SIZE = 800;
    private static final int OVERLAP = 120;

    /**
     * 将文本拆分为带重叠窗口的 chunk 列表。
     *
     * @param content 原始文本
     * @return chunk 列表
     */
    public List<String> split(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(content.length(), start + CHUNK_SIZE);
            chunks.add(content.substring(start, end).trim());
            if (end == content.length()) {
                break;
            }
            start = Math.max(end - OVERLAP, start + 1);
        }
        return chunks;
    }
}
