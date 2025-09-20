package com.alibaba.langengine.tencenttranslate.service;

import com.alibaba.langengine.tencenttranslate.model.TencentTranslateRequest;
import com.alibaba.langengine.tencenttranslate.model.TencentTranslateResponse;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

import java.util.Map;

/**
 * 腾讯翻译 API 接口
 *
 * @author Makoto
 */
public interface TencentTranslateApi {
    
    @POST("/")
    Single<TencentTranslateResponse> translate(@Body TencentTranslateRequest request,
                                              @HeaderMap Map<String, String> headers);
}
