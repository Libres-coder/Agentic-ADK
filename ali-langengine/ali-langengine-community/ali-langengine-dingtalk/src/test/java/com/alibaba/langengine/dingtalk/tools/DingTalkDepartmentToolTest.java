package com.alibaba.langengine.dingtalk.tools;

import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.dingtalk.DingTalkConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("钉钉部门工具测试")
class DingTalkDepartmentToolTest {

    private DingTalkDepartmentTool departmentTool;
    private DingTalkConfiguration config;

    @BeforeEach
    void setUp() {
        config = new DingTalkConfiguration("test_app_key", "test_app_secret", "test_agent_id", "test_corp_id");
        departmentTool = new DingTalkDepartmentTool(config);
    }

    @Nested
    @DisplayName("部门查询测试")
    class DepartmentQueryTests {

        @Test
        @DisplayName("查询根部门")
        void testQueryRootDepartment() {
            String toolInput = "{\n" +
                    "  \"deptId\": 1\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询指定部门ID")
        void testQuerySpecificDepartment() {
            String toolInput = "{\n" +
                    "  \"deptId\": 123\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询大数值部门ID")
        void testQueryLargeDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": 999999999\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询零部门ID")
        void testQueryZeroDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": 0\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("查询负数部门ID")
        void testQueryNegativeDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": -1\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ParameterValidationTests {

        @Test
        @DisplayName("测试缺少部门ID参数")
        void testMissingDepartmentId() {
            String toolInput = "{}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试null部门ID")
        void testNullDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": null\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试字符串部门ID")
        void testStringDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": \"123\"\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试浮点数部门ID")
        void testFloatDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": 123.45\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
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
                    "  \"deptId\": 123\n" +
                    "  // 缺少逗号\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(invalidJson, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试空输入")
        void testEmptyInput() {
            ToolExecuteResult result = departmentTool.run("", null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试null输入")
        void testNullInput() {
            ToolExecuteResult result = departmentTool.run(null, null);
            assertNotNull(result);
            assertTrue(result.getResult().contains("错误"));
        }

        @Test
        @DisplayName("测试非JSON输入")
        void testNonJsonInput() {
            String nonJsonInput = "这不是JSON格式的输入";
            ToolExecuteResult result = departmentTool.run(nonJsonInput, null);
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
            assertEquals("dingtalk_department", departmentTool.getName());
        }

        @Test
        @DisplayName("测试工具中文名称")
        void testToolHumanName() {
            assertEquals("钉钉部门工具", departmentTool.getHumanName());
        }

        @Test
        @DisplayName("测试工具描述")
        void testToolDescription() {
            assertNotNull(departmentTool.getDescription());
            assertTrue(departmentTool.getDescription().contains("钉钉部门"));
        }

        @Test
        @DisplayName("测试工具参数")
        void testToolParameters() {
            assertNotNull(departmentTool.getParameters());
            assertTrue(departmentTool.getParameters().contains("deptId"));
        }
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("测试默认配置")
        void testDefaultConfiguration() {
            DingTalkDepartmentTool defaultTool = new DingTalkDepartmentTool();
            assertNotNull(defaultTool);
            assertEquals("dingtalk_department", defaultTool.getName());
        }

        @Test
        @DisplayName("测试自定义配置")
        void testCustomConfiguration() {
            DingTalkConfiguration customConfig = new DingTalkConfiguration(
                    "custom_app_key", "custom_app_secret", "custom_agent_id", "custom_corp_id");
            DingTalkDepartmentTool customTool = new DingTalkDepartmentTool(customConfig);
            assertNotNull(customTool);
            assertEquals("dingtalk_department", customTool.getName());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("测试Integer最大值部门ID")
        void testMaxIntegerDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": " + Integer.MAX_VALUE + "\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试Integer最小值部门ID")
        void testMinIntegerDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": " + Integer.MIN_VALUE + "\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试Long类型部门ID")
        void testLongDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": " + Long.MAX_VALUE + "\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("特殊值测试")
    class SpecialValueTests {

        @Test
        @DisplayName("测试布尔值部门ID")
        void testBooleanDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": true\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试数组部门ID")
        void testArrayDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": [1, 2, 3]\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }

        @Test
        @DisplayName("测试对象部门ID")
        void testObjectDepartmentId() {
            String toolInput = "{\n" +
                    "  \"deptId\": {\"id\": 123}\n" +
                    "}";

            ToolExecuteResult result = departmentTool.run(toolInput, null);
            assertNotNull(result);
            assertNotNull(result.getResult());
        }
    }
}
