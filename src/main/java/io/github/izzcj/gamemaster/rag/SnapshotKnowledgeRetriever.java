package io.github.izzcj.gamemaster.rag;

import io.github.izzcj.gamemaster.mapper.KnowledgeChunkSnapshotMapper;
import io.github.izzcj.gamemaster.model.entity.KnowledgeChunkSnapshotEntity;
import io.github.izzcj.gamemaster.support.enums.EvidenceType;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 基于 {@code knowledge_chunk_snapshot} 表的内部检索实现。
 */
@Component
public class SnapshotKnowledgeRetriever implements InternalKnowledgeRetriever {

    private final KnowledgeChunkSnapshotMapper chunkSnapshotMapper;

    public SnapshotKnowledgeRetriever(KnowledgeChunkSnapshotMapper chunkSnapshotMapper) {
        this.chunkSnapshotMapper = chunkSnapshotMapper;
    }

    /**
     * 查询候选 chunk，并基于关键词重合度进行简单排序。
     *
     * @param plan 检索计划
     * @return 内部证据列表
     */
    @Override
    public List<Evidence> retrieve(RetrievalPlan plan) {
        List<KnowledgeChunkSnapshotEntity> candidates =
            chunkSnapshotMapper.findCandidates(plan.knowledgeBaseIds(), plan.resolvedGame());
        return candidates.stream()
            .map(chunk -> toEvidence(plan.queryText(), chunk))
            .filter(evidence -> evidence.score() > 0)
            .sorted(Comparator.comparingDouble(Evidence::score).reversed())
            .limit(plan.topK())
            .collect(Collectors.toList());
    }

    private Evidence toEvidence(String query, KnowledgeChunkSnapshotEntity chunk) {
        int score = KeywordUtils.overlapScore(query, chunk.getContent()) + 1;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileId", chunk.getFileId());
        metadata.put("knowledgeBaseId", chunk.getKnowledgeBaseId());
        metadata.put("chunkIndex", chunk.getChunkIndex());
        metadata.put("platform", chunk.getPlatform());
        metadata.put("gameName", chunk.getGameName());
        return new Evidence(
            EvidenceType.INTERNAL_CHUNK,
            chunk.getTitle() != null ? chunk.getTitle() : "Knowledge Chunk",
            chunk.getContent(),
            "INTERNAL",
            chunk.getFileId() + "#" + chunk.getChunkIndex(),
            chunk.getSourceUrl(),
            score,
            metadata
        );
    }
}
