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


public class NotionPageResult {
    
    private String id;
    private String status;
    private String message;
    private JSONObject data;
    
    public NotionPageResult() {
    }
    
    public NotionPageResult(String id, String status, String message) {
        this.id = id;
        this.status = status;
        this.message = message;
    }
    
    public NotionPageResult(String id, String status, String message, JSONObject data) {
        this.id = id;
        this.status = status;
        this.message = message;
        this.data = data;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public JSONObject getData() {
        return data;
    }
    
    public void setData(JSONObject data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "NotionPageResult{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
