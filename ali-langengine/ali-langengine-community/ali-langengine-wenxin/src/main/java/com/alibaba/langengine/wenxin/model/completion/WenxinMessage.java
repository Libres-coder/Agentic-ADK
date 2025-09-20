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
public class WenxinMessage {

    /**
     * 消息角色
     * user: 表示用户角色
     * assistant: 表示对话助手角色
     * system: 表示系统角色
     * function: 表示函数角色
     */
    @JSONField(name = "role")
    private String role;

    /**
     * 对话内容
     * 不能为空，且长度不超过20000个字符
     */
    @JSONField(name = "content")
    private String content;

    /**
     * message作者，当role为function时，该字段标识function name
     */
    @JSONField(name = "name")
    private String name;

    /**
     * 函数调用，当role为assistant时，该字段标识要调用的函数
     */
    @JSONField(name = "function_call")
    private WenxinFunctionCall functionCall;

    public WenxinMessage() {
    }

    public WenxinMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public WenxinMessage(String role, String content, String name) {
        this.role = role;
        this.content = content;
        this.name = name;
    }
}
