# ali-langengine-pyexecutor: 一个为JVM打造的安全Python代码执行沙箱

## 项目简介

`ali-langengine-pyexecutor` 是一个Java库，旨在为执行Python代码提供一个安全、隔离的环境。它专为AI代理和应用程序设计，作为一个沙箱化插件，能够安全地运行由大语言模型（LLM）生成或由用户提供的Python脚本，而不会对宿主系统造成安全风险。

该库通过进程级隔离以及对资源和权限（如网络访问、文件系统操作和模块导入）的精细控制来实现其安全性。

## 核心功能

- **强大的安全沙箱**: 每个Python脚本都在一个专用的、沙箱化的进程中运行，并遵循严格的安全策略。
- **资源限制**: 可强制限制CPU时间、内存使用（地址空间）和打开的文件描述符数量，以防止资源耗尽攻击。
- **权限控制**:
    - **网络访问**: 默认禁用网络，防止未经授权的外部通信。
    - **文件系统访问**: 限制文件系统操作，可根据配置实现“禁止访问”、“沙箱目录内只读”或“完全访问”。
    - **模块导入控制**: 利用白名单或黑名单来控制可以导入的Python模块，防止使用`os`、`subprocess`或`ctypes`等危险库。
- **双执行模式**:
    - **无状态执行 (`executeOnce`)**: 运行不可信、一次性脚本的理想选择。每次执行都会生成一个全新的、干净的Python进程，确保最大程度的隔离。
    - **有状态执行 (会话模式)**: 非常适合需要跨多个代码执行周期保持状态的交互式场景。每个会话使用一个持久化的Python守护进程，允许保留变量和上下文。
- **会话管理**: 包括空闲超时（TTL）、硬性生命周期限制和基于最大会话数的驱逐策略（LRU），以高效管理资源。
- **无缝的Spring Boot集成**: 提供自动配置功能，可轻松集成到Spring Boot应用中。只需添加依赖并在`application.properties`中进行配置即可。

## 快速上手

### 环境要求

- Java 17 或更高版本
- Apache Maven 3.6+
- Python 3 解释器（需在系统PATH中，或通过配置指定其路径）

### 安装

将以下依赖项添加到您的 `pom.xml` 文件中：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-langengine-pyexecutor</artifactId>
    <version>1.2.6-202508111516</version> 
</dependency>
```

## 使用指南

您可以将 `PyExecutor` 用于独立的Java应用程序，也可以将其与Spring Boot集成。

### 快速启动 (独立的 Main 函数)

这是了解该库工作原理的最直接方式。以下示例演示了无状态执行、有状态会话和错误处理。

```java
package com.alibaba.langengine.pyexecutor.examples;

import com.alibaba.langengine.pyexecutor.PyExecutor;
import com.alibaba.langengine.pyexecutor.PyExecutionPolicy;
import com.alibaba.langengine.pyexecutor.PyExecutionResult;
import com.alibaba.langengine.pyexecutor.SessionConfig;

public class PyExecutorDemo {

    public static void main(String[] args) {
        // 1. 配置执行策略
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setPythonBin("python3"); // 如果不在系统PATH中，请使用绝对路径
        policy.setTimeout(java.time.Duration.ofSeconds(10));
        
        // 2. 为有状态执行配置会话管理器
        SessionConfig sessionConfig = new SessionConfig();
        sessionConfig.setWorkspaceRoot(System.getProperty("java.io.tmpdir"));

        // 3. 创建 PyExecutor 实例
        PyExecutor pyExecutor = new PyExecutor(policy, sessionConfig);

        System.out.println("PyExecutor 演示开始。\n");

        // 运行所有示例
        runStatelessExample(pyExecutor);
        runStatefulSessionExample(pyExecutor);
        runErrorHandlingExample(pyExecutor);

        System.out.println("\nPyExecutor 演示结束。");
    }

    /**
     * 演示无状态（一次性）执行。
     */
    public static void runStatelessExample(PyExecutor pyExecutor) {
        System.out.println("--- 运行无状态 (executeOnce) 示例 ---");
        try {
            String code = "a = 5\nb = 10\na * b";
            PyExecutionResult result = pyExecutor.executeOnce(code, null);

            if (result.getExitCode() == 0) {
                System.out.println("执行成功。最后一个表达式的值: " + result.getLastValueRepr());
            } else {
                System.err.println("执行失败: " + result.getErrorRepr());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("-------------------------------------------------\n");
    }

    /**
     * 演示基于会话的有状态执行。
     */
    public static void runStatefulSessionExample(PyExecutor pyExecutor) {
        System.out.println("--- 运行有状态 (Session) 示例 ---");
        String sessionId = "my-interactive-session";
        try {
            System.out.println("步骤 1: 定义变量 'my_list'");
            pyExecutor.execute(sessionId, "my_list = [1, 2, 3]", null);

            System.out.println("步骤 2: 向 'my_list' 追加一个值");
            PyExecutionResult result = pyExecutor.execute(sessionId, "my_list.append(4); my_list", null);

            if (result.getExitCode() == 0) {
                System.out.println("执行成功。新的列表值: " + result.getLastValueRepr());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("步骤 3: 关闭会话");
            pyExecutor.closeSession(sessionId);
        }
        System.out.println("--------------------------------------------\n");
    }

    /**
     * 演示错误处理。
     */
    public static void runErrorHandlingExample(PyExecutor pyExecutor) {
        System.out.println("--- 运行错误处理示例 ---");
        try {
            String badCode = "result = 1 / 0";
            PyExecutionResult result = pyExecutor.executeOnce(badCode, null);

            System.out.println("执行完毕，分析结果...");
            System.out.println("   退出码: " + result.getExitCode());
            System.out.println("   错误信息: " + result.getErrorRepr());
            System.out.println("   标准错误流 (Stderr) 包含追溯信息: " + (result.getStderr() != null && !result.getStderr().isEmpty()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("--------------------------------------\n");
    }
}
```

### Spring Boot 集成

该库提供自动配置功能，可实现轻松集成。

**1. 配置 `application.properties`**

将您想要的配置添加到 `src/main/resources/application.properties` 文件中。

```properties
# =============================================
# ALIBABA LANGENGINE PYEXECUTOR 配置
# =============================================

# Python解释器的路径。如果不在系统PATH中，请使用绝对路径。
# 例如: /usr/bin/python3 或 C:/Python39/python.exe
ali.langengine.pyexecutor.python-bin=python3

# 全局执行超时时间 (例如: 10秒)
ali.langengine.pyexecutor.timeout=10s

# 禁用网络访问 (默认为true，为安全考虑强烈建议)
ali.langengine.pyexecutor.disable-networking=true

# 启用会话模式 (默认为true)
ali.langengine.pyexecutor.session-enabled=true

# 会话工作区的根目录 (默认为系统临时目录)
# ali.langengine.pyexecutor.workspace-root=/path/to/your/workspaces

# 最大并发会话数
ali.langengine.pyexecutor.session-max-count=20

# 会话在空闲15分钟后被驱逐
ali.langengine.pyexecutor.session-idle-ttl=15m
```

**2. 注入并使用 `PyExecutor`**

`PyExecutor` bean 将被自动创建。您可以直接将其注入到您的服务或组件中。

```java
@Service
public class MyAiService {

    private final PyExecutor pyExecutor;

    @Autowired
    public MyAiService(PyExecutor pyExecutor) {
        this.pyExecutor = pyExecutor;
    }

    public String executePythonForAi(String pythonCode) throws Exception {
        // 为每个用户或对话使用唯一的会话ID
        String sessionId = "user-conversation-abc-123";
        PyExecutionResult result = pyExecutor.execute(sessionId, pythonCode, null);

        if (result.getErrorRepr() != null) {
            return "错误: " + result.getErrorRepr();
        }
        return result.getLastValueRepr();
    }
}
```

## 配置详解

您可以使用 `PyExecutionPolicy`（在独立使用时）或通过 `application.properties`（在Spring Boot中）来微调执行环境。

| 属性 (`ali.langengine.pyexecutor.*`) | 默认值 | 描述 |
|------------------------------------------|---------|-------------|
| `python-bin`                             | `python3` | Python可执行文件的路径。 |
| `timeout`                                | `5s`    | 执行的最大物理时间。 |
| `cpu-time-seconds`                       | `2`     | (类UNIX系统) 最大CPU时间（秒）。会比物理时间先触发。 |
| `address-space-bytes`                    | `536870912` (512MB) | (类UNIX系统) 最大内存地址空间。 |
| `disable-networking`                     | `true`  | 若为true，则阻止所有与网络相关的系统调用。 |
| `disable-open`                           | `true`  | 若为true，则完全禁用 `open()` 内置函数。 |
| `allow-readonly-open`                    | `false` | 若 `disable-open` 为false，设为true可将 `open()` 限制在沙箱化的工作目录内的只读模式。 |
| `use-import-whitelist`                   | `true`  | 若为true，只有 `allowed-imports` 中的模块可被导入。若为false，则 `banned-imports` 中的模块被禁止。 |
| `allowed-imports`                        | (一组安全模块) | 白名单模式下允许的模块列表，以逗号分隔。 |
| `banned-imports`                         | (一组危险模块) | 黑名单模式下禁止的模块列表，以逗号分隔。 |
| `session-enabled`                        | `true`  | 启用或禁用会话执行模式 (`execute` 方法)。 |
| `session-max-count`                      | `50`    | 最大并发会话数。最久未使用的会话将被驱逐。 |
| `session-idle-ttl`                       | `10m`   | 非活动会话被驱逐前的持续时间。 |
| `session-hard-ttl`                       | `1h`    | 会话的最大生命周期，无论其活动状态如何。 |

## 工作原理

该库通过启动Python进程并通过标准流（`stdin`, `stdout`, `stderr`）与其通信。

1.  **引导脚本**: 动态生成一个特殊的Python“引导”脚本。该脚本负责设置安全沙箱（挂钩 `import`、`open` 等函数）并准备接收命令。
2.  **进程启动**:
    - 对于 `executeOnce`，每次调用都会启动一个运行引导脚本的新Python进程。
    - 对于 `execute`，会为每个会话启动并管理一个持久化的Python守护进程。
3.  **通信**: Java代码将用户脚本和选项作为JSON消息发送到Python进程的 `stdin`。
4.  **执行与响应**: Python引导脚本执行用户代码，捕获结果或任何错误，并将其作为结构化的JSON消息打印回 `stdout`。
5.  **结果解析**: Java库从进程的 `stdout` 读取JSON响应，将其解析为 `PyExecutionResult` 对象，并返回给调用者。

这种基于进程的沙箱模型提供了高级别的安全性，因为Python代码无法直接访问JVM的内存或资源。

## 许可证

本项目采用 **Apache License 2.0** 许可证。