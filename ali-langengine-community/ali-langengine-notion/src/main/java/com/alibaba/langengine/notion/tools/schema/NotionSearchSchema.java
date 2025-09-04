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
public class NotionSearchSchema extends StructuredSchema {
    
    public NotionSearchSchema() {
        // 查询关键词参数
        StructuredParameter queryParam = new StructuredParameter();
        queryParam.setName("query");
        queryParam.setDescription("搜索查询关键词，可以搜索页面标题和数据库名称");
        queryParam.setRequired(false);
        Map<String, Object> querySchema = new HashMap<>();
        querySchema.put("type", "string");
        queryParam.setSchema(querySchema);
        getParameters().add(queryParam);
        
        // 过滤器参数
        StructuredParameter filterParam = new StructuredParameter();
        filterParam.setName("filter");
        filterParam.setDescription("过滤条件，用于筛选特定类型的对象（如只搜索page或database）");
        filterParam.setRequired(false);
        Map<String, Object> filterSchema = new HashMap<>();
        filterSchema.put("type", "object");
        Map<String, Object> filterProps = new HashMap<>();
        Map<String, Object> valueSchema = new HashMap<>();
        valueSchema.put("type", "string");
        valueSchema.put("enum", new String[]{"page", "database"});
        filterProps.put("value", valueSchema);
        Map<String, Object> propertySchema = new HashMap<>();
        propertySchema.put("type", "string");
        propertySchema.put("enum", new String[]{"object"});
        filterProps.put("property", propertySchema);
        filterSchema.put("properties", filterProps);
        filterParam.setSchema(filterSchema);
        getParameters().add(filterParam);
        
        // 排序参数
        StructuredParameter sortParam = new StructuredParameter();
        sortParam.setName("sort");
        sortParam.setDescription("排序条件，可按最后编辑时间排序");
        sortParam.setRequired(false);
        Map<String, Object> sortSchema = new HashMap<>();
        sortSchema.put("type", "object");
        Map<String, Object> sortProps = new HashMap<>();
        Map<String, Object> directionSchema = new HashMap<>();
        directionSchema.put("type", "string");
        directionSchema.put("enum", new String[]{"ascending", "descending"});
        sortProps.put("direction", directionSchema);
        Map<String, Object> timestampSchema = new HashMap<>();
        timestampSchema.put("type", "string");
        timestampSchema.put("enum", new String[]{"last_edited_time"});
        sortProps.put("timestamp", timestampSchema);
        sortSchema.put("properties", sortProps);
        sortParam.setSchema(sortSchema);
        getParameters().add(sortParam);
    }
}
