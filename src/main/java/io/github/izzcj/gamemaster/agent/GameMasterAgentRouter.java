package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.exception.AgentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Locale;
import java.util.Set;

/**
 * 游戏大师Agent路由
 *
 * @author Ale
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GameMasterAgentRouter {

    private final GameBagAgent gameBagAgent;
    private final GameWalkthroughAgent gameWalkthroughAgent;

    /**
     * 聊天
     *
     * @param agentName      Agent名称
     * @param chatClientName ChatClient名称
     * @param prompt         提示词
     * @return 回复内容
     */
    public String chat(String agentName, String chatClientName, String prompt) {
        return selectTarget(agentName).chat(this, chatClientName, prompt);
    }

    /**
     * 聊天流式响应
     *
     * @param agentName      Agent名称
     * @param chatClientName ChatClient名称
     * @param prompt         提示词
     * @return 流式回复内容
     */
    public Flux<String> chatStream(String agentName, String chatClientName, String prompt) {
        return selectTarget(agentName).chatStream(this, chatClientName, prompt);
    }

    /**
     * 根据Agent名称选择目标Agent
     *
     * @param agentName Agent名称
     * @return 目标Agent
     */
    private RoutedAgent selectTarget(String agentName) {
        if (!StringUtils.hasText(agentName)) {
            return RoutedAgent.GAME_BAG;
        }

        String normalizedAgentName = normalize(agentName);
        for (RoutedAgent candidate : RoutedAgent.values()) {
            if (candidate.matches(normalizedAgentName)) {
                return candidate;
            }
        }

        throw new AgentNotFoundException("Unsupported agent: " + agentName);
    }

    /**
     * 规范化Agent名称
     *
     * @param agentName Agent名称
     * @return 规范化后的Agent名称
     */
    private String normalize(String agentName) {
        return agentName.trim()
                .toLowerCase(Locale.ROOT)
                .replace('_', '-')
                .replace(' ', '-');
    }

    /**
     * Agent路由枚举
     */
    private enum RoutedAgent {
        GAME_BAG(Set.of("game-bag", "bag", "recommend", "recommender")) {
            @Override
            String chat(GameMasterAgentRouter router, String chatClientName, String prompt) {
                return router.gameBagAgent.chat(chatClientName, prompt);
            }

            @Override
            Flux<String> chatStream(GameMasterAgentRouter router, String chatClientName, String prompt) {
                return router.gameBagAgent.chatStream(chatClientName, prompt);
            }
        },
        GAME_WALKTHROUGH(Set.of("game-walkthrough", "walkthrough", "guide", "strategy")) {
            @Override
            String chat(GameMasterAgentRouter router, String chatClientName, String prompt) {
                return router.gameWalkthroughAgent.chat(chatClientName, prompt);
            }

            @Override
            Flux<String> chatStream(GameMasterAgentRouter router, String chatClientName, String prompt) {
                return router.gameWalkthroughAgent.chatStream(chatClientName, prompt);
            }
        };

        private final Set<String> aliases;

        RoutedAgent(Set<String> aliases) {
            this.aliases = aliases;
        }

        boolean matches(String agentName) {
            return this.aliases.contains(agentName);
        }

        abstract String chat(GameMasterAgentRouter router, String chatClientName, String prompt);

        abstract Flux<String> chatStream(GameMasterAgentRouter router, String chatClientName, String prompt);
    }
}
