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
package com.alibaba.langengine.tusharestock.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Tushare response from Tushare API
 */
public class TushareResponse {
    
    /**
     * The response code
     */
    @JsonProperty("code")
    private Integer code;
    
    /**
     * The message
     */
    @JsonProperty("msg")
    private String msg;
    
    /**
     * The data
     */
    @JsonProperty("data")
    private TushareData data;
    
    // Getters and Setters
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public TushareData getData() {
        return data;
    }
    
    public void setData(TushareData data) {
        this.data = data;
    }
    
    /**
     * Tushare data structure
     */
    public static class TushareData {
        /**
         * The fields
         */
        @JsonProperty("fields")
        private List<String> fields;
        
        /**
         * The items
         */
        @JsonProperty("items")
        private List<List<Object>> items;
        
        // Getters and Setters
        
        public List<String> getFields() {
            return fields;
        }
        
        public void setFields(List<String> fields) {
            this.fields = fields;
        }
        
        public List<List<Object>> getItems() {
            return items;
        }
        
        public void setItems(List<List<Object>> items) {
            this.items = items;
        }
    }
}