package io.github.izzcj.gamemaster.search;

import io.github.izzcj.gamemaster.rag.Evidence;
import java.util.List;

/**
 * 外部搜索网关抽象。
 */
public interface SearchGateway {

    /**
     * 执行搜索并返回统一的证据结构。
     *
     * @param queryText 查询文本
     * @param topK 召回条数
     * @return 证据列表
     */
    List<Evidence> search(String queryText, int topK);
}
