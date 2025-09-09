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

import lombok.Data;

import java.time.Instant;


@Data
public class SlackMessage {
    
    /**
     * 消息时间戳
     */
    private String ts;
    
    /**
     * 频道ID
     */
    private String channelId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 消息文本内容
     */
    private String text;
    
    /**
     * 线程时间戳（用于线程回复）
     */
    private String threadTs;
    
    /**
     * 消息时间戳（Java时间类型）
     */
    private Instant timestamp;
    
    /**
     * 消息类型
     */
    private String type = "message";
    
    /**
     * 子类型
     */
    private String subtype;
    
    /**
     * 是否被编辑
     */
    private boolean edited;
    
    /**
     * 是否被删除
     */
    private boolean deleted;
    
    /**
     * 消息附件
     */
    private String attachments;
    
    /**
     * 消息块（Block Kit）
     */
    private String blocks;
}
