/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.wenxin.service;

import com.alibaba.langengine.wenxin.model.completion.WenxinCompletionRequest;
import com.alibaba.langengine.wenxin.model.completion.WenxinCompletionResult;
import com.alibaba.langengine.wenxin.model.embedding.WenxinEmbeddingRequest;
import com.alibaba.langengine.wenxin.model.embedding.WenxinEmbeddingResult;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface WenxinApi {

    /**
     * ERNIE-4.0-8K 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-4.0-8k")
    Call<WenxinCompletionResult> ernie40Completion(@Header("Authorization") String authorization,
                                                   @Query("access_token") String accessToken,
                                                   @Body WenxinCompletionRequest request);

    /**
     * ERNIE-4.0-8K 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-4.0-8k")
    Call<ResponseBody> ernie40CompletionStream(@Header("Authorization") String authorization,
                                              @Query("access_token") String accessToken,
                                              @Body WenxinCompletionRequest request);

    /**
     * ERNIE-4.0-Turbo-8K 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-4.0-turbo-8k")
    Call<WenxinCompletionResult> ernie40TurboCompletion(@Header("Authorization") String authorization,
                                                        @Query("access_token") String accessToken,
                                                        @Body WenxinCompletionRequest request);

    /**
     * ERNIE-4.0-Turbo-8K 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-4.0-turbo-8k")
    Call<ResponseBody> ernie40TurboCompletionStream(@Header("Authorization") String authorization,
                                                   @Query("access_token") String accessToken,
                                                   @Body WenxinCompletionRequest request);

    /**
     * ERNIE-3.5-8K 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-3.5-8k")
    Call<WenxinCompletionResult> ernie35Completion(@Header("Authorization") String authorization,
                                                   @Query("access_token") String accessToken,
                                                   @Body WenxinCompletionRequest request);

    /**
     * ERNIE-3.5-8K 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-3.5-8k")
    Call<ResponseBody> ernie35CompletionStream(@Header("Authorization") String authorization,
                                              @Query("access_token") String accessToken,
                                              @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Lite-8K 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-lite-8k")
    Call<WenxinCompletionResult> ernieLiteCompletion(@Header("Authorization") String authorization,
                                                     @Query("access_token") String accessToken,
                                                     @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Lite-8K 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-lite-8k")
    Call<ResponseBody> ernieLiteCompletionStream(@Header("Authorization") String authorization,
                                                @Query("access_token") String accessToken,
                                                @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Speed-8K 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-speed-8k")
    Call<WenxinCompletionResult> ernieSpeedCompletion(@Header("Authorization") String authorization,
                                                      @Query("access_token") String accessToken,
                                                      @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Speed-8K 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-speed-8k")
    Call<ResponseBody> ernieSpeedCompletionStream(@Header("Authorization") String authorization,
                                                 @Query("access_token") String accessToken,
                                                 @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Speed-128K 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-speed-128k")
    Call<WenxinCompletionResult> ernieSpeed128kCompletion(@Header("Authorization") String authorization,
                                                          @Query("access_token") String accessToken,
                                                          @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Speed-128K 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-speed-128k")
    Call<ResponseBody> ernieSpeed128kCompletionStream(@Header("Authorization") String authorization,
                                                     @Query("access_token") String accessToken,
                                                     @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Tiny-8K 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-tiny-8k")
    Call<WenxinCompletionResult> ernieTinyCompletion(@Header("Authorization") String authorization,
                                                     @Query("access_token") String accessToken,
                                                     @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Tiny-8K 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-tiny-8k")
    Call<ResponseBody> ernieTinyCompletionStream(@Header("Authorization") String authorization,
                                                @Query("access_token") String accessToken,
                                                @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Character-8K 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-char-8k")
    Call<WenxinCompletionResult> ernieCharacterCompletion(@Header("Authorization") String authorization,
                                                          @Query("access_token") String accessToken,
                                                          @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Character-8K 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-char-8k")
    Call<ResponseBody> ernieCharacterCompletionStream(@Header("Authorization") String authorization,
                                                     @Query("access_token") String accessToken,
                                                     @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Novel-8K 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-novel-8k")
    Call<WenxinCompletionResult> ernieNovelCompletion(@Header("Authorization") String authorization,
                                                      @Query("access_token") String accessToken,
                                                      @Body WenxinCompletionRequest request);

    /**
     * ERNIE-Novel-8K 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/ernie-novel-8k")
    Call<ResponseBody> ernieNovelCompletionStream(@Header("Authorization") String authorization,
                                                 @Query("access_token") String accessToken,
                                                 @Body WenxinCompletionRequest request);

    /**
     * Yi-34B-Chat 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/yi-34b-chat")
    Call<WenxinCompletionResult> yi34bChatCompletion(@Header("Authorization") String authorization,
                                                     @Query("access_token") String accessToken,
                                                     @Body WenxinCompletionRequest request);

    /**
     * Yi-34B-Chat 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/yi-34b-chat")
    Call<ResponseBody> yi34bChatCompletionStream(@Header("Authorization") String authorization,
                                                @Query("access_token") String accessToken,
                                                @Body WenxinCompletionRequest request);

    /**
     * Qianfan-Chinese-Llama2-7B 聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/qianfan-chinese-llama2-7b")
    Call<WenxinCompletionResult> qianfanChineseLlama27bCompletion(@Header("Authorization") String authorization,
                                                                  @Query("access_token") String accessToken,
                                                                  @Body WenxinCompletionRequest request);

    /**
     * Qianfan-Chinese-Llama2-7B 流式聊天补全
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/qianfan-chinese-llama2-7b")
    Call<ResponseBody> qianfanChineseLlama27bCompletionStream(@Header("Authorization") String authorization,
                                                             @Query("access_token") String accessToken,
                                                             @Body WenxinCompletionRequest request);

    /**
     * 通用聊天补全接口
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/chat/completions")
    Call<WenxinCompletionResult> chatCompletion(@Header("Authorization") String authorization,
                                                @Query("access_token") String accessToken,
                                                @Body WenxinCompletionRequest request);

    /**
     * 通用流式聊天补全接口
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/chat/completions")
    Call<ResponseBody> chatCompletionStream(@Header("Authorization") String authorization,
                                           @Query("access_token") String accessToken,
                                           @Body WenxinCompletionRequest request);

    /**
     * Embedding-V1 文本向量化
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/embedding-v1")
    Call<WenxinEmbeddingResult> embeddingV1(@Header("Authorization") String authorization,
                                           @Query("access_token") String accessToken,
                                           @Body WenxinEmbeddingRequest request);

    /**
     * bge-large-zh 文本向量化
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/bge-large-zh")
    Call<WenxinEmbeddingResult> bgeLargeZh(@Header("Authorization") String authorization,
                                          @Query("access_token") String accessToken,
                                          @Body WenxinEmbeddingRequest request);

    /**
     * bge-large-en 文本向量化
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/bge-large-en")
    Call<WenxinEmbeddingResult> bgeLargeEn(@Header("Authorization") String authorization,
                                          @Query("access_token") String accessToken,
                                          @Body WenxinEmbeddingRequest request);

    /**
     * tao-8k 文本向量化
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/tao-8k")
    Call<WenxinEmbeddingResult> tao8k(@Header("Authorization") String authorization,
                                     @Query("access_token") String accessToken,
                                     @Body WenxinEmbeddingRequest request);

    /**
     * 通用文本向量化接口
     */
    @POST("rpc/2.0/ai/v1/bce-qianfan/api/v1/embeddings")
    Call<WenxinEmbeddingResult> embeddings(@Header("Authorization") String authorization,
                                          @Query("access_token") String accessToken,
                                          @Body WenxinEmbeddingRequest request);
}
