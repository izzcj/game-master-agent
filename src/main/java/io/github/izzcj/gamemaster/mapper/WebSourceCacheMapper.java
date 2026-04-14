package io.github.izzcj.gamemaster.mapper;

import io.github.izzcj.gamemaster.model.entity.WebSourceCacheEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 外部搜索缓存 Mapper。
 */
@Mapper
public interface WebSourceCacheMapper {

    /**
     * 查询全部缓存记录。
     *
     * @return 缓存记录列表
     */
    @Select("SELECT * FROM web_source_cache ORDER BY updated_at DESC")
    List<WebSourceCacheEntity> findAll();
}
