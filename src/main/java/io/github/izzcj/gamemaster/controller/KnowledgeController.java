package io.github.izzcj.gamemaster.controller;

import io.github.izzcj.gamemaster.application.KnowledgeApplicationService;
import io.github.izzcj.gamemaster.model.response.IngestJobResponse;
import io.github.izzcj.gamemaster.model.response.KnowledgeFileResponse;
import io.github.izzcj.gamemaster.model.response.KnowledgeUploadResponse;
import io.github.izzcj.gamemaster.support.ApiResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库接口控制器。
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeApplicationService knowledgeApplicationService;

    public KnowledgeController(KnowledgeApplicationService knowledgeApplicationService) {
        this.knowledgeApplicationService = knowledgeApplicationService;
    }

    /**
     * 上传知识文件。
     *
     * @param file 文件
     * @param knowledgeBaseId 知识库 ID
     * @param gameName 关联游戏
     * @param platform 平台
     * @param tags 标签
     * @return 上传结果
     * @throws IOException 上传失败
     */
    @PostMapping(path = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<KnowledgeUploadResponse> upload(
        @RequestPart("file") MultipartFile file,
        @RequestParam(value = "knowledgeBaseId", required = false) String knowledgeBaseId,
        @RequestParam(value = "gameName", required = false) String gameName,
        @RequestParam(value = "platform", required = false) String platform,
        @RequestParam(value = "tags", required = false) String tags
    ) throws IOException {
        return ApiResponse.ok(knowledgeApplicationService.upload(file, knowledgeBaseId, gameName, platform, tags));
    }

    /**
     * 查询知识文件列表。
     *
     * @return 文件列表
     */
    @GetMapping("/files")
    public ApiResponse<List<KnowledgeFileResponse>> listFiles() {
        return ApiResponse.ok(knowledgeApplicationService.listFiles());
    }

    /**
     * 重建指定文件的索引。
     *
     * @param id 文件 ID
     * @return 新任务信息
     */
    @PostMapping("/files/{id}/reindex")
    public ApiResponse<KnowledgeUploadResponse> reindex(@PathVariable("id") String id) {
        return ApiResponse.ok(knowledgeApplicationService.reindex(id));
    }

    /**
     * 查询摄取任务状态。
     *
     * @param id 任务 ID
     * @return 任务详情
     */
    @GetMapping("/jobs/{id}")
    public ApiResponse<IngestJobResponse> getJob(@PathVariable("id") String id) {
        return ApiResponse.ok(knowledgeApplicationService.getJob(id));
    }
}
