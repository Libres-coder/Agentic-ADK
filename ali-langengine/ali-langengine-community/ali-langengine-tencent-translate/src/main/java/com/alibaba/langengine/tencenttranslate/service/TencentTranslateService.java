package com.alibaba.langengine.tencenttranslate.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.tencenttranslate.model.TencentTranslateRequest;
import com.alibaba.langengine.tencenttranslate.model.TencentTranslateResponse;
import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯翻译服务
 *
 * @author Makoto
 */
@Data
public class TencentTranslateService extends RetrofitInitService<TencentTranslateApi> {
    
    private String secretId;
    private String secretKey;
    private String region;
    private String service;
    private String version;
    private String action;
    
    public TencentTranslateService(String serverUrl, Duration timeout, String secretId, String secretKey, 
                                   String region, String service, String version, String action) {
        super(serverUrl, timeout, false, secretId, null);
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.region = region;
        this.service = service;
        this.version = version;
        this.action = action;
    }
    
    @Override
    public Class<TencentTranslateApi> getServiceApiClass() {
        return TencentTranslateApi.class;
    }
    
    public TencentTranslateResponse translate(TencentTranslateRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", generateAuthorization(request));
        headers.put("Content-Type", "application/json");
        headers.put("Host", service + ".tencentcloudapi.com");
        headers.put("X-TC-Action", action);
        headers.put("X-TC-Timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        headers.put("X-TC-Version", version);
        headers.put("X-TC-Region", region);
        
        return execute(getApi().translate(request, headers));
    }
    
    private String generateAuthorization(TencentTranslateRequest request) {
        // 这里需要实现腾讯云的签名算法
        // 为了简化，这里返回一个占位符
        return "TC3-HMAC-SHA256 Credential=" + secretId + "/" + getDate() + "/" + service + "/tc3_request";
    }
    
    private String getDate() {
        return java.time.LocalDate.now().toString().replace("-", "");
    }
}
