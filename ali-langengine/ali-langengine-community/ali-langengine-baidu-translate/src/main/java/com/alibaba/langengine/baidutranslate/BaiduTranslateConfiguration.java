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
package com.alibaba.langengine.baidutranslate;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * 百度翻译配置类
 *
 * @author Makoto
 */
public class BaiduTranslateConfiguration {

    /**
     * 百度翻译API服务器URL
     */
    public static final String BAIDU_TRANSLATE_SERVER_URL = WorkPropertiesUtils.get("baidu_translate_server_url", "https://fanyi-api.baidu.com/api/trans/vip/translate");

    /**
     * 百度翻译API应用ID
     */
    public static final String BAIDU_TRANSLATE_APP_ID = WorkPropertiesUtils.getFirstAvailable("baidu_translate_app_id", "BAIDU_TRANSLATE_APP_ID");

    /**
     * 百度翻译API密钥
     */
    public static final String BAIDU_TRANSLATE_SECRET_KEY = WorkPropertiesUtils.getFirstAvailable("baidu_translate_secret_key", "BAIDU_TRANSLATE_SECRET_KEY");

    /**
     * 百度翻译API超时时间（秒）
     */
    public static final String BAIDU_TRANSLATE_TIMEOUT = WorkPropertiesUtils.get("baidu_translate_timeout", 30L);

    private BaiduTranslateConfiguration() {
        // 工具类，不允许实例化
    }
}
