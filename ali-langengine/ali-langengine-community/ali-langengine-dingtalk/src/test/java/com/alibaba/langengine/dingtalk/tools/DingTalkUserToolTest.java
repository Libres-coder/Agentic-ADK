package com.alibaba.langengine.dingtalk.tools;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.dingtalk.DingTalkConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("钉钉用户工具测试")
class DingTalkUserToolTest {

    private DingTalkUserTool userTool;
    private DingTalkConfiguration config;

    @BeforeEach
    void setUp() {
        config = new DingTalkConfiguration("test_app_key", "test_app_secret", "test_agent_id", "test_corp_id");
        userTool = new DingTalkUserTool(config);
    }

    @Nested
    @DisplayName("用户信息查询测试")
    class UserInfoQueryTests {

        @Test
        @DisplayName("查询有效用户信息")
        void testQueryValidUserInfo() {
            String toolInput = "{\n" +
                    "  \"userId\": \"user123\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("查询数字用户ID")
        void testQueryNumericUserId() {
            String toolInput = "{\n" +
                    "  \"userId\": \"123456\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("查询包含特殊字符的用户ID")
        void testQueryUserIdWithSpecialCharacters() {
            String toolInput = "{\n" +
                    "  \"userId\": \"user_123-test\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("查询长用户ID")
        void testQueryLongUserId() {
            String longUserId = "user_" + "a".repeat(100);
            String toolInput = "{\n" +
                    "  \"userId\": \"" + longUserId + "\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("查询包含中文的用户ID")
        void testQueryChineseUserId() {
            String toolInput = "{\n" +
                    "  \"userId\": \"用户123\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ParameterValidationTests {

        @Test
        @DisplayName("测试空用户ID")
        void testEmptyUserId() {
            String toolInput = "{\n" +
                    "  \"userId\": \"\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试null用户ID")
        void testNullUserId() {
            String toolInput = "{\n" +
                    "  \"userId\": null\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少用户ID参数")
        void testMissingUserId() {
            String toolInput = "{}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试只有空格的用户ID")
        void testWhitespaceUserId() {
            String toolInput = "{\n" +
                    "  \"userId\": \"   \"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("测试无效JSON输入")
        void testInvalidJsonInput() {
            String invalidJson = "{\n" +
                    "  \"userId\": \"user123\"\n" +
                    "  // 缺少逗号\n" +
                    "}";

            ToolExecuteResult result = userTool.run(invalidJson, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试空输入")
        void testEmptyInput() {
            ToolExecuteResult result = userTool.run("", null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试null输入")
        void testNullInput() {
            ToolExecuteResult result = userTool.run(null, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试非JSON输入")
        void testNonJsonInput() {
            String nonJsonInput = "这不是JSON格式的输入";
            ToolExecuteResult result = userTool.run(nonJsonInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }
    }

    @Nested
    @DisplayName("工具属性测试")
    class ToolPropertiesTests {

        @Test
        @DisplayName("测试工具名称")
        void testToolName() {
            assertEquals("dingtalk_user", userTool.getName());
        }

        @Test
        @DisplayName("测试工具中文名称")
        void testToolHumanName() {
            assertEquals("钉钉用户工具", userTool.getHumanName());
        }

        @Test
        @DisplayName("测试工具描述")
        void testToolDescription() {
            assertNotNull(userTool.getDescription());
            assertTrue(userTool.getDescription().contains("钉钉用户"));
        }

        @Test
        @DisplayName("测试工具参数")
        void testToolParameters() {
            assertNotNull(userTool.getParameters());
            assertTrue(userTool.getParameters().contains("userId"));
        }
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("测试默认配置")
        void testDefaultConfiguration() {
            DingTalkUserTool defaultTool = new DingTalkUserTool();
            assertNotNull(defaultTool);
            assertEquals("dingtalk_user", defaultTool.getName());
        }

        @Test
        @DisplayName("测试自定义配置")
        void testCustomConfiguration() {
            DingTalkConfiguration customConfig = new DingTalkConfiguration(
                    "custom_app_key", "custom_app_secret", "custom_agent_id", "custom_corp_id");
            DingTalkUserTool customTool = new DingTalkUserTool(customConfig);
            assertNotNull(customTool);
            assertEquals("dingtalk_user", customTool.getName());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("测试最小长度用户ID")
        void testMinimumLengthUserId() {
            String toolInput = "{\n" +
                    "  \"userId\": \"a\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试最大长度用户ID")
        void testMaximumLengthUserId() {
            String maxLengthUserId = "a".repeat(1000);
            String toolInput = "{\n" +
                    "  \"userId\": \"" + maxLengthUserId + "\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试包含所有ASCII字符的用户ID")
        void testUserIdWithAllAsciiCharacters() {
            StringBuilder asciiUserId = new StringBuilder();
            for (int i = 32; i <= 126; i++) {
                asciiUserId.append((char) i);
            }
            
            String toolInput = "{\n" +
                    "  \"userId\": \"" + asciiUserId.toString() + "\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }
    }
}