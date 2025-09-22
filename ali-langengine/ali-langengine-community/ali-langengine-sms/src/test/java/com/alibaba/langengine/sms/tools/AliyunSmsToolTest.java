package com.alibaba.langengine.sms.tools;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.sms.SmsConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("阿里云短信工具测试")
class AliyunSmsToolTest {

    private AliyunSmsTool smsTool;
    private SmsConfiguration config;

    @BeforeEach
    void setUp() {
        config = new SmsConfiguration();
        smsTool = new AliyunSmsTool(config);
    }

    @Nested
    @DisplayName("短信发送测试")
    class SmsSendingTests {

        @Test
        @DisplayName("发送简单短信")
        void testSendSimpleSms() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"templateParam\": \"{\\\"code\\\":\\\"123456\\\"}\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送给多个手机号的短信")
        void testSendSmsToMultipleNumbers() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000,13900139000,13700137000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"templateParam\": \"{\\\"code\\\":\\\"123456\\\"}\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送验证码短信")
        void testSendVerificationCodeSms() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"templateParam\": \"{\\\"code\\\":\\\"123456\\\"}\",\n" +
                    "  \"signName\": \"验证码\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送通知短信")
        void testSendNotificationSms() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_987654321\",\n" +
                    "  \"templateParam\": \"{\\\"name\\\":\\\"张三\\\",\\\"amount\\\":\\\"100.00\\\"}\",\n" +
                    "  \"signName\": \"通知\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("发送营销短信")
        void testSendMarketingSms() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_555666777\",\n" +
                    "  \"templateParam\": \"{\\\"product\\\":\\\"优惠券\\\",\\\"discount\\\":\\\"8折\\\"}\",\n" +
                    "  \"signName\": \"营销\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ParameterValidationTests {

        @Test
        @DisplayName("测试空手机号")
        void testEmptyPhoneNumbers() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试空模板代码")
        void testEmptyTemplateCode() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试空签名")
        void testEmptySignName() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"signName\": \"\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试缺少手机号参数")
        void testMissingPhoneNumbers() {
            String toolInput = "{\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null手机号")
        void testNullPhoneNumbers() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": null,\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null模板代码")
        void testNullTemplateCode() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": null,\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null签名")
        void testNullSignName() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"signName\": null\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }
    }

    @Nested
    @DisplayName("手机号格式测试")
    class PhoneNumberFormatTests {

        @Test
        @DisplayName("测试有效手机号")
        void testValidPhoneNumbers() {
            String[] validPhones = {
                "13800138000",
                "13900139000",
                "13700137000",
                "18600186000",
                "18800188000"
            };

            for (String phone : validPhones) {
                String toolInput = "{\n" +
                        "  \"phoneNumbers\": \"" + phone + "\",\n" +
                        "  \"templateCode\": \"SMS_123456789\",\n" +
                        "  \"signName\": \"阿里云\"\n" +
                        "}";

                ToolExecuteResult result = smsTool.run(toolInput, null);
                assertNotNull(result);
                assertNotNull(result.getResult());
            }
        }

        @Test
        @DisplayName("测试国际手机号")
        void testInternationalPhoneNumbers() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"+8613800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试多个手机号格式")
        void testMultiplePhoneNumberFormats() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000,13900139000,13700137000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("模板参数测试")
    class TemplateParameterTests {

        @Test
        @DisplayName("测试简单模板参数")
        void testSimpleTemplateParams() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"templateParam\": \"{\\\"code\\\":\\\"123456\\\"}\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试复杂模板参数")
        void testComplexTemplateParams() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"templateParam\": \"{\\\"name\\\":\\\"张三\\\",\\\"amount\\\":\\\"100.00\\\",\\\"date\\\":\\\"2024-01-01\\\"}\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试空模板参数")
        void testEmptyTemplateParams() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"templateParam\": \"\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试null模板参数")
        void testNullTemplateParams() {
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"templateParam\": null,\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("测试无效JSON输入")
        void testInvalidJsonInput() {
            String invalidJson = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\"\n" +
                    "  // 缺少逗号\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(invalidJson, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试空输入")
        void testEmptyInput() {
            ToolExecuteResult result = smsTool.run("", null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null输入")
        void testNullInput() {
            ToolExecuteResult result = smsTool.run(null, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试非JSON输入")
        void testNonJsonInput() {
            String nonJsonInput = "这不是JSON格式的输入";
            ToolExecuteResult result = smsTool.run(nonJsonInput, null);
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
            assertEquals("aliyun_sms", smsTool.getName());
        }

        @Test
        @DisplayName("测试工具中文名称")
        void testToolHumanName() {
            assertEquals("阿里云短信工具", smsTool.getHumanName());
        }

        @Test
        @DisplayName("测试工具描述")
        void testToolDescription() {
            assertNotNull(smsTool.getDescription());
            assertTrue(smsTool.getDescription().contains("阿里云"));
        }

        @Test
        @DisplayName("测试工具参数")
        void testToolParameters() {
            assertNotNull(smsTool.getParameters());
            assertTrue(smsTool.getParameters().contains("phoneNumbers"));
            assertTrue(smsTool.getParameters().contains("templateCode"));
            assertTrue(smsTool.getParameters().contains("templateParam"));
            assertTrue(smsTool.getParameters().contains("signName"));
        }
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("测试默认配置")
        void testDefaultConfiguration() {
            AliyunSmsTool defaultTool = new AliyunSmsTool();
            assertNotNull(defaultTool);
            assertEquals("aliyun_sms", defaultTool.getName());
        }

        @Test
        @DisplayName("测试自定义配置")
        void testCustomConfiguration() {
            SmsConfiguration customConfig = new SmsConfiguration();
            AliyunSmsTool customTool = new AliyunSmsTool(customConfig);
            assertNotNull(customTool);
            assertEquals("aliyun_sms", customTool.getName());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("测试最大长度手机号列表")
        void testMaximumLengthPhoneNumberList() {
            StringBuilder phoneList = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                if (i > 0) phoneList.append(",");
                phoneList.append("1380013800").append(String.format("%02d", i));
            }

            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"" + phoneList.toString() + "\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试最大长度模板参数")
        void testMaximumLengthTemplateParams() {
            StringBuilder templateParams = new StringBuilder("{\"");
            for (int i = 0; i < 100; i++) {
                if (i > 0) templateParams.append(",\"");
                templateParams.append("param").append(i).append("\":\"value").append(i).append("\"");
            }
            templateParams.append("}");

            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"templateParam\": \"" + templateParams.toString() + "\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";

            ToolExecuteResult result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }
}
