package com.alibaba.langengine.alipay.examples;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.alipay.AlipayConfiguration;
import com.alibaba.langengine.alipay.AlipayToolFactory;
import com.alibaba.langengine.alipay.tools.AlipayBillQueryTool;
import com.alibaba.langengine.alipay.tools.AlipayTradeQueryTool;
import com.alibaba.langengine.alipay.tools.AlipayTransferTool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AlipayToolsExample {
    
    public static void main(String[] args) {
        // 创建配置
        AlipayConfiguration config = new AlipayConfiguration(
            "your_app_id",
            "your_private_key", 
            "your_public_key"
        );
        
        // 创建工具工厂
        AlipayToolFactory factory = new AlipayToolFactory(config);
        
        // 示例1: 查询交易状态
        queryTradeExample(factory);
        
        // 示例2: 查询账单
        queryBillExample(factory);
        
        // 示例3: 转账
        transferExample(factory);
        
        // 示例4: 使用所有工具
        useAllToolsExample(factory);
    }
    
    /**
     * 查询交易状态示例
     */
    private static void queryTradeExample(AlipayToolFactory factory) {
        log.info("=== 支付宝交易查询示例 ===");
        
        AlipayTradeQueryTool tradeQueryTool = factory.createTradeQueryTool();
        
        String toolInput = "{\n" +
                "  \"outTradeNo\": \"2024010100000000000000000000000000\"\n" +
                "}";
        
        ToolExecuteResult result = tradeQueryTool.run(toolInput, null);
        log.info("查询结果: {}", result.getResult());
    }
    
    /**
     * 查询账单示例
     */
    private static void queryBillExample(AlipayToolFactory factory) {
        log.info("=== 支付宝账单查询示例 ===");
        
        AlipayBillQueryTool billQueryTool = factory.createBillQueryTool();
        
        String toolInput = "{\n" +
                "  \"billType\": \"trade\",\n" +
                "  \"billDate\": \"2024-01-01\"\n" +
                "}";
        
        ToolExecuteResult result = billQueryTool.run(toolInput, null);
        log.info("查询结果: {}", result.getResult());
    }
    
    /**
     * 转账示例
     */
    private static void transferExample(AlipayToolFactory factory) {
        log.info("=== 支付宝转账示例 ===");
        
        AlipayTransferTool transferTool = factory.createTransferTool();
        
        String toolInput = "{\n" +
                "  \"payeeAccount\": \"example@alipay.com\",\n" +
                "  \"amount\": \"100.00\",\n" +
                "  \"remark\": \"测试转账\",\n" +
                "  \"payeeRealName\": \"张三\"\n" +
                "}";
        
        ToolExecuteResult result = transferTool.run(toolInput, null);
        log.info("转账结果: {}", result.getResult());
    }
    
    /**
     * 使用所有工具示例
     */
    private static void useAllToolsExample(AlipayToolFactory factory) {
        log.info("=== 支付宝所有工具使用示例 ===");
        
        List<BaseTool> tools = factory.getAllTools();
        
        for (BaseTool tool : tools) {
            log.info("工具: {} - {}", tool.getName(), tool.getDescription());
        }
    }
}
