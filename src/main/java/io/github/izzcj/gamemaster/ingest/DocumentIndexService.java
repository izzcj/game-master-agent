package io.github.izzcj.gamemaster.ingest;

import io.github.izzcj.gamemaster.mapper.KnowledgeChunkSnapshotMapper;
import io.github.izzcj.gamemaster.model.entity.KnowledgeChunkSnapshotEntity;
import io.github.izzcj.gamemaster.model.entity.KnowledgeFileEntity;
import io.github.izzcj.gamemaster.support.util.IdGenerator;
import io.github.izzcj.gamemaster.support.util.JsonUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 文档索引服务。
 *
 * <p>负责写入 chunk 快照，并在可用时同步写入向量库。
 */
@Service
public class DocumentIndexService {

    private final KnowledgeChunkSnapshotMapper chunkSnapshotMapper;
    private final ObjectProvider<VectorStore> vectorStoreProvider;

    public DocumentIndexService(
        KnowledgeChunkSnapshotMapper chunkSnapshotMapper,
        ObjectProvider<VectorStore> vectorStoreProvider
    ) {
        this.chunkSnapshotMapper = chunkSnapshotMapper;
        this.vectorStoreProvider = vectorStoreProvider;
    }

    /**
     * 重建指定文件的 chunk 快照和向量索引。
     *
     * @param file 文件元数据
     * @param chunks 文本分块
     * @return chunk 数量
     */
    public int reindex(KnowledgeFileEntity file, List<String> chunks) {
        chunkSnapshotMapper.deleteByFileId(file.getId());
        List<KnowledgeChunkSnapshotEntity> snapshots = new ArrayList<>();
        List<Document> documents = new ArrayList<>();
        for (int index = 0; index < chunks.size(); index++) {
            String chunk = chunks.get(index);
            Map<String, Object> metadata = baseMetadata(file, index);
            KnowledgeChunkSnapshotEntity snapshot = new KnowledgeChunkSnapshotEntity();
            snapshot.setId(IdGenerator.newId("chunk"));
            snapshot.setFileId(file.getId());
            snapshot.setKnowledgeBaseId(file.getKnowledgeBaseId());
            snapshot.setChunkIndex(index);
            snapshot.setTitle(file.getFileName());
            snapshot.setGameName(file.getGameName());
            snapshot.setPlatform(file.getPlatform());
            snapshot.setLanguage("auto");
            snapshot.setTags(file.getTags());
            snapshot.setSourceUrl(file.getStoragePath());
            snapshot.setContent(chunk);
            snapshot.setMetadataJson(JsonUtils.toJson(metadata));
            snapshot.setCreatedAt(LocalDateTime.now());
            snapshots.add(snapshot);
            documents.add(new Document(chunk, metadata));
        }
        if (!snapshots.isEmpty()) {
            chunkSnapshotMapper.insertBatch(snapshots);
        }
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore != null && !documents.isEmpty()) {
            vectorStore.add(documents);
        }
        return snapshots.size();
    }

    private Map<String, Object> baseMetadata(KnowledgeFileEntity file, int index) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("knowledgeBaseId", file.getKnowledgeBaseId());
        metadata.put("fileId", file.getId());
        metadata.put("gameName", file.getGameName());
        metadata.put("sourceType", file.getSourceType());
        metadata.put("title", file.getFileName());
        metadata.put("platform", file.getPlatform());
        metadata.put("language", "auto");
        metadata.put("tags", file.getTags());
        metadata.put("chunkIndex", index);
        metadata.put("sourceUrl", file.getStoragePath());
        return metadata;
    }
}
