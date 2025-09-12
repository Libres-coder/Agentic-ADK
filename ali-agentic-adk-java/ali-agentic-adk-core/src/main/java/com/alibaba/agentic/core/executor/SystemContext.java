/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.core.executor;

import io.reactivex.rxjava3.processors.FlowableProcessor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统上下文，贯穿整个执行生命周期的状态容器。
 * <p>
 * 包含当前执行器、调用模式、流处理器以及节点间的中间结果传递。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/28 09:52
 */
@Data
@Accessors(chain = true)
public class SystemContext {

    /**
     * 当前执行器实例。
     */
    private Executor executor;

    /**
     * 调用模式（同步、异步、双工）。
     */
    private InvokeMode invokeMode;

    /**
     * 流处理器，用于双工模式下的事件流处理。
     */
    private FlowableProcessor<String> processor;

    /**
     * 原始请求参数。
     */
    private Map<String, Object> requestParameter;

    /**
     * 节点间中间结果存储，键为节点ID，值为该节点的输出结果。
     */
    private Map<String, Map<String, Object>> interOutput = new HashMap<>();

}
