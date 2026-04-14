package io.github.izzcj.gamemaster.model.entity;

import java.time.LocalDateTime;

/**
 * 会话消息实体。
 */
public class ChatMessageEntity {

    /** 消息主键。 */
    private String id;
    /** 所属会话 ID。 */
    private String sessionId;
    /** 消息角色。 */
    private String role;
    /** 消息内容。 */
    private String content;
    /** 引用 JSON。 */
    private String citations;
    /** 创建时间。 */
    private LocalDateTime createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCitations() {
        return citations;
    }

    public void setCitations(String citations) {
        this.citations = citations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
