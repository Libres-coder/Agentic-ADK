/**
 * Copyright (C) 2024 AIDC-AI
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
package com.alibaba.agentic.core.engine.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 属性常量定义。
 * <p>
 * 定义框架使用的配置属性常量，包括API密钥、默认值以及符号标识等。
 * 通过Spring的@Value注解从配置文件中注入相关属性值。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/8/5 18:00
 */
@Component
public class PropertyConstant {

    /**
     * DashScope API密钥，从配置文件中注入。
     */
    public static String dashscopeApiKey;

    /**
     * 默认值常量。
     */
    public static final String DEFAULT = "default";

    /**
     * 符号键名常量。
     */
    public static final String SYMBOL_KEY = "symbol";

    /**
     * 条件默认流符号值常量。
     */
    public static final String SYMBOL_VALUE_CONDITION_DEFAULT_FLOW = "conditionDefaultFlow";

    /**
     * 设置DashScope API密钥。
     *
     * @param dashscopeApiKey 从配置文件注入的API密钥
     */
    @Value(value = "${ali.agentic.adk.flownode.dashscope.apiKey:**}")
    public void setDashscopeApiKey(String dashscopeApiKey) {
        PropertyConstant.dashscopeApiKey = dashscopeApiKey;
    }
}
