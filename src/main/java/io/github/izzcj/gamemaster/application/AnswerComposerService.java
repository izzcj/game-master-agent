package io.github.izzcj.gamemaster.application;

import io.github.izzcj.gamemaster.ai.AgentModelProperties;
import io.github.izzcj.gamemaster.ai.ModelRouter;
import io.github.izzcj.gamemaster.ai.ModelScene;
import io.github.izzcj.gamemaster.model.response.CitationResponse;
import io.github.izzcj.gamemaster.rag.Evidence;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * 答案生成服务。
 *
 * <p>优先调用配置好的大模型；当模型 Bean 不可用时退化为基于证据的文本拼接。
 */
@Service
public class AnswerComposerService {

    private final ModelRouter modelRouter;
    private final ApplicationContext applicationContext;
    private final PromptTemplateService promptTemplateService;

    public AnswerComposerService(
        ModelRouter modelRouter,
        ApplicationContext applicationContext,
        PromptTemplateService promptTemplateService
    ) {
        this.modelRouter = modelRouter;
        this.applicationContext = applicationContext;
        this.promptTemplateService = promptTemplateService;
    }

    /**
     * 根据证据生成最终回答。
     *
     * @param question 用户问题
     * @param evidenceList 证据列表
     * @return 回答与引用
     */
    public AnswerGenerationResult compose(String question, List<Evidence> evidenceList) {
        List<CitationResponse> citations = evidenceList.stream()
            .map(evidence -> new CitationResponse(
                evidence.sourceType(),
                evidence.title(),
                evidence.locator(),
                evidence.url(),
                evidence.content()
            ))
            .toList();
        if (evidenceList.isEmpty()) {
            return new AnswerGenerationResult("I do not have enough grounded evidence to answer this question yet.", citations);
        }

        AgentModelProperties.ModelBinding binding = modelRouter.selectChatModel(ModelScene.FINAL_ANSWER);
        if (binding.getBeanName() != null && applicationContext.containsBean(binding.getBeanName())) {
            ChatModel chatModel = applicationContext.getBean(binding.getBeanName(), ChatModel.class);
            String prompt = promptTemplateService.render("answer-with-citations", Map.of(
                "question", question,
                "evidence", renderEvidence(evidenceList)
            ));
            String content = ChatClient.builder(chatModel).build()
                .prompt()
                .user(prompt)
                .call()
                .content();
            return new AnswerGenerationResult(content, citations);
        }
        return new AnswerGenerationResult(fallbackAnswer(question, evidenceList), citations);
    }

    /**
     * 在无可用模型时生成兜底回答。
     *
     * @param question 用户问题
     * @param evidenceList 证据列表
     * @return 兜底答案
     */
    private String fallbackAnswer(String question, List<Evidence> evidenceList) {
        String facts = evidenceList.stream()
            .limit(3)
            .map(evidence -> "[" + evidence.sourceType() + "] " + evidence.title() + ": " + evidence.content())
            .collect(Collectors.joining("\n"));
        return "Grounded answer for: " + question + "\n" + facts
            + "\nIf you need a more polished answer, configure a chat model provider.";
    }

    /**
     * 将证据渲染为适合注入 Prompt 的纯文本。
     *
     * @param evidenceList 证据列表
     * @return 证据文本
     */
    private String renderEvidence(List<Evidence> evidenceList) {
        return evidenceList.stream()
            .map(evidence -> evidence.sourceType() + " | " + evidence.title() + " | " + evidence.content())
            .collect(Collectors.joining("\n"));
    }
}
