package com.alibaba.langengine.jieyue.model;

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.jieyue.JieYueConfiguration;
import com.alibaba.langengine.jieyue.JieYueModelConstant;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@Slf4j
public class JieYueChatModelTest {

    private static boolean hasApiKey = false;

    @BeforeAll
    static void checkApiKey() {
        hasApiKey = JieYueConfiguration.JIEYUE_API_KEY != null && !JieYueConfiguration.JIEYUE_API_KEY.trim().isEmpty();
        if (!hasApiKey) {
            System.out.println("API key not configured. Tests will be skipped. Configure jieyue_api_key in application.properties or set STEP_API_KEY environment variable.");
        }
    }

    @Test
    public void test_run() {
        Assumptions.assumeTrue(hasApiKey, "API key is required for this test");

        JieYueChatModel chatModel = new JieYueChatModel();
        chatModel.setModel(JieYueModelConstant.STEP_1_8K);
        chatModel.setTemperature(0.9d);
        chatModel.setMaxTokens(1024);

        String response = chatModel.predict("Hello, please introduce JieYue AI in one sentence.");
        System.out.println("Basic chat test completed!");
        System.out.println("Response: " + response);
    }

    @Test
    public void test_stream() {
        Assumptions.assumeTrue(hasApiKey, "API key is required for this test");

        JieYueChatModel chatModel = new JieYueChatModel();
        chatModel.setModel(JieYueModelConstant.STEP_2_MINI);
        chatModel.setTemperature(0.7d);
        chatModel.setMaxTokens(500);

        String response = chatModel.predict("Explain machine learning in simple terms.", 
            message -> {
                System.out.print(message.getContent());
            });
        
        System.out.println("\nStream chat test completed!");
        System.out.println("Full response: " + response);
    }

    @Test
    public void test_models() {
        Assumptions.assumeTrue(hasApiKey, "API key is required for this test");

        String[] models = {
            JieYueModelConstant.STEP_1_8K,
            JieYueModelConstant.STEP_2_MINI,
            JieYueModelConstant.STEP_2_16K
        };

        for (String model : models) {
            JieYueChatModel chatModel = new JieYueChatModel();
            chatModel.setModel(model);
            chatModel.setTemperature(0.5d);
            chatModel.setMaxTokens(100);

            String response = chatModel.predict("Say hello in Chinese.");
            System.out.println("Model " + model + " test completed!");
            System.out.println("Response: " + response);
        }
    }
}