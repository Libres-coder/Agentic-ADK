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
package com.alibaba.langengine.tavily.sdk;

public interface TavilyConstant {
    /**
     * The base URL for the Tavily API.
     */
    String BASE_URL = "https://api.tavily.com/";

    /**
     * The endpoint for the search API.
     */
    String SEARCH_ENDPOINT = "search";

    /**
     * The default timeout in seconds for API requests.
     */
    int DEFAULT_TIMEOUT = 30;
}
