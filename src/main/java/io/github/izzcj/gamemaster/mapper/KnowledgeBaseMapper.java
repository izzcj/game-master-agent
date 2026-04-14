package io.github.izzcj.gamemaster.mapper;

import io.github.izzcj.gamemaster.model.entity.KnowledgeBaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 知识库 Mapper。
 */
@Mapper
public interface KnowledgeBaseMapper {

    /**
     * 按主键查询知识库。
     *
     * @param id 知识库 ID
     * @return 知识库实体
     */
    @Select("SELECT * FROM knowledge_base WHERE id = #{id}")
    KnowledgeBaseEntity findById(String id);
}
