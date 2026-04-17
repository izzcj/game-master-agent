package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.exception.AgentNotFoundException;
import io.github.izzcj.gamemaster.exception.DuplicateAgentException;
import io.github.izzcj.gamemaster.exception.InvalidAgentConfigurationException;
import io.github.izzcj.gamemaster.support.ChatRequestPayload;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 游戏大师Agent路由器
 *
 * @author Ale
 * @version 1.0.0
 */
@Component
public class GameMasterAgentRouter {

    /**
     * 游戏大师Agent映射
     */
    private final Map<String, GameMasterAgent> agentsMap = new LinkedHashMap<>();

    /**
     * Agent别名映射
     */
    private final Map<String, String> aliases = new LinkedHashMap<>();

    /**
     * 默认Agent名称
     */
    private final String defaultAgentName;

    public GameMasterAgentRouter(List<GameMasterAgent> agents) {
        String resolvedDefaultAgentName = null;
        for (GameMasterAgent agent : agents) {
            validateAgent(agent);

            String normalizedName = this.normalize(agent.name());
            if (this.agentsMap.containsKey(normalizedName)) {
                throw new DuplicateAgentException(
                        "Agent name '%s' is already registered.".formatted(agent.name()));
            }

            this.ensureAliasesAvailable(agent.name(), agent.aliases());
            if (agent.isDefault()) {
                if (resolvedDefaultAgentName != null) {
                    throw new InvalidAgentConfigurationException(
                            "Multiple default agents configured. Existing='%s', new='%s'."
                                    .formatted(resolvedDefaultAgentName, agent.name()));
                }
                resolvedDefaultAgentName = normalizedName;
            }

            this.agentsMap.put(normalizedName, agent);
            for (String alias : agent.aliases()) {
                this.aliases.put(this.normalize(alias), normalizedName);
            }
        }

        if (this.agentsMap.isEmpty()) {
            throw new InvalidAgentConfigurationException("At least one game master agent must be configured.");
        }

        this.defaultAgentName = resolvedDefaultAgentName != null
                ? resolvedDefaultAgentName
                : this.agentsMap.keySet().iterator().next();
    }

    /**
     * 聊天
     *
     * @param payload 请求载体
     * @return 回复内容
     */
    public String chat(ChatRequestPayload payload) {
        return this.selectAgent(payload.getAgent()).chat(payload);
    }

    /**
     * 聊天流式响应
     *
     * @param payload 请求载体
     * @return 流式回复内容
     */
    public Flux<String> chatStream(ChatRequestPayload payload) {
        return this.selectAgent(payload.getAgent()).chatStream(payload);
    }

    /**
     * 选择目标Agent
     *
     * @param agentName Agent名称
     * @return 目标Agent
     */
    private GameMasterAgent selectAgent(String agentName) {
        if (!StringUtils.hasText(agentName)) {
            return this.agentsMap.get(this.defaultAgentName);
        }

        String normalizedAgentName = normalize(agentName);
        GameMasterAgent directMatch = this.agentsMap.get(normalizedAgentName);
        if (directMatch != null) {
            return directMatch;
        }

        String canonicalName = this.aliases.get(normalizedAgentName);
        if (canonicalName != null) {
            return this.agentsMap.get(canonicalName);
        }

        throw new AgentNotFoundException("Unsupported agent: " + agentName);
    }

    /**
     * 验证Agent配置
     *
     * @param agent Agent实例
     */
    private void validateAgent(GameMasterAgent agent) {
        if (agent == null) {
            throw new InvalidAgentConfigurationException("Game master agent must not be null.");
        }
        if (!StringUtils.hasText(agent.name())) {
            throw new InvalidAgentConfigurationException("Game master agent name must not be blank.");
        }
    }

    /**
     * 确保别名可用
     *
     * @param agentName Agent名称
     * @param aliases   Agent别名集合
     */
    private void ensureAliasesAvailable(String agentName, Set<String> aliases) {
        for (String alias : aliases) {
            if (!StringUtils.hasText(alias)) {
                throw new InvalidAgentConfigurationException(
                        "Blank alias is not allowed for agent '%s'.".formatted(agentName));
            }

            String normalizedAlias = normalize(alias);
            if (this.agentsMap.containsKey(normalizedAlias)) {
                throw new DuplicateAgentException(
                        "Alias '%s' conflicts with an existing agent name.".formatted(alias));
            }
            if (this.aliases.containsKey(normalizedAlias)) {
                throw new DuplicateAgentException("Alias '%s' is already registered.".formatted(alias));
            }
        }
    }

    /**
     * 标准化Agent名称或别名
     *
     * @param agentName Agent名称或别名
     * @return 标准化后的Agent名称或别名
     */
    private String normalize(String agentName) {
        return agentName.trim()
                .toLowerCase(Locale.ROOT)
                .replace('_', '-')
                .replace(' ', '-');
    }
}
