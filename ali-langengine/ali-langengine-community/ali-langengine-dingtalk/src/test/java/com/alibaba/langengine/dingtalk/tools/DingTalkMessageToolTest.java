package com.alibaba.langengine.dingtalk.tools;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.dingtalk.DingTalkConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("钉钉消息工具测试")
class DingTalkMessageToolTest {

    private DingTalkMessageTool messageTool;
    private DingTalkConfiguration config;

    @BeforeEach
    void setUp() {
        config = new DingTalkConfiguration("test_app_key", "test_app_secret", "test_agent_id", "test_corp_id");
        messageTool = new DingTalkMessageTool(config);
    }

    @Nested
    @DisplayName("文本消息测试")
    class TextMessageTests {

        @Test
        @DisplayName("发送简单文本消息")
        void testSendSimpleTextMessage() {
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"text\",\n" +
                    "  \"content\": \"这是一条测试消息\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送给多个用户的文本消息")
        void testSendTextMessageToMultipleUsers() {
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1,user2,user3\",\n" +
                    "  \"messageType\": \"text\",\n" +
                    "  \"content\": \"这是一条群发消息\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送长文本消息")
        void testSendLongTextMessage() {
            StringBuilder longContent = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                longContent.append("这是一条很长的测试消息，用于测试长文本处理能力。");
            }

            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"text\",\n" +
                    "  \"content\": \"" + longContent.toString() + "\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送包含特殊字符的文本消息")
        void testSendTextMessageWithSpecialCharacters() {
            String specialContent = "测试特殊字符：!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"text\",\n" +
                    "  \"content\": \"" + specialContent + "\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送包含换行符的文本消息")
        void testSendTextMessageWithNewlines() {
            String contentWithNewlines = "第一行\n第二行\n第三行";
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"text\",\n" +
                    "  \"content\": \"" + contentWithNewlines + "\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("链接消息测试")
    class LinkMessageTests {

        @Test
        @DisplayName("发送简单链接消息")
        void testSendSimpleLinkMessage() {
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"link\",\n" +
                    "  \"content\": \"点击查看详情\",\n" +
                    "  \"title\": \"重要通知\",\n" +
                    "  \"messageUrl\": \"https://www.example.com\",\n" +
                    "  \"picUrl\": \"https://www.example.com/image.jpg\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送无图片的链接消息")
        void testSendLinkMessageWithoutPicture() {
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"link\",\n" +
                    "  \"content\": \"点击查看详情\",\n" +
                    "  \"title\": \"重要通知\",\n" +
                    "  \"messageUrl\": \"https://www.example.com\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送长标题链接消息")
        void testSendLinkMessageWithLongTitle() {
            String longTitle = "这是一个非常长的标题，用于测试长标题的处理能力，应该能够正确处理和显示";
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"link\",\n" +
                    "  \"content\": \"点击查看详情\",\n" +
                    "  \"title\": \"" + longTitle + "\",\n" +
                    "  \"messageUrl\": \"https://www.example.com\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ParameterValidationTests {

        @Test
        @DisplayName("测试空用户ID")
        void testEmptyUserIds() {
            String toolInput = "{\n" +
                    "  \"userIds\": \"\",\n" +
                    "  \"messageType\": \"text\",\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试空消息内容")
        void testEmptyContent() {
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"text\",\n" +
                    "  \"content\": \"\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试空消息类型")
        void testEmptyMessageType() {
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"\",\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试无效消息类型")
        void testInvalidMessageType() {
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"invalid_type\",\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试链接消息缺少标题")
        void testLinkMessageMissingTitle() {
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"link\",\n" +
                    "  \"content\": \"点击查看详情\",\n" +
                    "  \"messageUrl\": \"https://www.example.com\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试链接消息缺少URL")
        void testLinkMessageMissingUrl() {
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"link\",\n" +
                    "  \"content\": \"点击查看详情\",\n" +
                    "  \"title\": \"重要通知\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("测试无效JSON输入")
        void testInvalidJsonInput() {
            String invalidJson = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"text\",\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "  // 缺少逗号\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(invalidJson, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试空输入")
        void testEmptyInput() {
            ToolExecuteResult result = messageTool.run("", null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null输入")
        void testNullInput() {
            ToolExecuteResult result = messageTool.run(null, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }
    }

    @Nested
    @DisplayName("工具属性测试")
    class ToolPropertiesTests {

        @Test
        @DisplayName("测试工具名称")
        void testToolName() {
            assertEquals("dingtalk_message", messageTool.getName());
        }

        @Test
        @DisplayName("测试工具中文名称")
        void testToolHumanName() {
            assertEquals("钉钉消息工具", messageTool.getHumanName());
        }

        @Test
        @DisplayName("测试工具描述")
        void testToolDescription() {
            assertNotNull(messageTool.getDescription());
            assertTrue(messageTool.getDescription().contains("钉钉消息"));
        }

        @Test
        @DisplayName("测试工具参数")
        void testToolParameters() {
            assertNotNull(messageTool.getParameters());
            assertTrue(messageTool.getParameters().contains("userIds"));
            assertTrue(messageTool.getParameters().contains("messageType"));
            assertTrue(messageTool.getParameters().contains("content"));
        }
    }
}
