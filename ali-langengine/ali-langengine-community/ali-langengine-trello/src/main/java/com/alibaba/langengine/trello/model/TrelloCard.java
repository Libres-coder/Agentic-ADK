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

import com.alibaba.fastjson.JSONArray;
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
public class TrelloCard {
    
    /**
     * 卡片ID
     */
    private String id;
    
    /**
     * 卡片名称
     */
    private String name;
    
    /**
     * 卡片描述
     */
    private String desc;
    
    /**
     * 卡片URL
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
     * 所属列表ID
     */
    private String idList;
    
    /**
     * 所属看板ID
     */
    private String idBoard;
    
    /**
     * 在列表中的位置
     */
    private Double pos;
    
    /**
     * 到期时间
     */
    private LocalDateTime due;
    
    /**
     * 是否已完成到期项
     */
    private Boolean dueComplete;
    
    /**
     * 创建时间
     */
    private LocalDateTime dateLastActivity;
    
    /**
     * 成员ID列表
     */
    @Builder.Default
    private List<String> idMembers = new ArrayList<>();
    
    /**
     * 标签ID列表
     */
    @Builder.Default
    private List<String> idLabels = new ArrayList<>();
    
    /**
     * 清单ID列表
     */
    @Builder.Default
    private List<String> idChecklists = new ArrayList<>();
    
    /**
     * 附件数量
     */
    private Integer attachments;
    
    /**
     * 是否已订阅
     */
    private Boolean subscribed;
    
    /**
     * 封面图片ID
     */
    private String idAttachmentCover;
    
    /**
     * 从JSON对象创建TrelloCard实例
     * 
     * @param json JSON对象
     * @return TrelloCard实例
     */
    public static TrelloCard fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        
        TrelloCard card = new TrelloCard();
        card.setId(json.getString("id"));
        card.setName(json.getString("name"));
        card.setDesc(json.getString("desc"));
        card.setUrl(json.getString("url"));
        card.setShortUrl(json.getString("shortUrl"));
        card.setClosed(json.getBoolean("closed"));
        card.setIdList(json.getString("idList"));
        card.setIdBoard(json.getString("idBoard"));
        card.setPos(json.getDouble("pos"));
        card.setDueComplete(json.getBoolean("dueComplete"));
        card.setSubscribed(json.getBoolean("subscribed"));
        card.setIdAttachmentCover(json.getString("idAttachmentCover"));
        
        // 处理到期时间
        String dueStr = json.getString("due");
        if (dueStr != null && !dueStr.isEmpty()) {
            try {
                card.setDue(LocalDateTime.parse(dueStr, DateTimeFormatter.ISO_DATE_TIME));
            } catch (Exception e) {
                // 忽略日期解析错误
            }
        }
        
        // 处理最后活动时间
        String dateStr = json.getString("dateLastActivity");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                card.setDateLastActivity(
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
            card.setIdMembers(members);
        }
        
        // 处理标签列表
        if (json.containsKey("idLabels") && json.getJSONArray("idLabels") != null) {
            List<String> labels = new ArrayList<>();
            json.getJSONArray("idLabels").forEach(item -> {
                if (item instanceof String) {
                    labels.add((String) item);
                }
            });
            card.setIdLabels(labels);
        }
        
        // 处理清单列表
        if (json.containsKey("idChecklists") && json.getJSONArray("idChecklists") != null) {
            List<String> checklists = new ArrayList<>();
            json.getJSONArray("idChecklists").forEach(item -> {
                if (item instanceof String) {
                    checklists.add((String) item);
                }
            });
            card.setIdChecklists(checklists);
        }
        
        // 处理附件数量
        if (json.containsKey("badges")) {
            JSONObject badges = json.getJSONObject("badges");
            if (badges != null) {
                card.setAttachments(badges.getInteger("attachments"));
            }
        }
        
        return card;
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
        if (idList != null) json.put("idList", idList);
        if (idBoard != null) json.put("idBoard", idBoard);
        if (pos != null) json.put("pos", pos);
        if (dueComplete != null) json.put("dueComplete", dueComplete);
        if (subscribed != null) json.put("subscribed", subscribed);
        if (idAttachmentCover != null) json.put("idAttachmentCover", idAttachmentCover);
        
        if (due != null) {
            json.put("due", due.format(DateTimeFormatter.ISO_DATE_TIME));
        }
        
        if (dateLastActivity != null) {
            json.put("dateLastActivity", 
                dateLastActivity.format(DateTimeFormatter.ISO_DATE_TIME));
        }
        
        if (idMembers != null && !idMembers.isEmpty()) {
            JSONArray membersArray = new JSONArray();
            membersArray.addAll(idMembers);
            json.put("idMembers", membersArray);
        }
        
        if (idLabels != null && !idLabels.isEmpty()) {
            JSONArray labelsArray = new JSONArray();
            labelsArray.addAll(idLabels);
            json.put("idLabels", labelsArray);
        }
        
        if (idChecklists != null && !idChecklists.isEmpty()) {
            JSONArray checklistsArray = new JSONArray();
            checklistsArray.addAll(idChecklists);
            json.put("idChecklists", checklistsArray);
        }
        
        return json;
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
     * 是否有到期时间
     * 
     * @return 是否有到期时间
     */
    public boolean hasDueDate() {
        return due != null;
    }
    
    /**
     * 是否已过期
     * 
     * @return 是否已过期
     */
    public boolean isOverdue() {
        return due != null && due.isBefore(LocalDateTime.now()) && !isDueComplete();
    }
    
    /**
     * 是否已完成到期项
     * 
     * @return 是否已完成到期项
     */
    public boolean isDueComplete() {
        return dueComplete != null && dueComplete;
    }
    
    /**
     * 是否有成员
     * 
     * @return 是否有成员
     */
    public boolean hasMembers() {
        return idMembers != null && !idMembers.isEmpty();
    }
    
    /**
     * 是否有标签
     * 
     * @return 是否有标签
     */
    public boolean hasLabels() {
        return idLabels != null && !idLabels.isEmpty();
    }
    
    /**
     * 是否有清单
     * 
     * @return 是否有清单
     */
    public boolean hasChecklists() {
        return idChecklists != null && !idChecklists.isEmpty();
    }
    
    /**
     * 是否有附件
     * 
     * @return 是否有附件
     */
    public boolean hasAttachments() {
        return attachments != null && attachments > 0;
    }
    
    /**
     * 获取成员数量
     * 
     * @return 成员数量
     */
    public int getMemberCount() {
        return idMembers != null ? idMembers.size() : 0;
    }
    
    /**
     * 获取标签数量
     * 
     * @return 标签数量
     */
    public int getLabelCount() {
        return idLabels != null ? idLabels.size() : 0;
    }
    
    /**
     * 获取清单数量
     * 
     * @return 清单数量
     */
    public int getChecklistCount() {
        return idChecklists != null ? idChecklists.size() : 0;
    }
    
    /**
     * 获取附件数量
     * 
     * @return 附件数量
     */
    public int getAttachmentCount() {
        return attachments != null ? attachments : 0;
    }
    
    /**
     * 获取卡片的简短描述
     * 
     * @return 卡片的简短描述
     */
    public String getShortDescription() {
        if (desc == null || desc.length() <= 100) {
            return desc;
        }
        return desc.substring(0, 97) + "...";
    }
    
    @Override
    public String toString() {
        return String.format("TrelloCard{id='%s', name='%s', closed=%s, list='%s'}", 
                           id, name, closed, idList);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrelloCard that = (TrelloCard) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
