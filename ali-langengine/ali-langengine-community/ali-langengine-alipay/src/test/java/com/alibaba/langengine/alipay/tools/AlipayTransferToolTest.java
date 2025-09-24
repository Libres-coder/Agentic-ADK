package com.alibaba.langengine.alipay.tools;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.alipay.AlipayConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("支付宝转账工具测试")
class AlipayTransferToolTest {

    private AlipayTransferTool transferTool;
    private AlipayConfiguration config;

    @BeforeEach
    void setUp() {
        config = new AlipayConfiguration("test_app_id", "test_private_key", "test_public_key");
        transferTool = new AlipayTransferTool(config);
    }

    @Nested
    @DisplayName("转账测试")
    class TransferTests {

        @Test
        @DisplayName("简单转账")
        void testSimpleTransfer() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("转账到手机号")
        void testTransferToPhoneNumber() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"13800138000\",\n" +
                    "  \"amount\": \"50.00\",\n" +
                    "  \"remark\": \"手机号转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("大额转账")
        void testLargeAmountTransfer() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"10000.00\",\n" +
                    "  \"remark\": \"大额转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("小额转账")
        void testSmallAmountTransfer() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"0.01\",\n" +
                    "  \"remark\": \"小额转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("带真实姓名的转账")
        void testTransferWithRealName() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"测试转账\",\n" +
                    "  \"payeeRealName\": \"张三\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("长备注转账")
        void testTransferWithLongRemark() {
            String longRemark = "这是一条很长的转账备注，用于测试长文本处理能力。" +
                    "包含各种特殊字符：!@#$%^&*()_+-=[]{}|;':\",./<>?`~" +
                    "以及中文和英文混合内容。";
            
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"" + longRemark + "\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ParameterValidationTests {

        @Test
        @DisplayName("测试空收款账号")
        void testEmptyPayeeAccount() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"\",\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试空转账金额")
        void testEmptyAmount() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试空转账备注")
        void testEmptyRemark() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少收款账号参数")
        void testMissingPayeeAccount() {
            String toolInput = "{\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少转账金额参数")
        void testMissingAmount() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少转账备注参数")
        void testMissingRemark() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100.00\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试null收款账号")
        void testNullPayeeAccount() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": null,\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试null转账金额")
        void testNullAmount() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": null,\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试null转账备注")
        void testNullRemark() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": null\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }
    }

    @Nested
    @DisplayName("金额格式测试")
    class AmountFormatTests {

        @Test
        @DisplayName("测试标准金额格式")
        void testStandardAmountFormat() {
            String[] standardAmounts = {
                "0.01",
                "1.00",
                "100.00",
                "1000.50",
                "9999.99"
            };

            for (String amount : standardAmounts) {
                String toolInput = "{\n" +
                        "  \"payeeAccount\": \"example@alipay.com\",\n" +
                        "  \"amount\": \"" + amount + "\",\n" +
                        "  \"remark\": \"测试转账\"\n" +
                        "}";

                ToolExecuteResult result = transferTool.run(toolInput, null);
                assertNotNull(result);
                assertNotNull(result.getOutput());
            }
        }

        @Test
        @DisplayName("测试整数金额")
        void testIntegerAmount() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试多位小数金额")
        void testMultipleDecimalAmount() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100.123456\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试零金额")
        void testZeroAmount() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"0.00\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试负数金额")
        void testNegativeAmount() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"-100.00\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }
    }

    @Nested
    @DisplayName("收款账号格式测试")
    class PayeeAccountFormatTests {

        @Test
        @DisplayName("测试邮箱格式账号")
        void testEmailFormatAccount() {
            String[] emailAccounts = {
                "example@alipay.com",
                "test@example.com",
                "user.name@domain.com",
                "user+tag@example.com"
            };

            for (String account : emailAccounts) {
                String toolInput = "{\n" +
                        "  \"payeeAccount\": \"" + account + "\",\n" +
                        "  \"amount\": \"100.00\",\n" +
                        "  \"remark\": \"测试转账\"\n" +
                        "}";

                ToolExecuteResult result = transferTool.run(toolInput, null);
                assertNotNull(result);
                assertNotNull(result.getOutput());
            }
        }

        @Test
        @DisplayName("测试手机号格式账号")
        void testPhoneNumberFormatAccount() {
            String[] phoneAccounts = {
                "13800138000",
                "13900139000",
                "13700137000",
                "18600186000"
            };

            for (String account : phoneAccounts) {
                String toolInput = "{\n" +
                        "  \"payeeAccount\": \"" + account + "\",\n" +
                        "  \"amount\": \"100.00\",\n" +
                        "  \"remark\": \"测试转账\"\n" +
                        "}";

                ToolExecuteResult result = transferTool.run(toolInput, null);
                assertNotNull(result);
                assertNotNull(result.getOutput());
            }
        }

        @Test
        @DisplayName("测试特殊字符账号")
        void testSpecialCharacterAccount() {
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"user_123-test@example.com\",\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("测试无效JSON输入")
        void testInvalidJsonInput() {
            String invalidJson = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100.00\"\n" +
                    "  // 缺少逗号\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(invalidJson, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试空输入")
        void testEmptyInput() {
            ToolExecuteResult result = transferTool.run("", null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试null输入")
        void testNullInput() {
            ToolExecuteResult result = transferTool.run(null, null);
            assertNotNull(result);
            assertTrue(result.getOutput().contains("错误"));
        }

        @Test
        @DisplayName("测试非JSON输入")
        void testNonJsonInput() {
            String nonJsonInput = "这不是JSON格式的输入";
            ToolExecuteResult result = transferTool.run(nonJsonInput, null);
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
            assertEquals("alipay_transfer", transferTool.getName());
        }

        @Test
        @DisplayName("测试工具中文名称")
        void testToolHumanName() {
            assertEquals("支付宝转账工具", transferTool.getHumanName());
        }

        @Test
        @DisplayName("测试工具描述")
        void testToolDescription() {
            assertNotNull(transferTool.getDescription());
            assertTrue(transferTool.getDescription().contains("支付宝"));
            assertTrue(transferTool.getDescription().contains("转账"));
        }

        @Test
        @DisplayName("测试工具参数")
        void testToolParameters() {
            assertNotNull(transferTool.getParameters());
            assertTrue(transferTool.getParameters().contains("payeeAccount"));
            assertTrue(transferTool.getParameters().contains("amount"));
            assertTrue(transferTool.getParameters().contains("remark"));
            assertTrue(transferTool.getParameters().contains("payeeRealName"));
        }
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("测试默认配置")
        void testDefaultConfiguration() {
            AlipayTransferTool defaultTool = new AlipayTransferTool();
            assertNotNull(defaultTool);
            assertEquals("alipay_transfer", defaultTool.getName());
        }

        @Test
        @DisplayName("测试自定义配置")
        void testCustomConfiguration() {
            AlipayConfiguration customConfig = new AlipayConfiguration(
                    "custom_app_id", "custom_private_key", "custom_public_key");
            AlipayTransferTool customTool = new AlipayTransferTool(customConfig);
            assertNotNull(customTool);
            assertEquals("alipay_transfer", customTool.getName());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("测试最大长度收款账号")
        void testMaximumLengthPayeeAccount() {
            String maxLengthAccount = "a".repeat(1000) + "@example.com";
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"" + maxLengthAccount + "\",\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试最大长度转账金额")
        void testMaximumLengthAmount() {
            String maxLengthAmount = "999999999999.99";
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"" + maxLengthAmount + "\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试最大长度转账备注")
        void testMaximumLengthRemark() {
            String maxLengthRemark = "a".repeat(10000);
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"" + maxLengthRemark + "\"\n" +
                    "}";

            ToolExecuteResult result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }
    }
}
