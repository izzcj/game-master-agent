# tavily-mcp-server

独立的 Tavily MCP Streamable HTTP 服务。启动后默认暴露 `http://localhost:8091/mcp`，供主服务 `game-master-agent` 以 MCP client 方式接入。

必填配置：

- `tavily.api-key`

默认行为：

- 工具名：`search_web_for_walkthrough`
- 当 Tavily 未启用、未配置 key、或请求失败时，返回空结果结构
