package com.alibaba.langengine.tianyancha.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;
/**
 * Company Holder Response
 * 
 * This class represents the response from the Tianyancha API for company holder information.
 * */
@Data
public class CompanyHolderResponse {
    
    @JSONField(name = "error_code")
    private Integer errorCode;
    
    private String reason;
    
    private CompanyHolderResult result;
    
    @Data
    public static class CompanyHolderResult {
        private Integer total;
        private List<CompanyHolder> items;
    }
    
    @Data
    public static class CompanyHolder {
        private Long id;
        private Long cgid;
        private String hcgid;
        private String logo;
        private String name;
        private String alias;
        private Integer type;
        private Long ftShareholding;
        private List<CapitalInfo> capital;
        private List<CapitalInfo> capitalActl;
    }
    
    @Data
    public static class CapitalInfo {
        private String amomon;
        private String time;
        private String percent;
        private String paymet;
    }
}