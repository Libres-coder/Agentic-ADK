package com.alibaba.langengine.sms;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.sms.tools.AliyunSmsTool;
import com.alibaba.langengine.sms.tools.HuaweiSmsTool;
import com.alibaba.langengine.sms.tools.TencentSmsTool;

import java.util.ArrayList;
import java.util.List;

public class SmsToolFactory {
    
    private final SmsConfiguration config;
    
    public SmsToolFactory() {
        this.config = new SmsConfiguration();
    }
    
    public SmsToolFactory(SmsConfiguration config) {
        this.config = config;
    }
    
    public AliyunSmsTool createAliyunSmsTool() {
        return new AliyunSmsTool(config);
    }
    
    public TencentSmsTool createTencentSmsTool() {
        return new TencentSmsTool(config);
    }
    
    public HuaweiSmsTool createHuaweiSmsTool() {
        return new HuaweiSmsTool(config);
    }
    
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createAliyunSmsTool());
        tools.add(createTencentSmsTool());
        tools.add(createHuaweiSmsTool());
        return tools;
    }
}
