/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You you may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.googletranslate.sdk;

public interface GoogleTranslateConstant {
    /**
     * The base URL for the Google Translate API.
     */
    String BASE_URL = "https://translation.googleapis.com/language/translate/";

    /**
     * The version of the API to use.
     */
    String API_VERSION = "v2";

    /**
     * The endpoint for the translate API.
     */
    String TRANSLATE_ENDPOINT = "v2";

    /**
     * The endpoint for the detect language API.
     */
    String DETECT_ENDPOINT = "v2/detect";

    /**
     * The endpoint for the supported languages API.
     */
    String LANGUAGES_ENDPOINT = "v2/languages";

    /**
     * The default timeout in seconds for API requests.
     */
    int DEFAULT_TIMEOUT = 30;
}