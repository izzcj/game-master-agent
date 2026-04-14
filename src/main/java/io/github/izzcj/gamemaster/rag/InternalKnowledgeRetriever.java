package io.github.izzcj.gamemaster.rag;

import java.util.List;

/**
 * 内部知识检索器抽象。
 */
public interface InternalKnowledgeRetriever {

    /**
     * 按检索计划返回内部知识证据。
     *
     * @param plan 检索计划
     * @return 证据列表
     */
    List<Evidence> retrieve(RetrievalPlan plan);
}
