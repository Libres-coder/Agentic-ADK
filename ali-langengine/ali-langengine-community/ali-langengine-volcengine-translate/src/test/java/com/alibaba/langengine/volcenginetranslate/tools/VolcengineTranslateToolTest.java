package com.alibaba.langengine.volcenginetranslate.tools;

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
 * 火山翻译工具测试
 *
 * @author Makoto
 */
@ExtendWith(MockitoExtension.class)
class VolcengineTranslateToolTest {

    private VolcengineTranslateTool volcengineTranslateTool;

    @BeforeEach
    void setUp() {
        volcengineTranslateTool = new VolcengineTranslateTool(
            "test-access-key", 
            "test-secret-key", 
            "cn-north-1", 
            "translate", 
            "2020-06-01", 
            "TranslateText", 
            30
        );
    }

    @Test
    void testToolName() {
        assertEquals("VolcengineTranslateTool", volcengineTranslateTool.getName());
    }

    @Test
    void testToolDescription() {
        assertTrue(volcengineTranslateTool.getDescription().contains("火山"));
        assertTrue(volcengineTranslateTool.getDescription().contains("翻译"));
        assertTrue(volcengineTranslateTool.getDescription().contains("字节跳动"));
    }

    @Test
    void testRunWithEmptyInput() {
        ToolExecuteResult result = volcengineTranslateTool.run("");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("翻译文本不能为空"));
    }

    @Test
    void testRunWithNullInput() {
        ToolExecuteResult result = volcengineTranslateTool.run(null);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("翻译文本不能为空"));
    }

    @Test
    void testRunWithBlankInput() {
        ToolExecuteResult result = volcengineTranslateTool.run("   ");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("翻译文本不能为空"));
    }

    @Test
    void testSetSourceLanguage() {
        volcengineTranslateTool.setSourceLang("en");
        assertEquals("en", volcengineTranslateTool.getSourceLang());
    }

    @Test
    void testSetTargetLanguage() {
        volcengineTranslateTool.setTargetLang("zh");
        assertEquals("zh", volcengineTranslateTool.getTargetLang());
    }

    @Test
    void testDefaultSourceLanguage() {
        assertEquals("auto", volcengineTranslateTool.getSourceLang());
    }

    @Test
    void testDefaultTargetLanguage() {
        assertEquals("zh", volcengineTranslateTool.getTargetLang());
    }

    @Test
    void testConstructorWithParameters() {
        VolcengineTranslateTool tool = new VolcengineTranslateTool(
            "test-key", 
            "test-secret", 
            "cn-south-1", 
            "translate", 
            "2020-06-01", 
            "TranslateText", 
            60
        );
        
        assertEquals("test-key", tool.getAccessKey());
        assertEquals("test-secret", tool.getSecretKey());
        assertEquals("cn-south-1", tool.getRegion());
        assertEquals("translate", tool.getService());
        assertEquals("2020-06-01", tool.getVersion());
        assertEquals("TranslateText", tool.getAction());
        assertEquals(60, tool.getTimeout());
    }

    @Test
    void testDefaultConstructor() {
        VolcengineTranslateTool tool = new VolcengineTranslateTool();
        
        assertNotNull(tool.getAccessKey());
        assertNotNull(tool.getSecretKey());
        assertNotNull(tool.getRegion());
        assertNotNull(tool.getService());
        assertNotNull(tool.getVersion());
        assertNotNull(tool.getAction());
        assertNotNull(tool.getTimeout());
    }

    @Test
    void testServiceInitialization() {
        assertNotNull(volcengineTranslateTool.getVolcengineTranslateService());
        assertEquals("test-access-key", volcengineTranslateTool.getAccessKey());
        assertEquals("test-secret-key", volcengineTranslateTool.getSecretKey());
        assertEquals("cn-north-1", volcengineTranslateTool.getRegion());
        assertEquals("translate", volcengineTranslateTool.getService());
        assertEquals("2020-06-01", volcengineTranslateTool.getVersion());
        assertEquals("TranslateText", volcengineTranslateTool.getAction());
    }
}
