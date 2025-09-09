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

package com.alibaba.langengine.cxxexecutor.autoconfig;

import com.alibaba.langengine.cxxexecutor.Backend;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the CxxExecutor.
 * <p>
 * This class maps external configuration (e.g., from {@code application.properties}
 * or {@code application.yml}) to the CxxExecutor's settings under the prefix {@code langengine.cxx}.
 */
@ConfigurationProperties(prefix = "langengine.cxx")
public class CxxExecutionProperties {

    /**
     * The execution backend to use: WASI or NSJAIL. If not specified, it will be auto-detected.
     */
    private Backend backend;

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
    private String workDir = "/tmp/cxxexec";
    private String nsjailPath = "/usr/bin/nsjail";

    // Compilation Flags
    private List<String> extraCompileFlags = new ArrayList<>(List.of("-O2", "-std=c++17"));

    // Include Guard Patterns
    private List<String> allowedIncludePatterns = new ArrayList<>(List.of(
            "^<.*>$", "^\"[A-Za-z0-9_./-]+\"$"
    ));
    private List<String> deniedIncludePatterns = new ArrayList<>(List.of(
            "<dlfcn.h>", "<sys/socket.h>", "<sys/ptrace.h>", "<sys/mman.h>",
            "<linux/.*>", "<netinet/.*>", "<sys/shm.h>", "<semaphore.h>"
    ));

    // WASI-specific Configuration
    private Integer maxWasmStackBytes = 262144;

    // Health Check Configuration
    private Health health = new Health();

    /**
     * Nested configuration class for health check settings.
     */
    public static class Health {
        private boolean enabled = true;

        /**
         * Checks if the health check is enabled.
         * @return {@code true} if enabled, {@code false} otherwise.
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets whether the health check is enabled.
         * @param enabled {@code true} to enable, {@code false} to disable.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    // Getters and Setters

    /**
     * Gets the execution backend.
     * @return The configured {@link Backend}.
     */
    public Backend getBackend() {
        return backend;
    }

    /**
     * Sets the execution backend.
     * @param backend The {@link Backend} to use.
     */
    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    /**
     * Checks if network access is disabled.
     * @return {@code true} if network is disabled.
     */
    public boolean isDisableNetwork() {
        return disableNetwork;
    }

    /**
     * Sets whether to disable network access.
     * @param disableNetwork {@code true} to disable the network.
     */
    public void setDisableNetwork(boolean disableNetwork) {
        this.disableNetwork = disableNetwork;
    }

    /**
     * Gets the maximum size for standard output in bytes.
     * @return The max stdout size.
     */
    public long getMaxStdoutBytes() {
        return maxStdoutBytes;
    }

    /**
     * Sets the maximum size for standard output in bytes.
     * @param maxStdoutBytes The max stdout size.
     */
    public void setMaxStdoutBytes(long maxStdoutBytes) {
        this.maxStdoutBytes = maxStdoutBytes;
    }

    /**
     * Gets the maximum size for standard error in bytes.
     * @return The max stderr size.
     */
    public long getMaxStderrBytes() {
        return maxStderrBytes;
    }

    /**
     * Sets the maximum size for standard error in bytes.
     * @param maxStderrBytes The max stderr size.
     */
    public void setMaxStderrBytes(long maxStderrBytes) {
        this.maxStderrBytes = maxStderrBytes;
    }

    /**
     * Gets the compilation timeout in milliseconds.
     * @return The compile timeout.
     */
    public int getCompileTimeoutMs() {
        return compileTimeoutMs;
    }

    /**
     * Sets the compilation timeout in milliseconds.
     * @param compileTimeoutMs The compile timeout.
     */
    public void setCompileTimeoutMs(int compileTimeoutMs) {
        this.compileTimeoutMs = compileTimeoutMs;
    }

    /**
     * Gets the runtime timeout in milliseconds.
     * @return The run timeout.
     */
    public int getRunTimeoutMs() {
        return runTimeoutMs;
    }

    /**
     * Sets the runtime timeout in milliseconds.
     * @param runTimeoutMs The run timeout.
     */
    public void setRunTimeoutMs(int runTimeoutMs) {
        this.runTimeoutMs = runTimeoutMs;
    }

    /**
     * Gets the grace period for forceful termination in milliseconds.
     * @return The hard kill grace period.
     */
    public long getHardKillGraceMs() {
        return hardKillGraceMs;
    }

    /**
     * Sets the grace period for forceful termination in milliseconds.
     * @param hardKillGraceMs The hard kill grace period.
     */
    public void setHardKillGraceMs(long hardKillGraceMs) {
        this.hardKillGraceMs = hardKillGraceMs;
    }

    /**
     * Gets the path to the clang++ compiler.
     * @return The clang path.
     */
    public String getClangPath() {
        return clangPath;
    }

    /**
     * Sets the path to the clang++ compiler.
     * @param clangPath The clang path.
     */
    public void setClangPath(String clangPath) {
        this.clangPath = clangPath;
    }

    /**
     * Gets the path to the wasmtime runtime.
     * @return The wasmtime path.
     */
    public String getWasmtimePath() {
        return wasmtimePath;
    }

    /**
     * Sets the path to the wasmtime runtime.
     * @param wasmtimePath The wasmtime path.
     */
    public void setWasmtimePath(String wasmtimePath) {
        this.wasmtimePath = wasmtimePath;
    }

    /**
     * Gets the path to the WASI sysroot.
     * @return The WASI sysroot path.
     */
    public String getWasiSysroot() {
        return wasiSysroot;
    }

    /**
     * Sets the path to the WASI sysroot.
     * @param wasiSysroot The WASI sysroot path.
     */
    public void setWasiSysroot(String wasiSysroot) {
        this.wasiSysroot = wasiSysroot;
    }

    /**
     * Gets the base directory for temporary files.
     * @return The working directory path.
     */
    public String getWorkDir() {
        return workDir;
    }

    /**
     * Sets the base directory for temporary files.
     * @param workDir The working directory path.
     */
    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    /**
     * Gets the path to the nsjail executable.
     * @return The nsjail path.
     */
    public String getNsjailPath() {
        return nsjailPath;
    }

    /**
     * Sets the path to the nsjail executable.
     * @param nsjailPath The nsjail path.
     */
    public void setNsjailPath(String nsjailPath) {
        this.nsjailPath = nsjailPath;
    }

    /**
     * Gets the list of extra compiler flags.
     * @return A list of flags.
     */
    public List<String> getExtraCompileFlags() {
        return extraCompileFlags;
    }

    /**
     * Sets the list of extra compiler flags.
     * @param extraCompileFlags A list of flags.
     */
    public void setExtraCompileFlags(List<String> extraCompileFlags) {
        this.extraCompileFlags = extraCompileFlags;
    }

    /**
     * Gets the list of allowed include patterns (regex).
     * @return A list of allowed patterns.
     */
    public List<String> getAllowedIncludePatterns() {
        return allowedIncludePatterns;
    }

    /**
     * Sets the list of allowed include patterns (regex).
     * @param allowedIncludePatterns A list of allowed patterns.
     */
    public void setAllowedIncludePatterns(List<String> allowedIncludePatterns) {
        this.allowedIncludePatterns = allowedIncludePatterns;
    }

    /**
     * Gets the list of denied include patterns (regex).
     * @return A list of denied patterns.
     */
    public List<String> getDeniedIncludePatterns() {
        return deniedIncludePatterns;
    }

    /**
     * Sets the list of denied include patterns (regex).
     * @param deniedIncludePatterns A list of denied patterns.
     */
    public void setDeniedIncludePatterns(List<String> deniedIncludePatterns) {
        this.deniedIncludePatterns = deniedIncludePatterns;
    }

    /**
     * Gets the maximum WASM stack size in bytes.
     * @return The max WASM stack size.
     */
    public Integer getMaxWasmStackBytes() {
        return maxWasmStackBytes;
    }

    /**
     * Sets the maximum WASM stack size in bytes.
     * @param maxWasmStackBytes The max WASM stack size.
     */
    public void setMaxWasmStackBytes(Integer maxWasmStackBytes) {
        this.maxWasmStackBytes = maxWasmStackBytes;
    }

    /**
     * Gets the health check configuration.
     * @return The health check settings.
     */
    public Health getHealth() {
        return health;
    }

    /**
     * Sets the health check configuration.
     * @param health The health check settings.
     */
    public void setHealth(Health health) {
        this.health = health;
    }
}