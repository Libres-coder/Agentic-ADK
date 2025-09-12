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
package com.alibaba.langengine.wenxin;


public class WenxinModelName {

    /**
     * 文心大模型4.0版本
     * 百度自研的旗舰级大语言模型，覆盖海量中文数据，具有更强的对话问答、内容创作生成等能力
     */
    public static final String ERNIE_4_0_8K = "completions_pro";

    /**
     * 文心大模型4.0版本
     * 百度自研的旗舰级大语言模型，覆盖海量中文数据，具有更强的对话问答、内容创作生成等能力
     */
    public static final String ERNIE_4_0 = "ernie-4.0-8k";

    /**
     * 文心大模型4.0 Turbo版本
     * 在保持效果的前提下，推理速度更快，价格更优惠
     */
    public static final String ERNIE_4_0_TURBO_8K = "ernie-4.0-turbo-8k";

    /**
     * 文心大模型3.5版本
     * 升级版本，综合能力显著提升，支持中英文对话
     */
    public static final String ERNIE_3_5 = "ernie-3.5-8k";

    /**
     * 文心大模型Lite版本
     * 兼顾效果与性能的轻量化模型，支持中英文对话
     */
    public static final String ERNIE_LITE_8K = "ernie-lite-8k";

    /**
     * 文心大模型Speed版本
     * 高性能版本，响应速度更快
     */
    public static final String ERNIE_SPEED_8K = "ernie-speed-8k";

    /**
     * 文心大模型Speed版本（128K）
     * 高性能版本，支持更长的上下文
     */
    public static final String ERNIE_SPEED_128K = "ernie-speed-128k";

    /**
     * 文心大模型Tiny版本
     * 超轻量级模型，适合对成本敏感的场景
     */
    public static final String ERNIE_TINY_8K = "ernie-tiny-8k";

    /**
     * 文心大模型Character版本
     * 擅长对话的角色扮演模型
     */
    public static final String ERNIE_CHARACTER_8K = "ernie-character-8k";

    /**
     * BLOOMZ-7B模型
     * 基于BLOOM的中文增强版本
     */
    public static final String BLOOMZ_7B = "bloomz_7b1";

    /**
     * Llama2-7B-Chat模型
     * Meta开源的对话模型
     */
    public static final String LLAMA_2_7B_CHAT = "llama_2_7b";

    /**
     * Llama2-13B-Chat模型  
     * Meta开源的对话模型，参数更多
     */
    public static final String LLAMA_2_13B_CHAT = "llama_2_13b";

    /**
     * Llama2-70B-Chat模型
     * Meta开源的大参数对话模型
     */
    public static final String LLAMA_2_70B_CHAT = "llama_2_70b";

    /**
     * ChatGLM2-6B模型
     * 清华大学开源的中英双语对话模型
     */
    public static final String CHATGLM2_6B = "chatglm2_6b_32k";

    /**
     * 默认模型
     */
    public static final String DEFAULT_MODEL = ERNIE_4_0_8K;

    // ========== 嵌入模型 ==========

    /**
     * Embedding-V1 文本向量化模型
     */
    public static final String EMBEDDING_V1 = "embedding-v1";

    /**
     * BGE-Large-ZH 中文文本向量化模型
     */
    public static final String BGE_LARGE_ZH = "bge-large-zh";

    /**
     * BGE-Large-EN 英文文本向量化模型
     */
    public static final String BGE_LARGE_EN = "bge-large-en";

    /**
     * TAO-8K 长文本向量化模型
     */
    public static final String TAO_8K = "tao-8k";

    /**
     * 默认嵌入模型
     */
    public static final String DEFAULT_EMBEDDING_MODEL = EMBEDDING_V1;

    private WenxinModelName() {
        // 工具类，不允许实例化
    }
}
