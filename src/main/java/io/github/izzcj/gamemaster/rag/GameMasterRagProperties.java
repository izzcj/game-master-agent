package io.github.izzcj.gamemaster.rag;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 攻略知识库 RAG 模块的配置项
 *
 * @author Ale
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "gamemaster.rag")
public class GameMasterRagProperties {

    /**
     * 是否启用基于本地文件的 RAG 流程。
     */
    private boolean enabled = true;

    /**
     * 外部 Markdown 知识库目录。
     */
    private String knowledgeBasePath = "knowledge_base";

    /**
     * 向量检索返回的最大结果数。
     */
    private int topK = 4;

    /**
     * 向量检索相似度阈值。
     */
    private double similarityThreshold = 0.6d;

    /**
     * 语义切分配置。
     */
    private SemanticProperties semantic = new SemanticProperties();

    @Data
    public static class SemanticProperties {

        /**
         * 单个分块允许的最大字符数。
         */
        private int maxChunkChars = 1_200;

        /**
         * 优先向后合并的最小字符数。
         */
        private int minChunkChars = 280;

        /**
         * 相邻片段允许合并的最小相似度。
         */
        private double similarityThreshold = 0.82d;

        /**
         * 最多向后尝试合并的片段数。
         */
        private int maxMergeLookahead = 2;

        /**
         * 是否在最终分块中保留标题路径。
         */
        private boolean preserveHeadings = true;
    }
}
