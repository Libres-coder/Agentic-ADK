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
package com.alibaba.langengine.youtube.examples;

import com.alibaba.langengine.youtube.YouTubeSearchTool;
import com.alibaba.langengine.youtube.model.YouTubeSearchRequest;
import com.alibaba.langengine.youtube.model.YouTubeSearchResponse;
import com.alibaba.langengine.youtube.sdk.YouTubeClient;
import com.alibaba.langengine.youtube.sdk.YouTubeException;

/**
 * YouTube 搜索工具使用示例
 */
public class YouTubeSearchExample {
    
    public static void main(String[] args) {
        // 创建搜索工具
        YouTubeSearchTool tool = new YouTubeSearchTool();
        
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
    private static void basicSearchExample(YouTubeSearchTool tool) {
        try {
            String toolInput = "{\n" +
                    "  \"query\": \"machine learning tutorial\",\n" +
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
    private static void advancedSearchExample(YouTubeSearchTool tool) {
        try {
            String toolInput = "{\n" +
                    "  \"query\": \"python programming\",\n" +
                    "  \"count\": 3,\n" +
                    "  \"sortBy\": \"viewCount\",\n" +
                    "  \"timeRange\": \"month\",\n" +
                    "  \"videoType\": \"video\",\n" +
                    "  \"duration\": \"medium\",\n" +
                    "  \"quality\": \"hd\",\n" +
                    "  \"language\": \"en\",\n" +
                    "  \"country\": \"US\",\n" +
                    "  \"includeSubtitles\": true,\n" +
                    "  \"includeHD\": true\n" +
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
            YouTubeClient client = new YouTubeClient();
            
            // 创建搜索请求
            YouTubeSearchRequest request = new YouTubeSearchRequest("machine learning")
                    .setCount(3)
                    .setSortBy("viewCount")
                    .setTimeRange("week")
                    .setVideoType("video")
                    .setDuration("medium")
                    .setQuality("hd");
            
            // 执行搜索
            YouTubeSearchResponse response = client.search(request);
            
            // 输出结果
            System.out.println("搜索查询: " + response.getQuery());
            System.out.println("结果摘要: " + response.getSummary());
            System.out.println("搜索结果:");
            System.out.println(response.getFormattedResults());
            
        } catch (YouTubeException e) {
            System.err.println("搜索失败: " + e.getMessage());
        }
    }
}
