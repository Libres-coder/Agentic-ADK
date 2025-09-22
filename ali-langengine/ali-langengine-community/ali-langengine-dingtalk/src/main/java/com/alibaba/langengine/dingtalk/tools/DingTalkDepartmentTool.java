package com.alibaba.langengine.dingtalk.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.dingtalk.DingTalkConfiguration;
import com.alibaba.langengine.dingtalk.service.DingTalkService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DingTalkDepartmentTool extends BaseTool {
    
    private final DingTalkService dingTalkService;
    
    public DingTalkDepartmentTool() {
        this(new DingTalkConfiguration());
    }
    
    public DingTalkDepartmentTool(DingTalkConfiguration config) {
        this.dingTalkService = new DingTalkService(config);
        setName("dingtalk_department");
        setHumanName("钉钉部门工具");
        setDescription("查询钉钉部门列表");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"deptId\": {\n" +
                "      \"type\": \"number\",\n" +
                "      \"description\": \"部门ID，不传则查询根部门\"\n" +
                "    }\n" +
                "  }\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            Long deptId = params.get("deptId") != null ? 
                Long.valueOf(params.get("deptId").toString()) : 1L;
            
            var response = dingTalkService.getDepartmentList(deptId);
            
            if (response.getErrcode() == 0 && response.getResult() != null) {
                StringBuilder result = new StringBuilder();
                result.append("部门列表：\n");
                
                for (var dept : response.getResult()) {
                    result.append("部门ID：").append(dept.getDeptId()).append("\n");
                    result.append("部门名称：").append(dept.getName()).append("\n");
                    result.append("父部门ID：").append(dept.getParentId()).append("\n");
                    result.append("排序：").append(dept.getOrder()).append("\n");
                    result.append("---\n");
                }
                
                ToolExecuteResult toolResult = new ToolExecuteResult(result.toString());
                onToolEnd(this, toolInput, toolResult, executionContext);
                return toolResult;
            } else {
                String errorMsg = "获取部门列表失败：" + response.getErrmsg();
                ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
                onToolError(this, new RuntimeException(errorMsg), executionContext);
                return errorResult;
            }
            
        } catch (Exception e) {
            String errorMsg = "查询部门列表失败：" + e.getMessage();
            log.error(errorMsg, e);
            ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
            onToolError(this, e, executionContext);
            return errorResult;
        }
    }
}
