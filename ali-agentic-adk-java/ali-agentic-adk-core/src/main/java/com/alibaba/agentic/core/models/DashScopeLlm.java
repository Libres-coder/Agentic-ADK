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
package com.alibaba.agentic.core.models;

import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.delegation.domain.LlmRequest;
import com.alibaba.agentic.core.engine.delegation.domain.LlmResponse;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhenkui.yzk
 * 阿里百炼Api说明:https://help.aliyun.com/zh/model-studio/use-qwen-by-calling-api
 * 使用SDK方式接入
 */
@Slf4j
@Component
public class DashScopeLlm implements BasicLlm {

    private String apiKey;

    @Override
    public String model() {
        return "dashscope";
    }

    private static Role mapRole(String role) {
        if (role == null) {
            return Role.USER;
        }
        return switch (role.toLowerCase()) {
            case "user" -> Role.USER;
            case "assistant" -> Role.ASSISTANT;
            case "system" -> Role.SYSTEM;
            case "tool" -> Role.TOOL;
            default -> Role.USER;
        };
    }


    private List<Message> toQwenMessages(LlmRequest llmRequest) {
        return llmRequest.getMessages().stream()
                .map(m -> Message.builder()
                        .role(mapRole(m.getRole()).getValue())
                        .content(m.getContent())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private LlmResponse toLlmResponse(GenerationResult result) {
        LlmResponse response = new LlmResponse();

        if (result == null) {
            return response;
        }
        response.setId(result.getRequestId());

        // Usage
        if (result.getUsage() != null) {
            LlmResponse.Usage usage = new LlmResponse.Usage();
            usage.setPromptTokens(result.getUsage().getInputTokens());
            usage.setCompletionTokens(result.getUsage().getOutputTokens());
            usage.setTotalTokens(result.getUsage().getTotalTokens());
            response.setUsage(usage);
        }

        // Choices
        if (result.getOutput() != null && result.getOutput().getChoices() != null) {
            List<LlmResponse.Choice> choices = result.getOutput().getChoices().stream().map(choice -> {
                LlmResponse.Choice c = new LlmResponse.Choice();
                if (choice.getMessage() != null) {
                    c.setText(choice.getMessage().getContent());
                    LlmResponse.Message m = new LlmResponse.Message();
                    m.setRole(choice.getMessage().getRole());
                    m.setContent(choice.getMessage().getContent());
                    c.setMessage(m);
                }
                c.setFinishReason(choice.getFinishReason());
                c.setIndex(choice.getIndex());
                return c;
            }).collect(Collectors.toList());
            response.setChoices(choices);
        }

        return response;
    }


    @Override
    public Flowable<LlmResponse> invoke(LlmRequest llmRequest, SystemContext systemContext) {
        List<Message> messages = toQwenMessages(llmRequest);

        if (InvokeMode.SSE.equals(systemContext.getInvokeMode())) {
            return invokeStream(llmRequest);
        }
        
        GenerationParam param = GenerationParam.builder()
                .model(llmRequest.getModelName())
                .apiKey(getApiKey())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
                
        if (llmRequest.getExtraParams() != null) {
            param.setParameters((Map<String, Object>) llmRequest.getExtraParams());
        }

        return Flowable.fromCallable(() -> {
            try {
                Generation gen = new Generation();
                GenerationResult result = gen.call(param);
                return toLlmResponse(result);
            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
                throw new RuntimeException("Qwen 调用失败: " + e.getMessage(), e);
            } catch (Throwable e) {
                throw new RuntimeException("Qwen 调用失败", e);
            }
        });
    }

    public Flowable<LlmResponse> invokeStream(LlmRequest llmRequest) {
        List<Message> messages = toQwenMessages(llmRequest);

        GenerationParam param = GenerationParam.builder()
                .model(llmRequest.getModelName())
                .apiKey(getApiKey())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .parameters((Map<? extends String, ?>) llmRequest.getExtraParams())
                .incrementalOutput(true)
                .build();

        return Flowable.create(emitter -> {
            try {
                Generation gen = new Generation();
                io.reactivex.Flowable<GenerationResult> stream = gen.streamCall(param);
                stream.blockingForEach(r -> emitter.onNext(toLlmResponse(r)));
                emitter.onComplete();
            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
                log.error("Qwen 流式调用失败: {}", e.getMessage(), e);
                emitter.onError(new RuntimeException("Qwen 流式调用失败: " + e.getMessage(), e));
            } catch (Exception e) {
                log.error("Qwen 流式调用失败", e);
                emitter.onError(new RuntimeException("Qwen 流式调用失败", e));
            }
        }, BackpressureStrategy.BUFFER);
    }

    @Value("${ali.agentic.adk.flownode.dashscope.apiKey:**}")
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    private String getApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            return PropertyConstant.dashscopeApiKey;
        }
        return apiKey;
    }
}