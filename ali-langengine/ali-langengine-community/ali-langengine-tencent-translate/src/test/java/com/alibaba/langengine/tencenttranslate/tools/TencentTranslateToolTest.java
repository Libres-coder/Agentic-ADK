package com.alibaba.langengine.tencenttranslate.tools;

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
 * 腾讯翻译工具测试
 *
 * @author Makoto
 */
@ExtendWith(MockitoExtension.class)
class TencentTranslateToolTest {

    private TencentTranslateTool tencentTranslateTool;

    @BeforeEach
    void setUp() {
        tencentTranslateTool = new TencentTranslateTool(
            "test-secret-id", 
            "test-secret-key", 
            "ap-beijing", 
            "tmt", 
            "2018-03-21", 
            "TextTranslate", 
            30
        );
    }

    @Test
    void testToolName() {
        assertEquals("TencentTranslateTool", tencentTranslateTool.getName());
    }

    @Test
    void testToolDescription() {
        assertTrue(tencentTranslateTool.getDescription().contains("腾讯"));
        assertTrue(tencentTranslateTool.getDescription().contains("翻译"));
        assertTrue(tencentTranslateTool.getDescription().contains("图片"));
    }

    @Test
    void testRunWithEmptyInput() {
        ToolExecuteResult result = tencentTranslateTool.run("");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("翻译文本不能为空"));
    }

    @Test
    void testRunWithNullInput() {
        ToolExecuteResult result = tencentTranslateTool.run(null);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("翻译文本不能为空"));
    }

    @Test
    void testRunWithBlankInput() {
        ToolExecuteResult result = tencentTranslateTool.run("   ");
        
        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("翻译文本不能为空"));
    }

    @Test
    void testSetSourceLanguage() {
        tencentTranslateTool.setSourceLang("en");
        assertEquals("en", tencentTranslateTool.getSourceLang());
    }

    @Test
    void testSetTargetLanguage() {
        tencentTranslateTool.setTargetLang("zh");
        assertEquals("zh", tencentTranslateTool.getTargetLang());
    }

    @Test
    void testDefaultSourceLanguage() {
        assertEquals("auto", tencentTranslateTool.getSourceLang());
    }

    @Test
    void testDefaultTargetLanguage() {
        assertEquals("zh", tencentTranslateTool.getTargetLang());
    }

    @Test
    void testConstructorWithParameters() {
        TencentTranslateTool tool = new TencentTranslateTool(
            "test-id", 
            "test-key", 
            "ap-shanghai", 
            "tmt", 
            "2018-03-21", 
            "TextTranslate", 
            60
        );
        
        assertEquals("test-id", tool.getSecretId());
        assertEquals("test-key", tool.getSecretKey());
        assertEquals("ap-shanghai", tool.getRegion());
        assertEquals("tmt", tool.getService());
        assertEquals("2018-03-21", tool.getVersion());
        assertEquals("TextTranslate", tool.getAction());
        assertEquals(60, tool.getTimeout());
    }

    @Test
    void testDefaultConstructor() {
        TencentTranslateTool tool = new TencentTranslateTool();
        
        assertNotNull(tool.getSecretId());
        assertNotNull(tool.getSecretKey());
        assertNotNull(tool.getRegion());
        assertNotNull(tool.getService());
        assertNotNull(tool.getVersion());
        assertNotNull(tool.getAction());
        assertNotNull(tool.getTimeout());
    }

    @Test
    void testServiceInitialization() {
        assertNotNull(tencentTranslateTool.getTencentTranslateService());
        assertEquals("test-secret-id", tencentTranslateTool.getSecretId());
        assertEquals("test-secret-key", tencentTranslateTool.getSecretKey());
        assertEquals("ap-beijing", tencentTranslateTool.getRegion());
        assertEquals("tmt", tencentTranslateTool.getService());
        assertEquals("2018-03-21", tencentTranslateTool.getVersion());
        assertEquals("TextTranslate", tencentTranslateTool.getAction());
    }
}
