package com.alibaba.langengine.tianyancha.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;
/**
 * Company Basic Info Response
 * 
 * This class represents the response from the Tianyancha API for company basic information.
 * */
@Data
public class CompanyBasicInfoResponse {
    
    @JSONField(name = "error_code")
    private Integer errorCode;
    
    private String reason;
    
    private CompanyBasicInfo result;
    
    @Data
    public static class CompanyBasicInfo {
        private Long id;
        private String name;
        private String alias;
        private String legalPersonName;
        private String regStatus;
        private String regCapital;
        private String regNumber;
        private String creditCode;
        private String orgNumber;
        private String taxNumber;
        private String regLocation;
        private String businessScope;
        private Long estiblishTime;
        private Long approvedTime;
        private String industry;
        private String companyOrgType;
        private String regInstitute;
        private Integer percentileScore;
        private String base;
        private String city;
        private String district;
        private String staffNumRange;
        private Integer socialStaffNum;
        private String bondName;
        private String bondNum;
        private String bondType;
        private String historyNames;
        
        @JSONField(name = "historyNameList")
        private List<String> historyNameList;
        
        private CompanyIndustryAll industryAll;
        
        @Data
        public static class CompanyIndustryAll {
            private String category;
            private String categoryBig;
            private String categoryMiddle;
            private String categorySmall;
            private String categoryCodeFirst;
            private String categoryCodeSecond;
            private String categoryCodeThird;
            private String categoryCodeFourth;
        }
    }
}