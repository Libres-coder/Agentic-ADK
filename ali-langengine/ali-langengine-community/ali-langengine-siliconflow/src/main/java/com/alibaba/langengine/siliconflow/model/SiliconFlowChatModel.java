package com.alibaba.langengine.siliconflow.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.ResponseCollector;
import com.alibaba.langengine.core.model.fastchat.completion.chat.*;
import com.alibaba.langengine.core.model.fastchat.service.FastChatService;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.siliconflow.SiliconFlowConfiguration.*;

/**
 * SiliconFlow ChatModel for large language models, using chatMessage approach
 * Compatible with OpenAI API format
 */
@Slf4j
@Data
public class SiliconFlowChatModel extends BaseChatModel<ChatCompletionRequest> {

    private FastChatService service;

    private static final String DEFAULT_BASE_URL = "https://api.siliconflow.cn/v1";

    public SiliconFlowChatModel() {
        this(null);
    }

    public SiliconFlowChatModel(String baseUrl) {
        this(baseUrl, SILICONFLOW_API_KEY);
    }

    public SiliconFlowChatModel(String baseUrl, String apiKey) {
        setModel(SiliconFlowModelConstants.DEFAULT_MODEL);
        String serverUrl;
        if(!StringUtils.isEmpty(baseUrl)) {
            serverUrl = baseUrl;
        } else {
            serverUrl = !StringUtils.isEmpty(SILICONFLOW_SERVER_URL) ? SILICONFLOW_SERVER_URL : DEFAULT_BASE_URL;
        }
        service = new FastChatService(serverUrl, Duration.ofSeconds(Long.parseLong(SILICONFLOW_AI_TIMEOUT)), true, apiKey);
        
        // Set default parameters to prevent garbled text as mentioned in docs
        setTemperature(0.7d);
        setTopP(0.9d);
        setFrequencyPenalty(0.0d);
    }

    /**
     * user identifier
     */
    private String user;

    /**
     * logit bias for token probability adjustment
     */
    private Map<String, Integer> logitBias;

    /**
     * top_k parameter for token sampling
     */
    private Integer topK;

    /**
     * whether model returns json format result
     */
    private boolean jsonMode = false;

    /**
     * whether to use streaming incremental output
     */
    private boolean sseInc = true;

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        ChatCompletionRequest.ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder();
        builder.messages(chatMessages);
        
        if(user != null) {
            builder.user(user);
        }
        if(logitBias != null) {
            builder.logitBias(logitBias);
        }
        if(topK != null) {
            // Add top_k to extra parameters if supported
            Map<String, Object> extras = new HashMap<>();
            extras.put("top_k", topK);
            if(extraAttributes != null) {
                extras.putAll(extraAttributes);
            }
            extraAttributes = extras;
        }
        if(jsonMode) {
            builder.responseFormat(new HashMap<String, String>() {{
                put("type", "json_object");
            }});
        }
        
        // Handle function/tool definitions
        if(!CollectionUtils.isEmpty(functions)) {
            List<ToolDefinition> toolDefinitions = functions.stream().map(e -> {
                ToolDefinition toolDefinition = new ToolDefinition();
                ToolFunction toolFunction = new ToolFunction();
                toolFunction.setName(e.getName());
                toolFunction.setDescription(e.getDescription());

                FunctionParameter toolParameter = new FunctionParameter();
                if(e.getParameters() != null) {
                    toolParameter.setProperties(e.getParameters().getProperties());
                    toolParameter.setType(e.getParameters().getType());
                    toolParameter.setRequired(e.getParameters().getRequired());
                }
                toolFunction.setParameters(toolParameter);
                toolDefinition.setFunction(toolFunction);
                return toolDefinition;
            }).collect(Collectors.toList());
            builder.tools(toolDefinitions);
        }
        
        return builder.build();
    }

    @Override
    public BaseMessage runRequest(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();
        service.createChatCompletion(request).getChoices().forEach(e -> {
            ChatMessage chatMessage = e.getMessage();
            if(chatMessage != null) {
                BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                if (message != null) {
                    message.setOrignalContent(JSON.toJSONString(e));
                    baseMessage.set(message);
                }
            }
        });
        BaseMessage finalMessage = baseMessage.get();
        log.info("finalMessage response is {}", JSON.toJSONString(finalMessage));
        return finalMessage;
    }

    @Override
    public BaseMessage runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<Object> functionCallContent = new AtomicReference<>();
        AtomicReference<ResponseCollector> functionCallNameContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<ResponseCollector> argumentContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<String> role = new AtomicReference<>();
        
        service.streamChatCompletion(request)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(e -> {
                    log.info("chunk result is {}", JSON.toJSONString(e));
                    if(CollectionUtils.isEmpty(e.getChoices())) {
                        log.error("chunk result choices is empty");
                        return;
                    }

                    ChatCompletionChoice choice = e.getChoices().get(0);
                    if("stop".equals(choice.getFinishReason())
                            || "function_call".equals(choice.getFinishReason())) {
                        return;
                    }
                    
                    ChatMessage chatMessage = choice.getMessage();
                    if(chatMessage != null) {
                        if(!StringUtils.isEmpty(chatMessage.getRole())) {
                            role.set(chatMessage.getRole());
                        }
                        chatMessage.setRole(role.get());
                        BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                        if(message != null) {
                            // Handle regular content
                            if(chatMessage.getContent() != null) {
                                String answer = chatMessage.getContent().toString();
                                log.warn(getModel() + " chat stream answer is {}", answer);
                                answerContent.get().collect(message.getContent());
                                String response = answerContent.get().joining();
                                message.setContent(response);
                                baseMessage.set(message);
                                if (consumer != null) {
                                    consumer.accept(message);
                                }
                            } 
                            // Handle function calls
                            else if (chatMessage.getFunctionCall() != null && chatMessage.getFunctionCall().size() > 0) {
                                Map<String, Object> functionCallMap = Maps.newHashMap();
                                if (chatMessage.getFunctionCall().get("name") != null) {
                                    functionCallNameContent.get().collect(chatMessage.getFunctionCall().get("name").toString());
                                }
                                if (chatMessage.getFunctionCall().get("arguments") != null) {
                                    argumentContent.get().collect(chatMessage.getFunctionCall().get("arguments").toString());
                                }
                                functionCallMap.put("function_call", new HashMap<String, String>() {{
                                    put("name", functionCallNameContent.get().joining());
                                    put("arguments", argumentContent.get().joining());
                                }});
                                functionCallContent.set(functionCallMap);

                                if (functionCallContent.get() != null && consumer != null) {
                                    String functionCallContentString = JSON.toJSONString(functionCallContent.get());
                                    log.warn(getModel() + " functionCall stream answer is {}", functionCallContentString);
                                    AIMessage aiMessage = new AIMessage();
                                    aiMessage.setAdditionalKwargs((Map<String, Object>) functionCallContent.get());
                                    consumer.accept(aiMessage);
                                }
                            }
                        }
                    }
                });

        // Return function call result if exists
        if(functionCallContent.get() != null) {
            AIMessage aiMessage = new AIMessage();
            aiMessage.setAdditionalKwargs((Map<String, Object>)functionCallContent.get());
            baseMessage.set(aiMessage);
            log.info("functionCallContent get is {}", JSON.toJSONString(aiMessage));
            return baseMessage.get();
        }

        // Return final answer
        String responseContent = answerContent.get().joining();
        log.warn(getModel() + " final answer:" + responseContent);

        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent(responseContent);
        baseMessage.set(aiMessage);

        return baseMessage.get();
    }

    // Getters and setters for additional parameters
    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Map<String, Integer> getLogitBias() {
        return logitBias;
    }

    public void setLogitBias(Map<String, Integer> logitBias) {
        this.logitBias = logitBias;
    }

    public boolean isJsonMode() {
        return jsonMode;
    }

    public void setJsonMode(boolean jsonMode) {
        this.jsonMode = jsonMode;
    }

    public boolean isSseInc() {
        return sseInc;
    }

    public void setSseInc(boolean sseInc) {
        this.sseInc = sseInc;
    }
}