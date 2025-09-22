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
package com.alibaba.langengine.txtai;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import com.alibaba.langengine.txtai.exception.TxtaiException;
import org.apache.commons.lang3.StringUtils;

/**
 * txtai configuration
 *
 * @author xiaoxuan.lp
 */
public class TxtaiConfiguration {

    /**
     * txtai server url
     */
    public static final String TXTAI_SERVER_URL = getConfigValue("txtai_server_url");

    /**
     * 默认服务器URL（用于开发和测试）
     */
    private static final String DEFAULT_TXTAI_URL = "http://localhost:8000";

    /**
     * 获取配置值，提供默认值处理
     */
    private static String getConfigValue(String key) {
        String value = WorkPropertiesUtils.get(key);
        if (StringUtils.isBlank(value) && "txtai_server_url".equals(key)) {
            return DEFAULT_TXTAI_URL;
        }
        return value;
    }

    /**
     * 验证配置是否完整
     */
    public static void validateConfiguration() {
        if (StringUtils.isBlank(TXTAI_SERVER_URL)) {
            throw TxtaiException.configurationError("txtai服务器URL未配置，请设置 txtai_server_url 参数");
        }
    }
}