/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.slack.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.langengine.slack.SlackConfiguration;
import com.alibaba.langengine.slack.model.SlackChannel;
import com.alibaba.langengine.slack.model.SlackMessage;
import com.alibaba.langengine.slack.model.SlackUser;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.channels.ChannelsListRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.request.users.UsersListRequest;
import com.slack.api.methods.response.channels.ChannelsListResponse;
import com.slack.api.model.ConversationType;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.Channel;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.slack.api.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Slf4j
public class SlackClient {

    private final Slack slack;
    private final String botToken;
    private final ExecutorService executorService;

    public SlackClient() {
        this(SlackConfiguration.getSlackBotToken());
    }

    public SlackClient(String botToken) {
        if (StringUtils.isBlank(botToken)) {
            throw new IllegalArgumentException("Slack Bot Token cannot be null or empty");
        }
        this.botToken = botToken;
        this.slack = Slack.getInstance();
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "slack-client-thread");
            thread.setDaemon(true);
            return thread;
        });
        log.info("SlackClient initialized with bot token");
    }

    /**
     * 发送消息到指定频道
     *
     * @param channelId 频道ID
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String channelId, String message) {
        return sendMessage(channelId, message, null);
    }

    /**
     * 发送消息到指定频道
     *
     * @param channelId 频道ID
     * @param message 消息内容
     * @param threadTs 线程时间戳（可选，用于回复特定消息）
     * @return 是否发送成功
     */
    public boolean sendMessage(String channelId, String message, String threadTs) {
        try {
            ChatPostMessageRequest.ChatPostMessageRequestBuilder requestBuilder = ChatPostMessageRequest.builder()
                    .token(botToken)
                    .channel(channelId)
                    .text(message);

            if (StringUtils.isNotBlank(threadTs)) {
                requestBuilder.threadTs(threadTs);
            }

            ChatPostMessageResponse response = slack.methods().chatPostMessage(requestBuilder.build());
            
            if (response.isOk()) {
                log.debug("Message sent successfully to channel: {}", channelId);
                return true;
            } else {
                log.error("Failed to send message to channel {}: {}", channelId, response.getError());
                return false;
            }
        } catch (IOException | SlackApiException e) {
            log.error("Error sending message to channel {}: {}", channelId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 异步发送消息
     *
     * @param channelId 频道ID
     * @param message 消息内容
     * @return CompletableFuture<Boolean>
     */
    public CompletableFuture<Boolean> sendMessageAsync(String channelId, String message) {
        return CompletableFuture.supplyAsync(() -> sendMessage(channelId, message), executorService);
    }

    /**
     * 获取频道列表
     *
     * @return 频道列表
     */
    public List<SlackChannel> getChannels() {
        try {
            ConversationsListRequest request = ConversationsListRequest.builder()
                    .token(botToken)
                    .excludeArchived(true)
                    .types(Collections.singletonList(ConversationType.PUBLIC_CHANNEL))
                    .build();

            ConversationsListResponse response = slack.methods().conversationsList(request);
            
            if (response.isOk()) {
                return response.getChannels().stream()
                        .map(this::convertToSlackChannel)
                        .collect(Collectors.toList());
            } else {
                log.error("Failed to get channels: {}", response.getError());
                return Collections.emptyList();
            }
        } catch (IOException | SlackApiException e) {
            log.error("Error getting channels: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取私有频道列表
     *
     * @return 私有频道列表
     */
    public List<SlackChannel> getPrivateChannels() {
        try {
            ConversationsListRequest request = ConversationsListRequest.builder()
                    .token(botToken)
                    .excludeArchived(true)
                    .types(Collections.singletonList(ConversationType.PRIVATE_CHANNEL))
                    .build();

            ConversationsListResponse response = slack.methods().conversationsList(request);
            
            if (response.isOk()) {
                return response.getChannels().stream()
                        .map(this::convertToSlackChannel)
                        .collect(Collectors.toList());
            } else {
                log.error("Failed to get private channels: {}", response.getError());
                return Collections.emptyList();
            }
        } catch (IOException | SlackApiException e) {
            log.error("Error getting private channels: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户列表
     *
     * @return 用户列表
     */
    public List<SlackUser> getUsers() {
        try {
            UsersListRequest request = UsersListRequest.builder()
                    .token(botToken)
                    .build();

            UsersListResponse response = slack.methods().usersList(request);
            
            if (response.isOk()) {
                return response.getMembers().stream()
                        .filter(user -> !user.isBot() && !user.isDeleted())
                        .map(this::convertToSlackUser)
                        .collect(Collectors.toList());
            } else {
                log.error("Failed to get users: {}", response.getError());
                return Collections.emptyList();
            }
        } catch (IOException | SlackApiException e) {
            log.error("Error getting users: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取频道历史消息
     *
     * @param channelId 频道ID
     * @param limit 消息数量限制
     * @return 消息列表
     */
    public List<SlackMessage> getChannelHistory(String channelId, int limit) {
        try {
            ConversationsHistoryRequest request = ConversationsHistoryRequest.builder()
                    .token(botToken)
                    .channel(channelId)
                    .limit(limit)
                    .build();

            ConversationsHistoryResponse response = slack.methods().conversationsHistory(request);
            
            if (response.isOk()) {
                return response.getMessages().stream()
                        .map(message -> convertToSlackMessage(message, channelId))
                        .collect(Collectors.toList());
            } else {
                log.error("Failed to get channel history for {}: {}", channelId, response.getError());
                return Collections.emptyList();
            }
        } catch (IOException | SlackApiException e) {
            log.error("Error getting channel history for {}: {}", channelId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取频道历史消息（指定时间范围）
     *
     * @param channelId 频道ID
     * @param oldest 最早时间戳
     * @param latest 最晚时间戳
     * @param limit 消息数量限制
     * @return 消息列表
     */
    public List<SlackMessage> getChannelHistory(String channelId, String oldest, String latest, int limit) {
        try {
            ConversationsHistoryRequest.ConversationsHistoryRequestBuilder requestBuilder = 
                ConversationsHistoryRequest.builder()
                    .token(botToken)
                    .channel(channelId)
                    .limit(limit);

            if (StringUtils.isNotBlank(oldest)) {
                requestBuilder.oldest(oldest);
            }
            if (StringUtils.isNotBlank(latest)) {
                requestBuilder.latest(latest);
            }

            ConversationsHistoryResponse response = slack.methods().conversationsHistory(requestBuilder.build());
            
            if (response.isOk()) {
                return response.getMessages().stream()
                        .map(message -> convertToSlackMessage(message, channelId))
                        .collect(Collectors.toList());
            } else {
                log.error("Failed to get channel history for {}: {}", channelId, response.getError());
                return Collections.emptyList();
            }
        } catch (IOException | SlackApiException e) {
            log.error("Error getting channel history for {}: {}", channelId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据名称查找频道
     *
     * @param channelName 频道名称
     * @return 频道信息，如果未找到返回null
     */
    public SlackChannel findChannelByName(String channelName) {
        List<SlackChannel> channels = getChannels();
        return channels.stream()
                .filter(channel -> channelName.equals(channel.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息，如果未找到返回null
     */
    public SlackUser findUserByName(String username) {
        List<SlackUser> users = getUsers();
        return users.stream()
                .filter(user -> username.equals(user.getName()) || username.equals(user.getRealName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查客户端连接状态
     *
     * @return 是否连接正常
     */
    public boolean isHealthy() {
        try {
            // 通过获取用户列表来检查连接状态
            UsersListRequest request = UsersListRequest.builder()
                    .token(botToken)
                    .limit(1)
                    .build();

            UsersListResponse response = slack.methods().usersList(request);
            return response.isOk();
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 关闭客户端
     */
    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            log.info("SlackClient executor service shut down");
        }
    }

    // 转换方法

    private SlackChannel convertToSlackChannel(Conversation conversation) {
        SlackChannel channel = new SlackChannel();
        channel.setId(conversation.getId());
        channel.setName(conversation.getName());
        channel.setTopic(conversation.getTopic() != null ? conversation.getTopic().getValue() : "");
        channel.setPurpose(conversation.getPurpose() != null ? conversation.getPurpose().getValue() : "");
        channel.setPrivateChannel(conversation.isPrivate());
        channel.setArchived(conversation.isArchived());
        channel.setMemberCount(0); // Member count 需要通过单独的API调用获取
        return channel;
    }

    private SlackUser convertToSlackUser(User user) {
        SlackUser slackUser = new SlackUser();
        slackUser.setId(user.getId());
        slackUser.setName(user.getName());
        slackUser.setRealName(user.getRealName());
        slackUser.setDisplayName(user.getProfile() != null ? user.getProfile().getDisplayName() : "");
        slackUser.setEmail(user.getProfile() != null ? user.getProfile().getEmail() : "");
        slackUser.setBot(user.isBot());
        slackUser.setDeleted(user.isDeleted());
        slackUser.setAdmin(user.isAdmin());
        slackUser.setTimezone(user.getTz());
        return slackUser;
    }

    private SlackMessage convertToSlackMessage(Message message, String channelId) {
        SlackMessage slackMessage = new SlackMessage();
        slackMessage.setTs(message.getTs());
        slackMessage.setChannelId(channelId);
        slackMessage.setUserId(message.getUser());
        slackMessage.setText(message.getText());
        slackMessage.setThreadTs(message.getThreadTs());
        
        // 转换时间戳
        try {
            String ts = message.getTs();
            if (StringUtils.isNotBlank(ts)) {
                double timestamp = Double.parseDouble(ts);
                slackMessage.setTimestamp(Instant.ofEpochSecond((long) timestamp));
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse timestamp: {}", message.getTs());
        }
        
        return slackMessage;
    }
}
