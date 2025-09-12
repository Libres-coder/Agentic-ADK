# Ali-LangEngine-Core

## Overview

Ali-LangEngine-Core is the core engine module of the Agentic-ADK framework, providing essential components and abstraction layers for building AI Agent applications. This module implements core functionalities based on Google ADK interfaces, integrating various large language models, tool systems, memory management, vector storage, and other core capabilities, offering developers a powerful, flexible, and extensible Agent development framework.

## Key Features

### 🤖 Multi-Agent Support
- **Agent Base Framework**: Complete Agent lifecycle management
- **Multiple Agent Types**: Support for AutoGPT, MRKL, Plan-Execute, ReAct and other Agent patterns
- **Agent Executor**: Unified Agent execution and scheduling engine
- **Intelligent Tool Calling**: Automatic tool selection and execution management

### 🔗 Chain Processing Architecture
- **LLM Chain**: Basic language model invocation chain
- **Sequential Chain**: Multi-step chain processing support
- **Conditional Branching**: Intelligent routing and conditional judgment
- **Composite Chain**: Complex business logic composition processing

### 🧠 Language Model Abstraction
- **Unified Interface**: Compatible with multiple large language models
- **Stream Processing**: Real-time streaming response support
- **Async Calls**: High-performance asynchronous processing capabilities
- **Context Management**: Intelligent context length management

### 🛠 Tool System
- **Tool Abstraction**: Unified tool interface definition
- **Structured Tools**: Support for complex parameters and return values
- **Tool Registry**: Dynamic tool discovery and registration
- **Tool Executor**: Safe tool execution environment

### 💾 Memory & Storage
- **Conversation Memory**: Multiple conversation history management strategies
- **Vector Storage**: Efficient semantic search and retrieval
- **Document Processing**: Document loading, splitting, and indexing
- **Embedding Models**: Unified text vectorization interface

### 🔄 Runnable Framework
- **Reactive Programming**: RxJava3-based reactive processing
- **Pipeline Operations**: Flexible data processing pipelines
- **Parallel Processing**: Efficient parallel computing support
- **Error Handling**: Comprehensive exception handling and retry mechanisms

## Project Structure

```
ali-langengine-core/
├── src/main/java/com/alibaba/langengine/core/
│   ├── adapter/           # Adapter modules
│   ├── agent/            # Agent core modules
│   │   ├── autogpt/      # AutoGPT Agent implementation
│   │   ├── conversational/ # Conversational Agent
│   │   ├── mrkl/         # MRKL Agent
│   │   ├── planexecute/  # Plan-Execute Agent
│   │   ├── reactdoc/     # ReAct Document Agent
│   │   ├── selfask/      # SelfAsk Agent
│   │   ├── semantickernel/ # Semantic Kernel Agent
│   │   └── structured/   # Structured Agent
│   ├── caches/           # Cache management
│   ├── callback/         # Callback system
│   ├── chain/            # Chain processing
│   ├── chatmodel/        # Chat models
│   ├── config/           # Configuration management
│   ├── docloader/        # Document loaders
│   ├── doctransformer/   # Document transformers
│   ├── embeddings/       # Embedding models
│   ├── indexes/          # Indexing and retrieval
│   ├── languagemodel/    # Language model abstraction
│   ├── memory/           # Memory management
│   ├── messages/         # Message types
│   ├── model/            # Model definitions
│   ├── outputparser/     # Output parsers
│   ├── prompt/           # Prompt templates
│   ├── runnables/        # Runnable framework
│   ├── storage/          # Storage abstraction
│   ├── textsplitter/     # Text splitters
│   ├── tokenizers/       # Tokenizers
│   ├── tool/             # Tool system
│   ├── util/             # Utility classes
│   └── vectorstore/      # Vector storage
└── src/test/             # Test code
```

## Core Component Details

### 1. Agent System

#### BaseLanguageModel
- Abstract base class for all language models
- Provides unified text generation, prediction, and streaming interfaces
- Supports function calling and structured output

```java
public abstract class BaseLanguageModel<T> extends Runnable<RunnableInput, RunnableOutput> {
    // Unified prediction interface
    public abstract String predict(String text, List<String> stops, 
                                 ExecutionContext executionContext, 
                                 Consumer<T> consumer, 
                                 Map<String, Object> extraAttributes);
    
    // Batch generation
    public abstract LLMResult generatePrompt(List<PromptValue> prompts, 
                                           List<FunctionDefinition> functions, 
                                           List<String> stops, 
                                           ExecutionContext executionContext, 
                                           Consumer<T> consumer, 
                                           Map<String, Object> extraAttributes);
}
```

#### Agent
- Core abstract class for Agents
- Manages reasoning process and tool calling decisions
- Builds Agent's reasoning scratchpad

```java
public abstract class Agent extends BaseSingleActionAgent {
    // Build reasoning scratchpad
    public String constructScratchpad(List<AgentAction> intermediateSteps);
    
    // Plan next action
    public Object plan(List<AgentAction> intermediateSteps, 
                      Map<String, Object> inputs, 
                      Consumer<String> consumer, 
                      ExecutionContext executionContext, 
                      Map<String, Object> extraAttributes);
}
```

#### AgentExecutor
- Agent executor managing complete execution flow
- Controls maximum iterations and execution time
- Handles tool calls and result feedback

```java
public class AgentExecutor extends Chain {
    private BaseSingleActionAgent agent;
    private List<BaseTool> tools;
    private Integer maxIterations = 10;
    private String earlyStoppingMethod = "generate";
    
    // Execute thought-action-observation loop
    public Object takeNextStep(Map<String, BaseTool> nameToToolMap,
                              Map<String, Object> inputs,
                              List<AgentAction> intermediateSteps,
                              Consumer<String> consumer,
                              ExecutionContext executionContext,
                              Map<String, Object> extraAttributes);
}
```

### 2. Chain Processing

#### Chain
- Base class for all chain processing components
- Provides unified call, run, and callback interfaces
- Supports memory management and execution context

```java
public abstract class Chain extends Runnable<RunnableInput, RunnableOutput> {
    private BaseLanguageModel llm;
    private BaseMemory memory;
    private BaseCallbackManager callbackManager;
    
    // Core call method
    public abstract Map<String, Object> call(Map<String, Object> inputs, 
                                           ExecutionContext executionContext,
                                           Consumer<String> consumer, 
                                           Map<String, Object> extraAttributes);
}
```

#### LLMChain
- Basic chain combining prompt templates and language models
- Supports batch processing and async calls
- Provides prediction and formatted output

```java
public class LLMChain extends Chain {
    private BasePromptTemplate prompt;
    private String outputKey = "text";
    
    // Generate LLM results
    public LLMResult generate(List<Map<String, Object>> inputs, 
                            ExecutionContext executionContext, 
                            Consumer<String> consumer, 
                            Map<String, Object> extraAttributes);
}
```

### 3. Tool System

#### BaseTool
- Abstract base class for all tools
- Defines tool name, description, and execution interface
- Supports function definition and parameter validation

```java
public abstract class BaseTool extends Runnable<Object, RunnableOutput> {
    private String name;
    private String description;
    private String parameters;
    private Map<String, Object> args;
    private boolean returnDirect;
    
    // Tool execution method
    public abstract ToolExecuteResult run(String toolInput, 
                                        ExecutionContext executionContext);
}
```

### 4. Memory Management

#### BaseMemory
- Abstract base class for memory systems
- Manages conversation history and context information
- Supports different role message prefix settings

```java
public abstract class BaseMemory {
    private String humanPrefix = "Human";
    private String aiPrefix = "AI";
    private String memoryKey = "history";
    
    // Load memory variables
    public abstract Map<String, Object> loadMemoryVariables(String sessionId, 
                                                          Map<String, Object> inputs);
    
    // Save context
    public abstract void saveContext(String sessionId, 
                                   Map<String, Object> inputs, 
                                   Map<String, Object> outputs);
}
```

### 5. Vector Storage

#### VectorStore
- Abstract base class for vector databases
- Provides document storage and similarity search
- Supports retriever integration

```java
public abstract class VectorStore {
    // Add documents
    public abstract void addDocuments(List<Document> documents);
    
    // Similarity search
    public abstract List<Document> similaritySearch(String query, 
                                                  int k, 
                                                  Double maxDistanceValue, 
                                                  Integer type);
    
    // Convert to retriever
    public BaseRetriever asRetriever();
}
```

### 6. Runnable Framework

#### Runnable
- Base class for executable work units
- Provides unified call, batch, and streaming interfaces
- Supports async execution and composition transformations

```java
public abstract class Runnable<Input, Output> implements RunnableInterface<Input, Output> {
    // Basic invocation
    public abstract Output invoke(Input input, RunnableConfig config);
    
    // Stream processing
    public abstract Output stream(Input input, 
                                RunnableConfig config, 
                                Consumer<Object> chunkConsumer);
    
    // Batch processing
    public List<Output> batch(List<Input> inputs, RunnableConfig config);
    
    // Composition operations
    public static RunnableSequence sequence(RunnableInterface... runnables);
    public static RunnableParallel parallel(RunnableInterface... runnables);
}
```

## Technology Stack

- **Java 8+**: Core development language
- **Maven**: Project build and dependency management
- **RxJava3**: Reactive programming support
- **Jackson**: JSON serialization and deserialization
- **OkHttp3**: HTTP client
- **Retrofit2**: REST API client
- **Guava**: Utility library and caching
- **Commons Lang3**: Apache utility library
- **Dom4j**: XML processing
- **JSoup**: HTML parsing

## Quick Start

### 1. Basic LLM Call

```java
// Create language model (requires concrete implementation)
BaseLanguageModel llm = new YourLLMImplementation();

// Create LLM chain
LLMChain chain = new LLMChain();
chain.setLlm(llm);

// Create prompt template
PromptTemplate prompt = new PromptTemplate();
prompt.setTemplate("Please answer the following question: {question}");
prompt.setInputVariables(Arrays.asList("question"));
chain.setPrompt(prompt);

// Execute call
Map<String, Object> inputs = new HashMap<>();
inputs.put("question", "What is artificial intelligence?");
Map<String, Object> result = chain.run(inputs);
System.out.println(result.get("text"));
```

### 2. Create Agent

```java
// Prepare tools
List<BaseTool> tools = Arrays.asList(
    new SearchTool(),
    new CalculatorTool()
);

// Create Agent
Agent agent = StructuredChatAgent.fromLlmAndTools(llm, tools);

// Create Agent executor
AgentExecutor executor = new AgentExecutor();
executor.setAgent(agent);
executor.setTools(tools);
executor.setMaxIterations(5);

// Execute task
Map<String, Object> inputs = new HashMap<>();
inputs.put("input", "Search today's weather, then calculate 25 * 4");
Map<String, Object> result = executor.run(inputs);
System.out.println(result.get("output"));
```

### 3. Use Vector Storage

```java
// Create embedding model (requires concrete implementation)
Embeddings embeddings = new YourEmbeddingsImplementation();

// Create vector storage (requires concrete implementation)
VectorStore vectorStore = new YourVectorStoreImplementation();
vectorStore.setEmbedding(embeddings);

// Add documents
List<String> texts = Arrays.asList(
    "Artificial intelligence is a branch of computer science",
    "Machine learning is the core technology of AI",
    "Deep learning is a subset of machine learning"
);
vectorStore.addTexts(texts);

// Search similar documents
List<Document> results = vectorStore.similaritySearch("What is AI?", 3);
for (Document doc : results) {
    System.out.println(doc.getPageContent());
}
```

### 4. Stream Processing

```java
// Create stream Consumer
Consumer<String> streamConsumer = chunk -> {
    System.out.print(chunk);
};

// Stream call
ExecutionContext context = new ExecutionContext();
chain.run(inputs, context, streamConsumer, null);
```

## Extension Development

### 1. Custom Agent

```java
public class CustomAgent extends Agent {
    
    @Override
    public String observationPrefix() {
        return "Observation: ";
    }
    
    @Override
    public String llmPrefix() {
        return "Thought: ";
    }
    
    // Implement other abstract methods...
}
```

### 2. Custom Tool

```java
public class CustomTool extends BaseTool {
    
    public CustomTool() {
        setName("custom_tool");
        setDescription("This is a custom tool");
        setParameters("{\"type\":\"object\",\"properties\":{\"input\":{\"type\":\"string\"}}}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        // Implement tool logic
        String result = processInput(toolInput);
        return new ToolExecuteResult(result);
    }
    
    private String processInput(String input) {
        // Custom processing logic
        return "Processing result: " + input;
    }
}
```

### 3. Custom Chain

```java
public class CustomChain extends Chain {
    
    @Override
    public Map<String, Object> call(Map<String, Object> inputs,
                                  ExecutionContext executionContext,
                                  Consumer<String> consumer,
                                  Map<String, Object> extraAttributes) {
        // Implement custom chain logic
        Map<String, Object> outputs = new HashMap<>();
        // Processing logic...
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

## Configuration

### Global Configuration

```java
// Set global callback manager
LangEngineConfiguration.CALLBACK_MANAGER = new CustomCallbackManager();

// Set cache
LangEngineConfiguration.CurrentCache = new InMemoryCache();

// Set recommendation count
LangEngineConfiguration.RETRIEVAL_QA_RECOMMEND_COUNT = "5";
```

### Callback Handling

```java
// Create callback handler
BaseCallbackHandler handler = new LocalLogCallbackHandler();

// Create callback manager
CallbackManager callbackManager = new CallbackManager();
callbackManager.addHandler(handler);

// Set to component
chain.setCallbackManager(callbackManager);
```

## Performance Optimization

### 1. Async Processing

```java
// Async chain call
CompletableFuture<Map<String, Object>> future = chain.runAsync(inputs);
Map<String, Object> result = future.get();

// Async LLM call
CompletableFuture<String> prediction = llm.predictAsync("Input text");
String result = prediction.get();
```

### 2. Batch Processing

```java
// Batch process multiple inputs
List<Map<String, Object>> batchInputs = Arrays.asList(inputs1, inputs2, inputs3);
List<Map<String, Object>> results = chain.batch(batchInputs);
```

### 3. Caching Strategy

```java
// Use cached embedding model
CacheBackedEmbeddings cachedEmbeddings = new CacheBackedEmbeddings();
cachedEmbeddings.setUnderlyingEmbeddings(baseEmbeddings);
cachedEmbeddings.setDocumentEmbeddingCache(cache);
```

## Best Practices

### 1. Error Handling

```java
try {
    Map<String, Object> result = chain.run(inputs);
    // Handle success result
} catch (Exception e) {
    // Log error
    log.error("Chain execution failed", e);
    // Handle error cases
}
```

### 2. Resource Management

```java
// Use try-with-resources for resource management
try (ExecutionContext context = new ExecutionContext()) {
    context.setChain(chain);
    Map<String, Object> result = chain.run(inputs, context, null, null);
}
```

### 3. Configuration Management

```java
// Use configuration files for parameter management
Properties config = WorkPropertiesUtils.loadProperties("config.properties");
String maxTokens = config.getProperty("llm.max_tokens", "2048");
```

## Testing

The project includes comprehensive unit tests and integration tests:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AgentExecutorTest

# Generate test reports
mvn surefire-report:report
```

## Contributing

1. Fork the project
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add some amazing feature'`
4. Push branch: `git push origin feature/amazing-feature`
5. Submit Pull Request

## License

This project is licensed under the Apache License 2.0. See [LICENSE](../LICENSE) file for details.

## Related Links

- [Agentic-ADK Main Project](https://github.com/AIDC-AI/Agentic-ADK)
- [Ali-LangEngine Infrastructure Module](../ali-langengine-infrastructure/README.md)
- [Ali-LangEngine Community Module](../ali-langengine-community/README.md)
- [Project Documentation](https://github.com/AIDC-AI/Agentic-ADK/wiki)

---

This module serves as the core engine of Agentic-ADK, providing a solid technical foundation for building intelligent Agent applications. Through modular design and rich extension points, developers can easily build various types of AI applications.