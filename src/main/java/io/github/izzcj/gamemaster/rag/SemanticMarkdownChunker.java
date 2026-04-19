package io.github.izzcj.gamemaster.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按 Markdown 结构提取基础片段，并使用 EmbeddingModel 合并相邻语义片段。
 *
 * @author Ale
 * @version 1.0.0
 */
@RequiredArgsConstructor
public class SemanticMarkdownChunker {

    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)$");

    private static final Pattern LIST_PATTERN = Pattern.compile("^\\s*([-*+]\\s+|\\d+\\.\\s+).+$");

    private final EmbeddingModel embeddingModel;

    private final GameMasterRagProperties.SemanticProperties properties;

    /**
     * 将 Markdown 文档切分为语义分块。
     *
     * @param documents 待处理的 Markdown 文档集合
     * @return 切分后的语义分块文档集合
     */
    public List<Document> chunk(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return List.of();
        }

        return documents.stream()
                .flatMap(document -> this.chunkDocument(document).stream())
                .toList();
    }

    /**
     * 将 Markdown 文档切分为语义分块。
     *
     * @param document 待处理的 Markdown 文档
     * @return 切分后的语义分块文档集合
     */
    private List<Document> chunkDocument(Document document) {
        List<Fragment> fragments = this.extractFragments(document);
        if (CollectionUtils.isEmpty(fragments)) {
            return List.of();
        }

        List<Document> chunks = new ArrayList<>();
        int index = 0;
        int chunkIndex = 0;
        while (index < fragments.size()) {
            Fragment current = fragments.get(index);
            String mergedText = current.text();
            String structureType = current.structureType();
            boolean semanticMerged = false;
            int lookahead = 0;
            int nextIndex = index + 1;

            while (nextIndex < fragments.size()
                    && lookahead < this.properties.getMaxMergeLookahead()
                    && mergedText.length() < this.properties.getMinChunkChars()) {
                Fragment candidate = fragments.get(nextIndex);
                if (!Objects.equals(current.sectionPath(), candidate.sectionPath())) {
                    break;
                }

                String candidateText = mergedText + System.lineSeparator() + System.lineSeparator() + candidate.text();
                if (candidateText.length() > this.properties.getMaxChunkChars()) {
                    break;
                }

                double similarity = this.cosineSimilarity(
                        this.embeddingModel.embed(mergedText),
                        this.embeddingModel.embed(candidate.text())
                );
                if (similarity < this.properties.getSimilarityThreshold()) {
                    break;
                }

                mergedText = candidateText;
                semanticMerged = true;
                if (!Objects.equals(structureType, candidate.structureType())) {
                    structureType = "mixed";
                }
                nextIndex++;
                lookahead++;
            }

            chunks.add(this.toChunkDocument(document, mergedText, current.sectionPath(), structureType, chunkIndex++, semanticMerged));
            index = semanticMerged ? nextIndex : index + 1;
        }
        return chunks;
    }

    /**
     * 按 Markdown 结构提取基础片段。
     *
     * @param document 待处理的 Markdown 文档
     * @return 提取的片段集合
     */
    private List<Fragment> extractFragments(Document document) {
        List<Fragment> fragments = new ArrayList<>();
        HeadingState headingState = new HeadingState(List.of());
        List<String> paragraphBuffer = new ArrayList<>();
        List<String> listBuffer = new ArrayList<>();
        List<String> quoteBuffer = new ArrayList<>();
        List<String> tableBuffer = new ArrayList<>();
        List<String> codeBuffer = new ArrayList<>();
        boolean inCodeBlock = false;

        if (!StringUtils.hasText(document.getText())) {
            return fragments;
        }
        for (String rawLine : document.getText().split("\\R", -1)) {
            String line = rawLine.stripTrailing();

            if (line.startsWith("```")) {
                if (!inCodeBlock) {
                    this.flushTable(fragments, headingState, tableBuffer);
                    this.flushQuote(fragments, headingState, quoteBuffer);
                    this.flushList(fragments, headingState, listBuffer);
                    this.flushParagraph(fragments, headingState, paragraphBuffer);
                    inCodeBlock = true;
                    codeBuffer.add(line);
                    continue;
                }

                codeBuffer.add(line);
                this.flushBlock(fragments, headingState, codeBuffer, "code");
                inCodeBlock = false;
                continue;
            }

            if (inCodeBlock) {
                codeBuffer.add(line);
                continue;
            }

            Matcher headingMatcher = HEADING_PATTERN.matcher(line);
            if (headingMatcher.matches()) {
                this.flushTable(fragments, headingState, tableBuffer);
                this.flushQuote(fragments, headingState, quoteBuffer);
                this.flushList(fragments, headingState, listBuffer);
                this.flushParagraph(fragments, headingState, paragraphBuffer);
                headingState = headingState.withHeading(headingMatcher.group(1).length(), headingMatcher.group(2).trim());
                continue;
            }

            if (line.isBlank()) {
                this.flushTable(fragments, headingState, tableBuffer);
                this.flushQuote(fragments, headingState, quoteBuffer);
                this.flushList(fragments, headingState, listBuffer);
                this.flushParagraph(fragments, headingState, paragraphBuffer);
                continue;
            }

            if (line.startsWith(">")) {
                this.flushTable(fragments, headingState, tableBuffer);
                this.flushList(fragments, headingState, listBuffer);
                this.flushParagraph(fragments, headingState, paragraphBuffer);
                quoteBuffer.add(line);
                continue;
            }

            if (this.isTableLine(line)) {
                this.flushQuote(fragments, headingState, quoteBuffer);
                this.flushList(fragments, headingState, listBuffer);
                this.flushParagraph(fragments, headingState, paragraphBuffer);
                tableBuffer.add(line);
                continue;
            }

            if (LIST_PATTERN.matcher(line).matches()) {
                this.flushTable(fragments, headingState, tableBuffer);
                this.flushQuote(fragments, headingState, quoteBuffer);
                this.flushParagraph(fragments, headingState, paragraphBuffer);
                listBuffer.add(line);
                continue;
            }

            this.flushTable(fragments, headingState, tableBuffer);
            this.flushQuote(fragments, headingState, quoteBuffer);
            this.flushList(fragments, headingState, listBuffer);
            paragraphBuffer.add(line);
        }

        this.flushTable(fragments, headingState, tableBuffer);
        this.flushQuote(fragments, headingState, quoteBuffer);
        this.flushList(fragments, headingState, listBuffer);
        this.flushParagraph(fragments, headingState, paragraphBuffer);
        this.flushBlock(fragments, headingState, codeBuffer, "code");
        return fragments;
    }

    /**
     * 判断行是否为 Markdown 表格行。
     *
     * @param line 待检查的行内容
     * @return 是否为表格行
     */
    private boolean isTableLine(String line) {
        return line.startsWith("|") || line.endsWith("|");
    }

    /**
     * 将段落缓冲区中的内容转换为片段并添加到结果列表中。
     *
     * @param fragments 结果片段列表
     * @param headingState 当前标题状态
     * @param paragraphBuffer 段落缓冲区
     */
    private void flushParagraph(List<Fragment> fragments, HeadingState headingState, List<String> paragraphBuffer) {
        this.flushBlock(fragments, headingState, paragraphBuffer, "paragraph");
    }

    /**
     * 将列表缓冲区中的内容转换为片段并添加到结果列表中。
     *
     * @param fragments 结果片段列表
     * @param headingState 当前标题状态
     * @param listBuffer 列表缓冲区
     */
    private void flushList(List<Fragment> fragments, HeadingState headingState, List<String> listBuffer) {
        this.flushBlock(fragments, headingState, listBuffer, "list");
    }

    /**
     * 将引用缓冲区中的内容转换为片段并添加到结果列表中。
     *
     * @param fragments 结果片段列表
     * @param headingState 当前标题状态
     * @param quoteBuffer 引用缓冲区
     */
    private void flushQuote(List<Fragment> fragments, HeadingState headingState, List<String> quoteBuffer) {
        this.flushBlock(fragments, headingState, quoteBuffer, "quote");
    }

    /**
     * 将表格缓冲区中的内容转换为片段并添加到结果列表中。
     *
     * @param fragments 结果片段列表
     * @param headingState 当前标题状态
     * @param tableBuffer 表格缓冲区
     */
    private void flushTable(List<Fragment> fragments, HeadingState headingState, List<String> tableBuffer) {
        this.flushBlock(fragments, headingState, tableBuffer, "table");
    }

    /**
     * 将块缓冲区中的内容转换为片段并添加到结果列表中。
     *
     * @param fragments 结果片段列表
     * @param headingState 当前标题状态
     * @param lines 块缓冲区
     * @param structureType 块结构类型
     */
    private void flushBlock(List<Fragment> fragments, HeadingState headingState, List<String> lines, String structureType) {
        if (CollectionUtils.isEmpty(lines)) {
            return;
        }

        Fragment fragment = this.toFragment(headingState, String.join(System.lineSeparator(), lines), structureType);
        if (fragment != null) {
            fragments.add(fragment);
        }
        lines.clear();
    }

    /**
     * 将 Markdown 文本片段转换为语义片段。
     *
     * @param headingState 当前的标题状态
     * @param text 待处理的 Markdown 文本
     * @param structureType 文本的结构类型
     * @return 转换后的语义片段，若文本为空则返回 null
     */
    private Fragment toFragment(HeadingState headingState, String text, String structureType) {
        String body = text.trim();
        if (!StringUtils.hasText(body)) {
            return null;
        }
        return new Fragment(body, headingState.sectionPath(), structureType);
    }

    /**
     * 将语义片段转换为文档格式。
     *
     * @param source 原始文档
     * @param text 待处理的语义文本
     * @param sectionPath 文本所属的章节路径
     * @param structureType 文本的结构类型
     * @param chunkIndex 文本在章节中的索引
     * @param semanticMerged 是否进行了语义合并
     * @return 转换后的文档
     */
    private Document toChunkDocument(
            Document source,
            String text,
            String sectionPath,
            String structureType,
            int chunkIndex,
            boolean semanticMerged
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>(source.getMetadata());
        metadata.put("sectionPath", sectionPath);
        metadata.put("chunkIndex", chunkIndex);
        metadata.put("structureType", structureType);
        metadata.put("semanticMerged", semanticMerged);
        return new Document(this.chunkId(source, chunkIndex), this.chunkText(sectionPath, text), metadata);
    }

    /**
     * 将段落文本与标题路径合并为完整段落。
     *
     * @param sectionPath 标题路径
     * @param text 段落文本
     * @return 合并后的段落文本
     */
    private String chunkText(String sectionPath, String text) {
        if (!this.properties.isPreserveHeadings() || !StringUtils.hasText(sectionPath)) {
            return text;
        }
        return sectionPath + System.lineSeparator() + text;
    }

    /**
     * 根据原始文档和索引生成唯一且稳定的文档 ID。
     *
     * @param source 原始文档
     * @param chunkIndex 文档在章节中的索引
     * @return 唯一的文档 ID
     */
    private String chunkId(Document source, int chunkIndex) {
        String sourceId = source.getId();
        if (!StringUtils.hasText(sourceId)) {
            sourceId = String.valueOf(source.getMetadata().getOrDefault("relativePath", "chunk"));
        }
        String rawChunkId = sourceId + "#" + chunkIndex;
        return UUID.nameUUIDFromBytes(rawChunkId.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * 计算两个向量的余弦相似度。
     *
     * @param left 左侧向量
     * @param right 右侧向量
     * @return 相似度值
     */
    private double cosineSimilarity(float[] left, float[] right) {
        if (left == null || right == null || left.length != right.length || left.length == 0) {
            return 0.0d;
        }

        double dot = 0.0d;
        double leftNorm = 0.0d;
        double rightNorm = 0.0d;
        for (int index = 0; index < left.length; index++) {
            dot += left[index] * right[index];
            leftNorm += left[index] * left[index];
            rightNorm += right[index] * right[index];
        }

        if (leftNorm == 0.0d || rightNorm == 0.0d) {
            return 0.0d;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private record Fragment(String text, String sectionPath, String structureType) {
    }

    private record HeadingState(List<String> headings) {

        private HeadingState withHeading(int level, String heading) {
            List<String> next = new ArrayList<>(this.headings);
            while (next.size() >= level) {
                next.removeLast();
            }
            next.add(heading);
            return new HeadingState(List.copyOf(next));
        }

        private String sectionPath() {
            return String.join(" > ", this.headings);
        }
    }
}
