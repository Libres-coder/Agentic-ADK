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
public class WenxinChoice {

    /**
     * 选择索引
     */
    @JSONField(name = "index")
    private Integer index;

    /**
     * 消息内容
     */
    @JSONField(name = "message")
    private WenxinMessage message;

    /**
     * 增量消息内容（流式返回）
     */
    @JSONField(name = "delta")
    private WenxinMessage delta;

    /**
     * 结束原因
     * stop: 正常结束
     * length: 长度限制
     * function_call: 函数调用
     */
    @JSONField(name = "finish_reason")
    private String finishReason;

    /**
     * 日志概率
     */
    @JSONField(name = "logprobs")
    private Object logprobs;
}
