package io.github.izzcj.gamemaster.rag;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 攻略问答 RAG 链路的自动配置。
 *
 * @author Ale
 * @version 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(GameMasterRagProperties.class)
@ConditionalOnProperty(prefix = "gamemaster.rag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GameMasterRagAutoConfiguration {

    /**
     * 创建面向攻略问答场景的向量检索顾问。
     */
    @Bean
    public QuestionAnswerAdvisor walkthroughQuestionAnswerAdvisor(VectorStore vectorStore, GameMasterRagProperties properties) {
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(properties.getTopK())
                .similarityThreshold(properties.getSimilarityThreshold())
                .build();
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();
    }

    /**
     * 创建知识库 Markdown 文档加载器。
     */
    @Bean
    public MarkdownKnowledgeBaseDocumentLoader markdownKnowledgeBaseDocumentLoader() {
        return new MarkdownKnowledgeBaseDocumentLoader();
    }

    /**
     * 创建基于 EmbeddingModel 的语义 Markdown 切分器。
     */
    @Bean
    public SemanticMarkdownChunker semanticMarkdownChunker(
            EmbeddingModel embeddingModel,
            GameMasterRagProperties properties
    ) {
        return new SemanticMarkdownChunker(embeddingModel, properties.getSemantic());
    }

    /**
     * 创建知识库入库服务，负责将切分后的文档写入 pgvector。
     */
    @Bean
    public KnowledgeBaseIngestionService knowledgeBaseIngestionService(
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate,
            SemanticMarkdownChunker semanticMarkdownChunker,
            @Value("${spring.ai.vectorstore.pgvector.schema-name:public}") String schemaName,
            @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}") String tableName
    ) {
        return new KnowledgeBaseIngestionService(vectorStore, jdbcTemplate, semanticMarkdownChunker, schemaName, tableName);
    }

    /**
     * 创建启动时执行的知识库导入器。
     */
    @Bean
    public KnowledgeBaseIngestionRunner knowledgeBaseIngestionRunner(
            GameMasterRagProperties properties,
            MarkdownKnowledgeBaseDocumentLoader documentLoader,
            KnowledgeBaseIngestionService ingestionService
    ) {
        return new KnowledgeBaseIngestionRunner(properties, documentLoader, ingestionService);
    }
}
