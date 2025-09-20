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
package com.alibaba.langengine.txtai.vectorstore.service;

import com.alibaba.langengine.txtai.exception.TxtaiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;


@Slf4j
public class TxtaiService {

    private final TxtaiClient client;
    private final ObjectMapper objectMapper;

    public TxtaiService(String serverUrl) {
        this.client = new TxtaiClient(serverUrl);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 添加文档到向量索引
     *
     * @param request 添加文档请求
     * @throws TxtaiException API调用异常
     */
    public void addDocuments(TxtaiAddRequest request) {
        try {
            log.debug("添加 {} 个文档到txtai索引", request.getDocuments().size());
            String response = client.post("/add", request);
            log.debug("添加文档成功: {}", response);
        } catch (IOException e) {
            String errorMessage = String.format("添加文档到txtai失败，文档数量: %d, 错误: %s",
                                               request.getDocuments().size(), e.getMessage());
            log.error(errorMessage, e);
            throw TxtaiException.networkError(errorMessage, e);
        }
    }

    /**
     * 搜索相似文档
     *
     * @param request 搜索请求
     * @return 搜索结果列表
     * @throws TxtaiException API调用异常
     */
    public List<TxtaiSearchResponse.SearchResult> search(TxtaiSearchRequest request) {
        try {
            log.debug("搜索查询: {} 限制: {}", request.getQuery(), request.getLimit());
            String response = client.post("/search", request);

            List<TxtaiSearchResponse.SearchResult> results = objectMapper.readValue(
                response,
                new TypeReference<List<TxtaiSearchResponse.SearchResult>>() {}
            );

            log.debug("搜索返回 {} 个结果", results.size());
            return results;
        } catch (IOException e) {
            String errorMessage = String.format("搜索txtai失败，查询: %s, 限制: %d, 错误: %s",
                                               request.getQuery(), request.getLimit(), e.getMessage());
            log.error(errorMessage, e);
            throw TxtaiException.networkError(errorMessage, e);
        }
    }

    /**
     * 重建索引
     *
     * @throws TxtaiException API调用异常
     */
    public void index() {
        try {
            log.debug("开始重建txtai索引");
            String response = client.post("/index", null);
            log.debug("重建索引成功: {}", response);
        } catch (IOException e) {
            log.error("重建txtai索引失败", e);
            throw TxtaiException.networkError("重建txtai索引失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取索引信息
     *
     * @return 索引信息JSON字符串
     * @throws TxtaiException API调用异常
     */
    public String info() {
        try {
            log.debug("获取txtai索引信息");
            String response = client.get("/info");
            log.debug("索引信息: {}", response);
            return response;
        } catch (IOException e) {
            log.error("获取txtai索引信息失败", e);
            throw TxtaiException.networkError("获取txtai索引信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 关闭服务
     */
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}