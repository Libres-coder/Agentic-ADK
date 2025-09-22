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
public class AlipayBillQueryTool extends BaseTool {
    
    private final AlipayService alipayService;
    
    public AlipayBillQueryTool() {
        this(new AlipayConfiguration());
    }
    
    public AlipayBillQueryTool(AlipayConfiguration config) {
        this.alipayService = new AlipayService(config);
        setName("alipay_bill_query");
        setHumanName("支付宝账单查询工具");
        setDescription("查询支付宝账单下载链接");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"billType\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"enum\": [\"trade\", \"signcustomer\"],\n" +
                "      \"description\": \"账单类型：trade-交易账单，signcustomer-签约客户账单\"\n" +
                "    },\n" +
                "    \"billDate\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"账单日期，格式：yyyy-MM-dd\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"billType\", \"billDate\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            String billType = (String) params.get("billType");
            String billDate = (String) params.get("billDate");
            
            if (StringUtils.isBlank(billType) || StringUtils.isBlank(billDate)) {
                return new ToolExecuteResult("错误：账单类型和账单日期不能为空");
            }
            
            var response = alipayService.queryBill(billType, billDate);
            
            if ("10000".equals(response.getCode())) {
                StringBuilder result = new StringBuilder();
                result.append("账单查询成功：\n");
                result.append("账单类型：").append(billType).append("\n");
                result.append("账单日期：").append(billDate).append("\n");
                result.append("下载链接：").append(response.getBillDownloadUrl()).append("\n");
                result.append("账单大小：").append(response.getBillSize()).append("\n");
                
                ToolExecuteResult toolResult = new ToolExecuteResult(result.toString());
                onToolEnd(this, toolInput, toolResult, executionContext);
                return toolResult;
            } else {
                String errorMsg = "账单查询失败：" + response.getMsg();
                ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
                onToolError(this, new RuntimeException(errorMsg), executionContext);
                return errorResult;
            }
            
        } catch (Exception e) {
            String errorMsg = "账单查询失败：" + e.getMessage();
            log.error(errorMsg, e);
            ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
            onToolError(this, e, executionContext);
            return errorResult;
        }
    }
}
