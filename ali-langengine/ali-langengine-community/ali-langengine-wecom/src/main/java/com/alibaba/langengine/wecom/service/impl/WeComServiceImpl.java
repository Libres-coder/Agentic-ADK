package com.alibaba.langengine.wecom.service.impl;

import com.alibaba.langengine.wecom.client.WeComClient;
import com.alibaba.langengine.wecom.exception.WeComException;
import com.alibaba.langengine.wecom.model.WeComMessage;
import com.alibaba.langengine.wecom.model.WeComUser;
import com.alibaba.langengine.wecom.service.WeComService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


@Service
public class WeComServiceImpl implements WeComService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeComServiceImpl.class);
    
    private final WeComClient weComClient;
    
    // 统计信息
    private final AtomicInteger totalRequests = new AtomicInteger(0);
    private final AtomicInteger successRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final long startTime = System.currentTimeMillis();
    
    public WeComServiceImpl(WeComClient weComClient) {
        this.weComClient = weComClient;
    }
    
    @Override
    public SendMessageResult sendTextMessage(String toUser, String content) throws WeComException {
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();
        
        try {
            logger.debug("发送文本消息: toUser={}, contentLength={}", toUser, content.length());
            
            boolean success = weComClient.sendTextMessage(toUser, content);
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTime.addAndGet(responseTime);
            
            if (success) {
                successRequests.incrementAndGet();
                String messageId = generateMessageId();
                logger.info("文本消息发送成功: toUser={}, messageId={}, responseTime={}ms", 
                    toUser, messageId, responseTime);
                return SendMessageResult.success(messageId);
            } else {
                failedRequests.incrementAndGet();
                logger.warn("文本消息发送失败: toUser={}, responseTime={}ms", toUser, responseTime);
                return SendMessageResult.failure("消息发送失败，原因未知");
            }
            
        } catch (WeComException e) {
            failedRequests.incrementAndGet();
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTime.addAndGet(responseTime);
            
            logger.error("发送文本消息异常: toUser={}, error={}, responseTime={}ms", 
                toUser, e.getMessage(), responseTime);
            throw e;
        }
    }
    
    @Override
    public SendMessageResult sendMarkdownMessage(String toUser, String title, String content) throws WeComException {
        // TODO: 实现Markdown消息发送
        throw new WeComException("Markdown消息发送功能暂未实现");
    }
    
    @Override
    public SendMessageResult sendMessage(WeComMessage message) throws WeComException {
        if (message == null) {
            throw new WeComException("消息对象不能为空");
        }
        
        // 根据消息类型调用相应的发送方法
        switch (message.getMsgType()) {
            case "text":
                return sendTextMessage(message.getToUser(), message.getContent());
            case "markdown":
                return sendMarkdownMessage(message.getToUser(), message.getTitle(), message.getContent());
            default:
                throw new WeComException("不支持的消息类型: " + message.getMsgType());
        }
    }
    
    @Override
    public WeComUser getUserInfo(String userId) throws WeComException {
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();
        
        try {
            logger.debug("查询用户信息: userId={}", userId);
            
            WeComUser user = weComClient.getUserInfo(userId);
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTime.addAndGet(responseTime);
            
            if (user != null) {
                successRequests.incrementAndGet();
                logger.info("用户信息查询成功: userId={}, userName={}, responseTime={}ms", 
                    userId, user.getName(), responseTime);
            } else {
                failedRequests.incrementAndGet();
                logger.warn("用户信息查询失败: userId={}, responseTime={}ms", userId, responseTime);
            }
            
            return user;
            
        } catch (WeComException e) {
            failedRequests.incrementAndGet();
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTime.addAndGet(responseTime);
            
            logger.error("查询用户信息异常: userId={}, error={}, responseTime={}ms", 
                userId, e.getMessage(), responseTime);
            throw e;
        }
    }
    
    @Override
    public boolean validateConfiguration() {
        try {
            // 尝试获取Access Token来验证配置
            String token = weComClient.getAccessToken();
            boolean valid = token != null && !token.trim().isEmpty();
            
            logger.info("企业微信配置验证结果: {}", valid ? "有效" : "无效");
            return valid;
            
        } catch (Exception e) {
            logger.warn("企业微信配置验证失败", e);
            return false;
        }
    }
    
    @Override
    public ServiceStatus getServiceStatus() {
        int total = totalRequests.get();
        int success = successRequests.get();
        int failed = failedRequests.get();
        double avgResponseTime = total > 0 ? (double) totalResponseTime.get() / total : 0;
        
        ConnectionStats stats = new ConnectionStats(total, success, failed, avgResponseTime);
        
        boolean healthy = validateConfiguration() && (total == 0 || stats.getSuccessRate() > 50);
        String status = healthy ? "运行正常" : "存在问题";
        String version = "1.0.0"; // 可以从配置或构建信息中获取
        long uptime = System.currentTimeMillis() - this.startTime;
        
        return new ServiceStatus(healthy, status, version, uptime, stats);
    }
    
    /**
     * 生成消息ID
     */
    private String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
}
