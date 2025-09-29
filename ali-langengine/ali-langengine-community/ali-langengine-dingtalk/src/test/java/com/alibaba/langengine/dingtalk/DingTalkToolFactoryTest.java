package com.alibaba.langengine.dingtalk;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.dingtalk.tools.DingTalkDepartmentTool;
import com.alibaba.langengine.dingtalk.tools.DingTalkMessageTool;
import com.alibaba.langengine.dingtalk.tools.DingTalkUserTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("钉钉工具工厂测试")
class DingTalkToolFactoryTest {

    private DingTalkToolFactory factory;
    private DingTalkConfiguration config;

    @BeforeEach
    void setUp() {
        config = new DingTalkConfiguration("test_app_key", "test_app_secret", "test_agent_id", "test_corp_id");
        factory = new DingTalkToolFactory(config);
    }

    @Nested
    @DisplayName("工具创建测试")
    class ToolCreationTests {

        @Test
        @DisplayName("创建消息工具")
        void testCreateMessageTool() {
            DingTalkMessageTool messageTool = factory.createMessageTool();
            assertNotNull(messageTool);
            assertEquals("dingtalk_message", messageTool.getName());
            assertEquals("钉钉消息工具", messageTool.getHumanName());
        }

        @Test
        @DisplayName("创建用户工具")
        void testCreateUserTool() {
            DingTalkUserTool userTool = factory.createUserTool();
            assertNotNull(userTool);
            assertEquals("dingtalk_user", userTool.getName());
            assertEquals("钉钉用户工具", userTool.getHumanName());
        }

        @Test
        @DisplayName("创建部门工具")
        void testCreateDepartmentTool() {
            DingTalkDepartmentTool departmentTool = factory.createDepartmentTool();
            assertNotNull(departmentTool);
            assertEquals("dingtalk_department", departmentTool.getName());
            assertEquals("钉钉部门工具", departmentTool.getHumanName());
        }

        @Test
        @DisplayName("创建所有工具")
        void testGetAllTools() {
            List<BaseTool> tools = factory.getAllTools();
            assertNotNull(tools);
            assertEquals(3, tools.size());
            
            // 验证工具类型
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof DingTalkMessageTool));
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof DingTalkUserTool));
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof DingTalkDepartmentTool));
        }
    }

    @Nested
    @DisplayName("默认配置测试")
    class DefaultConfigurationTests {

        @Test
        @DisplayName("测试默认工厂")
        void testDefaultFactory() {
            DingTalkToolFactory defaultFactory = new DingTalkToolFactory();
            assertNotNull(defaultFactory);
            
            DingTalkMessageTool messageTool = defaultFactory.createMessageTool();
            assertNotNull(messageTool);
            assertEquals("dingtalk_message", messageTool.getName());
        }

        @Test
        @DisplayName("测试默认工厂创建所有工具")
        void testDefaultFactoryGetAllTools() {
            DingTalkToolFactory defaultFactory = new DingTalkToolFactory();
            List<BaseTool> tools = defaultFactory.getAllTools();
            
            assertNotNull(tools);
            assertEquals(3, tools.size());
        }
    }

    @Nested
    @DisplayName("工具功能测试")
    class ToolFunctionalityTests {

        @Test
        @DisplayName("测试消息工具功能")
        void testMessageToolFunctionality() {
            DingTalkMessageTool messageTool = factory.createMessageTool();
            
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"text\",\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "}";
            
            var result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试用户工具功能")
        void testUserToolFunctionality() {
            DingTalkUserTool userTool = factory.createUserTool();
            
            String toolInput = "{\n" +
                    "  \"userId\": \"user123\"\n" +
                    "}";
            
            var result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }

        @Test
        @DisplayName("测试部门工具功能")
        void testDepartmentToolFunctionality() {
            DingTalkDepartmentTool departmentTool = factory.createDepartmentTool();
            
            String toolInput = "{\n" +
                    "  \"deptId\": 1\n" +
                    "}";
            
            var result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getOutput());
        }
    }

    @Nested
    @DisplayName("工具属性验证测试")
    class ToolPropertiesValidationTests {

        @Test
        @DisplayName("验证消息工具属性")
        void testMessageToolProperties() {
            DingTalkMessageTool messageTool = factory.createMessageTool();
            
            assertNotNull(messageTool.getName());
            assertNotNull(messageTool.getHumanName());
            assertNotNull(messageTool.getDescription());
            assertNotNull(messageTool.getParameters());
            
            assertTrue(messageTool.getName().contains("dingtalk"));
            assertTrue(messageTool.getHumanName().contains("钉钉"));
            assertTrue(messageTool.getDescription().contains("消息"));
        }

        @Test
        @DisplayName("验证用户工具属性")
        void testUserToolProperties() {
            DingTalkUserTool userTool = factory.createUserTool();
            
            assertNotNull(userTool.getName());
            assertNotNull(userTool.getHumanName());
            assertNotNull(userTool.getDescription());
            assertNotNull(userTool.getParameters());
            
            assertTrue(userTool.getName().contains("dingtalk"));
            assertTrue(userTool.getHumanName().contains("钉钉"));
            assertTrue(userTool.getDescription().contains("用户"));
        }

        @Test
        @DisplayName("验证部门工具属性")
        void testDepartmentToolProperties() {
            DingTalkDepartmentTool departmentTool = factory.createDepartmentTool();
            
            assertNotNull(departmentTool.getName());
            assertNotNull(departmentTool.getHumanName());
            assertNotNull(departmentTool.getDescription());
            assertNotNull(departmentTool.getParameters());
            
            assertTrue(departmentTool.getName().contains("dingtalk"));
            assertTrue(departmentTool.getHumanName().contains("钉钉"));
            assertTrue(departmentTool.getDescription().contains("部门"));
        }
    }

    @Nested
    @DisplayName("工具参数测试")
    class ToolParametersTests {

        @Test
        @DisplayName("测试消息工具参数")
        void testMessageToolParameters() {
            DingTalkMessageTool messageTool = factory.createMessageTool();
            String parameters = messageTool.getParameters();
            
            assertTrue(parameters.contains("userIds"));
            assertTrue(parameters.contains("messageType"));
            assertTrue(parameters.contains("content"));
            assertTrue(parameters.contains("title"));
            assertTrue(parameters.contains("messageUrl"));
            assertTrue(parameters.contains("picUrl"));
        }

        @Test
        @DisplayName("测试用户工具参数")
        void testUserToolParameters() {
            DingTalkUserTool userTool = factory.createUserTool();
            String parameters = userTool.getParameters();
            
            assertTrue(parameters.contains("userId"));
        }

        @Test
        @DisplayName("测试部门工具参数")
        void testDepartmentToolParameters() {
            DingTalkDepartmentTool departmentTool = factory.createDepartmentTool();
            String parameters = departmentTool.getParameters();
            
            assertTrue(parameters.contains("deptId"));
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
            DingTalkConfiguration customConfig = new DingTalkConfiguration(
                    "custom_app_key", "custom_app_secret", "custom_agent_id", "custom_corp_id");
            DingTalkToolFactory customFactory = new DingTalkToolFactory(customConfig);
            
            DingTalkMessageTool messageTool = customFactory.createMessageTool();
            assertNotNull(messageTool);
            
            // 验证工具能正常工作
            String toolInput = "{\n" +
                    "  \"userIds\": \"user1\",\n" +
                    "  \"messageType\": \"text\",\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "}";
            
            var result = messageTool.run(toolInput, null);
            assertNotNull(result);
        }
    }
}