package com.alibaba.langengine.alipay.model;

import lombok.Data;

@Data
public class TransferRequest {
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
        private String outBizNo;
        private String payeeType;
        private String payeeAccount;
        private String amount;
        private String remark;
        private String payeeRealName;
        
        public BizContent(String payeeAccount, String amount, String remark) {
            this.outBizNo = "out_biz_no_" + System.currentTimeMillis();
            this.payeeType = "ALIPAY_LOGONID";
            this.payeeAccount = payeeAccount;
            this.amount = amount;
            this.remark = remark;
        }
    }
}
