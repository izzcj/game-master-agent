package io.github.izzcj.gamemaster.model.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 问答请求参数。
 */
public class ChatQueryRequest {

    /** 用户原始问题。 */
    @NotBlank
    private String question;
    /** 客户端显式指定的游戏名。 */
    private String gameName;
    /** 客户端显式指定的平台。 */
    private String platform;
    /** 是否启用外部搜索。 */
    private Boolean useExternalSearch = Boolean.TRUE;
    /** 会话 ID，用于多轮上下文归档。 */
    private String sessionId;
    /** 限定检索范围的知识库 ID 列表。 */
    private List<String> knowledgeBaseIds;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
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

    public Boolean getUseExternalSearch() {
        return useExternalSearch;
    }

    public void setUseExternalSearch(Boolean useExternalSearch) {
        this.useExternalSearch = useExternalSearch;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getKnowledgeBaseIds() {
        return knowledgeBaseIds;
    }

    public void setKnowledgeBaseIds(List<String> knowledgeBaseIds) {
        this.knowledgeBaseIds = knowledgeBaseIds;
    }
}
