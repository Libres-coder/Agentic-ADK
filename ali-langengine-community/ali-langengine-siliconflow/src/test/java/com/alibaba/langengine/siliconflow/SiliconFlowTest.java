package com.alibaba.langengine.siliconflow;

import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.runnables.RunnableModelInput;
import com.alibaba.langengine.siliconflow.model.SiliconFlowChatModel;
import com.alibaba.langengine.siliconflow.model.SiliconFlowModelConstants;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * SiliconFlow integration tests
 * Requires SILICONFLOW_API_KEY environment variable to be set
 */
public class SiliconFlowTest {

    @Test
    public void testBasicChat() {
        // 先检查API密钥是否正确读取
        System.out.println("API Key loaded: " + (com.alibaba.langengine.siliconflow.SiliconFlowConfiguration.SILICONFLOW_API_KEY != null ?
            com.alibaba.langengine.siliconflow.SiliconFlowConfiguration.SILICONFLOW_API_KEY.substring(0, 10) + "..." : "null"));

        SiliconFlowChatModel chatModel = new SiliconFlowChatModel();
        // 使用一个免费的模型进行测试
        chatModel.setModel(SiliconFlowModelConstants.QWEN2_5_7B_INSTRUCT);

        String response = chatModel.predict("Hello, how are you?");
        System.out.println("Basic Chat Response: " + response);

        assert response != null && !response.trim().isEmpty();
    }

    @Test
    public void testStreamChat() {
        SiliconFlowChatModel chatModel = new SiliconFlowChatModel();
        chatModel.setModel(SiliconFlowModelConstants.QWEN2_5_7B_INSTRUCT);

        RunnableModelInput input = new RunnableModelInput();
        input.setMessages(Arrays.asList(new HumanMessage("Explain quantum computing in simple terms")));

        Object response = chatModel.stream(input, null, message -> System.out.print(message));

        System.out.println("\nStream Chat Complete: " + response);
        assert response != null;
    }

    @Test
    public void testCodingModel() {
        SiliconFlowChatModel chatModel = new SiliconFlowChatModel();
        chatModel.setModel(SiliconFlowModelConstants.QWEN2_5_CODER_7B_INSTRUCT);
        
        String response = chatModel.predict("Write a Python function to calculate fibonacci numbers");
        System.out.println("Coding Response: " + response);
        
        assert response != null && response.contains("def");
    }

    @Test
    public void testJsonMode() {
        SiliconFlowChatModel chatModel = new SiliconFlowChatModel();
        chatModel.setModel(SiliconFlowModelConstants.DEEPSEEK_V2_5);
        chatModel.setJsonMode(true);
        
        String response = chatModel.predict("Return information about cats in JSON format with fields: name, species, characteristics");
        System.out.println("JSON Response: " + response);
        
        assert response != null && response.contains("{") && response.contains("}");
    }

    @Test
    public void testReasoningModel() {
        SiliconFlowChatModel chatModel = new SiliconFlowChatModel();
        chatModel.setModel(SiliconFlowModelConstants.DEEPSEEK_R1_DISTILL_7B);
        
        String response = chatModel.predict("Solve this step by step: If a train travels at 60 mph for 2.5 hours, how far does it travel?");
        System.out.println("Reasoning Response: " + response);
        
        assert response != null && response.contains("150");
    }

    @Test
    public void testCustomParameters() {
        SiliconFlowChatModel chatModel = new SiliconFlowChatModel();
        chatModel.setModel(SiliconFlowModelConstants.GLM_4_9B_CHAT);
        chatModel.setTemperature(0.9);
        chatModel.setTopP(0.8);
        chatModel.setTopK(50);
        chatModel.setMaxTokens(500);
        
        String response = chatModel.predict("Write a creative short story about a robot learning to paint");
        System.out.println("Creative Response: " + response);
        
        assert response != null && !response.trim().isEmpty();
    }

    @Test
    public void testModelConstants() {
        // Test that model constants are properly defined
        assert SiliconFlowModelConstants.DEFAULT_MODEL != null;
        assert SiliconFlowModelConstants.TEXT_GENERATION_MODELS.length > 0;
        assert SiliconFlowModelConstants.CODING_MODELS.length > 0;
        assert SiliconFlowModelConstants.REASONING_MODELS.length > 0;
        assert SiliconFlowModelConstants.FREE_TIER_MODELS.length > 0;
        
        System.out.println("Model constants validation passed");
        System.out.println("Default model: " + SiliconFlowModelConstants.DEFAULT_MODEL);
        System.out.println("Available text generation models: " + SiliconFlowModelConstants.TEXT_GENERATION_MODELS.length);
        System.out.println("Available coding models: " + SiliconFlowModelConstants.CODING_MODELS.length);
        System.out.println("Available reasoning models: " + SiliconFlowModelConstants.REASONING_MODELS.length);
        System.out.println("Free tier models: " + SiliconFlowModelConstants.FREE_TIER_MODELS.length);
    }
}