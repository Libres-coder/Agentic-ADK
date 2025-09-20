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


@Data
public class SlackChannel {
    
    /**
     * 频道ID
     */
    private String id;
    
    /**
     * 频道名称
     */
    private String name;
    
    /**
     * 频道主题
     */
    private String topic;
    
    /**
     * 频道目的
     */
    private String purpose;
    
    /**
     * 是否为私有频道
     */
    private boolean privateChannel;
    
    /**
     * 是否已归档
     */
    private boolean archived;
    
    /**
     * 成员数量
     */
    private Integer memberCount;
    
    /**
     * 频道创建时间戳
     */
    private Long created;
    
    /**
     * 频道创建者ID
     */
    private String creatorId;
}
