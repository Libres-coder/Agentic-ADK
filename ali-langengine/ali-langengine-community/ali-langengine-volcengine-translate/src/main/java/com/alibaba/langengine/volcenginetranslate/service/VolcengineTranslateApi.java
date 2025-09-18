package com.alibaba.langengine.volcenginetranslate.service;

import com.alibaba.langengine.volcenginetranslate.model.VolcengineTranslateRequest;
import com.alibaba.langengine.volcenginetranslate.model.VolcengineTranslateResponse;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

import java.util.Map;

/**
 * 火山翻译 API 接口
 *
 * @author Makoto
 */
public interface VolcengineTranslateApi {
    
    @POST("/")
    Single<VolcengineTranslateResponse> translate(@Body VolcengineTranslateRequest request,
                                                 @HeaderMap Map<String, String> headers);
}
