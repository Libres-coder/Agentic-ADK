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
package com.alibaba.langengine.stackoverflow;

import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class StackOverflowConfigurationTest {
    
    @Test
    void testDefaultConfiguration() {
        // Test that configuration values have proper defaults
        assertNotNull(StackOverflowConfiguration.getApiBaseUrl());
        assertEquals("https://api.stackexchange.com/2.3", StackOverflowConfiguration.getApiBaseUrl());
        
        assertNotNull(StackOverflowConfiguration.getDefaultSite());
        assertEquals("stackoverflow", StackOverflowConfiguration.getDefaultSite());
        
        assertNotNull(StackOverflowConfiguration.getApiTimeout());
        assertEquals("30", StackOverflowConfiguration.getApiTimeout());
        
        assertNotNull(StackOverflowConfiguration.getApiReadTimeout());
        assertEquals("60", StackOverflowConfiguration.getApiReadTimeout());
        
        assertNotNull(StackOverflowConfiguration.getMaxResults());
        assertEquals("10", StackOverflowConfiguration.getMaxResults());
        
        assertNotNull(StackOverflowConfiguration.getEnableScraping());
        assertEquals("true", StackOverflowConfiguration.getEnableScraping());
        
        assertNotNull(StackOverflowConfiguration.getSortOrder());
        assertEquals("votes", StackOverflowConfiguration.getSortOrder());
        
        assertNotNull(StackOverflowConfiguration.getMinScore());
        assertEquals("0", StackOverflowConfiguration.getMinScore());
    }
    
    @Test
    void testConfigurationConstants() {
        // Test that configuration class has the expected structure
        assertNotNull(StackOverflowConfiguration.class.getDeclaredFields());
        assertTrue(StackOverflowConfiguration.class.getDeclaredFields().length >= 1);
        
        // API key can be null (optional)
        // No assertion needed for STACKOVERFLOW_API_KEY as it might be null
    }
    
    @Test
    void testNumericConfigurationValues() {
        // Test that numeric configuration values can be parsed
        assertDoesNotThrow(() -> {
            Integer.parseInt(StackOverflowConfiguration.getApiTimeout());
        });
        
        assertDoesNotThrow(() -> {
            Integer.parseInt(StackOverflowConfiguration.getApiReadTimeout());
        });
        
        assertDoesNotThrow(() -> {
            Integer.parseInt(StackOverflowConfiguration.getMaxResults());
        });
        
        assertDoesNotThrow(() -> {
            Integer.parseInt(StackOverflowConfiguration.getMinScore());
        });
        
        // Test that timeouts are reasonable values
        int timeout = Integer.parseInt(StackOverflowConfiguration.getApiTimeout());
        assertTrue(timeout > 0 && timeout <= 300, "Timeout should be between 1 and 300 seconds");
        
        int readTimeout = Integer.parseInt(StackOverflowConfiguration.getApiReadTimeout());
        assertTrue(readTimeout > 0 && readTimeout <= 600, "Read timeout should be between 1 and 600 seconds");
        
        int maxResults = Integer.parseInt(StackOverflowConfiguration.getMaxResults());
        assertTrue(maxResults > 0 && maxResults <= 100, "Max results should be between 1 and 100");
        
        int minScore = Integer.parseInt(StackOverflowConfiguration.getMinScore());
        assertTrue(minScore >= 0, "Min score should be non-negative");
    }
    
    @Test
    void testBooleanConfigurationValues() {
        // Test that boolean configuration values can be parsed
        assertDoesNotThrow(() -> {
            Boolean.parseBoolean(StackOverflowConfiguration.getEnableScraping());
        });
        
        // Test that scraping is enabled by default
        assertTrue(Boolean.parseBoolean(StackOverflowConfiguration.getEnableScraping()));
    }
    
    @Test
    void testValidSortOrderValues() {
        String sortOrder = StackOverflowConfiguration.getSortOrder();
        assertTrue(
            "votes".equals(sortOrder) || 
            "activity".equals(sortOrder) || 
            "creation".equals(sortOrder) || 
            "relevance".equals(sortOrder),
            "Sort order should be one of: votes, activity, creation, relevance"
        );
    }
    
    @Test
    void testValidSiteValue() {
        String site = StackOverflowConfiguration.getDefaultSite();
        assertNotNull(site);
        assertFalse(site.trim().isEmpty(), "Site should not be empty");
        
        // Common Stack Exchange sites
        assertTrue(
            "stackoverflow".equals(site) ||
            "superuser".equals(site) ||
            "serverfault".equals(site) ||
            "askubuntu".equals(site) ||
            "mathoverflow.net".equals(site),
            "Site should be a valid Stack Exchange site"
        );
    }
    
    @Test
    void testApiBaseUrlFormat() {
        String baseUrl = StackOverflowConfiguration.getApiBaseUrl();
        assertNotNull(baseUrl);
        assertTrue(baseUrl.startsWith("https://"), "API base URL should use HTTPS");
        assertTrue(baseUrl.contains("stackexchange.com") || baseUrl.contains("stackoverflow.com"), 
                  "API base URL should be a Stack Exchange API endpoint");
        assertFalse(baseUrl.endsWith("/"), "API base URL should not end with slash");
    }
    
    @Test
    void testSiteValidation() {
        // Test valid sites
        assertTrue(StackOverflowConfiguration.isValidSite("stackoverflow"));
        assertTrue(StackOverflowConfiguration.isValidSite("superuser"));
        assertTrue(StackOverflowConfiguration.isValidSite("serverfault"));
        assertTrue(StackOverflowConfiguration.isValidSite("askubuntu"));
        assertTrue(StackOverflowConfiguration.isValidSite("mathoverflow.net"));
        
        // Test invalid sites
        assertFalse(StackOverflowConfiguration.isValidSite("invalid-site"));
        assertFalse(StackOverflowConfiguration.isValidSite(""));
        assertFalse(StackOverflowConfiguration.isValidSite(null));
        assertFalse(StackOverflowConfiguration.isValidSite("  "));
        assertFalse(StackOverflowConfiguration.isValidSite("malicious.site"));
        assertFalse(StackOverflowConfiguration.isValidSite("site with spaces"));
        
        // Test case insensitive
        assertTrue(StackOverflowConfiguration.isValidSite("STACKOVERFLOW"));
        assertTrue(StackOverflowConfiguration.isValidSite("SuperUser"));
    }
    
    @Test
    void testGetAllowedSites() {
        Set<String> allowedSites = StackOverflowConfiguration.getAllowedSites();
        assertNotNull(allowedSites);
        assertFalse(allowedSites.isEmpty());
        assertTrue(allowedSites.contains("stackoverflow"));
        assertTrue(allowedSites.contains("superuser"));
        assertTrue(allowedSites.contains("serverfault"));
        
        // Test that returned set is immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            allowedSites.add("malicious-site");
        });
    }
}
