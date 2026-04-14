package io.github.izzcj.gamemaster.ai;

/**
 * 模型调用场景。
 *
 * <p>用于将业务语义映射到具体的模型绑定配置。
 */
public enum ModelScene {
    QUERY_REWRITE("query-rewrite"),
    GAME_RESOLVE("game-resolve"),
    ANSWER_SUMMARY("answer-summary"),
    FINAL_ANSWER("final-answer"),
    DOC_TAG_EXTRACT("doc-tag-extract");

    private final String key;

    ModelScene(String key) {
        this.key = key;
    }

    /**
     * 返回配置文件中的路由键。
     *
     * @return 路由键
     */
    public String getKey() {
        return key;
    }
}
