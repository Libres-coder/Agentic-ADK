package com.alibaba.langengine.zhihu.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.zhihu.model.ZhihuAuthor;
import com.alibaba.langengine.zhihu.sdk.ZhihuClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 知乎用户信息工具
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class ZhihuUserTool extends DefaultTool {
    
    private ZhihuClient zhihuClient;
    
    public ZhihuUserTool() {
        this.zhihuClient = new ZhihuClient();
        init();
    }
    
    private void init() {
        setName("ZhihuUserTool");
        setDescription("知乎用户信息工具，可以获取知乎用户的详细信息。输入参数：userId(用户ID)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"userId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"知乎用户ID\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"userId\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("知乎用户信息工具输入: {}", toolInput);
        
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String userId = (String) inputMap.get("userId");
            
            if (StringUtils.isBlank(userId)) {
                return new ToolExecuteResult("错误：用户ID不能为空");
            }
            
            ZhihuAuthor author = zhihuClient.getUserInfo(userId);
            
            StringBuilder result = new StringBuilder();
            result.append("用户信息：\n");
            result.append("用户名：").append(author.getName()).append("\n");
            result.append("用户ID：").append(author.getId()).append("\n");
            result.append("个人简介：").append(author.getHeadline()).append("\n");
            result.append("详细描述：").append(author.getDescription()).append("\n");
            result.append("头像：").append(author.getAvatarUrl()).append("\n");
            result.append("个人主页：").append(author.getUrl()).append("\n");
            result.append("性别：").append(author.getGender()).append("\n");
            result.append("所在地：").append(author.getLocation()).append("\n");
            result.append("行业：").append(author.getBusiness()).append("\n");
            result.append("关注者数：").append(author.getFollowerCount()).append("\n");
            result.append("关注数：").append(author.getFollowingCount()).append("\n");
            result.append("回答数：").append(author.getAnswerCount()).append("\n");
            result.append("提问数：").append(author.getQuestionCount()).append("\n");
            result.append("文章数：").append(author.getArticleCount()).append("\n");
            result.append("获得赞同数：").append(author.getVoteupCount()).append("\n");
            result.append("徽章：").append(author.getBadge()).append("\n");
            
            return new ToolExecuteResult(result.toString());
            
        } catch (Exception e) {
            log.error("获取知乎用户信息失败", e);
            return new ToolExecuteResult("获取用户信息失败: " + e.getMessage());
        }
    }
}