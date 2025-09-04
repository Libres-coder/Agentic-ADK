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
public class NotionPageCreateSchema extends StructuredSchema {
    
    public NotionPageCreateSchema() {
        // 数据库ID参数
        StructuredParameter databaseIdParam = new StructuredParameter();
        databaseIdParam.setName("database_id");
        databaseIdParam.setDescription("要在其中创建页面的数据库ID");
        databaseIdParam.setRequired(true);
        
        Map<String, Object> databaseIdSchema = new HashMap<>();
        databaseIdSchema.put("type", "string");
        databaseIdParam.setSchema(databaseIdSchema);
        
        // 页面属性参数
        StructuredParameter propertiesParam = new StructuredParameter();
        propertiesParam.setName("properties");
        propertiesParam.setDescription("页面属性，根据数据库的属性结构设置");
        propertiesParam.setRequired(true);
        
        Map<String, Object> propertiesSchema = new HashMap<>();
        propertiesSchema.put("type", "object");
        propertiesParam.setSchema(propertiesSchema);
        
        // 页面内容参数
        StructuredParameter childrenParam = new StructuredParameter();
        childrenParam.setName("children");
        childrenParam.setDescription("页面内容块数组，包含文本、标题、列表等内容");
        childrenParam.setRequired(false);
        
        Map<String, Object> childrenSchema = new HashMap<>();
        childrenSchema.put("type", "array");
        Map<String, Object> blockSchema = new HashMap<>();
        blockSchema.put("type", "object");
        childrenSchema.put("items", blockSchema);
        childrenParam.setSchema(childrenSchema);
        
        getParameters().add(databaseIdParam);
        getParameters().add(propertiesParam);
        getParameters().add(childrenParam);
    }
}
