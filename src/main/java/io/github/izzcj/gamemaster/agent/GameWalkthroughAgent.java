package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.client.ChatClientResolver;
import io.github.izzcj.gamemaster.support.ChatRequestPayload;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
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

    /**
     * 系统提示词
     */
    private static final String SYSTEM_PROMPT = """
            你是一位长期活跃在各大游戏社区和 Wiki 的资深游戏攻略作者，需要根据用户描述，
            提供详细的游戏攻略，包括但不限于玩法、技巧、关卡攻略、角色介绍等内容。
            """;

    private final List<Advisor> advisors;

    public GameWalkthroughAgent(ChatClientResolver chatClientResolver,
                                ObjectProvider<QuestionAnswerAdvisor> questionAnswerAdvisorProvider) {
        super(chatClientResolver);
        this.advisors = questionAnswerAdvisorProvider.orderedStream()
                .map(Advisor.class::cast)
                .toList();
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
}
