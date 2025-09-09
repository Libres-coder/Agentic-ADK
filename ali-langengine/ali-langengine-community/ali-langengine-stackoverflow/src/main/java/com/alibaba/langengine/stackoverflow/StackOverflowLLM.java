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
package com.alibaba.langengine.stackoverflow;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseLLM;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.stackoverflow.model.StackOverflowSearchRequest;
import com.alibaba.langengine.stackoverflow.model.StackOverflowSearchResult;
import com.alibaba.langengine.stackoverflow.service.StackOverflowApiService;
import com.alibaba.langengine.stackoverflow.service.impl.StackOverflowApiServiceImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.stackoverflow.StackOverflowConfiguration.*;


@Data
@EqualsAndHashCode(callSuper = false)
public class StackOverflowLLM extends BaseLLM<ChatCompletionRequest> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StackOverflowLLM.class);
    
    private StackOverflowApiService apiService;
    
    /**
     * 搜索站点 (stackoverflow, superuser, serverfault等)
     */
    private String site = getDefaultSite();
    
    /**
     * 最大结果数量
     */
    private Integer maxResults;
    
    /**
     * 排序方式 (votes, activity, creation, relevance)
     */
    private String sortOrder;
    
    /**
     * 最低分数过滤
     */
    private Integer minScore;
    
    /**
     * 只显示已回答的问题
     */
    private Boolean answeredOnly = false;
    
    /**
     * 只显示有已接受答案的问题
     */
    private Boolean acceptedAnswerOnly = false;
    
    /**
     * 是否包含问题详情
     */
    private Boolean includeBody = true;
    
    /**
     * 是否包含答案
     */
    private Boolean includeAnswers = true;
    
    /**
     * 标签过滤
     */
    private List<String> tags;
    
    /**
     * 自定义搜索范围
     */
    private String customSite;
    
    public StackOverflowLLM() {
        this.apiService = new StackOverflowApiServiceImpl();
        initializeConfiguration();
    }
    
    public StackOverflowLLM(StackOverflowApiService apiService) {
        this.apiService = apiService;
        initializeConfiguration();
    }
    
    /**
     * 初始化配置参数
     */
    private void initializeConfiguration() {
        this.maxResults = Integer.parseInt(StackOverflowConfiguration.getMaxResults());
        this.sortOrder = StackOverflowConfiguration.getSortOrder();
        this.minScore = Integer.parseInt(StackOverflowConfiguration.getMinScore());
    }
    
    public void setApiService(StackOverflowApiService apiService) {
        this.apiService = apiService;
    }
    
    @Override
    public String run(String prompt, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        try {
            log.info("Stack Overflow搜索开始，查询: {}", prompt);
            
            // 解析查询参数
            StackOverflowSearchRequest searchRequest = parseSearchQuery(prompt, extraAttributes);
            
            // 执行搜索
            List<StackOverflowSearchResult> results = apiService.searchQuestions(searchRequest);
            
            // 格式化结果
            String formattedResults = formatSearchResults(results, searchRequest.getQuery());
            
            log.info("Stack Overflow搜索完成，返回{}个结果", results.size());
            
            if (consumer != null) {
                consumer.accept(formattedResults);
            }
            
            return formattedResults;
            
        } catch (IllegalArgumentException e) {
            log.error("Stack Overflow搜索参数错误: {}", e.getMessage(), e);
            String errorMessage = "搜索参数错误: " + e.getMessage();
            
            if (consumer != null) {
                consumer.accept(errorMessage);
            }
            
            return errorMessage;
            
        } catch (java.net.SocketTimeoutException e) {
            log.error("Stack Overflow搜索超时: {}", e.getMessage(), e);
            String errorMessage = "搜索请求超时，请稍后重试";
            
            if (consumer != null) {
                consumer.accept(errorMessage);
            }
            
            return errorMessage;
            
        } catch (java.net.ConnectException e) {
            log.error("Stack Overflow连接失败: {}", e.getMessage(), e);
            String errorMessage = "无法连接到Stack Overflow服务，请检查网络连接";
            
            if (consumer != null) {
                consumer.accept(errorMessage);
            }
            
            return errorMessage;
            
        } catch (Exception e) {
            log.error("Stack Overflow搜索失败: {}", e.getMessage(), e);
            String errorMessage = "Stack Overflow搜索失败: " + e.getMessage();
            
            if (consumer != null) {
                consumer.accept(errorMessage);
            }
            
            return errorMessage;
        }
    }
    
    /**
     * 解析搜索查询，支持多种查询格式
     */
    private StackOverflowSearchRequest parseSearchQuery(String prompt, Map<String, Object> extraAttributes) {
        StackOverflowSearchRequest.StackOverflowSearchRequestBuilder builder = StackOverflowSearchRequest.builder();
        
        // 基本查询
        String query = prompt;
        
        // 从extraAttributes中提取参数
        String useSite = customSite != null ? customSite : site;
        Integer useMaxResults = maxResults;
        String useSortOrder = sortOrder;
        Integer useMinScore = minScore;
        Boolean useAnsweredOnly = answeredOnly;
        Boolean useAcceptedAnswerOnly = acceptedAnswerOnly;
        List<String> baseTags = new ArrayList<>();
        
        if (extraAttributes != null) {
            Object tagsObj = extraAttributes.get("tags");
            if (tagsObj instanceof List) {
                baseTags.addAll((List<String>) tagsObj);
            } else if (tagsObj instanceof String) {
                String tagsStr = (String) tagsObj;
                if (StringUtils.isNotBlank(tagsStr)) {
                    baseTags.addAll(Arrays.asList(tagsStr.split(",")));
                }
            }
            
            Object siteObj = extraAttributes.get("site");
            if (siteObj instanceof String && StringUtils.isNotBlank((String) siteObj)) {
                useSite = (String) siteObj;
            }
            
            Object maxResultsObj = extraAttributes.get("maxResults");
            if (maxResultsObj instanceof Integer) {
                useMaxResults = (Integer) maxResultsObj;
            }
            
            Object sortObj = extraAttributes.get("sort");
            if (sortObj instanceof String && StringUtils.isNotBlank((String) sortObj)) {
                useSortOrder = (String) sortObj;
            }
            
            Object minScoreObj = extraAttributes.get("minScore");
            if (minScoreObj instanceof Integer) {
                useMinScore = (Integer) minScoreObj;
            }
            
            Object answeredOnlyObj = extraAttributes.get("answeredOnly");
            if (answeredOnlyObj instanceof Boolean) {
                useAnsweredOnly = (Boolean) answeredOnlyObj;
            }
            
            Object acceptedAnswerOnlyObj = extraAttributes.get("acceptedAnswerOnly");
            if (acceptedAnswerOnlyObj instanceof Boolean) {
                useAcceptedAnswerOnly = (Boolean) acceptedAnswerOnlyObj;
            }
        }
        
        // 解析查询文本中的标签 [tag1] [tag2]
        if (query.contains("[") && query.contains("]")) {
            List<String> extractedTags = extractTagsFromQuery(query);
            if (!extractedTags.isEmpty()) {
                // 合并标签而不是覆盖
                baseTags.addAll(extractedTags);
                // 清理查询文本中的标签
                query = query.replaceAll("\\[\\w+(-\\w+)*\\]", "").trim().replaceAll("\\s+", " ");
            }
        }
        
        // 设置合并后的标签
        if (!baseTags.isEmpty()) {
            builder.tags(baseTags);
        }
        
        return builder
                .query(query)
                .site(useSite)
                .pageSize(useMaxResults)
                .sort(useSortOrder)
                .order("desc")
                .minScore(useMinScore)
                .answeredOnly(useAnsweredOnly)
                .acceptedAnswerOnly(acceptedAnswerOnly)
                .includeBody(includeBody)
                .includeAnswers(includeAnswers)
                .build();
    }
    
    /**
     * 从查询文本中提取标签
     */
    private List<String> extractTagsFromQuery(String query) {
        return Arrays.stream(query.split("\\s+"))
                .filter(word -> word.startsWith("[") && word.endsWith("]"))
                .map(tag -> tag.substring(1, tag.length() - 1))
                .collect(Collectors.toList());
    }
    
    /**
     * 格式化搜索结果
     */
    private String formatSearchResults(List<StackOverflowSearchResult> results, String query) {
        if (results == null || results.isEmpty()) {
            return "未找到相关的Stack Overflow问答。请尝试使用不同的关键词或标签。";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Stack Overflow技术问答搜索结果 ===\\n\\n");
        sb.append(String.format("查询: %s\\n", query));
        sb.append(String.format("找到 %d 个相关问题\\n\\n", results.size()));
        
        for (int i = 0; i < results.size(); i++) {
            StackOverflowSearchResult result = results.get(i);
            sb.append(String.format("【问题 %d】\\n", i + 1));
            sb.append(String.format("标题: %s\\n", result.getTitle()));
            sb.append(String.format("链接: %s\\n", result.getLink()));
            
            if (result.getScore() != null) {
                sb.append(String.format("评分: %d", result.getScore()));
            }
            
            if (result.getAnswerCount() != null) {
                sb.append(String.format(" | 答案数: %d", result.getAnswerCount()));
            }
            
            if (result.getViewCount() != null) {
                sb.append(String.format(" | 浏览数: %d", result.getViewCount()));
            }
            
            if (Boolean.TRUE.equals(result.getIsAnswered())) {
                sb.append(" | ✓已回答");
            }
            
            if (result.getAcceptedAnswerId() != null) {
                sb.append(" | ✓已采纳");
            }
            
            sb.append("\\n");
            
            if (result.getTags() != null && !result.getTags().isEmpty()) {
                sb.append("标签: ");
                sb.append(result.getTags().stream()
                        .map(tag -> "[" + tag + "]")
                        .collect(Collectors.joining(" ")));
                sb.append("\\n");
            }
            
            if (StringUtils.isNotBlank(result.getBody())) {
                String body = result.getBody();
                if (body.length() > 200) {
                    body = body.substring(0, 200) + "...";
                }
                sb.append(String.format("摘要: %s\\n", body));
            }
            
            if (result.getBestAnswer() != null && StringUtils.isNotBlank(result.getBestAnswer().getBody())) {
                String answer = result.getBestAnswer().getBody();
                if (answer.length() > 300) {
                    answer = answer.substring(0, 300) + "...";
                }
                sb.append(String.format("最佳答案: %s\\n", answer));
            }
            
            sb.append("\\n");
        }
        
        // 添加使用提示
        sb.append("=== 使用提示 ===\\n");
        sb.append("• 点击链接查看完整问题和答案\\n");
        sb.append("• 优先查看评分高和已采纳答案的问题\\n");
        sb.append("• 可以使用标签如[java][python]来精确搜索\\n");
        sb.append("• 当前API配额剩余: ").append(apiService.getRemainingQuota()).append("\\n");
        
        return sb.toString();
    }
    
    /**
     * 设置搜索标签
     */
    public StackOverflowLLM withTags(String... tags) {
        this.tags = Arrays.asList(tags);
        return this;
    }
    
    /**
     * 设置搜索站点
     */
    public StackOverflowLLM withSite(String site) {
        this.customSite = site;
        return this;
    }
    
    /**
     * 设置最大结果数
     */
    public StackOverflowLLM withMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }
    
    /**
     * 设置排序方式
     */
    public StackOverflowLLM withSort(String sort) {
        this.sortOrder = sort;
        return this;
    }
    
    /**
     * 设置最低分数过滤
     */
    public StackOverflowLLM withMinScore(int minScore) {
        this.minScore = minScore;
        return this;
    }
    
    /**
     * 只显示已回答的问题
     */
    public StackOverflowLLM onlyAnswered() {
        this.answeredOnly = true;
        return this;
    }
    
    /**
     * 只显示有已接受答案的问题
     */
    public StackOverflowLLM onlyAccepted() {
        this.acceptedAnswerOnly = true;
        return this;
    }
    
    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        // 为Stack Overflow搜索创建一个简单的ChatCompletionRequest
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setMessages(chatMessages);
        if (stops != null) {
            request.setStop(stops);
        }
        // 设置其他默认参数
        request.setModel("stackoverflow-search");
        request.setMaxTokens(getMaxTokens() != null ? getMaxTokens() : 1000);
        request.setTemperature(0.0); // 搜索不需要随机性
        
        return request;
    }
    
    @Override
    public String runRequest(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        // 从ChatCompletionRequest中提取查询并执行搜索
        String query = extractQueryFromRequest(request);
        return run(query, stops, consumer, extraAttributes);
    }
    
    @Override
    public String runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        // Stack Overflow搜索不支持流式请求，直接调用普通搜索方法
        return runRequest(request, stops, consumer, extraAttributes);
    }
    
    /**
     * 从ChatCompletionRequest中提取查询字符串
     */
    private String extractQueryFromRequest(ChatCompletionRequest request) {
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            return "";
        }
        
        // 获取最后一条用户消息作为查询
        return request.getMessages().stream()
                .filter(message -> "user".equals(message.getRole()))
                .reduce((first, second) -> second)
                .map(message -> message.getContent() != null ? message.getContent().toString() : "")
                .orElse("");
    }
    
    /**
     * 获取自定义站点（用于测试）
     */
    public String getCustomSite() {
        return this.customSite;
    }
}
