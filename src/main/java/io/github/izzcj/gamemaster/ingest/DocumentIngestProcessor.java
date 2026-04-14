package io.github.izzcj.gamemaster.ingest;

import io.github.izzcj.gamemaster.mapper.IngestJobMapper;
import io.github.izzcj.gamemaster.mapper.KnowledgeFileMapper;
import io.github.izzcj.gamemaster.model.entity.IngestJobEntity;
import io.github.izzcj.gamemaster.model.entity.KnowledgeFileEntity;
import io.github.izzcj.gamemaster.support.enums.IngestJobStatus;
import io.github.izzcj.gamemaster.support.enums.KnowledgeFileStatus;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 文档摄取处理器。
 *
 * <p>异步完成文档解析、清洗、切分、索引和状态更新。
 */
@Service
public class DocumentIngestProcessor {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestProcessor.class);

    private final KnowledgeFileMapper knowledgeFileMapper;
    private final IngestJobMapper ingestJobMapper;
    private final ObjectStorageService objectStorageService;
    private final DocumentParseService documentParseService;
    private final DocumentCleanService documentCleanService;
    private final ChunkService chunkService;
    private final DocumentIndexService documentIndexService;

    public DocumentIngestProcessor(
        KnowledgeFileMapper knowledgeFileMapper,
        IngestJobMapper ingestJobMapper,
        ObjectStorageService objectStorageService,
        DocumentParseService documentParseService,
        DocumentCleanService documentCleanService,
        ChunkService chunkService,
        DocumentIndexService documentIndexService
    ) {
        this.knowledgeFileMapper = knowledgeFileMapper;
        this.ingestJobMapper = ingestJobMapper;
        this.objectStorageService = objectStorageService;
        this.documentParseService = documentParseService;
        this.documentCleanService = documentCleanService;
        this.chunkService = chunkService;
        this.documentIndexService = documentIndexService;
    }

    /**
     * 异步执行指定摄取任务。
     *
     * @param jobId 任务 ID
     */
    @Async("ingestTaskExecutor")
    public void process(String jobId) {
        IngestJobEntity job = ingestJobMapper.findById(jobId);
        if (job == null) {
            return;
        }
        KnowledgeFileEntity file = knowledgeFileMapper.findById(job.getFileId());
        if (file == null) {
            job.setStatus(IngestJobStatus.FAILED.name());
            job.setErrorMessage("Knowledge file not found");
            job.setFinishedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            ingestJobMapper.update(job);
            return;
        }
        try {
            markRunning(job, file);
            String rawText;
            try (AutoCloseableStoredObject storedObject = toAutoCloseable(objectStorageService.get(file.getStoragePath()))) {
                rawText = documentParseService.parse(storedObject.inputStream());
            }
            String cleanText = documentCleanService.clean(rawText);
            List<String> chunks = chunkService.split(cleanText);
            int chunkCount = documentIndexService.reindex(file, chunks);
            file.setSummary(summarize(cleanText));
            file.setStatus(KnowledgeFileStatus.READY.name());
            file.setUpdatedAt(LocalDateTime.now());
            knowledgeFileMapper.updateStatus(file);
            job.setStatus(IngestJobStatus.SUCCEEDED.name());
            job.setChunkCount(chunkCount);
            job.setFinishedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            ingestJobMapper.update(job);
        } catch (Exception exception) {
            log.warn("Failed to process ingest job {}", jobId, exception);
            file.setStatus(KnowledgeFileStatus.FAILED.name());
            file.setSummary(null);
            file.setUpdatedAt(LocalDateTime.now());
            knowledgeFileMapper.updateStatus(file);
            job.setStatus(IngestJobStatus.FAILED.name());
            job.setErrorMessage(exception.getMessage());
            job.setFinishedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            ingestJobMapper.update(job);
        }
    }

    private void markRunning(IngestJobEntity job, KnowledgeFileEntity file) {
        file.setStatus(KnowledgeFileStatus.PROCESSING.name());
        file.setUpdatedAt(LocalDateTime.now());
        knowledgeFileMapper.updateStatus(file);
        job.setStatus(IngestJobStatus.RUNNING.name());
        job.setStartedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        ingestJobMapper.update(job);
    }

    /**
     * 将存储对象包装成可用于 try-with-resources 的类型。
     *
     * @param object 存储对象
     * @return 可自动关闭的包装
     */
    private AutoCloseableStoredObject toAutoCloseable(ObjectStorageService.StoredObject object) {
        return new AutoCloseableStoredObject(object);
    }

    /**
     * 生成文件摘要。
     *
     * @param cleanText 清洗后的全文
     * @return 摘要文本
     */
    private String summarize(String cleanText) {
        return cleanText.length() <= 240 ? cleanText : cleanText.substring(0, 240) + "...";
    }

    /**
     * 方便在 try-with-resources 中关闭底层输入流的包装对象。
     */
    private static class AutoCloseableStoredObject implements AutoCloseable {

        private final ObjectStorageService.StoredObject delegate;

        private AutoCloseableStoredObject(ObjectStorageService.StoredObject delegate) {
            this.delegate = delegate;
        }

        /**
         * 返回底层输入流。
         *
         * @return 输入流
         */
        public java.io.InputStream inputStream() {
            return delegate.inputStream();
        }

        @Override
        public void close() throws IOException {
            delegate.inputStream().close();
        }
    }
}
