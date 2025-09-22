package com.alibaba.langengine.volcenginetranslate.service;

import com.alibaba.langengine.volcenginetranslate.model.VolcengineTranslateRequest;
import com.alibaba.langengine.volcenginetranslate.model.VolcengineTranslateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 火山翻译服务测试
 *
 * @author Makoto
 */
@ExtendWith(MockitoExtension.class)
class VolcengineTranslateServiceTest {

    private VolcengineTranslateService volcengineTranslateService;

    @BeforeEach
    void setUp() {
        volcengineTranslateService = new VolcengineTranslateService(
            "https://translate.volcengineapi.com", 
            Duration.ofSeconds(30), 
            "test-access-key", 
            "test-secret-key", 
            "cn-north-1", 
            "translate", 
            "2020-06-01", 
            "TranslateText"
        );
    }

    @Test
    void testServiceInitialization() {
        assertNotNull(volcengineTranslateService);
        assertEquals("test-access-key", volcengineTranslateService.getAccessKey());
        assertEquals("test-secret-key", volcengineTranslateService.getSecretKey());
        assertEquals("cn-north-1", volcengineTranslateService.getRegion());
        assertEquals("translate", volcengineTranslateService.getService());
        assertEquals("2020-06-01", volcengineTranslateService.getVersion());
        assertEquals("TranslateText", volcengineTranslateService.getAction());
    }

    @Test
    void testServiceApiClass() {
        assertEquals(VolcengineTranslateApi.class, volcengineTranslateService.getServiceApiClass());
    }

    @Test
    void testTranslateRequestCreation() {
        VolcengineTranslateRequest request = new VolcengineTranslateRequest();
        request.setSourceLanguage("en");
        request.setTargetLanguage("zh");
        request.setTextList(Arrays.asList("Hello world"));
        request.setScene("general");
        
        assertEquals("en", request.getSourceLanguage());
        assertEquals("zh", request.getTargetLanguage());
        assertNotNull(request.getTextList());
        assertEquals(1, request.getTextList().size());
        assertEquals("Hello world", request.getTextList().get(0));
        assertEquals("general", request.getScene());
    }

    @Test
    void testTranslateRequestWithGlossary() {
        VolcengineTranslateRequest request = new VolcengineTranslateRequest();
        VolcengineTranslateRequest.GlossaryInfo glossaryInfo = new VolcengineTranslateRequest.GlossaryInfo();
        glossaryInfo.setGlossaryTmx("test-glossary.tmx");
        request.setGlossaryInfo(glossaryInfo);
        
        assertNotNull(request.getGlossaryInfo());
        assertEquals("test-glossary.tmx", request.getGlossaryInfo().getGlossaryTmx());
    }

    @Test
    void testTranslateResponseCreation() {
        VolcengineTranslateResponse response = new VolcengineTranslateResponse();
        
        VolcengineTranslateResponse.ResponseMetadata metadata = new VolcengineTranslateResponse.ResponseMetadata();
        metadata.setRequestId("test-request-id");
        metadata.setAction("TranslateText");
        metadata.setVersion("2020-06-01");
        metadata.setService("translate");
        metadata.setRegion("cn-north-1");
        
        VolcengineTranslateResponse.Result result = new VolcengineTranslateResponse.Result();
        result.setDetectedLanguage("en");
        
        VolcengineTranslateResponse.Translation translation = new VolcengineTranslateResponse.Translation();
        translation.setTranslation("你好世界");
        translation.setDetectedSourceLanguage("en");
        translation.setExtra("extra-info");
        
        result.setTranslationList(Arrays.asList(translation));
        response.setResponseMetadata(metadata);
        response.setResult(result);
        
        assertNotNull(response.getResponseMetadata());
        assertEquals("test-request-id", response.getResponseMetadata().getRequestId());
        assertEquals("TranslateText", response.getResponseMetadata().getAction());
        assertEquals("2020-06-01", response.getResponseMetadata().getVersion());
        assertEquals("translate", response.getResponseMetadata().getService());
        assertEquals("cn-north-1", response.getResponseMetadata().getRegion());
        
        assertNotNull(response.getResult());
        assertEquals("en", response.getResult().getDetectedLanguage());
        assertNotNull(response.getResult().getTranslationList());
        assertEquals(1, response.getResult().getTranslationList().size());
        assertEquals("你好世界", response.getResult().getTranslationList().get(0).getTranslation());
        assertEquals("en", response.getResult().getTranslationList().get(0).getDetectedSourceLanguage());
        assertEquals("extra-info", response.getResult().getTranslationList().get(0).getExtra());
    }

    @Test
    void testTranslateResponseWithError() {
        VolcengineTranslateResponse response = new VolcengineTranslateResponse();
        VolcengineTranslateResponse.ResponseMetadata metadata = new VolcengineTranslateResponse.ResponseMetadata();
        VolcengineTranslateResponse.Error error = new VolcengineTranslateResponse.Error();
        error.setCode("InvalidParameter");
        error.setMessage("参数错误");
        metadata.setError(error);
        
        response.setResponseMetadata(metadata);
        
        assertNotNull(response.getResponseMetadata());
        assertNotNull(response.getResponseMetadata().getError());
        assertEquals("InvalidParameter", response.getResponseMetadata().getError().getCode());
        assertEquals("参数错误", response.getResponseMetadata().getError().getMessage());
    }

    @Test
    void testDefaultRequestValues() {
        VolcengineTranslateRequest request = new VolcengineTranslateRequest();
        
        assertEquals("general", request.getScene());
    }

    @Test
    void testRequestWithMultipleTexts() {
        VolcengineTranslateRequest request = new VolcengineTranslateRequest();
        request.setTextList(Arrays.asList("Hello", "World", "Test"));
        
        assertNotNull(request.getTextList());
        assertEquals(3, request.getTextList().size());
        assertEquals("Hello", request.getTextList().get(0));
        assertEquals("World", request.getTextList().get(1));
        assertEquals("Test", request.getTextList().get(2));
    }

    @Test
    void testResponseWithMultipleTranslations() {
        VolcengineTranslateResponse response = new VolcengineTranslateResponse();
        VolcengineTranslateResponse.Result result = new VolcengineTranslateResponse.Result();
        
        VolcengineTranslateResponse.Translation translation1 = new VolcengineTranslateResponse.Translation();
        translation1.setTranslation("你好");
        translation1.setDetectedSourceLanguage("en");
        
        VolcengineTranslateResponse.Translation translation2 = new VolcengineTranslateResponse.Translation();
        translation2.setTranslation("世界");
        translation2.setDetectedSourceLanguage("en");
        
        result.setTranslationList(Arrays.asList(translation1, translation2));
        response.setResult(result);
        
        assertNotNull(response.getResult());
        assertNotNull(response.getResult().getTranslationList());
        assertEquals(2, response.getResult().getTranslationList().size());
        assertEquals("你好", response.getResult().getTranslationList().get(0).getTranslation());
        assertEquals("世界", response.getResult().getTranslationList().get(1).getTranslation());
    }
}
