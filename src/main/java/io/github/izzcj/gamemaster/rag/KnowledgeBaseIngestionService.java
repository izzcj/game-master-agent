package io.github.izzcj.gamemaster.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 将知识库文档切分后写入 pgvector
 *
 * @author Ale
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseIngestionService {

    /**
     * 仅允许由字母、数字和下划线组成的 SQL 标识符，避免拼接表名时引入非法字符。
     */
    private static final Pattern SQL_IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * VectorStore
     */
    private final VectorStore vectorStore;

    /**
     * JdbcTemplate
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 文本切分器
     */
    private final TokenTextSplitter tokenTextSplitter;

    /**
     * 数据库模式名
     */
    private final String schemaName;

    /**
     * 向量表名
     */
    private final String tableName;

    /**
     * 当目标表尚未存在数据时，将 Markdown 文档切分后写入 pgvector。
     *
     * @param documents 已加载的 Markdown 文档
     * @return 是否实际执行了导入
     */
    public boolean ingest(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.info("Skip knowledge base ingestion because no markdown documents were loaded.");
            return false;
        }

        if (this.hasIndexedDocuments()) {
            log.info("Skip knowledge base ingestion because pgvector table {} already contains data.", this.qualifiedTableName());
            return false;
        }

        List<Document> chunks = this.tokenTextSplitter.apply(documents);
        if (CollectionUtils.isEmpty(chunks)) {
            log.info("Skip knowledge base ingestion because markdown documents produced no chunks.");
            return false;
        }

        this.vectorStore.add(chunks);
        log.info("Ingested {} markdown chunks into {}.", chunks.size(), this.qualifiedTableName());
        return true;
    }

    /**
     * 检查目标向量表中是否已经存在索引数据。
     */
    boolean hasIndexedDocuments() {
        try {
            Long count = this.jdbcTemplate.queryForObject(
                    "select count(*) from " + this.qualifiedTableName(),
                    Long.class
            );
            return count != null && count > 0L;
        } catch (DataAccessException ex) {
            log.warn("Failed to inspect pgvector table {} before ingestion. Proceeding with ingestion attempt.", this.qualifiedTableName(), ex);
            return false;
        }
    }

    /**
     * 生成带 schema 的完整表名。
     */
    private String qualifiedTableName() {
        return this.normalizeIdentifier(this.schemaName) + "." + this.normalizeIdentifier(this.tableName);
    }

    /**
     * 校验并返回可安全拼接到 SQL 中的标识符。
     */
    private String normalizeIdentifier(String identifier) {
        if (!SQL_IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
        return identifier;
    }
}
