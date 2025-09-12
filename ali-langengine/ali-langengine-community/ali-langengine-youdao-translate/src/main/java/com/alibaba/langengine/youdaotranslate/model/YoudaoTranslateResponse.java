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
package com.alibaba.langengine.youdaotranslate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 有道翻译响应模型
 *
 * @author Makoto
 */
@Data
public class YoudaoTranslateResponse {

    /**
     * 翻译源语言
     */
    @JsonProperty("l")
    private String language;

    /**
     * 翻译结果
     */
    @JsonProperty("translation")
    private List<String> translation;

    /**
     * 基本词典
     */
    @JsonProperty("basic")
    private Basic basic;

    /**
     * 网络释义
     */
    @JsonProperty("web")
    private List<Web> web;

    /**
     * 错误码
     */
    @JsonProperty("errorCode")
    private String errorCode;

    /**
     * 错误信息
     */
    @JsonProperty("errorMsg")
    private String errorMsg;

    /**
     * 基本词典信息
     */
    @Data
    public static class Basic {
        /**
         * 音标
         */
        @JsonProperty("phonetic")
        private String phonetic;

        /**
         * 美式音标
         */
        @JsonProperty("us-phonetic")
        private String usPhonetic;

        /**
         * 英式音标
         */
        @JsonProperty("uk-phonetic")
        private String ukPhonetic;

        /**
         * 词性释义
         */
        @JsonProperty("explains")
        private List<String> explains;
    }

    /**
     * 网络释义信息
     */
    @Data
    public static class Web {
        /**
         * 关键词
         */
        @JsonProperty("key")
        private String key;

        /**
         * 网络释义
         */
        @JsonProperty("value")
        private List<String> value;
    }
} 