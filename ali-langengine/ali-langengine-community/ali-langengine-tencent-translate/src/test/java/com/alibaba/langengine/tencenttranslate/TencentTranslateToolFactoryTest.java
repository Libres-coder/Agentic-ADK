package com.alibaba.langengine.tencenttranslate;

import com.alibaba.langengine.core.tool.BaseTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 腾讯翻译工具工厂测试
 *
 * @author Makoto
 */
class TencentTranslateToolFactoryTest {

    private TencentTranslateToolFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TencentTranslateToolFactory();
    }

    @Test
    void testGetAllTools() {
        List<BaseTool> tools = factory.getAllTools();
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("TencentTranslateTool", tools.get(0).getName());
    }

    @Test
    void testGetToolByName() {
        BaseTool tool = factory.getToolByName("tencent_translate");
        
        assertNotNull(tool);
        assertEquals("TencentTranslateTool", tool.getName());
    }

    @Test
    void testGetToolByNameCaseInsensitive() {
        BaseTool tool = factory.getToolByName("TENCENT_TRANSLATE");
        
        assertNotNull(tool);
        assertEquals("TencentTranslateTool", tool.getName());
    }

    @Test
    void testGetToolByNameWithSpaces() {
        BaseTool tool = factory.getToolByName("  tencent_translate  ");
        
        assertNotNull(tool);
        assertEquals("TencentTranslateTool", tool.getName());
    }

    @Test
    void testGetToolByNameWithUnsupportedName() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getToolByName("unsupported_tool");
        });
    }

    @Test
    void testGetToolByNameWithNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getToolByName(null);
        });
    }

    @Test
    void testGetToolByNameWithEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getToolByName("");
        });
    }

    @Test
    void testGetToolByNameWithBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getToolByName("   ");
        });
    }

    @Test
    void testGetSupportedToolTypes() {
        List<String> supportedTypes = factory.getSupportedToolTypes();
        
        assertNotNull(supportedTypes);
        assertEquals(1, supportedTypes.size());
        assertTrue(supportedTypes.contains("tencent_translate"));
    }

    @Test
    void testCreateTools() {
        List<String> toolTypes = Arrays.asList("tencent_translate");
        List<BaseTool> tools = factory.createTools(toolTypes);
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("TencentTranslateTool", tools.get(0).getName());
    }

    @Test
    void testCreateToolsWithUnsupportedType() {
        List<String> toolTypes = Arrays.asList("tencent_translate", "unsupported_tool");
        List<BaseTool> tools = factory.createTools(toolTypes);
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("TencentTranslateTool", tools.get(0).getName());
    }

    @Test
    void testCreateToolsWithEmptyList() {
        List<String> toolTypes = Arrays.asList();
        List<BaseTool> tools = factory.createTools(toolTypes);
        
        assertNotNull(tools);
        assertEquals(0, tools.size());
    }

    @Test
    void testGetTranslateTool() {
        BaseTool tool = factory.getTranslateTool();
        
        assertNotNull(tool);
        assertEquals("TencentTranslateTool", tool.getName());
    }
}
