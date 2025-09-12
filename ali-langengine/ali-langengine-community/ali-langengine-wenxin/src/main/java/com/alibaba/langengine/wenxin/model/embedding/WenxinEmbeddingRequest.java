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
package com.alibaba.langengine.wenxin.model.embedding;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;


@Data
public class WenxinEmbeddingRequest {

    /**
     * 输入文本，支持字符串或字符串数组
     * 最大支持16个字符串，每个字符串最长384个token
     */
    @JSONField(name = "input")
    private Object input;

    /**
     * 模型名称
     * 默认为 embedding-v1
     */
    @JSONField(name = "model")
    private String model;

    /**
     * 用户ID，用于标识不同用户
     */
    @JSONField(name = "user_id")
    private String userId;

    public WenxinEmbeddingRequest() {
    }

    public WenxinEmbeddingRequest(String input) {
        this.input = input;
    }

    public WenxinEmbeddingRequest(List<String> input) {
        this.input = input;
    }

    public WenxinEmbeddingRequest(String input, String model) {
        this.input = input;
        this.model = model;
    }

    public WenxinEmbeddingRequest(List<String> input, String model) {
        this.input = input;
        this.model = model;
    }
}
