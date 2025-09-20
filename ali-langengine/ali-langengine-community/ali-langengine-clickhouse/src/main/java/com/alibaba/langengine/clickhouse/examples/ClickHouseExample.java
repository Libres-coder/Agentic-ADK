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
package com.alibaba.langengine.clickhouse.examples;

import com.alibaba.langengine.clickhouse.vectorstore.ClickHouse;
import com.alibaba.langengine.clickhouse.vectorstore.ClickHouseParam;
import com.alibaba.langengine.clickhouse.vectorstore.ClickHouseSimilarityFunction;
import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;

import java.util.*;


public class ClickHouseExample {

    public static void main(String[] args) {
        System.out.println("=== ClickHouse Vector Store 使用示例 ===\n");
        
        try {
            // 示例1: 基本使用
            basicUsageExample();
            
            // 示例2: 高级配置
            advancedConfigurationExample();
            
            // 示例3: 批量操作
            batchOperationsExample();
            
            // 示例4: 相似性搜索
            similaritySearchExample();
            
        } catch (Exception e) {
            System.err.println("示例执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 基本使用示例
     */
    public static void basicUsageExample() {
        System.out.println("1. 基本使用示例");
        System.out.println("================");
        
        try {
            // 创建ClickHouse向量存储 (使用默认配置)
            ClickHouse clickHouse = new ClickHouse("example_database");
            
            // 设置嵌入模型
            clickHouse.setEmbedding(new FakeEmbeddings());
            
            // 初始化向量存储
            clickHouse.init();
            System.out.println("✓ ClickHouse向量存储初始化成功");
            
            // 创建示例文档
            List<Document> documents = Arrays.asList(
                createDocument("doc1", "人工智能是计算机科学的一个分支", "technology"),
                createDocument("doc2", "机器学习是人工智能的核心技术", "technology"),
                createDocument("doc3", "深度学习基于神经网络模型", "technology")
            );
            
            // 添加文档
            clickHouse.addDocuments(documents);
            System.out.println("✓ 成功添加 " + documents.size() + " 个文档");
            
            // 执行相似性搜索
            List<Document> results = clickHouse.similaritySearch("机器学习技术", 2);
            System.out.println("✓ 搜索结果:");
            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                System.out.println("  " + (i + 1) + ". " + doc.getPageContent() + 
                                 " (相似度: " + doc.getScore() + ")");
            }
            
            // 清理资源
            clickHouse.close();
            System.out.println("✓ 资源清理完成\n");
            
        } catch (Exception e) {
            System.err.println("✗ 基本使用示例失败: " + e.getMessage());
        }
    }

    /**
     * 高级配置示例
     */
    public static void advancedConfigurationExample() {
        System.out.println("2. 高级配置示例");
        System.out.println("================");
        
        try {
            // 创建自定义参数
            ClickHouseParam param = new ClickHouseParam();
            param.setTableName("advanced_vector_table");
            param.setBatchSize(50);
            
            // 配置初始化参数
            ClickHouseParam.InitParam initParam = param.getInitParam();
            initParam.setVectorDimensions(768);
            initParam.setSimilarityFunction(ClickHouseSimilarityFunction.L2);
            initParam.setEngineType("ReplacingMergeTree");
            
            System.out.println("✓ 自定义配置:");
            System.out.println("  - 表名: " + param.getTableName());
            System.out.println("  - 批处理大小: " + param.getBatchSize());
            System.out.println("  - 向量维度: " + initParam.getVectorDimensions());
            System.out.println("  - 相似性函数: " + initParam.getSimilarityFunction());
            System.out.println("  - 引擎类型: " + initParam.getEngineType());
            
            // 使用自定义连接参数
            ClickHouse clickHouse = new ClickHouse(
                "jdbc:clickhouse://localhost:8123/example_db",
                "default",
                "",
                "example_db",
                param
            );
            
            clickHouse.setEmbedding(new FakeEmbeddings());
            
            // 注意: 这里不实际连接，只是展示配置
            System.out.println("✓ 高级配置创建成功\n");
            
        } catch (Exception e) {
            System.err.println("✗ 高级配置示例失败: " + e.getMessage());
        }
    }

    /**
     * 批量操作示例
     */
    public static void batchOperationsExample() {
        System.out.println("3. 批量操作示例");
        System.out.println("================");
        
        try {
            // 创建大批量文档
            List<Document> largeBatch = new ArrayList<>();
            String[] topics = {"科技", "医学", "教育", "金融", "体育"};
            
            for (int i = 0; i < 100; i++) {
                String topic = topics[i % topics.length];
                largeBatch.add(createDocument(
                    "batch_doc_" + i,
                    "这是关于" + topic + "的第" + i + "个文档",
                    topic.toLowerCase()
                ));
            }
            
            System.out.println("✓ 创建了 " + largeBatch.size() + " 个批量文档");
            
            // 配置批量参数
            ClickHouseParam batchParam = new ClickHouseParam();
            batchParam.setBatchSize(25); // 每批25个文档
            
            System.out.println("✓ 批量处理配置: 每批 " + batchParam.getBatchSize() + " 个文档");
            System.out.println("✓ 总共需要 " + (largeBatch.size() / batchParam.getBatchSize()) + " 批次处理\n");
            
        } catch (Exception e) {
            System.err.println("✗ 批量操作示例失败: " + e.getMessage());
        }
    }

    /**
     * 相似性搜索示例
     */
    public static void similaritySearchExample() {
        System.out.println("4. 相似性搜索示例");
        System.out.println("==================");
        
        try {
            // 演示不同的相似性函数
            ClickHouseSimilarityFunction[] functions = {
                ClickHouseSimilarityFunction.COSINE,
                ClickHouseSimilarityFunction.L2,
                ClickHouseSimilarityFunction.L1,
                ClickHouseSimilarityFunction.DOT_PRODUCT
            };
            
            System.out.println("✓ 支持的相似性函数:");
            for (ClickHouseSimilarityFunction func : functions) {
                System.out.println("  - " + func.name() + ": " + getDescription(func));
            }
            
            // 演示搜索参数
            System.out.println("\n✓ 搜索参数示例:");
            System.out.println("  - 查询文本: '人工智能技术发展'");
            System.out.println("  - 返回数量: 5");
            System.out.println("  - 距离阈值: 0.8");
            System.out.println("  - 过滤条件: {category: 'technology'}");
            
            // 创建过滤条件
            Map<String, Object> filter = new HashMap<>();
            filter.put("category", "technology");
            
            System.out.println("✓ 过滤条件: " + filter + "\n");
            
        } catch (Exception e) {
            System.err.println("✗ 相似性搜索示例失败: " + e.getMessage());
        }
    }

    /**
     * 创建示例文档
     */
    private static Document createDocument(String id, String content, String category) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", id);
        metadata.put("category", category);
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.put("source", "example");
        
        Document document = new Document(content, metadata);
        document.setUniqueId(id);
        return document;
    }

    /**
     * 获取相似性函数描述
     */
    private static String getDescription(ClickHouseSimilarityFunction function) {
        switch (function) {
            case COSINE:
                return "余弦相似度，适用于文本向量";
            case L2:
                return "欧几里得距离，适用于数值向量";
            case L1:
                return "曼哈顿距离，适用于稀疏向量";
            case DOT_PRODUCT:
                return "点积，适用于归一化向量";
            default:
                return "未知函数";
        }
    }

    /**
     * 性能测试示例
     */
    public static void performanceTestExample() {
        System.out.println("5. 性能测试示例");
        System.out.println("================");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 模拟性能测试
            int documentCount = 1000;
            int searchCount = 100;
            
            System.out.println("✓ 性能测试参数:");
            System.out.println("  - 文档数量: " + documentCount);
            System.out.println("  - 搜索次数: " + searchCount);
            
            long endTime = System.currentTimeMillis();
            System.out.println("✓ 模拟测试完成，耗时: " + (endTime - startTime) + "ms\n");
            
        } catch (Exception e) {
            System.err.println("✗ 性能测试示例失败: " + e.getMessage());
        }
    }
}
