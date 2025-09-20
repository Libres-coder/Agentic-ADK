package com.alibaba.langengine.dingtalk.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.dingtalk.DingTalkConfiguration;
import com.alibaba.langengine.dingtalk.service.DingTalkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class DingTalkUserTool extends BaseTool {
    
    private final DingTalkService dingTalkService;
    
    public DingTalkUserTool() {
        this(new DingTalkConfiguration());
    }
    
    public DingTalkUserTool(DingTalkConfiguration config) {
        this.dingTalkService = new DingTalkService(config);
        setName("dingtalk_user");
        setHumanName("钉钉用户工具");
        setDescription("查询钉钉用户信息");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"userId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"用户ID\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"userId\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            String userId = (String) params.get("userId");
            
            if (StringUtils.isBlank(userId)) {
                return new ToolExecuteResult("错误：用户ID不能为空");
            }
            
            var response = dingTalkService.getUserInfo(userId);
            
            if (response.getErrcode() == 0 && response.getResult() != null) {
                var userInfo = response.getResult();
                StringBuilder result = new StringBuilder();
                result.append("用户信息：\n");
                result.append("用户ID：").append(userInfo.getUserid()).append("\n");
                result.append("姓名：").append(userInfo.getName()).append("\n");
                result.append("手机号：").append(userInfo.getMobile()).append("\n");
                result.append("邮箱：").append(userInfo.getEmail()).append("\n");
                result.append("职位：").append(userInfo.getPosition()).append("\n");
                result.append("工号：").append(userInfo.getJobnumber()).append("\n");
                result.append("部门ID：").append(userInfo.getDeptIdList()).append("\n");
                result.append("工作地点：").append(userInfo.getWorkPlace()).append("\n");
                result.append("备注：").append(userInfo.getRemark()).append("\n");
                
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
