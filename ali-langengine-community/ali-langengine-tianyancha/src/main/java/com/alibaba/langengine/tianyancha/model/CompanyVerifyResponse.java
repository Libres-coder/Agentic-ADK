package com.alibaba.langengine.tianyancha.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Company Verify Response
 * 
 * This class represents the response from the Tianyancha API for company verification.
 * */ 
@Data
public class CompanyVerifyResponse {
    
    @JSONField(name = "error_code")
    private Integer errorCode;
    
    private String reason;
    
    private CompanyVerifyResult result;
    
    @Data
    public static class CompanyVerifyResult {
        private Integer result;
        private String remark;
    }
}