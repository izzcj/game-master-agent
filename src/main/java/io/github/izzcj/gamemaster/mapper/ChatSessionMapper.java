package io.github.izzcj.gamemaster.mapper;

import io.github.izzcj.gamemaster.model.entity.ChatSessionEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 会话 Mapper。
 */
@Mapper
public interface ChatSessionMapper {

    /**
     * 按主键查询会话。
     *
     * @param id 会话 ID
     * @return 会话实体
     */
    @Select("SELECT * FROM chat_session WHERE id = #{id}")
    ChatSessionEntity findById(String id);

    /**
     * 插入新会话。
     *
     * @param entity 会话实体
     */
    @Insert("""
        INSERT INTO chat_session (id, title, created_at, updated_at)
        VALUES (#{id}, #{title}, #{createdAt}, #{updatedAt})
        """)
    void insert(ChatSessionEntity entity);

    /**
     * 更新会话标题与更新时间。
     *
     * @param entity 会话实体
     */
    @Update("""
        UPDATE chat_session
        SET title = #{title}, updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    void update(ChatSessionEntity entity);
}
