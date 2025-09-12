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
package com.alibaba.langengine.trello.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrelloMember {
    
    /**
     * 成员ID
     */
    private String id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 全名
     */
    private String fullName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 是否已确认邮箱
     */
    private Boolean confirmed;
    
    /**
     * 成员类型
     */
    private String memberType;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 从JSON对象创建TrelloMember实例
     * 
     * @param json JSON对象
     * @return TrelloMember实例
     */
    public static TrelloMember fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        
        TrelloMember member = new TrelloMember();
        member.setId(json.getString("id"));
        member.setUsername(json.getString("username"));
        member.setFullName(json.getString("fullName"));
        member.setEmail(json.getString("email"));
        member.setBio(json.getString("bio"));
        member.setConfirmed(json.getBoolean("confirmed"));
        member.setMemberType(json.getString("memberType"));
        member.setStatus(json.getString("status"));
        
        // 处理头像URL
        if (json.containsKey("avatarUrl")) {
            member.setAvatarUrl(json.getString("avatarUrl"));
        } else if (json.containsKey("avatarHash")) {
            String avatarHash = json.getString("avatarHash");
            if (avatarHash != null && !avatarHash.isEmpty()) {
                member.setAvatarUrl("https://trello-avatars.s3.amazonaws.com/" + avatarHash + "/170.png");
            }
        }
        
        return member;
    }
    
    /**
     * 转换为JSON对象
     * 
     * @return JSON对象
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        
        if (id != null) json.put("id", id);
        if (username != null) json.put("username", username);
        if (fullName != null) json.put("fullName", fullName);
        if (email != null) json.put("email", email);
        if (avatarUrl != null) json.put("avatarUrl", avatarUrl);
        if (bio != null) json.put("bio", bio);
        if (confirmed != null) json.put("confirmed", confirmed);
        if (memberType != null) json.put("memberType", memberType);
        if (status != null) json.put("status", status);
        
        return json;
    }
    
    /**
     * 是否已确认邮箱
     * 
     * @return 是否已确认邮箱
     */
    public boolean isConfirmed() {
        return confirmed != null && confirmed;
    }
    
    /**
     * 是否为管理员
     * 
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return "admin".equals(memberType);
    }
    
    /**
     * 是否为普通成员
     * 
     * @return 是否为普通成员
     */
    public boolean isNormal() {
        return "normal".equals(memberType);
    }
    
    /**
     * 是否在线
     * 
     * @return 是否在线
     */
    public boolean isActive() {
        return "active".equals(status);
    }
    
    /**
     * 获取显示名称
     * 
     * @return 显示名称（优先使用全名，否则使用用户名）
     */
    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        return username;
    }
    
    /**
     * 获取头像URL（如果没有则返回默认头像）
     * 
     * @return 头像URL
     */
    public String getAvatarUrlOrDefault() {
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            return avatarUrl;
        }
        return "https://trello.com/1/members/" + id + "/avatar?size=170";
    }
    
    @Override
    public String toString() {
        return String.format("TrelloMember{id='%s', username='%s', fullName='%s', confirmed=%s}", 
                           id, username, fullName, confirmed);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrelloMember that = (TrelloMember) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
