# Game Master Agent Walkthrough Architecture Plan

## 1. Background

The current project has completed a usable prototype:

- Spring Boot backend with model registration and chat endpoints
- Vue frontend with streaming chat UI
- Redis-backed conversation memory
- Multiple prompt-based agents for different chat roles

This is enough to validate the basic chat loop, but it is not yet a strong architecture for the real product goal:

> Generate reliable, detailed, and actionable game walkthroughs based on user needs.

The main issue is that the current "agent" design is still prompt-routing oriented. It can switch system prompts, but it does not yet form a complete walkthrough production pipeline.

## 2. Current Problems

### 2.1 Agent responsibilities are too thin

The current `GameBagAgent` and `GameWalkthroughAgent` differ mainly in system prompt. Their execution path is still:

`select model -> inject system prompt -> send user message -> return response`

This is suitable for a chat prototype, but not for walkthrough generation that depends on:

- game identification
- user goal understanding
- progress recognition
- version/platform differentiation
- knowledge retrieval
- structured answer generation
- answer validation

### 2.2 Product direction is not fully aligned

If the product goal is walkthrough generation for a specific game, then `GameBag` no longer fits the core path. Game recommendation is a different product capability and should not remain the default entry.

### 2.3 Knowledge layer is missing

The project description already mentions RAG, but the current implementation does not yet include a real retrieval loop:

- no knowledge source abstraction
- no document ingestion
- no retrieval service
- no evidence assembly
- no source/version filtering

Without a knowledge layer, the system is only a prompt-based generator and will be unreliable for detailed攻略.

### 2.4 Conversation isolation is insufficient

The frontend currently uses a fixed `chatId`, which will mix different sessions into the same conversation memory.

This is acceptable in a temporary local demo, but not for a real walkthrough assistant.

### 2.5 Redis memory grows without control

The current Redis memory appends the full message history and does not enforce:

- message window limits
- summary compaction
- TTL
- long-term vs short-term separation

This will eventually degrade answer quality and increase token cost.

### 2.6 User request model is too thin for walkthrough business

The current `ChatRequestPayload` is enough for generic chat, but not enough for walkthrough-specific flow control.

At the same time, expanding the request model must not force users to fill large forms. The system must carry more structure internally while keeping the user interaction simple.

## 3. Target Product Definition

After removing `GameBag`, the product should focus on a single main capability:

> For a specific game, combine user goal, current progress, platform, version, and relevant knowledge to generate a trustworthy and structured walkthrough.

This implies:

- one core walkthrough pipeline
- fewer prompt-agents
- more business orchestration
- stronger retrieval and context handling

## 4. Target Architecture

The recommended architecture has 5 layers.

### 4.1 Interface Layer

Responsibilities:

- receive HTTP/SSE requests
- validate basic request shape
- manage request/session identifiers
- map exceptions to stable API responses

Recommended components:

- `ChatController`
- `GlobalExceptionHandler`

This layer should stay thin and should not directly decide walkthrough logic.

### 4.2 Orchestration Layer

This becomes the main business entry instead of prompt-based agent routing.

Responsibilities:

- parse request
- resolve game context
- detect walkthrough intent
- decide whether more clarification is needed
- assemble retrieval query
- retrieve evidence
- generate final answer
- validate answer completeness

Recommended core component:

- `WalkthroughOrchestrator`

Suggested pipeline:

`request -> context resolve -> intent analyze -> retrieval -> generate -> validate -> stream/return`

### 4.3 Domain Service Layer

This layer contains walkthrough-specific business capabilities.

Recommended services:

- `GameResolver`
  - identify game name, platform, version, DLC, class, chapter, boss, region
- `IntentAnalyzer`
  - identify whether the user needs mainline, side quest, boss, build, collection, farming, or progression help
- `WalkthroughPlanner`
  - decide the answer structure and retrieval focus
- `WalkthroughGenerator`
  - generate the final walkthrough from plan and evidence
- `AnswerValidator`
  - check missing prerequisites, conflicting statements, version mismatch, and unsupported claims

This layer should reflect business semantics, not model vendor semantics.

### 4.4 Foundation Layer

This layer keeps general model and runtime infrastructure.

Responsibilities:

- register chat clients
- resolve default model
- attach logging and memory advisors
- provide reusable prompt templates

Existing code that can remain in this layer:

- `ChatClientRegistry`
- `ChatClientResolver`
- `MemoryChatClientRegistry`
- `DefaultChatClientResolver`
- `GameMasterAgentAutoConfiguration`

This layer should support business orchestration but should not define business flow.

### 4.5 Knowledge Layer

This is the key addition for walkthrough quality.

Responsibilities:

- define knowledge sources
- ingest guide documents
- retrieve evidence for a specific game/version/task
- preserve source identity
- support future ranking/filtering

Recommended components:

- `KnowledgeSource`
- `KnowledgeDocument`
- `KnowledgeRetriever`
- `RetrievedEvidence`

Initial implementation can start from local markdown/json guide files before moving to a full vector database or external wiki integration.

## 5. Suggested Package Structure

Recommended backend package split:

```text
io.github.izzcj.gamemaster
├─ controller
│  ├─ ChatController
│  └─ GlobalExceptionHandler
├─ application
│  ├─ WalkthroughApplicationService
│  └─ WalkthroughStreamApplicationService
├─ orchestrator
│  └─ WalkthroughOrchestrator
├─ domain
│  ├─ game
│  │  ├─ GameContext
│  │  ├─ GameResolver
│  │  └─ GameVersion
│  ├─ intent
│  │  ├─ WalkthroughIntent
│  │  └─ IntentAnalyzer
│  └─ walkthrough
│     ├─ WalkthroughPlan
│     ├─ WalkthroughPlanner
│     ├─ WalkthroughGenerator
│     └─ AnswerValidator
├─ knowledge
│  ├─ KnowledgeSource
│  ├─ KnowledgeDocument
│  ├─ KnowledgeRetriever
│  └─ RetrievedEvidence
├─ memory
│  ├─ ConversationMemoryService
│  └─ RedisChatMemory
├─ llm
│  ├─ ChatClientRegistry
│  ├─ ChatClientResolver
│  └─ PromptTemplateFactory
├─ config
└─ support
```

## 6. What to Keep, Remove, and Refactor

### 6.1 Keep

The following parts still fit the target architecture:

- model registration and resolution infrastructure
- logging advisor
- exception mapping
- Redis-based memory implementation idea
- frontend streaming chat interaction

Representative existing classes:

- `GameMasterAgentAutoConfiguration`
- `MemoryChatClientRegistry`
- `DefaultChatClientResolver`
- `GlobalExceptionHandler`
- `RedisChatMemory`

### 6.2 Remove

The following should be removed because they no longer match the product direction:

- `GameBagAgent`

Reason:

- it solves a different problem: game discovery/recommendation
- it should not remain the default path for a walkthrough product

### 6.3 Refactor

The following should be transformed rather than kept as-is:

- `GameWalkthroughAgent`
  - refactor into a walkthrough generation capability, not a standalone prompt-agent
- `AbstractGameMasterAgent`
  - replace with orchestration-based business flow
- `GameMasterAgentRouter`
  - replace with a walkthrough orchestrator entry

The key shift is:

`prompt-routing architecture -> walkthrough pipeline architecture`

## 7. Request Model Design

## 7.1 Core Principle

The internal request model can become richer, but the user input must remain simple.

This means:

- the user should still be able to type one natural-language message
- the system should infer most structured fields automatically
- only missing critical context should trigger follow-up questions

### 7.2 Why not directly enlarge the visible form

If the frontend forces the user to fill:

- game name
- platform
- version
- progress
- intent type
- spoiler level

before every message, the mental burden becomes too high.

That would reduce usability and weaken the conversational nature of the product.

### 7.3 Recommended split: external request vs internal resolved request

Instead of exposing a large payload directly to the user, split the model into two stages.

#### External request

```java
public class UserChatRequest {
    private String chatId;
    private String message;
    private String chatClient;
}
```

#### Internal resolved request

```java
public class ResolvedWalkthroughRequest {
    private String chatId;
    private String message;
    private String gameName;
    private String platform;
    private String gameVersion;
    private String progressStage;
    private String intentType;
    private String spoilerLevel;
    private String responseMode;
    private String chatClient;
}
```

This design keeps the user interaction light while allowing the system to operate with richer semantics.

### 7.4 Field classification

#### User explicitly inputs

- `message`

#### System automatically infers

- `gameName`
- `intentType`
- `progressStage`
- `targetBoss` or `targetTask`
- initial `spoilerLevel`

#### Ask only when needed

- `platform`
- `gameVersion`
- `buildStyle`
- `responseMode`

## 8. Frontend Interaction Design

After removing `GameBag`, the frontend should no longer ask the user to choose an agent.

The UI should shift from "select agent" to "supplement walkthrough context".

Recommended interaction model:

- default entry is still a single input box
- the system extracts game/context tags automatically
- recognized tags are shown above the chat input or answer area
- the user can correct them if recognition is wrong
- only when critical information is missing should the assistant ask follow-up questions

Recommended optional UI controls:

- game name tag
- progress stage tag
- walkthrough mode tag
  - mainline
  - side quest
  - boss
  - build
  - collection
- spoiler level

Do not turn the main experience into a large form.

## 9. Memory Redesign

The current memory model should evolve into two layers.

### 9.1 Short-term memory

Used for recent dialogue continuity.

Suggested characteristics:

- keep only the latest N rounds
- support chat streaming
- scoped by a true per-session `chatId`

### 9.2 Long-term summary memory

Used for stable user/game context.

Examples:

- current game: Elden Ring
- current issue: stuck on a specific boss
- play style: melee
- spoiler preference: low spoiler

This reduces token waste and avoids replaying the full raw history on every turn.

### 9.3 Required changes

- stop using a fixed frontend `chatId`
- add expiration or lifecycle management in Redis
- support compaction/summarization
- define memory boundaries per user/session

## 10. Recommended Request Flow

The walkthrough request should follow this order:

1. Receive user message and session id
2. Load recent memory and summarized context
3. Resolve game context from message and memory
4. Analyze walkthrough intent
5. Ask a follow-up question if critical context is missing
6. Build retrieval query
7. Retrieve evidence from knowledge sources
8. Generate structured walkthrough answer
9. Validate completeness and consistency
10. Return or stream the final answer

This is the core replacement for the current direct prompt call.

## 11. Prompt Strategy

Do not continue expanding the system by adding more prompt-agent classes such as:

- `BossAgent`
- `BuildAgent`
- `QuestAgent`

That path will create prompt sprawl and weak business control.

Instead, use a small set of phase-based prompts:

- `intent-analyze`
- `game-resolve`
- `retrieval-query`
- `walkthrough-generate`
- `answer-validate`

Prompt templates should map to pipeline stages, not to agent names.

## 12. Phased Refactor Plan

### Phase 1

Goal:

- remove `GameBag`
- keep one walkthrough-oriented entry path
- remove agent selection from the frontend

Expected result:

- the product direction is unified

### Phase 2

Goal:

- introduce `WalkthroughOrchestrator`
- move direct model invocation behind orchestration

Expected result:

- business flow starts to become explicit

### Phase 3

Goal:

- add initial knowledge retrieval support
- start from local guide files if necessary

Expected result:

- answers rely less on model memory and more on controlled evidence

### Phase 4

Goal:

- add memory summarization
- add answer validation
- add evidence/source attribution

Expected result:

- improved consistency, lower token cost, and stronger trustworthiness

## 13. Final Recommendation

The next step for this project should not be adding more agents.

The correct direction is:

> remove agent-name-centered business design, converge to a single walkthrough orchestration pipeline, and progressively add retrieval, memory compaction, and answer validation.

In short:

- keep the infrastructure
- remove `GameBag`
- shrink prompt-routing
- build a real walkthrough workflow

This path is more aligned with the actual product goal and will scale better than continuing the current agent split.
