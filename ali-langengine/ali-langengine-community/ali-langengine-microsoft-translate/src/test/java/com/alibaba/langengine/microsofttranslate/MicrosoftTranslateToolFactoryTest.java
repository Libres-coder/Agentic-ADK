package com.alibaba.langengine.microsofttranslate;

import com.alibaba.langengine.core.tool.BaseTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Microsoft 翻译工具工厂测试
 *
 * @author Makoto
 */
class MicrosoftTranslateToolFactoryTest {

    private MicrosoftTranslateToolFactory factory;

    @BeforeEach
    void setUp() {
        factory = new MicrosoftTranslateToolFactory();
    }

    @Test
    void testGetAllTools() {
        List<BaseTool> tools = factory.getAllTools();
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("MicrosoftTranslateTool", tools.get(0).getName());
    }

    @Test
    void testGetToolByName() {
        BaseTool tool = factory.getToolByName("microsoft_translate");
        
        assertNotNull(tool);
        assertEquals("MicrosoftTranslateTool", tool.getName());
    }

    @Test
    void testGetToolByNameCaseInsensitive() {
        BaseTool tool = factory.getToolByName("MICROSOFT_TRANSLATE");
        
        assertNotNull(tool);
        assertEquals("MicrosoftTranslateTool", tool.getName());
    }

    @Test
    void testGetToolByNameWithSpaces() {
        BaseTool tool = factory.getToolByName("  microsoft_translate  ");
        
        assertNotNull(tool);
        assertEquals("MicrosoftTranslateTool", tool.getName());
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
        assertTrue(supportedTypes.contains("microsoft_translate"));
    }

    @Test
    void testCreateTools() {
        List<String> toolTypes = Arrays.asList("microsoft_translate");
        List<BaseTool> tools = factory.createTools(toolTypes);
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("MicrosoftTranslateTool", tools.get(0).getName());
    }

    @Test
    void testCreateToolsWithUnsupportedType() {
        List<String> toolTypes = Arrays.asList("microsoft_translate", "unsupported_tool");
        List<BaseTool> tools = factory.createTools(toolTypes);
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("MicrosoftTranslateTool", tools.get(0).getName());
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
        assertEquals("MicrosoftTranslateTool", tool.getName());
    }
}
