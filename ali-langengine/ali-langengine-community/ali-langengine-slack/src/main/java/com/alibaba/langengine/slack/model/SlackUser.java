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
public class SlackUser {
    
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 用户名
     */
    private String name;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 显示名称
     */
    private String displayName;
    
    /**
     * 电子邮箱
     */
    private String email;
    
    /**
     * 是否为机器人
     */
    private boolean bot;
    
    /**
     * 是否已删除
     */
    private boolean deleted;
    
    /**
     * 是否为管理员
     */
    private boolean admin;
    
    /**
     * 时区
     */
    private String timezone;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 状态文本
     */
    private String statusText;
    
    /**
     * 状态表情符号
     */
    private String statusEmoji;
}
