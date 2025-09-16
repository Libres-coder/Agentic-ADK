package com.alibaba.langengine.tencenttranslate.service;

import com.alibaba.langengine.tencenttranslate.model.TencentTranslateRequest;
import com.alibaba.langengine.tencenttranslate.model.TencentTranslateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 腾讯翻译服务测试
 *
 * @author Makoto
 */
@ExtendWith(MockitoExtension.class)
class TencentTranslateServiceTest {

    private TencentTranslateService tencentTranslateService;

    @BeforeEach
    void setUp() {
        tencentTranslateService = new TencentTranslateService(
            "https://tmt.tencentcloudapi.com", 
            Duration.ofSeconds(30), 
            "test-secret-id", 
            "test-secret-key", 
            "ap-beijing", 
            "tmt", 
            "2018-03-21", 
            "TextTranslate"
        );
    }

    @Test
    void testServiceInitialization() {
        assertNotNull(tencentTranslateService);
        assertEquals("test-secret-id", tencentTranslateService.getSecretId());
        assertEquals("test-secret-key", tencentTranslateService.getSecretKey());
        assertEquals("ap-beijing", tencentTranslateService.getRegion());
        assertEquals("tmt", tencentTranslateService.getService());
        assertEquals("2018-03-21", tencentTranslateService.getVersion());
        assertEquals("TextTranslate", tencentTranslateService.getAction());
    }

    @Test
    void testServiceApiClass() {
        assertEquals(TencentTranslateApi.class, tencentTranslateService.getServiceApiClass());
    }

    @Test
    void testTranslateRequestCreation() {
        TencentTranslateRequest request = new TencentTranslateRequest();
        request.setSourceText("Hello world");
        request.setSource("en");
        request.setTarget("zh");
        request.setProjectId(12345L);
        request.setUntranslatedText("Hello world");
        
        assertEquals("Hello world", request.getSourceText());
        assertEquals("en", request.getSource());
        assertEquals("zh", request.getTarget());
        assertEquals(12345L, request.getProjectId());
        assertEquals("Hello world", request.getUntranslatedText());
    }

    @Test
    void testTranslateResponseCreation() {
        TencentTranslateResponse response = new TencentTranslateResponse();
        TencentTranslateResponse.Response responseData = new TencentTranslateResponse.Response();
        responseData.setTargetText("你好世界");
        responseData.setSource("en");
        responseData.setTarget("zh");
        responseData.setRequestId("test-request-id");
        
        response.setResponse(responseData);
        
        assertNotNull(response.getResponse());
        assertEquals("你好世界", response.getResponse().getTargetText());
        assertEquals("en", response.getResponse().getSource());
        assertEquals("zh", response.getResponse().getTarget());
        assertEquals("test-request-id", response.getResponse().getRequestId());
    }

    @Test
    void testTranslateResponseWithError() {
        TencentTranslateResponse response = new TencentTranslateResponse();
        TencentTranslateResponse.Response responseData = new TencentTranslateResponse.Response();
        TencentTranslateResponse.Error error = new TencentTranslateResponse.Error();
        error.setCode("InvalidParameter");
        error.setMessage("参数错误");
        responseData.setError(error);
        
        response.setResponse(responseData);
        
        assertNotNull(response.getResponse());
        assertNotNull(response.getResponse().getError());
        assertEquals("InvalidParameter", response.getResponse().getError().getCode());
        assertEquals("参数错误", response.getResponse().getError().getMessage());
    }

    @Test
    void testRequestWithProjectId() {
        TencentTranslateRequest request = new TencentTranslateRequest();
        request.setSourceText("Test text");
        request.setSource("auto");
        request.setTarget("zh");
        request.setProjectId(99999L);
        
        assertEquals("Test text", request.getSourceText());
        assertEquals("auto", request.getSource());
        assertEquals("zh", request.getTarget());
        assertEquals(99999L, request.getProjectId());
    }

    @Test
    void testRequestWithUntranslatedText() {
        TencentTranslateRequest request = new TencentTranslateRequest();
        request.setSourceText("Hello");
        request.setUntranslatedText("World");
        
        assertEquals("Hello", request.getSourceText());
        assertEquals("World", request.getUntranslatedText());
    }

    @Test
    void testResponseWithAllFields() {
        TencentTranslateResponse response = new TencentTranslateResponse();
        TencentTranslateResponse.Response responseData = new TencentTranslateResponse.Response();
        responseData.setTargetText("翻译结果");
        responseData.setSource("en");
        responseData.setTarget("zh");
        responseData.setRequestId("req-123456");
        
        TencentTranslateResponse.Error error = new TencentTranslateResponse.Error();
        error.setCode("Success");
        error.setMessage("成功");
        responseData.setError(error);
        
        response.setResponse(responseData);
        
        assertEquals("翻译结果", response.getResponse().getTargetText());
        assertEquals("en", response.getResponse().getSource());
        assertEquals("zh", response.getResponse().getTarget());
        assertEquals("req-123456", response.getResponse().getRequestId());
        assertEquals("Success", response.getResponse().getError().getCode());
        assertEquals("成功", response.getResponse().getError().getMessage());
    }
}
