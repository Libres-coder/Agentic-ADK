package com.alibaba.langengine.wecom.client;

import com.alibaba.langengine.wecom.WeComConfiguration;
import com.alibaba.langengine.wecom.constant.WeComConstant;
import com.alibaba.langengine.wecom.exception.WeComException;
import com.alibaba.langengine.wecom.model.WeComMessage;
import com.alibaba.langengine.wecom.model.WeComUser;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


@Component
public class WeComClient {
    
    private static final Logger logger = LoggerFactory.getLogger(WeComClient.class);
    
    // 消息内容最大长度限制
    private static final int MAX_MESSAGE_LENGTH = 2048;
    // 用户ID最大长度限制
    private static final int MAX_USER_ID_LENGTH = 64;
    
    private final WeComConfiguration config;
    private final CloseableHttpClient httpClient;
    private final ConcurrentHashMap<String, String> accessTokenCache = new ConcurrentHashMap<>();
    private final ReentrantLock tokenLock = new ReentrantLock();
    private volatile long tokenExpireTime = 0;
    
    public WeComClient(WeComConfiguration config) {
        if (config == null || !config.isValid()) {
            throw new WeComException("WeComConfiguration is invalid");
        }
        this.config = config;
        this.httpClient = HttpClientManager.getClient(); // 使用共享的HTTP客户端
        logger.info("WeComClient初始化完成，企业ID: {}", maskSensitiveInfo(config.getCorpId()));
    }
    
    /**
     * 掩码敏感信息用于日志输出
     */
    private String maskSensitiveInfo(String info) {
        if (StringUtils.isBlank(info)) {
            return "null";
        }
        if (info.length() <= 3) {
            return "***";
        }
        return info.substring(0, 3) + "***";
    }
    
    /**
     * 获取配置对象
     * 
     * @return WeComConfiguration配置对象
     */
    public WeComConfiguration getConfiguration() {
        return config;
    }
    
    /**
     * 获取访问令牌
     */
    public String getAccessToken() throws WeComException {
        if (System.currentTimeMillis() < tokenExpireTime && accessTokenCache.containsKey("access_token")) {
            return accessTokenCache.get("access_token");
        }
        
        tokenLock.lock();
        try {
            // 双重检查
            if (System.currentTimeMillis() < tokenExpireTime && accessTokenCache.containsKey("access_token")) {
                return accessTokenCache.get("access_token");
            }
            
            String url = WeComConstant.API_BASE_URL + WeComConstant.API_GET_TOKEN 
                    + "?corpid=" + config.getCorpId() 
                    + "&corpsecret=" + config.getCorpSecret();
            
            HttpGet httpGet = new HttpGet(url);
            
            try {
                String response = httpClient.execute(httpGet, responseHandler -> {
                    try (InputStream content = responseHandler.getEntity().getContent()) {
                        return new String(content.readAllBytes(), StandardCharsets.UTF_8);
                    }
                });
                
                JSONObject jsonResponse = JSON.parseObject(response);
                if (jsonResponse.getIntValue("errcode") != 0) {
                    throw new WeComException(jsonResponse.getIntValue("errcode"), 
                            jsonResponse.getString("errmsg"));
                }
                
                String accessToken = jsonResponse.getString("access_token");
                int expiresIn = jsonResponse.getIntValue("expires_in");
                
                accessTokenCache.put("access_token", accessToken);
                tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000; // 提前5分钟过期
                
                return accessToken;
                
            } catch (IOException e) {
                throw new WeComException("获取访问令牌失败", e);
            }
            
        } finally {
            tokenLock.unlock();
        }
    }
    
    /**
     * 发送消息
     */
    public boolean sendMessage(WeComMessage message) throws WeComException {
        if (message == null) {
            throw new WeComException("消息对象不能为空");
        }
        
        String accessToken = getAccessToken();
        String url = WeComConstant.API_BASE_URL + WeComConstant.API_SEND_MESSAGE + "?access_token=" + accessToken;
        
        HttpPost httpPost = new HttpPost(url);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("touser", message.getToUser());
        requestBody.put("toparty", message.getToParty());
        requestBody.put("totag", message.getToTag());
        requestBody.put("msgtype", message.getMsgType());
        requestBody.put("agentid", message.getAgentId());
        requestBody.put("safe", message.getSafe());
        
        // 根据消息类型设置内容
        if (WeComConstant.MSG_TYPE_TEXT.equals(message.getMsgType())) {
            JSONObject textContent = new JSONObject();
            textContent.put("content", message.getContent());
            requestBody.put("text", textContent);
        }
        
        httpPost.setEntity(new StringEntity(requestBody.toJSONString(), ContentType.APPLICATION_JSON));
        
        try {
            String response = httpClient.execute(httpPost, responseHandler -> {
                try (InputStream content = responseHandler.getEntity().getContent()) {
                    return new String(content.readAllBytes(), StandardCharsets.UTF_8);
                }
            });
            
            JSONObject jsonResponse = JSON.parseObject(response);
            if (jsonResponse.getIntValue("errcode") != 0) {
                throw new WeComException(jsonResponse.getIntValue("errcode"), 
                        jsonResponse.getString("errmsg"));
            }
            
            logger.info("发送消息成功: {}", response);
            return true;
            
        } catch (IOException e) {
            throw new WeComException("发送消息失败", e);
        }
    }
    
    /**
     * 验证用户ID
     * 
     * @param userId 用户ID
     * @throws WeComException 如果用户ID无效
     */
    private void validateUserId(String userId) throws WeComException {
        if (StringUtils.isBlank(userId)) {
            throw new WeComException("用户ID不能为空");
        }
        if (userId.length() > MAX_USER_ID_LENGTH) {
            throw new WeComException("用户ID长度不能超过" + MAX_USER_ID_LENGTH + "个字符");
        }
        // 检查是否包含特殊字符
        if (!userId.matches("^[a-zA-Z0-9@._-]+$")) {
            throw new WeComException("用户ID包含非法字符，只允许字母、数字、@、.、_、-");
        }
    }
    
    /**
     * 验证消息内容
     * 
     * @param content 消息内容
     * @throws WeComException 如果消息内容无效
     */
    private void validateMessageContent(String content) throws WeComException {
        if (StringUtils.isBlank(content)) {
            throw new WeComException("消息内容不能为空");
        }
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new WeComException("消息内容长度不能超过" + MAX_MESSAGE_LENGTH + "个字符");
        }
    }
    
    /**
     * 发送文本消息（增强版本，包含输入验证）
     * 
     * @param toUser 接收消息的用户ID
     * @param content 消息内容
     * @return 发送是否成功
     * @throws WeComException 发送失败时抛出异常
     */
    /**
     * 发送文本消息（增强版本，包含输入验证）
     * 
     * @param toUser 接收消息的用户ID
     * @param content 消息内容
     * @return 发送是否成功
     * @throws WeComException 发送失败时抛出异常
     */
    public boolean sendTextMessage(String toUser, String content) throws WeComException {
        // 输入验证
        validateUserId(toUser);
        validateMessageContent(content);
        
        WeComMessage message = new WeComMessage();
        message.setToUser(toUser);
        message.setMsgType("text");
        message.setContent(content);
        message.setAgentId(config.getAgentId());
        
        return sendMessage(message);
    }
    
    /**
     * 获取用户信息
     */
    public WeComUser getUserInfo(String userId) throws WeComException {
        if (StringUtils.isBlank(userId)) {
            throw new WeComException(WeComConstant.ERROR_INVALID_USERID, "用户ID不能为空");
        }
        
        String accessToken = getAccessToken();
        String url = WeComConstant.API_BASE_URL + WeComConstant.API_GET_USER 
                + "?access_token=" + accessToken + "&userid=" + userId;
        
        HttpGet httpGet = new HttpGet(url);
        
        try {
            String response = httpClient.execute(httpGet, responseHandler -> {
                try (InputStream content = responseHandler.getEntity().getContent()) {
                    return new String(content.readAllBytes(), StandardCharsets.UTF_8);
                }
            });
            
            JSONObject jsonResponse = JSON.parseObject(response);
            if (jsonResponse.getIntValue("errcode") != 0) {
                throw new WeComException(jsonResponse.getIntValue("errcode"), 
                        jsonResponse.getString("errmsg"));
            }
            
            WeComUser user = new WeComUser();
            user.setUserId(jsonResponse.getString("userid"));
            user.setName(jsonResponse.getString("name"));
            user.setPosition(jsonResponse.getString("position"));
            user.setMobile(jsonResponse.getString("mobile"));
            user.setGender(jsonResponse.getInteger("gender"));
            user.setEmail(jsonResponse.getString("email"));
            user.setAvatar(jsonResponse.getString("avatar"));
            user.setStatus(jsonResponse.getInteger("status"));
            
            return user;
            
        } catch (IOException e) {
            throw new WeComException("获取用户信息失败", e);
        }
    }
    
    /**
     * 关闭客户端
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            logger.error("关闭HTTP客户端失败", e);
        }
    }
}
