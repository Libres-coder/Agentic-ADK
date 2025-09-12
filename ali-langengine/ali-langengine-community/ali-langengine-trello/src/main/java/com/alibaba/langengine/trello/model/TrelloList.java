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
public class TrelloList {
    
    /**
     * 列表ID
     */
    private String id;
    
    /**
     * 列表名称
     */
    private String name;
    
    /**
     * 是否已关闭
     */
    private Boolean closed;
    
    /**
     * 所属看板ID
     */
    private String idBoard;
    
    /**
     * 在看板中的位置
     */
    private Double pos;
    
    /**
     * 是否已订阅
     */
    private Boolean subscribed;
    
    /**
     * 软限制（卡片数量限制）
     */
    private Integer softLimit;
    
    /**
     * 限制类型
     */
    private String limitType;
    
    /**
     * 从JSON对象创建TrelloList实例
     * 
     * @param json JSON对象
     * @return TrelloList实例
     */
    public static TrelloList fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        
        TrelloList list = new TrelloList();
        list.setId(json.getString("id"));
        list.setName(json.getString("name"));
        list.setClosed(json.getBoolean("closed"));
        list.setIdBoard(json.getString("idBoard"));
        list.setPos(json.getDouble("pos"));
        list.setSubscribed(json.getBoolean("subscribed"));
        
        // 处理limits对象
        JSONObject limits = json.getJSONObject("limits");
        if (limits != null) {
            JSONObject cards = limits.getJSONObject("cards");
            if (cards != null) {
                list.setSoftLimit(cards.getInteger("softLimit"));
                list.setLimitType(cards.getString("limitType"));
            }
        }
        
        return list;
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
        if (closed != null) json.put("closed", closed);
        if (idBoard != null) json.put("idBoard", idBoard);
        if (pos != null) json.put("pos", pos);
        if (subscribed != null) json.put("subscribed", subscribed);
        
        if (softLimit != null || limitType != null) {
            JSONObject limits = new JSONObject();
            JSONObject cards = new JSONObject();
            if (softLimit != null) cards.put("softLimit", softLimit);
            if (limitType != null) cards.put("limitType", limitType);
            limits.put("cards", cards);
            json.put("limits", limits);
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
     * 是否已订阅
     * 
     * @return 是否已订阅
     */
    public boolean isSubscribed() {
        return subscribed != null && subscribed;
    }
    
    /**
     * 是否有卡片数量限制
     * 
     * @return 是否有卡片数量限制
     */
    public boolean hasCardLimit() {
        return softLimit != null && softLimit > 0;
    }
    
    /**
     * 获取卡片限制数量
     * 
     * @return 卡片限制数量，如果没有限制则返回-1
     */
    public int getCardLimit() {
        return softLimit != null ? softLimit : -1;
    }
    
    @Override
    public String toString() {
        return String.format("TrelloList{id='%s', name='%s', closed=%s, pos=%.2f}", 
                           id, name, closed, pos);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrelloList that = (TrelloList) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
