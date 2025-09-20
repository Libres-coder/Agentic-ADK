package com.alibaba.langengine.volcenginetranslate.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.volcenginetranslate.model.VolcengineTranslateRequest;
import com.alibaba.langengine.volcenginetranslate.model.VolcengineTranslateResponse;
import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 火山翻译服务
 *
 * @author Makoto
 */
@Data
public class VolcengineTranslateService extends RetrofitInitService<VolcengineTranslateApi> {
    
    private String accessKey;
    private String secretKey;
    private String region;
    private String service;
    private String version;
    private String action;
    
    public VolcengineTranslateService(String serverUrl, Duration timeout, String accessKey, String secretKey, 
                                      String region, String service, String version, String action) {
        super(serverUrl, timeout, false, accessKey, null);
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.service = service;
        this.version = version;
        this.action = action;
    }
    
    @Override
    public Class<VolcengineTranslateApi> getServiceApiClass() {
        return VolcengineTranslateApi.class;
    }
    
    public VolcengineTranslateResponse translate(VolcengineTranslateRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", generateAuthorization(request));
        headers.put("Content-Type", "application/json");
        headers.put("Host", service + ".volcengineapi.com");
        headers.put("X-Date", getCurrentDate());
        headers.put("X-Version", version);
        headers.put("X-Action", action);
        headers.put("X-Region", region);
        
        return execute(getApi().translate(request, headers));
    }
    
    private String generateAuthorization(VolcengineTranslateRequest request) {
        // 这里需要实现火山引擎的签名算法
        // 为了简化，这里返回一个占位符
        return "HMAC-SHA256 Credential=" + accessKey + "/" + getCurrentDate() + "/" + region + "/" + service + "/request";
    }
    
    private String getCurrentDate() {
        return java.time.Instant.now().toString().substring(0, 8);
    }
}
