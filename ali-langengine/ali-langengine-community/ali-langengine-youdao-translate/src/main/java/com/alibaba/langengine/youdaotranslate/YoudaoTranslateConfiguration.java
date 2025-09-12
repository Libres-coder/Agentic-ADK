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
package com.alibaba.langengine.youdaotranslate;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * 有道翻译配置类
 *
 * @author Makoto
 */
public class YoudaoTranslateConfiguration {

    /**
     * 有道翻译API服务器URL
     */
    public static String YOUDAO_TRANSLATE_SERVER_URL = WorkPropertiesUtils.get("youdao_translate_server_url", "https://openapi.youdao.com/api");

    /**
     * 有道翻译API应用ID
     */
    public static String YOUDAO_TRANSLATE_APP_ID = WorkPropertiesUtils.getFirstAvailable("youdao_translate_app_id", "YOUDAO_TRANSLATE_APP_ID");

    /**
     * 有道翻译API密钥
     */
    public static String YOUDAO_TRANSLATE_SECRET_KEY = WorkPropertiesUtils.getFirstAvailable("youdao_translate_secret_key", "YOUDAO_TRANSLATE_SECRET_KEY");

    /**
     * 有道翻译API超时时间（秒）
     */
    public static String YOUDAO_TRANSLATE_TIMEOUT = WorkPropertiesUtils.get("youdao_translate_timeout", 30L);

    /**
     * 有道翻译API QPS限制
     */
    public static String YOUDAO_TRANSLATE_QPS = WorkPropertiesUtils.get("youdao_translate_qps", 10L);
} 