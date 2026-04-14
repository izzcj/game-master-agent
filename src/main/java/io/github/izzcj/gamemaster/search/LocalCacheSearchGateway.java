package io.github.izzcj.gamemaster.search;

import io.github.izzcj.gamemaster.mapper.WebSourceCacheMapper;
import io.github.izzcj.gamemaster.model.entity.WebSourceCacheEntity;
import io.github.izzcj.gamemaster.rag.Evidence;
import io.github.izzcj.gamemaster.rag.KeywordUtils;
import io.github.izzcj.gamemaster.support.enums.EvidenceType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 基于本地缓存表的搜索实现。
 *
 * <p>用于在未接入真实搜索服务前提供可运行的搜索结果。
 */
@Component
public class LocalCacheSearchGateway implements SearchGateway {

    private final WebSourceCacheMapper webSourceCacheMapper;

    public LocalCacheSearchGateway(WebSourceCacheMapper webSourceCacheMapper) {
        this.webSourceCacheMapper = webSourceCacheMapper;
    }

    /**
     * 从缓存表中按关键词重排，返回命中的网页摘要证据。
     *
     * @param queryText 查询文本
     * @param topK 召回条数
     * @return 证据列表
     */
    @Override
    public List<Evidence> search(String queryText, int topK) {
        return webSourceCacheMapper.findAll().stream()
            .map(source -> toEvidence(queryText, source))
            .filter(evidence -> evidence.score() > 0)
            .sorted(Comparator.comparingDouble(Evidence::score).reversed())
            .limit(topK)
            .collect(Collectors.toList());
    }

    private Evidence toEvidence(String queryText, WebSourceCacheEntity entity) {
        int score = KeywordUtils.overlapScore(queryText, entity.getQueryText() + " " + entity.getTitle() + " " + entity.getSnippet()) + 1;
        return new Evidence(
            EvidenceType.WEB_SNIPPET,
            entity.getTitle(),
            entity.getSnippet(),
            "WEB",
            entity.getSourceName(),
            entity.getUrl(),
            score,
            Map.of("queryText", entity.getQueryText(), "sourceName", entity.getSourceName())
        );
    }
}
