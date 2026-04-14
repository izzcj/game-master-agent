# Game Master Agent 技术选型与架构设计

## 1. 目标与边界

这个项目的目标不是做一个通用聊天机器人，而是做一个面向游戏问答/攻略聚合的 Agent 服务，核心能力如下：

- 根据用户输入识别目标游戏、玩法主题、平台和问题类型。
- 同时检索外部游戏资料与内部知识库文档。
- 对检索结果去重、聚合、总结，输出带来源依据的答案。
- 支持文档上传，经过解析、切分、向量化后进入知识库。
- 支持多模型配置，并能按场景做路由和降级。
- 基于 `Spring AI` 实现，不采用 DDD。

本方案采用“分层 + 模块化”架构，而不是 DDD 聚合/领域对象建模。原因很直接：这个项目的复杂度主要在 AI 编排、检索链路、数据摄取和多模型路由，不在复杂领域建模上。用 DDD 只会增加样板代码和理解成本。

## 2. 推荐技术选型

### 2.1 后端基础

- `Java 21`
- `Spring Boot 3.5.x`
- `Spring AI 1.1.x`
- `Spring MVC`
- `MyBatis`
- `Flyway`
- `lombok`

说明：

- 保留 `Spring MVC`，不建议首版切到 `WebFlux`。当前业务主要是 HTTP API、上传、后台异步入库、可选 SSE 输出，MVC 已经够用。
- 数据访问继续沿用 `MyBatis`，因为你的业务表会偏“任务表/配置表/来源表/会话表”，不需要引入 ORM 复杂度。
- 使用 `Flyway` 进行数据库迁移，确保版本控制和回滚。
- 使用 `lombok` 注解简化代码

### 2.2 AI 与检索

- 对话与总结编排：`ChatClient`
- RAG 检索注入：`QuestionAnswerAdvisor` / 向量检索 advisor
- 向量库：`pgvector`
- 嵌入模型：优先用 `ZhiPu Embedding`
- 总结/问答模型：`DeepSeek` + `ZhiPu` 双提供商

建议：

- `EmbeddingModel` 尽量固定为一个主模型，不要频繁切换，否则向量空间不一致会导致召回质量下降。
- `ChatModel` 才是多模型路由的重点。比如：
  - 低成本总结、改写、标签抽取走 `DeepSeek`
  - 高质量最终回答、复杂归纳、低幻觉场景走 `ZhiPu`

### 2.3 文档摄取

- 文档存储：`MinIO`
- 文档解析：`Apache Tika`
- 分块：`TokenTextSplitter`
- 异步处理：Spring `@Async` + 数据库任务表

建议：

- 原始文件放 `MinIO`
- 文件元数据、摄取任务、检索缓存、会话数据放 PostgreSQL
- 向量数据放 `pgvector`

### 2.4 外部游戏资料检索

外部资料检索不要直接让 LLM 自己“回忆”网页内容，应该独立成搜索适配层：

- 统一接口：`SearchGateway`
- 首版推荐：对接商业搜索 API 或自建聚合搜索服务
- 不建议首版直接做站点爬虫，维护成本高、反爬和清洗成本也高

落地建议：

- 如果优先快速上线，先接一个成熟搜索 API。
- 如果优先可控和可替换，接 `SearXNG` 这一类自托管聚合搜索，再按需要补充站点定向抓取。

## 3. 总体架构

```text
User
  -> REST API
  -> Query Orchestrator
      -> Game Resolver
      -> Retrieval Planner
      -> Internal RAG Retriever
          -> PgVectorStore
      -> External Search Gateway
          -> Search API / SearXNG
      -> Evidence Fusion Service
      -> Answer Composer
          -> ChatClient + ModelRouter + Advisors
  -> Response with citations

Upload Document
  -> Upload API
  -> MinIO
  -> Ingest Job
  -> Tika Parser
  -> Text Cleaner
  -> TokenTextSplitter
  -> EmbeddingModel
  -> PgVectorStore
```

## 4. 分层设计

不使用 DDD，采用下面这套简单分层：

- `controller`
  - 对外 REST API
  - 上传接口、问答接口、知识库管理接口、配置接口
- `application`
  - 用例编排层
  - 负责一个请求从入参到结果的完整流程
- `ai`
  - `ChatClient`、`ChatModel`、`EmbeddingModel`、Prompt 模板、模型路由
- `rag`
  - 向量检索、元数据过滤、检索上下文组装、引用生成
- `search`
  - 外部搜索适配、网页内容清洗、结果缓存
- `ingest`
  - 文件上传、解析、切分、向量写入、重建索引
- `mapper`
  - MyBatis Mapper
- `model`
  - `entity`、`dto`、`request`、`response`
- `config`
  - Spring 配置、模型注册、对象存储配置、线程池配置
- `support`
  - 工具类、异常、枚举、常量、统一响应

建议的包结构：

```text
io.github.izzcj.gamemaster
  |- controller
  |- application
  |- ai
  |- rag
  |- search
  |- ingest
  |- mapper
  |- model
  |   |- entity
  |   |- dto
  |   |- request
  |   |- response
  |- config
  |- support
```

## 5. 核心流程设计

### 5.1 用户问答流程

1. `QueryController` 接收用户问题。
2. `QueryApplicationService` 做请求编排。
3. `GameResolver` 识别候选游戏、别名、平台、版本。
4. `RetrievalPlanner` 生成检索策略：
   - 是否只查内部知识库
   - 是否补充外部搜索
   - 是否需要更偏攻略、评测、配装、剧情或 mod 资料
5. `InternalKnowledgeRetriever` 从 `PgVectorStore` 检索相关片段。
6. `ExternalSearchGateway` 查询外部资料，并把结果转成统一 `Evidence` 结构。
7. `EvidenceFusionService` 去重、排序、过滤低质量来源。
8. `AnswerComposerService` 使用 `ChatClient` 总结并生成最终答案。
9. 返回答案、来源链接、命中的知识片段摘要。

### 5.2 文档上传入库流程

1. `KnowledgeUploadController` 接收文件和标签。
2. 文件原件写入 `MinIO`。
3. 创建 `ingest_job` 记录，异步执行解析。
4. `DocumentParseService` 用 `Tika` 抽取文本。
5. `DocumentCleanService` 清洗页眉页脚、乱码、重复空白。
6. `ChunkService` 用 `TokenTextSplitter` 分块。
7. `EmbeddingService` 生成向量。
8. 写入 `vector_store`，同时写业务表元数据。
9. 更新任务状态，供后台查看。

## 6. RAG 设计

### 6.1 数据来源分层

- `内部知识`
  - 用户上传文档
  - 项目内置游戏资料
  - 后台录入的 FAQ、术语表、版本差异说明
- `外部知识`
  - 搜索 API 返回的搜索结果
  - 按需抓取并清洗的页面正文
  - 可缓存的攻略站页面摘要

### 6.2 检索策略

建议采用“两段式召回”：

1. `向量召回`
   - 面向上传文档、内部知识库
   - 使用 `PgVectorStore`
2. `外部搜索召回`
   - 面向最新攻略、社区讨论、版本改动
   - 由 `SearchGateway` 提供

然后在应用层做融合：

- 按来源可信度打分
- 按游戏匹配度打分
- 按问题类型做权重调整
- 去除重复摘要和明显低质量页面

### 6.3 Chunk 元数据

建议写入向量库的 metadata 至少包括：

- `knowledgeBaseId`
- `fileId`
- `gameId`
- `gameName`
- `sourceType`
- `sourceUrl`
- `title`
- `version`
- `language`
- `tags`
- `chunkIndex`

这样后面可以直接做：

- 按游戏过滤
- 按资料类型过滤
- 按语言过滤
- 按版本过滤

### 6.4 Prompt 与引用策略

最终回答不要只输出“结论”，必须要求模型：

- 只基于证据回答
- 区分“知识库文档”与“外部网页”
- 对不确定内容明确说明
- 返回引用来源标题、链接或文件名

建议将 Prompt 拆成三类：

- `query-rewrite.st`
- `answer-with-citations.st`
- `evidence-summary.st`

## 7. 多模型配置方案

### 7.1 设计原则

- 模型配置外置，不在代码里写死 model name
- 将“提供商配置”和“业务路由规则”拆开
- 由 `ModelRouter` 决定当前场景使用哪个模型

### 7.2 推荐实现

定义一个 `AgentModelProperties`：

- `chatModels`
- `embeddingModels`
- `routing`
- `fallback`

示例思路：

```yaml
agent:
  model:
    routing:
      rewrite: deepseek-chat
      summarize: deepseek-chat
      final-answer: zhipuai-primary
      fallback-chat: deepseek-chat
      embedding: zhipuai-embedding
```

业务层只依赖：

- `ModelRouter.selectChatModel(scene)`
- `ModelRouter.selectEmbeddingModel(scene)`

不要让每个 service 自己判断到底调用哪个厂商。

### 7.3 建议的场景路由

- `QUERY_REWRITE`
  - 低成本模型
- `GAME_RESOLVE`
  - 低成本模型
- `ANSWER_SUMMARY`
  - 中成本模型
- `FINAL_ANSWER`
  - 高质量模型
- `DOC_TAG_EXTRACT`
  - 低成本模型

## 8. 数据库设计建议

### 8.1 核心业务表

建议首版至少有以下表：

- `game_catalog`
  - 游戏主数据、别名、平台、标签
- `knowledge_base`
  - 知识库定义，如“用户上传文档库”“官方攻略库”
- `knowledge_file`
  - 文件元数据、MinIO 路径、解析状态
- `ingest_job`
  - 上传、解析、切分、向量化任务状态
- `web_source_cache`
  - 外部检索缓存和正文摘要
- `chat_session`
  - 会话维度
- `chat_message`
  - 消息记录

向量表建议直接使用 Spring AI 的 `vector_store`。

### 8.2 为什么不单独建 chunk 业务表

首版不建议再建一张完整的 `knowledge_chunk` 表，原因是：

- `vector_store` 已经保存 chunk 内容和 metadata
- 再维护一张 chunk 表会出现双写和一致性问题
- 首版重点是检索质量，不是复杂内容审计

如果后面需要：

- 回溯每次向量化版本
- 人工审核 chunk
- 精细化统计 chunk 生命周期

再补 `knowledge_chunk_snapshot` 即可。

## 9. API 设计建议

### 9.1 对话接口

- `POST /api/chat/query`
  - 普通问答
- `POST /api/chat/query/stream`
  - SSE 流式回答

请求体建议包含：

- `question`
- `gameName`
- `platform`
- `useExternalSearch`
- `sessionId`
- `knowledgeBaseIds`

### 9.2 知识库接口

- `POST /api/knowledge/files`
  - 上传文档
- `GET /api/knowledge/files`
  - 查询文件列表
- `POST /api/knowledge/files/{id}/reindex`
  - 重建索引
- `GET /api/knowledge/jobs/{id}`
  - 查询摄取任务状态

### 9.3 管理接口

- `GET /api/admin/models`
  - 查看模型配置
- `POST /api/admin/models/reload`
  - 热更新模型配置
- `GET /api/admin/search-sources`
  - 查看搜索适配器状态

## 10. 可观测性与质量控制

建议接入：

- `Micrometer`
- `Spring AI Observability`
- `Actuator`

重点观测指标：

- 模型调用耗时
- Token 消耗
- 向量检索耗时
- 外部搜索耗时
- 命中来源数量
- 引用率
- 无答案率
- 幻觉反馈率

同时加两类日志：

- 业务日志：请求、检索计划、命中来源
- AI 日志：模型、prompt 模板、token、耗时、fallback

## 11. 首版实现顺序

建议按下面的顺序做，而不是一开始把所有能力都堆上去：

1. 先完成内部知识库上传 + RAG 问答闭环
2. 再补多模型路由
3. 再接外部搜索聚合
4. 最后补流式输出、缓存、后台管理

这样可以尽快验证两个关键问题：

- 你的知识切分和召回质量是否足够
- 游戏问答场景下，多模型路由是否真能提升性价比

## 12. 对当前项目的具体建议

### 12.1 依赖层面

当前 `pom.xml` 已经有 Spring AI、MyBatis、PostgreSQL、MinIO、Tika 的基础依赖，方向是对的。

建议使用 Spring AI Boot Starter 组合，而不是只停留在底层模块依赖。这样后面做自动装配、属性配置和 provider 切换会更顺。

### 12.2 包结构层面

当前项目还很空，适合直接按“分层 + 模块化”建目录，不要后面再重构一次。

### 12.3 数据层面

首版优先 PostgreSQL + pgvector，不要同时引入 Elasticsearch、Redis 向量检索或额外图数据库，复杂度不值得。

### 12.4 外部资料层面

首版建议把“网页检索”和“网页正文抓取”拆开：

- 第一步先拿搜索结果摘要做证据融合
- 第二步再对高价值结果抓正文

不要一上来对所有搜索结果都抓全文，否则成本和延迟都会失控。

## 13. 结论

这套项目最合适的落地方向是：

- 用 `Spring Boot + Spring AI + MyBatis + PostgreSQL/pgvector + MinIO + Tika`
- 架构上采用“分层 + 模块化”，明确拆分 `application / ai / rag / search / ingest`
- RAG 以 `PgVectorStore` 为核心，外部资料通过 `SearchGateway` 接入
- 多模型通过 `ModelRouter` 统一路由，Embedding 尽量固定，ChatModel 按场景切换
- 文档上传走 `MinIO -> Tika -> TokenTextSplitter -> Embedding -> PgVectorStore`

如果继续往下做实现，建议下一步直接落 4 个最小闭环：

1. 上传文档并写入向量库
2. 基于内部知识库问答
3. 多模型配置与路由
4. 外部搜索结果聚合
