package com.alibaba.langengine.wecom;

import com.alibaba.langengine.wecom.tools.WeComMessageTool;
import com.alibaba.langengine.wecom.tools.WeComContactTool;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class BasicTest {

    private WeComConfiguration configuration;

    @Before
    public void setUp() {
        // 创建测试配置
        configuration = new WeComConfiguration();
        configuration.setCorpId("test_corp_id");
        configuration.setCorpSecret("test_corp_secret");
        configuration.setAgentId("1000001");
    }

    @Test
    public void testConfigurationCreation() {
        assertNotNull("配置应该创建成功", configuration);
        assertEquals("test_corp_id", configuration.getCorpId());
        assertEquals("test_corp_secret", configuration.getCorpSecret());
        assertEquals("1000001", configuration.getAgentId());
    }

    @Test
    public void testConfigurationValidation() {
        WeComConfiguration validConfig = new WeComConfiguration();
        validConfig.setCorpId("test_id");
        validConfig.setCorpSecret("test_secret");
        validConfig.setAgentId("123456");
        
        assertTrue("有效配置应该通过验证", validConfig.isValid());
        
        WeComConfiguration invalidConfig = new WeComConfiguration();
        // 不设置任何字段
        
        assertFalse("无效配置应该不通过验证", invalidConfig.isValid());
    }

    @Test
    public void testToolCreation() {
        WeComMessageTool messageTool = new WeComMessageTool(configuration);
        WeComContactTool contactTool = new WeComContactTool(configuration);
        
        assertNotNull("消息工具应该创建成功", messageTool);
        assertNotNull("通讯录工具应该创建成功", contactTool);
        
        assertTrue("消息工具名称应该正确", messageTool.getName().contains("Message"));
        assertTrue("通讯录工具名称应该正确", contactTool.getName().contains("Contact"));
    }
}
