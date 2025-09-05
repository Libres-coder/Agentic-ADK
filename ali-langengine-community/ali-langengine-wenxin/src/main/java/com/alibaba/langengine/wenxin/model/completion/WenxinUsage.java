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
public class WenxinUsage {

    /**
     * 问题tokens数
     */
    @JSONField(name = "prompt_tokens")
    private Integer promptTokens;

    /**
     * 回答tokens数
     */
    @JSONField(name = "completion_tokens")
    private Integer completionTokens;

    /**
     * tokens总数
     */
    @JSONField(name = "total_tokens")
    private Integer totalTokens;

    /**
     * 插件消耗的tokens
     */
    @JSONField(name = "plugins_tokens")
    private Integer pluginsTokens;
}
