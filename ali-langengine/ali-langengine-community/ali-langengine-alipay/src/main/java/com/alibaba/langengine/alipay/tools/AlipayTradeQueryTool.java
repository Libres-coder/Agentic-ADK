package com.alibaba.langengine.alipay.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.alipay.AlipayConfiguration;
import com.alibaba.langengine.alipay.service.AlipayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class AlipayTradeQueryTool extends BaseTool {
    
    private final AlipayService alipayService;
    
    public AlipayTradeQueryTool() {
        this(new AlipayConfiguration());
    }
    
    public AlipayTradeQueryTool(AlipayConfiguration config) {
        this.alipayService = new AlipayService(config);
        setName("alipay_trade_query");
        setHumanName("支付宝交易查询工具");
        setDescription("查询支付宝交易状态和详情");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"outTradeNo\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"商户订单号\"\n" +
                "    },\n" +
                "    \"tradeNo\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"支付宝交易号（可选）\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"outTradeNo\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            String outTradeNo = (String) params.get("outTradeNo");
            
            if (StringUtils.isBlank(outTradeNo)) {
                return new ToolExecuteResult("错误：商户订单号不能为空");
            }
            
            var response = alipayService.queryTrade(outTradeNo);
            
            if ("10000".equals(response.getCode())) {
                StringBuilder result = new StringBuilder();
                result.append("交易查询成功：\n");
                result.append("商户订单号：").append(response.getOutTradeNo()).append("\n");
                result.append("支付宝交易号：").append(response.getTradeNo()).append("\n");
                result.append("交易状态：").append(response.getTradeStatus()).append("\n");
                result.append("订单金额：").append(response.getTotalAmount()).append("\n");
                result.append("实收金额：").append(response.getReceiptAmount()).append("\n");
                result.append("买家付款金额：").append(response.getBuyerPayAmount()).append("\n");
                result.append("买家用户ID：").append(response.getBuyerUserId()).append("\n");
                result.append("买家登录账号：").append(response.getBuyerLogonId()).append("\n");
                
                ToolExecuteResult toolResult = new ToolExecuteResult(result.toString());
                onToolEnd(this, toolInput, toolResult, executionContext);
                return toolResult;
            } else {
                String errorMsg = "交易查询失败：" + response.getMsg();
                ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
                onToolError(this, new RuntimeException(errorMsg), executionContext);
                return errorResult;
            }
            
        } catch (Exception e) {
            String errorMsg = "交易查询失败：" + e.getMessage();
            log.error(errorMsg, e);
            ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
            onToolError(this, e, executionContext);
            return errorResult;
        }
    }
}
