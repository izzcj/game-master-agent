package io.github.izzcj.gamemaster.application;

import io.github.izzcj.gamemaster.model.request.ChatQueryRequest;
import io.github.izzcj.gamemaster.model.response.ChatQueryResponse;
import io.github.izzcj.gamemaster.model.response.RetrievalPlanResponse;
import io.github.izzcj.gamemaster.rag.Evidence;
import io.github.izzcj.gamemaster.rag.EvidenceFusionService;
import io.github.izzcj.gamemaster.rag.InternalKnowledgeRetriever;
import io.github.izzcj.gamemaster.rag.RetrievalPlan;
import io.github.izzcj.gamemaster.search.SearchGateway;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 问答应用服务。
 *
 * <p>负责串联游戏识别、检索计划、证据召回、证据融合、答案生成与会话持久化。
 */
@Service
public class QueryApplicationService {

    private static final Logger log = LoggerFactory.getLogger(QueryApplicationService.class);

    private final GameResolver gameResolver;
    private final RetrievalPlanner retrievalPlanner;
    private final InternalKnowledgeRetriever internalKnowledgeRetriever;
    private final SearchGateway searchGateway;
    private final EvidenceFusionService evidenceFusionService;
    private final AnswerComposerService answerComposerService;
    private final ChatSessionService chatSessionService;

    public QueryApplicationService(
        GameResolver gameResolver,
        RetrievalPlanner retrievalPlanner,
        InternalKnowledgeRetriever internalKnowledgeRetriever,
        SearchGateway searchGateway,
        EvidenceFusionService evidenceFusionService,
        AnswerComposerService answerComposerService,
        ChatSessionService chatSessionService
    ) {
        this.gameResolver = gameResolver;
        this.retrievalPlanner = retrievalPlanner;
        this.internalKnowledgeRetriever = internalKnowledgeRetriever;
        this.searchGateway = searchGateway;
        this.evidenceFusionService = evidenceFusionService;
        this.answerComposerService = answerComposerService;
        this.chatSessionService = chatSessionService;
    }

    /**
     * 执行一次问答请求。
     *
     * @param request 请求参数
     * @return 问答结果
     */
    public ChatQueryResponse query(ChatQueryRequest request) {
        String sessionId = chatSessionService.ensureSession(request.getSessionId(), request.getQuestion());
        chatSessionService.appendUserMessage(sessionId, request.getQuestion());
        ResolvedGame resolvedGame = gameResolver.resolve(request);
        RetrievalPlan plan = retrievalPlanner.plan(request, resolvedGame);
        log.info("query sessionId={}, resolvedGame={}, useExternalSearch={}, topK={}",
            sessionId, resolvedGame.gameName(), plan.useExternalSearch(), plan.topK());
        List<Evidence> internalEvidence = internalKnowledgeRetriever.retrieve(plan);
        List<Evidence> externalEvidence = plan.useExternalSearch()
            ? searchGateway.search(plan.queryText(), plan.topK())
            : Collections.emptyList();
        List<Evidence> fusedEvidence = evidenceFusionService.fuse(internalEvidence, externalEvidence, plan.topK());
        AnswerGenerationResult answer = answerComposerService.compose(request.getQuestion(), fusedEvidence);
        chatSessionService.appendAssistantMessage(sessionId, answer.answer(), answer.citations());
        return new ChatQueryResponse(
            sessionId,
            answer.answer(),
            resolvedGame.gameName(),
            new RetrievalPlanResponse(plan.queryText(), plan.useExternalSearch(), plan.topK(), plan.knowledgeBaseIds()),
            answer.citations()
        );
    }
}
