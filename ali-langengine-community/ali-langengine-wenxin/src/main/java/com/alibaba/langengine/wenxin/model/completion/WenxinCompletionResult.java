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

import java.util.List;


@Data
public class WenxinCompletionResult {

    /**
     * 本轮对话的id
     */
    @JSONField(name = "id")
    private String id;

    /**
     * 回包类型
     * chat.completion：多轮对话返回
     */
    @JSONField(name = "object")
    private String object;

    /**
     * 时间戳
     */
    @JSONField(name = "created")
    private Long created;

    /**
     * 表示当前子句的序号。只有在流式接口模式下会返回该字段
     */
    @JSONField(name = "sentence_id")
    private Integer sentenceId;

    /**
     * 表示当前子句是否是最后一句。只有在流式接口模式下会返回该字段
     */
    @JSONField(name = "is_end")
    private Boolean isEnd;

    /**
     * 对话返回结果
     */
    @JSONField(name = "result")
    private String result;

    /**
     * 表示用户输入是否存在安全风险
     * true：存在安全风险
     * false：不存在安全风险
     */
    @JSONField(name = "need_clear_history")
    private Boolean needClearHistory;

    /**
     * 当need_clear_history为true时，此字段会告知第几轮对话有敏感信息，如果是当前问题，ban_round=-1
     */
    @JSONField(name = "ban_round")
    private Integer banRound;

    /**
     * token统计信息
     */
    @JSONField(name = "usage")
    private WenxinUsage usage;

    /**
     * 函数调用结果
     */
    @JSONField(name = "function_call")
    private WenxinFunctionCall functionCall;

    /**
     * 搜索数据，当请求参数enable_search为true时，会返回该字段
     */
    @JSONField(name = "search_info")
    private WenxinSearchInfo searchInfo;

    /**
     * 错误码
     */
    @JSONField(name = "error_code")
    private Integer errorCode;

    /**
     * 错误描述信息
     */
    @JSONField(name = "error_msg")
    private String errorMsg;

    /**
     * 错误详细信息
     */
    @JSONField(name = "error")
    private WenxinError error;

    /**
     * 选择结果列表（用于兼容OpenAI格式）
     */
    @JSONField(name = "choices")
    private List<WenxinChoice> choices;

    /**
     * 模型名称
     */
    @JSONField(name = "model")
    private String model;
}
