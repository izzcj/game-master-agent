package io.github.izzcj.gamemaster.application;

import io.github.izzcj.gamemaster.model.response.CitationResponse;
import java.util.List;

/**
 * 答案生成结果。
 *
 * @param answer 最终回答文本
 * @param citations 引用列表
 */
public record AnswerGenerationResult(String answer, List<CitationResponse> citations) {
}
