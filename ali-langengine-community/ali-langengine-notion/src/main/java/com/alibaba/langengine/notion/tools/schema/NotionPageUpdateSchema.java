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
public class NotionPageUpdateSchema extends StructuredSchema {
    
    public NotionPageUpdateSchema() {
        // 页面ID参数
        StructuredParameter pageIdParam = new StructuredParameter();
        pageIdParam.setName("page_id");
        pageIdParam.setDescription("要更新的页面ID");
        pageIdParam.setRequired(true);
        
        Map<String, Object> pageIdSchema = new HashMap<>();
        pageIdSchema.put("type", "string");
        pageIdParam.setSchema(pageIdSchema);
        
        // 页面属性参数
        StructuredParameter propertiesParam = new StructuredParameter();
        propertiesParam.setName("properties");
        propertiesParam.setDescription("要更新的页面属性");
        propertiesParam.setRequired(true);
        
        Map<String, Object> propertiesSchema = new HashMap<>();
        propertiesSchema.put("type", "object");
        propertiesParam.setSchema(propertiesSchema);
        
        getParameters().add(pageIdParam);
        getParameters().add(propertiesParam);
    }
}
