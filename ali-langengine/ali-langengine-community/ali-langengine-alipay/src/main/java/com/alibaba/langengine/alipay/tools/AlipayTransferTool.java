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
public class AlipayTransferTool extends BaseTool {
    
    private final AlipayService alipayService;
    
    public AlipayTransferTool() {
        this(new AlipayConfiguration());
    }
    
    public AlipayTransferTool(AlipayConfiguration config) {
        this.alipayService = new AlipayService(config);
        setName("alipay_transfer");
        setHumanName("支付宝转账工具");
        setDescription("向支付宝账户转账");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"payeeAccount\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"收款方支付宝账号\"\n" +
                "    },\n" +
                "    \"amount\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"转账金额，单位：元\"\n" +
                "    },\n" +
                "    \"remark\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"转账备注\"\n" +
                "    },\n" +
                "    \"payeeRealName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"收款方真实姓名（可选）\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"payeeAccount\", \"amount\", \"remark\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            String payeeAccount = (String) params.get("payeeAccount");
            String amount = (String) params.get("amount");
            String remark = (String) params.get("remark");
            String payeeRealName = (String) params.get("payeeRealName");
            
            if (StringUtils.isBlank(payeeAccount) || StringUtils.isBlank(amount) || StringUtils.isBlank(remark)) {
                return new ToolExecuteResult("错误：收款账号、转账金额和备注不能为空");
            }
            
            var response = alipayService.transferToAccount(payeeAccount, amount, remark);
            
            if ("10000".equals(response.getCode())) {
                StringBuilder result = new StringBuilder();
                result.append("转账成功：\n");
                result.append("支付宝订单号：").append(response.getOrderId()).append("\n");
                result.append("商户订单号：").append(response.getOutBizNo()).append("\n");
                result.append("收款账号：").append(payeeAccount).append("\n");
                result.append("转账金额：").append(amount).append("元\n");
                result.append("转账备注：").append(remark).append("\n");
                result.append("转账状态：").append(response.getStatus()).append("\n");
                
                ToolExecuteResult toolResult = new ToolExecuteResult(result.toString());
                onToolEnd(this, toolInput, toolResult, executionContext);
                return toolResult;
            } else {
                String errorMsg = "转账失败：" + response.getMsg();
                ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
                onToolError(this, new RuntimeException(errorMsg), executionContext);
                return errorResult;
            }
            
        } catch (Exception e) {
            String errorMsg = "转账失败：" + e.getMessage();
            log.error(errorMsg, e);
            ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
            onToolError(this, e, executionContext);
            return errorResult;
        }
    }
}
