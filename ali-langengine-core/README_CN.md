# Ali-LangEngine-Core

## 项目简介

Ali-LangEngine-Core 是 Agentic-ADK 框架的核心引擎模块，提供了构建 AI Agent 应用所需的基础组件和抽象层。该模块实现了基于 Google ADK 接口的核心功能，集成了多种大语言模型、工具系统、内存管理、向量存储等核心能力，为开发者提供了一个强大、灵活且可扩展的 Agent 开发框架。

## 核心特性

### 🤖 多智能体支持
- **Agent 基础框架**：提供完整的 Agent 生命周期管理
- **多种 Agent 类型**：支持 AutoGPT、MRKL、Plan-Execute、ReAct 等多种 Agent 模式
- **Agent 执行器**：统一的 Agent 执行和调度引擎
- **智能工具调用**：自动工具选择和执行管理

### 🔗 链式处理架构
- **LLM 链**：基础的语言模型调用链
- **顺序链**：支持多步骤链式处理
- **条件分支**：智能路由和条件判断
- **组合链**：复杂业务逻辑的组合处理

### 🧠 语言模型抽象
- **统一接口**：兼容多种大语言模型
- **流式处理**：支持实时流式响应
- **异步调用**：高性能异步处理能力
- **上下文管理**：智能上下文长度管理

### 🛠 工具系统
- **工具抽象**：统一的工具接口定义
- **结构化工具**：支持复杂参数和返回值
- **工具注册**：动态工具发现和注册
- **工具执行器**：安全的工具执行环境

### 💾 记忆与存储
- **对话记忆**：多种对话历史管理策略
- **向量存储**：高效的语义搜索和检索
- **文档处理**：文档加载、分割和索引
- **嵌入模型**：统一的文本向量化接口

### 🔄 Runnable 框架
- **响应式编程**：基于 RxJava3 的响应式处理
- **管道操作**：灵活的数据处理管道
- **并行处理**：高效的并行计算支持
- **错误处理**：完善的异常处理和重试机制

## 项目结构

```
ali-langengine-core/
├── src/main/java/com/alibaba/langengine/core/
│   ├── adapter/           # 适配器模块
│   ├── agent/            # Agent 核心模块
│   │   ├── autogpt/      # AutoGPT Agent 实现
│   │   ├── conversational/ # 对话 Agent
│   │   ├── mrkl/         # MRKL Agent
│   │   ├── planexecute/  # 规划执行 Agent
│   │   ├── reactdoc/     # ReAct Document Agent
│   │   ├── selfask/      # SelfAsk Agent
│   │   ├── semantickernel/ # Semantic Kernel Agent
│   │   └── structured/   # 结构化 Agent
│   ├── caches/           # 缓存管理
│   ├── callback/         # 回调系统
│   ├── chain/            # 链式处理
│   ├── chatmodel/        # 聊天模型
│   ├── config/           # 配置管理
│   ├── docloader/        # 文档加载器
│   ├── doctransformer/   # 文档转换器
│   ├── embeddings/       # 嵌入模型
│   ├── indexes/          # 索引和检索
│   ├── languagemodel/    # 语言模型抽象
│   ├── memory/           # 内存管理
│   ├── messages/         # 消息类型
│   ├── model/            # 模型定义
│   ├── outputparser/     # 输出解析器
│   ├── prompt/           # 提示模板
│   ├── runnables/        # Runnable 框架
│   ├── storage/          # 存储抽象
│   ├── textsplitter/     # 文本分割器
│   ├── tokenizers/       # 分词器
│   ├── tool/             # 工具系统
│   ├── util/             # 工具类
│   └── vectorstore/      # 向量存储
└── src/test/             # 测试代码
```

## 核心组件详解

### 1. Agent 系统

#### BaseLanguageModel
- 所有语言模型的抽象基类
- 提供统一的文本生成、预测和流式处理接口
- 支持函数调用和结构化输出

```java
public abstract class BaseLanguageModel<T> extends Runnable<RunnableInput, RunnableOutput> {
    // 统一的预测接口
    public abstract String predict(String text, List<String> stops, 
                                 ExecutionContext executionContext, 
                                 Consumer<T> consumer, 
                                 Map<String, Object> extraAttributes);
    
    // 批量生成
    public abstract LLMResult generatePrompt(List<PromptValue> prompts, 
                                           List<FunctionDefinition> functions, 
                                           List<String> stops, 
                                           ExecutionContext executionContext, 
                                           Consumer<T> consumer, 
                                           Map<String, Object> extraAttributes);
}
```

#### Agent
- Agent 的核心抽象类
- 管理思考过程和工具调用决策
- 构建 Agent 的推理暂存器

```java
public abstract class Agent extends BaseSingleActionAgent {
    // 构建思考暂存器
    public String constructScratchpad(List<AgentAction> intermediateSteps);
    
    // 规划下一步动作
    public Object plan(List<AgentAction> intermediateSteps, 
                      Map<String, Object> inputs, 
                      Consumer<String> consumer, 
                      ExecutionContext executionContext, 
                      Map<String, Object> extraAttributes);
}
```

#### AgentExecutor
- Agent 执行器，管理完整的执行流程
- 控制最大迭代次数和执行时间
- 处理工具调用和结果反馈

```java
public class AgentExecutor extends Chain {
    private BaseSingleActionAgent agent;
    private List<BaseTool> tools;
    private Integer maxIterations = 10;
    private String earlyStoppingMethod = "generate";
    
    // 执行思考-行动-观察循环
    public Object takeNextStep(Map<String, BaseTool> nameToToolMap,
                              Map<String, Object> inputs,
                              List<AgentAction> intermediateSteps,
                              Consumer<String> consumer,
                              ExecutionContext executionContext,
                              Map<String, Object> extraAttributes);
}
```

### 2. 链式处理

#### Chain
- 所有链式处理组件的基类
- 提供统一的调用、运行和回调接口
- 支持内存管理和执行上下文

```java
public abstract class Chain extends Runnable<RunnableInput, RunnableOutput> {
    private BaseLanguageModel llm;
    private BaseMemory memory;
    private BaseCallbackManager callbackManager;
    
    // 核心调用方法
    public abstract Map<String, Object> call(Map<String, Object> inputs, 
                                           ExecutionContext executionContext,
                                           Consumer<String> consumer, 
                                           Map<String, Object> extraAttributes);
}
```

#### LLMChain
- 结合提示模板和语言模型的基础链
- 支持批量处理和异步调用
- 提供预测和格式化输出

```java
public class LLMChain extends Chain {
    private BasePromptTemplate prompt;
    private String outputKey = "text";
    
    // 生成 LLM 结果
    public LLMResult generate(List<Map<String, Object>> inputs, 
                            ExecutionContext executionContext, 
                            Consumer<String> consumer, 
                            Map<String, Object> extraAttributes);
}
```

### 3. 工具系统

#### BaseTool
- 所有工具的抽象基类
- 定义工具的名称、描述和执行接口
- 支持函数定义和参数验证

```java
public abstract class BaseTool extends Runnable<Object, RunnableOutput> {
    private String name;
    private String description;
    private String parameters;
    private Map<String, Object> args;
    private boolean returnDirect;
    
    // 工具执行方法
    public abstract ToolExecuteResult run(String toolInput, 
                                        ExecutionContext executionContext);
}
```

### 4. 内存管理

#### BaseMemory
- 内存系统的抽象基类
- 管理对话历史和上下文信息
- 支持不同角色的消息前缀设置

```java
public abstract class BaseMemory {
    private String humanPrefix = "Human";
    private String aiPrefix = "AI";
    private String memoryKey = "history";
    
    // 加载内存变量
    public abstract Map<String, Object> loadMemoryVariables(String sessionId, 
                                                          Map<String, Object> inputs);
    
    // 保存上下文
    public abstract void saveContext(String sessionId, 
                                   Map<String, Object> inputs, 
                                   Map<String, Object> outputs);
}
```

### 5. 向量存储

#### VectorStore
- 向量数据库的抽象基类
- 提供文档存储和相似性搜索
- 支持检索器集成

```java
public abstract class VectorStore {
    // 添加文档
    public abstract void addDocuments(List<Document> documents);
    
    // 相似性搜索
    public abstract List<Document> similaritySearch(String query, 
                                                  int k, 
                                                  Double maxDistanceValue, 
                                                  Integer type);
    
    // 转换为检索器
    public BaseRetriever asRetriever();
}
```

### 6. Runnable 框架

#### Runnable
- 可执行工作单元的基类
- 提供统一的调用、批处理、流式处理接口
- 支持异步执行和组合变换

```java
public abstract class Runnable<Input, Output> implements RunnableInterface<Input, Output> {
    // 基础调用
    public abstract Output invoke(Input input, RunnableConfig config);
    
    // 流式处理
    public abstract Output stream(Input input, 
                                RunnableConfig config, 
                                Consumer<Object> chunkConsumer);
    
    // 批处理
    public List<Output> batch(List<Input> inputs, RunnableConfig config);
    
    // 组合操作
    public static RunnableSequence sequence(RunnableInterface... runnables);
    public static RunnableParallel parallel(RunnableInterface... runnables);
}
```

## 技术栈

- **Java 8+**：核心开发语言
- **Maven**：项目构建和依赖管理
- **RxJava3**：响应式编程支持
- **Jackson**：JSON 序列化和反序列化
- **OkHttp3**：HTTP 客户端
- **Retrofit2**：REST API 客户端
- **Guava**：工具库和缓存
- **Commons Lang3**：Apache 工具库
- **Dom4j**：XML 处理
- **JSoup**：HTML 解析

## 主要依赖

```xml
<dependencies>
    <!-- Apache Commons -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
    
    <!-- JSON 处理 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
    </dependency>
    
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <!-- HTTP 客户端 -->
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
    </dependency>
    
    <dependency>
        <groupId>com.squareup.retrofit2</groupId>
        <artifactId>retrofit</artifactId>
    </dependency>
    
    <!-- 工具库 -->
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
    </dependency>
    
    <!-- HTML/XML 处理 -->
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.dom4j</groupId>
        <artifactId>dom4j</artifactId>
    </dependency>
</dependencies>
```

## 快速开始

### 1. 基本 LLM 调用

```java
// 创建语言模型（需要具体实现）
BaseLanguageModel llm = new YourLLMImplementation();

// 创建 LLM 链
LLMChain chain = new LLMChain();
chain.setLlm(llm);

// 创建提示模板
PromptTemplate prompt = new PromptTemplate();
prompt.setTemplate("请回答以下问题：{question}");
prompt.setInputVariables(Arrays.asList("question"));
chain.setPrompt(prompt);

// 执行调用
Map<String, Object> inputs = new HashMap<>();
inputs.put("question", "什么是人工智能？");
Map<String, Object> result = chain.run(inputs);
System.out.println(result.get("text"));
```

### 2. 创建 Agent

```java
// 准备工具
List<BaseTool> tools = Arrays.asList(
    new SearchTool(),
    new CalculatorTool()
);

// 创建 Agent
Agent agent = StructuredChatAgent.fromLlmAndTools(llm, tools);

// 创建 Agent 执行器
AgentExecutor executor = new AgentExecutor();
executor.setAgent(agent);
executor.setTools(tools);
executor.setMaxIterations(5);

// 执行任务
Map<String, Object> inputs = new HashMap<>();
inputs.put("input", "搜索今天的天气，然后计算 25 * 4");
Map<String, Object> result = executor.run(inputs);
System.out.println(result.get("output"));
```

### 3. 使用向量存储

```java
// 创建嵌入模型（需要具体实现）
Embeddings embeddings = new YourEmbeddingsImplementation();

// 创建向量存储（需要具体实现）
VectorStore vectorStore = new YourVectorStoreImplementation();
vectorStore.setEmbedding(embeddings);

// 添加文档
List<String> texts = Arrays.asList(
    "人工智能是计算机科学的一个分支",
    "机器学习是人工智能的核心技术",
    "深度学习是机器学习的一个子集"
);
vectorStore.addTexts(texts);

// 搜索相似文档
List<Document> results = vectorStore.similaritySearch("什么是AI？", 3);
for (Document doc : results) {
    System.out.println(doc.getPageContent());
}
```

### 4. 流式处理

```java
// 创建流式 Consumer
Consumer<String> streamConsumer = chunk -> {
    System.out.print(chunk);
};

// 流式调用
ExecutionContext context = new ExecutionContext();
chain.run(inputs, context, streamConsumer, null);
```

## 扩展开发

### 1. 自定义 Agent

```java
public class CustomAgent extends Agent {
    
    @Override
    public String observationPrefix() {
        return "观察: ";
    }
    
    @Override
    public String llmPrefix() {
        return "思考: ";
    }
    
    // 实现其他抽象方法...
}
```

### 2. 自定义工具

```java
public class CustomTool extends BaseTool {
    
    public CustomTool() {
        setName("custom_tool");
        setDescription("这是一个自定义工具");
        setParameters("{\"type\":\"object\",\"properties\":{\"input\":{\"type\":\"string\"}}}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        // 实现工具逻辑
        String result = processInput(toolInput);
        return new ToolExecuteResult(result);
    }
    
    private String processInput(String input) {
        // 自定义处理逻辑
        return "处理结果: " + input;
    }
}
```

### 3. 自定义链

```java
public class CustomChain extends Chain {
    
    @Override
    public Map<String, Object> call(Map<String, Object> inputs,
                                  ExecutionContext executionContext,
                                  Consumer<String> consumer,
                                  Map<String, Object> extraAttributes) {
        // 实现自定义链逻辑
        Map<String, Object> outputs = new HashMap<>();
        // 处理逻辑...
        return outputs;
    }
    
    @Override
    public List<String> getInputKeys() {
        return Arrays.asList("input");
    }
    
    @Override
    public List<String> getOutputKeys() {
        return Arrays.asList("output");
    }
}
```

## 配置说明

### 全局配置

```java
// 设置全局回调管理器
LangEngineConfiguration.CALLBACK_MANAGER = new CustomCallbackManager();

// 设置缓存
LangEngineConfiguration.CurrentCache = new InMemoryCache();

// 设置推荐数量
LangEngineConfiguration.RETRIEVAL_QA_RECOMMEND_COUNT = "5";
```

### 回调处理

```java
// 创建回调处理器
BaseCallbackHandler handler = new LocalLogCallbackHandler();

// 创建回调管理器
CallbackManager callbackManager = new CallbackManager();
callbackManager.addHandler(handler);

// 设置给组件
chain.setCallbackManager(callbackManager);
```

## 性能优化

### 1. 异步处理

```java
// 异步调用链
CompletableFuture<Map<String, Object>> future = chain.runAsync(inputs);
Map<String, Object> result = future.get();

// 异步 LLM 调用
CompletableFuture<String> prediction = llm.predictAsync("输入文本");
String result = prediction.get();
```

### 2. 批处理

```java
// 批量处理多个输入
List<Map<String, Object>> batchInputs = Arrays.asList(inputs1, inputs2, inputs3);
List<Map<String, Object>> results = chain.batch(batchInputs);
```

### 3. 缓存策略

```java
// 使用缓存的嵌入模型
CacheBackedEmbeddings cachedEmbeddings = new CacheBackedEmbeddings();
cachedEmbeddings.setUnderlyingEmbeddings(baseEmbeddings);
cachedEmbeddings.setDocumentEmbeddingCache(cache);
```

## 最佳实践

### 1. 错误处理

```java
try {
    Map<String, Object> result = chain.run(inputs);
    // 处理成功结果
} catch (Exception e) {
    // 记录错误
    log.error("Chain execution failed", e);
    // 处理错误情况
}
```

### 2. 资源管理

```java
// 使用 try-with-resources 管理资源
try (ExecutionContext context = new ExecutionContext()) {
    context.setChain(chain);
    Map<String, Object> result = chain.run(inputs, context, null, null);
}
```

### 3. 配置管理

```java
// 使用配置文件管理参数
Properties config = WorkPropertiesUtils.loadProperties("config.properties");
String maxTokens = config.getProperty("llm.max_tokens", "2048");
```

## 测试

项目包含完整的单元测试和集成测试：

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=AgentExecutorTest

# 生成测试报告
mvn surefire-report:report
```

## 贡献指南

1. Fork 项目
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add some amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 提交 Pull Request

## 许可证

本项目采用 Apache License 2.0 许可证。详见 [LICENSE](../LICENSE) 文件。

## 相关链接

- [Agentic-ADK 主项目](https://github.com/AIDC-AI/Agentic-ADK)
- [Ali-LangEngine 基础设施模块](../ali-langengine-infrastructure/README.md)
- [Ali-LangEngine 社区模块](../ali-langengine-community/README.md)
- [项目文档](https://github.com/AIDC-AI/Agentic-ADK/wiki)

---

该模块作为 Agentic-ADK 的核心引擎，为构建智能 Agent 应用提供了坚实的技术基础。通过模块化的设计和丰富的扩展点，开发者可以轻松构建各种类型的 AI 应用。