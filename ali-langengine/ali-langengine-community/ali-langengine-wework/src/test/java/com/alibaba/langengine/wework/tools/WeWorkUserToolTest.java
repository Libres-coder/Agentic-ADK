package com.alibaba.langengine.wework.tools;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.wework.WeWorkConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("企业微信用户工具测试")
class WeWorkUserToolTest {

    private WeWorkUserTool userTool;
    private WeWorkConfiguration config;

    @BeforeEach
    void setUp() {
        config = new WeWorkConfiguration("test_corp_id", "test_corp_secret", "test_agent_id");
        userTool = new WeWorkUserTool(config);
    }

    @Nested
    @DisplayName("用户信息查询测试")
    class UserInfoQueryTests {

        @Test
        @DisplayName("查询有效用户信息")
        void testQueryValidUserInfo() {
            String toolInput = "{\n" +
                    "  \"userid\": \"user123\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询数字用户ID")
        void testQueryNumericUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": \"123456\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询包含特殊字符的用户ID")
        void testQueryUserIdWithSpecialCharacters() {
            String toolInput = "{\n" +
                    "  \"userid\": \"user_123-test\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询长用户ID")
        void testQueryLongUserId() {
            String longUserId = "user_" + "a".repeat(100);
            String toolInput = "{\n" +
                    "  \"userid\": \"" + longUserId + "\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询包含中文的用户ID")
        void testQueryChineseUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": \"用户123\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询包含下划线的用户ID")
        void testQueryUserIdWithUnderscores() {
            String toolInput = "{\n" +
                    "  \"userid\": \"user_name_123\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询包含连字符的用户ID")
        void testQueryUserIdWithHyphens() {
            String toolInput = "{\n" +
                    "  \"userid\": \"user-name-123\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ParameterValidationTests {

        @Test
        @DisplayName("测试空用户ID")
        void testEmptyUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": \"\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null用户ID")
        void testNullUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": null\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少用户ID参数")
        void testMissingUserId() {
            String toolInput = "{}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试只有空格的用户ID")
        void testWhitespaceUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": \"   \"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试制表符用户ID")
        void testTabUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": \"\t\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试换行符用户ID")
        void testNewlineUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": \"\\n\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
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
                    "  \"userid\": \"user123\"\n" +
                    "  // 缺少逗号\n" +
                    "}";

            ToolExecuteResult result = userTool.run(invalidJson, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试空输入")
        void testEmptyInput() {
            ToolExecuteResult result = userTool.run("", null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null输入")
        void testNullInput() {
            ToolExecuteResult result = userTool.run(null, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试非JSON输入")
        void testNonJsonInput() {
            String nonJsonInput = "这不是JSON格式的输入";
            ToolExecuteResult result = userTool.run(nonJsonInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试数组输入")
        void testArrayInput() {
            String arrayInput = "[\"user123\"]";
            ToolExecuteResult result = userTool.run(arrayInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试字符串输入")
        void testStringInput() {
            String stringInput = "\"user123\"";
            ToolExecuteResult result = userTool.run(stringInput, null);
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
            assertEquals("wework_user", userTool.getName());
        }

        @Test
        @DisplayName("测试工具中文名称")
        void testToolHumanName() {
            assertEquals("企业微信用户工具", userTool.getHumanName());
        }

        @Test
        @DisplayName("测试工具描述")
        void testToolDescription() {
            assertNotNull(userTool.getDescription());
            assertTrue(userTool.getDescription().contains("企业微信"));
            assertTrue(userTool.getDescription().contains("用户"));
        }

        @Test
        @DisplayName("测试工具参数")
        void testToolParameters() {
            assertNotNull(userTool.getParameters());
            assertTrue(userTool.getParameters().contains("userid"));
        }
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("测试默认配置")
        void testDefaultConfiguration() {
            WeWorkUserTool defaultTool = new WeWorkUserTool();
            assertNotNull(defaultTool);
            assertEquals("wework_user", defaultTool.getName());
        }

        @Test
        @DisplayName("测试自定义配置")
        void testCustomConfiguration() {
            WeWorkConfiguration customConfig = new WeWorkConfiguration(
                    "custom_corp_id", "custom_corp_secret", "custom_agent_id");
            WeWorkUserTool customTool = new WeWorkUserTool(customConfig);
            assertNotNull(customTool);
            assertEquals("wework_user", customTool.getName());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("测试最小长度用户ID")
        void testMinimumLengthUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": \"a\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试最大长度用户ID")
        void testMaximumLengthUserId() {
            String maxLengthUserId = "a".repeat(1000);
            String toolInput = "{\n" +
                    "  \"userid\": \"" + maxLengthUserId + "\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试包含所有ASCII字符的用户ID")
        void testUserIdWithAllAsciiCharacters() {
            StringBuilder asciiUserId = new StringBuilder();
            for (int i = 32; i <= 126; i++) {
                asciiUserId.append((char) i);
            }
            
            String toolInput = "{\n" +
                    "  \"userid\": \"" + asciiUserId.toString() + "\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试Unicode用户ID")
        void testUnicodeUserId() {
            String unicodeUserId = "用户123测试🚀🎉";
            String toolInput = "{\n" +
                    "  \"userid\": \"" + unicodeUserId + "\"\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("特殊值测试")
    class SpecialValueTests {

        @Test
        @DisplayName("测试布尔值用户ID")
        void testBooleanUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": true\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试数字用户ID")
        void testNumericUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": 123456\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试浮点数用户ID")
        void testFloatUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": 123.456\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试数组用户ID")
        void testArrayUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": [\"user123\"]\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试对象用户ID")
        void testObjectUserId() {
            String toolInput = "{\n" +
                    "  \"userid\": {\"id\": \"user123\"}\n" +
                    "}";

            ToolExecuteResult result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }
}
