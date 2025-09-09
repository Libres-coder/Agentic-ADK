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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;


@Data
public class NotionPage {
    
    @JSONField(name = "object")
    private String object;
    
    @JSONField(name = "id")
    private String id;
    
    @JSONField(name = "created_time")
    private Date createdTime;
    
    @JSONField(name = "created_by")
    private JSONObject createdBy;
    
    @JSONField(name = "last_edited_time")
    private Date lastEditedTime;
    
    @JSONField(name = "last_edited_by")
    private JSONObject lastEditedBy;
    
    @JSONField(name = "cover")
    private JSONObject cover;
    
    @JSONField(name = "icon")
    private JSONObject icon;
    
    @JSONField(name = "parent")
    private JSONObject parent;
    
    @JSONField(name = "archived")
    private Boolean archived;
    
    @JSONField(name = "properties")
    private JSONObject properties;
    
    @JSONField(name = "url")
    private String url;
    
    @JSONField(name = "public_url")
    private String publicUrl;
}
