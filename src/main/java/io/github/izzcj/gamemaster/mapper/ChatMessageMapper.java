package io.github.izzcj.gamemaster.mapper;

import io.github.izzcj.gamemaster.model.entity.ChatMessageEntity;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 会话消息 Mapper。
 */
@Mapper
public interface ChatMessageMapper {

    /**
     * 插入消息记录。
     *
     * @param entity 消息实体
     */
    @Insert("""
        INSERT INTO chat_message (id, session_id, role, content, citations, created_at)
        VALUES (#{id}, #{sessionId}, #{role}, #{content}, #{citations}, #{createdAt})
        """)
    void insert(ChatMessageEntity entity);

    /**
     * 查询指定会话下的全部消息。
     *
     * @param sessionId 会话 ID
     * @return 消息列表
     */
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY created_at ASC")
    List<ChatMessageEntity> findBySessionId(String sessionId);
}
