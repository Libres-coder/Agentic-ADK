package com.alibaba.langengine.deepl.service;

import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.deepl.model.DeepLTranslateRequest;
import com.alibaba.langengine.deepl.model.DeepLTranslateResponse;
import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * DeepL 翻译服务
 *
 * @author Makoto
 */
@Data
public class DeepLService extends RetrofitInitService<DeepLApi> {
    
    private String apiKey;
    private boolean isPro;
    
    public DeepLService(String serverUrl, Duration timeout, String apiKey, boolean isPro) {
        super(serverUrl, timeout, false, apiKey, null);
        this.apiKey = apiKey;
        this.isPro = isPro;
    }
    
    @Override
    public Class<DeepLApi> getServiceApiClass() {
        return DeepLApi.class;
    }
    
    public DeepLTranslateResponse translate(DeepLTranslateRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "DeepL-Auth-Key " + apiKey);
        headers.put("Content-Type", "application/json");
        
        return execute(getApi().translate(request, headers));
    }
}
