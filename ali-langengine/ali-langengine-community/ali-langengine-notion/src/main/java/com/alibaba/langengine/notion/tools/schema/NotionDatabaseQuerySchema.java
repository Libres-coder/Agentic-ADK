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
public class NotionDatabaseQuerySchema extends StructuredSchema {
    
    public NotionDatabaseQuerySchema() {
        // 数据库ID参数
        StructuredParameter databaseIdParam = new StructuredParameter();
        databaseIdParam.setName("database_id");
        databaseIdParam.setDescription("要查询的数据库ID");
        databaseIdParam.setRequired(true);
        
        Map<String, Object> databaseIdSchema = new HashMap<>();
        databaseIdSchema.put("type", "string");
        databaseIdParam.setSchema(databaseIdSchema);
        
        // 过滤器参数
        StructuredParameter filterParam = new StructuredParameter();
        filterParam.setName("filter");
        filterParam.setDescription("查询过滤条件，用于筛选数据库中的页面");
        filterParam.setRequired(false);
        
        Map<String, Object> filterSchema = new HashMap<>();
        filterSchema.put("type", "object");
        filterParam.setSchema(filterSchema);
        
        // 排序参数
        StructuredParameter sortsParam = new StructuredParameter();
        sortsParam.setName("sorts");
        sortsParam.setDescription("排序条件数组，可以按属性排序");
        sortsParam.setRequired(false);
        
        Map<String, Object> sortsSchema = new HashMap<>();
        sortsSchema.put("type", "array");
        Map<String, Object> sortItemSchema = new HashMap<>();
        sortItemSchema.put("type", "object");
        Map<String, Object> sortProperties = new HashMap<>();
        Map<String, Object> propertyProperty = new HashMap<>();
        propertyProperty.put("type", "string");
        Map<String, Object> directionProperty = new HashMap<>();
        directionProperty.put("type", "string");
        directionProperty.put("enum", new String[]{"ascending", "descending"});
        sortProperties.put("property", propertyProperty);
        sortProperties.put("direction", directionProperty);
        sortItemSchema.put("properties", sortProperties);
        sortsSchema.put("items", sortItemSchema);
        sortsParam.setSchema(sortsSchema);
        
        // 页面大小参数
        StructuredParameter pageSizeParam = new StructuredParameter();
        pageSizeParam.setName("page_size");
        pageSizeParam.setDescription("每页返回的记录数，最大100");
        pageSizeParam.setRequired(false);
        
        Map<String, Object> pageSizeSchema = new HashMap<>();
        pageSizeSchema.put("type", "integer");
        pageSizeSchema.put("minimum", 1);
        pageSizeSchema.put("maximum", 100);
        pageSizeParam.setSchema(pageSizeSchema);
        
        // 开始游标参数
        StructuredParameter startCursorParam = new StructuredParameter();
        startCursorParam.setName("start_cursor");
        startCursorParam.setDescription("分页查询的开始游标");
        startCursorParam.setRequired(false);
        
        Map<String, Object> startCursorSchema = new HashMap<>();
        startCursorSchema.put("type", "string");
        startCursorParam.setSchema(startCursorSchema);
        
        getParameters().add(databaseIdParam);
        getParameters().add(filterParam);
        getParameters().add(sortsParam);
        getParameters().add(pageSizeParam);
        getParameters().add(startCursorParam);
    }
}
