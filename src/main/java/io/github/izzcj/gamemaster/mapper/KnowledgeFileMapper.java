package io.github.izzcj.gamemaster.mapper;

import io.github.izzcj.gamemaster.model.entity.KnowledgeFileEntity;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 知识文件 Mapper。
 */
@Mapper
public interface KnowledgeFileMapper {

    /**
     * 插入知识文件元数据。
     *
     * @param entity 文件实体
     */
    @Insert("""
        INSERT INTO knowledge_file (
            id, knowledge_base_id, file_name, content_type, storage_path, source_type, game_name,
            platform, tags, summary, status, created_at, updated_at
        ) VALUES (
            #{id}, #{knowledgeBaseId}, #{fileName}, #{contentType}, #{storagePath}, #{sourceType}, #{gameName},
            #{platform}, #{tags}, #{summary}, #{status}, #{createdAt}, #{updatedAt}
        )
        """)
    void insert(KnowledgeFileEntity entity);

    /**
     * 按主键查询文件。
     *
     * @param id 文件 ID
     * @return 文件实体
     */
    @Select("SELECT * FROM knowledge_file WHERE id = #{id}")
    KnowledgeFileEntity findById(String id);

    /**
     * 查询全部文件，按创建时间倒序。
     *
     * @return 文件列表
     */
    @Select("SELECT * FROM knowledge_file ORDER BY created_at DESC")
    List<KnowledgeFileEntity> findAll();

    /**
     * 更新文件状态与摘要。
     *
     * @param entity 文件实体
     */
    @Update("""
        UPDATE knowledge_file
        SET summary = #{summary},
            status = #{status},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    void updateStatus(KnowledgeFileEntity entity);
}
