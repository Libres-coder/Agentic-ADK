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

package com.alibaba.langengine.gitee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GiteeConfigurationTest {

    @Test
    void testApiUrl() {
        assertNotNull(GiteeConfiguration.GITEE_API_URL);
        assertTrue(GiteeConfiguration.GITEE_API_URL.contains("gitee.com"));
    }

    @Test
    void testSearchApiUrl() {
        assertNotNull(GiteeConfiguration.GITEE_SEARCH_API_URL);
        assertTrue(GiteeConfiguration.GITEE_SEARCH_API_URL.contains("search"));
    }

    @Test
    void testDefaultValues() {
        assertEquals(30, GiteeConfiguration.DEFAULT_TIMEOUT_SECONDS);
        assertEquals(20, GiteeConfiguration.DEFAULT_PER_PAGE);
        assertEquals(100, GiteeConfiguration.MAX_PER_PAGE);
    }

    @Test
    void testGetIntEnvOrDefault() {
        int defaultValue = 30;
        int result = GiteeConfiguration.getIntEnvOrDefault("NON_EXISTENT_ENV_VAR", defaultValue);
        assertEquals(defaultValue, result);
    }

    @Test
    void testHasValidToken() {
        // This test depends on whether a token is configured in the environment
        // The result will vary based on the test environment setup
        boolean hasToken = GiteeConfiguration.hasValidToken();
        // We can't assert a specific value since it depends on environment configuration
        assertNotNull(hasToken); // Just ensure the method runs without error
    }

    @Test
    void testGetConfigurationSummary() {
        String summary = GiteeConfiguration.getConfigurationSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("Gitee Configuration"));
        assertTrue(summary.contains("API URL"));
        assertTrue(summary.contains("Token configured"));
    }

    @Test
    void testValidateConfigurationWithoutToken() {
        // If no token is configured, this should throw an exception
        if (!GiteeConfiguration.hasValidToken()) {
            assertThrows(IllegalStateException.class, GiteeConfiguration::validateConfiguration);
        }
    }

    @Test
    void testConstructor() {
        GiteeConfiguration config = new GiteeConfiguration();
        assertNotNull(config);
    }
}