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
package com.alibaba.langengine.googlescholar.examples;

import com.alibaba.langengine.googlescholar.GoogleScholarSearchTool;
import com.alibaba.langengine.googlescholar.model.GoogleScholarSearchRequest;
import com.alibaba.langengine.googlescholar.model.GoogleScholarSearchResponse;
import com.alibaba.langengine.googlescholar.sdk.GoogleScholarClient;
import com.alibaba.langengine.googlescholar.sdk.GoogleScholarException;

/**
 * Google Scholar 搜索工具使用示例
 */
public class GoogleScholarSearchExample {
    
    public static void main(String[] args) {
        // 创建搜索工具
        GoogleScholarSearchTool tool = new GoogleScholarSearchTool();
        
        // 示例1: 基本搜索
        System.out.println("=== 示例1: 基本搜索 ===");
        basicSearchExample(tool);
        
        // 示例2: 高级搜索
        System.out.println("\n=== 示例2: 高级搜索 ===");
        advancedSearchExample(tool);
        
        // 示例3: 直接使用客户端
        System.out.println("\n=== 示例3: 直接使用客户端 ===");
        directClientExample();
    }
    
    /**
     * 基本搜索示例
     */
    private static void basicSearchExample(GoogleScholarSearchTool tool) {
        try {
            String toolInput = "{\n" +
                    "  \"query\": \"machine learning\",\n" +
                    "  \"count\": 5\n" +
                    "}";
            
            var result = tool.run(toolInput, null);
            System.out.println(result.getResult());
            
        } catch (Exception e) {
            System.err.println("搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 高级搜索示例
     */
    private static void advancedSearchExample(GoogleScholarSearchTool tool) {
        try {
            String toolInput = "{\n" +
                    "  \"query\": \"artificial intelligence\",\n" +
                    "  \"count\": 3,\n" +
                    "  \"sortBy\": \"date\",\n" +
                    "  \"timeRange\": \"y\",\n" +
                    "  \"documentType\": \"articles\",\n" +
                    "  \"author\": \"Geoffrey Hinton\",\n" +
                    "  \"journal\": \"Nature\",\n" +
                    "  \"language\": \"en\",\n" +
                    "  \"country\": \"us\"\n" +
                    "}";
            
            var result = tool.run(toolInput, null);
            System.out.println(result.getResult());
            
        } catch (Exception e) {
            System.err.println("搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 直接使用客户端示例
     */
    private static void directClientExample() {
        try {
            GoogleScholarClient client = new GoogleScholarClient();
            
            // 创建搜索请求
            GoogleScholarSearchRequest request = new GoogleScholarSearchRequest("deep learning")
                    .setCount(3)
                    .setSortBy("citations")
                    .setTimeRange("5y")
                    .setDocumentType("articles");
            
            // 执行搜索
            GoogleScholarSearchResponse response = client.search(request);
            
            // 输出结果
            System.out.println("搜索查询: " + response.getQuery());
            System.out.println("结果摘要: " + response.getSummary());
            System.out.println("搜索结果:");
            System.out.println(response.getFormattedResults());
            
        } catch (GoogleScholarException e) {
            System.err.println("搜索失败: " + e.getMessage());
        }
    }
}
