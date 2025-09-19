package com.alibaba.langengine.alipay.model;

import lombok.Data;

@Data
public class TradeQueryRequest {
    private String appId;
    private String method;
    private String charset;
    private String signType;
    private String timestamp;
    private String version;
    private String bizContent;
    private String sign;
    
    @Data
    public static class BizContent {
        private String outTradeNo;
        private String tradeNo;
        
        public BizContent(String outTradeNo) {
            this.outTradeNo = outTradeNo;
        }
    }
}
