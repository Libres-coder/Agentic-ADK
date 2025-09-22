package com.alibaba.langengine.volcenginetranslate;

import com.alibaba.langengine.core.tool.BaseTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 火山翻译工具工厂测试
 *
 * @author Makoto
 */
class VolcengineTranslateToolFactoryTest {

    private VolcengineTranslateToolFactory factory;

    @BeforeEach
    void setUp() {
        factory = new VolcengineTranslateToolFactory();
    }

    @Test
    void testGetAllTools() {
        List<BaseTool> tools = factory.getAllTools();
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("VolcengineTranslateTool", tools.get(0).getName());
    }

    @Test
    void testGetToolByName() {
        BaseTool tool = factory.getToolByName("volcengine_translate");
        
        assertNotNull(tool);
        assertEquals("VolcengineTranslateTool", tool.getName());
    }

    @Test
    void testGetToolByNameCaseInsensitive() {
        BaseTool tool = factory.getToolByName("VOLCENGINE_TRANSLATE");
        
        assertNotNull(tool);
        assertEquals("VolcengineTranslateTool", tool.getName());
    }

    @Test
    void testGetToolByNameWithSpaces() {
        BaseTool tool = factory.getToolByName("  volcengine_translate  ");
        
        assertNotNull(tool);
        assertEquals("VolcengineTranslateTool", tool.getName());
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
        assertTrue(supportedTypes.contains("volcengine_translate"));
    }

    @Test
    void testCreateTools() {
        List<String> toolTypes = Arrays.asList("volcengine_translate");
        List<BaseTool> tools = factory.createTools(toolTypes);
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("VolcengineTranslateTool", tools.get(0).getName());
    }

    @Test
    void testCreateToolsWithUnsupportedType() {
        List<String> toolTypes = Arrays.asList("volcengine_translate", "unsupported_tool");
        List<BaseTool> tools = factory.createTools(toolTypes);
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("VolcengineTranslateTool", tools.get(0).getName());
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
        assertEquals("VolcengineTranslateTool", tool.getName());
    }
}
