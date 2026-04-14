package io.github.izzcj.gamemaster.model.entity;

import java.time.LocalDateTime;

/**
 * 文档分块快照实体。
 */
public class KnowledgeChunkSnapshotEntity {

    /** 快照主键。 */
    private String id;
    /** 文件 ID。 */
    private String fileId;
    /** 知识库 ID。 */
    private String knowledgeBaseId;
    /** 分块序号。 */
    private Integer chunkIndex;
    /** 标题。 */
    private String title;
    /** 关联游戏名。 */
    private String gameName;
    /** 关联平台。 */
    private String platform;
    /** 语言。 */
    private String language;
    /** 标签。 */
    private String tags;
    /** 来源地址。 */
    private String sourceUrl;
    /** 分块正文。 */
    private String content;
    /** 元数据 JSON。 */
    private String metadataJson;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(String knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
