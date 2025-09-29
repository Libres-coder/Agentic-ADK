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
package com.alibaba.langengine.rockset.vectorstore.service;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;


public interface RocksetApi {
    
    @POST("v1/orgs/self/ws/{workspace}/collections")
    Call<RocksetCreateCollectionResponse> createCollection(
        @Path("workspace") String workspace,
        @Body RocksetCreateCollectionRequest request,
        @HeaderMap Map<String, String> headers
    );
    
    @GET("v1/orgs/self/ws/{workspace}/collections/{collection}")
    Call<RocksetCollectionData> getCollection(
        @Path("workspace") String workspace,
        @Path("collection") String collection,
        @HeaderMap Map<String, String> headers
    );
    
    @POST("v1/orgs/self/ws/{workspace}/collections/{collection}/docs")
    Call<RocksetInsertResponse> insertDocuments(
        @Path("workspace") String workspace,
        @Path("collection") String collection,
        @Body RocksetInsertRequest request,
        @HeaderMap Map<String, String> headers
    );
    
    @POST("v1/orgs/self/queries")
    Call<RocksetQueryResponse> query(
        @Body RocksetQueryRequest request,
        @HeaderMap Map<String, String> headers
    );
    
    @DELETE("v1/orgs/self/ws/{workspace}/collections/{collection}/docs")
    Call<RocksetDeleteResponse> deleteDocuments(
        @Path("workspace") String workspace,
        @Path("collection") String collection,
        @Body RocksetDeleteRequest request,
        @HeaderMap Map<String, String> headers
    );
}
