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
package com.alibaba.langengine.baidutranslate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 百度翻译响应模型
 *
 * @author Makoto
 */
@Data
public class BaiduTranslateResponse {

    /**
     * 翻译源语言
     */
    @JsonProperty("from")
    private String from;

    /**
     * 翻译目标语言
     */
    @JsonProperty("to")
    private String to;

    /**
     * 翻译结果
     */
    @JsonProperty("trans_result")
    private List<TransResult> transResult;

    /**
     * 错误码
     */
    @JsonProperty("error_code")
    private String errorCode;

    /**
     * 错误信息
     */
    @JsonProperty("error_msg")
    private String errorMsg;

    /**
     * 翻译结果项
     */
    @Data
    public static class TransResult {
        /**
         * 原文
         */
        @JsonProperty("src")
        private String src;

        /**
         * 译文
         */
        @JsonProperty("dst")
        private String dst;
    }
} 