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

import lombok.Builder;
import lombok.Data;

/**
 * 有道翻译请求模型
 *
 * @author Makoto
 */
@Data
@Builder
public class YoudaoTranslateRequest {

    /**
     * 待翻译文本
     */
    private String q;

    /**
     * 源语言，可设置为auto
     */
    private String from;

    /**
     * 目标语言
     */
    private String to;

    /**
     * 应用ID
     */
    private String appKey;

    /**
     * 随机数
     */
    private String salt;

    /**
     * 签名
     */
    private String sign;

    /**
     * 签名类型，固定值：v3
     */
    private String signType;

    /**
     * 请求时间戳
     */
    private String curtime;

    /**
     * 请求来源
     */
    private String source;

    /**
     * 翻译类型，可选值：text, html
     */
    private String type;

    /**
     * 是否返回音标，可选值：0, 1
     */
    private String strict;

    /**
     * 是否返回词典，可选值：0, 1
     */
    private String vocabId;
} 