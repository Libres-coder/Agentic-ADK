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

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import java.util.Set;


public class StackOverflowConfiguration {
    
    /**
     * Allowed Stack Exchange sites for security validation
     * White list to prevent injection attacks
     */
    private static final Set<String> ALLOWED_SITES = Set.of(
        "stackoverflow", "superuser", "serverfault", "askubuntu", 
        "mathoverflow.net", "meta.stackoverflow.com", "stackapps.com",
        "math.stackexchange.com", "unix.stackexchange.com", "apple.stackexchange.com",
        "dba.stackexchange.com", "webmasters.stackexchange.com", "gamedev.stackexchange.com",
        "security.stackexchange.com", "codereview.stackexchange.com", "softwareengineering.stackexchange.com",
        "physics.stackexchange.com", "chemistry.stackexchange.com", "biology.stackexchange.com",
        "academia.stackexchange.com", "workplace.stackexchange.com", "english.stackexchange.com"
    );
    
    /**
     * Stack Exchange API Key (optional, for higher rate limits)
     * Retrieved from work properties with key "stackoverflow_api_key"
     */
    public static String getApiKey() {
        return WorkPropertiesUtils.get("stackoverflow_api_key");
    }
    
    /**
     * Stack Exchange API Base URL
     */
    public static String getApiBaseUrl() {
        return WorkPropertiesUtils.get("stackoverflow_api_base_url", "https://api.stackexchange.com/2.3");
    }
    
    /**
     * Default site for Stack Exchange API (stackoverflow, superuser, serverfault, etc.)
     */
    public static String getDefaultSite() {
        return WorkPropertiesUtils.get("stackoverflow_site", "stackoverflow");
    }
    
    /**
     * Validate if the provided site is in the allowed list
     * @param site the site to validate
     * @return true if the site is allowed, false otherwise
     */
    public static boolean isValidSite(String site) {
        if (site == null || site.trim().isEmpty()) {
            return false;
        }
        return ALLOWED_SITES.contains(site.toLowerCase().trim());
    }
    
    /**
     * Get all allowed sites
     * @return set of allowed sites
     */
    public static Set<String> getAllowedSites() {
        return Set.copyOf(ALLOWED_SITES);
    }
    
    
    /**
     * API timeout in seconds
     */
    public static String getApiTimeout() {
        return WorkPropertiesUtils.get("stackoverflow_api_timeout", "30");
    }
    
    /**
     * Read timeout in seconds
     */
    public static String getApiReadTimeout() {
        return WorkPropertiesUtils.get("stackoverflow_api_read_timeout", "60");
    }
    
    /**
     * Maximum number of results to return per search
     */
    public static String getMaxResults() {
        return WorkPropertiesUtils.get("stackoverflow_max_results", "10");
    }
    
    /**
     * Enable/disable web scraping fallback when API fails
     */
    public static String getEnableScraping() {
        return WorkPropertiesUtils.get("stackoverflow_enable_scraping", "true");
    }
    
    /**
     * Sort order for search results (activity, votes, creation, relevance)
     */
    public static String getSortOrder() {
        return WorkPropertiesUtils.get("stackoverflow_sort_order", "votes");
    }
    
    /**
     * Filter for question quality (minimum score)
     */
    public static String getMinScore() {
        return WorkPropertiesUtils.get("stackoverflow_min_score", "0");
    }
}
