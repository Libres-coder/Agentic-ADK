package com.alibaba.langengine.alipay;

import lombok.Data;

@Data
public class AlipayConfiguration {
    
    public static final String ALIPAY_APP_ID = System.getProperty("alipay.app.id");
    public static final String ALIPAY_PRIVATE_KEY = System.getProperty("alipay.private.key");
    public static final String ALIPAY_PUBLIC_KEY = System.getProperty("alipay.public.key");
    public static final String ALIPAY_GATEWAY_URL = System.getProperty("alipay.gateway.url", "https://openapi.alipay.com/gateway.do");
    public static final String ALIPAY_CHARSET = System.getProperty("alipay.charset", "UTF-8");
    public static final String ALIPAY_SIGN_TYPE = System.getProperty("alipay.sign.type", "RSA2");
    public static final String ALIPAY_FORMAT = System.getProperty("alipay.format", "JSON");
    public static final String ALIPAY_VERSION = System.getProperty("alipay.version", "1.0");
    public static final int ALIPAY_TIMEOUT = Integer.parseInt(System.getProperty("alipay.timeout", "30"));
    
    private String appId;
    private String privateKey;
    private String publicKey;
    private String gatewayUrl;
    private String charset;
    private String signType;
    private String format;
    private String version;
    private int timeout;
    
    public AlipayConfiguration() {
        this.appId = ALIPAY_APP_ID;
        this.privateKey = ALIPAY_PRIVATE_KEY;
        this.publicKey = ALIPAY_PUBLIC_KEY;
        this.gatewayUrl = ALIPAY_GATEWAY_URL;
        this.charset = ALIPAY_CHARSET;
        this.signType = ALIPAY_SIGN_TYPE;
        this.format = ALIPAY_FORMAT;
        this.version = ALIPAY_VERSION;
        this.timeout = ALIPAY_TIMEOUT;
    }
    
    public AlipayConfiguration(String appId, String privateKey, String publicKey) {
        this.appId = appId;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.gatewayUrl = ALIPAY_GATEWAY_URL;
        this.charset = ALIPAY_CHARSET;
        this.signType = ALIPAY_SIGN_TYPE;
        this.format = ALIPAY_FORMAT;
        this.version = ALIPAY_VERSION;
        this.timeout = ALIPAY_TIMEOUT;
    }
}
