package com.alibaba.langengine.alipay;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.alipay.tools.AlipayBillQueryTool;
import com.alibaba.langengine.alipay.tools.AlipayTradeQueryTool;
import com.alibaba.langengine.alipay.tools.AlipayTransferTool;

import java.util.ArrayList;
import java.util.List;

public class AlipayToolFactory {
    
    private final AlipayConfiguration config;
    
    public AlipayToolFactory() {
        this.config = new AlipayConfiguration();
    }
    
    public AlipayToolFactory(AlipayConfiguration config) {
        this.config = config;
    }
    
    public AlipayTradeQueryTool createTradeQueryTool() {
        return new AlipayTradeQueryTool(config);
    }
    
    public AlipayBillQueryTool createBillQueryTool() {
        return new AlipayBillQueryTool(config);
    }
    
    public AlipayTransferTool createTransferTool() {
        return new AlipayTransferTool(config);
    }
    
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createTradeQueryTool());
        tools.add(createBillQueryTool());
        tools.add(createTransferTool());
        return tools;
    }
}
