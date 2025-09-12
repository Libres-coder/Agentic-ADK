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

package com.alibaba.langengine.pyexecutor.spring;

import com.alibaba.langengine.pyexecutor.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Maps external configuration (e.g., from {@code application.properties}) to Python executor settings.
 * <p>
 * This class uses Spring Boot's {@code @ConfigurationProperties} to bind properties
 * under the prefix {@code ali.langengine.pyexecutor}. It provides a convenient way
 * to configure both the {@link PyExecutionPolicy} and {@link SessionConfig} from
 * the application's configuration files.
 */
@ConfigurationProperties(prefix = "ali.langengine.pyexecutor")
public class PyExecutorProperties {

    // Properties for PyExecutionPolicy
    private String pythonBin = "python3";
    private Duration timeout = Duration.ofSeconds(5);
    private long maxStdoutBytes = 1_000_000;
    private long maxStderrBytes = 256_000;
    private boolean useImportWhitelist = true;
    private Set<String> allowedImports = new LinkedHashSet<>();
    private Set<String> bannedImports = new LinkedHashSet<>();
    private boolean blockDunderImports = true;
    private Integer cpuTimeSeconds = 2;
    private Long addressSpaceBytes = 512L * 1024 * 1024;
    private Integer maxOpenFiles = 16;
    private boolean disableOpen = true;
    private boolean allowReadonlyOpen = false;
    private boolean disableNetworking = true;
    private boolean printLastExpression = true;
    private boolean isolateSite = true;

    // Properties for SessionConfig
    private boolean sessionEnabled = true;
    private int sessionMaxCount = 50;
    private Duration sessionIdleTtl = Duration.ofMinutes(10);
    private Duration sessionHardTtl = Duration.ofHours(1);
    private String workspaceRoot = System.getProperty("java.io.tmpdir");

    /**
     * Converts the loaded properties into a {@link PyExecutionPolicy} object.
     *
     * @return A new {@code PyExecutionPolicy} instance configured with the property values.
     */
    public PyExecutionPolicy toPolicy() {
        PyExecutionPolicy p = new PyExecutionPolicy();
        p.setPythonBin(pythonBin);
        p.setTimeout(timeout);
        p.setMaxStdoutBytes(maxStdoutBytes);
        p.setMaxStderrBytes(maxStderrBytes);
        p.setUseImportWhitelist(useImportWhitelist);
        p.getAllowedImports().clear();
        p.getAllowedImports().addAll(allowedImports);
        p.getBannedImports().clear();
        p.getBannedImports().addAll(bannedImports);
        p.setBlockDunderImports(blockDunderImports);
        p.setCpuTimeSeconds(cpuTimeSeconds);
        p.setAddressSpaceBytes(addressSpaceBytes);
        p.setMaxOpenFiles(maxOpenFiles);
        p.setDisableOpen(disableOpen);
        p.setAllowReadonlyOpen(allowReadonlyOpen);
        p.setDisableNetworking(disableNetworking);
        p.setPrintLastExpression(printLastExpression);
        p.setIsolateSite(isolateSite);
        return p;
    }

    /**
     * Converts the loaded properties into a {@link SessionConfig} object.
     *
     * @return A new {@code SessionConfig} instance configured with the property values.
     */
    public SessionConfig toSessionConfig() {
        return new SessionConfig()
                .setEnabled(sessionEnabled)
                .setMaxCount(sessionMaxCount)
                .setIdleTtl(sessionIdleTtl)
                .setHardTtl(sessionHardTtl)
                .setWorkspaceRoot(workspaceRoot);
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

    public void setAllowedImports(Set<String> allowedImports) {
        this.allowedImports = allowedImports;
    }

    public Set<String> getBannedImports() {
        return bannedImports;
    }

    public void setBannedImports(Set<String> bannedImports) {
        this.bannedImports = bannedImports;
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

    public boolean isSessionEnabled() {
        return sessionEnabled;
    }

    public void setSessionEnabled(boolean sessionEnabled) {
        this.sessionEnabled = sessionEnabled;
    }

    public int getSessionMaxCount() {
        return sessionMaxCount;
    }

    public void setSessionMaxCount(int sessionMaxCount) {
        this.sessionMaxCount = sessionMaxCount;
    }

    public Duration getSessionIdleTtl() {
        return sessionIdleTtl;
    }

    public void setSessionIdleTtl(Duration sessionIdleTtl) {
        this.sessionIdleTtl = sessionIdleTtl;
    }

    public Duration getSessionHardTtl() {
        return sessionHardTtl;
    }

    public void setSessionHardTtl(Duration sessionHardTtl) {
        this.sessionHardTtl = sessionHardTtl;
    }

    public String getWorkspaceRoot() {
        return workspaceRoot;
    }

    public void setWorkspaceRoot(String workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }
}