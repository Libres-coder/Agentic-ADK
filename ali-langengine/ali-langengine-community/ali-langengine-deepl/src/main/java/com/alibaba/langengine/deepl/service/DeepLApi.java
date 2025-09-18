package com.alibaba.langengine.deepl.service;

import com.alibaba.langengine.deepl.model.DeepLTranslateRequest;
import com.alibaba.langengine.deepl.model.DeepLTranslateResponse;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

import java.util.Map;

/**
 * DeepL API 接口
 *
 * @author Makoto
 */
public interface DeepLApi {
    
    @POST("/v2/translate")
    Single<DeepLTranslateResponse> translate(@Body DeepLTranslateRequest request,
                                            @HeaderMap Map<String, String> headers);
}
