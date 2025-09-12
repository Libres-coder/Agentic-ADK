/*
 * Copyright 2024 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.cxxexecutor;

/**
 * Defines the policy for C++ code execution, including security constraints,
 * resource limits, and toolchain configurations.
 */
public class CxxExecutionPolicy {

    private Backend backend = Backend.WASI;

    // Security and Resource Limits
    private boolean disableNetwork = true;
    private long maxStdoutBytes = 64 * 1024;
    private long maxStderrBytes = 16 * 1024;
    private int compileTimeoutMs = 8000;
    private int runTimeoutMs = 3000;
    private long hardKillGraceMs = 300;

    // Toolchain Paths
    private String clangPath = "clang++";
    private String wasmtimePath = "wasmtime";
    private String wasiSysroot = "";

    // Working Directory
    private String workDir = "/tmp/cxxexec";

    // NsJail-specific Configuration
    private String nsjailPath = "/usr/bin/nsjail";

    // Compilation Flags
    private String[] extraCompileFlags = new String[]{"-O2", "-std=c++17"};

    // Include Guard Patterns
    private String[] allowedIncludePatterns = new String[]{
            "^<.*>$",
            "^\"[A-Za-z0-9_./-]+\"$"
    };
    private String[] deniedIncludePatterns = new String[]{
            "<dlfcn.h>", "<sys/socket.h>", "<sys/ptrace.h>", "<sys/mman.h>",
            "<linux/.*>", "<netinet/.*>", "<sys/shm.h>", "<semaphore.h>"
    };

    // WASI-specific Runtime Configuration
    private Integer maxWasmStackBytes = 262144; // 256 KiB

    /**
     * Gets the execution backend.
     *
     * @return The currently configured {@link Backend}.
     */
    public Backend getBackend() {
        return backend;
    }

    /**
     * Sets the execution backend.
     *
     * @param backend The {@link Backend} to use.
     */
    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    /**
     * Checks if network access is disabled.
     * For WASI, networking is unavailable by default.
     * For NsJail, isolation is achieved via a new network namespace.
     *
     * @return {@code true} if network is disabled, {@code false} otherwise.
     */
    public boolean isDisableNetwork() {
        return disableNetwork;
    }

    /**
     * Sets whether to disable network access.
     *
     * @param disableNetwork {@code true} to disable the network, {@code false} to enable it.
     */
    public void setDisableNetwork(boolean disableNetwork) {
        this.disableNetwork = disableNetwork;
    }

    /**
     * Gets the maximum allowed size for standard output in bytes.
     *
     * @return The maximum stdout size in bytes.
     */
    public long getMaxStdoutBytes() {
        return maxStdoutBytes;
    }

    /**
     * Sets the maximum allowed size for standard output in bytes.
     *
     * @param maxStdoutBytes The maximum stdout size in bytes.
     */
    public void setMaxStdoutBytes(long maxStdoutBytes) {
        this.maxStdoutBytes = maxStdoutBytes;
    }

    /**
     * Gets the maximum allowed size for standard error in bytes.
     *
     * @return The maximum stderr size in bytes.
     */
    public long getMaxStderrBytes() {
        return maxStderrBytes;
    }

    /**
     * Sets the maximum allowed size for standard error in bytes.
     *
     * @param maxStderrBytes The maximum stderr size in bytes.
     */
    public void setMaxStderrBytes(long maxStderrBytes) {
        this.maxStderrBytes = maxStderrBytes;
    }

    /**
     * Gets the timeout for the compilation phase in milliseconds.
     *
     * @return The compilation timeout in milliseconds.
     */
    public int getCompileTimeoutMs() {
        return compileTimeoutMs;
    }

    /**
     * Sets the timeout for the compilation phase in milliseconds.
     *
     * @param compileTimeoutMs The compilation timeout in milliseconds.
     */
    public void setCompileTimeoutMs(int compileTimeoutMs) {
        this.compileTimeoutMs = compileTimeoutMs;
    }

    /**
     * Gets the timeout for the execution phase in milliseconds.
     *
     * @return The execution timeout in milliseconds.
     */
    public int getRunTimeoutMs() {
        return runTimeoutMs;
    }

    /**
     * Sets the timeout for the execution phase in milliseconds.
     *
     * @param runTimeoutMs The execution timeout in milliseconds.
     */
    public void setRunTimeoutMs(int runTimeoutMs) {
        this.runTimeoutMs = runTimeoutMs;
    }

    /**
     * Gets the grace period in milliseconds before a forceful termination (e.g., kill -9)
     * is issued after a timeout.
     *
     * @return The grace period in milliseconds.
     */
    public long getHardKillGraceMs() {
        return hardKillGraceMs;
    }

    /**
     * Sets the grace period in milliseconds for forceful termination after a timeout.
     *
     * @param hardKillGraceMs The grace period in milliseconds.
     */
    public void setHardKillGraceMs(long hardKillGraceMs) {
        this.hardKillGraceMs = hardKillGraceMs;
    }

    /**
     * Gets the path to the clang++ compiler.
     * This can be the system's default clang++ or one provided by a toolchain like the WASI SDK.
     *
     * @return The path to the clang++ executable.
     */
    public String getClangPath() {
        return clangPath;
    }

    /**
     * Sets the path to the clang++ compiler.
     *
     * @param clangPath The path to the clang++ executable.
     */
    public void setClangPath(String clangPath) {
        this.clangPath = clangPath;
    }

    /**
     * Gets the path to the wasmtime executable.
     *
     * @return The path to the wasmtime executable.
     */
    public String getWasmtimePath() {
        return wasmtimePath;
    }

    /**
     * Sets the path to the wasmtime executable.
     *
     * @param wasmtimePath The path to the wasmtime executable.
     */
    public void setWasmtimePath(String wasmtimePath) {
        this.wasmtimePath = wasmtimePath;
    }

    /**
     * Gets the path to the WASI sysroot directory.
     * If empty, the system default will be used.
     *
     * @return The path to the WASI sysroot.
     */
    public String getWasiSysroot() {
        return wasiSysroot;
    }

    /**
     * Sets the path to the WASI sysroot directory.
     *
     * @param wasiSysroot The path to the WASI sysroot.
     */
    public void setWasiSysroot(String wasiSysroot) {
        this.wasiSysroot = wasiSysroot;
    }

    /**
     * Gets the root directory for temporary execution files.
     *
     * @return The working directory path.
     */
    public String getWorkDir() {
        return workDir;
    }

    /**
     * Sets the root directory for temporary execution files.
     *
     * @param workDir The working directory path.
     */
    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    /**
     * Gets the path to the nsjail executable.
     *
     * @return The path to the nsjail executable.
     */
    public String getNsjailPath() {
        return nsjailPath;
    }

    /**
     * Sets the path to the nsjail executable.
     *
     * @param nsjailPath The path to the nsjail executable.
     */
    public void setNsjailPath(String nsjailPath) {
        this.nsjailPath = nsjailPath;
    }

    /**
     * Gets the extra flags to be passed to the compiler.
     *
     * @return An array of extra compiler flags.
     */
    public String[] getExtraCompileFlags() {
        return extraCompileFlags;
    }

    /**
     * Sets the extra flags to be passed to the compiler.
     *
     * @param extraCompileFlags An array of extra compiler flags.
     */
    public void setExtraCompileFlags(String[] extraCompileFlags) {
        this.extraCompileFlags = extraCompileFlags;
    }

    /**
     * Gets the whitelist of regular expression patterns for allowed #include directives.
     *
     * @return An array of allowed patterns.
     */
    public String[] getAllowedIncludePatterns() {
        return allowedIncludePatterns;
    }

    /**
     * Sets the whitelist of regular expression patterns for allowed #include directives.
     *
     * @param allowedIncludePatterns An array of allowed patterns.
     */
    public void setAllowedIncludePatterns(String[] allowedIncludePatterns) {
        this.allowedIncludePatterns = allowedIncludePatterns;
    }

    /**
     * Gets the blacklist of regular expression patterns for denied #include directives.
     *
     * @return An array of denied patterns.
     */
    public String[] getDeniedIncludePatterns() {
        return deniedIncludePatterns;
    }

    /**
     * Sets the blacklist of regular expression patterns for denied #include directives.
     *
     * @param deniedIncludePatterns An array of denied patterns.
     */
    public void setDeniedIncludePatterns(String[] deniedIncludePatterns) {
        this.deniedIncludePatterns = deniedIncludePatterns;
    }

    /**
     * Gets the maximum stack size for the WASM guest in bytes.
     *
     * @return The maximum WASM stack size in bytes.
     */
    public Integer getMaxWasmStackBytes() {
        return maxWasmStackBytes;
    }

    /**
     * Sets the maximum stack size for the WASM guest in bytes.
     *
     * @param maxWasmStackBytes The maximum WASM stack size in bytes.
     */
    public void setMaxWasmStackBytes(Integer maxWasmStackBytes) {
        this.maxWasmStackBytes = maxWasmStackBytes;
    }
}