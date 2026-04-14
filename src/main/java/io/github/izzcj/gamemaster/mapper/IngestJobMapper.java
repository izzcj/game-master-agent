package io.github.izzcj.gamemaster.mapper;

import io.github.izzcj.gamemaster.model.entity.IngestJobEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 摄取任务 Mapper。
 */
@Mapper
public interface IngestJobMapper {

    /**
     * 新增摄取任务。
     *
     * @param entity 任务实体
     */
    @Insert("""
        INSERT INTO ingest_job (
            id, file_id, status, error_message, chunk_count, started_at, finished_at, created_at, updated_at
        ) VALUES (
            #{id}, #{fileId}, #{status}, #{errorMessage}, #{chunkCount}, #{startedAt}, #{finishedAt}, #{createdAt}, #{updatedAt}
        )
        """)
    void insert(IngestJobEntity entity);

    /**
     * 按主键查询任务。
     *
     * @param id 任务 ID
     * @return 任务实体
     */
    @Select("SELECT * FROM ingest_job WHERE id = #{id}")
    IngestJobEntity findById(String id);

    /**
     * 更新任务状态与统计信息。
     *
     * @param entity 任务实体
     */
    @Update("""
        UPDATE ingest_job
        SET status = #{status},
            error_message = #{errorMessage},
            chunk_count = #{chunkCount},
            started_at = #{startedAt},
            finished_at = #{finishedAt},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    void update(IngestJobEntity entity);
}
