package com.alibaba.langengine.douyin;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.douyin.tool.DouyinSearchTool;
import com.alibaba.langengine.douyin.tool.DouyinUserTool;
import com.alibaba.langengine.douyin.tool.DouyinVideoTool;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 抖音工具调用简单测试
 * 说明：抖音对接口有严格风控，下述测试默认禁用。
 * 如需本地验证，请去掉 @Disabled，并适配必要的 Cookie/Headers。
 */
public class DouyinToolsTest {

    @Test
    @Disabled("集成环境下禁用外网调用，仅供本地验证")
    @DisplayName("抖音搜索工具-示例(视频)")
    void testSearchVideo() {
        DouyinSearchTool tool = new DouyinSearchTool();
        ToolExecuteResult res = tool.run("{\"keyword\":\"风景\",\"type\":\"video\",\"count\":3}");
        System.out.println(res.getOutput());
    }

    @Test
    @Disabled("集成环境下禁用外网调用，仅供本地验证")
    @DisplayName("抖音搜索工具-示例(用户)")
    void testSearchUser() {
        DouyinSearchTool tool = new DouyinSearchTool();
        ToolExecuteResult res = tool.run("{\"keyword\":\"旅行\",\"type\":\"user\",\"count\":3}");
        System.out.println(res.getOutput());
    }

    @Test
    @Disabled("集成环境下禁用外网调用，仅供本地验证")
    @DisplayName("抖音用户工具-示例")
    void testUser() {
        DouyinUserTool tool = new DouyinUserTool();
        ToolExecuteResult res = tool.run("{\"userId\":\"123456789\",\"includeVideos\":true,\"videoCount\":2}");
        System.out.println(res.getOutput());
    }

    @Test
    @Disabled("集成环境下禁用外网调用，仅供本地验证")
    @DisplayName("抖音视频详情工具-示例")
    void testVideo() {
        DouyinVideoTool tool = new DouyinVideoTool();
        ToolExecuteResult res = tool.run("{\"awemeId\":\"987654321\"}");
        System.out.println(res.getOutput());
    }
}

