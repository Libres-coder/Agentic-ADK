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
public class WenxinError {

    /**
     * 错误码
     */
    @JSONField(name = "code")
    private String code;

    /**
     * 错误消息
     */
    @JSONField(name = "message")
    private String message;

    /**
     * 错误类型
     */
    @JSONField(name = "type")
    private String type;

    /**
     * 错误参数
     */
    @JSONField(name = "param")
    private String param;
}
