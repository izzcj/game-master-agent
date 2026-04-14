package io.github.izzcj.gamemaster.model.response;

import java.time.LocalDateTime;

/**
 * 摄取任务响应。
 *
 * @param id 任务 ID
 * @param fileId 文件 ID
 * @param status 状态
 * @param chunkCount chunk 数量
 * @param errorMessage 错误信息
 * @param startedAt 开始时间
 * @param finishedAt 完成时间
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
public record IngestJobResponse(
    String id,
    String fileId,
    String status,
    Integer chunkCount,
    String errorMessage,
    LocalDateTime startedAt,
    LocalDateTime finishedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
