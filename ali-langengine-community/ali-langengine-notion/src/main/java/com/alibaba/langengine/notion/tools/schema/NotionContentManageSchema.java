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

import com.alibaba.fastjson.JSONObject;
import lombok.Data;


@Data
public class NotionContentManageSchema {
    
    /**
     * 操作类型：add_content、update_content、delete_content、format_content
     */
    private String operation;
    
    /**
     * 页面ID
     */
    private String pageId;
    
    /**
     * 内容文本
     */
    private String content;
    
    /**
     * 内容类型：paragraph、heading_1、heading_2、heading_3、bulleted_list_item、numbered_list_item、code
     */
    private String contentType;
    
    /**
     * 内容位置（可选）
     */
    private Integer position;
    
    /**
     * 块ID（用于更新、删除、格式化操作）
     */
    private String blockId;
    
    /**
     * 格式化选项（用于format_content操作）
     */
    private JSONObject formatting;
}
