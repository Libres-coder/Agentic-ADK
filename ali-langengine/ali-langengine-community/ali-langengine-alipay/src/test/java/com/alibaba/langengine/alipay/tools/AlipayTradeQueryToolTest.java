package com.alibaba.langengine.alipay.tools;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.alipay.AlipayConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("支付宝交易查询工具测试")
class AlipayTradeQueryToolTest {

    private AlipayTradeQueryTool tradeQueryTool;
    private AlipayConfiguration config;

    @BeforeEach
    void setUp() {
        config = new AlipayConfiguration("test_app_id", "test_private_key", "test_public_key");
        tradeQueryTool = new AlipayTradeQueryTool(config);
    }

    @Nested
    @DisplayName("交易查询测试")
    class TradeQueryTests {

        @Test
        @DisplayName("查询有效交易")
        void testQueryValidTrade() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"2024010100000000000000000000000000\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("查询数字订单号")
        void testQueryNumericOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"123456789012345678901234567890\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("查询包含特殊字符的订单号")
        void testQueryOrderNoWithSpecialCharacters() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"order_123-test_456\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("查询长订单号")
        void testQueryLongOrderNo() {
            String longOrderNo = "order_" + "a".repeat(100);
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"" + longOrderNo + "\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("查询包含中文的订单号")
            void testQueryChineseOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"订单123456\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("查询包含下划线的订单号")
        void testQueryOrderNoWithUnderscores() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"order_name_123\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("查询包含连字符的订单号")
        void testQueryOrderNoWithHyphens() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"order-name-123\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ParameterValidationTests {

        @Test
        @DisplayName("测试空订单号")
        void testEmptyOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试null订单号")
        void testNullOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": null\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少订单号参数")
        void testMissingOrderNo() {
            String toolInput = "{}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试只有空格的订单号")
        void testWhitespaceOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"   \"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试制表符订单号")
        void testTabOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"\t\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试换行符订单号")
        void testNewlineOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"\\n\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
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
                    "  \"outTradeNo\": \"2024010100000000000000000000000000\"\n" +
                    "  // 缺少逗号\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(invalidJson, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试空输入")
        void testEmptyInput() {
            ToolExecuteResult result = tradeQueryTool.run("", null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试null输入")
        void testNullInput() {
            ToolExecuteResult result = tradeQueryTool.run(null, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试非JSON输入")
        void testNonJsonInput() {
            String nonJsonInput = "这不是JSON格式的输入";
            ToolExecuteResult result = tradeQueryTool.run(nonJsonInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试数组输入")
        void testArrayInput() {
            String arrayInput = "[\"2024010100000000000000000000000000\"]";
            ToolExecuteResult result = tradeQueryTool.run(arrayInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试字符串输入")
        void testStringInput() {
            String stringInput = "\"2024010100000000000000000000000000\"";
            ToolExecuteResult result = tradeQueryTool.run(stringInput, null);
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
            assertEquals("alipay_trade_query", tradeQueryTool.getName());
        }

        @Test
        @DisplayName("测试工具中文名称")
        void testToolHumanName() {
            assertEquals("支付宝交易查询工具", tradeQueryTool.getHumanName());
        }

        @Test
        @DisplayName("测试工具描述")
        void testToolDescription() {
            assertNotNull(tradeQueryTool.getDescription());
            assertTrue(tradeQueryTool.getDescription().contains("支付宝"));
            assertTrue(tradeQueryTool.getDescription().contains("交易"));
        }

        @Test
        @DisplayName("测试工具参数")
        void testToolParameters() {
            assertNotNull(tradeQueryTool.getParameters());
            assertTrue(tradeQueryTool.getParameters().contains("outTradeNo"));
            assertTrue(tradeQueryTool.getParameters().contains("tradeNo"));
        }
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("测试默认配置")
        void testDefaultConfiguration() {
            AlipayTradeQueryTool defaultTool = new AlipayTradeQueryTool();
            assertNotNull(defaultTool);
            assertEquals("alipay_trade_query", defaultTool.getName());
        }

        @Test
        @DisplayName("测试自定义配置")
        void testCustomConfiguration() {
            AlipayConfiguration customConfig = new AlipayConfiguration(
                    "custom_app_id", "custom_private_key", "custom_public_key");
            AlipayTradeQueryTool customTool = new AlipayTradeQueryTool(customConfig);
            assertNotNull(customTool);
            assertEquals("alipay_trade_query", customTool.getName());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("测试最小长度订单号")
        void testMinimumLengthOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"a\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试最大长度订单号")
        void testMaximumLengthOrderNo() {
            String maxLengthOrderNo = "a".repeat(1000);
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"" + maxLengthOrderNo + "\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试包含所有ASCII字符的订单号")
        void testOrderNoWithAllAsciiCharacters() {
            StringBuilder asciiOrderNo = new StringBuilder();
            for (int i = 32; i <= 126; i++) {
                asciiOrderNo.append((char) i);
            }
            
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"" + asciiOrderNo.toString() + "\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试Unicode订单号")
        void testUnicodeOrderNo() {
            String unicodeOrderNo = "订单123测试🚀🎉";
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"" + unicodeOrderNo + "\"\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }
    }

    @Nested
    @DisplayName("特殊值测试")
    class SpecialValueTests {

        @Test
        @DisplayName("测试布尔值订单号")
        void testBooleanOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": true\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试数字订单号")
        void testNumericOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": 123456789\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试浮点数订单号")
        void testFloatOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": 123.456\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试数组订单号")
        void testArrayOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": [\"2024010100000000000000000000000000\"]\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试对象订单号")
        void testObjectOrderNo() {
            String toolInput = "{\n" +
                    "  \"outTradeNo\": {\"id\": \"2024010100000000000000000000000000\"}\n" +
                    "}";

            ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }
    }
}
