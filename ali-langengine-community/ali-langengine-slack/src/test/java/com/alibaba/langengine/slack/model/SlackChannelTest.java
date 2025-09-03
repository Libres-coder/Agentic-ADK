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

import static org.assertj.core.api.Assertions.assertThat;


class SlackChannelTest {

    private SlackChannel slackChannel;

    @BeforeEach
    void setUp() {
        slackChannel = new SlackChannel();
    }

    @Test
    void testSetAndGetId() {
        String id = "C1234567890";
        slackChannel.setId(id);
        assertThat(slackChannel.getId()).isEqualTo(id);
    }

    @Test
    void testSetAndGetName() {
        String name = "general";
        slackChannel.setName(name);
        assertThat(slackChannel.getName()).isEqualTo(name);
    }

    @Test
    void testSetAndGetTopic() {
        String topic = "General discussion";
        slackChannel.setTopic(topic);
        assertThat(slackChannel.getTopic()).isEqualTo(topic);
    }

    @Test
    void testSetAndGetPurpose() {
        String purpose = "Company-wide announcements and discussion";
        slackChannel.setPurpose(purpose);
        assertThat(slackChannel.getPurpose()).isEqualTo(purpose);
    }

    @Test
    void testSetAndGetPrivateChannel() {
        slackChannel.setPrivateChannel(true);
        assertThat(slackChannel.isPrivateChannel()).isTrue();

        slackChannel.setPrivateChannel(false);
        assertThat(slackChannel.isPrivateChannel()).isFalse();
    }

    @Test
    void testSetAndGetArchived() {
        slackChannel.setArchived(true);
        assertThat(slackChannel.isArchived()).isTrue();

        slackChannel.setArchived(false);
        assertThat(slackChannel.isArchived()).isFalse();
    }

    @Test
    void testSetAndGetMemberCount() {
        Integer memberCount = 42;
        slackChannel.setMemberCount(memberCount);
        assertThat(slackChannel.getMemberCount()).isEqualTo(memberCount);
    }

    @Test
    void testSetAndGetCreated() {
        Long created = System.currentTimeMillis() / 1000;
        slackChannel.setCreated(created);
        assertThat(slackChannel.getCreated()).isEqualTo(created);
    }

    @Test
    void testSetAndGetCreatorId() {
        String creatorId = "U1234567890";
        slackChannel.setCreatorId(creatorId);
        assertThat(slackChannel.getCreatorId()).isEqualTo(creatorId);
    }

    @Test
    void testDefaultValues() {
        SlackChannel channel = new SlackChannel();
        assertThat(channel.getId()).isNull();
        assertThat(channel.getName()).isNull();
        assertThat(channel.getTopic()).isNull();
        assertThat(channel.getPurpose()).isNull();
        assertThat(channel.isPrivateChannel()).isFalse();
        assertThat(channel.isArchived()).isFalse();
        assertThat(channel.getMemberCount()).isNull();
        assertThat(channel.getCreated()).isNull();
        assertThat(channel.getCreatorId()).isNull();
    }

    @Test
    void testEqualsAndHashCode() {
        SlackChannel channel1 = new SlackChannel();
        channel1.setId("C1234567890");
        channel1.setName("general");

        SlackChannel channel2 = new SlackChannel();
        channel2.setId("C1234567890");
        channel2.setName("general");

        assertThat(channel1).isEqualTo(channel2);
        assertThat(channel1.hashCode()).isEqualTo(channel2.hashCode());
    }

    @Test
    void testToString() {
        slackChannel.setId("C1234567890");
        slackChannel.setName("general");
        slackChannel.setTopic("General discussion");
        
        String toString = slackChannel.toString();
        assertThat(toString).contains("C1234567890");
        assertThat(toString).contains("general");
        assertThat(toString).contains("General discussion");
    }
}
