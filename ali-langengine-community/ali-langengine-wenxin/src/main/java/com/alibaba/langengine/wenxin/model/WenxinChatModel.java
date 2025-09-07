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
package com.alibaba.langengine.wenxin.model;

import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.wenxin.model.completion.WenxinCompletionRequest;
import com.alibaba.langengine.wenxin.model.completion.WenxinCompletionResult;
import com.alibaba.langengine.wenxin.model.completion.WenxinMessage;
import com.alibaba.langengine.wenxin.service.WenxinService;
import com.alibaba.langengine.wenxin.WenxinModelName;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 文心一言聊天模型 - 使用简化的实现方式
 */
@Slf4j
@Data
public class WenxinChatModel {

    private String apiKey;
    private String secretKey;
    private String model = WenxinModelName.ERNIE_4_0_8K;
    private String serverUrl = "https://aip.baidubce.com/";
    private Duration timeout = Duration.ofSeconds(60);
    private Double temperature = 0.95;
    private Integer maxTokens = 1024;
    
    private WenxinService wenxinService;

    public WenxinChatModel(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.wenxinService = new WenxinService(serverUrl, timeout, apiKey, secretKey);
    }

    public BaseMessage run(List<BaseMessage> messages) {
        return run(messages, null, null, null);
    }

    public BaseMessage run(List<BaseMessage> messages, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        WenxinCompletionRequest request = buildRequest(messages, stops, extraAttributes);
        WenxinCompletionResult result = wenxinService.createCompletion(request);
        
        BaseMessage response = new HumanMessage();
        if (result != null && result.getResult() != null) {
            response.setContent(result.getResult());
        }
        
        if (consumer != null) {
            consumer.accept(response);
        }
        
        return response;
    }

    private WenxinCompletionRequest buildRequest(List<BaseMessage> messages, List<String> stops, Map<String, Object> extraAttributes) {
        List<WenxinMessage> wenxinMessages = new ArrayList<>();
        for (BaseMessage message : messages) {
            WenxinMessage wenxinMessage = new WenxinMessage();
            wenxinMessage.setRole("user");
            wenxinMessage.setContent(message.getContent());
            wenxinMessages.add(wenxinMessage);
        }
        
        return WenxinCompletionRequest.builder()
                .messages(wenxinMessages)
                .temperature(temperature)
                .maxOutputTokens(maxTokens)
                .stream(false)
                .build();
    }

    public String getLlmModelName() {
        return model;
    }
}
