package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.client.ChatClientResolver;
import io.github.izzcj.gamemaster.support.ChatRequestPayload;
import io.github.izzcj.gamemaster.tool.McpToolCallbackAdapter;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 游戏攻略Agent，侧重攻略、玩法和关卡说明
 *
 * @author Ale
 * @version 1.0.0
 */
@Component
public class GameWalkthroughAgent extends AbstractGameMasterAgent {

    private static final String SYSTEM_PROMPT = """
            你是一位长期活跃在各大游戏社区、论坛和 Wiki 的资深游戏攻略作者，需要根据用户描述，
            提供详细的游戏攻略，包括但不限于玩法、技巧、关卡攻略、角色介绍等内容。
            你应优先使用自身知识和本地知识库回答问题。
            只有在问题涉及最新补丁变化、论坛经验、Wiki 页面，或用户明确要求检索网页时，才调用网页搜索工具。
            如果调用了网页搜索工具，请优先给出来源站点名称和链接。
            如果搜索失败或没有可靠结果，明确说明未检索到可靠网页来源，不要编造已查到的网页内容。
            """;

    private final List<Advisor> advisors;
    private final McpToolCallbackAdapter mcpToolCallbackAdapter;

    public GameWalkthroughAgent(ChatClientResolver chatClientResolver,
                                ObjectProvider<QuestionAnswerAdvisor> questionAnswerAdvisorProvider,
                                McpToolCallbackAdapter mcpToolCallbackAdapter) {
        super(chatClientResolver);
        this.advisors = questionAnswerAdvisorProvider.orderedStream()
                .map(Advisor.class::cast)
                .toList();
        this.mcpToolCallbackAdapter = mcpToolCallbackAdapter;
    }

    @Override
    public String name() {
        return "game-walkthrough";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("walkthrough", "guide", "strategy");
    }

    @Override
    protected String systemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    protected List<Advisor> advisors(ChatRequestPayload payload) {
        return this.advisors;
    }

    @Override
    protected ToolCallback[] toolCallbacks(ChatRequestPayload payload) {
        return this.mcpToolCallbackAdapter.toolCallbacks(true);
    }
}
