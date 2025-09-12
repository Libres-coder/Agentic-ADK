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

package com.alibaba.langengine.pyexecutor;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Defines the global security and resource policies for Python execution.
 * <p>
 * This class encapsulates various settings that control the Python execution environment,
 * such as resource limits (CPU, memory), network access, file system access, and import
 * controls. These policies are applied globally but can be temporarily overridden for
 * specific calls via {@link PyExecutionOptions}.
 */
public class PyExecutionPolicy {

    private String pythonBin = System.getenv().getOrDefault("PYTHON_BIN", "python3");
    private Duration timeout = Duration.ofSeconds(5);
    private long maxStdoutBytes = 1_000_000;
    private long maxStderrBytes = 256_000;

    // Import control settings
    private boolean useImportWhitelist = true;
    private final Set<String> allowedImports = new LinkedHashSet<>();
    private final Set<String> bannedImports = new LinkedHashSet<>();
    private boolean blockDunderImports = true;

    // Resource limits (UNIX best-effort)
    private Integer cpuTimeSeconds = 2;
    private Long addressSpaceBytes = 512L * 1024 * 1024; // 512MB
    private Integer maxOpenFiles = 16;

    // Filesystem and network access control
    private boolean disableOpen = true;
    private boolean allowReadonlyOpen = false;
    private boolean disableNetworking = true;

    // Execution behavior settings
    private boolean printLastExpression = true;
    private boolean isolateSite = true; // Corresponds to Python's -I and -S flags

    /**
     * Constructs a PyExecutionPolicy with a default set of safe import rules.
     * By default, it uses a whitelist and allows common, safe modules while banning
     * modules that could pose a security risk.
     */
    public PyExecutionPolicy() {
        // A default list of allowed modules for whitelist mode.
        String[] allow = {
                "math", "statistics", "random", "re", "json", "itertools", "functools", "collections",
                "decimal", "fractions", "string", "textwrap", "heapq", "bisect", "array", "base64",
                "hashlib", "hmac", "uuid", "time", "datetime", "socket", "traceback"
        };
        for (String s : allow) {
            allowedImports.add(s);
        }

        // A default list of banned modules for blacklist mode.
        String[] ban = {
                "os", "sys", "subprocess", "pathlib", "shutil", "multiprocessing",
                "ctypes", "resource", "inspect", "builtins", "pickle"
        };
        for (String s : ban) {
            bannedImports.add(s);
        }
    }

    // =================================================================
    // Getters and Setters
    // =================================================================

    public String getPythonBin() {
        return pythonBin;
    }

    public void setPythonBin(String pythonBin) {
        this.pythonBin = pythonBin;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public long getMaxStdoutBytes() {
        return maxStdoutBytes;
    }

    public void setMaxStdoutBytes(long maxStdoutBytes) {
        this.maxStdoutBytes = maxStdoutBytes;
    }

    public long getMaxStderrBytes() {
        return maxStderrBytes;
    }

    public void setMaxStderrBytes(long maxStderrBytes) {
        this.maxStderrBytes = maxStderrBytes;
    }

    public boolean isUseImportWhitelist() {
        return useImportWhitelist;
    }

    public void setUseImportWhitelist(boolean useImportWhitelist) {
        this.useImportWhitelist = useImportWhitelist;
    }

    public Set<String> getAllowedImports() {
        return allowedImports;
    }

    public Set<String> getBannedImports() {
        return bannedImports;
    }

    public boolean isBlockDunderImports() {
        return blockDunderImports;
    }

    public void setBlockDunderImports(boolean blockDunderImports) {
        this.blockDunderImports = blockDunderImports;
    }

    public Integer getCpuTimeSeconds() {
        return cpuTimeSeconds;
    }

    public void setCpuTimeSeconds(Integer cpuTimeSeconds) {
        this.cpuTimeSeconds = cpuTimeSeconds;
    }

    public Long getAddressSpaceBytes() {
        return addressSpaceBytes;
    }

    public void setAddressSpaceBytes(Long addressSpaceBytes) {
        this.addressSpaceBytes = addressSpaceBytes;
    }

    public Integer getMaxOpenFiles() {
        return maxOpenFiles;
    }

    public void setMaxOpenFiles(Integer maxOpenFiles) {
        this.maxOpenFiles = maxOpenFiles;
    }

    public boolean isDisableOpen() {
        return disableOpen;
    }

    public void setDisableOpen(boolean disableOpen) {
        this.disableOpen = disableOpen;
    }

    public boolean isAllowReadonlyOpen() {
        return allowReadonlyOpen;
    }

    public void setAllowReadonlyOpen(boolean allowReadonlyOpen) {
        this.allowReadonlyOpen = allowReadonlyOpen;
    }

    public boolean isDisableNetworking() {
        return disableNetworking;
    }

    public void setDisableNetworking(boolean disableNetworking) {
        this.disableNetworking = disableNetworking;
    }

    public boolean isPrintLastExpression() {
        return printLastExpression;
    }

    public void setPrintLastExpression(boolean printLastExpression) {
        this.printLastExpression = printLastExpression;
    }

    public boolean isIsolateSite() {
        return isolateSite;
    }

    public void setIsolateSite(boolean isolateSite) {
        this.isolateSite = isolateSite;
    }
}