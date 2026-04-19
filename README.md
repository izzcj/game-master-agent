# Game Master Agent

一个面向游戏攻略问答场景的 Agent 服务示例项目。仓库当前包含：

- `game-master-agent`：Spring Boot 后端，负责 Agent 路由、模型调用、会话记忆、RAG 检索与 MCP 工具接入
- `game-master-web`：Vue 3 前端聊天界面
- `tavily-mcp-server`：独立的 Tavily MCP Streamable HTTP 服务，为主服务提供联网搜索工具
- `knowledge_base`：本地 Markdown 知识库样例，当前内置了《艾尔登法环》相关攻略文档

项目目标不是做一个“通用聊天壳”，而是围绕“游戏攻略问答”这一垂直场景，把本地知识库、在线搜索和多模型能力串成一条可运行、可扩展的链路。

## 简介

`game-master-agent` 基于 Spring Boot 3、Spring AI 和 Java 21 构建，当前已经实现了一条完整的攻略问答链路：

1. 前端或调用方通过 `/ai/chat` 或 `/ai/chat/stream` 发起请求
2. 后端根据 `agent` 字段路由到对应 Agent
3. Agent 选择目标 `ChatClient`，附加会话记忆、RAG 检索和 MCP 工具
4. 模型优先结合本地知识库回答，必要时通过 Tavily MCP 工具补充最新网页信息
5. 返回同步文本或流式响应

当前仓库默认聚焦一个核心 Agent：`game-walkthrough`。它的职责是回答游戏攻略、流程、玩法、Boss、Build 和关卡相关问题。

## 技术栈

- 后端：Java 21、Spring Boot 3.5、Spring AI 1.1
- 数据层：PostgreSQL、pgvector、Redis
- 前端：Vue 3、Vite、TypeScript、Tailwind CSS
- 工具协议：MCP（Model Context Protocol）
- 外部能力：DeepSeek、MiniMax、智谱 Embedding、Tavily Search

## 架构

### 总体架构

```text
+-------------+        +-------------------+        +------------------------+
|  Browser /  | -----> | game-master-web   | -----> | /ai/chat, /ai/chat/    |
| API Client  |        | Vue 3 + Vite      |        | stream                 |
+-------------+        +-------------------+        +-----------+------------+
                                                              |
                                                              v
                                            +-------------------------------+
                                            | GameMasterAgentRouter         |
                                            | 按 agent 名称/别名路由        |
                                            +---------------+---------------+
                                                            |
                                                            v
                                            +-------------------------------+
                                            | GameWalkthroughAgent          |
                                            | 系统提示词 + 工具/Advisor编排 |
                                            +---------------+---------------+
                                                            |
                  +-------------------------+---------------+-------------------------+
                  |                         |                                         |
                  v                         v                                         v
        +-------------------+   +---------------------------+           +----------------------------+
        | ChatClient        |   | ChatMemory                |           | MCP Tool Callbacks         |
        | DeepSeek / Minimax|   | Redis / 本地窗口记忆      |           | Tavily Streamable HTTP     |
        +-------------------+   +---------------------------+           +----------------------------+
                  |
                  v
        +-------------------+
        | RAG Advisor       |
        | pgvector + 本地MD |
        +-------------------+
```

### 后端核心分层

- 接口层：`ChatController` 提供同步与 SSE 流式聊天接口
- 路由层：`GameMasterAgentRouter` 负责 Agent 注册、默认 Agent 选择和别名解析
- Agent 层：`AbstractGameMasterAgent` 定义统一调用骨架，具体 Agent 决定系统提示词、Advisor 与工具
- 模型层：`GameMasterAgentAutoConfiguration` 注册多个 `ChatClient`，当前内置 `deepseek` 与 `minimax`
- 记忆层：优先使用 Redis 持久化会话消息，不可用时回退为本地窗口记忆
- RAG 层：启动时从 `knowledge_base` 读取 Markdown，做语义切分后写入 pgvector，问答时通过 `QuestionAnswerAdvisor` 检索
- 工具层：通过 MCP Client 接入 `tavily-mcp-server`，仅放行指定工具名

### RAG 链路

RAG 相关逻辑位于 `src/main/java/io/github/izzcj/gamemaster/rag`，当前流程是：

1. 读取 `gamemaster.rag.knowledge-base-path` 指向的 Markdown 文档
2. 使用 `SemanticMarkdownChunker` 按语义相关性与标题结构切分
3. 将切分结果写入 pgvector
4. 问答时基于 `top-k` 和 `similarity-threshold` 做向量召回

这条链路的目标很明确：优先回答本地已经沉淀的攻略知识，减少模型“自由发挥”。

### MCP 联网搜索链路

当问题涉及补丁变更、论坛经验、Wiki 页面更新，或用户明确要求联网检索时，`GameWalkthroughAgent` 会通过 `McpToolCallbackAdapter` 调用 Tavily MCP 工具。

仓库内的 `tavily-mcp-server` 子模块负责暴露 `search_web_for_walkthrough` 工具，默认对外提供：

- Streamable HTTP MCP 端点：`http://localhost:8091/mcp`
- Tavily 搜索封装
- 结果站点名与引用链接整理

## 特点

### 1. 场景收敛，而不是泛化过度

项目没有把自己包装成“大而全 Agent 平台”，而是聚焦游戏攻略问答。这个收敛是有价值的，因为系统提示词、RAG 数据、联网工具和前端交互都围绕同一任务设计。

### 2. 多模型接入已经具备基础骨架

后端通过 `RegisteredChatClient` + `ChatClientRegistry` + `ChatClientResolver` 管理模型客户端，当前可在请求中切换：

- `deepseek` / `ds`，默认模型
- `minimax` / `mm`

这意味着后续接入新模型时，不需要改 Agent 主流程，只需补充新的 `ChatClient` 注册。

### 3. 本地知识库优先

知识库直接使用 Markdown 文件维护，适合攻略文档这类结构相对稳定、人工可编辑的内容。当前项目已经支持：

- 启动时自动导入知识库
- 按语义分块而不是简单按字符截断
- 基于 pgvector 做向量检索

### 4. 联网能力是“补充”，不是“默认依赖”

系统提示词明确要求优先使用模型已有知识和本地知识库，只有在确实需要最新信息时才调用联网搜索工具。这种策略能减少无意义的外部请求，也更符合攻略问答的实际场景。

### 5. 前后端职责清晰

`game-master-web` 只负责聊天交互和流式展示，领域能力集中在后端。这样做有两个好处：

- 前端可以替换成任意调用方而不影响核心能力
- Agent、RAG、MCP 等复杂逻辑不会散落在页面层

## 项目结构

```text
.
├─ src/main/java/io/github/izzcj
│  ├─ GameMasterAgentApplication.java
│  └─ gamemaster
│     ├─ agent        # Agent 抽象、实现与路由
│     ├─ client       # ChatClient 注册与解析
│     ├─ config       # 自动配置
│     ├─ controller   # HTTP 接口
│     ├─ memory       # Redis / 本地记忆
│     ├─ rag          # 知识库导入、语义切分、向量检索
│     └─ tool         # MCP 工具适配
├─ src/main/resources/application.yaml
├─ knowledge_base     # 本地 Markdown 知识库
├─ game-master-web    # Vue 3 前端
└─ tavily-mcp-server  # 独立 MCP 服务
```

## 运行关系

如果只看依赖关系，可以把项目理解成三层：

- 展示层：`game-master-web`，负责聊天 UI 和流式输出
- 核心服务层：`game-master-agent`，负责 Agent 编排、模型调用、RAG 和记忆
- 外部能力层：PostgreSQL/pgvector、Redis、模型 API、Tavily MCP 服务

其中 `tavily-mcp-server` 是一个可单独替换的工具服务。即使暂时不启用它，主服务依然可以依靠本地知识库和模型完成问答，只是缺少联网补充能力。

## 快速开始

### 环境要求

- Java 21
- Maven 3.9+
- Node.js 18+
- pnpm
- PostgreSQL + pgvector
- Redis（可选，但建议启用）

### 1. 准备基础依赖

需要至少准备：

- PostgreSQL 数据库
- `pgvector` 扩展
- 可用的模型 API Key
- Tavily API Key（如果需要联网搜索）

建议不要直接使用仓库中的示例敏感配置。实际运行前，请将数据库密码、模型密钥和 Tavily 密钥改为你自己的本地配置或环境变量。

### 2. 配置主服务

主服务关键配置集中在 `src/main/resources/application.yaml`，重点包括：

- `spring.datasource.*`：PostgreSQL 连接
- `spring.ai.model.embedding`：Embedding 模型提供方
- `spring.ai.vectorstore.pgvector.*`：向量库配置
- `gamemaster.rag.*`：知识库路径、召回条数、阈值、语义切分参数
- `gamemaster.mcp.walkthrough.*`：MCP 工具开关、连接名、允许工具名

### 3. 启动 Tavily MCP 服务

```bash
cd tavily-mcp-server
mvn spring-boot:run
```

默认端口为 `8091`，主服务会通过 `http://localhost:8091/mcp` 连接它。

### 4. 启动主服务

```bash
mvn spring-boot:run
```

主服务首次启动时，如果向量表中还没有数据，会尝试从 `knowledge_base` 导入 Markdown 文档。

### 5. 启动前端

```bash
cd game-master-web
pnpm install
pnpm dev
```

前端通过 `VITE_CHAT_API_BASE_URL` 指向后端地址；未设置时默认走当前域名。

## 接口示例

### 同步聊天

```bash
curl -X POST http://localhost:8080/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "chatId": "demo-chat",
    "message": "风暴山丘城怎么打？",
    "chatClient": "deepseek",
    "agent": "game-walkthrough"
  }'
```

### 流式聊天

```bash
curl -N -X POST http://localhost:8080/ai/chat/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "chatId": "demo-chat",
    "message": "给我一个前期近战开荒路线",
    "chatClient": "deepseek",
    "agent": "game-walkthrough"
  }'
```

请求体字段说明：

- `chatId`：会话 ID，用于聊天记忆
- `message`：用户问题
- `chatClient`：使用的模型客户端
- `agent`：目标 Agent，当前建议使用 `game-walkthrough`
- `requestId`：可选，请求追踪字段

## 当前适用场景

这个项目目前更适合以下类型的场景：

- 单一垂直领域的 Agent 原型验证
- 本地知识库 + 在线搜索混合问答
- Spring AI、pgvector、MCP 集成方式的学习和实践
- 游戏攻略站、工具站或社区问答的后端雏形

如果目标是一个面向公网、高并发、多租户、强治理的生产级平台，当前仓库还只是一个不错的起点，不是终态。

## 扩展指南

### 新增一个 Agent

如果要新增一个领域 Agent，通常只需要沿着现有骨架扩展：

1. 新建一个继承 `AbstractGameMasterAgent` 的实现类
2. 定义 `name()`、`aliases()` 和系统提示词
3. 按需挂载 Advisor 或 ToolCallback
4. 交给 Spring 托管后，`GameMasterAgentRouter` 会自动发现并注册

这种方式适合继续扩展成“攻略 Agent + 配装 Agent + Lore Agent”的多角色结构。

### 新增一个模型客户端

新增模型客户端的入口在 `GameMasterAgentAutoConfiguration`。现有代码已经把“模型实例”和“调用入口”解耦，典型步骤是：

1. 提供新的 `ChatModel`
2. 构建对应 `ChatClient`
3. 使用 `@RegisteredChatClient` 声明名称、别名和默认策略

这样前端或 API 调用方就可以通过 `chatClient` 字段切换模型。

### 补充知识库

当前最直接的方式是向 `knowledge_base` 目录添加 Markdown 文档。建议文档组织尽量遵循以下原则：

- 以游戏、章节、主题分目录
- 标题层级清晰
- 一篇文档只表达一类问题
- 保留足够的步骤、地点、装备和前置条件信息

这样可以让语义切分和向量检索效果更稳定。

## 已知限制

- 当前默认 Agent 仍然比较单一，尚未形成多角色协作
- 知识库导入策略偏启动期导入，缺少后台化管理能力
- 仓库里虽然有前端与 MCP 服务，但整体还偏本地开发形态
- 测试覆盖和部署方案还不完整，距离生产化仍有明显差距

## 未来方向

### 1. 扩展更多 Agent 角色

当前只有攻略 Agent。后续可以按职责继续拆分，例如：

- Build / 配装建议 Agent
- Lore / 世界观问答 Agent
- Boss 机制分析 Agent
- 新手引导 Agent

路由和注册机制已经具备，新增 Agent 的成本不高。

### 2. 完善知识库治理

现阶段知识库导入偏向“启动即导入”。后续可以继续增强：

- 增量导入与重建策略
- 文档版本管理
- 文档来源与引用展示
- 更细粒度的检索评估与调参

### 3. 提升工具编排能力

现在 MCP 工具主要承担联网搜索。未来可以扩展成更完整的工具链，例如：

- Wiki / 社区站点专用检索
- 攻略结构化提取
- 多来源结果融合与可信度排序

### 4. 加强工程化能力

当前核心能力已经可跑，但工程化仍有明显提升空间：

- 单元测试与集成测试补齐
- Docker / Compose 部署方案
- 配置脱敏与环境隔离
- 监控、限流、鉴权与审计

### 5. 前端从演示走向产品化

`game-master-web` 当前已经具备聊天体验，但还可以继续演进：

- 引用来源展示
- Agent / 模型切换界面
- 会话管理与历史记录
- 攻略卡片、步骤流和结构化答案渲染

## License

本项目基于 [Apache License 2.0](LICENSE) 开源协议。
