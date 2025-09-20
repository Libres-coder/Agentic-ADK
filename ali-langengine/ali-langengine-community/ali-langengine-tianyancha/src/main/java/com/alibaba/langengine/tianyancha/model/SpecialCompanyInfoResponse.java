package com.alibaba.langengine.tianyancha.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;
/**
 * Special Company Info Response
 * 
 * This class represents the response from the Tianyancha API for special company information.
 * */
@Data
public class SpecialCompanyInfoResponse {
    
    @JSONField(name = "error_code")
    private Integer errorCode;
    
    private String reason;
    
    private SpecialCompanyInfo result;
    
    @Data
    public static class SpecialCompanyInfo {
        private Long id;
        private String name;
        private String nameEn;
        private String nameTraditional;
        private String logo;
        private Integer entityType;
        private String regStatus;
        private Long estiblishTime;
        private String companyOrgType;
        private String companyNum;
        private String legalPersonName;
        private String regLocation;
        private String businessScope;
        private String creditCode;
        private String regCapital;
        private String regInstitute;
        private String businessUnit;
        private String phoneNumber;
        private List<String> phoneList;
        private String email;
        private List<String> emailList;
        private String websiteList;
        private String district;
        private String districtCode;
        private Boolean haveReport;
        private String base;
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