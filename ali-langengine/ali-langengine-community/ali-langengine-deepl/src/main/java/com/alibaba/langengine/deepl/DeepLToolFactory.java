package com.alibaba.langengine.deepl;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.deepl.tools.DeepLTranslateTool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DeepL 工具工厂
 *
 * @author Makoto
 */
@Slf4j
public class DeepLToolFactory {
    
    /**
     * 创建所有 DeepL 工具
     * 
     * @return 所有 DeepL 工具的列表
     */
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        
        tools.add(getTranslateTool());
        
        log.info("Created {} DeepL tools", tools.size());
        return tools;
    }
    
    /**
     * 根据名称获取工具
     * 
     * @param name 工具名称
     * @return 对应的工具实例
     * @throws IllegalArgumentException 不支持的工具名称
     */
    public BaseTool getToolByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool name cannot be null or empty");
        }
        
        switch (name.toLowerCase().trim()) {
            case "deepl_translate":
                return getTranslateTool();
            default:
                throw new IllegalArgumentException("Unsupported tool name: " + name);
        }
    }
    
    /**
     * 获取支持的工具类型
     * 
     * @return 支持的工具类型列表
     */
    public List<String> getSupportedToolTypes() {
        return Arrays.asList("deepl_translate");
    }
    
    /**
     * 创建指定类型的工具
     * 
     * @param toolTypes 工具类型列表
     * @return 对应的工具实例列表
     */
    public List<BaseTool> createTools(List<String> toolTypes) {
        List<BaseTool> tools = new ArrayList<>();
        
        for (String toolType : toolTypes) {
            try {
                BaseTool tool = getToolByName(toolType);
                tools.add(tool);
            } catch (IllegalArgumentException e) {
                log.warn("Skipping unsupported tool type: {}", toolType);
            }
        }
        
        return tools;
    }
    
    /**
     * 获取翻译工具
     * 
     * @return 翻译工具实例
     */
    public BaseTool getTranslateTool() {
        return new DeepLTranslateTool();
    }
}
