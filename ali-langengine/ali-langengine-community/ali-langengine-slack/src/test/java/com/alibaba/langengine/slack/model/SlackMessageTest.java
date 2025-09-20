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
package com.alibaba.langengine.slack.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


class SlackMessageTest {

    private SlackMessage slackMessage;

    @BeforeEach
    void setUp() {
        slackMessage = new SlackMessage();
    }

    @Test
    void testSetAndGetTs() {
        String ts = "1234567890.123456";
        slackMessage.setTs(ts);
        assertThat(slackMessage.getTs()).isEqualTo(ts);
    }

    @Test
    void testSetAndGetChannelId() {
        String channelId = "C1234567890";
        slackMessage.setChannelId(channelId);
        assertThat(slackMessage.getChannelId()).isEqualTo(channelId);
    }

    @Test
    void testSetAndGetUserId() {
        String userId = "U1234567890";
        slackMessage.setUserId(userId);
        assertThat(slackMessage.getUserId()).isEqualTo(userId);
    }

    @Test
    void testSetAndGetText() {
        String text = "Hello, world!";
        slackMessage.setText(text);
        assertThat(slackMessage.getText()).isEqualTo(text);
    }

    @Test
    void testSetAndGetThreadTs() {
        String threadTs = "1234567890.123456";
        slackMessage.setThreadTs(threadTs);
        assertThat(slackMessage.getThreadTs()).isEqualTo(threadTs);
    }

    @Test
    void testSetAndGetTimestamp() {
        Instant timestamp = Instant.now();
        slackMessage.setTimestamp(timestamp);
        assertThat(slackMessage.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void testSetAndGetType() {
        String type = "message";
        slackMessage.setType(type);
        assertThat(slackMessage.getType()).isEqualTo(type);
    }

    @Test
    void testSetAndGetSubtype() {
        String subtype = "bot_message";
        slackMessage.setSubtype(subtype);
        assertThat(slackMessage.getSubtype()).isEqualTo(subtype);
    }

    @Test
    void testSetAndGetEdited() {
        slackMessage.setEdited(true);
        assertThat(slackMessage.isEdited()).isTrue();

        slackMessage.setEdited(false);
        assertThat(slackMessage.isEdited()).isFalse();
    }

    @Test
    void testSetAndGetDeleted() {
        slackMessage.setDeleted(true);
        assertThat(slackMessage.isDeleted()).isTrue();

        slackMessage.setDeleted(false);
        assertThat(slackMessage.isDeleted()).isFalse();
    }

    @Test
    void testSetAndGetAttachments() {
        String attachments = "[{\"fallback\":\"test\"}]";
        slackMessage.setAttachments(attachments);
        assertThat(slackMessage.getAttachments()).isEqualTo(attachments);
    }

    @Test
    void testSetAndGetBlocks() {
        String blocks = "[{\"type\":\"section\"}]";
        slackMessage.setBlocks(blocks);
        assertThat(slackMessage.getBlocks()).isEqualTo(blocks);
    }

    @Test
    void testDefaultValues() {
        SlackMessage message = new SlackMessage();
        assertThat(message.getTs()).isNull();
        assertThat(message.getChannelId()).isNull();
        assertThat(message.getUserId()).isNull();
        assertThat(message.getText()).isNull();
        assertThat(message.getThreadTs()).isNull();
        assertThat(message.getTimestamp()).isNull();
        assertThat(message.getType()).isEqualTo("message");
        assertThat(message.getSubtype()).isNull();
        assertThat(message.isEdited()).isFalse();
        assertThat(message.isDeleted()).isFalse();
        assertThat(message.getAttachments()).isNull();
        assertThat(message.getBlocks()).isNull();
    }

    @Test
    void testEqualsAndHashCode() {
        SlackMessage message1 = new SlackMessage();
        message1.setTs("1234567890.123456");
        message1.setChannelId("C1234567890");
        message1.setText("Hello, world!");

        SlackMessage message2 = new SlackMessage();
        message2.setTs("1234567890.123456");
        message2.setChannelId("C1234567890");
        message2.setText("Hello, world!");

        assertThat(message1).isEqualTo(message2);
        assertThat(message1.hashCode()).isEqualTo(message2.hashCode());
    }

    @Test
    void testToString() {
        slackMessage.setTs("1234567890.123456");
        slackMessage.setChannelId("C1234567890");
        slackMessage.setText("Hello, world!");
        
        String toString = slackMessage.toString();
        assertThat(toString).contains("1234567890.123456");
        assertThat(toString).contains("C1234567890");
        assertThat(toString).contains("Hello, world!");
    }
}
