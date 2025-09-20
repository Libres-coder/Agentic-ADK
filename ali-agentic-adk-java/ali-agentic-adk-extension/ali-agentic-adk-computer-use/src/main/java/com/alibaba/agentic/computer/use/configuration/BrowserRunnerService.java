/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.computer.use.configuration;

import com.google.adk.agents.BaseAgent;
import com.google.adk.artifacts.BaseArtifactService;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.BaseSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BrowserRunnerService {
    private static final Logger log = LoggerFactory.getLogger(BrowserRunnerService.class);
    private final BaseArtifactService artifactService;
    private final BaseSessionService sessionService;
    private final Map<String, Runner> runnerCache = new ConcurrentHashMap();

    @Autowired
    public BrowserRunnerService(BaseArtifactService artifactService, @Qualifier("baseSessionService") BaseSessionService sessionService) {
        this.artifactService = artifactService;
        this.sessionService = sessionService;
    }

    public Runner getRunner(String appName) {
        return this.runnerCache.computeIfAbsent(appName, (key) -> {
            BaseAgent agent = BrowserAgentRegister.getAgents().get(key);
            if (agent == null) {
                log.error("Agent/App named '{}' not found in registry. Available apps: {}", key, BrowserAgentRegister.getAgents().keySet());
                throw new RuntimeException("Agent/App not found: " + key);
            } else {
                log.info("RunnerService: Creating Runner for appName: {}, using agent definition: {}", appName, agent.name());
                return new Runner(agent, appName, this.artifactService, this.sessionService);
            }
        });
    }
}