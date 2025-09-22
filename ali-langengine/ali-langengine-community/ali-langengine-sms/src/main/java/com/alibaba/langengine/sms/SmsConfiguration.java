package com.alibaba.langengine.sms;

import lombok.Data;

@Data
public class SmsConfiguration {
    
    // 阿里云短信配置
    public static final String ALIYUN_ACCESS_KEY_ID = System.getProperty("aliyun.sms.access.key.id");
    public static final String ALIYUN_ACCESS_KEY_SECRET = System.getProperty("aliyun.sms.access.key.secret");
    public static final String ALIYUN_SIGN_NAME = System.getProperty("aliyun.sms.sign.name");
    public static final String ALIYUN_TEMPLATE_CODE = System.getProperty("aliyun.sms.template.code");
    public static final String ALIYUN_REGION_ID = System.getProperty("aliyun.sms.region.id", "cn-hangzhou");
    
    // 腾讯云短信配置
    public static final String TENCENT_SECRET_ID = System.getProperty("tencent.sms.secret.id");
    public static final String TENCENT_SECRET_KEY = System.getProperty("tencent.sms.secret.key");
    public static final String TENCENT_APP_ID = System.getProperty("tencent.sms.app.id");
    public static final String TENCENT_SIGN_NAME = System.getProperty("tencent.sms.sign.name");
    public static final String TENCENT_TEMPLATE_ID = System.getProperty("tencent.sms.template.id");
    public static final String TENCENT_REGION = System.getProperty("tencent.sms.region", "ap-beijing");
    
    // 华为云短信配置
    public static final String HUAWEI_ACCESS_KEY = System.getProperty("huawei.sms.access.key");
    public static final String HUAWEI_SECRET_KEY = System.getProperty("huawei.sms.secret.key");
    public static final String HUAWEI_SIGN_NAME = System.getProperty("huawei.sms.sign.name");
    public static final String HUAWEI_TEMPLATE_ID = System.getProperty("huawei.sms.template.id");
    public static final String HUAWEI_REGION = System.getProperty("huawei.sms.region", "cn-north-4");
    
    // 通用配置
    public static final int SMS_TIMEOUT = Integer.parseInt(System.getProperty("sms.timeout", "30"));
    
    // 阿里云配置
    private String aliyunAccessKeyId;
    private String aliyunAccessKeySecret;
    private String aliyunSignName;
    private String aliyunTemplateCode;
    private String aliyunRegionId;
    
    // 腾讯云配置
    private String tencentSecretId;
    private String tencentSecretKey;
    private String tencentAppId;
    private String tencentSignName;
    private String tencentTemplateId;
    private String tencentRegion;
    
    // 华为云配置
    private String huaweiAccessKey;
    private String huaweiSecretKey;
    private String huaweiSignName;
    private String huaweiTemplateId;
    private String huaweiRegion;
    
    private int timeout;
    
    public SmsConfiguration() {
        this.aliyunAccessKeyId = ALIYUN_ACCESS_KEY_ID;
        this.aliyunAccessKeySecret = ALIYUN_ACCESS_KEY_SECRET;
        this.aliyunSignName = ALIYUN_SIGN_NAME;
        this.aliyunTemplateCode = ALIYUN_TEMPLATE_CODE;
        this.aliyunRegionId = ALIYUN_REGION_ID;
        
        this.tencentSecretId = TENCENT_SECRET_ID;
        this.tencentSecretKey = TENCENT_SECRET_KEY;
        this.tencentAppId = TENCENT_APP_ID;
        this.tencentSignName = TENCENT_SIGN_NAME;
        this.tencentTemplateId = TENCENT_TEMPLATE_ID;
        this.tencentRegion = TENCENT_REGION;
        
        this.huaweiAccessKey = HUAWEI_ACCESS_KEY;
        this.huaweiSecretKey = HUAWEI_SECRET_KEY;
        this.huaweiSignName = HUAWEI_SIGN_NAME;
        this.huaweiTemplateId = HUAWEI_TEMPLATE_ID;
        this.huaweiRegion = HUAWEI_REGION;
        
        this.timeout = SMS_TIMEOUT;
    }
}
