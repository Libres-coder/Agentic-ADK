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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrelloBoard {
    
    /**
     * 看板ID
     */
    private String id;
    
    /**
     * 看板名称
     */
    private String name;
    
    /**
     * 看板描述
     */
    private String desc;
    
    /**
     * 看板URL
     */
    private String url;
    
    /**
     * 短URL
     */
    private String shortUrl;
    
    /**
     * 是否已关闭
     */
    private Boolean closed;
    
    /**
     * 组织ID
     */
    private String idOrganization;
    
    /**
     * 是否已固定
     */
    private Boolean pinned;
    
    /**
     * 是否已加星标
     */
    private Boolean starred;
    
    /**
     * 背景颜色
     */
    private String prefs;
    
    /**
     * 创建时间
     */
    private LocalDateTime dateLastActivity;
    
    /**
     * 成员列表
     */
    private List<String> idMembers = new ArrayList<>();
    
    /**
     * 标签列表
     */
    private List<TrelloLabel> labels = new ArrayList<>();
    
    /**
     * 从JSON对象创建TrelloBoard实例
     * 
     * @param json JSON对象
     * @return TrelloBoard实例
     */
    public static TrelloBoard fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        
        TrelloBoard board = new TrelloBoard();
        board.setId(json.getString("id"));
        board.setName(json.getString("name"));
        board.setDesc(json.getString("desc"));
        board.setUrl(json.getString("url"));
        board.setShortUrl(json.getString("shortUrl"));
        board.setClosed(json.getBoolean("closed"));
        board.setIdOrganization(json.getString("idOrganization"));
        board.setPinned(json.getBoolean("pinned"));
        board.setStarred(json.getBoolean("starred"));
        
        // 处理prefs对象
        JSONObject prefsObj = json.getJSONObject("prefs");
        if (prefsObj != null) {
            board.setPrefs(prefsObj.toJSONString());
        }
        
        // 处理日期
        String dateStr = json.getString("dateLastActivity");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                board.setDateLastActivity(
                    LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME)
                );
            } catch (Exception e) {
                // 忽略日期解析错误
            }
        }
        
        // 处理成员列表
        if (json.containsKey("idMembers") && json.getJSONArray("idMembers") != null) {
            List<String> members = new ArrayList<>();
            json.getJSONArray("idMembers").forEach(item -> {
                if (item instanceof String) {
                    members.add((String) item);
                }
            });
            board.setIdMembers(members);
        }
        
        return board;
    }
    
    /**
     * 转换为JSON对象
     * 
     * @return JSON对象
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        
        if (id != null) json.put("id", id);
        if (name != null) json.put("name", name);
        if (desc != null) json.put("desc", desc);
        if (url != null) json.put("url", url);
        if (shortUrl != null) json.put("shortUrl", shortUrl);
        if (closed != null) json.put("closed", closed);
        if (idOrganization != null) json.put("idOrganization", idOrganization);
        if (pinned != null) json.put("pinned", pinned);
        if (starred != null) json.put("starred", starred);
        if (prefs != null) json.put("prefs", prefs);
        
        if (dateLastActivity != null) {
            json.put("dateLastActivity", 
                dateLastActivity.format(DateTimeFormatter.ISO_DATE_TIME));
        }
        
        if (idMembers != null && !idMembers.isEmpty()) {
            json.put("idMembers", idMembers);
        }
        
        return json;
    }
    
    /**
     * 是否为公开看板
     * 
     * @return 是否为公开看板
     */
    public boolean isPublic() {
        return prefs != null && prefs.contains("public");
    }
    
    /**
     * 是否已关闭
     * 
     * @return 是否已关闭
     */
    public boolean isClosed() {
        return closed != null && closed;
    }
    
    /**
     * 是否已加星标
     * 
     * @return 是否已加星标
     */
    public boolean isStarred() {
        return starred != null && starred;
    }
    
    /**
     * 获取看板的简短描述
     * 
     * @return 看板的简短描述
     */
    public String getShortDescription() {
        if (desc == null || desc.length() <= 100) {
            return desc;
        }
        return desc.substring(0, 97) + "...";
    }
    
    @Override
    public String toString() {
        return String.format("TrelloBoard{id='%s', name='%s', closed=%s, starred=%s}", 
                           id, name, closed, starred);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrelloBoard that = (TrelloBoard) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
