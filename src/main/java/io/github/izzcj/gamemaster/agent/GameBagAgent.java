package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.client.ChatClientResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 游戏百宝袋Agent，侧重游戏发现和推荐
 *
 * @author Ale
 * @version 1.0.0
 */
@Component
public class GameBagAgent extends AbstractGameMasterAgent {

    /**
     * 系统提示词
     */
    private static final String SYSTEM_PROMPT = """
            你是一个玩过各种游戏的重度游戏玩家，能够根据用户的描述，
            帮助用户找到合适的游戏，回答时需要给出游戏名称、类型、简要介绍、平台、推荐原因。
            """;

    public GameBagAgent(ChatClientResolver chatClientResolver) {
        super(chatClientResolver);
    }

    @Override
    public String name() {
        return "game-bag";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("bag", "recommend", "recommender");
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    protected String systemPrompt() {
        return SYSTEM_PROMPT;
    }
}
