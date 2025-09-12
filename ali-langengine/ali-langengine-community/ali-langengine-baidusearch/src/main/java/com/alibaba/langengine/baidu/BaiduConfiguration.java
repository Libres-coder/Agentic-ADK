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
package com.alibaba.langengine.baidu;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

import static com.alibaba.langengine.baidu.sdk.BaiduConstant.DEFAULT_USER_AGENT;

public class BaiduConfiguration {
    /**
     * Optional custom user agent for Baidu requests
     */
    public static String BAIDU_USER_AGENT = WorkPropertiesUtils.get("baidu_user_agent", DEFAULT_USER_AGENT);
}

