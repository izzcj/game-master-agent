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
     * 文本切分时的目标分块大小。
     */
    private int chunkSize = 800;

    /**
     * 文本切分时允许的最小字符块大小。
     */
    private int minChunkSizeChars = 350;

    /**
     * 允许写入向量库的最小文本长度。
     */
    private int minChunkLengthToEmbed = 10;

    /**
     * 单批文档切分后允许生成的最大分块数。
     */
    private int maxNumChunks = 10_000;
}
