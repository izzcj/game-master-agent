package io.github.izzcj.gamemaster.ingest;

import org.springframework.stereotype.Service;

/**
 * 文本清洗服务。
 */
@Service
public class DocumentCleanService {

    /**
     * 对解析后的文本做基础清洗，包括空字符、空白与换行归一化。
     *
     * @param rawText 原始文本
     * @return 清洗后的文本
     */
    public String clean(String rawText) {
        if (rawText == null) {
            return "";
        }
        return rawText
            .replace('\u0000', ' ')
            .replaceAll("\\r", "\n")
            .replaceAll("[ \\t]+", " ")
            .replaceAll("\\n{3,}", "\n\n")
            .trim();
    }
}
