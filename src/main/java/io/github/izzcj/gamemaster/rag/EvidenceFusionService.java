package io.github.izzcj.gamemaster.rag;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 证据融合服务。
 *
 * <p>负责对内部知识和外部搜索结果去重、排序和裁剪。
 */
@Service
public class EvidenceFusionService {

    /**
     * 融合两类证据并输出最终结果集。
     *
     * @param internalEvidence 内部证据
     * @param externalEvidence 外部证据
     * @param topK 最终保留条数
     * @return 融合后的证据列表
     */
    public List<Evidence> fuse(List<Evidence> internalEvidence, List<Evidence> externalEvidence, int topK) {
        Map<String, Evidence> unique = new LinkedHashMap<>();
        internalEvidence.stream().sorted(Comparator.comparingDouble(Evidence::score).reversed())
            .forEach(evidence -> unique.putIfAbsent(dedupKey(evidence), evidence));
        externalEvidence.stream().sorted(Comparator.comparingDouble(Evidence::score).reversed())
            .forEach(evidence -> unique.putIfAbsent(dedupKey(evidence), evidence));
        return unique.values().stream()
            .sorted(Comparator.comparingDouble(Evidence::score).reversed())
            .limit(topK)
            .collect(Collectors.toList());
    }

    private String dedupKey(Evidence evidence) {
        return evidence.sourceType() + "|" + evidence.title() + "|" + evidence.locator();
    }
}
