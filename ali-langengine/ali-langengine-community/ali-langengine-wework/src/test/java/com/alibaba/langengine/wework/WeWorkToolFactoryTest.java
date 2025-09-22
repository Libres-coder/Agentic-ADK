package com.alibaba.langengine.wework;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.wework.tools.WeWorkMessageTool;
import com.alibaba.langengine.wework.tools.WeWorkUserTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("企业微信工具工厂测试")
class WeWorkToolFactoryTest {

    private WeWorkToolFactory factory;
    private WeWorkConfiguration config;

    @BeforeEach
    void setUp() {
        config = new WeWorkConfiguration("test_corp_id", "test_corp_secret", "test_agent_id");
        factory = new WeWorkToolFactory(config);
    }

    @Nested
    @DisplayName("工具创建测试")
    class ToolCreationTests {

        @Test
        @DisplayName("创建消息工具")
        void testCreateMessageTool() {
            WeWorkMessageTool messageTool = factory.createMessageTool();
            assertNotNull(messageTool);
            assertEquals("wework_message", messageTool.getName());
            assertEquals("企业微信消息工具", messageTool.getHumanName());
        }

        @Test
        @DisplayName("创建用户工具")
        void testCreateUserTool() {
            WeWorkUserTool userTool = factory.createUserTool();
            assertNotNull(userTool);
            assertEquals("wework_user", userTool.getName());
            assertEquals("企业微信用户工具", userTool.getHumanName());
        }

        @Test
        @DisplayName("创建所有工具")
        void testGetAllTools() {
            List<BaseTool> tools = factory.getAllTools();
            assertNotNull(tools);
            assertEquals(2, tools.size());
            
            // 验证工具类型
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof WeWorkMessageTool));
            assertTrue(tools.stream().anyMatch(tool -> tool instanceof WeWorkUserTool));
        }
    }

    @Nested
    @DisplayName("默认配置测试")
    class DefaultConfigurationTests {

        @Test
        @DisplayName("测试默认工厂")
        void testDefaultFactory() {
            WeWorkToolFactory defaultFactory = new WeWorkToolFactory();
            assertNotNull(defaultFactory);
            
            WeWorkMessageTool messageTool = defaultFactory.createMessageTool();
            assertNotNull(messageTool);
            assertEquals("wework_message", messageTool.getName());
        }

        @Test
        @DisplayName("测试默认工厂创建所有工具")
        void testDefaultFactoryGetAllTools() {
            WeWorkToolFactory defaultFactory = new WeWorkToolFactory();
            List<BaseTool> tools = defaultFactory.getAllTools();
            
            assertNotNull(tools);
            assertEquals(2, tools.size());
        }
    }

    @Nested
    @DisplayName("工具功能测试")
    class ToolFunctionalityTests {

        @Test
        @DisplayName("测试消息工具功能")
        void testMessageToolFunctionality() {
            WeWorkMessageTool messageTool = factory.createMessageTool();
            
            String toolInput = "{\n" +
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "}";
            
            var result = messageTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试用户工具功能")
        void testUserToolFunctionality() {
            WeWorkUserTool userTool = factory.createUserTool();
            
            String toolInput = "{\n" +
                    "  \"userid\": \"user123\"\n" +
                    "}";
            
            var result = userTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("工具属性验证测试")
    class ToolPropertiesValidationTests {

        @Test
        @DisplayName("验证消息工具属性")
        void testMessageToolProperties() {
            WeWorkMessageTool messageTool = factory.createMessageTool();
            
            assertNotNull(messageTool.getName());
            assertNotNull(messageTool.getHumanName());
            assertNotNull(messageTool.getDescription());
            assertNotNull(messageTool.getParameters());
            
            assertTrue(messageTool.getName().contains("wework"));
            assertTrue(messageTool.getHumanName().contains("企业微信"));
            assertTrue(messageTool.getDescription().contains("消息"));
        }

        @Test
        @DisplayName("验证用户工具属性")
        void testUserToolProperties() {
            WeWorkUserTool userTool = factory.createUserTool();
            
            assertNotNull(userTool.getName());
            assertNotNull(userTool.getHumanName());
            assertNotNull(userTool.getDescription());
            assertNotNull(userTool.getParameters());
            
            assertTrue(userTool.getName().contains("wework"));
            assertTrue(userTool.getHumanName().contains("企业微信"));
            assertTrue(userTool.getDescription().contains("用户"));
        }
    }

    @Nested
    @DisplayName("工具参数测试")
    class ToolParametersTests {

        @Test
        @DisplayName("测试消息工具参数")
        void testMessageToolParameters() {
            WeWorkMessageTool messageTool = factory.createMessageTool();
            String parameters = messageTool.getParameters();
            
            assertTrue(parameters.contains("touser"));
            assertTrue(parameters.contains("content"));
        }

        @Test
        @DisplayName("测试用户工具参数")
        void testUserToolParameters() {
            WeWorkUserTool userTool = factory.createUserTool();
            String parameters = userTool.getParameters();
            
            assertTrue(parameters.contains("userid"));
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
            WeWorkConfiguration customConfig = new WeWorkConfiguration(
                    "custom_corp_id", "custom_corp_secret", "custom_agent_id");
            WeWorkToolFactory customFactory = new WeWorkToolFactory(customConfig);
            
            WeWorkMessageTool messageTool = customFactory.createMessageTool();
            assertNotNull(messageTool);
            
            // 验证工具能正常工作
            String toolInput = "{\n" +
                    "  \"touser\": \"user1\",\n" +
                    "  \"content\": \"测试消息\"\n" +
                    "}";
            
            var result = messageTool.run(toolInput, null);
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
            assertEquals(2, tools.size());
        }

        @Test
        @DisplayName("测试工具列表包含所有工具")
        void testToolListContainsAllTools() {
            List<BaseTool> tools = factory.getAllTools();
            
            boolean hasMessageTool = tools.stream()
                    .anyMatch(tool -> tool instanceof WeWorkMessageTool);
            boolean hasUserTool = tools.stream()
                    .anyMatch(tool -> tool instanceof WeWorkUserTool);
            
            assertTrue(hasMessageTool);
            assertTrue(hasUserTool);
        }
    }

    @Nested
    @DisplayName("工具实例测试")
    class ToolInstanceTests {

        @Test
        @DisplayName("测试每次创建的工具实例不同")
        void testToolInstancesAreDifferent() {
            WeWorkMessageTool tool1 = factory.createMessageTool();
            WeWorkMessageTool tool2 = factory.createMessageTool();
            
            assertNotSame(tool1, tool2);
        }

        @Test
        @DisplayName("测试工具实例类型正确")
        void testToolInstanceTypes() {
            WeWorkMessageTool messageTool = factory.createMessageTool();
            WeWorkUserTool userTool = factory.createUserTool();
            
            assertTrue(messageTool instanceof WeWorkMessageTool);
            assertTrue(userTool instanceof WeWorkUserTool);
        }
    }
}
