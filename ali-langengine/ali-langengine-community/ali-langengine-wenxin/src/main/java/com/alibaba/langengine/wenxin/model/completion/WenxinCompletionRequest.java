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
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;


@Data
@Builder
@EqualsAndHashCode(callSuper=false)
public class WenxinCompletionRequest {

    /**
     * 聊天上下文信息
     * 每个元素包含role和content字段
     */
    @JSONField(name = "messages")
    private List<WenxinMessage> messages;

    /**
     * 表示最终用户的唯一标识符
     */
    @JSONField(name = "user_id")
    private String userId;

    /**
     * 模型人设，主要用于人设设定
     */
    @JSONField(name = "system")
    private String system;

    /**
     * 生成停止的标识
     */
    @JSONField(name = "stop")
    private List<String> stop;

    /**
     * 是否以流式接口的形式返回数据
     */
    @JSONField(name = "stream")
    private Boolean stream;

    /**
     * 说明：
     * （1）较高的数值会使输出更加随机，而较低的数值会使其更加集中和确定
     * （2）默认0.8，范围 (0, 1.0]
     */
    @JSONField(name = "temperature")
    private Double temperature;

    /**
     * 说明：
     * （1）影响输出文本的多样性，取值越大，生成文本的多样性越强
     * （2）默认0.8，取值范围 [0, 1.0]
     */
    @JSONField(name = "top_p")
    private Double topP;

    /**
     * 通过对已生成的token增加惩罚，减少重复生成的现象
     * 说明：
     * （1）值越大表示惩罚越大
     * （2）默认1.0，取值范围：[1.0, 2.0]
     */
    @JSONField(name = "penalty_score")
    private Double penaltyScore;

    /**
     * 模型人设，主要用于人设设定
     * 说明：
     * （1）长度限制，message中content总长度和system字段总内容不能超过20000个字符，且不能超过5120 tokens
     */
    @JSONField(name = "functions")
    private List<WenxinFunction> functions;

    /**
     * 工具选择
     * 控制模型如何选择要调用的函数（如果有多个函数可用）
     */
    @JSONField(name = "tool_choice")
    private Object toolChoice;

    /**
     * 模型回答的tokens的最大长度
     * 说明：
     * （1）默认值1024
     * （2）范围: [2, 2048]
     */
    @JSONField(name = "max_output_tokens")
    private Integer maxOutputTokens;

    /**
     * 是否开启联网搜索功能
     * 说明：
     * （1）true表示开启联网搜索
     * （2）false表示关闭联网搜索
     * （3）默认false
     */
    @JSONField(name = "enable_search")
    private Boolean enableSearch;

    /**
     * 搜索数据的时间范围
     * 格式：搜索开始时间,搜索结束时间
     * 其中时间格式为yyyy-mm-dd，如2023-01-01,2023-12-31
     * 要求：
     * （1）搜索结束时间需晚于搜索开始时间
     * （2）搜索时间的区间建议不超过一年
     */
    @JSONField(name = "search_time_range")
    private String searchTimeRange;

    /**
     * 强制搜索自定义搜索结果，
     * 格式为搜索结果列表，列表长度限制：0-5，每个搜索结果字符数限制：512
     */
    @JSONField(name = "search_results")
    private List<Map<String, String>> searchResults;

    /**
     * 指定响应内容的格式，默认text
     * json_object：以json格式返回，可能出现不满足json格式要求的内容
     */
    @JSONField(name = "response_format")
    private String responseFormat;
}
