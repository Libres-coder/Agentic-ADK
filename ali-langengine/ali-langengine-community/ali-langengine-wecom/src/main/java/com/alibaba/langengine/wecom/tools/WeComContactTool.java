package com.alibaba.langengine.wecom.tools;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.wecom.WeComConfiguration;
import com.alibaba.langengine.wecom.client.WeComClient;
import com.alibaba.langengine.wecom.exception.WeComException;
import com.alibaba.langengine.wecom.model.WeComUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;


@EqualsAndHashCode(callSuper = true)
@Data
@Component
public class WeComContactTool extends BaseWeComTool {

    private WeComClient client;
    private WeComConfiguration configuration;

    public WeComContactTool() {
        setName("WeComContactTool");
        setDescription("企业微信通讯录管理工具，支持JSON格式的查询参数：{\"action\":\"getUserInfo\",\"userId\":\"用户ID\"}");
    }

    public WeComContactTool(WeComConfiguration configuration) {
        this();
        this.configuration = configuration;
        this.client = new WeComClient(configuration);
    }
    
    /**
     * 通讯录查询请求参数
     */
    public static class ContactRequest {
        private String action;      // 操作类型：getUserInfo, getDepartments等
        private String userId;      // 用户ID（获取用户信息时使用）
        private String departmentId; // 部门ID（获取部门信息时使用）
        
        // Getters and Setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getDepartmentId() { return departmentId; }
        public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
        
        public void validate() throws WeComException {
            if (action == null || action.trim().isEmpty()) {
                throw new WeComException("操作类型(action)不能为空");
            }
            
            switch (action.toLowerCase()) {
                case "getuserinfo":
                    if (userId == null || userId.trim().isEmpty()) {
                        throw new WeComException("获取用户信息时，用户ID不能为空");
                    }
                    break;
                case "getdepartments":
                    // 部门查询可选参数
                    break;
                default:
                    throw new WeComException("不支持的操作类型: " + action);
            }
        }
    }

    public WeComContactTool(WeComClient client) {
        this();
        this.client = client;
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        try {
            // 解析输入参数
            ContactRequest request = parseInput(toolInput, ContactRequest.class);
            request.validate();
            
            // 根据操作类型执行相应功能
            return executeAction(request);
            
        } catch (Exception e) {
            return handleError(e);
        }
    }
    
    @Override
    protected String getToolName() {
        return "WeComContactTool";
    }
    
    /**
     * 执行具体的操作
     */
    private ToolExecuteResult executeAction(ContactRequest request) throws WeComException {
        switch (request.getAction().toLowerCase()) {
            case "getuserinfo":
                return getUserInfo(request.getUserId());
            case "getdepartments":
                return getDepartments(request.getDepartmentId());
            default:
                throw new WeComException("不支持的操作类型: " + request.getAction());
        }
    }

    /**
     * 获取用户信息
     */
    private ToolExecuteResult getUserInfo(String userId) throws WeComException {
        validateRequired(userId, "userId");
        
        WeComUser user = client.getUserInfo(userId);
        if (user != null) {
            return handleSuccess(user);
        } else {
            throw new WeComException("用户不存在或查询失败: " + userId);
        }
    }
    
    /**
     * 获取部门信息
     */
    private ToolExecuteResult getDepartments(String departmentId) throws WeComException {
        // TODO: 实现部门信息查询
        throw new WeComException("部门信息查询功能暂未实现");
    }
}
