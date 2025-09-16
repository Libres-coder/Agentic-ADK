package com.alibaba.langengine.wecom.service;

import com.alibaba.langengine.wecom.model.WeComMessage;
import com.alibaba.langengine.wecom.model.WeComUser;
import com.alibaba.langengine.wecom.exception.WeComException;


public interface WeComService {
    
    /**
     * 发送文本消息
     * 
     * @param toUser 接收用户ID
     * @param content 消息内容
     * @return 发送结果
     * @throws WeComException 发送失败时抛出异常
     */
    SendMessageResult sendTextMessage(String toUser, String content) throws WeComException;
    
    /**
     * 发送Markdown消息
     * 
     * @param toUser 接收用户ID
     * @param title 消息标题
     * @param content 消息内容
     * @return 发送结果
     * @throws WeComException 发送失败时抛出异常
     */
    SendMessageResult sendMarkdownMessage(String toUser, String title, String content) throws WeComException;
    
    /**
     * 发送消息（通用方法）
     * 
     * @param message 消息对象
     * @return 发送结果
     * @throws WeComException 发送失败时抛出异常
     */
    SendMessageResult sendMessage(WeComMessage message) throws WeComException;
    
    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     * @throws WeComException 查询失败时抛出异常
     */
    WeComUser getUserInfo(String userId) throws WeComException;
    
    /**
     * 验证企业微信配置
     * 
     * @return 配置是否有效
     */
    boolean validateConfiguration();
    
    /**
     * 获取服务状态
     * 
     * @return 服务状态信息
     */
    ServiceStatus getServiceStatus();
    
    /**
     * 消息发送结果
     */
    class SendMessageResult {
        private final boolean success;
        private final String messageId;
        private final String errorMessage;
        private final long timestamp;
        
        public SendMessageResult(boolean success, String messageId, String errorMessage) {
            this.success = success;
            this.messageId = messageId;
            this.errorMessage = errorMessage;
            this.timestamp = System.currentTimeMillis();
        }
        
        public static SendMessageResult success(String messageId) {
            return new SendMessageResult(true, messageId, null);
        }
        
        public static SendMessageResult failure(String errorMessage) {
            return new SendMessageResult(false, null, errorMessage);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessageId() { return messageId; }
        public String getErrorMessage() { return errorMessage; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * 服务状态信息
     */
    class ServiceStatus {
        private final boolean healthy;
        private final String status;
        private final String version;
        private final long uptime;
        private final ConnectionStats connectionStats;
        
        public ServiceStatus(boolean healthy, String status, String version, long uptime, ConnectionStats connectionStats) {
            this.healthy = healthy;
            this.status = status;
            this.version = version;
            this.uptime = uptime;
            this.connectionStats = connectionStats;
        }
        
        // Getters
        public boolean isHealthy() { return healthy; }
        public String getStatus() { return status; }
        public String getVersion() { return version; }
        public long getUptime() { return uptime; }
        public ConnectionStats getConnectionStats() { return connectionStats; }
    }
    
    /**
     * 连接统计信息
     */
    class ConnectionStats {
        private final int totalRequests;
        private final int successRequests;
        private final int failedRequests;
        private final double averageResponseTime;
        
        public ConnectionStats(int totalRequests, int successRequests, int failedRequests, double averageResponseTime) {
            this.totalRequests = totalRequests;
            this.successRequests = successRequests;
            this.failedRequests = failedRequests;
            this.averageResponseTime = averageResponseTime;
        }
        
        // Getters
        public int getTotalRequests() { return totalRequests; }
        public int getSuccessRequests() { return successRequests; }
        public int getFailedRequests() { return failedRequests; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public double getSuccessRate() { 
            return totalRequests > 0 ? (double) successRequests / totalRequests * 100 : 0; 
        }
    }
}
