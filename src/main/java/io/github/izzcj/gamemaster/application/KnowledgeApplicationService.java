package io.github.izzcj.gamemaster.application;

import io.github.izzcj.gamemaster.ingest.DocumentIngestProcessor;
import io.github.izzcj.gamemaster.ingest.ObjectStorageService;
import io.github.izzcj.gamemaster.mapper.IngestJobMapper;
import io.github.izzcj.gamemaster.mapper.KnowledgeBaseMapper;
import io.github.izzcj.gamemaster.mapper.KnowledgeFileMapper;
import io.github.izzcj.gamemaster.model.entity.IngestJobEntity;
import io.github.izzcj.gamemaster.model.entity.KnowledgeFileEntity;
import io.github.izzcj.gamemaster.model.response.IngestJobResponse;
import io.github.izzcj.gamemaster.model.response.KnowledgeFileResponse;
import io.github.izzcj.gamemaster.model.response.KnowledgeUploadResponse;
import io.github.izzcj.gamemaster.support.enums.IngestJobStatus;
import io.github.izzcj.gamemaster.support.enums.KnowledgeFileStatus;
import io.github.izzcj.gamemaster.support.enums.SourceType;
import io.github.izzcj.gamemaster.support.exception.BusinessException;
import io.github.izzcj.gamemaster.support.util.IdGenerator;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库应用服务。
 *
 * <p>负责文件上传、任务创建、任务查询和重建索引等用例编排。
 */
@Service
public class KnowledgeApplicationService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final KnowledgeFileMapper knowledgeFileMapper;
    private final IngestJobMapper ingestJobMapper;
    private final ObjectStorageService objectStorageService;
    private final DocumentIngestProcessor documentIngestProcessor;

    public KnowledgeApplicationService(
        KnowledgeBaseMapper knowledgeBaseMapper,
        KnowledgeFileMapper knowledgeFileMapper,
        IngestJobMapper ingestJobMapper,
        ObjectStorageService objectStorageService,
        DocumentIngestProcessor documentIngestProcessor
    ) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeFileMapper = knowledgeFileMapper;
        this.ingestJobMapper = ingestJobMapper;
        this.objectStorageService = objectStorageService;
        this.documentIngestProcessor = documentIngestProcessor;
    }

    /**
     * 上传文件并创建异步摄取任务。
     *
     * @param file 上传文件
     * @param knowledgeBaseId 知识库 ID
     * @param gameName 关联游戏名
     * @param platform 关联平台
     * @param tags 标签
     * @return 上传结果
     * @throws IOException 存储失败
     */
    public KnowledgeUploadResponse upload(
        MultipartFile file,
        String knowledgeBaseId,
        String gameName,
        String platform,
        String tags
    ) throws IOException {
        String targetKnowledgeBaseId = knowledgeBaseId == null || knowledgeBaseId.isBlank() ? "kb-default" : knowledgeBaseId;
        if (knowledgeBaseMapper.findById(targetKnowledgeBaseId) == null) {
            throw new BusinessException("Knowledge base not found: " + targetKnowledgeBaseId);
        }
        String originalFilename = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
            ? "upload.bin"
            : file.getOriginalFilename();
        String contentType = file.getContentType() == null || file.getContentType().isBlank()
            ? "application/octet-stream"
            : file.getContentType();

        String fileId = IdGenerator.newId("file");
        String objectKey = targetKnowledgeBaseId + "/" + fileId + "/" + originalFilename;
        String storagePath;
        try (var inputStream = file.getInputStream()) {
            storagePath = objectStorageService.put(objectKey, inputStream, file.getSize(), contentType);
        }

        LocalDateTime now = LocalDateTime.now();
        KnowledgeFileEntity entity = new KnowledgeFileEntity();
        entity.setId(fileId);
        entity.setKnowledgeBaseId(targetKnowledgeBaseId);
        entity.setFileName(originalFilename);
        entity.setContentType(contentType);
        entity.setStoragePath(storagePath);
        entity.setSourceType(SourceType.UPLOAD.name());
        entity.setGameName(gameName);
        entity.setPlatform(platform);
        entity.setTags(tags);
        entity.setStatus(KnowledgeFileStatus.UPLOADED.name());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        knowledgeFileMapper.insert(entity);

        IngestJobEntity job = new IngestJobEntity();
        job.setId(IdGenerator.newId("job"));
        job.setFileId(fileId);
        job.setStatus(IngestJobStatus.PENDING.name());
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        ingestJobMapper.insert(job);
        documentIngestProcessor.process(job.getId());
        return new KnowledgeUploadResponse(fileId, job.getId(), job.getStatus());
    }

    /**
     * 查询知识文件列表。
     *
     * @return 文件列表
     */
    public List<KnowledgeFileResponse> listFiles() {
        return knowledgeFileMapper.findAll().stream()
            .map(entity -> new KnowledgeFileResponse(
                entity.getId(),
                entity.getKnowledgeBaseId(),
                entity.getFileName(),
                entity.getGameName(),
                entity.getPlatform(),
                entity.getTags(),
                entity.getStatus(),
                entity.getSummary(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
            ))
            .toList();
    }

    /**
     * 查询摄取任务状态。
     *
     * @param id 任务 ID
     * @return 任务详情
     */
    public IngestJobResponse getJob(String id) {
        IngestJobEntity entity = ingestJobMapper.findById(id);
        if (entity == null) {
            throw new BusinessException("Ingest job not found: " + id);
        }
        return new IngestJobResponse(
            entity.getId(),
            entity.getFileId(),
            entity.getStatus(),
            entity.getChunkCount(),
            entity.getErrorMessage(),
            entity.getStartedAt(),
            entity.getFinishedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 重新为指定文件创建摄取任务。
     *
     * @param fileId 文件 ID
     * @return 新任务信息
     */
    public KnowledgeUploadResponse reindex(String fileId) {
        KnowledgeFileEntity file = knowledgeFileMapper.findById(fileId);
        if (file == null) {
            throw new BusinessException("Knowledge file not found: " + fileId);
        }
        LocalDateTime now = LocalDateTime.now();
        IngestJobEntity job = new IngestJobEntity();
        job.setId(IdGenerator.newId("job"));
        job.setFileId(fileId);
        job.setStatus(IngestJobStatus.PENDING.name());
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        ingestJobMapper.insert(job);
        documentIngestProcessor.process(job.getId());
        return new KnowledgeUploadResponse(fileId, job.getId(), job.getStatus());
    }
}
