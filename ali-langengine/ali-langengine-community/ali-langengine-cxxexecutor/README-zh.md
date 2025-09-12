# C/C++ 安全执行引擎 (AI 插件)

本项目是一个为 AI 应用设计的安全执行引擎，用于安全地编译和运行来自不可信来源的 C/C++ 代码。它作为一个插件，利用现代沙箱技术来隔离代码执行，防止对宿主系统造成潜在危害，并为 AI 应用提供了通过原生代码性能进行能力扩展的可靠途径。

## 功能特性

* **双后端支持**: 可在两种强大的沙箱后端中进行选择：
    * **WASI (WebAssembly System Interface)**: 将 C/C++ 代码编译为 WebAssembly，以实现一个安全的、跨平台的模型。它在 Linux 和 macOS 上均可工作。
    * **NsJail**: 在 Linux 上利用 Google 的进程隔离工具，实现强有力的沙箱隔离。**(仅支持 Linux)**
* **资源限制**: 可对编译时间、执行时间以及 `stdout` 和 `stderr` 的输出大小进行严格限制，有效防止资源耗尽和拒绝服务攻击。
* **安全优先**: 默认禁用网络访问，并使用可配置的 `#include` 指令白名单/黑名单，以阻止对高危头文件（如 `<sys/socket.h>` 或 `<dlfcn.h>`）的访问。
* **Spring Boot 集成**: 提供无缝的自动配置功能，可以轻松集成到 Spring Boot 应用中。只需添加依赖并在 `application.properties` 中进行配置即可。
* **动态配置**: 所有主要参数，包括工具链路径、超时时间和资源限制，都可通过外部属性文件进行配置，实现了灵活部署。

## 工作原理

引擎遵循一个安全的多阶段流程来处理每一次执行请求：

1.  **预检**: 首先对源代码进行扫描，所有的 `#include` 指令都会根据可配置的白名单/黑名单进行校验。
2.  **隔离编译**: 源代码被写入一个临时的、隔离的工作目录中。然后，使用所选后端的工具链（WASI 后端使用 `clang`，NsJail 后端使用原生 `clang++`）进行编译。
3.  **沙箱执行**: 编译后的产物（`.wasm` 模块或原生可执行文件）将在指定的沙箱环境（Wasmtime 或 NsJail）中运行，并严格执行所有资源限制和安全策略。
4.  **结果捕获**: 捕获程序的 `stdout`、`stderr` 和退出码。如果输出内容超出配置的限制，将被截断。
5.  **清理**: 执行完毕后，临时目录及所有编译产物都将被安全地删除。

## 环境准备

本引擎依赖外部工具链来进行沙箱化和编译。您必须为您计划使用的后端安装相应的依赖。

### 1. WASI 后端依赖 (适用于 Linux & macOS)

#### WASI-SDK

WASI-SDK 提供了将 C/C++ 编译为 WebAssembly 所需的 `clang` 编译器。

```sh
# 推荐下载官方预编译包 (推荐版本 22.0)
wget https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-22/wasi-sdk-22.0-linux.tar.gz

# 解压压缩包
tar -xzf wasi-sdk-22.0-linux.tar.gz

# 将其移动到一个标准位置，例如 /opt
sudo mv wasi-sdk-22.0 /opt/wasi-sdk

# 设置环境变量，以便应用程序可以找到 SDK
# 建议将此行添加到您的 ~/.bashrc 或 ~/.zshrc 文件中
export WASI_SDK_PATH=/opt/wasi-sdk
```

#### Wasmtime

Wasmtime 是用于执行已编译的 WebAssembly 模块的运行时。

```sh
# 运行官方的 Linux/macOS 安装脚本
curl https://wasmtime.dev/install.sh -sSf | bash

# 该脚本通常会将 wasmtime 安装到 ~/.wasmtime 目录
# 设置环境变量，以便应用程序可以找到该运行时
# 建议将此行添加到您的 ~/.bashrc 或 ~/.zshrc 文件中
export WASMTIME_PATH=$HOME/.wasmtime/bin/wasmtime
```

### 2. NsJail 后端依赖 (仅适用于 Linux)

NsJail 必须从源代码编译。这需要 `make` 和标准的 C++ 构建工具 (如 `g++` 等)。

```sh
# 克隆官方仓库
git clone https://github.com/google/nsjail.git
cd nsjail

# 编译源代码
make

# 将可执行文件复制到您系统的 PATH 路径下
sudo cp nsjail /usr/local/bin/
```

## 配置说明

在集成到 Spring Boot 项目后，您可以在 `application.properties` 或 `application.yml` 文件中配置本引擎。

```properties
# 选择后端: "WASI" 或 "NSJAIL"。如果留空，引擎将自动检测。
langengine.cxx.backend=WASI

# --- 资源限制 ---
# 编译超时时间 (毫秒)
langengine.cxx.compile-timeout-ms=8000
# 执行超时时间 (毫秒)
langengine.cxx.run-timeout-ms=3000
# 最大标准输出大小 (字节)
langengine.cxx.max-stdout-bytes=65536
# 最大标准错误大小 (字节)
langengine.cxx.max-stderr-bytes=16384

# --- 工具链路径 (如果未通过环境变量设置) ---
# 这些是可选配置，会覆盖默认值或环境变量。
# langengine.cxx.clang-path=/opt/wasi-sdk/bin/clang++
# langengine.cxx.wasmtime-path=/home/user/.wasmtime/bin/wasmtime
# langengine.cxx.nsjail-path=/usr/bin/nsjail
```

## 使用示例

以下是一个如何在您的服务中使用 `CxxExecutor` Bean 的基本示例。

```java
import com.alibaba.langengine.cxxexecutor.CxxExecutor;
import com.alibaba.langengine.cxxexecutor.CxxExecutionOptions;
import com.alibaba.langengine.cxxexecutor.CxxExecutionPolicy;
import com.alibaba.langengine.cxxexecutor.CxxExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeExecutionService {

    @Autowired
    private CxxExecutor cxxExecutor;

    @Autowired
    private CxxExecutionPolicy defaultPolicy; // 从 application.properties 自动配置的策略

    public CxxExecutionResult runCppCode(String sourceCode) {
        // 使用传入的源代码和 application.properties 中的默认策略来构建执行选项
        CxxExecutionOptions options = CxxExecutionOptions.builder()
                .code(sourceCode)
                .isCpp(true)
                .policy(defaultPolicy)
                .build();

        // 执行代码
        CxxExecutionResult result = cxxExecutor.executeOnce(options);

        // 处理结果
        if (result.isOk()) {
            System.out.println("执行成功!");
            System.out.println("输出:\n" + result.getStdout());
        } else {
            System.err.println("执行失败，失败阶段: " + result.getPhase());
            System.err.println("错误信息:\n" + result.getStderr());
        }

        return result;
    }
}
```

## 注意事项：为 WASI 进行编译

当为 WASI 目标编译 C++ 代码时，您可能会遇到与 C++ 异常相关的 `undefined symbol` 链接错误，例如 `__cxa_throw` 或 `__cxa_allocate_exception`。

**问题解释**:
默认情况下，即使您的代码中没有显式使用 `throw`，C++ 标准库（被 `iostream`, `vector` 等使用）在编译时也开启了异常支持。因此，它会引用与异常处理相关的符号。在标准的 Linux 系统上，这些符号由 `libc++abi` 等库提供，并会自动链接。

然而，多数版本的 WASI-SDK 中包含的 `libc++` 库本身是使用 `-fno-exceptions` 标志编译的，这意味着异常处理相关的符号**并未被包含**在库中。当您的代码尝试链接这个版本的 `libc++` 时，链接器找不到所需的异常符号，从而导致链接失败。

**解决方案**:
要解决这个问题，您必须在编译您的代码时也添加 `-fno-exceptions` 标志。这可以确保您的代码与标准库的编译选项保持一致，从而在链接阶段不会引用缺失的符号。

您可以在 `application.properties` 中全局配置此标志：

```properties
# 将 -fno-exceptions 添加到编译器标志列表中
langengine.cxx.extra-compile-flags=-O2, -std=c++17, -fno-exceptions
```

## 开源许可

本项目基于 Apache License 2.0 许可证开源。