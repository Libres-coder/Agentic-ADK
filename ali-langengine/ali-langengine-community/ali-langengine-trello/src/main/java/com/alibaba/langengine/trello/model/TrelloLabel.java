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
public class TrelloLabel {
    
    /**
     * 标签ID
     */
    private String id;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 标签颜色
     */
    private String color;
    
    /**
     * 所属看板ID
     */
    private String idBoard;
    
    /**
     * 使用次数
     */
    private Integer uses;
    
    /**
     * 从JSON对象创建TrelloLabel实例
     * 
     * @param json JSON对象
     * @return TrelloLabel实例
     */
    public static TrelloLabel fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        
        TrelloLabel label = new TrelloLabel();
        label.setId(json.getString("id"));
        label.setName(json.getString("name"));
        label.setColor(json.getString("color"));
        label.setIdBoard(json.getString("idBoard"));
        label.setUses(json.getInteger("uses"));
        
        return label;
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
        if (color != null) json.put("color", color);
        if (idBoard != null) json.put("idBoard", idBoard);
        if (uses != null) json.put("uses", uses);
        
        return json;
    }
    
    /**
     * 获取颜色的十六进制值
     * 
     * @return 颜色的十六进制值
     */
    public String getColorHex() {
        if (color == null) {
            return "#808080"; // 默认灰色
        }
        
        switch (color.toLowerCase()) {
            case "green":
                return "#61BD4F";
            case "yellow":
                return "#F2D600";
            case "orange":
                return "#FF9F1A";
            case "red":
                return "#EB5A46";
            case "purple":
                return "#C377E0";
            case "blue":
                return "#0079BF";
            case "sky":
                return "#00C2E0";
            case "lime":
                return "#51E898";
            case "pink":
                return "#FF78CB";
            case "black":
                return "#355263";
            default:
                return "#808080";
        }
    }
    
    /**
     * 是否为系统默认颜色
     * 
     * @return 是否为系统默认颜色
     */
    public boolean isDefaultColor() {
        if (color == null) {
            return true;
        }
        
        String[] defaultColors = {
            "green", "yellow", "orange", "red", "purple", 
            "blue", "sky", "lime", "pink", "black"
        };
        
        for (String defaultColor : defaultColors) {
            if (defaultColor.equals(color.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取使用次数
     * 
     * @return 使用次数
     */
    public int getUsageCount() {
        return uses != null ? uses : 0;
    }
    
    /**
     * 是否有名称
     * 
     * @return 是否有名称
     */
    public boolean hasName() {
        return name != null && !name.trim().isEmpty();
    }
    
    /**
     * 获取显示名称
     * 
     * @return 显示名称（如果没有名称则返回颜色）
     */
    public String getDisplayName() {
        if (hasName()) {
            return name;
        }
        return color != null ? color : "未命名标签";
    }
    
    @Override
    public String toString() {
        return String.format("TrelloLabel{id='%s', name='%s', color='%s', uses=%d}", 
                           id, name, color, getUsageCount());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TrelloLabel that = (TrelloLabel) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
