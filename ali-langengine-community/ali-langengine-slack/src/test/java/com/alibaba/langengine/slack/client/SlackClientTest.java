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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
class SlackClientTest {

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
}
