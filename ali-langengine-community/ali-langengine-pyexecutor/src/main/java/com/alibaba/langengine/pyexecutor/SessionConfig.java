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

/**
 * Configuration for the session management feature.
 * <p>
 * This class holds settings related to whether sessions are enabled, their maximum
 * count, idle and hard time-to-live (TTL), and the root directory for session workspaces.
 * It uses a fluent builder pattern for easy configuration.
 */
public class SessionConfig {

    private boolean enabled = true;
    private int maxCount = 50;
    private Duration idleTtl = Duration.ofMinutes(10);
    private Duration hardTtl = Duration.ofHours(1);
    private String workspaceRoot = System.getProperty("java.io.tmpdir");

    // =================================================================
    // Getters and Setters
    // =================================================================

    /**
     * Checks if session mode is enabled.
     *
     * @return true if enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether session mode is enabled.
     *
     * @param enabled true to enable, false to disable.
     * @return this {@code SessionConfig} instance for chaining.
     */
    public SessionConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Gets the maximum number of concurrent sessions allowed.
     *
     * @return The maximum session count.
     */
    public int getMaxCount() {
        return maxCount;
    }

    /**
     * Sets the maximum number of concurrent sessions to maintain.
     *
     * @param maxCount The maximum session count.
     * @return this {@code SessionConfig} instance for chaining.
     */
    public SessionConfig setMaxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    /**
     * Gets the idle time-to-live (TTL) for a session.
     * A session that is idle for longer than this duration may be evicted.
     *
     * @return The idle TTL duration.
     */
    public Duration getIdleTtl() {
        return idleTtl;
    }

    /**
     * Sets the idle time-to-live (TTL) for a session.
     *
     * @param idleTtl The idle TTL duration.
     * @return this {@code SessionConfig} instance for chaining.
     */
    public SessionConfig setIdleTtl(Duration idleTtl) {
        this.idleTtl = idleTtl;
        return this;
    }

    /**
     * Gets the hard time-to-live (TTL) for a session.
     * A session will be evicted after this duration, regardless of activity.
     *
     * @return The hard TTL duration.
     */
    public Duration getHardTtl() {
        return hardTtl;
    }

    /**
     * Sets the hard time-to-live (TTL) for a session.
     *
     * @param hardTtl The hard TTL duration.
     * @return this {@code SessionConfig} instance for chaining.
     */
    public SessionConfig setHardTtl(Duration hardTtl) {
        this.hardTtl = hardTtl;
        return this;
    }

    /**
     * Gets the root directory where session workspaces will be created.
     *
     * @return The path to the workspace root directory.
     */
    public String getWorkspaceRoot() {
        return workspaceRoot;
    }

    /**
     * Sets the root directory for session workspaces.
     *
     * @param workspaceRoot The path to the workspace root directory.
     * @return this {@code SessionConfig} instance for chaining.
     */
    public SessionConfig setWorkspaceRoot(String workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
        return this;
    }
}