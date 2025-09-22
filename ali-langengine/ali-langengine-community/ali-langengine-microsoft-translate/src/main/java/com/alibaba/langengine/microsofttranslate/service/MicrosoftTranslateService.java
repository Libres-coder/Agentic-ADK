package com.alibaba.langengine.microsofttranslate.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.microsofttranslate.model.MicrosoftTranslateRequest;
import com.alibaba.langengine.microsofttranslate.model.MicrosoftTranslateResponse;
import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Microsoft 翻译服务
 *
 * @author Makoto
 */
@Data
public class MicrosoftTranslateService extends RetrofitInitService<MicrosoftTranslateApi> {
    
    private String subscriptionKey;
    private String region;
    private String apiVersion;
    
    public MicrosoftTranslateService(String serverUrl, Duration timeout, String subscriptionKey, String region, String apiVersion) {
        super(serverUrl, timeout, false, subscriptionKey, null);
        this.subscriptionKey = subscriptionKey;
        this.region = region;
        this.apiVersion = apiVersion;
    }
    
    @Override
    public Class<MicrosoftTranslateApi> getServiceApiClass() {
        return MicrosoftTranslateApi.class;
    }
    
    public MicrosoftTranslateResponse translate(MicrosoftTranslateRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Ocp-Apim-Subscription-Key", subscriptionKey);
        headers.put("Ocp-Apim-Subscription-Region", region);
        headers.put("Content-Type", "application/json");
        
        return execute(getApi().translate(request, apiVersion, headers));
    }
}
