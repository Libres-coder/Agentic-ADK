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
package com.alibaba.langengine.trello.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.trello.TrelloConfiguration;
import com.alibaba.langengine.trello.client.TrelloClient;
import com.alibaba.langengine.trello.exception.TrelloException;
import com.alibaba.langengine.trello.model.TrelloCard;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Data
public class TrelloCardManageTool extends DefaultTool {
    
    private TrelloClient trelloClient;
    
    /**
     * 默认构造函数
     */
    public TrelloCardManageTool() {
        setName("trello_card_manage");
        setFunctionName("manageTrelloCard");
        setHumanName("Trello卡片管理");
        setDescription("管理Trello卡片，支持创建、更新、删除、移动卡片等操作");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration Trello配置
     */
    public TrelloCardManageTool(TrelloConfiguration configuration) {
        this();
        this.trelloClient = new TrelloClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param trelloClient Trello客户端
     */
    public TrelloCardManageTool(TrelloClient trelloClient) {
        this();
        this.trelloClient = trelloClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行卡片管理操作
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (trelloClient == null) {
                return new ToolExecuteResult("Trello客户端未初始化，请先配置Trello API Key和Token", true);
            }
            
            log.info("执行Trello卡片管理操作，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String action = input.getString("action");
            
            if (StringUtils.isBlank(action)) {
                return new ToolExecuteResult("操作类型不能为空，支持的操作: create, update, delete, move, get, list", true);
            }
            
            switch (action.toLowerCase()) {
                case "create":
                    return createCard(input);
                case "update":
                    return updateCard(input);
                case "delete":
                    return deleteCard(input);
                case "move":
                    return moveCard(input);
                case "get":
                    return getCard(input);
                case "list":
                    return listCards(input);
                default:
                    return new ToolExecuteResult("不支持的操作类型: " + action + "，支持的操作: create, update, delete, move, get, list", true);
            }
            
        } catch (Exception e) {
            log.error("执行Trello卡片管理操作失败", e);
            return new ToolExecuteResult("执行卡片管理操作失败: " + e.getMessage(), true);
        }
    }
    
    /**
     * 创建卡片
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult createCard(JSONObject input) throws TrelloException {
        String name = input.getString("name");
        String desc = input.getString("desc");
        String idList = input.getString("idList");
        String due = input.getString("due");
        JSONArray idMembers = input.getJSONArray("idMembers");
        JSONArray idLabels = input.getJSONArray("idLabels");
        Double pos = input.getDouble("pos");
        
        // 验证必需参数
        if (StringUtils.isBlank(name)) {
            return new ToolExecuteResult("卡片名称不能为空", true);
        }
        if (StringUtils.isBlank(idList)) {
            return new ToolExecuteResult("列表ID不能为空", true);
        }
        
        // 构建卡片对象
        TrelloCard card = TrelloCard.builder()
                .name(name)
                .desc(desc)
                .idList(idList)
                .pos(pos)
                .build();
        
        // 处理到期时间
        if (StringUtils.isNotBlank(due)) {
            try {
                card.setDue(LocalDateTime.parse(due, DateTimeFormatter.ISO_DATE_TIME));
            } catch (Exception e) {
                return new ToolExecuteResult("到期时间格式不正确，请使用ISO 8601格式", true);
            }
        }
        
        // 处理成员列表
        if (idMembers != null && !idMembers.isEmpty()) {
            List<String> members = new ArrayList<>();
            for (Object member : idMembers) {
                if (member instanceof String) {
                    members.add((String) member);
                }
            }
            card.setIdMembers(members);
        }
        
        // 处理标签列表
        if (idLabels != null && !idLabels.isEmpty()) {
            List<String> labels = new ArrayList<>();
            for (Object label : idLabels) {
                if (label instanceof String) {
                    labels.add((String) label);
                }
            }
            card.setIdLabels(labels);
        }
        
        TrelloCard createdCard = trelloClient.createCard(card);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", "卡片创建成功");
        result.put("card", createdCard.toJson());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 更新卡片
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult updateCard(JSONObject input) throws TrelloException {
        String cardId = input.getString("cardId");
        
        if (StringUtils.isBlank(cardId)) {
            return new ToolExecuteResult("卡片ID不能为空", true);
        }
        
        // 构建更新的卡片对象
        TrelloCard card = new TrelloCard();
        
        if (input.containsKey("name")) {
            card.setName(input.getString("name"));
        }
        if (input.containsKey("desc")) {
            card.setDesc(input.getString("desc"));
        }
        if (input.containsKey("idList")) {
            card.setIdList(input.getString("idList"));
        }
        if (input.containsKey("pos")) {
            card.setPos(input.getDouble("pos"));
        }
        if (input.containsKey("closed")) {
            card.setClosed(input.getBoolean("closed"));
        }
        if (input.containsKey("dueComplete")) {
            card.setDueComplete(input.getBoolean("dueComplete"));
        }
        
        // 处理到期时间
        if (input.containsKey("due")) {
            String due = input.getString("due");
            if (StringUtils.isNotBlank(due)) {
                try {
                    card.setDue(LocalDateTime.parse(due, DateTimeFormatter.ISO_DATE_TIME));
                } catch (Exception e) {
                    return new ToolExecuteResult("到期时间格式不正确，请使用ISO 8601格式", true);
                }
            }
        }
        
        // 处理成员列表
        if (input.containsKey("idMembers")) {
            JSONArray idMembers = input.getJSONArray("idMembers");
            if (idMembers != null) {
                List<String> members = new ArrayList<>();
                for (Object member : idMembers) {
                    if (member instanceof String) {
                        members.add((String) member);
                    }
                }
                card.setIdMembers(members);
            }
        }
        
        // 处理标签列表
        if (input.containsKey("idLabels")) {
            JSONArray idLabels = input.getJSONArray("idLabels");
            if (idLabels != null) {
                List<String> labels = new ArrayList<>();
                for (Object label : idLabels) {
                    if (label instanceof String) {
                        labels.add((String) label);
                    }
                }
                card.setIdLabels(labels);
            }
        }
        
        TrelloCard updatedCard = trelloClient.updateCard(cardId, card);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", "卡片更新成功");
        result.put("card", updatedCard.toJson());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 删除卡片
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult deleteCard(JSONObject input) throws TrelloException {
        String cardId = input.getString("cardId");
        
        if (StringUtils.isBlank(cardId)) {
            return new ToolExecuteResult("卡片ID不能为空", true);
        }
        
        trelloClient.deleteCard(cardId);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", "卡片删除成功");
        result.put("cardId", cardId);
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 移动卡片
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult moveCard(JSONObject input) throws TrelloException {
        String cardId = input.getString("cardId");
        String targetListId = input.getString("targetListId");
        
        if (StringUtils.isBlank(cardId)) {
            return new ToolExecuteResult("卡片ID不能为空", true);
        }
        if (StringUtils.isBlank(targetListId)) {
            return new ToolExecuteResult("目标列表ID不能为空", true);
        }
        
        TrelloCard movedCard = trelloClient.moveCard(cardId, targetListId);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", "卡片移动成功");
        result.put("card", movedCard.toJson());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 获取单个卡片信息
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult getCard(JSONObject input) throws TrelloException {
        String cardId = input.getString("cardId");
        
        if (StringUtils.isBlank(cardId)) {
            return new ToolExecuteResult("卡片ID不能为空", true);
        }
        
        JSONObject cardInfo = trelloClient.get("/cards/" + cardId);
        TrelloCard card = TrelloCard.fromJson(cardInfo);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("card", card.toJson());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 获取列表中的卡片
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult listCards(JSONObject input) throws TrelloException {
        String listId = input.getString("listId");
        
        if (StringUtils.isBlank(listId)) {
            return new ToolExecuteResult("列表ID不能为空", true);
        }
        
        List<TrelloCard> cards = trelloClient.getCards(listId);
        
        JSONArray cardsArray = new JSONArray();
        for (TrelloCard card : cards) {
            cardsArray.add(card.toJson());
        }
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", String.format("获取到 %d 张卡片", cards.size()));
        result.put("cards", cardsArray);
        result.put("count", cards.size());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
}
