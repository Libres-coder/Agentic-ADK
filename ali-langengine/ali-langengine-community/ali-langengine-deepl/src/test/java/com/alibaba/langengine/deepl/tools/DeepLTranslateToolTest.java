package com.alibaba.langengine.deepl.tools;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DeepL 翻译工具测试
 *
 * @author Makoto
 */
@ExtendWith(MockitoExtension.class)
class DeepLTranslateToolTest {

    private DeepLTranslateTool deepLTranslateTool;

    @BeforeEach
    void setUp() {
        deepLTranslateTool = new DeepLTranslateTool("test-api-key", false, 30);
    }

    @Test
    void testToolName() {
        assertEquals("DeepLTranslateTool", deepLTranslateTool.getName());
    }

    @Test
    void testToolDescription() {
        assertTrue(deepLTranslateTool.getDescription().contains("DeepL"));
        assertTrue(deepLTranslateTool.getDescription().contains("翻译"));
    }

    @Test
    void testRunWithEmptyInput() {
        ToolExecuteResult result = deepLTranslateTool.run("");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("翻译文本不能为空"));
    }

    @Test
    void testRunWithNullInput() {
        ToolExecuteResult result = deepLTranslateTool.run(null);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("翻译文本不能为空"));
    }

    @Test
    void testRunWithBlankInput() {
        ToolExecuteResult result = deepLTranslateTool.run("   ");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("翻译文本不能为空"));
    }

    @Test
    void testSetSourceLanguage() {
        deepLTranslateTool.setSourceLang("en");
        assertEquals("en", deepLTranslateTool.getSourceLang());
    }

    @Test
    void testSetTargetLanguage() {
        deepLTranslateTool.setTargetLang("zh");
        assertEquals("zh", deepLTranslateTool.getTargetLang());
    }

    @Test
    void testDefaultSourceLanguage() {
        assertEquals("auto", deepLTranslateTool.getSourceLang());
    }

    @Test
    void testDefaultTargetLanguage() {
        assertEquals("ZH", deepLTranslateTool.getTargetLang());
    }

    @Test
    void testConstructorWithParameters() {
        DeepLTranslateTool tool = new DeepLTranslateTool("test-key", true, 60);
        
        assertEquals("test-key", tool.getApiKey());
        assertTrue(tool.isPro());
        assertEquals(60, tool.getTimeout());
    }

    @Test
    void testDefaultConstructor() {
        DeepLTranslateTool tool = new DeepLTranslateTool();
        
        assertNotNull(tool.getApiKey());
        assertNotNull(tool.getTimeout());
    }
}
