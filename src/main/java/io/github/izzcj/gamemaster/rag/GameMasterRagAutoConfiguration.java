package io.github.izzcj.gamemaster.rag;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
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
 * 攻略问答 RAG 链路的自动配置
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
    @ConditionalOnBean(VectorStore.class)
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
    @ConditionalOnBean(VectorStore.class)
    public MarkdownKnowledgeBaseDocumentLoader markdownKnowledgeBaseDocumentLoader() {
        return new MarkdownKnowledgeBaseDocumentLoader();
    }

    /**
     * 创建用于知识库分块的文本切分器。
     */
    @Bean
    @ConditionalOnBean(VectorStore.class)
    public TokenTextSplitter ragTokenTextSplitter(GameMasterRagProperties properties) {
        return TokenTextSplitter.builder()
                .withChunkSize(properties.getChunkSize())
                .withMinChunkSizeChars(properties.getMinChunkSizeChars())
                .withMinChunkLengthToEmbed(properties.getMinChunkLengthToEmbed())
                .withMaxNumChunks(properties.getMaxNumChunks())
                .withKeepSeparator(true)
                .build();
    }

    /**
     * 创建知识库入库服务，负责将切分后的文档写入 pgvector。
     */
    @Bean
    @ConditionalOnBean(VectorStore.class)
    public KnowledgeBaseIngestionService knowledgeBaseIngestionService(
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate,
            TokenTextSplitter ragTokenTextSplitter,
            @Value("${spring.ai.vectorstore.pgvector.schema-name:public}") String schemaName,
            @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}") String tableName
    ) {
        return new KnowledgeBaseIngestionService(vectorStore, jdbcTemplate, ragTokenTextSplitter, schemaName, tableName);
    }

    /**
     * 创建启动时执行的知识库导入器。
     */
    @Bean
    @ConditionalOnBean({VectorStore.class, KnowledgeBaseIngestionService.class})
    public KnowledgeBaseIngestionRunner knowledgeBaseIngestionRunner(
            GameMasterRagProperties properties,
            MarkdownKnowledgeBaseDocumentLoader documentLoader,
            KnowledgeBaseIngestionService ingestionService
    ) {
        return new KnowledgeBaseIngestionRunner(properties, documentLoader, ingestionService);
    }
}
