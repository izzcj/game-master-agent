package io.github.izzcj.gamemaster.model.response;

import java.time.LocalDateTime;

/**
 * 知识文件响应。
 *
 * @param id 文件 ID
 * @param knowledgeBaseId 所属知识库
 * @param fileName 文件名
 * @param gameName 关联游戏
 * @param platform 关联平台
 * @param tags 标签
 * @param status 状态
 * @param summary 摘要
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record KnowledgeFileResponse(
    String id,
    String knowledgeBaseId,
    String fileName,
    String gameName,
    String platform,
    String tags,
    String status,
    String summary,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
