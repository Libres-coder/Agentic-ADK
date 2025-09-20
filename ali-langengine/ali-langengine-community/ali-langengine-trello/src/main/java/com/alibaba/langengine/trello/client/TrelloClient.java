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
package com.alibaba.langengine.trello.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.trello.TrelloConfiguration;
import com.alibaba.langengine.trello.exception.TrelloException;
import com.alibaba.langengine.trello.model.TrelloBoard;
import com.alibaba.langengine.trello.model.TrelloCard;
import com.alibaba.langengine.trello.model.TrelloList;
import com.alibaba.langengine.trello.model.TrelloMember;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Data
public class TrelloClient {
    
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    
    private TrelloConfiguration configuration;
    private OkHttpClient httpClient;
    
    /**
     * 构造函数
     * 
     * @param configuration Trello配置
     */
    public TrelloClient(TrelloConfiguration configuration) {
        this.configuration = configuration;
        this.httpClient = createHttpClient();
    }
    
    /**
     * 创建HTTP客户端
     * 
     * @return OkHttpClient实例
     */
    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(configuration.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(configuration.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(configuration.getTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }
    
    /**
     * 执行GET请求
     * 
     * @param endpoint API端点
     * @return 响应的JSON对象
     * @throws TrelloException Trello异常
     */
    public JSONObject get(String endpoint) throws TrelloException {
        return request("GET", endpoint, null);
    }
    
    /**
     * 执行POST请求
     * 
     * @param endpoint API端点
     * @param body 请求体
     * @return 响应的JSON对象
     * @throws TrelloException Trello异常
     */
    public JSONObject post(String endpoint, JSONObject body) throws TrelloException {
        return request("POST", endpoint, body);
    }
    
    /**
     * 执行PUT请求
     * 
     * @param endpoint API端点
     * @param body 请求体
     * @return 响应的JSON对象
     * @throws TrelloException Trello异常
     */
    public JSONObject put(String endpoint, JSONObject body) throws TrelloException {
        return request("PUT", endpoint, body);
    }
    
    /**
     * 执行DELETE请求
     * 
     * @param endpoint API端点
     * @return 响应的JSON对象
     * @throws TrelloException Trello异常
     */
    public JSONObject delete(String endpoint) throws TrelloException {
        return request("DELETE", endpoint, null);
    }
    
    /**
     * 执行HTTP请求
     * 
     * @param method HTTP方法
     * @param endpoint API端点
     * @param body 请求体
     * @return 响应的JSON对象
     * @throws TrelloException Trello异常
     */
    private JSONObject request(String method, String endpoint, JSONObject body) throws TrelloException {
        try {
            String url = configuration.getApiUrl(endpoint);
            Request.Builder requestBuilder = new Request.Builder().url(url);
            
            RequestBody requestBody = null;
            if (body != null) {
                requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    body.toJSONString()
                );
            }
            
            switch (method.toUpperCase()) {
                case "GET":
                    requestBuilder.get();
                    break;
                case "POST":
                    requestBuilder.post(requestBody != null ? requestBody : 
                        RequestBody.create(MediaType.parse("application/json"), ""));
                    break;
                case "PUT":
                    requestBuilder.put(requestBody != null ? requestBody : 
                        RequestBody.create(MediaType.parse("application/json"), ""));
                    break;
                case "DELETE":
                    requestBuilder.delete(requestBody);
                    break;
                default:
                    throw new TrelloException("不支持的HTTP方法: " + method);
            }
            
            Request request = requestBuilder.build();
            
            if (configuration.isDebug()) {
                log.debug("发送Trello API请求: {} {}", method, url);
                if (body != null) {
                    log.debug("请求体: {}", body.toJSONString());
                }
            }
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                
                if (configuration.isDebug()) {
                    log.debug("Trello API响应: {} {}", response.code(), responseBody);
                }
                
                if (!response.isSuccessful()) {
                    throw new TrelloException(
                        String.format("Trello API请求失败: %d %s - %s", 
                                     response.code(), response.message(), responseBody)
                    );
                }
                
                if (StringUtils.isBlank(responseBody)) {
                    return new JSONObject();
                }
                
                return JSON.parseObject(responseBody);
            }
            
        } catch (IOException e) {
            throw new TrelloException("Trello API请求失败", e);
        }
    }
    
    /**
     * 发送GET请求并返回JSONArray
     * 
     * @param endpoint API端点
     * @return JSONArray响应
     * @throws TrelloException Trello异常
     */
    public JSONArray getArray(String endpoint) throws TrelloException {
        return executeArrayRequest("GET", endpoint, null);
    }
    
    /**
     * 执行HTTP请求，返回JSONArray
     * 
     * @param method HTTP方法
     * @param endpoint API端点
     * @param body 请求体
     * @return JSONArray响应
     * @throws TrelloException Trello异常
     */
    private JSONArray executeArrayRequest(String method, String endpoint, JSONObject body) throws TrelloException {
        String url = configuration.getApiUrl(endpoint);
        
        try {
            Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "OAuth " + 
                          "oauth_consumer_key=\"" + configuration.getApiKey() + "\", " +
                          "oauth_token=\"" + configuration.getToken() + "\"");
            
            switch (method.toUpperCase()) {
                case "GET":
                    requestBuilder.get();
                    break;
                case "POST":
                    RequestBody postBody = body != null 
                        ? RequestBody.create(body.toJSONString(), JSON_MEDIA_TYPE)
                        : RequestBody.create("", JSON_MEDIA_TYPE);
                    requestBuilder.post(postBody);
                    break;
                case "PUT":
                    RequestBody putBody = body != null 
                        ? RequestBody.create(body.toJSONString(), JSON_MEDIA_TYPE)
                        : RequestBody.create("", JSON_MEDIA_TYPE);
                    requestBuilder.put(putBody);
                    break;
                case "DELETE":
                    requestBuilder.delete();
                    break;
                default:
                    throw new TrelloException("不支持的HTTP方法: " + method);
            }
            
            Request request = requestBuilder.build();
            
            if (configuration.isDebug()) {
                log.debug("发送Trello API请求: {} {}", method, url);
                if (body != null) {
                    log.debug("请求体: {}", body.toJSONString());
                }
            }
            
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                
                if (configuration.isDebug()) {
                    log.debug("Trello API响应: {} {}", response.code(), responseBody);
                }
                
                if (!response.isSuccessful()) {
                    throw new TrelloException(
                        String.format("Trello API请求失败: %d %s - %s", 
                                     response.code(), response.message(), responseBody)
                    );
                }
                
                if (StringUtils.isBlank(responseBody)) {
                    return new JSONArray();
                }
                
                return JSON.parseArray(responseBody);
            }
            
        } catch (IOException e) {
            throw new TrelloException("Trello API请求失败", e);
        }
    }

    /**
     * 获取用户的所有看板
     * 
     * @param memberId 用户ID（可以是"me"表示当前用户）
     * @return 看板列表
     * @throws TrelloException Trello异常
     */
    public List<TrelloBoard> getBoards(String memberId) throws TrelloException {
        JSONArray boardsArray = getArray(String.format("/members/%s/boards", memberId));
        
        List<TrelloBoard> boards = new ArrayList<>();
        for (int i = 0; i < boardsArray.size(); i++) {
            JSONObject boardJson = boardsArray.getJSONObject(i);
            boards.add(TrelloBoard.fromJson(boardJson));
        }
        return boards;
    }
    
    /**
     * 获取看板信息
     * 
     * @param boardId 看板ID
     * @return 看板信息
     * @throws TrelloException Trello异常
     */
    public TrelloBoard getBoard(String boardId) throws TrelloException {
        JSONObject response = get(String.format("/boards/%s", boardId));
        return TrelloBoard.fromJson(response);
    }
    
    /**
     * 获取看板中的列表
     * 
     * @param boardId 看板ID
     * @return 列表集合
     * @throws TrelloException Trello异常
     */
    public List<TrelloList> getLists(String boardId) throws TrelloException {
        JSONArray listsArray = getArray(String.format("/boards/%s/lists", boardId));
        
        List<TrelloList> lists = new ArrayList<>();
        for (int i = 0; i < listsArray.size(); i++) {
            JSONObject listJson = listsArray.getJSONObject(i);
            lists.add(TrelloList.fromJson(listJson));
        }
        return lists;
    }
    
    /**
     * 获取列表中的卡片
     * 
     * @param listId 列表ID
     * @return 卡片列表
     * @throws TrelloException Trello异常
     */
    public List<TrelloCard> getCards(String listId) throws TrelloException {
        JSONArray cardsArray = getArray(String.format("/lists/%s/cards", listId));
        
        List<TrelloCard> cards = new ArrayList<>();
        for (int i = 0; i < cardsArray.size(); i++) {
            JSONObject cardJson = cardsArray.getJSONObject(i);
            cards.add(TrelloCard.fromJson(cardJson));
        }
        return cards;
    }
    
    /**
     * 创建新卡片
     * 
     * @param card 卡片信息
     * @return 创建的卡片
     * @throws TrelloException Trello异常
     */
    public TrelloCard createCard(TrelloCard card) throws TrelloException {
        JSONObject cardJson = card.toJson();
        JSONObject response = post("/cards", cardJson);
        return TrelloCard.fromJson(response);
    }
    
    /**
     * 更新卡片
     * 
     * @param cardId 卡片ID
     * @param card 更新的卡片信息
     * @return 更新后的卡片
     * @throws TrelloException Trello异常
     */
    public TrelloCard updateCard(String cardId, TrelloCard card) throws TrelloException {
        JSONObject cardJson = card.toJson();
        JSONObject response = put(String.format("/cards/%s", cardId), cardJson);
        return TrelloCard.fromJson(response);
    }
    
    /**
     * 删除卡片
     * 
     * @param cardId 卡片ID
     * @throws TrelloException Trello异常
     */
    public void deleteCard(String cardId) throws TrelloException {
        delete(String.format("/cards/%s", cardId));
    }
    
    /**
     * 移动卡片到其他列表
     * 
     * @param cardId 卡片ID
     * @param targetListId 目标列表ID
     * @return 移动后的卡片
     * @throws TrelloException Trello异常
     */
    public TrelloCard moveCard(String cardId, String targetListId) throws TrelloException {
        JSONObject updateData = new JSONObject();
        updateData.put("idList", targetListId);
        JSONObject response = put(String.format("/cards/%s", cardId), updateData);
        return TrelloCard.fromJson(response);
    }
    
    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     * @throws TrelloException Trello异常
     */
    public TrelloMember getCurrentMember() throws TrelloException {
        JSONObject response = get("/members/me");
        return TrelloMember.fromJson(response);
    }
    
    /**
     * 获取看板成员列表
     * 
     * @param boardId 看板ID
     * @return 成员列表
     * @throws TrelloException Trello异常
     */
    public List<TrelloMember> getBoardMembers(String boardId) throws TrelloException {
        JSONArray membersArray = getArray(String.format("/boards/%s/members", boardId));
        
        List<TrelloMember> members = new ArrayList<>();
        for (int i = 0; i < membersArray.size(); i++) {
            JSONObject memberJson = membersArray.getJSONObject(i);
            members.add(TrelloMember.fromJson(memberJson));
        }
        return members;
    }
    
    /**
     * 获取卡片成员列表
     * 
     * @param cardId 卡片ID
     * @return 成员列表
     * @throws TrelloException Trello异常
     */
    public List<TrelloMember> getCardMembers(String cardId) throws TrelloException {
        JSONArray membersArray = getArray(String.format("/cards/%s/members", cardId));
        
        List<TrelloMember> members = new ArrayList<>();
        for (int i = 0; i < membersArray.size(); i++) {
            JSONObject memberJson = membersArray.getJSONObject(i);
            members.add(TrelloMember.fromJson(memberJson));
        }
        return members;
    }
}
