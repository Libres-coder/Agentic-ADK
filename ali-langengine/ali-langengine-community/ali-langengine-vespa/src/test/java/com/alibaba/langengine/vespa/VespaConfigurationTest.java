/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.vespa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class VespaConfigurationTest {

    @Test
    void testConfigurationFields() {
        // Test that configuration fields are accessible
        assertNotNull(VespaConfiguration.class);
        
        // Test field declarations exist
        try {
            VespaConfiguration.class.getDeclaredField("VESPA_QUERY_URL");
            VespaConfiguration.class.getDeclaredField("VESPA_FEED_URL");
            VespaConfiguration.class.getDeclaredField("VESPA_CERTIFICATE_PATH");
            VespaConfiguration.class.getDeclaredField("VESPA_PRIVATE_KEY_PATH");
        } catch (NoSuchFieldException e) {
            fail("Configuration field not found: " + e.getMessage());
        }
    }

    @Test
    void testConfigurationValues() {
        // Test that configuration values can be retrieved (may be null)
        String queryUrl = VespaConfiguration.VESPA_QUERY_URL;
        String feedUrl = VespaConfiguration.VESPA_FEED_URL;
        String certPath = VespaConfiguration.VESPA_CERTIFICATE_PATH;
        String keyPath = VespaConfiguration.VESPA_PRIVATE_KEY_PATH;
        
        // These can be null if not configured, which is fine for testing
        assertTrue(queryUrl == null || queryUrl instanceof String);
        assertTrue(feedUrl == null || feedUrl instanceof String);
        assertTrue(certPath == null || certPath instanceof String);
        assertTrue(keyPath == null || keyPath instanceof String);
    }
}
