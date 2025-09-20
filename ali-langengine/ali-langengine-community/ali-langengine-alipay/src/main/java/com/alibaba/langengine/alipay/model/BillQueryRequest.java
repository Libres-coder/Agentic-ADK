package com.alibaba.langengine.alipay.model;

import lombok.Data;

@Data
public class BillQueryRequest {
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
        private String billType;
        private String billDate;
        
        public BizContent(String billType, String billDate) {
            this.billType = billType;
            this.billDate = billDate;
        }
    }
}
