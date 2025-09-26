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
package com.alibaba.langengine.deeplake.vectorstore.service;

import io.reactivex.Single;
import retrofit2.http.*;

import java.util.Map;


public interface DeepLakeApi {

    /**
     * Create a new dataset
     */
    @POST("datasets")
    Single<DeepLakeDatasetInfo> createDataset(@Body Map<String, Object> request);

    /**
     * Get dataset information
     */
    @GET("datasets/{datasetName}")
    Single<DeepLakeDatasetInfo> getDataset(@Path("datasetName") String datasetName);

    /**
     * Delete dataset
     */
    @DELETE("datasets/{datasetName}")
    Single<Void> deleteDataset(@Path("datasetName") String datasetName);

    /**
     * Insert vectors into dataset
     */
    @POST("datasets/{datasetName}/vectors")
    Single<Map<String, Object>> insertVectors(
        @Path("datasetName") String datasetName,
        @Body DeepLakeInsertRequest request
    );

    /**
     * Query vectors from dataset
     */
    @POST("datasets/{datasetName}/query")
    Single<DeepLakeQueryResponse> queryVectors(
        @Path("datasetName") String datasetName,
        @Body DeepLakeQueryRequest request
    );

    /**
     * Delete vectors by IDs
     */
    @DELETE("datasets/{datasetName}/vectors")
    Single<Void> deleteVectors(
        @Path("datasetName") String datasetName,
        @Query("ids") String ids
    );

    /**
     * Update vectors
     */
    @PUT("datasets/{datasetName}/vectors")
    Single<Map<String, Object>> updateVectors(
        @Path("datasetName") String datasetName,
        @Body DeepLakeInsertRequest request
    );

    /**
     * Get dataset statistics
     */
    @GET("datasets/{datasetName}/stats")
    Single<Map<String, Object>> getDatasetStats(@Path("datasetName") String datasetName);
}
