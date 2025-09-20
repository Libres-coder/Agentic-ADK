package com.alibaba.langengine.wenxin.service;

import com.alibaba.langengine.wenxin.model.completion.WenxinCompletionRequest;
import com.alibaba.langengine.wenxin.model.embedding.WenxinEmbeddingRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;


public class WenxinServiceTest {

    private WenxinService wenxinService;
    private static final String TEST_API_KEY = "test_api_key";
    private static final String TEST_SECRET_KEY = "test_secret_key";
    private static final String TEST_SERVER_URL = "https://aip.baidubce.com/";

    @BeforeEach
    public void setUp() {
        wenxinService = new WenxinService(TEST_SERVER_URL, Duration.ofSeconds(30), TEST_API_KEY, TEST_SECRET_KEY);
    }

    @Test
    public void testServiceInitialization() {
        assertNotNull(wenxinService, "WenxinService should be successfully initialized");
        assertEquals(TEST_API_KEY, wenxinService.getApiKey(), "API key should be set correctly");
        assertEquals(TEST_SECRET_KEY, wenxinService.getSecretKey(), "Secret key should be set correctly");
        assertEquals(TEST_SERVER_URL, wenxinService.getServerUrl(), "Server URL should be set correctly");
    }

    @Test
    public void testServiceInstanceCreation() {
        WenxinService service1 = new WenxinService(TEST_SERVER_URL, Duration.ofSeconds(30), "key1", "secret1");
        WenxinService service2 = new WenxinService(TEST_SERVER_URL, Duration.ofSeconds(30), "key2", "secret2");
        
        assertNotNull(service1, "First service instance should not be null");
        assertNotNull(service2, "Second service instance should not be null");
        assertNotSame(service1, service2, "Different instances should not be the same object");
    }

    @Test
    public void testServiceClassStructure() {
        // 验证类是否存在必要的方法
        try {
            Class<?> serviceClass = WenxinService.class;
            assertNotNull(serviceClass.getDeclaredMethod("createCompletion", WenxinCompletionRequest.class), 
                         "createCompletion method should exist");
            assertNotNull(serviceClass.getDeclaredMethod("createEmbedding", WenxinEmbeddingRequest.class), 
                         "createEmbedding method should exist");
        } catch (NoSuchMethodException e) {
            fail("Required methods should exist in WenxinService: " + e.getMessage());
        }
    }

    @Test
    public void testServiceConfiguration() {
        // 测试服务配置
        assertEquals(Duration.ofSeconds(30), wenxinService.getTimeout(), "Timeout should be set correctly");
        assertNotNull(wenxinService.getAuthService(), "Auth service should be initialized");
    }

    @Test
    public void testServiceBasicFunctionality() {
        // 基础功能测试 - 不依赖API调用
        assertNotNull(wenxinService.getHttpClient(), "HTTP client should be initialized");
        assertTrue(wenxinService.getTimeout().getSeconds() > 0, "Timeout should be positive");
    }
}