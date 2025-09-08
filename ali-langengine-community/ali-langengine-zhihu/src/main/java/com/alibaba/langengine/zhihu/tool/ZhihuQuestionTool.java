package com.alibaba.langengine.zhihu.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.zhihu.model.ZhihuAnswer;
import com.alibaba.langengine.zhihu.model.ZhihuAnswerResponse;
import com.alibaba.langengine.zhihu.model.ZhihuQuestion;
import com.alibaba.langengine.zhihu.sdk.ZhihuClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 知乎问题详情工具
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class ZhihuQuestionTool extends DefaultTool {
    
    private ZhihuClient zhihuClient;
    
    public ZhihuQuestionTool() {
        this.zhihuClient = new ZhihuClient();
        init();
    }
    
    private void init() {
        setName("ZhihuQuestionTool");
        setDescription("知乎问题详情工具，可以获取知乎问题的详细信息和回答。输入参数：questionId(问题ID), includeAnswers(是否包含回答，默认true), answerLimit(回答数量限制，默认5)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"questionId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"知乎问题ID\"\n" +
                "    },\n" +
                "    \"includeAnswers\": {\n" +
                "      \"type\": \"boolean\",\n" +
                "      \"description\": \"是否包含回答，默认true\",\n" +
                "      \"default\": true\n" +
                "    },\n" +
                "    \"answerLimit\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"回答数量限制，默认5\",\n" +
                "      \"default\": 5\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"questionId\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("知乎问题详情工具输入: {}", toolInput);
        
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String questionId = (String) inputMap.get("questionId");
            Boolean includeAnswers = (Boolean) inputMap.getOrDefault("includeAnswers", true);
            Integer answerLimit = (Integer) inputMap.getOrDefault("answerLimit", 5);
            
            if (StringUtils.isBlank(questionId)) {
                return new ToolExecuteResult("错误：问题ID不能为空");
            }
            
            if (answerLimit == null || answerLimit <= 0) {
                answerLimit = 5;
            }
            
            // 获取问题详情
            ZhihuQuestion question = zhihuClient.getQuestion(questionId);
            
            StringBuilder result = new StringBuilder();
            result.append("问题详情：\n");
            result.append("标题：").append(question.getTitle()).append("\n");
            result.append("问题ID：").append(question.getId()).append("\n");
            result.append("回答数：").append(question.getAnswerCount()).append("\n");
            result.append("关注数：").append(question.getFollowerCount()).append("\n");
            result.append("浏览数：").append(question.getViewCount()).append("\n");
            result.append("创建时间：").append(question.getCreatedTime()).append("\n");
            result.append("更新时间：").append(question.getUpdatedTime()).append("\n");
            result.append("链接：").append(question.getUrl()).append("\n");
            
            if (StringUtils.isNotBlank(question.getContent())) {
                result.append("问题内容：").append(question.getContent()).append("\n");
            }
            
            if (question.getAuthor() != null) {
                result.append("提问者：").append(question.getAuthor().getName()).append("\n");
            }
            
            if (question.getTopics() != null && !question.getTopics().isEmpty()) {
                result.append("话题标签：").append(String.join(", ", question.getTopics())).append("\n");
            }
            
            // 获取回答
            if (includeAnswers) {
                result.append("\n--- 回答列表 ---\n");
                ZhihuAnswerResponse answerResponse = zhihuClient.getQuestionAnswers(questionId, answerLimit);
                
                if (answerResponse.getData() != null && !answerResponse.getData().isEmpty()) {
                    for (int i = 0; i < answerResponse.getData().size(); i++) {
                        ZhihuAnswer answer = answerResponse.getData().get(i);
                        result.append("\n回答 ").append(i + 1).append("：\n");
                        result.append("作者：").append(answer.getAuthor().getName()).append("\n");
                        result.append("点赞数：").append(answer.getVoteupCount()).append("\n");
                        result.append("评论数：").append(answer.getCommentCount()).append("\n");
                        result.append("创建时间：").append(answer.getCreatedTime()).append("\n");
                        result.append("回答内容：").append(answer.getContent()).append("\n");
                        result.append("---\n");
                    }
                } else {
                    result.append("暂无回答\n");
                }
            }
            
            return new ToolExecuteResult(result.toString());
            
        } catch (Exception e) {
            log.error("获取知乎问题详情失败", e);
            return new ToolExecuteResult("获取问题详情失败: " + e.getMessage());
        }
    }
}