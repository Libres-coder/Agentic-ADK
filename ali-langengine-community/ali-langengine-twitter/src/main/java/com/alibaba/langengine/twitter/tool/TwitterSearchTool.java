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
package com.alibaba.langengine.twitter.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.twitter.sdk.TwitterClient;
import com.alibaba.langengine.twitter.sdk.TwitterException;
import com.alibaba.langengine.twitter.sdk.TweetSearchResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class TwitterSearchTool extends BaseTool {

    private TwitterClient twitterClient;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"query\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"搜索查询关键词，支持hashtag、@用户名等\"\n" +
            "\t\t},\n" +
            "\t\t\"maxResults\": {\n" +
            "\t\t\t\"type\": \"integer\",\n" +
            "\t\t\t\"description\": \"最大返回结果数，默认10，最大100\",\n" +
            "\t\t\t\"default\": 10\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"query\"]\n" +
            "}";

    public TwitterSearchTool() {
        setName("twitter_search");
        setHumanName("Twitter搜索工具");
        setDescription("搜索Twitter/X上的推文内容，支持关键词、hashtag、@用户名等搜索");
        setParameters(PARAMETERS);
        this.twitterClient = new TwitterClient();
    }

    public TwitterSearchTool(TwitterClient twitterClient) {
        setName("twitter_search");
        setHumanName("Twitter搜索工具");
        setDescription("搜索Twitter/X上的推文内容，支持关键词、hashtag、@用户名等搜索");
        setParameters(PARAMETERS);
        this.twitterClient = twitterClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("TwitterSearchTool toolInput:" + toolInput);

        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String query = (String) toolInputMap.get("query");
            Integer maxResults = (Integer) toolInputMap.getOrDefault("maxResults", 10);

            if (query == null || query.trim().isEmpty()) {
                return new ToolExecuteResult("错误：搜索查询不能为空");
            }

            TweetSearchResponse response = twitterClient.searchTweets(query, maxResults);
            
            if (response.getData() == null || response.getData().isEmpty()) {
                return new ToolExecuteResult("未找到相关推文");
            }

            StringBuilder result = new StringBuilder();
            result.append("找到 ").append(response.getData().size()).append(" 条推文：\n\n");
            
            for (TweetSearchResponse.Tweet tweet : response.getData()) {
                result.append("推文ID: ").append(tweet.getId()).append("\n");
                result.append("内容: ").append(tweet.getText()).append("\n");
                result.append("发布时间: ").append(tweet.getCreatedAt()).append("\n");
                if (tweet.getPublicMetrics() != null) {
                    result.append("互动数据: 转发(").append(tweet.getPublicMetrics().getRetweetCount())
                          .append(") 点赞(").append(tweet.getPublicMetrics().getLikeCount())
                          .append(") 回复(").append(tweet.getPublicMetrics().getReplyCount()).append(")\n");
                }
                result.append("---\n");
            }

            return new ToolExecuteResult(result.toString());

        } catch (TwitterException e) {
            log.error("Twitter API调用失败", e);
            return new ToolExecuteResult("Twitter API调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Twitter搜索工具执行失败", e);
            return new ToolExecuteResult("搜索失败: " + e.getMessage());
        }
    }

    public TwitterClient getTwitterClient() {
        return twitterClient;
    }

    public void setTwitterClient(TwitterClient twitterClient) {
        this.twitterClient = twitterClient;
    }
}