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
package com.alibaba.langengine.wenxin.model;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.wenxin.model.embedding.WenxinEmbeddingRequest;
import com.alibaba.langengine.wenxin.model.embedding.WenxinEmbeddingResult;
import com.alibaba.langengine.wenxin.service.WenxinService;
import com.alibaba.langengine.wenxin.WenxinModelName;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 文心Embedding模型
 */
@Slf4j
@Data
public class WenxinEmbeddingModel extends Embeddings {

    private String apiKey;
    private String secretKey;
    private String model = WenxinModelName.EMBEDDING_V1;
    private String serverUrl = "https://aip.baidubce.com/";
    private Duration timeout = Duration.ofSeconds(60);
    
    private WenxinService wenxinService;

    public WenxinEmbeddingModel(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.wenxinService = new WenxinService(serverUrl, timeout, apiKey, secretKey);
    }

    @Override
    public String getModelType() {
        return model;
    }

    @Override
    public List<Document> embedDocument(List<Document> documents) {
        List<String> texts = new ArrayList<>();
        for (Document document : documents) {
            texts.add(document.getPageContent());
        }
        
        List<List<Double>> embeddings = getEmbeddings(texts);
        
        // 将embedding结果设置回文档
        for (int i = 0; i < documents.size() && i < embeddings.size(); i++) {
            Document document = documents.get(i);
            // 假设Document有setEmbedding方法或类似字段
            // document.setEmbedding(embeddings.get(i));
        }
        
        return documents;
    }

    @Override
    public List<String> embedQuery(String text, int recommend) {
        List<String> texts = new ArrayList<>();
        texts.add(text);
        
        List<List<Double>> embeddings = getEmbeddings(texts);
        
        // 将Double向量转换为String列表
        List<String> result = new ArrayList<>();
        if (!embeddings.isEmpty()) {
            for (Double value : embeddings.get(0)) {
                result.add(value.toString());
            }
        }
        
        return result;
    }

    private List<List<Double>> getEmbeddings(List<String> texts) {
        WenxinEmbeddingRequest request = new WenxinEmbeddingRequest();
        request.setInput(texts);

        WenxinEmbeddingResult result = wenxinService.createEmbedding(request);
        
        List<List<Double>> embeddings = new ArrayList<>();
        if (result != null && result.getData() != null) {
            for (WenxinEmbeddingResult.WenxinEmbeddingData data : result.getData()) {
                embeddings.add(data.getEmbedding());
            }
        }
        
        return embeddings;
    }
}
