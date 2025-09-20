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


@Data
public class WenxinFunctionCall {

    /**
     * 函数名
     */
    @JSONField(name = "name")
    private String name;

    /**
     * 函数参数，JSON格式字符串
     */
    @JSONField(name = "arguments")
    private String arguments;

    /**
     * 函数调用的思考过程
     */
    @JSONField(name = "thoughts")
    private String thoughts;

    public WenxinFunctionCall() {
    }

    public WenxinFunctionCall(String name, String arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public WenxinFunctionCall(String name, String arguments, String thoughts) {
        this.name = name;
        this.arguments = arguments;
        this.thoughts = thoughts;
    }
}
