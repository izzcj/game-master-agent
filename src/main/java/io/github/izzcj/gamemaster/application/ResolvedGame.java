package io.github.izzcj.gamemaster.application;

/**
 * 游戏识别结果。
 *
 * @param gameId 命中的游戏 ID
 * @param gameName 命中的游戏名
 * @param platform 平台
 * @param score 匹配得分
 */
public record ResolvedGame(
    String gameId,
    String gameName,
    String platform,
    double score
) {
}
