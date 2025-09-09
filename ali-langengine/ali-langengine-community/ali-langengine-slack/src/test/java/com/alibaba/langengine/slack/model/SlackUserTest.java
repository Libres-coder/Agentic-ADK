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


class SlackUserTest {

    private SlackUser slackUser;

    @BeforeEach
    void setUp() {
        slackUser = new SlackUser();
    }

    @Test
    void testSetAndGetId() {
        String id = "U1234567890";
        slackUser.setId(id);
        assertThat(slackUser.getId()).isEqualTo(id);
    }

    @Test
    void testSetAndGetName() {
        String name = "john.doe";
        slackUser.setName(name);
        assertThat(slackUser.getName()).isEqualTo(name);
    }

    @Test
    void testSetAndGetRealName() {
        String realName = "John Doe";
        slackUser.setRealName(realName);
        assertThat(slackUser.getRealName()).isEqualTo(realName);
    }

    @Test
    void testSetAndGetDisplayName() {
        String displayName = "Johnny";
        slackUser.setDisplayName(displayName);
        assertThat(slackUser.getDisplayName()).isEqualTo(displayName);
    }

    @Test
    void testSetAndGetEmail() {
        String email = "john.doe@example.com";
        slackUser.setEmail(email);
        assertThat(slackUser.getEmail()).isEqualTo(email);
    }

    @Test
    void testSetAndGetBot() {
        slackUser.setBot(true);
        assertThat(slackUser.isBot()).isTrue();

        slackUser.setBot(false);
        assertThat(slackUser.isBot()).isFalse();
    }

    @Test
    void testSetAndGetDeleted() {
        slackUser.setDeleted(true);
        assertThat(slackUser.isDeleted()).isTrue();

        slackUser.setDeleted(false);
        assertThat(slackUser.isDeleted()).isFalse();
    }

    @Test
    void testSetAndGetAdmin() {
        slackUser.setAdmin(true);
        assertThat(slackUser.isAdmin()).isTrue();

        slackUser.setAdmin(false);
        assertThat(slackUser.isAdmin()).isFalse();
    }

    @Test
    void testSetAndGetTimezone() {
        String timezone = "America/New_York";
        slackUser.setTimezone(timezone);
        assertThat(slackUser.getTimezone()).isEqualTo(timezone);
    }

    @Test
    void testSetAndGetAvatarUrl() {
        String avatarUrl = "https://example.com/avatar.jpg";
        slackUser.setAvatarUrl(avatarUrl);
        assertThat(slackUser.getAvatarUrl()).isEqualTo(avatarUrl);
    }

    @Test
    void testSetAndGetStatusText() {
        String statusText = "Working from home";
        slackUser.setStatusText(statusText);
        assertThat(slackUser.getStatusText()).isEqualTo(statusText);
    }

    @Test
    void testSetAndGetStatusEmoji() {
        String statusEmoji = ":house:";
        slackUser.setStatusEmoji(statusEmoji);
        assertThat(slackUser.getStatusEmoji()).isEqualTo(statusEmoji);
    }

    @Test
    void testDefaultValues() {
        SlackUser user = new SlackUser();
        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getRealName()).isNull();
        assertThat(user.getDisplayName()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.isBot()).isFalse();
        assertThat(user.isDeleted()).isFalse();
        assertThat(user.isAdmin()).isFalse();
        assertThat(user.getTimezone()).isNull();
        assertThat(user.getAvatarUrl()).isNull();
        assertThat(user.getStatusText()).isNull();
        assertThat(user.getStatusEmoji()).isNull();
    }

    @Test
    void testEqualsAndHashCode() {
        SlackUser user1 = new SlackUser();
        user1.setId("U1234567890");
        user1.setName("john.doe");

        SlackUser user2 = new SlackUser();
        user2.setId("U1234567890");
        user2.setName("john.doe");

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void testToString() {
        slackUser.setId("U1234567890");
        slackUser.setName("john.doe");
        slackUser.setRealName("John Doe");
        
        String toString = slackUser.toString();
        assertThat(toString).contains("U1234567890");
        assertThat(toString).contains("john.doe");
        assertThat(toString).contains("John Doe");
    }
}
