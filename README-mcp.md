**"[中文版说明](https://github.com/AIDC-AI/Agentic-ADK/blob/main/README-mcp.md)"**
# ali-langengine-mcp-jdk8

## Project Overview

`ali-langengine-mcp-jdk8` is an independent module of Alibaba's LangEngine project that provides a JDK 8 compatible implementation of the Model Context Protocol (MCP). This module enables interaction with AI models using the MCP protocol in JDK 8 environments.

## Version Management

This module adopts an independent versioning strategy and does not maintain consistency with the main `ali-langengine` project version. This is because:

1. This module is a completely independent component that doesn't depend on other parts of the main project
2. Independent versioning allows this module to evolve at its own pace
3. Better adherence to semantic versioning principles
4. Provides clearer dependency relationships for projects that depend on this module
5. Facilitates long-term maintenance for JDK 8 environments

Current Version: `1.0.1-SNAPSHOT`

## Features

- Full compatibility with JDK 8 environment
- Implementation of Model Context Protocol (MCP) client
- Support for synchronous and asynchronous APIs
- Support for multiple transport methods (stdin/stdout, SSE, etc.)
- Tool discovery and invocation capabilities
- Resource access and management support
- Prompt template processing support
- Real-time update support
- Structured logging support

## Usage

### Maven Dependency

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>ali-langengine-mcp-jdk8</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

### Basic Usage

```java
// Create synchronous client
McpSyncClient client = McpClient.createSync(
    StdioClientTransport.create(
        ServerParameters.builder("path/to/server")
            .arg("--some-arg")
            .build()
    )
);

// Initialize client
client.initialize();

// Get available tools list
List<Tool> tools = client.listTools();

// Call tool
CallToolResult result = client.callTool("tool-name", params);
```

## Debugging Guide

For detailed debugging information, please refer to [README-DEBUG.md](README-DEBUG.md).

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

## Repository Configuration

This module uses GitHub Package Registry for publishing:

```xml
<distributionManagement>
    <repository>
        <id>github</id>
        <name>GitHub AIDC-AI Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/AIDC-AI/ali-langengine</url>
    </repository>
    <snapshotRepository>
        <id>github</id>
        <name>GitHub AIDC-AI Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/AIDC-AI/ali-langengine</url>
    </snapshotRepository>
</distributionManagement>
```

## Contributing

We welcome Pull Requests and Issues to help improve this project. Before submitting code, please ensure:

1. Code follows the project's coding standards
2. Appropriate unit tests have been added
3. All tests pass
4. Relevant documentation has been updated

## Contact

For questions or suggestions, please contact us through GitHub Issues.