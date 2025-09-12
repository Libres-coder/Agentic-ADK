/**
 * Copyright (C) 2025 AIDC-AI
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

package com.alibaba.langengine.gitcode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GitCodeConfigurationTest {

    @Test
    void testDefaultConfiguration() {
        assertNotNull(GitCodeConfiguration.GITCODE_API_URL);
        assertEquals("https://api.gitcode.com", GitCodeConfiguration.GITCODE_API_URL);
        assertEquals("https://api.gitcode.com/api/v5/search", GitCodeConfiguration.GITCODE_SEARCH_API_URL);
        assertEquals(30, GitCodeConfiguration.DEFAULT_TIMEOUT_SECONDS);
        assertEquals(20, GitCodeConfiguration.DEFAULT_PER_PAGE);
        assertEquals(50, GitCodeConfiguration.MAX_PER_PAGE);
    }

    @Test
    void testGetIntEnvOrDefault() {
        int defaultValue = 100;
        int result = GitCodeConfiguration.getIntEnvOrDefault("NON_EXISTENT_ENV_VAR", defaultValue);
        assertEquals(defaultValue, result);
    }

    @Test
    void testGetConfigurationSummary() {
        String summary = GitCodeConfiguration.getConfigurationSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("GitCode Configuration"));
        assertTrue(summary.contains("API URL"));
        assertTrue(summary.contains("Token configured"));
        assertTrue(summary.contains("Search API URL"));
        assertTrue(summary.contains("Default timeout"));
        assertTrue(summary.contains("Default per page"));
        assertTrue(summary.contains("Max per page"));
    }

    @Test
    void testHasValidToken() {
        boolean hasToken = GitCodeConfiguration.hasValidToken();
        assertNotNull(hasToken);
    }
}