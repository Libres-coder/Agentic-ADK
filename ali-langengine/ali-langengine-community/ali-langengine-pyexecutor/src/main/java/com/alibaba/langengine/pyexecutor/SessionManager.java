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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle of Python daemon sessions.
 * <p>
 * This class is responsible for creating, reusing, evicting (based on TTL and count),
 * and closing {@link DaemonSession} instances. It ensures that sessions are managed
 * efficiently and according to the configured policies. All operations that modify
 * the session map are synchronized to ensure thread safety.
 */
public class SessionManager {

    private final Map<String, DaemonSession> sessions = new ConcurrentHashMap<>();
    private final SessionConfig config;
    private final PyExecutionPolicy policy;

    /**
     * Constructs a new SessionManager.
     *
     * @param policy The global execution policy to be applied to all sessions.
     * @param config The configuration for session management (e.g., TTL, max count).
     * If null, a default configuration is used.
     */
    public SessionManager(PyExecutionPolicy policy, SessionConfig config) {
        this.policy = policy;
        this.config = (config == null ? new SessionConfig() : config);
    }

    /**
     * Retrieves an existing session or creates a new one if it doesn't exist.
     * <p>
     * A session will be recreated if the underlying process is dead, or if its
     * idle or hard time-to-live (TTL) has expired. This method is synchronized
     * to prevent race conditions during session creation and validation.
     *
     * @param sessionId The unique identifier for the session.
     * @return A valid, running {@link DaemonSession}.
     * @throws Exception if a new session fails to start.
     */
    public synchronized DaemonSession getOrCreate(String sessionId) throws Exception {
        DaemonSession s = sessions.get(sessionId);
        long now = System.currentTimeMillis();

        if (s != null) {
            boolean dead = !s.isAlive();

            // Check for idle timeout.
            boolean idleExpired = false;
            if (config.getIdleTtl() != null && s.getLastUsedEpochMs() > 0) {
                // Use >= for robustness.
                idleExpired = (now - s.getLastUsedEpochMs()) >= config.getIdleTtl().toMillis();
            }

            // Check for hard timeout (absolute lifetime).
            boolean hardExpired = false;
            if (config.getHardTtl() != null) {
                hardExpired = (now - s.getCreationEpochMs()) >= config.getHardTtl().toMillis();
            }

            // If the session is dead or has expired, shut it down and remove it.
            if (dead || idleExpired || hardExpired) {
                try {
                    s.shutdown();
                } catch (Exception ignore) {
                }
                sessions.remove(sessionId);
                s = null;
            }
        }

        // If no valid session exists, create a new one.
        if (s == null) {
            Path sessionCwd = Paths.get(config.getWorkspaceRoot()).resolve(sessionId);
            evictIfNeeded(); // Make space if the session limit is reached.
            DaemonSession ns = new DaemonSession();
            ns.start(policy, sessionCwd);
            sessions.put(sessionId, ns);
            s = ns;
        }
        return s;
    }

    /**
     * Closes and terminates a specific session by its ID.
     * This method is synchronized to ensure safe removal from the session map.
     *
     * @param sessionId The ID of the session to close.
     */
    public synchronized void close(String sessionId) {
        DaemonSession s = sessions.remove(sessionId);
        if (s != null) {
            try {
                s.shutdown();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Closes and terminates all active sessions managed by this manager.
     * This method is synchronized.
     */
    public synchronized void closeAll() {
        for (Map.Entry<String, DaemonSession> e : sessions.entrySet()) {
            try {
                e.getValue().shutdown();
            } catch (Exception ignore) {
            }
        }
        sessions.clear();
    }

    /**
     * Evicts sessions if the number of active sessions exceeds the configured maximum.
     * The eviction strategy is Least Recently Used (LRU), based on the `lastUsedEpochMs`
     * timestamp of each session. This method should be called before creating a new session.
     */
    private void evictIfNeeded() {
        Integer max = config.getMaxCount();
        if (max == null || max <= 0) {
            return; // No limit.
        }
        while (sessions.size() >= max) {
            // Find the least recently used session to evict.
            String victimId = null;
            long oldest = Long.MAX_VALUE;
            for (Map.Entry<String, DaemonSession> e : sessions.entrySet()) {
                long ts = e.getValue().getLastUsedEpochMs();
                if (ts < oldest) {
                    oldest = ts;
                    victimId = e.getKey();
                }
            }
            if (victimId == null) {
                break; // Should not happen in a non-empty map.
            }
            DaemonSession v = sessions.remove(victimId);
            if (v != null) {
                try {
                    v.shutdown();
                } catch (Exception ignore) {
                }
            }
        }
    }

    // =================================================================
    // Getters
    // =================================================================

    /**
     * Gets the configuration object for this session manager.
     *
     * @return The {@link SessionConfig} instance.
     */
    public SessionConfig getConfig() {
        return config;
    }

    /**
     * Gets the execution policy applied by this session manager.
     *
     * @return The {@link PyExecutionPolicy} instance.
     */
    public PyExecutionPolicy getPolicy() {
        return policy;
    }
}