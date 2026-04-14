package io.github.izzcj.gamemaster.application;

import io.github.izzcj.gamemaster.mapper.GameCatalogMapper;
import io.github.izzcj.gamemaster.model.entity.GameCatalogEntity;
import io.github.izzcj.gamemaster.model.request.ChatQueryRequest;
import io.github.izzcj.gamemaster.rag.KeywordUtils;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 游戏解析服务。
 *
 * <p>根据请求里的显式游戏名或问题文本，从游戏目录中推断目标游戏。
 */
@Service
public class GameResolver {

    private final GameCatalogMapper gameCatalogMapper;

    public GameResolver(GameCatalogMapper gameCatalogMapper) {
        this.gameCatalogMapper = gameCatalogMapper;
    }

    /**
     * 解析当前请求命中的游戏。
     *
     * @param request 问答请求
     * @return 解析结果
     */
    public ResolvedGame resolve(ChatQueryRequest request) {
        if (request.getGameName() != null && !request.getGameName().isBlank()) {
            return new ResolvedGame(null, request.getGameName(), request.getPlatform(), 1.0);
        }
        String question = request.getQuestion();
        List<GameCatalogEntity> candidates = gameCatalogMapper.findAll();
        return candidates.stream()
            .map(entity -> match(question, request.getPlatform(), entity))
            .max(Comparator.comparingDouble(ResolvedGame::score))
            .filter(game -> game.score() > 0)
            .orElseGet(() -> new ResolvedGame(null, null, request.getPlatform(), 0));
    }

    private ResolvedGame match(String question, String requestedPlatform, GameCatalogEntity entity) {
        String haystack = entity.getName() + " " + entity.getAliases() + " " + entity.getTags();
        double score = KeywordUtils.overlapScore(question, haystack);
        if (requestedPlatform != null && entity.getPlatforms() != null
            && entity.getPlatforms().toLowerCase().contains(requestedPlatform.toLowerCase())) {
            score += 0.5;
        }
        return new ResolvedGame(entity.getId(), entity.getName(), requestedPlatform, score);
    }
}
