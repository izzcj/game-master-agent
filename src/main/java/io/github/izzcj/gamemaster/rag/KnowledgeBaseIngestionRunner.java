package io.github.izzcj.gamemaster.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 应用启动后加载本地 Markdown 知识库
 *
 * @author Ale
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseIngestionRunner implements ApplicationRunner {

    /**
     * 配置属性
     */
    private final GameMasterRagProperties properties;

    /**
     * Markdown 文档加载器
     */
    private final MarkdownKnowledgeBaseDocumentLoader documentLoader;

    /**
     * 知识库入库服务
     */
    private final KnowledgeBaseIngestionService ingestionService;

    /**
     * 启动时检查知识库目录是否存在，存在时执行文档导入。
     */
    @Override
    public void run(ApplicationArguments args) throws IOException {
        Path knowledgeBasePath = Path.of(this.properties.getKnowledgeBasePath()).toAbsolutePath().normalize();
        if (Files.notExists(knowledgeBasePath)) {
            log.info("Skip knowledge base ingestion because directory does not exist: {}", knowledgeBasePath);
            return;
        }

        this.ingestionService.ingest(this.documentLoader.load(knowledgeBasePath));
    }
}
