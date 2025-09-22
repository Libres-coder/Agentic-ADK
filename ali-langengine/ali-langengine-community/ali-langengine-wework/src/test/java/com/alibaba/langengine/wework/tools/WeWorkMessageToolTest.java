package com.alibaba.langengine.wework.tools;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.wework.WeWorkConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("企业微信消息工具测试")
class WeWorkMessageToolTest {

    private WeWorkMessageTool messageTool;
    private WeWorkConfiguration config;

    @BeforeEach
    void setUp() {
        config = new WeWorkConfiguration("test_corp_id", "test_corp_secret", "test_agent_id");
        messageTool = new WeWorkMessageTool(config);
    }

    @Nested
    @DisplayName("消息发送测试")
    class MessageSendingTests {

        @Test
        @DisplayName("发送简单消息")
        void testSendSimpleMessage() {
            String toolInput = "{\n" +
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": \"这是一条测试消息\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送给多个用户的消息")
        void testSendMessageToMultipleUsers() {
            String toolInput = "{\n" +
                    "  \"touser\": \"user1|user2|user3\",\n" +
                    "  \"content\": \"这是一条群发消息\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送长消息")
        void testSendLongMessage() {
            StringBuilder longContent = new StringBuilder();
            for (int i = 0; i < 200; i++) {
                longContent.append("这是一条很长的测试消息，用于测试长文本处理能力。");
            }

            String toolInput = "{\n" +
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": \"" + longContent.toString() + "\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送包含特殊字符的消息")
        void testSendMessageWithSpecialCharacters() {
            String specialContent = "测试特殊字符：!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
            String toolInput = "{\n" +
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": \"" + specialContent + "\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送包含换行符的消息")
        void testSendMessageWithNewlines() {
            String contentWithNewlines = "第一行\n第二行\n第三行";
            String toolInput = "{\n" +
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": \"" + contentWithNewlines + "\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送包含表情符号的消息")
        void testSendMessageWithEmojis() {
            String emojiContent = "测试表情符号：😀😃😄😁😆😅😂🤣😊😇🙂🙃😉😌😍🥰😘😗😙😚😋😛😝😜🤪🤨🧐🤓😎🤩🥳😏😒😞😔😟😕🙁☹️😣😖😫😩🥺😢😭😤😠😡🤬🤯😳🥵🥶😱😨😰😥😓🤗🤔🤭🤫🤥😶😐😑😬🙄😯😦😧😮😲🥱😴🤤😪😵🤐🥴🤢🤮🤧😷🤒🤕🤑🤠😈👿👹👺🤡💩👻💀☠️👽👾🤖🎃😺😸😹😻😼😽🙀😿😾";
            String toolInput = "{\n" +
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": \"" + emojiContent + "\"\n" +
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
        @DisplayName("测试空接收用户")
        void testEmptyTouser() {
            String toolInput = "{\n" +
                    "  \"touser\": \"\",\n" +
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
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": \"\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少接收用户参数")
        void testMissingTouser() {
            String toolInput = "{\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少消息内容参数")
        void testMissingContent() {
            String toolInput = "{\n" +
                    "  \"touser\": \"user1\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null接收用户")
        void testNullTouser() {
            String toolInput = "{\n" +
                    "  \"touser\": null,\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null消息内容")
        void testNullContent() {
            String toolInput = "{\n" +
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": null\n" +
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
                    "  \"touser\": \"user1\",\n" +
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

        @Test
        @DisplayName("测试非JSON输入")
        void testNonJsonInput() {
            String nonJsonInput = "这不是JSON格式的输入";
            ToolExecuteResult result = messageTool.run(nonJsonInput, null);
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
            assertEquals("wework_message", messageTool.getName());
        }

        @Test
        @DisplayName("测试工具中文名称")
        void testToolHumanName() {
            assertEquals("企业微信消息工具", messageTool.getHumanName());
        }

        @Test
        @DisplayName("测试工具描述")
        void testToolDescription() {
            assertNotNull(messageTool.getDescription());
            assertTrue(messageTool.getDescription().contains("企业微信"));
        }

        @Test
        @DisplayName("测试工具参数")
        void testToolParameters() {
            assertNotNull(messageTool.getParameters());
            assertTrue(messageTool.getParameters().contains("touser"));
            assertTrue(messageTool.getParameters().contains("content"));
        }
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("测试默认配置")
        void testDefaultConfiguration() {
            WeWorkMessageTool defaultTool = new WeWorkMessageTool();
            assertNotNull(defaultTool);
            assertEquals("wework_message", defaultTool.getName());
        }

        @Test
        @DisplayName("测试自定义配置")
        void testCustomConfiguration() {
            WeWorkConfiguration customConfig = new WeWorkConfiguration(
                    "custom_corp_id", "custom_corp_secret", "custom_agent_id");
            WeWorkMessageTool customTool = new WeWorkMessageTool(customConfig);
            assertNotNull(customTool);
            assertEquals("wework_message", customTool.getName());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("测试最大长度用户列表")
        void testMaximumLengthUserList() {
            StringBuilder longUserList = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                if (i > 0) longUserList.append("|");
                longUserList.append("user").append(i);
            }

            String toolInput = "{\n" +
                    "  \"touser\": \"" + longUserList.toString() + "\",\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试最大长度消息内容")
        void testMaximumLengthContent() {
            String maxLengthContent = "a".repeat(10000);
            String toolInput = "{\n" +
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": \"" + maxLengthContent + "\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试包含所有ASCII字符的消息")
        void testMessageWithAllAsciiCharacters() {
            StringBuilder asciiContent = new StringBuilder();
            for (int i = 32; i <= 126; i++) {
                asciiContent.append((char) i);
            }
            
            String toolInput = "{\n" +
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": \"" + asciiContent.toString() + "\"\n" +
                    "}";

            ToolExecuteResult result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }
}
