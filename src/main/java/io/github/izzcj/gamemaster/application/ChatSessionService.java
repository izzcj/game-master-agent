package io.github.izzcj.gamemaster.application;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.izzcj.gamemaster.mapper.ChatMessageMapper;
import io.github.izzcj.gamemaster.mapper.ChatSessionMapper;
import io.github.izzcj.gamemaster.model.entity.ChatMessageEntity;
import io.github.izzcj.gamemaster.model.entity.ChatSessionEntity;
import io.github.izzcj.gamemaster.model.response.CitationResponse;
import io.github.izzcj.gamemaster.support.enums.ChatRole;
import io.github.izzcj.gamemaster.support.util.IdGenerator;
import io.github.izzcj.gamemaster.support.util.JsonUtils;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 会话读写服务。
 */
@Service
public class ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;

    public ChatSessionService(ChatSessionMapper chatSessionMapper, ChatMessageMapper chatMessageMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    /**
     * 确保会话存在；若未提供或不存在，则创建新会话。
     *
     * @param sessionId 客户端传入的会话 ID
     * @param question 当前问题，用于生成标题
     * @return 可用会话 ID
     */
    public String ensureSession(String sessionId, String question) {
        if (sessionId != null && !sessionId.isBlank()) {
            ChatSessionEntity existing = chatSessionMapper.findById(sessionId);
            if (existing != null) {
                existing.setTitle(question.length() > 80 ? question.substring(0, 80) : question);
                existing.setUpdatedAt(LocalDateTime.now());
                chatSessionMapper.update(existing);
                return existing.getId();
            }
        }
        ChatSessionEntity created = new ChatSessionEntity();
        created.setId(IdGenerator.newId("session"));
        created.setTitle(question.length() > 80 ? question.substring(0, 80) : question);
        created.setCreatedAt(LocalDateTime.now());
        created.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.insert(created);
        return created.getId();
    }

    /**
     * 追加用户消息。
     *
     * @param sessionId 会话 ID
     * @param question 用户问题
     */
    public void appendUserMessage(String sessionId, String question) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setId(IdGenerator.newId("msg"));
        entity.setSessionId(sessionId);
        entity.setRole(ChatRole.USER.name());
        entity.setContent(question);
        entity.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(entity);
    }

    /**
     * 追加助手消息和引用。
     *
     * @param sessionId 会话 ID
     * @param answer 答案
     * @param citations 引用列表
     */
    public void appendAssistantMessage(String sessionId, String answer, List<CitationResponse> citations) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setId(IdGenerator.newId("msg"));
        entity.setSessionId(sessionId);
        entity.setRole(ChatRole.ASSISTANT.name());
        entity.setContent(answer);
        entity.setCitations(JsonUtils.toJson(citations));
        entity.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(entity);
    }

    /**
     * 将持久化的引用 JSON 转回对象列表。
     *
     * @param raw JSON 字符串
     * @return 引用列表
     */
    public List<CitationResponse> decodeCitations(String raw) {
        return JsonUtils.fromJsonList(raw, new TypeReference<List<CitationResponse>>() {
        });
    }
}
