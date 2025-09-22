package com.alibaba.langengine.alipay.tools;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.alipay.AlipayConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("支付宝账单查询工具测试")
class AlipayBillQueryToolTest {

    private AlipayBillQueryTool billQueryTool;
    private AlipayConfiguration config;

    @BeforeEach
    void setUp() {
        config = new AlipayConfiguration("test_app_id", "test_private_key", "test_public_key");
        billQueryTool = new AlipayBillQueryTool(config);
    }

    @Nested
    @DisplayName("账单查询测试")
    class BillQueryTests {

        @Test
        @DisplayName("查询交易账单")
        void testQueryTradeBill() {
            String toolInput = "{\n" +
                    "  \"billType\": \"trade\",\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询签约客户账单")
        void testQuerySignCustomerBill() {
            String toolInput = "{\n" +
                    "  \"billType\": \"signcustomer\",\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询不同日期的账单")
        void testQueryDifferentDateBills() {
            String[] dates = {
                "2024-01-01",
                "2024-02-29",
                "2024-12-31",
                "2023-01-01",
                "2025-01-01"
            };

            for (String date : dates) {
                String toolInput = "{\n" +
                        "  \"billType\": \"trade\",\n" +
                        "  \"billDate\": \"" + date + "\"\n" +
                        "}";

                ToolExecuteResult result = billQueryTool.run(toolInput, null);
                assertNotNull(result);
                assertNotNull(result.getResult());
            }
        }

        @Test
        @DisplayName("查询未来日期账单")
        void testQueryFutureDateBill() {
            String toolInput = "{\n" +
                    "  \"billType\": \"trade\",\n" +
                    "  \"billDate\": \"2030-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询历史日期账单")
        void testQueryHistoricalDateBill() {
            String toolInput = "{\n" +
                    "  \"billType\": \"trade\",\n" +
                    "  \"billDate\": \"2020-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ParameterValidationTests {

        @Test
        @DisplayName("测试空账单类型")
        void testEmptyBillType() {
            String toolInput = "{\n" +
                    "  \"billType\": \"\",\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试空账单日期")
        void testEmptyBillDate() {
            String toolInput = "{\n" +
                    "  \"billType\": \"trade\",\n" +
                    "  \"billDate\": \"\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少账单类型参数")
        void testMissingBillType() {
            String toolInput = "{\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少账单日期参数")
        void testMissingBillDate() {
            String toolInput = "{\n" +
                    "  \"billType\": \"trade\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null账单类型")
        void testNullBillType() {
            String toolInput = "{\n" +
                    "  \"billType\": null,\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null账单日期")
        void testNullBillDate() {
            String toolInput = "{\n" +
                    "  \"billType\": \"trade\",\n" +
                    "  \"billDate\": null\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试无效账单类型")
        void testInvalidBillType() {
            String toolInput = "{\n" +
                    "  \"billType\": \"invalid_type\",\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }
    }

    @Nested
    @DisplayName("日期格式测试")
    class DateFormatTests {

        @Test
        @DisplayName("测试标准日期格式")
        void testStandardDateFormat() {
            String[] standardDates = {
                "2024-01-01",
                "2024-02-29",
                "2024-12-31",
                "2023-12-31",
                "2025-01-01"
            };

            for (String date : standardDates) {
                String toolInput = "{\n" +
                        "  \"billType\": \"trade\",\n" +
                        "  \"billDate\": \"" + date + "\"\n" +
                        "}";

                ToolExecuteResult result = billQueryTool.run(toolInput, null);
                assertNotNull(result);
                assertNotNull(result.getResult());
            }
        }

        @Test
        @DisplayName("测试非标准日期格式")
        void testNonStandardDateFormat() {
            String[] nonStandardDates = {
                "2024/01/01",
                "2024.01.01",
                "01-01-2024",
                "20240101",
                "2024-1-1"
            };

            for (String date : nonStandardDates) {
                String toolInput = "{\n" +
                        "  \"billType\": \"trade\",\n" +
                        "  \"billDate\": \"" + date + "\"\n" +
                        "}";

                ToolExecuteResult result = billQueryTool.run(toolInput, null);
                assertNotNull(result);
                assertNotNull(result.getResult());
            }
        }

        @Test
        @DisplayName("测试无效日期格式")
        void testInvalidDateFormat() {
            String[] invalidDates = {
                "2024-13-01",
                "2024-02-30",
                "2024-04-31",
                "2024-00-01",
                "2024-01-00",
                "not_a_date"
            };

            for (String date : invalidDates) {
                String toolInput = "{\n" +
                        "  \"billType\": \"trade\",\n" +
                        "  \"billDate\": \"" + date + "\"\n" +
                        "}";

                ToolExecuteResult result = billQueryTool.run(toolInput, null);
                assertNotNull(result);
                assertNotNull(result.getResult());
            }
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("测试无效JSON输入")
        void testInvalidJsonInput() {
            String invalidJson = "{\n" +
                    "  \"billType\": \"trade\",\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "  // 缺少逗号\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(invalidJson, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试空输入")
        void testEmptyInput() {
            ToolExecuteResult result = billQueryTool.run("", null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null输入")
        void testNullInput() {
            ToolExecuteResult result = billQueryTool.run(null, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试非JSON输入")
        void testNonJsonInput() {
            String nonJsonInput = "这不是JSON格式的输入";
            ToolExecuteResult result = billQueryTool.run(nonJsonInput, null);
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
            assertEquals("alipay_bill_query", billQueryTool.getName());
        }

        @Test
        @DisplayName("测试工具中文名称")
        void testToolHumanName() {
            assertEquals("支付宝账单查询工具", billQueryTool.getHumanName());
        }

        @Test
        @DisplayName("测试工具描述")
        void testToolDescription() {
            assertNotNull(billQueryTool.getDescription());
            assertTrue(billQueryTool.getDescription().contains("支付宝"));
            assertTrue(billQueryTool.getDescription().contains("账单"));
        }

        @Test
        @DisplayName("测试工具参数")
        void testToolParameters() {
            assertNotNull(billQueryTool.getParameters());
            assertTrue(billQueryTool.getParameters().contains("billType"));
            assertTrue(billQueryTool.getParameters().contains("billDate"));
        }
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("测试默认配置")
        void testDefaultConfiguration() {
            AlipayBillQueryTool defaultTool = new AlipayBillQueryTool();
            assertNotNull(defaultTool);
            assertEquals("alipay_bill_query", defaultTool.getName());
        }

        @Test
        @DisplayName("测试自定义配置")
        void testCustomConfiguration() {
            AlipayConfiguration customConfig = new AlipayConfiguration(
                    "custom_app_id", "custom_private_key", "custom_public_key");
            AlipayBillQueryTool customTool = new AlipayBillQueryTool(customConfig);
            assertNotNull(customTool);
            assertEquals("alipay_bill_query", customTool.getName());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("测试最小长度账单类型")
        void testMinimumLengthBillType() {
            String toolInput = "{\n" +
                    "  \"billType\": \"a\",\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试最大长度账单类型")
        void testMaximumLengthBillType() {
            String maxLengthBillType = "a".repeat(100);
            String toolInput = "{\n" +
                    "  \"billType\": \"" + maxLengthBillType + "\",\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试最大长度账单日期")
        void testMaximumLengthBillDate() {
            String maxLengthBillDate = "a".repeat(100);
            String toolInput = "{\n" +
                    "  \"billType\": \"trade\",\n" +
                    "  \"billDate\": \"" + maxLengthBillDate + "\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("特殊值测试")
    class SpecialValueTests {

        @Test
        @DisplayName("测试布尔值账单类型")
        void testBooleanBillType() {
            String toolInput = "{\n" +
                    "  \"billType\": true,\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试数字账单类型")
        void testNumericBillType() {
            String toolInput = "{\n" +
                    "  \"billType\": 123,\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试浮点数账单类型")
        void testFloatBillType() {
            String toolInput = "{\n" +
                    "  \"billType\": 123.456,\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试数组账单类型")
        void testArrayBillType() {
            String toolInput = "{\n" +
                    "  \"billType\": [\"trade\"],\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试对象账单类型")
        void testObjectBillType() {
            String toolInput = "{\n" +
                    "  \"billType\": {\"type\": \"trade\"},\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";

            ToolExecuteResult result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }
}
