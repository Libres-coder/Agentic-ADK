/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.notion.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.notion.client.NotionClient;
import com.alibaba.langengine.notion.exception.NotionException;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
@Data
public class NotionContentManageTool extends DefaultTool {
    
    private NotionClient notionClient;
    
    /**
     * 默认构造函数
     */
    public NotionContentManageTool() {
        setName("notion_content_manage");
        setFunctionName("manageNotionContent");
        setHumanName("Notion内容管理");
        setDescription("Notion内容管理工具，支持页面内容的创建、更新、删除和格式化操作。" +
               "可以添加不同类型的内容块（文本、标题、列表、代码块等），编辑现有内容，删除特定块。");
    }
    
    /**
     * 带客户端的构造函数
     */
    public NotionContentManageTool(NotionClient notionClient) {
        this();
        this.notionClient = notionClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, Map.class);
            
            String operation = (String) inputMap.get("operation");
            String pageId = (String) inputMap.get("pageId");
            String content = (String) inputMap.get("content");
            String contentType = (String) inputMap.get("contentType");
            Integer position = (Integer) inputMap.get("position");
            String blockId = (String) inputMap.get("blockId");
            JSONObject formatting = (JSONObject) inputMap.get("formatting");
            
            if (operation == null || pageId == null) {
                return new ToolExecuteResult("缺少必需的参数", true);
            }
            
            switch (operation.toLowerCase()) {
                case "add_content":
                    return addContent(pageId, content, contentType, position);
                case "update_content":
                    return updateContent(pageId, blockId, content);
                case "delete_content":
                    return deleteContent(pageId, blockId);
                case "format_content":
                    return formatContent(pageId, blockId, formatting);
                default:
                    return new ToolExecuteResult("不支持的操作: " + operation, true);
            }
        } catch (Exception e) {
            log.error("内容管理操作失败", e);
            return new ToolExecuteResult("参数解析失败: " + e.getMessage(), true);
        }
    }
    
    /**
     * 添加内容到页面
     */
    private ToolExecuteResult addContent(String pageId, String content, String contentType, Integer position) {
        try {
            JSONObject blockData = createBlockData(content, contentType);
            JSONArray children = new JSONArray();
            children.add(blockData);
            
            String result = notionClient.appendBlockChildren(pageId, children);
            JSONObject response = JSON.parseObject(result);
            
            if (response.containsKey("results") && response.getJSONArray("results").size() > 0) {
                JSONObject newBlock = response.getJSONArray("results").getJSONObject(0);
                String blockId = newBlock.getString("id");
                
                JSONObject resultData = new JSONObject();
                resultData.put("block_id", blockId);
                resultData.put("content_type", contentType);
                resultData.put("content", content);
                
                return new ToolExecuteResult(resultData.toJSONString(), false);
            } else {
                return new ToolExecuteResult("内容添加失败: 无法获取新建块信息", true);
            }
        } catch (NotionException e) {
            log.error("添加内容失败", e);
            return new ToolExecuteResult("Notion API调用失败: " + e.getMessage(), true);
        }
    }
    
    /**
     * 更新页面内容
     */
    private ToolExecuteResult updateContent(String pageId, String blockId, String content) {
        try {
            JSONObject updateData = new JSONObject();
            
            // 获取当前块信息以确定类型
            String blockInfo = notionClient.retrieveBlock(blockId);
            JSONObject block = JSON.parseObject(blockInfo);
            String blockType = block.getString("type");
            
            // 根据块类型更新内容
            if ("paragraph".equals(blockType)) {
                JSONObject paragraph = new JSONObject();
                JSONArray richText = new JSONArray();
                JSONObject textObj = new JSONObject();
                JSONObject text = new JSONObject();
                text.put("content", content);
                textObj.put("text", text);
                richText.add(textObj);
                paragraph.put("rich_text", richText);
                updateData.put("paragraph", paragraph);
            } else if ("heading_1".equals(blockType) || "heading_2".equals(blockType) || "heading_3".equals(blockType)) {
                JSONObject heading = new JSONObject();
                JSONArray richText = new JSONArray();
                JSONObject textObj = new JSONObject();
                JSONObject text = new JSONObject();
                text.put("content", content);
                textObj.put("text", text);
                richText.add(textObj);
                heading.put("rich_text", richText);
                updateData.put(blockType, heading);
            }
            
            String result = notionClient.updateBlock(blockId, updateData);
            
            JSONObject resultData = new JSONObject();
            resultData.put("block_id", blockId);
            resultData.put("updated_content", content);
            
            return new ToolExecuteResult(resultData.toJSONString(), false);
        } catch (NotionException e) {
            log.error("更新内容失败", e);
            return new ToolExecuteResult("Notion API调用失败: " + e.getMessage(), true);
        }
    }
    
    /**
     * 删除页面内容
     */
    private ToolExecuteResult deleteContent(String pageId, String blockId) {
        try {
            String result = notionClient.deleteBlock(blockId);
            
            JSONObject resultData = new JSONObject();
            resultData.put("block_id", blockId);
            resultData.put("status", "deleted");
            
            return new ToolExecuteResult(resultData.toJSONString(), false);
        } catch (NotionException e) {
            log.error("删除内容失败", e);
            return new ToolExecuteResult("Notion API调用失败: " + e.getMessage(), true);
        }
    }
    
    /**
     * 格式化页面内容
     */
    private ToolExecuteResult formatContent(String pageId, String blockId, JSONObject formatting) {
        try {
            // 获取当前块信息
            String blockInfo = notionClient.retrieveBlock(blockId);
            JSONObject block = JSON.parseObject(blockInfo);
            String blockType = block.getString("type");
            
            JSONObject updateData = new JSONObject();
            JSONObject blockData = block.getJSONObject(blockType);
            
            if (blockData.containsKey("rich_text")) {
                JSONArray richText = blockData.getJSONArray("rich_text");
                for (int i = 0; i < richText.size(); i++) {
                    JSONObject textObj = richText.getJSONObject(i);
                    JSONObject annotations = textObj.getJSONObject("annotations");
                    if (annotations == null) {
                        annotations = new JSONObject();
                        textObj.put("annotations", annotations);
                    }
                    
                    // 应用格式化
                    if (formatting.containsKey("bold")) {
                        annotations.put("bold", formatting.getBoolean("bold"));
                    }
                    if (formatting.containsKey("italic")) {
                        annotations.put("italic", formatting.getBoolean("italic"));
                    }
                    if (formatting.containsKey("strikethrough")) {
                        annotations.put("strikethrough", formatting.getBoolean("strikethrough"));
                    }
                    if (formatting.containsKey("underline")) {
                        annotations.put("underline", formatting.getBoolean("underline"));
                    }
                    if (formatting.containsKey("code")) {
                        annotations.put("code", formatting.getBoolean("code"));
                    }
                    if (formatting.containsKey("color")) {
                        annotations.put("color", formatting.getString("color"));
                    }
                }
                
                blockData.put("rich_text", richText);
                updateData.put(blockType, blockData);
            }
            
            String result = notionClient.updateBlock(blockId, updateData);
            
            JSONObject resultData = new JSONObject();
            resultData.put("block_id", blockId);
            resultData.put("formatting", formatting);
            
            return new ToolExecuteResult(resultData.toJSONString(), false);
        } catch (NotionException e) {
            log.error("格式化内容失败", e);
            return new ToolExecuteResult("Notion API调用失败: " + e.getMessage(), true);
        }
    }
    
    /**
     * 创建块数据
     */
    private JSONObject createBlockData(String content, String contentType) {
        JSONObject blockData = new JSONObject();
        blockData.put("object", "block");
        blockData.put("type", contentType);
        
        JSONArray richText = new JSONArray();
        JSONObject textObj = new JSONObject();
        JSONObject text = new JSONObject();
        text.put("content", content);
        textObj.put("text", text);
        richText.add(textObj);
        
        switch (contentType.toLowerCase()) {
            case "paragraph":
                JSONObject paragraph = new JSONObject();
                paragraph.put("rich_text", richText);
                blockData.put("paragraph", paragraph);
                break;
            case "heading_1":
                JSONObject heading1 = new JSONObject();
                heading1.put("rich_text", richText);
                blockData.put("heading_1", heading1);
                break;
            case "heading_2":
                JSONObject heading2 = new JSONObject();
                heading2.put("rich_text", richText);
                blockData.put("heading_2", heading2);
                break;
            case "heading_3":
                JSONObject heading3 = new JSONObject();
                heading3.put("rich_text", richText);
                blockData.put("heading_3", heading3);
                break;
            case "bulleted_list_item":
                JSONObject bulletedList = new JSONObject();
                bulletedList.put("rich_text", richText);
                blockData.put("bulleted_list_item", bulletedList);
                break;
            case "numbered_list_item":
                JSONObject numberedList = new JSONObject();
                numberedList.put("rich_text", richText);
                blockData.put("numbered_list_item", numberedList);
                break;
            case "code":
                JSONObject code = new JSONObject();
                code.put("rich_text", richText);
                code.put("language", "plain text");
                blockData.put("code", code);
                break;
            default:
                // 默认为段落
                JSONObject defaultParagraph = new JSONObject();
                defaultParagraph.put("rich_text", richText);
                blockData.put("paragraph", defaultParagraph);
                blockData.put("type", "paragraph");
                break;
        }
        
        return blockData;
    }
    
    public NotionClient getClient() {
        return notionClient;
    }
}
