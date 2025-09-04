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
package com.alibaba.langengine.notion.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;


@Data
public class NotionBlocksResult {
    
    @JSONField(name = "object")
    private String object;
    
    @JSONField(name = "results")
    private JSONArray results;
    
    @JSONField(name = "next_cursor")
    private String nextCursor;
    
    @JSONField(name = "has_more")
    private Boolean hasMore;
    
    @JSONField(name = "type")
    private String type;
    
    @JSONField(name = "block")
    private Object block;
}
