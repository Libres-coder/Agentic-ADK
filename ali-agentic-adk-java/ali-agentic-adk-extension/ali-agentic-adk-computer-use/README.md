# ali-agentic-adk-computer-use

## 项目简介

`ali-agentic-adk-computer-use` 是一个基于Agentic-ADK的扩展模块，旨在提供计算机操作相关的原子能力。它封装了与浏览器交互、执行脚本等核心功能，方便上层应用调用。

该项目依赖于阿里云的ECD（Elastic Desktop Service）和EDS（Elastic Desktop Service for Android）服务，通过调用这些服务的API来实现远程桌面命令的下发和结果获取。

## 功能特性

- **脚本上传**：支持将脚本内容上传到指定路径。
- **脚本执行**：支持在远程桌面上执行PowerShell脚本，并获取执行结果。
- **浏览器操作**：提供与浏览器交互的能力，例如模拟点击、输入等操作（具体实现需在`doScriptExecute`方法中定义）。

## 环境依赖

- Java 17+
- Maven 3.6+
- 阿里云ECD/EDS服务访问权限

## 快速开始

### 构建项目

```bash
mvn clean install
```

### 配置文件

在使用本模块之前，需要在`application.properties`或`application.yml`中配置以下参数：

- `ali.adk.browser.use.properties.ak`: 阿里云账户ak
- `ali.adk.browser.use.properties.sk`: 阿里云账户sk
- `ali.adk.browser.use.properties.endpoint`: 阿里云终端地址
- `ali.adk.browser.use.properties.endUserId`: 登录无影电脑的用户名称
- `ali.adk.browser.use.properties.password`: 登录无影电脑的用户密码
- `ali.adk.browser.use.properties.officeSiteId`: 无影的办公网络code


computer use参数
- `ali.adk.browser.use.properties.enable`: 开启阿里云无影computer开关
- `ali.adk.browser.use.properties.computerResourceId`: 无影机器id

mobile use参数
- `ali.adk.browser.use.properties.mobileEndPoint`: 阿里云无影手机的终端地址
- `ali.adk.browser.use.properties.appStreamEndPoint`: 阿里云无影appStream的终端地址
- `ali.adk.browser.use.properties.instanceGroupId`: 阿里云无影手机机器组id
- `ali.adk.browser.use.properties.mobileResourceId`: 阿里云无影手机机器id

### 使用示例

```java
//示例代码：如何构建一个computer use agent
@Bean
public LlmAgent browserOperateAgent() {

    OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
            .apiKey("your dashscope key")
            .modelName("your dashscope model")
            .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
            .build();

    LangChain4j langChain4j = new LangChain4j(openAiChatModel);

    LlmAgent agent = LlmAgent.builder()
            .name("browserOperateAgent")
            .model(langChain4j)
            .instruction(PromptUtils.generatePrompt(PromptConstant.browserOperateAgentPrompt, this::getHtmlInfo))
            .tools(FunctionTool.create(BrowserUseTool.class, "operateBrowser"),
                    FunctionTool.create(BrowserUseTool.class, "openBrowser"),
                    FunctionTool.create(BrowserUseTool.class, "loginFinish")
            )
            .build();

    BrowserAgentRegister.register(agent);
    return agent;
}
```

## 代码结构

```
src/main/java/com/alibaba/agentic/computer/use
├── AtomicOperations.java          // 原子操作接口定义
├── AtomicOperationsImpl.java      // 原子操作接口实现
├── configuration/                 // 配置类
├── controller/                    // 控制器类（如果有的话）
├── domain/                        // 数据传输对象（DTOs）
│   ├── BrowserUseRequest.java
│   └── BrowserUseResponse.java
├── enums/                         // 枚举类型
├── service/                       // 业务逻辑层
│   └── EcdCommandService.java
└── utils/                         // 工具类
```

## 贡献

欢迎提交 Issue 或 Pull Request 来改进本项目。