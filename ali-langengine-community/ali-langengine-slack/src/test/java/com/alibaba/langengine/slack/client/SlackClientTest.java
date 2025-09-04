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

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.request.users.UsersListRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.Message;
import com.slack.api.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SlackClientTest {

    @Mock
    private Slack mockSlack;

    @Mock
    private MethodsClient mockMethodsClient;

    @Test
    void testConstructorWithValidToken() {
        String token = "xoxb-test-token";
        SlackClient client = new SlackClient(token);
        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    void testConstructorWithNullToken() {
        assertThatThrownBy(() -> new SlackClient(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Slack Bot Token cannot be null or empty");
    }

    @Test
    void testConstructorWithEmptyToken() {
        assertThatThrownBy(() -> new SlackClient(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Slack Bot Token cannot be null or empty");
    }

    @Test
    void testConstructorWithBlankToken() {
        assertThatThrownBy(() -> new SlackClient("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Slack Bot Token cannot be null or empty");
    }

    @Test
    void testClose() {
        String token = "xoxb-test-token";
        SlackClient client = new SlackClient(token);
        
        // 测试关闭操作不会抛出异常
        client.close();
        
        // 多次调用close应该是安全的
        client.close();
    }

    @Test
    void testSendMessageWithInvalidToken() {
        String token = "xoxb-invalid-token";
        SlackClient client = new SlackClient(token);
        
        // 使用无效token发送消息应该返回false
        boolean result = client.sendMessage("C1234567890", "Test message");
        assertThat(result).isFalse();
        
        client.close();
    }

    @Test
    void testGetChannelsWithInvalidToken() {
        String token = "xoxb-invalid-token";
        SlackClient client = new SlackClient(token);
        
        // 使用无效token获取频道列表应该返回空列表
        assertThat(client.getChannels()).isEmpty();
        
        client.close();
    }

    @Test
    void testGetUsersWithInvalidToken() {
        String token = "xoxb-invalid-token";
        SlackClient client = new SlackClient(token);
        
        // 使用无效token获取用户列表应该返回空列表
        assertThat(client.getUsers()).isEmpty();
        
        client.close();
    }

    @Test
    void testGetChannelHistoryWithInvalidToken() {
        String token = "xoxb-invalid-token";
        SlackClient client = new SlackClient(token);
        
        // 使用无效token获取频道历史应该返回空列表
        assertThat(client.getChannelHistory("C1234567890", 10)).isEmpty();
        
        client.close();
    }

    @Test
    void testFindChannelByNameWithInvalidToken() {
        String token = "xoxb-invalid-token";
        SlackClient client = new SlackClient(token);
        
        // 使用无效token查找频道应该返回null
        assertThat(client.findChannelByName("general")).isNull();
        
        client.close();
    }

    @Test
    void testFindUserByNameWithInvalidToken() {
        String token = "xoxb-invalid-token";
        SlackClient client = new SlackClient(token);
        
        // 使用无效token查找用户应该返回null
        assertThat(client.findUserByName("john.doe")).isNull();
        
        client.close();
    }

    @Test
    void testIsHealthyWithInvalidToken() {
        String token = "xoxb-invalid-token";
        SlackClient client = new SlackClient(token);
        
        // 使用无效token进行健康检查应该返回false
        assertThat(client.isHealthy()).isFalse();
        
        client.close();
    }

    @Test
    void testSendMessageAsyncWithInvalidToken() {
        String token = "xoxb-invalid-token";
        SlackClient client = new SlackClient(token);
        
        // 异步发送消息应该完成并返回false
        boolean result = client.sendMessageAsync("C1234567890", "Test message").join();
        assertThat(result).isFalse();
        
        client.close();
    }

    @Test
    void testSendMessageSuccess() throws Exception {
        String token = "xoxb-valid-token";
        
        try (MockedStatic<Slack> mockedStatic = mockStatic(Slack.class)) {
            // 设置模拟
            mockedStatic.when(Slack::getInstance).thenReturn(mockSlack);
            when(mockSlack.methods()).thenReturn(mockMethodsClient);
            
            ChatPostMessageResponse successResponse = new ChatPostMessageResponse();
            successResponse.setOk(true);
            successResponse.setTs("1234567890.123456");
            
            when(mockMethodsClient.chatPostMessage(any(ChatPostMessageRequest.class)))
                    .thenReturn(successResponse);
            
            // 创建客户端并测试
            SlackClient client = new SlackClient(token);
            boolean result = client.sendMessage("C1234567890", "Test message");
            
            // 验证结果
            assertThat(result).isTrue();
            verify(mockMethodsClient).chatPostMessage(any(ChatPostMessageRequest.class));
            
            client.close();
        }
    }

    @Test
    void testSendMessageWithThreadSuccess() throws Exception {
        String token = "xoxb-valid-token";
        
        try (MockedStatic<Slack> mockedStatic = mockStatic(Slack.class)) {
            // 设置模拟
            mockedStatic.when(Slack::getInstance).thenReturn(mockSlack);
            when(mockSlack.methods()).thenReturn(mockMethodsClient);
            
            ChatPostMessageResponse successResponse = new ChatPostMessageResponse();
            successResponse.setOk(true);
            successResponse.setTs("1234567890.123456");
            // Note: threadTs is not a settable field in response, it's in the message
            
            when(mockMethodsClient.chatPostMessage(any(ChatPostMessageRequest.class)))
                    .thenReturn(successResponse);
            
            // 创建客户端并测试
            SlackClient client = new SlackClient(token);
            boolean result = client.sendMessage("C1234567890", "Thread reply", "1234567890.000000");
            
            // 验证结果
            assertThat(result).isTrue();
            verify(mockMethodsClient).chatPostMessage(argThat((ChatPostMessageRequest request) -> 
                "1234567890.000000".equals(request.getThreadTs())
            ));
            
            client.close();
        }
    }

    @Test
    void testGetChannelsSuccess() throws Exception {
        String token = "xoxb-valid-token";
        
        try (MockedStatic<Slack> mockedStatic = mockStatic(Slack.class)) {
            // 设置模拟
            mockedStatic.when(Slack::getInstance).thenReturn(mockSlack);
            when(mockSlack.methods()).thenReturn(mockMethodsClient);
            
            // 模拟频道数据
            Conversation channel1 = new Conversation();
            channel1.setId("C1234567890");
            channel1.setName("general");
            // Note: setIsPrivate might not exist, use actual API methods
            
            Conversation channel2 = new Conversation();
            channel2.setId("C0987654321");
            channel2.setName("random");
            
            ConversationsListResponse successResponse = new ConversationsListResponse();
            successResponse.setOk(true);
            successResponse.setChannels(Arrays.asList(channel1, channel2));
            
            when(mockMethodsClient.conversationsList(any(ConversationsListRequest.class)))
                    .thenReturn(successResponse);
            
            // 创建客户端并测试
            SlackClient client = new SlackClient(token);
            List<com.alibaba.langengine.slack.model.SlackChannel> channels = client.getChannels();
            
            // 验证结果
            assertThat(channels).hasSize(2);
            assertThat(channels.get(0).getName()).isEqualTo("general");
            assertThat(channels.get(1).getName()).isEqualTo("random");
            verify(mockMethodsClient).conversationsList(any(ConversationsListRequest.class));
            
            client.close();
        }
    }

    @Test
    void testGetUsersSuccess() throws Exception {
        String token = "xoxb-valid-token";
        
        try (MockedStatic<Slack> mockedStatic = mockStatic(Slack.class)) {
            // 设置模拟
            mockedStatic.when(Slack::getInstance).thenReturn(mockSlack);
            when(mockSlack.methods()).thenReturn(mockMethodsClient);
            
            // 模拟用户数据
            User user1 = new User();
            user1.setId("U1234567890");
            user1.setName("alice");
            user1.setRealName("Alice Smith");
            
            User user2 = new User();
            user2.setId("U0987654321");
            user2.setName("bob");
            user2.setRealName("Bob Johnson");
            
            UsersListResponse successResponse = new UsersListResponse();
            successResponse.setOk(true);
            successResponse.setMembers(Arrays.asList(user1, user2));
            
            when(mockMethodsClient.usersList(any(UsersListRequest.class)))
                    .thenReturn(successResponse);
            
            // 创建客户端并测试
            SlackClient client = new SlackClient(token);
            List<com.alibaba.langengine.slack.model.SlackUser> users = client.getUsers();
            
            // 验证结果
            assertThat(users).hasSize(2);
            assertThat(users.get(0).getName()).isEqualTo("alice");
            assertThat(users.get(1).getName()).isEqualTo("bob");
            verify(mockMethodsClient).usersList(any(UsersListRequest.class));
            
            client.close();
        }
    }

    @Test
    void testGetChannelHistorySuccess() throws Exception {
        String token = "xoxb-valid-token";
        
        try (MockedStatic<Slack> mockedStatic = mockStatic(Slack.class)) {
            // 设置模拟
            mockedStatic.when(Slack::getInstance).thenReturn(mockSlack);
            when(mockSlack.methods()).thenReturn(mockMethodsClient);
            
            // 模拟消息数据
            Message message1 = new Message();
            message1.setTs("1234567890.123456");
            message1.setUser("U1234567890");
            message1.setText("Hello, world!");
            
            Message message2 = new Message();
            message2.setTs("1234567891.123456");
            message2.setUser("U0987654321");
            message2.setText("How are you?");
            
            ConversationsHistoryResponse successResponse = new ConversationsHistoryResponse();
            successResponse.setOk(true);
            successResponse.setMessages(Arrays.asList(message1, message2));
            
            when(mockMethodsClient.conversationsHistory(any(ConversationsHistoryRequest.class)))
                    .thenReturn(successResponse);
            
            // 创建客户端并测试
            SlackClient client = new SlackClient(token);
            List<com.alibaba.langengine.slack.model.SlackMessage> messages = client.getChannelHistory("C1234567890", 10);
            
            // 验证结果
            assertThat(messages).hasSize(2);
            assertThat(messages.get(0).getText()).isEqualTo("Hello, world!");
            assertThat(messages.get(1).getText()).isEqualTo("How are you?");
            verify(mockMethodsClient).conversationsHistory(any(ConversationsHistoryRequest.class));
            
            client.close();
        }
    }

    @Test
    void testIsHealthySuccess() throws Exception {
        String token = "xoxb-valid-token";
        
        try (MockedStatic<Slack> mockedStatic = mockStatic(Slack.class)) {
            // 设置模拟
            mockedStatic.when(Slack::getInstance).thenReturn(mockSlack);
            when(mockSlack.methods()).thenReturn(mockMethodsClient);
            
            UsersListResponse successResponse = new UsersListResponse();
            successResponse.setOk(true);
            successResponse.setMembers(Arrays.asList(new User()));
            
            when(mockMethodsClient.usersList(any(UsersListRequest.class)))
                    .thenReturn(successResponse);
            
            // 创建客户端并测试
            SlackClient client = new SlackClient(token);
            boolean isHealthy = client.isHealthy();
            
            // 验证结果
            assertThat(isHealthy).isTrue();
            verify(mockMethodsClient).usersList(argThat((UsersListRequest request) -> 
                Integer.valueOf(1).equals(request.getLimit())
            ));
            
            client.close();
        }
    }
}
