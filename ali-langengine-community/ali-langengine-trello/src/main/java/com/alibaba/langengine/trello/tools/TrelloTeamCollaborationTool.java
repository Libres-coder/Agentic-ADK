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
import com.alibaba.langengine.trello.model.TrelloMember;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


@Slf4j
@Data
public class TrelloTeamCollaborationTool extends DefaultTool {
    
    private TrelloClient trelloClient;
    
    /**
     * 默认构造函数
     */
    public TrelloTeamCollaborationTool() {
        setName("trello_team_collaboration");
        setFunctionName("collaborateInTrello");
        setHumanName("Trello团队协作");
        setDescription("管理Trello团队协作，支持成员管理、卡片分配、团队通知等功能");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration Trello配置
     */
    public TrelloTeamCollaborationTool(TrelloConfiguration configuration) {
        this();
        this.trelloClient = new TrelloClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param trelloClient Trello客户端
     */
    public TrelloTeamCollaborationTool(TrelloClient trelloClient) {
        this();
        this.trelloClient = trelloClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行团队协作操作
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (trelloClient == null) {
                return new ToolExecuteResult("Trello客户端未初始化，请先配置Trello API Key和Token", true);
            }
            
            log.info("执行Trello团队协作操作，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String action = input.getString("action");
            
            if (StringUtils.isBlank(action)) {
                return new ToolExecuteResult("操作类型不能为空，支持的操作: get_current_member, get_board_members, assign_member, remove_member, add_comment, get_card_members", true);
            }
            
            switch (action.toLowerCase()) {
                case "get_current_member":
                    return getCurrentMember(input);
                case "get_board_members":
                    return getBoardMembers(input);
                case "assign_member":
                    return assignMemberToCard(input);
                case "remove_member":
                    return removeMemberFromCard(input);
                case "add_comment":
                    return addCommentToCard(input);
                case "get_card_members":
                    return getCardMembers(input);
                default:
                    return new ToolExecuteResult("不支持的操作类型: " + action + "，支持的操作: get_current_member, get_board_members, assign_member, remove_member, add_comment, get_card_members", true);
            }
            
        } catch (Exception e) {
            log.error("执行Trello团队协作操作失败", e);
            return new ToolExecuteResult("执行团队协作操作失败: " + e.getMessage(), true);
        }
    }
    
    /**
     * 获取当前用户信息
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult getCurrentMember(JSONObject input) throws TrelloException {
        TrelloMember member = trelloClient.getCurrentMember();
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("member", member.toJson());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 获取看板成员
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult getBoardMembers(JSONObject input) throws TrelloException {
        String boardId = input.getString("boardId");
        
        if (StringUtils.isBlank(boardId)) {
            return new ToolExecuteResult("看板ID不能为空", true);
        }
        
        JSONObject response = trelloClient.get(String.format("/boards/%s/members", boardId));
        JSONArray membersArray;
        
        // 检查响应数据结构
        if (response.containsKey("data")) {
            Object dataObj = response.get("data");
            if (dataObj instanceof JSONArray) {
                membersArray = (JSONArray) dataObj;
            } else {
                membersArray = new JSONArray();
            }
        } else {
            membersArray = new JSONArray();
        }
        
        JSONArray members = new JSONArray();
        for (Object memberObj : membersArray) {
            if (memberObj instanceof JSONObject) {
                JSONObject memberJson = (JSONObject) memberObj;
                TrelloMember member = TrelloMember.fromJson(memberJson);
                
                JSONObject memberSummary = new JSONObject();
                memberSummary.put("id", member.getId());
                memberSummary.put("username", member.getUsername());
                memberSummary.put("fullName", member.getFullName());
                memberSummary.put("displayName", member.getDisplayName());
                memberSummary.put("avatarUrl", member.getAvatarUrlOrDefault());
                memberSummary.put("memberType", member.getMemberType());
                memberSummary.put("confirmed", member.isConfirmed());
                
                members.add(memberSummary);
            }
        }
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", String.format("获取到 %d 位成员", members.size()));
        result.put("members", members);
        result.put("count", members.size());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 将成员分配到卡片
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult assignMemberToCard(JSONObject input) throws TrelloException {
        String cardId = input.getString("cardId");
        String memberId = input.getString("memberId");
        
        if (StringUtils.isBlank(cardId)) {
            return new ToolExecuteResult("卡片ID不能为空", true);
        }
        if (StringUtils.isBlank(memberId)) {
            return new ToolExecuteResult("成员ID不能为空", true);
        }
        
        // 添加成员到卡片
        JSONObject updateData = new JSONObject();
        updateData.put("value", memberId);
        
        JSONObject response = trelloClient.post(
            String.format("/cards/%s/idMembers", cardId), 
            updateData
        );
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", "成员分配成功");
        result.put("cardId", cardId);
        result.put("memberId", memberId);
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 从卡片移除成员
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult removeMemberFromCard(JSONObject input) throws TrelloException {
        String cardId = input.getString("cardId");
        String memberId = input.getString("memberId");
        
        if (StringUtils.isBlank(cardId)) {
            return new ToolExecuteResult("卡片ID不能为空", true);
        }
        if (StringUtils.isBlank(memberId)) {
            return new ToolExecuteResult("成员ID不能为空", true);
        }
        
        // 从卡片移除成员
        JSONObject response = trelloClient.delete(
            String.format("/cards/%s/idMembers/%s", cardId, memberId)
        );
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", "成员移除成功");
        result.put("cardId", cardId);
        result.put("memberId", memberId);
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 为卡片添加评论
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult addCommentToCard(JSONObject input) throws TrelloException {
        String cardId = input.getString("cardId");
        String text = input.getString("text");
        
        if (StringUtils.isBlank(cardId)) {
            return new ToolExecuteResult("卡片ID不能为空", true);
        }
        if (StringUtils.isBlank(text)) {
            return new ToolExecuteResult("评论内容不能为空", true);
        }
        
        // 添加评论到卡片
        JSONObject commentData = new JSONObject();
        commentData.put("text", text);
        
        JSONObject response = trelloClient.post(
            String.format("/cards/%s/actions/comments", cardId), 
            commentData
        );
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", "评论添加成功");
        result.put("cardId", cardId);
        result.put("comment", new JSONObject() {{
            put("id", response.getString("id"));
            put("text", text);
            put("date", response.getString("date"));
            put("memberCreator", response.getJSONObject("memberCreator"));
        }});
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 获取卡片的成员信息
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult getCardMembers(JSONObject input) throws TrelloException {
        String cardId = input.getString("cardId");
        
        if (StringUtils.isBlank(cardId)) {
            return new ToolExecuteResult("卡片ID不能为空", true);
        }
        
        JSONObject response = trelloClient.get(String.format("/cards/%s/members", cardId));
        JSONArray membersArray;
        
        // 检查响应数据结构
        if (response.containsKey("data")) {
            Object dataObj = response.get("data");
            if (dataObj instanceof JSONArray) {
                membersArray = (JSONArray) dataObj;
            } else {
                membersArray = new JSONArray();
            }
        } else {
            membersArray = new JSONArray();
        }
        
        JSONArray members = new JSONArray();
        for (Object memberObj : membersArray) {
            if (memberObj instanceof JSONObject) {
                JSONObject memberJson = (JSONObject) memberObj;
                TrelloMember member = TrelloMember.fromJson(memberJson);
                
                JSONObject memberSummary = new JSONObject();
                memberSummary.put("id", member.getId());
                memberSummary.put("username", member.getUsername());
                memberSummary.put("fullName", member.getFullName());
                memberSummary.put("displayName", member.getDisplayName());
                memberSummary.put("avatarUrl", member.getAvatarUrlOrDefault());
                
                members.add(memberSummary);
            }
        }
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", String.format("卡片有 %d 位成员", members.size()));
        result.put("cardId", cardId);
        result.put("members", members);
        result.put("count", members.size());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
}
