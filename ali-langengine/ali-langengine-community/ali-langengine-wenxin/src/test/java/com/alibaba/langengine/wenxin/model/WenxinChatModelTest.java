package com.alibaba.langengine.wenxin.model;

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;


public class WenxinChatModelTest {

    private WenxinChatModel chatModel;
    private static final String TEST_API_KEY = "test_api_key";
    private static final String TEST_SECRET_KEY = "test_secret_key";

    @BeforeEach
    public void setUp() {
        chatModel = new WenxinChatModel(TEST_API_KEY, TEST_SECRET_KEY);
    }

    @Test
    public void testModelInitialization() {
        assertNotNull(chatModel, "WenxinChatModel should be successfully initialized");
        assertEquals(TEST_API_KEY, chatModel.getApiKey(), "API key should be set correctly");
        assertEquals(TEST_SECRET_KEY, chatModel.getSecretKey(), "Secret key should be set correctly");
    }

    @Test
    public void testModelConfiguration() {
        // 测试模型配置
        chatModel.setModel("ERNIE-4.0-8K");
        chatModel.setMaxTokens(1000);
        chatModel.setTemperature(0.7);
        
        assertEquals("ERNIE-4.0-8K", chatModel.getModel(), "Model should be set correctly");
        assertEquals(1000, chatModel.getMaxTokens(), "Max tokens should be set correctly");
        assertEquals(0.7, chatModel.getTemperature(), 0.001, "Temperature should be set correctly");
    }

    @Test
    public void testMessagePreparation() {
        // 测试消息准备
        HumanMessage message = new HumanMessage();
        message.setContent("测试消息");
        
        assertNotNull(message, "HumanMessage should be created");
        assertEquals("测试消息", message.getContent(), "Message content should be set correctly");
    }

    @Test
    public void testModelDefaultValues() {
        // 测试默认值
        WenxinChatModel defaultModel = new WenxinChatModel("key", "secret");
        
        assertNotNull(defaultModel.getModel(), "Default model should not be null");
        assertTrue(defaultModel.getMaxTokens() > 0, "Default max tokens should be positive");
        assertTrue(defaultModel.getTemperature() >= 0 && defaultModel.getTemperature() <= 2, 
                  "Default temperature should be in valid range");
    }

    @Test
    public void testModelPropertySetters() {
        // 测试属性设置器
        chatModel.setServerUrl("https://test.example.com/");
        assertEquals("https://test.example.com/", chatModel.getServerUrl(), "Server URL should be set correctly");
        
        chatModel.setTimeout(java.time.Duration.ofSeconds(30));
        assertEquals(java.time.Duration.ofSeconds(30), chatModel.getTimeout(), "Timeout should be set correctly");
    }

    @Test
    public void testModelServiceInitialization() {
        // 测试服务初始化
        assertNotNull(chatModel.getWenxinService(), "Wenxin service should be initialized");
    }

    @Test
    public void testModelMethodsExist() {
        // 验证必要的方法是否存在
        try {
            Class<?> modelClass = WenxinChatModel.class;
            assertTrue(modelClass.getDeclaredMethods().length > 0, "Model should have methods");
        } catch (Exception e) {
            fail("Model class should be accessible: " + e.getMessage());
        }
    }
}