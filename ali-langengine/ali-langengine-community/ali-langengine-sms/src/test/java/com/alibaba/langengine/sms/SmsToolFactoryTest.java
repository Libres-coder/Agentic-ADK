package com.alibaba.langengine.sms;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.sms.tools.AliyunSmsTool;
import com.alibaba.langengine.sms.tools.HuaweiSmsTool;
import com.alibaba.langengine.sms.tools.TencentSmsTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("短信服务工具工厂测试")
class SmsToolFactoryTest {

    private SmsToolFactory factory;
    private SmsConfiguration config;

    @BeforeEach
    void setUp() {
        config = new SmsConfiguration();
        factory = new SmsToolFactory(config);
    }

    @Nested
    @DisplayName("工具创建测试")
    class ToolCreationTests {

        @Test
        @DisplayName("创建阿里云短信工具")
        void testCreateAliyunSmsTool() {
            AliyunSmsTool smsTool = factory.createAliyunSmsTool();
            assertNotNull(smsTool);
            assertEquals("aliyun_sms", smsTool.getName());
            assertEquals("阿里云短信工具", smsTool.getHumanName());
        }

        @Test
        @DisplayName("创建腾讯云短信工具")
        void testCreateTencentSmsTool() {
            TencentSmsTool smsTool = factory.createTencentSmsTool();
            assertNotNull(smsTool);
            assertEquals("tencent_sms", smsTool.getName());
            assertEquals("腾讯云短信工具", smsTool.getHumanName());
        }

        @Test
        @DisplayName("创建华为云短信工具")
        void testCreateHuaweiSmsTool() {
            HuaweiSmsTool smsTool = factory.createHuaweiSmsTool();
            assertNotNull(smsTool);
            assertEquals("huawei_sms", smsTool.getName());
            assertEquals("华为云短信工具", smsTool.getHumanName());
        }

        @Test
        @DisplayName("创建所有工具")
        void testGetAllTools() {
            List<BaseTool> tools = factory.getAllTools();
            assertNotNull(tools);
            assertEquals(3, tools.size());
            
            // 验证工具类型
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof AliyunSmsTool));
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof TencentSmsTool));
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof HuaweiSmsTool));
        }
    }

    @Nested
    @DisplayName("默认配置测试")
    class DefaultConfigurationTests {

        @Test
        @DisplayName("测试默认工厂")
        void testDefaultFactory() {
            SmsToolFactory defaultFactory = new SmsToolFactory();
            assertNotNull(defaultFactory);
            
            AliyunSmsTool smsTool = defaultFactory.createAliyunSmsTool();
            assertNotNull(smsTool);
            assertEquals("aliyun_sms", smsTool.getName());
        }

        @Test
        @DisplayName("测试默认工厂创建所有工具")
        void testDefaultFactoryGetAllTools() {
            SmsToolFactory defaultFactory = new SmsToolFactory();
            List<BaseTool> tools = defaultFactory.getAllTools();
            
            assertNotNull(tools);
            assertEquals(3, tools.size());
        }
    }

    @Nested
    @DisplayName("工具功能测试")
    class ToolFunctionalityTests {

        @Test
        @DisplayName("测试阿里云短信工具功能")
        void testAliyunSmsToolFunctionality() {
            AliyunSmsTool smsTool = factory.createAliyunSmsTool();
            
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"templateParam\": \"{\\\"code\\\":\\\"123456\\\"}\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";
            
            var result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试腾讯云短信工具功能")
        void testTencentSmsToolFunctionality() {
            TencentSmsTool smsTool = factory.createTencentSmsTool();
            
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateId\": \"123456\",\n" +
                    "  \"templateParam\": [\"123456\"],\n" +
                    "  \"signName\": \"腾讯云\"\n" +
                    "}";
            
            var result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试华为云短信工具功能")
        void testHuaweiSmsToolFunctionality() {
            HuaweiSmsTool smsTool = factory.createHuaweiSmsTool();
            
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateId\": \"123456\",\n" +
                    "  \"templateParam\": \"{\\\"code\\\":\\\"123456\\\"}\",\n" +
                    "  \"signName\": \"华为云\"\n" +
                    "}";
            
            var result = smsTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("工具属性验证测试")
    class ToolPropertiesValidationTests {

        @Test
        @DisplayName("验证阿里云短信工具属性")
        void testAliyunSmsToolProperties() {
            AliyunSmsTool smsTool = factory.createAliyunSmsTool();
            
            assertNotNull(smsTool.getName());
            assertNotNull(smsTool.getHumanName());
            assertNotNull(smsTool.getDescription());
            assertNotNull(smsTool.getParameters());
            
            assertTrue(smsTool.getName().contains("aliyun"));
            assertTrue(smsTool.getHumanName().contains("阿里云"));
            assertTrue(smsTool.getDescription().contains("短信"));
        }

        @Test
        @DisplayName("验证腾讯云短信工具属性")
        void testTencentSmsToolProperties() {
            TencentSmsTool smsTool = factory.createTencentSmsTool();
            
            assertNotNull(smsTool.getName());
            assertNotNull(smsTool.getHumanName());
            assertNotNull(smsTool.getDescription());
            assertNotNull(smsTool.getParameters());
            
            assertTrue(smsTool.getName().contains("tencent"));
            assertTrue(smsTool.getHumanName().contains("腾讯云"));
            assertTrue(smsTool.getDescription().contains("短信"));
        }

        @Test
        @DisplayName("验证华为云短信工具属性")
        void testHuaweiSmsToolProperties() {
            HuaweiSmsTool smsTool = factory.createHuaweiSmsTool();
            
            assertNotNull(smsTool.getName());
            assertNotNull(smsTool.getHumanName());
            assertNotNull(smsTool.getDescription());
            assertNotNull(smsTool.getParameters());
            
            assertTrue(smsTool.getName().contains("huawei"));
            assertTrue(smsTool.getHumanName().contains("华为云"));
            assertTrue(smsTool.getDescription().contains("短信"));
        }
    }

    @Nested
    @DisplayName("工具参数测试")
    class ToolParametersTests {

        @Test
        @DisplayName("测试阿里云短信工具参数")
        void testAliyunSmsToolParameters() {
            AliyunSmsTool smsTool = factory.createAliyunSmsTool();
            String parameters = smsTool.getParameters();
            
            assertTrue(parameters.contains("phoneNumbers"));
            assertTrue(parameters.contains("templateCode"));
            assertTrue(parameters.contains("templateParam"));
            assertTrue(parameters.contains("signName"));
        }

        @Test
        @DisplayName("测试腾讯云短信工具参数")
        void testTencentSmsToolParameters() {
            TencentSmsTool smsTool = factory.createTencentSmsTool();
            String parameters = smsTool.getParameters();
            
            assertTrue(parameters.contains("phoneNumbers"));
            assertTrue(parameters.contains("templateId"));
            assertTrue(parameters.contains("templateParam"));
            assertTrue(parameters.contains("signName"));
        }

        @Test
        @DisplayName("测试华为云短信工具参数")
        void testHuaweiSmsToolParameters() {
            HuaweiSmsTool smsTool = factory.createHuaweiSmsTool();
            String parameters = smsTool.getParameters();
            
            assertTrue(parameters.contains("phoneNumbers"));
            assertTrue(parameters.contains("templateId"));
            assertTrue(parameters.contains("templateParam"));
            assertTrue(parameters.contains("signName"));
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
            SmsConfiguration customConfig = new SmsConfiguration();
            SmsToolFactory customFactory = new SmsToolFactory(customConfig);
            
            AliyunSmsTool smsTool = customFactory.createAliyunSmsTool();
            assertNotNull(smsTool);
            
            // 验证工具能正常工作
            String toolInput = "{\n" +
                    "  \"phoneNumbers\": \"13800138000\",\n" +
                    "  \"templateCode\": \"SMS_123456789\",\n" +
                    "  \"signName\": \"阿里云\"\n" +
                    "}";
            
            var result = smsTool.run(toolInput, null);
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
            
            boolean hasAliyunTool = tools.stream()
                    .anyMatch(tool -> tool instanceof AliyunSmsTool);
            boolean hasTencentTool = tools.stream()
                    .anyMatch(tool -> tool instanceof TencentSmsTool);
            boolean hasHuaweiTool = tools.stream()
                    .anyMatch(tool -> tool instanceof HuaweiSmsTool);
            
            assertTrue(hasAliyunTool);
            assertTrue(hasTencentTool);
            assertTrue(hasHuaweiTool);
        }
    }

    @Nested
    @DisplayName("工具实例测试")
    class ToolInstanceTests {

        @Test
        @DisplayName("测试每次创建的工具实例不同")
        void testToolInstancesAreDifferent() {
            AliyunSmsTool tool1 = factory.createAliyunSmsTool();
            AliyunSmsTool tool2 = factory.createAliyunSmsTool();
            
            assertNotSame(tool1, tool2);
        }

        @Test
        @DisplayName("测试工具实例类型正确")
        void testToolInstanceTypes() {
            AliyunSmsTool aliyunTool = factory.createAliyunSmsTool();
            TencentSmsTool tencentTool = factory.createTencentSmsTool();
            HuaweiSmsTool huaweiTool = factory.createHuaweiSmsTool();
            
            assertTrue(aliyunTool instanceof AliyunSmsTool);
            assertTrue(tencentTool instanceof TencentSmsTool);
            assertTrue(huaweiTool instanceof HuaweiSmsTool);
        }
    }

    @Nested
    @DisplayName("工具功能对比测试")
    class ToolFunctionalityComparisonTests {

        @Test
        @DisplayName("测试不同云服务商工具参数差异")
        void testDifferentCloudProviderParameters() {
            AliyunSmsTool aliyunTool = factory.createAliyunSmsTool();
            TencentSmsTool tencentTool = factory.createTencentSmsTool();
            HuaweiSmsTool huaweiTool = factory.createHuaweiSmsTool();
            
            String aliyunParams = aliyunTool.getParameters();
            String tencentParams = tencentTool.getParameters();
            String huaweiParams = huaweiTool.getParameters();
            
            // 阿里云使用templateCode
            assertTrue(aliyunParams.contains("templateCode"));
            // 腾讯云和华为云使用templateId
            assertTrue(tencentParams.contains("templateId"));
            assertTrue(huaweiParams.contains("templateId"));
        }

        @Test
        @DisplayName("测试所有工具都支持基本参数")
        void testAllToolsSupportBasicParameters() {
            List<BaseTool> tools = factory.getAllTools();
            
            for (BaseTool tool : tools) {
                String parameters = tool.getParameters();
                assertTrue(parameters.contains("phoneNumbers"));
                assertTrue(parameters.contains("signName"));
            }
        }
    }
}
