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
package com.alibaba.langengine.clickhouse.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClickHouseDemo {

    public static void main(String[] args) {
        try {
            // 创建ClickHouse向量存储实例
            ClickHouse clickHouse = new ClickHouse("test_db");
            clickHouse.setEmbedding(new FakeEmbeddings());

            // 初始化向量存储
            clickHouse.init();

            // 准备测试文档
            List<Document> documents = Arrays.asList(
                createDocument("doc1", "ClickHouse是一个高性能的列式数据库管理系统", "database", "clickhouse"),
                createDocument("doc2", "向量数据库支持高效的相似性搜索和检索", "database", "vector"),
                createDocument("doc3", "机器学习模型可以生成文本的向量表示", "ai", "machine_learning"),
                createDocument("doc4", "自然语言处理技术在搜索引擎中广泛应用", "ai", "nlp"),
                createDocument("doc5", "大数据分析需要高性能的存储和计算系统", "bigdata", "analytics")
            );

            System.out.println("=== 添加文档 ===");
            clickHouse.addDocuments(documents);
            System.out.println("成功添加 " + documents.size() + " 个文档");
            System.out.println("当前文档总数: " + clickHouse.getDocumentCount());

            // 执行相似性搜索
            System.out.println("\n=== 相似性搜索 ===");
            
            // 搜索与数据库相关的文档
            System.out.println("搜索: '数据库系统'");
            List<Document> results1 = clickHouse.similaritySearch("数据库系统", 3);
            printSearchResults(results1);

            // 搜索与AI相关的文档
            System.out.println("\n搜索: '人工智能技术'");
            List<Document> results2 = clickHouse.similaritySearch("人工智能技术", 3);
            printSearchResults(results2);

            // 搜索与向量相关的文档
            System.out.println("\n搜索: '向量搜索'");
            List<Document> results3 = clickHouse.similaritySearch("向量搜索", 2);
            printSearchResults(results3);

            // 带距离过滤的搜索
            System.out.println("\n搜索: '高性能计算' (最大距离: 0.8)");
            List<Document> results4 = clickHouse.similaritySearch("高性能计算", 5, 0.8, null);
            printSearchResults(results4);

            // 健康检查
            System.out.println("\n=== 健康检查 ===");
            System.out.println("ClickHouse健康状态: " + (clickHouse.isHealthy() ? "正常" : "异常"));

            // 清理资源
            clickHouse.close();
            System.out.println("\n演示完成！");

        } catch (Exception e) {
            System.err.println("演示过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建测试文档
     */
    private static Document createDocument(String id, String content, String category, String tag) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", category);
        metadata.put("tag", tag);
        metadata.put("timestamp", System.currentTimeMillis());
        
        Document document = new Document(content, metadata);
        document.setUniqueId(id);
        return document;
    }

    /**
     * 打印搜索结果
     */
    private static void printSearchResults(List<Document> results) {
        if (results.isEmpty()) {
            System.out.println("  未找到相关文档");
            return;
        }
        
        for (int i = 0; i < results.size(); i++) {
            Document doc = results.get(i);
            System.out.printf("  %d. [ID: %s] [分数: %.4f] %s%n", 
                i + 1, 
                doc.getUniqueId(), 
                doc.getScore() != null ? doc.getScore() : 0.0,
                doc.getPageContent()
            );
            
            if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
                System.out.println("     元数据: " + doc.getMetadata());
            }
        }
    }

    /**
     * 演示自定义参数配置
     */
    public static void customConfigDemo() {
        // 创建自定义参数
        ClickHouseParam customParam = new ClickHouseParam();
        customParam.setTableName("custom_vector_table");
        customParam.setBatchSize(500);
        customParam.setConnectionTimeout(60000);
        
        // 自定义初始化参数
        ClickHouseParam.InitParam initParam = customParam.getInitParam();
        initParam.setVectorDimensions(768);
        initParam.setSimilarityFunction(ClickHouseSimilarityFunction.L2);
        initParam.setEngineType("ReplacingMergeTree");
        initParam.setOrderBy("content_id, timestamp");
        
        // 使用自定义参数创建ClickHouse实例
        ClickHouse customClickHouse = new ClickHouse("custom_db", customParam);
        customClickHouse.setEmbedding(new FakeEmbeddings());
        
        System.out.println("使用自定义参数创建ClickHouse向量存储");
        System.out.println("表名: " + customParam.getTableName());
        System.out.println("批量大小: " + customParam.getBatchSize());
        System.out.println("向量维度: " + initParam.getVectorDimensions());
        System.out.println("相似性函数: " + initParam.getSimilarityFunction());
        System.out.println("引擎类型: " + initParam.getEngineType());
    }
}
