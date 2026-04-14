package io.github.izzcj.gamemaster.rag;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 简单关键词处理工具。
 */
public final class KeywordUtils {

    private KeywordUtils() {
    }

    /**
     * 将文本拆分为去重的小写 token 列表。
     *
     * @param text 原始文本
     * @return token 列表
     */
    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
            .filter(token -> token.length() >= 2)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 计算两段文本的关键词重合度。
     *
     * @param left 左侧文本
     * @param right 右侧文本
     * @return 重合 token 数量
     */
    public static int overlapScore(String left, String right) {
        List<String> leftTokens = tokenize(left);
        List<String> rightTokens = tokenize(right);
        int score = 0;
        for (String token : leftTokens) {
            if (rightTokens.contains(token)) {
                score++;
            }
        }
        return score;
    }
}
