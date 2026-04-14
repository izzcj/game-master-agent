package io.github.izzcj.gamemaster.mapper;

import io.github.izzcj.gamemaster.model.entity.KnowledgeChunkSnapshotEntity;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文档分块快照 Mapper。
 */
@Mapper
public interface KnowledgeChunkSnapshotMapper {

    /**
     * 删除指定文件已有的全部 chunk 快照。
     *
     * @param fileId 文件 ID
     */
    @Delete("DELETE FROM knowledge_chunk_snapshot WHERE file_id = #{fileId}")
    void deleteByFileId(String fileId);

    /**
     * 批量插入 chunk 快照。
     *
     * @param chunks 快照列表
     */
    @Insert("""
        <script>
        INSERT INTO knowledge_chunk_snapshot (
            id, file_id, knowledge_base_id, chunk_index, title, game_name, platform, language, tags,
            source_url, content, metadata_json, created_at
        ) VALUES
        <foreach collection="chunks" item="item" separator=",">
            (
              #{item.id}, #{item.fileId}, #{item.knowledgeBaseId}, #{item.chunkIndex}, #{item.title}, #{item.gameName},
              #{item.platform}, #{item.language}, #{item.tags}, #{item.sourceUrl}, #{item.content},
              #{item.metadataJson}, #{item.createdAt}
            )
        </foreach>
        </script>
    """)
    void insertBatch(@Param("chunks") List<KnowledgeChunkSnapshotEntity> chunks);

    /**
     * 根据知识库范围和游戏名查询候选 chunk。
     *
     * @param knowledgeBaseIds 知识库 ID 列表
     * @param gameName 游戏名
     * @return 候选快照列表
     */
    @Select("""
        <script>
        SELECT * FROM knowledge_chunk_snapshot
        <where>
            <if test="knowledgeBaseIds != null and knowledgeBaseIds.size() > 0">
                knowledge_base_id IN
                <foreach collection="knowledgeBaseIds" item="item" open="(" separator="," close=")">
                    #{item}
                </foreach>
            </if>
            <if test="gameName != null and gameName != ''">
                AND LOWER(COALESCE(game_name, '')) LIKE CONCAT('%', LOWER(#{gameName}), '%')
            </if>
        </where>
        ORDER BY created_at DESC
        </script>
        """)
    List<KnowledgeChunkSnapshotEntity> findCandidates(
        @Param("knowledgeBaseIds") List<String> knowledgeBaseIds,
        @Param("gameName") String gameName
    );
}
