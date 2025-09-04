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
package com.alibaba.langengine.notion.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.notion.NotionConfiguration;
import com.alibaba.langengine.notion.exception.NotionException;
import com.alibaba.langengine.notion.model.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
public class NotionClient {
    
    private static final String BASE_URL = "https://api.notion.com";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    
    private final NotionConfiguration configuration;
    private final OkHttpClient httpClient;
    
    /**
     * 构造函数
     * 
     * @param configuration Notion配置
     */
    public NotionClient(NotionConfiguration configuration) {
        this.configuration = configuration;
        if (!configuration.isValid()) {
            throw new IllegalArgumentException("Invalid Notion configuration");
        }
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS)
                .addInterceptor(this::addAuthHeaders)
                .build();
    }
    
    /**
     * 添加认证头部
     */
    private Response addAuthHeaders(Interceptor.Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + configuration.getToken())
                .header("Notion-Version", configuration.getVersion())
                .header("Content-Type", "application/json");
        
        return chain.proceed(builder.build());
    }
    
    /**
     * 搜索数据库和页面
     * 
     * @param query 搜索查询
     * @param filter 过滤条件
     * @param sort 排序条件
     * @return 搜索结果
     * @throws NotionException 当API调用失败时
     */
    public NotionSearchResult search(String query, JSONObject filter, JSONObject sort) throws NotionException {
        JSONObject requestBody = new JSONObject();
        
        if (StringUtils.isNotBlank(query)) {
            requestBody.put("query", query);
        }
        
        if (filter != null) {
            requestBody.put("filter", filter);
        }
        
        if (sort != null) {
            requestBody.put("sort", sort);
        }
        
        String response = post("/v1/search", requestBody.toJSONString());
        return JSON.parseObject(response, NotionSearchResult.class);
    }
    
    /**
     * 获取数据库
     * 
     * @param databaseId 数据库ID
     * @return 数据库信息
     * @throws NotionException 当API调用失败时
     */
    public NotionDatabase getDatabase(String databaseId) throws NotionException {
        String response = get("/v1/databases/" + databaseId);
        return JSON.parseObject(response, NotionDatabase.class);
    }
    
    /**
     * 查询数据库
     * 
     * @param databaseId 数据库ID
     * @param filter 过滤条件
     * @param sorts 排序条件
     * @param startCursor 开始游标
     * @param pageSize 页面大小
     * @return 查询结果
     * @throws NotionException 当API调用失败时
     */
    public NotionQueryResult queryDatabase(String databaseId, JSONObject filter, 
                                         JSONArray sorts, String startCursor, Integer pageSize) throws NotionException {
        JSONObject requestBody = new JSONObject();
        
        if (filter != null) {
            requestBody.put("filter", filter);
        }
        
        if (sorts != null && !sorts.isEmpty()) {
            requestBody.put("sorts", sorts);
        }
        
        if (StringUtils.isNotBlank(startCursor)) {
            requestBody.put("start_cursor", startCursor);
        }
        
        if (pageSize != null && pageSize > 0) {
            requestBody.put("page_size", Math.min(pageSize, 100)); // API最大限制100
        }
        
        String response = post("/v1/databases/" + databaseId + "/query", requestBody.toJSONString());
        return JSON.parseObject(response, NotionQueryResult.class);
    }
    
    /**
     * 创建数据库页面
     * 
     * @param databaseId 数据库ID
     * @param properties 页面属性
     * @param children 子块内容
     * @return 创建的页面
     * @throws NotionException 当API调用失败时
     */
    public NotionPage createDatabasePage(String databaseId, JSONObject properties, JSONArray children) throws NotionException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("parent", new JSONObject().fluentPut("database_id", databaseId));
        
        if (properties != null) {
            requestBody.put("properties", properties);
        }
        
        if (children != null && !children.isEmpty()) {
            requestBody.put("children", children);
        }
        
        String response = post("/v1/pages", requestBody.toJSONString());
        return JSON.parseObject(response, NotionPage.class);
    }
    
    /**
     * 获取页面
     * 
     * @param pageId 页面ID
     * @return 页面信息
     * @throws NotionException 当API调用失败时
     */
    public NotionPage getPage(String pageId) throws NotionException {
        String response = get("/v1/pages/" + pageId);
        return JSON.parseObject(response, NotionPage.class);
    }
    
    /**
     * 更新页面
     * 
     * @param pageId 页面ID
     * @param properties 要更新的属性
     * @return 更新后的页面
     * @throws NotionException 当API调用失败时
     */
    public NotionPage updatePage(String pageId, JSONObject properties) throws NotionException {
        JSONObject requestBody = new JSONObject();
        if (properties != null) {
            requestBody.put("properties", properties);
        }
        
        String response = patch("/v1/pages/" + pageId, requestBody.toJSONString());
        return JSON.parseObject(response, NotionPage.class);
    }
    
    /**
     * 获取页面的子块
     * 
     * @param pageId 页面ID
     * @param startCursor 开始游标
     * @param pageSize 页面大小
     * @return 块列表
     * @throws NotionException 当API调用失败时
     */
    public NotionBlocksResult getPageBlocks(String pageId, String startCursor, Integer pageSize) throws NotionException {
        StringBuilder url = new StringBuilder("/v1/blocks/" + pageId + "/children");
        List<String> params = new ArrayList<>();
        
        if (StringUtils.isNotBlank(startCursor)) {
            params.add("start_cursor=" + startCursor);
        }
        
        if (pageSize != null && pageSize > 0) {
            params.add("page_size=" + Math.min(pageSize, 100));
        }
        
        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }
        
        String response = get(url.toString());
        return JSON.parseObject(response, NotionBlocksResult.class);
    }
    
    /**
     * 向页面添加块
     * 
     * @param pageId 页面ID
     * @param children 要添加的子块
     * @return 添加结果
     * @throws NotionException 当API调用失败时
     */
    public NotionBlocksResult appendBlocks(String pageId, JSONArray children) throws NotionException {
        JSONObject requestBody = new JSONObject();
        if (children != null && !children.isEmpty()) {
            requestBody.put("children", children);
        }
        
        String response = patch("/v1/blocks/" + pageId + "/children", requestBody.toJSONString());
        return JSON.parseObject(response, NotionBlocksResult.class);
    }
    
    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     * @throws NotionException 当API调用失败时
     */
    public NotionUser getUser(String userId) throws NotionException {
        String response = get("/v1/users/" + userId);
        return JSON.parseObject(response, NotionUser.class);
    }
    
    /**
     * 获取当前用户信息
     * 
     * @return 当前用户信息
     * @throws NotionException 当API调用失败时
     */
    public NotionUser getCurrentUser() throws NotionException {
        String response = get("/v1/users/me");
        return JSON.parseObject(response, NotionUser.class);
    }
    
    /**
     * 列出所有用户
     * 
     * @param startCursor 开始游标
     * @param pageSize 页面大小
     * @return 用户列表
     * @throws NotionException 当API调用失败时
     */
    public NotionUsersResult listUsers(String startCursor, Integer pageSize) throws NotionException {
        StringBuilder url = new StringBuilder("/v1/users");
        List<String> params = new ArrayList<>();
        
        if (StringUtils.isNotBlank(startCursor)) {
            params.add("start_cursor=" + startCursor);
        }
        
        if (pageSize != null && pageSize > 0) {
            params.add("page_size=" + Math.min(pageSize, 100));
        }
        
        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }
        
        String response = get(url.toString());
        return JSON.parseObject(response, NotionUsersResult.class);
    }
    
    /**
     * 执行GET请求
     * 
     * @param endpoint API端点
     * @return 响应内容
     * @throws NotionException 当请求失败时
     */
    private String get(String endpoint) throws NotionException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .get()
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * 执行POST请求
     * 
     * @param endpoint API端点
     * @param body 请求体
     * @return 响应内容
     * @throws NotionException 当请求失败时
     */
    private String post(String endpoint, String body) throws NotionException {
        RequestBody requestBody = RequestBody.create(body, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(requestBody)
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * 执行PATCH请求
     * 
     * @param endpoint API端点
     * @param body 请求体
     * @return 响应内容
     * @throws NotionException 当请求失败时
     */
    private String patch(String endpoint, String body) throws NotionException {
        RequestBody requestBody = RequestBody.create(body, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .patch(requestBody)
                .build();
        
        return executeRequest(request);
    }
    
    /**
     * 执行HTTP请求
     * 
     * @param request HTTP请求
     * @return 响应内容
     * @throws NotionException 当请求失败时
     */
    private String executeRequest(Request request) throws NotionException {
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (configuration.isDebug()) {
                log.debug("Notion API Request: {} {}", request.method(), request.url());
                log.debug("Notion API Response: {} - {}", response.code(), responseBody);
            }
            
            if (!response.isSuccessful()) {
                JSONObject errorInfo = JSON.parseObject(responseBody);
                String errorMessage = String.format("Notion API error: %d - %s", 
                    response.code(), 
                    errorInfo != null ? errorInfo.getString("message") : "Unknown error");
                throw new NotionException(errorMessage, response.code(), errorInfo);
            }
            
            return responseBody;
        } catch (IOException e) {
            throw new NotionException("Failed to execute Notion API request", e);
        }
    }
    
    /**
     * 追加子块到页面
     * 
     * @param pageId 页面ID
     * @param children 子块数组
     * @return API响应
     * @throws NotionException 当API调用失败时
     */
    public String appendBlockChildren(String pageId, JSONArray children) throws NotionException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("children", children);
        return patch("/v1/blocks/" + pageId + "/children", requestBody.toJSONString());
    }
    
    /**
     * 获取块信息
     * 
     * @param blockId 块ID
     * @return 块信息
     * @throws NotionException 当API调用失败时
     */
    public String retrieveBlock(String blockId) throws NotionException {
        return get("/v1/blocks/" + blockId);
    }
    
    /**
     * 更新块
     * 
     * @param blockId 块ID
     * @param updateData 更新数据
     * @return API响应
     * @throws NotionException 当API调用失败时
     */
    public String updateBlock(String blockId, JSONObject updateData) throws NotionException {
        return patch("/v1/blocks/" + blockId, updateData.toJSONString());
    }
    
    /**
     * 删除块
     * 
     * @param blockId 块ID
     * @return API响应
     * @throws NotionException 当API调用失败时
     */
    public String deleteBlock(String blockId) throws NotionException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("archived", true);
        return patch("/v1/blocks/" + blockId, requestBody.toJSONString());
    }
}
