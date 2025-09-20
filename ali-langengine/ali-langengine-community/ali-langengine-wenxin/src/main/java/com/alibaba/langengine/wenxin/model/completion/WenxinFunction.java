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
package com.alibaba.langengine.wenxin.model.completion;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Map;


@Data
public class WenxinFunction {

    /**
     * 函数名
     * 只能包含a-z，A-Z，0-9，下划线和短横线，最大长度为64个字符
     */
    @JSONField(name = "name")
    private String name;

    /**
     * 函数描述
     * 用于描述函数的功能，帮助模型选择合适的函数进行调用，不超过256个字符
     */
    @JSONField(name = "description")
    private String description;

    /**
     * 函数参数
     * 函数参数信息，JSON Schema格式
     */
    @JSONField(name = "parameters")
    private Map<String, Object> parameters;

    /**
     * 函数返回参数
     * 函数返回参数信息，JSON Schema格式
     */
    @JSONField(name = "responses")
    private Map<String, Object> responses;

    /**
     * function调用的一些历史示例
     * 用于提升函数调用的准确性
     */
    @JSONField(name = "examples")
    private Object examples;

    public WenxinFunction() {
    }

    public WenxinFunction(String name, String description, Map<String, Object> parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }
}
