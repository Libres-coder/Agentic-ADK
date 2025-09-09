package com.alibaba.langengine.zhihu.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.zhihu.model.ZhihuQuestion;
import com.alibaba.langengine.zhihu.model.ZhihuSearchResponse;
import com.alibaba.langengine.zhihu.sdk.ZhihuClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 知乎搜索工具
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class ZhihuSearchTool extends DefaultTool {
    
    private ZhihuClient zhihuClient;
    
    public ZhihuSearchTool() {
        this.zhihuClient = new ZhihuClient();
        init();
    }
    
    private void init() {
        setName("ZhihuSearchTool");
        setDescription("知乎搜索工具，可以搜索知乎上的问题和回答。输入参数：query(搜索关键词), limit(返回数量，默认10)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"搜索关键词\"\n" +
                "    },\n" +
                "    \"limit\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"返回结果数量，默认10\",\n" +
                "      \"default\": 10\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"query\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("知乎搜索工具输入: {}", toolInput);
        
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String query = (String) inputMap.get("query");
            Integer limit = (Integer) inputMap.getOrDefault("limit", 10);
            
            if (StringUtils.isBlank(query)) {
                return new ToolExecuteResult("错误：搜索关键词不能为空");
            }
            
            if (limit == null || limit <= 0) {
                limit = 10;
            }
            
            ZhihuSearchResponse response = zhihuClient.searchQuestions(query, limit);
            
            if (response.getData() == null || response.getData().isEmpty()) {
                return new ToolExecuteResult("未找到相关问题");
            }
            
            StringBuilder result = new StringBuilder();
            result.append("找到 ").append(response.getData().size()).append(" 个相关问题：\n\n");
            
            for (int i = 0; i < response.getData().size(); i++) {
                ZhihuQuestion question = response.getData().get(i);
                result.append("问题 ").append(i + 1).append("：\n");
                result.append("标题：").append(question.getTitle()).append("\n");
                result.append("问题ID：").append(question.getId()).append("\n");
                result.append("回答数：").append(question.getAnswerCount()).append("\n");
                result.append("关注数：").append(question.getFollowerCount()).append("\n");
                result.append("浏览数：").append(question.getViewCount()).append("\n");
                result.append("链接：").append(question.getUrl()).append("\n");
                if (StringUtils.isNotBlank(question.getContent())) {
                    result.append("内容：").append(question.getContent().substring(0, Math.min(200, question.getContent().length()))).append("...\n");
                }
                result.append("---\n");
            }
            
            return new ToolExecuteResult(result.toString());
            
        } catch (Exception e) {
            log.error("知乎搜索失败", e);
            return new ToolExecuteResult("搜索失败: " + e.getMessage());
        }
    }
}