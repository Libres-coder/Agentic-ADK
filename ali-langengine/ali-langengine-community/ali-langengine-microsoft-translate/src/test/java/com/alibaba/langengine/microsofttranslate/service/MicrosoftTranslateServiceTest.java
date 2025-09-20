package com.alibaba.langengine.microsofttranslate.service;

import com.alibaba.langengine.microsofttranslate.model.MicrosoftTranslateRequest;
import com.alibaba.langengine.microsofttranslate.model.MicrosoftTranslateResponse;
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
 * Microsoft 翻译服务测试
 *
 * @author Makoto
 */
@ExtendWith(MockitoExtension.class)
class MicrosoftTranslateServiceTest {

    private MicrosoftTranslateService microsoftTranslateService;

    @BeforeEach
    void setUp() {
        microsoftTranslateService = new MicrosoftTranslateService(
            "https://api.cognitive.microsofttranslator.com", 
            Duration.ofSeconds(30), 
            "test-subscription-key", 
            "eastus", 
            "3.0"
        );
    }

    @Test
    void testServiceInitialization() {
        assertNotNull(microsoftTranslateService);
        assertEquals("test-subscription-key", microsoftTranslateService.getSubscriptionKey());
        assertEquals("eastus", microsoftTranslateService.getRegion());
        assertEquals("3.0", microsoftTranslateService.getApiVersion());
    }

    @Test
    void testServiceApiClass() {
        assertEquals(MicrosoftTranslateApi.class, microsoftTranslateService.getServiceApiClass());
    }

    @Test
    void testTranslateRequestCreation() {
        MicrosoftTranslateRequest request = new MicrosoftTranslateRequest();
        request.setText(Arrays.asList("Hello world"));
        request.setTo("zh-Hans");
        request.setFrom("en");
        
        assertNotNull(request.getText());
        assertEquals(1, request.getText().size());
        assertEquals("Hello world", request.getText().get(0));
        assertEquals("zh-Hans", request.getTo());
        assertEquals("en", request.getFrom());
    }

    @Test
    void testTranslateResponseCreation() {
        MicrosoftTranslateResponse response = new MicrosoftTranslateResponse();
        
        MicrosoftTranslateResponse.DetectedLanguage detectedLanguage = new MicrosoftTranslateResponse.DetectedLanguage();
        detectedLanguage.setLanguage("en");
        detectedLanguage.setScore(0.95);
        
        MicrosoftTranslateResponse.Translation translation = new MicrosoftTranslateResponse.Translation();
        translation.setText("你好世界");
        translation.setTo("zh-Hans");
        
        response.setDetectedLanguage(detectedLanguage);
        response.setTranslations(Arrays.asList(translation));
        
        assertNotNull(response.getTranslations());
        assertEquals(1, response.getTranslations().size());
        assertEquals("你好世界", response.getTranslations().get(0).getText());
        assertEquals("zh-Hans", response.getTranslations().get(0).getTo());
        assertEquals("en", response.getDetectedLanguage().getLanguage());
        assertEquals(0.95, response.getDetectedLanguage().getScore());
    }

    @Test
    void testDefaultRequestValues() {
        MicrosoftTranslateRequest request = new MicrosoftTranslateRequest();
        
        assertEquals("Plain", request.getTextType());
        assertEquals("general", request.getCategory());
        assertEquals("NoAction", request.getProfanityAction());
        assertEquals("Asterisk", request.getProfanityMarker());
        assertFalse(request.isIncludeAlignment());
        assertFalse(request.isIncludeSentenceLength());
        assertTrue(request.isAllowFallback());
    }

    @Test
    void testRequestWithAllParameters() {
        MicrosoftTranslateRequest request = new MicrosoftTranslateRequest();
        request.setText(Arrays.asList("Test text"));
        request.setTo("zh-Hans");
        request.setFrom("en");
        request.setTextType("Html");
        request.setCategory("medical");
        request.setProfanityAction("Marked");
        request.setProfanityMarker("Tag");
        request.setIncludeAlignment(true);
        request.setIncludeSentenceLength(true);
        request.setSuggestedFrom("auto");
        request.setFromScript("Latn");
        request.setToScript("Hans");
        request.setAllowFallback(false);
        
        assertEquals("Test text", request.getText().get(0));
        assertEquals("zh-Hans", request.getTo());
        assertEquals("en", request.getFrom());
        assertEquals("Html", request.getTextType());
        assertEquals("medical", request.getCategory());
        assertEquals("Marked", request.getProfanityAction());
        assertEquals("Tag", request.getProfanityMarker());
        assertTrue(request.isIncludeAlignment());
        assertTrue(request.isIncludeSentenceLength());
        assertEquals("auto", request.getSuggestedFrom());
        assertEquals("Latn", request.getFromScript());
        assertEquals("Hans", request.getToScript());
        assertFalse(request.isAllowFallback());
    }

    @Test
    void testTranslationWithAlignment() {
        MicrosoftTranslateResponse.Translation translation = new MicrosoftTranslateResponse.Translation();
        MicrosoftTranslateResponse.Alignment alignment = new MicrosoftTranslateResponse.Alignment();
        alignment.setProj("0:0-5:5");
        translation.setAlignment(alignment);
        
        assertNotNull(translation.getAlignment());
        assertEquals("0:0-5:5", translation.getAlignment().getProj());
    }

    @Test
    void testTranslationWithSentenceLength() {
        MicrosoftTranslateResponse.Translation translation = new MicrosoftTranslateResponse.Translation();
        MicrosoftTranslateResponse.SentenceLength sentenceLength = new MicrosoftTranslateResponse.SentenceLength();
        sentenceLength.setSrcSentLen(Arrays.asList(11));
        sentenceLength.setTransSentLen(Arrays.asList(4));
        translation.setSentLen(sentenceLength);
        
        assertNotNull(translation.getSentLen());
        assertEquals(1, translation.getSentLen().getSrcSentLen().size());
        assertEquals(1, translation.getSentLen().getTransSentLen().size());
        assertEquals(11, translation.getSentLen().getSrcSentLen().get(0));
        assertEquals(4, translation.getSentLen().getTransSentLen().get(0));
    }
}
