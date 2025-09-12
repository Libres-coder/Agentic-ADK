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
package com.alibaba.langengine.core.agent;

import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import lombok.Data;

import java.util.*;
import java.util.function.Consumer;

/**
 * Agent基础抽象类，负责调用语言模型并决定下一步动作
 * 
 * 主要功能：
 * - 管理Agent的思考过程和中间步骤
 * - 构建完整的输入参数供LLM处理
 * - 解析LLM输出并决定下一步动作
 *
 * @author xiaoxuan.lp
 */
@Data
public abstract class Agent extends BaseSingleActionAgent {

    @Override
    public void setCallbackManager(BaseCallbackManager callbackManager) {
        getLlmChain().setCallbackManager(callbackManager != null ? callbackManager.getChild() : null);
    }

    private AgentOutputParser outputParser;
    private List<String> allowedTools;

    public List<String> returnValues() {
        return Arrays.asList(new String[] { "output" });
    }

    public String stop() {
        return "\n" + observationPrefix().trim();
    }

    /**
     * 附加观察的前缀
     *
     * @return
     */
    public abstract String observationPrefix();

    /**
     * 附加 LLM 调用的前缀
     *
     * @return
     */
    public abstract String llmPrefix();

    public void init(boolean isCH) {
        // do nothings...
    }

    /**
     * 构建Agent的思考暂存器，记录中间步骤和观察结果
     * 用于让Agent基于历史动作和结果进行下一步推理
     *
     * @param intermediateSteps 中间执行步骤列表
     * @return 格式化的思考过程字符串
     */
    public String constructScratchpad(List<AgentAction> intermediateSteps) {
        String thoughts = "";
        for (AgentAction action : intermediateSteps) {
            thoughts += action.getLog();
            if(thoughts.endsWith("\n")) {
                thoughts = thoughts.substring(0, thoughts.length() - 1);
            }
            thoughts += "\n" + observationPrefix() + action.getObservation() + "\n" + llmPrefix();
        }
        return thoughts;
    }

    public Object plan(List<AgentAction> intermediateSteps, Map<String, Object> inputs, Consumer<String> consumer, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        Map<String, Object> fullInputs = getFullInputs(intermediateSteps, inputs);
        if(executionContext != null) {
            executionContext.setChildExecutionType("childChain-" + intermediateSteps.size());
        }
        String fullOutput = getLlmChain().predictToOutput(fullInputs, consumer, executionContext, extraAttributes);
        return outputParser.parse(fullOutput);
    }

    /**
     * 为LLMChain创建完整的输入参数
     * 包含用户输入、思考暂存器和停止条件
     *
     * @param intermediateSteps 中间执行步骤
     * @param inputs 用户输入参数
     * @return 完整的LLM输入参数映射
     */
    public Map<String, Object> getFullInputs(List<AgentAction> intermediateSteps, Map<String, Object> inputs) {
        String thoughts = constructScratchpad(intermediateSteps);

        Map<String, Object> fullInputs = new HashMap<>();
        fullInputs.putAll(inputs);
        fullInputs.put("agent_scratchpad", thoughts);
        fullInputs.put("stop", stop());
        return fullInputs;
    }

    @Override
    public List<String> getInputKeys() {
        List<String> inputKeys = new ArrayList<>(getLlmChain().getInputKeys());
        inputKeys.remove("agent_scratchpad");
        return inputKeys;
    }
}
