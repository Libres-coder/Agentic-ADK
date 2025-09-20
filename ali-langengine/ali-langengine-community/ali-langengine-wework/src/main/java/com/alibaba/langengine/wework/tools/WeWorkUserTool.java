package com.alibaba.langengine.wework.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.wework.WeWorkConfiguration;
import com.alibaba.langengine.wework.service.WeWorkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class WeWorkUserTool extends BaseTool {
    
    private final WeWorkService weWorkService;
    
    public WeWorkUserTool() {
        this(new WeWorkConfiguration());
    }
    
    public WeWorkUserTool(WeWorkConfiguration config) {
        this.weWorkService = new WeWorkService(config);
        setName("wework_user");
        setHumanName("企业微信用户工具");
        setDescription("查询企业微信用户信息");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"userid\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"用户ID\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"userid\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            String userid = (String) params.get("userid");
            
            if (StringUtils.isBlank(userid)) {
                return new ToolExecuteResult("错误：用户ID不能为空");
            }
            
            var response = weWorkService.getUserInfo(userid);
            
            if (response.getErrcode() == 0) {
                StringBuilder result = new StringBuilder();
                result.append("用户信息：\n");
                result.append("用户ID：").append(response.getUserid()).append("\n");
                result.append("姓名：").append(response.getName()).append("\n");
                result.append("手机号：").append(response.getMobile()).append("\n");
                result.append("邮箱：").append(response.getEmail()).append("\n");
                result.append("职位：").append(response.getPosition()).append("\n");
                result.append("头像：").append(response.getAvatar()).append("\n");
                result.append("部门：").append(response.getDepartment()).append("\n");
                result.append("状态：").append(response.getStatus()).append("\n");
                
                ToolExecuteResult toolResult = new ToolExecuteResult(result.toString());
                onToolEnd(this, toolInput, toolResult, executionContext);
                return toolResult;
            } else {
                String errorMsg = "获取用户信息失败：" + response.getErrmsg();
                ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
                onToolError(this, new RuntimeException(errorMsg), executionContext);
                return errorResult;
            }
            
        } catch (Exception e) {
            String errorMsg = "查询用户信息失败：" + e.getMessage();
            log.error(errorMsg, e);
            ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
            onToolError(this, e, executionContext);
            return errorResult;
        }
    }
}
