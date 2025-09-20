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

/**
 * Stock basic request parameters
 */
public class StockBasicRequest {
    
    /**
     * 交易所 SSE上交所,SZSE深交所,BSE北交所
     */
    private String exchange;
    
    /**
     * 上市状态 L上市,D退市,P暂停上市
     */
    private String listStatus = "L";
    
    /**
     * 返回字段 ts_code,symbol,name,area,industry,market,list_date
     */
    private String fields = "ts_code,symbol,name,area,industry,market,list_date";
    
    // Getters and Setters
    
    public String getExchange() {
        return exchange;
    }
    
    public void setExchange(String exchange) {
        this.exchange = exchange;
        }
    
    public String getListStatus() {
        return listStatus;
    }
    
    public void setListStatus(String listStatus) {
        this.listStatus = listStatus;
    }
    
    public String getFields() {
        return fields;
    }
    
    public void setFields(String fields) {
        this.fields = fields;
    }
}