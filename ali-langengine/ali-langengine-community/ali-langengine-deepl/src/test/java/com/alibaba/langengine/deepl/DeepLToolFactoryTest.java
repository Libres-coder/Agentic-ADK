package com.alibaba.langengine.deepl;

import com.alibaba.langengine.core.tool.BaseTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeepL 工具工厂测试
 *
 * @author Makoto
 */
class DeepLToolFactoryTest {

    private DeepLToolFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DeepLToolFactory();
    }

    @Test
    void testGetAllTools() {
        List<BaseTool> tools = factory.getAllTools();
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("DeepLTranslateTool", tools.get(0).getName());
    }

    @Test
    void testGetToolByName() {
        BaseTool tool = factory.getToolByName("deepl_translate");
        
        assertNotNull(tool);
        assertEquals("DeepLTranslateTool", tool.getName());
    }

    @Test
    void testGetToolByNameCaseInsensitive() {
        BaseTool tool = factory.getToolByName("DEEPL_TRANSLATE");
        
        assertNotNull(tool);
        assertEquals("DeepLTranslateTool", tool.getName());
    }

    @Test
    void testGetToolByNameWithSpaces() {
        BaseTool tool = factory.getToolByName("  deepl_translate  ");
        
        assertNotNull(tool);
        assertEquals("DeepLTranslateTool", tool.getName());
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
        assertTrue(supportedTypes.contains("deepl_translate"));
    }

    @Test
    void testCreateTools() {
        List<String> toolTypes = Arrays.asList("deepl_translate");
        List<BaseTool> tools = factory.createTools(toolTypes);
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("DeepLTranslateTool", tools.get(0).getName());
    }

    @Test
    void testCreateToolsWithUnsupportedType() {
        List<String> toolTypes = Arrays.asList("deepl_translate", "unsupported_tool");
        List<BaseTool> tools = factory.createTools(toolTypes);
        
        assertNotNull(tools);
        assertEquals(1, tools.size());
        assertEquals("DeepLTranslateTool", tools.get(0).getName());
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
        assertEquals("DeepLTranslateTool", tool.getName());
    }
}
