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
package com.alibaba.langengine.weibo.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.weibo.sdk.WeiboClient;
import com.alibaba.langengine.weibo.sdk.WeiboException;
import com.alibaba.langengine.weibo.sdk.WeiboSearchResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class WeiboSearchTool extends BaseTool {

    private WeiboClient weiboClient;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"keyword\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"搜索关键词\"\n" +
            "\t\t},\n" +
            "\t\t\"maxResults\": {\n" +
            "\t\t\t\"type\": \"integer\",\n" +
            "\t\t\t\"description\": \"最大返回结果数，默认10，最大50\",\n" +
            "\t\t\t\"default\": 10\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"keyword\"]\n" +
            "}";

    public WeiboSearchTool() {
        setName("weibo_search");
        setHumanName("微博搜索工具");
        setDescription("搜索微博内容");
        setParameters(PARAMETERS);
        this.weiboClient = new WeiboClient();
    }

    public WeiboSearchTool(WeiboClient weiboClient) {
        setName("weibo_search");
        setHumanName("微博搜索工具");
        setDescription("搜索微博内容");
        setParameters(PARAMETERS);
        this.weiboClient = weiboClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("WeiboSearchTool toolInput:" + toolInput);

        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String keyword = (String) toolInputMap.get("keyword");
            Integer maxResults = (Integer) toolInputMap.getOrDefault("maxResults", 10);

            if (keyword == null || keyword.trim().isEmpty()) {
                return new ToolExecuteResult("错误：搜索关键词不能为空");
            }

            WeiboSearchResponse response = weiboClient.searchWeibo(keyword, maxResults);
            
            if (response.getStatuses() == null || response.getStatuses().isEmpty()) {
                return new ToolExecuteResult("未找到相关微博");
            }

            StringBuilder result = new StringBuilder();
            result.append("找到 ").append(response.getStatuses().size()).append(" 条微博：\n\n");
            
            for (WeiboSearchResponse.Status status : response.getStatuses()) {
                result.append("微博ID: ").append(status.getId()).append("\n");
                result.append("内容: ").append(status.getText()).append("\n");
                result.append("发布时间: ").append(status.getCreatedAt()).append("\n");
                result.append("来源: ").append(status.getSource()).append("\n");
                if (status.getUser() != null) {
                    result.append("用户: @").append(status.getUser().getScreenName()).append("\n");
                }
                result.append("互动数据: 转发(").append(status.getRepostsCount())
                      .append(") 评论(").append(status.getCommentsCount())
                      .append(") 点赞(").append(status.getAttitudesCount()).append(")\n");
                result.append("---\n");
            }

            return new ToolExecuteResult(result.toString());

        } catch (WeiboException e) {
            log.error("微博 API调用失败", e);
            return new ToolExecuteResult("微博 API调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("微博搜索工具执行失败", e);
            return new ToolExecuteResult("搜索失败: " + e.getMessage());
        }
    }

    public WeiboClient getWeiboClient() {
        return weiboClient;
    }

    public void setWeiboClient(WeiboClient weiboClient) {
        this.weiboClient = weiboClient;
    }
}