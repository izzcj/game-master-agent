package io.github.izzcj.gamemaster.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 从外部知识库目录加载 Markdown 文档
 *
 * @author Ale
 * @version 1.0.0
 */
public class MarkdownKnowledgeBaseDocumentLoader {

    /**
     * 从指定知识库根目录递归加载 Markdown 文档。
     *
     * @param knowledgeBasePath 知识库根目录
     * @return 加载后的文档集合
     * @throws IOException 当目录无法读取时抛出
     */
    public List<Document> load(Path knowledgeBasePath) throws IOException {
        if (knowledgeBasePath == null || Files.notExists(knowledgeBasePath)) {
            return List.of();
        }

        List<Document> documents = new ArrayList<>();
        try (Stream<Path> pathStream = Files.walk(knowledgeBasePath)) {
            pathStream.filter(Files::isRegularFile)
                    .filter(this::isMarkdownFile)
                    .sorted()
                    .forEach(path -> documents.addAll(this.readMarkdownDocuments(knowledgeBasePath, path)));
        }
        return List.copyOf(documents);
    }

    /**
     * 判断文件是否为 Markdown 文档。
     */
    private boolean isMarkdownFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".md");
    }

    /**
     * 读取单个 Markdown 文件，并补充统一的元数据字段。
     */
    private List<Document> readMarkdownDocuments(Path knowledgeBasePath, Path markdownPath) {
        String relativePath = normalizeRelativePath(knowledgeBasePath.relativize(markdownPath));
        String fileName = markdownPath.getFileName().toString();
        String title = StringUtils.stripFilenameExtension(fileName);

        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withAdditionalMetadata("source", markdownPath.toAbsolutePath().toString())
                .withAdditionalMetadata("fileName", fileName)
                .withAdditionalMetadata("relativePath", relativePath)
                .withAdditionalMetadata("title", title)
                .build();

        MarkdownDocumentReader reader = new MarkdownDocumentReader(new FileSystemResource(markdownPath), config);
        return reader.get().stream()
                .map(document -> this.enrichDocument(document, markdownPath, fileName, relativePath, title))
                .toList();
    }

    /**
     * 将文件路径、标题等信息补充到文档元数据中，便于后续检索和追踪来源。
     */
    private Document enrichDocument(Document document, Path markdownPath, String fileName, String relativePath, String title) {
        Map<String, Object> metadata = new LinkedHashMap<>(document.getMetadata());
        metadata.put("source", markdownPath.toAbsolutePath().toString());
        metadata.put("fileName", fileName);
        metadata.put("relativePath", relativePath);
        metadata.put("title", title);
        return new Document(document.getId(), document.getText(), metadata);
    }

    /**
     * 将相对路径统一转换为使用正斜杠的格式，便于跨平台存储和展示。
     */
    private String normalizeRelativePath(Path path) {
        return path.toString().replace('\\', '/');
    }
}
