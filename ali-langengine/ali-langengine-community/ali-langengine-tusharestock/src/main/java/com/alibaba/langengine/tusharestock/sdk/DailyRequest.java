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
 * Daily request parameters
 */
public class DailyRequest {
    
    /**
     * 股票代码（支持多个股票同时提取，逗号分隔）
     */
    private String tsCode;
    
    /**
     * 交易日期 （格式：YYYYMMDD，下同）
     */
    private String tradeDate;
    
    /**
     * 开始日期
     */
    private String startDate;
    
    /**
     * 结束日期
     */
    private String endDate;
    
    /**
     * 返回字段 ts_code,trade_date,open,high,low,close,pre_close,change,pct_chg,vol,amount
     */
    private String fields = "ts_code,trade_date,open,high,low,close,pre_close,change,pct_chg,vol,amount";
    
    // Getters and Setters
    
    public String getTsCode() {
        return tsCode;
    }
    
    public void setTsCode(String tsCode) {
        this.tsCode = tsCode;
    }
    
    public String getTradeDate() {
        return tradeDate;
    }
    
    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public String getFields() {
        return fields;
    }
    
    public void setFields(String fields) {
        this.fields = fields;
    }
}