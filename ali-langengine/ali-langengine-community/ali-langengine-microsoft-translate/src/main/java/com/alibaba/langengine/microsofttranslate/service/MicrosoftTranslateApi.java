package com.alibaba.langengine.microsofttranslate.service;

import com.alibaba.langengine.microsofttranslate.model.MicrosoftTranslateRequest;
import com.alibaba.langengine.microsofttranslate.model.MicrosoftTranslateResponse;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.Map;

/**
 * Microsoft 翻译 API 接口
 *
 * @author Makoto
 */
public interface MicrosoftTranslateApi {
    
    @POST("/translate")
    Single<MicrosoftTranslateResponse> translate(@Body MicrosoftTranslateRequest request,
                                                @Query("api-version") String apiVersion,
                                                @HeaderMap Map<String, String> headers);
}
