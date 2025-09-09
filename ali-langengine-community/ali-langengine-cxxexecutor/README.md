# C/C++ Secure Execution Engine for AI

A secure execution engine designed for AI applications to safely compile and run untrusted C/C++ code. This project acts as a plugin, leveraging modern sandboxing technologies to isolate code execution, prevent potential harm to the host system, and provide a reliable way to extend AI capabilities with native code performance.

## Features

* **Dual-Backend Support**: Choose between two powerful sandboxing backends:
    * **WASI (WebAssembly System Interface)**: Compiles C/C++ to WebAssembly for a secure, cross-platform, and capability-based security model. Works on Linux and macOS.
    * **NsJail**: Utilizes Google's process isolation tool on Linux for strong, kernel-level sandboxing using namespaces and cgroups. **(Linux Only)**
* **Resource Limiting**: Enforce strict limits on execution time, compilation time, and the size of `stdout` and `stderr` to prevent resource exhaustion and denial-of-service attacks.
* **Security First**: Disables networking by default and uses a configurable allow/deny list for `#include` directives to block access to potentially dangerous headers like `<sys/socket.h>` or `<dlfcn.h>`.
* **Spring Boot Integration**: Provides seamless auto-configuration for easy integration into Spring Boot applications. Simply include the dependency and configure it in your `application.properties`.
* **Dynamic Configuration**: All major parameters, including toolchain paths, timeouts, and resource limits, are configurable through external properties, allowing for flexible deployment.

## How It Works

The engine follows a secure, multi-stage process to handle each execution request:

1.  **Pre-flight Check**: The source code is first scanned. Any `#include` directives are validated against a configurable allow/deny list.
2.  **Isolated Compilation**: The code is written to a temporary, isolated directory. It is then compiled using the selected backend's toolchain (`clang` for WASI, native `clang++` for NsJail).
3.  **Sandboxed Execution**: The compiled artifact (`.wasm` module or native executable) is run within the chosen sandboxed environment (Wasmtime or NsJail), with all resource limits and security policies strictly enforced.
4.  **Result Capture**: `stdout`, `stderr`, and the exit code are captured. If the output exceeds the configured limits, it is truncated.
5.  **Cleanup**: The temporary directory and all artifacts are securely deleted after execution.

## Prerequisites

This engine relies on external toolchains for sandboxing and compilation. You must install the required dependencies for the backend you intend to use.

### 1. WASI Backend Dependencies (Linux & macOS)

#### WASI-SDK

The WASI-SDK provides the `clang` compiler needed to compile C/C++ to WebAssembly.

```sh
# Download the official pre-compiled package (version 22.0 is recommended)
wget https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-22/wasi-sdk-22.0-linux.tar.gz

# Extract the archive
tar -xzf wasi-sdk-22.0-linux.tar.gz

# Move it to a standard location like /opt
sudo mv wasi-sdk-22.0 /opt/wasi-sdk

# Set the environment variable for the application to find the SDK
# It's recommended to add this line to your ~/.bashrc or ~/.zshrc file
export WASI_SDK_PATH=/opt/wasi-sdk
```

#### Wasmtime

Wasmtime is the runtime used to execute the compiled WebAssembly modules.

```sh
# Run the official installation script for Linux/macOS
curl https://wasmtime.dev/install.sh -sSf | bash

# The script typically installs wasmtime to ~/.wasmtime.
# Set the environment variable for the application to find the runtime.
# It's recommended to add this line to your ~/.bashrc or ~/.zshrc file
export WASMTIME_PATH=$HOME/.wasmtime/bin/wasmtime
```

### 2. NsJail Backend Dependency (Linux Only)

NsJail must be compiled from source. This requires `make` and standard C++ build tools (`g++`, etc.).

```sh
# Clone the official repository
git clone https://github.com/google/nsjail.git
cd nsjail

# Compile the source code
make

# Copy the executable to a location in your system's PATH
sudo cp nsjail /usr/bin/
```

## Configuration

Once integrated into a Spring Boot project, you can configure the engine in your `application.properties` or `application.yml` file.

```properties
# Choose the backend: "WASI" or "NSJAIL". If omitted, the engine will auto-detect.
langengine.cxx.backend=WASI

# --- Resource Limits ---
# Compilation timeout in milliseconds
langengine.cxx.compile-timeout-ms=8000
# Execution timeout in milliseconds
langengine.cxx.run-timeout-ms=3000
# Max stdout size in bytes
langengine.cxx.max-stdout-bytes=65536
# Max stderr size in bytes
langengine.cxx.max-stderr-bytes=16384

# --- Toolchain Paths (if not set via environment variables) ---
# These are optional and override the defaults or environment variables.
# langengine.cxx.clang-path=/opt/wasi-sdk/bin/clang++
# langengine.cxx.wasmtime-path=/home/user/.wasmtime/bin/wasmtime
# langengine.cxx.nsjail-path=/usr/bin/nsjail
```

## Usage Example

Here is a basic example of how to use the `CxxExecutor` bean in your service.

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
    private CxxExecutionPolicy defaultPolicy; // The auto-configured policy

    public CxxExecutionResult runCppCode(String sourceCode) {
        // Build the execution options using the provided source code
        // and the default policy from your application properties.
        CxxExecutionOptions options = CxxExecutionOptions.builder()
                .code(sourceCode)
                .isCpp(true)
                .policy(defaultPolicy)
                .build();

        // Execute the code
        CxxExecutionResult result = cxxExecutor.executeOnce(options);

        // Process the result
        if (result.isOk()) {
            System.out.println("Execution successful!");
            System.out.println("Output:\n" + result.getStdout());
        } else {
            System.err.println("Execution failed in phase: " + result.getPhase());
            System.err.println("Error:\n" + result.getStderr());
        }

        return result;
    }
}
```

## Important Notes: Compiling for WASI

When compiling C++ code for the WASI target, you may encounter `undefined symbol` errors related to exceptions, such as `__cxa_throw` or `__cxa_allocate_exception`.

**Explanation**:
By default, even if your code doesn't explicitly use `throw`, the C++ standard library (used by `iostream`, `vector`, etc.) is compiled with exception support. It therefore references symbols related to exception handling. On a standard Linux system, these symbols are provided by libraries like `libc++abi` and are linked automatically.

However, the `libc++` included in most versions of the WASI-SDK is itself compiled with the `-fno-exceptions` flag, meaning the exception-handling symbols are **not included** in the library. When your code tries to link against this version of `libc++`, the linker cannot find the required exception symbols and fails.

**Solution**:
To resolve this, you must compile your code with the `-fno-exceptions` flag. This ensures that your code and the standard library are aligned, and no missing symbols will be referenced during the linking phase.

You can configure this globally in `application.properties`:

```properties
# Add -fno-exceptions to the list of compiler flags
langengine.cxx.extra-compile-flags=-O2, -std=c++17, -fno-exceptions
```

## License

This project is licensed under the Apache License 2.0.