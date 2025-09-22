package com.alibaba.langengine.wecom.tools;

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.wecom.exception.WeComException;
import com.alibaba.langengine.wecom.handler.WeComExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class BaseWeComTool extends DefaultTool {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseWeComTool.class);
    
    @Autowired
    protected WeComExceptionHandler exceptionHandler;
    
    protected final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 解析输入参数
     * 
     * @param input 输入字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 解析后的对象
     * @throws WeComException 解析失败时抛出异常
     */
    protected <T> T parseInput(String input, Class<T> clazz) throws WeComException {
        try {
            return objectMapper.readValue(input, clazz);
        } catch (JsonProcessingException e) {
            logger.error("输入参数解析失败: input={}, targetClass={}", input, clazz.getSimpleName(), e);
            throw new WeComException("输入参数格式错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理成功结果
     * 
     * @param result 结果对象
     * @return 工具执行结果
     */
    protected ToolExecuteResult handleSuccess(Object result) {
        try {
            String jsonResult = objectMapper.writeValueAsString(result);
            logger.debug("工具执行成功: result={}", jsonResult);
            return new ToolExecuteResult(jsonResult, false);
        } catch (JsonProcessingException e) {
            logger.error("结果序列化失败", e);
            return new ToolExecuteResult("操作成功，但结果序列化失败", false);
        }
    }
    
    /**
     * 处理简单成功结果
     * 
     * @param message 成功消息
     * @return 工具执行结果
     */
    protected ToolExecuteResult handleSuccess(String message) {
        logger.debug("工具执行成功: message={}", message);
        return new ToolExecuteResult(message, false);
    }
    
    /**
     * 处理异常
     * 
     * @param e 异常
     * @return 工具执行结果
     */
    protected ToolExecuteResult handleError(Exception e) {
        if (exceptionHandler != null) {
            return exceptionHandler.handleException(e, getToolName());
        }
        
        // 降级处理
        logger.error("工具执行失败: toolName={}", getToolName(), e);
        if (e instanceof WeComException) {
            WeComException weComException = (WeComException) e;
            return new ToolExecuteResult(
                String.format("操作失败: %s (错误码: %d)", weComException.getMessage(), weComException.getErrorCode()), 
                true
            );
        }
        return new ToolExecuteResult("操作失败: " + e.getMessage(), true);
    }
    
    /**
     * 获取工具名称（子类需要实现）
     * 
     * @return 工具名称
     */
    protected abstract String getToolName();
    
    /**
     * 验证必需参数
     * 
     * @param value 参数值
     * @param paramName 参数名称
     * @throws WeComException 参数无效时抛出异常
     */
    protected void validateRequired(String value, String paramName) throws WeComException {
        if (value == null || value.trim().isEmpty()) {
            throw new WeComException("必需参数 '" + paramName + "' 不能为空");
        }
    }
    
    /**
     * 验证参数长度
     * 
     * @param value 参数值
     * @param paramName 参数名称
     * @param maxLength 最大长度
     * @throws WeComException 参数长度超限时抛出异常
     */
    protected void validateLength(String value, String paramName, int maxLength) throws WeComException {
        if (value != null && value.length() > maxLength) {
            throw new WeComException("参数 '" + paramName + "' 长度不能超过 " + maxLength + " 个字符");
        }
    }
}
