package com.alibaba.langengine.zhihu;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.zhihu.tool.ZhihuQuestionTool;
import com.alibaba.langengine.zhihu.tool.ZhihuSearchTool;
import com.alibaba.langengine.zhihu.tool.ZhihuUserTool;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 知乎工具调用简单测试
 * 说明：由于知乎官方接口存在风控与反爬策略，下述测试默认禁用。
 * 如需本地验证，请去掉 @Disabled，并确保网络可访问且具备必要 Cookie/Headers。
 */
public class ZhihuToolsTest {

    @Test
    @Disabled("集成环境下禁用外网调用，仅供本地验证")
    @DisplayName("知乎搜索工具-示例")
    void testSearch() {
        ZhihuSearchTool tool = new ZhihuSearchTool();
        ToolExecuteResult res = tool.run("{\"query\":\"人工智能\",\"limit\":3}");
        System.out.println(res.getOutput());
    }

    @Test
    @Disabled("集成环境下禁用外网调用，仅供本地验证")
    @DisplayName("知乎问题详情工具-示例")
    void testQuestion() {
        ZhihuQuestionTool tool = new ZhihuQuestionTool();
        ToolExecuteResult res = tool.run("{\"questionId\":\"123456789\",\"includeAnswers\":true,\"answerLimit\":2}");
        System.out.println(res.getOutput());
    }

    @Test
    @Disabled("集成环境下禁用外网调用，仅供本地验证")
    @DisplayName("知乎用户工具-示例")
    void testUser() {
        ZhihuUserTool tool = new ZhihuUserTool();
        ToolExecuteResult res = tool.run("{\"userId\":\"excited-vczh\"}");
        System.out.println(res.getOutput());
    }
}

