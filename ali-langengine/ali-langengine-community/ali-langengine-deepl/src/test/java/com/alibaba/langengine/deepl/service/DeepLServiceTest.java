package com.alibaba.langengine.deepl.service;

import com.alibaba.langengine.deepl.model.DeepLTranslateRequest;
import com.alibaba.langengine.deepl.model.DeepLTranslateResponse;
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
 * DeepL 服务测试
 *
 * @author Makoto
 */
@ExtendWith(MockitoExtension.class)
class DeepLServiceTest {

    private DeepLService deepLService;

    @BeforeEach
    void setUp() {
        deepLService = new DeepLService("https://api-free.deepl.com", Duration.ofSeconds(30), "test-api-key", false);
    }

    @Test
    void testServiceInitialization() {
        assertNotNull(deepLService);
        assertEquals("test-api-key", deepLService.getApiKey());
        assertFalse(deepLService.isPro());
    }

    @Test
    void testServiceApiClass() {
        assertEquals(DeepLApi.class, deepLService.getServiceApiClass());
    }

    @Test
    void testProServiceInitialization() {
        DeepLService proService = new DeepLService("https://api.deepl.com", Duration.ofSeconds(30), "pro-api-key", true);
        
        assertEquals("pro-api-key", proService.getApiKey());
        assertTrue(proService.isPro());
    }

    @Test
    void testTranslateRequestCreation() {
        DeepLTranslateRequest request = new DeepLTranslateRequest();
        request.setText(Arrays.asList("Hello world"));
        request.setSourceLang("en");
        request.setTargetLang("zh");
        
        assertNotNull(request.getText());
        assertEquals(1, request.getText().size());
        assertEquals("Hello world", request.getText().get(0));
        assertEquals("en", request.getSourceLang());
        assertEquals("zh", request.getTargetLang());
    }

    @Test
    void testTranslateResponseCreation() {
        DeepLTranslateResponse response = new DeepLTranslateResponse();
        DeepLTranslateResponse.Translation translation = new DeepLTranslateResponse.Translation();
        translation.setText("你好世界");
        translation.setDetectedSourceLanguage("en");
        
        response.setTranslations(Arrays.asList(translation));
        
        assertNotNull(response.getTranslations());
        assertEquals(1, response.getTranslations().size());
        assertEquals("你好世界", response.getTranslations().get(0).getText());
        assertEquals("en", response.getTranslations().get(0).getDetectedSourceLanguage());
    }

    @Test
    void testDefaultRequestValues() {
        DeepLTranslateRequest request = new DeepLTranslateRequest();
        
        assertEquals("1", request.getSplitSentences());
        assertEquals("0", request.getPreserveFormatting());
        assertEquals("1", request.getOutlineDetection());
    }

    @Test
    void testRequestWithAllParameters() {
        DeepLTranslateRequest request = new DeepLTranslateRequest();
        request.setText(Arrays.asList("Test text"));
        request.setSourceLang("auto");
        request.setTargetLang("zh");
        request.setSplitSentences("0");
        request.setPreserveFormatting("1");
        request.setFormality("formal");
        request.setGlossaryId("test-glossary");
        request.setTagHandling("xml");
        
        assertEquals("Test text", request.getText().get(0));
        assertEquals("auto", request.getSourceLang());
        assertEquals("zh", request.getTargetLang());
        assertEquals("0", request.getSplitSentences());
        assertEquals("1", request.getPreserveFormatting());
        assertEquals("formal", request.getFormality());
        assertEquals("test-glossary", request.getGlossaryId());
        assertEquals("xml", request.getTagHandling());
    }
}
