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
package com.alibaba.langengine.notion.tools.schema;

import com.alibaba.langengine.core.tool.StructuredParameter;
import com.alibaba.langengine.core.tool.StructuredSchema;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
public class NotionPageContentSchema extends StructuredSchema {
    
    public NotionPageContentSchema() {
        // 页面ID参数
        StructuredParameter pageIdParam = new StructuredParameter();
        pageIdParam.setName("page_id");
        pageIdParam.setDescription("要添加内容的页面ID");
        pageIdParam.setRequired(true);
        
        Map<String, Object> pageIdSchema = new HashMap<>();
        pageIdSchema.put("type", "string");
        pageIdParam.setSchema(pageIdSchema);
        
        // 内容块参数
        StructuredParameter childrenParam = new StructuredParameter();
        childrenParam.setName("children");
        childrenParam.setDescription("要添加的内容块数组，支持段落、标题、列表、代码块等");
        childrenParam.setRequired(true);
        
        Map<String, Object> childrenSchema = new HashMap<>();
        childrenSchema.put("type", "array");
        Map<String, Object> blockSchema = new HashMap<>();
        blockSchema.put("type", "object");
        Map<String, Object> blockProperties = new HashMap<>();
        Map<String, Object> typeProperty = new HashMap<>();
        typeProperty.put("type", "string");
        typeProperty.put("enum", new String[]{"paragraph", "heading_1", "heading_2", "heading_3", 
                                             "bulleted_list_item", "numbered_list_item", "to_do", 
                                             "code", "quote", "callout", "divider"});
        blockProperties.put("type", typeProperty);
        blockProperties.put("paragraph", new HashMap<String, Object>());
        blockProperties.put("heading_1", new HashMap<String, Object>());
        blockProperties.put("heading_2", new HashMap<String, Object>());
        blockProperties.put("heading_3", new HashMap<String, Object>());
        blockProperties.put("bulleted_list_item", new HashMap<String, Object>());
        blockProperties.put("numbered_list_item", new HashMap<String, Object>());
        blockProperties.put("to_do", new HashMap<String, Object>());
        blockProperties.put("code", new HashMap<String, Object>());
        blockProperties.put("quote", new HashMap<String, Object>());
        blockProperties.put("callout", new HashMap<String, Object>());
        blockSchema.put("properties", blockProperties);
        childrenSchema.put("items", blockSchema);
        childrenParam.setSchema(childrenSchema);
        
        getParameters().add(pageIdParam);
        getParameters().add(childrenParam);
    }
}
