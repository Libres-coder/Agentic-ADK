package com.alibaba.langengine.wecom.handler;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.wecom.exception.WeComException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class WeComExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(WeComExceptionHandler.class);
    
    /**
     * 处理企业微信异常
     * 
     * @param e 异常
     * @param operation 操作名称
     * @return 工具执行结果
     */
    public ToolExecuteResult handleWeComException(WeComException e, String operation) {
        log.error("企业微信操作失败: operation={}, errorCode={}", operation, e.getErrorCode(), e);
        
        String userMessage = getUserFriendlyMessage(e.getErrorCode(), operation);
        return new ToolExecuteResult(userMessage, true);
    }
    
    /**
     * 处理通用异常
     * 
     * @param e 异常
     * @param operation 操作名称
     * @return 工具执行结果
     */
    public ToolExecuteResult handleException(Exception e, String operation) {
        if (e instanceof WeComException) {
            return handleWeComException((WeComException) e, operation);
        }
        
        log.error("企业微信操作异常: operation={}", operation, e);
        return new ToolExecuteResult(operation + "失败: " + e.getMessage(), true);
    }
    
    /**
     * 根据错误码获取用户友好的错误消息
     * 
     * @param errorCode 错误码
     * @param operation 操作名称
     * @return 用户友好的错误消息
     */
    private String getUserFriendlyMessage(String errorCode, String operation) {
        int code;
        try {
            code = Integer.parseInt(errorCode);
        } catch (NumberFormatException e) {
            return "企业微信操作失败: " + errorCode;
        }
        return getUserFriendlyMessage(code, operation);
    }
    
    private String getUserFriendlyMessage(int errorCode, String operation) {
        switch (errorCode) {
            case 40001:
                return "企业微信应用密钥无效，请检查配置";
            case 40014:
                return "企业微信Access Token无效，请重新获取";
            case 42001:
                return "访问令牌已过期，系统正在自动刷新";
            case 43004:
                return "企业微信应用ID不存在";
            case 60011:
                return "部门不存在";
            case 60102:
                return "用户不存在";
            case 60103:
                return "手机号不存在";
            case 60104:
                return "用户未关注企业微信应用";
            case 60112:
                return "用户已被删除";
            case 90001:
                return "企业微信API调用频率超限，请稍后重试";
            case 90002:
                return "企业微信API每日调用量已用完";
            default:
                return operation + "失败，请稍后重试（错误码：" + errorCode + "）";
        }
    }
}
