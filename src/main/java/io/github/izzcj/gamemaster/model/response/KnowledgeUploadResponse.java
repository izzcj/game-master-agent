package io.github.izzcj.gamemaster.model.response;

/**
 * 知识文件上传结果。
 *
 * @param fileId 文件 ID
 * @param jobId 摄取任务 ID
 * @param status 任务初始状态
 */
public record KnowledgeUploadResponse(
    String fileId,
    String jobId,
    String status
) {
}
