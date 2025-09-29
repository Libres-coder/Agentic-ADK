# Agentic ADK Python 版本开发路线

## 当前情况
- Java 版本 `ali-agentic-adk-java` 已拥有 FlowCanvas/FlowNode、DelegationExecutor、CallbackChain、Pipeline 等核心模块，并在 `ali-agentic-adk-extension` 中扩展了 computer-use 等场景，整体成熟度高。
- Python 版本目前聚焦在 `core/model` 的 LLM 封装、`core/tool` 的 DashScope/Bailian 工具、`core/embedding` 的 OpenAI 向量服务，以及 Browser Use 的 FastAPI 扩展，缺少统一运行时、流程编排、回调体系与完善的测试文档。
- 目标是在半年内交付一个可服务 PoC/轻量生产的 Python 版 ADK，沿用 Java 版的核心概念，但以 Pythonic 方式分阶段实现。

## 指导原则
- 优先复用现有 Google ADK 入口，渐进式抽象出独立的 Python Runtime，避免一次性推翻现有代码。
- 每个里程碑都交付文档、自动化测试与可运行示例，确保能力逐步可验证。
- 与 Java 版保持术语/目录对齐，同时利用 asyncio、类型提示等 Python 最佳实践简化实现。

## 里程碑

### Milestone 0（0-4 周）：打磨基础与可观测性
- [x] 补齐 `README.md` 与 `docs/module_overview.md`，说明 `core/model|tool|embedding` 现有接口和依赖配置。
- [x] 为 `DashscopeLLM`、`BailianAppTool`、`OpenAIEmbedding` 增加单元测试与最小集成测试（mock 外部服务，记录所需环境变量）。
- [x] 引入 `pydantic` 配置模型，统一管理 API Key / Endpoint（`src/ali_agentic_adk_python/config`），并更新示例代码以使用配置。

### Milestone 1（4-10 周）：最小执行运行时
- [ ] 新建 `core/runtime` 模块，实现 `Request`、`Result`、`InvokeMode`、`SystemContext` 数据结构，对齐 Java 版语义。
- [ ] 定义 `Executor` 抽象与 `SyncExecutor` 实现，串联现有 LLM/Tool 调用并输出结构化事件日志。
- [ ] 将 Browser Use 扩展与运行时集成：把 `BrowserUseServiceCaller` 挂载进 `SystemContext`，提供端到端示例脚本。

### Milestone 2（10-18 周）：流程编排 MVP
- [ ] 新建 `core/flow` 包，实现 `FlowCanvas`、`FlowNode`、`NodeFactory`，支持 YAML/JSON 流程描述映射到节点树。
- [ ] 提供内置节点 `LlmFlowNode`、`ToolFlowNode`、`ConditionalNode`，并与 `Executor` 结合完成流程运行。
- [ ] 实现 `core/callback` 与最小 `CallbackChain`，默认内置日志/指标回调，并支持流程级别快照导出。

### Milestone 3（18-24 周）：生态与稳定
- [ ] 扩展工具体系：新增 Function Tool / MCP 适配器，统一注册发现机制。
- [ ] 引入 `core/storage` 抽象，提供内存实现与 Redis 插件，用于持久化会话与节点状态。
- [ ] 完成 FastAPI 管理入口：暴露流程部署、执行监控、调试面板（复用 Java Runner/Callback 思路），并撰写部署指引。

## 持续性工作
- [ ] 建立与 `ali-agentic-adk-java` 的功能对照表，按 Sprint 更新差异与迁移计划。
- [ ] 配置 `pytest` + `ruff` + `mypy` CI，确保新增模块都具备测试与类型检查覆盖。
- [ ] 定期审查安全合规（密钥管理、日志脱敏），并同步到文档。

> 说明：时间估算基于 2 人全职投入，可随资源滚动调整；每个里程碑需交付可演示 Demo 与文档，使 Python 版能力持续逼近 Java 核心特性。
