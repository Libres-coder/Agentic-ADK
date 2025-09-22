package com.alibaba.langengine.alipay;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.alipay.tools.AlipayBillQueryTool;
import com.alibaba.langengine.alipay.tools.AlipayTradeQueryTool;
import com.alibaba.langengine.alipay.tools.AlipayTransferTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("支付宝工具工厂测试")
class AlipayToolFactoryTest {

    private AlipayToolFactory factory;
    private AlipayConfiguration config;

    @BeforeEach
    void setUp() {
        config = new AlipayConfiguration("test_app_id", "test_private_key", "test_public_key");
        factory = new AlipayToolFactory(config);
    }

    @Nested
    @DisplayName("工具创建测试")
    class ToolCreationTests {

        @Test
        @DisplayName("创建交易查询工具")
        void testCreateTradeQueryTool() {
            AlipayTradeQueryTool tradeQueryTool = factory.createTradeQueryTool();
            assertNotNull(tradeQueryTool);
            assertEquals("alipay_trade_query", tradeQueryTool.getName());
            assertEquals("支付宝交易查询工具", tradeQueryTool.getHumanName());
        }

        @Test
        @DisplayName("创建账单查询工具")
        void testCreateBillQueryTool() {
            AlipayBillQueryTool billQueryTool = factory.createBillQueryTool();
            assertNotNull(billQueryTool);
            assertEquals("alipay_bill_query", billQueryTool.getName());
            assertEquals("支付宝账单查询工具", billQueryTool.getHumanName());
        }

        @Test
        @DisplayName("创建转账工具")
        void testCreateTransferTool() {
            AlipayTransferTool transferTool = factory.createTransferTool();
            assertNotNull(transferTool);
            assertEquals("alipay_transfer", transferTool.getName());
            assertEquals("支付宝转账工具", transferTool.getHumanName());
        }

        @Test
        @DisplayName("创建所有工具")
        void testGetAllTools() {
            List<BaseTool> tools = factory.getAllTools();
            assertNotNull(tools);
            assertEquals(3, tools.size());
            
            // 验证工具类型
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof AlipayTradeQueryTool));
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof AlipayBillQueryTool));
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof AlipayTransferTool));
        }
    }

    @Nested
    @DisplayName("默认配置测试")
    class DefaultConfigurationTests {

        @Test
        @DisplayName("测试默认工厂")
        void testDefaultFactory() {
            AlipayToolFactory defaultFactory = new AlipayToolFactory();
            assertNotNull(defaultFactory);
            
            AlipayTradeQueryTool tradeQueryTool = defaultFactory.createTradeQueryTool();
            assertNotNull(tradeQueryTool);
            assertEquals("alipay_trade_query", tradeQueryTool.getName());
        }

        @Test
        @DisplayName("测试默认工厂创建所有工具")
        void testDefaultFactoryGetAllTools() {
            AlipayToolFactory defaultFactory = new AlipayToolFactory();
            List<BaseTool> tools = defaultFactory.getAllTools();
            
            assertNotNull(tools);
            assertEquals(3, tools.size());
        }
    }

    @Nested
    @DisplayName("工具功能测试")
    class ToolFunctionalityTests {

        @Test
        @DisplayName("测试交易查询工具功能")
        void testTradeQueryToolFunctionality() {
            AlipayTradeQueryTool tradeQueryTool = factory.createTradeQueryTool();
            
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"2024010100000000000000000000000000\"\n" +
                    "}";
            
            var result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试账单查询工具功能")
        void testBillQueryToolFunctionality() {
            AlipayBillQueryTool billQueryTool = factory.createBillQueryTool();
            
            String toolInput = "{\n" +
                    "  \"billType\": \"trade\",\n" +
                    "  \"billDate\": \"2024-01-01\"\n" +
                    "}";
            
            var result = billQueryTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试转账工具功能")
        void testTransferToolFunctionality() {
            AlipayTransferTool transferTool = factory.createTransferTool();
            
            String toolInput = "{\n" +
                    "  \"payeeAccount\": \"example@alipay.com\",\n" +
                    "  \"amount\": \"100.00\",\n" +
                    "  \"remark\": \"测试转账\"\n" +
                    "}";
            
            var result = transferTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("工具属性验证测试")
    class ToolPropertiesValidationTests {

        @Test
        @DisplayName("验证交易查询工具属性")
        void testTradeQueryToolProperties() {
            AlipayTradeQueryTool tradeQueryTool = factory.createTradeQueryTool();
            
            assertNotNull(tradeQueryTool.getName());
            assertNotNull(tradeQueryTool.getHumanName());
            assertNotNull(tradeQueryTool.getDescription());
            assertNotNull(tradeQueryTool.getParameters());
            
            assertTrue(tradeQueryTool.getName().contains("alipay"));
            assertTrue(tradeQueryTool.getHumanName().contains("支付宝"));
            assertTrue(tradeQueryTool.getDescription().contains("交易"));
        }

        @Test
        @DisplayName("验证账单查询工具属性")
        void testBillQueryToolProperties() {
            AlipayBillQueryTool billQueryTool = factory.createBillQueryTool();
            
            assertNotNull(billQueryTool.getName());
            assertNotNull(billQueryTool.getHumanName());
            assertNotNull(billQueryTool.getDescription());
            assertNotNull(billQueryTool.getParameters());
            
            assertTrue(billQueryTool.getName().contains("alipay"));
            assertTrue(billQueryTool.getHumanName().contains("支付宝"));
            assertTrue(billQueryTool.getDescription().contains("账单"));
        }

        @Test
        @DisplayName("验证转账工具属性")
        void testTransferToolProperties() {
            AlipayTransferTool transferTool = factory.createTransferTool();
            
            assertNotNull(transferTool.getName());
            assertNotNull(transferTool.getHumanName());
            assertNotNull(transferTool.getDescription());
            assertNotNull(transferTool.getParameters());
            
            assertTrue(transferTool.getName().contains("alipay"));
            assertTrue(transferTool.getHumanName().contains("支付宝"));
            assertTrue(transferTool.getDescription().contains("转账"));
        }
    }

    @Nested
    @DisplayName("工具参数测试")
    class ToolParametersTests {

        @Test
        @DisplayName("测试交易查询工具参数")
        void testTradeQueryToolParameters() {
            AlipayTradeQueryTool tradeQueryTool = factory.createTradeQueryTool();
            String parameters = tradeQueryTool.getParameters();
            
            assertTrue(parameters.contains("outTradeNo"));
            assertTrue(parameters.contains("tradeNo"));
        }

        @Test
        @DisplayName("测试账单查询工具参数")
        void testBillQueryToolParameters() {
            AlipayBillQueryTool billQueryTool = factory.createBillQueryTool();
            String parameters = billQueryTool.getParameters();
            
            assertTrue(parameters.contains("billType"));
            assertTrue(parameters.contains("billDate"));
        }

        @Test
        @DisplayName("测试转账工具参数")
        void testTransferToolParameters() {
            AlipayTransferTool transferTool = factory.createTransferTool();
            String parameters = transferTool.getParameters();
            
            assertTrue(parameters.contains("payeeAccount"));
            assertTrue(parameters.contains("amount"));
            assertTrue(parameters.contains("remark"));
            assertTrue(parameters.contains("payeeRealName"));
        }
    }

    @Nested
    @DisplayName("工具唯一性测试")
    class ToolUniquenessTests {

        @Test
        @DisplayName("测试工具名称唯一性")
        void testToolNameUniqueness() {
            List<BaseTool> tools = factory.getAllTools();
            
            long uniqueNames = tools.stream()
                    .map(BaseTool::getName)
                    .distinct()
                    .count();
            
            assertEquals(tools.size(), uniqueNames);
        }

        @Test
        @DisplayName("测试工具中文名称唯一性")
        void testToolHumanNameUniqueness() {
            List<BaseTool> tools = factory.getAllTools();
            
            long uniqueHumanNames = tools.stream()
                    .map(BaseTool::getHumanName)
                    .distinct()
                    .count();
            
            assertEquals(tools.size(), uniqueHumanNames);
        }
    }

    @Nested
    @DisplayName("配置传递测试")
    class ConfigurationPassingTests {

        @Test
        @DisplayName("测试配置是否正确传递")
        void testConfigurationPassing() {
            AlipayConfiguration customConfig = new AlipayConfiguration(
                    "custom_app_id", "custom_private_key", "custom_public_key");
            AlipayToolFactory customFactory = new AlipayToolFactory(customConfig);
            
            AlipayTradeQueryTool tradeQueryTool = customFactory.createTradeQueryTool();
            assertNotNull(tradeQueryTool);
            
            // 验证工具能正常工作
            String toolInput = "{\n" +
                    "  \"outTradeNo\": \"2024010100000000000000000000000000\"\n" +
                    "}";
            
            var result = tradeQueryTool.run(toolInput, null);
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("工具列表测试")
    class ToolListTests {

        @Test
        @DisplayName("测试工具列表不为空")
        void testToolListNotEmpty() {
            List<BaseTool> tools = factory.getAllTools();
            assertFalse(tools.isEmpty());
        }

        @Test
        @DisplayName("测试工具列表大小")
        void testToolListSize() {
            List<BaseTool> tools = factory.getAllTools();
            assertEquals(3, tools.size());
        }

        @Test
        @DisplayName("测试工具列表包含所有工具")
        void testToolListContainsAllTools() {
            List<BaseTool> tools = factory.getAllTools();
            
            boolean hasTradeQueryTool = tools.stream()
                    .anyMatch(tool -> tool instanceof AlipayTradeQueryTool);
            boolean hasBillQueryTool = tools.stream()
                    .anyMatch(tool -> tool instanceof AlipayBillQueryTool);
            boolean hasTransferTool = tools.stream()
                    .anyMatch(tool -> tool instanceof AlipayTransferTool);
            
            assertTrue(hasTradeQueryTool);
            assertTrue(hasBillQueryTool);
            assertTrue(hasTransferTool);
        }
    }

    @Nested
    @DisplayName("工具实例测试")
    class ToolInstanceTests {

        @Test
        @DisplayName("测试每次创建的工具实例不同")
        void testToolInstancesAreDifferent() {
            AlipayTradeQueryTool tool1 = factory.createTradeQueryTool();
            AlipayTradeQueryTool tool2 = factory.createTradeQueryTool();
            
            assertNotSame(tool1, tool2);
        }

        @Test
        @DisplayName("测试工具实例类型正确")
        void testToolInstanceTypes() {
            AlipayTradeQueryTool tradeQueryTool = factory.createTradeQueryTool();
            AlipayBillQueryTool billQueryTool = factory.createBillQueryTool();
            AlipayTransferTool transferTool = factory.createTransferTool();
            
            assertTrue(tradeQueryTool instanceof AlipayTradeQueryTool);
            assertTrue(billQueryTool instanceof AlipayBillQueryTool);
            assertTrue(transferTool instanceof AlipayTransferTool);
        }
    }

    @Nested
    @DisplayName("工具功能对比测试")
    class ToolFunctionalityComparisonTests {

        @Test
        @DisplayName("测试不同工具的参数差异")
        void testDifferentToolParameters() {
            AlipayTradeQueryTool tradeQueryTool = factory.createTradeQueryTool();
            AlipayBillQueryTool billQueryTool = factory.createBillQueryTool();
            AlipayTransferTool transferTool = factory.createTransferTool();
            
            String tradeParams = tradeQueryTool.getParameters();
            String billParams = billQueryTool.getParameters();
            String transferParams = transferTool.getParameters();
            
            // 交易查询工具特有参数
            assertTrue(tradeParams.contains("outTradeNo"));
            assertTrue(tradeParams.contains("tradeNo"));
            
            // 账单查询工具特有参数
            assertTrue(billParams.contains("billType"));
            assertTrue(billParams.contains("billDate"));
            
            // 转账工具特有参数
            assertTrue(transferParams.contains("payeeAccount"));
            assertTrue(transferParams.contains("amount"));
            assertTrue(transferParams.contains("remark"));
        }

        @Test
        @DisplayName("测试所有工具都包含支付宝相关描述")
        void testAllToolsContainAlipayDescription() {
            List<BaseTool> tools = factory.getAllTools();
            
            for (BaseTool tool : tools) {
                String description = tool.getDescription();
                assertTrue(description.contains("支付宝"));
            }
        }
    }
}
