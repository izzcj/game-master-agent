package io.github.izzcj.gamemaster.mapper;

import io.github.izzcj.gamemaster.model.entity.GameCatalogEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 游戏目录 Mapper。
 */
@Mapper
public interface GameCatalogMapper {

    /**
     * 查询全部游戏目录数据。
     *
     * @return 游戏列表
     */
    @Select("SELECT * FROM game_catalog ORDER BY updated_at DESC")
    List<GameCatalogEntity> findAll();
}
