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
import com.alibaba.langengine.trello.model.TrelloBoard;
import com.alibaba.langengine.trello.model.TrelloList;
import com.alibaba.langengine.trello.model.TrelloCard;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


@Slf4j
@Data
@EqualsAndHashCode(callSuper=false)
public class TrelloBoardOperationTool extends DefaultTool {
    
    private TrelloClient trelloClient;
    
    /**
     * 默认构造函数
     */
    public TrelloBoardOperationTool() {
        setName("trello_board_operation");
        setFunctionName("operateTrelloBoard");
        setHumanName("Trello看板操作");
        setDescription("操作Trello看板，支持查看看板信息、获取列表、看板概览等功能");
    }
    
    /**
     * 构造函数
     * 
     * @param configuration Trello配置
     */
    public TrelloBoardOperationTool(TrelloConfiguration configuration) {
        this();
        this.trelloClient = new TrelloClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param trelloClient Trello客户端
     */
    public TrelloBoardOperationTool(TrelloClient trelloClient) {
        this();
        this.trelloClient = trelloClient;
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        return execute(toolInput);
    }
    
    /**
     * 执行看板操作
     * 
     * @param toolInput 工具输入参数
     * @return 执行结果
     */
    public ToolExecuteResult execute(String toolInput) {
        try {
            if (trelloClient == null) {
                return new ToolExecuteResult("Trello客户端未初始化，请先配置Trello API Key和Token", true);
            }
            
            log.info("执行Trello看板操作，输入参数: {}", toolInput);
            
            JSONObject input = JSON.parseObject(toolInput);
            String action = input.getString("action");
            
            if (StringUtils.isBlank(action)) {
                return new ToolExecuteResult("操作类型不能为空，支持的操作: list_boards, get_board, get_lists, board_overview", true);
            }
            
            switch (action.toLowerCase()) {
                case "list_boards":
                    return listBoards(input);
                case "get_board":
                    return getBoard(input);
                case "get_lists":
                    return getLists(input);
                case "board_overview":
                    return getBoardOverview(input);
                default:
                    return new ToolExecuteResult("不支持的操作类型: " + action + "，支持的操作: list_boards, get_board, get_lists, board_overview", true);
            }
            
        } catch (Exception e) {
            log.error("执行Trello看板操作失败", e);
            return new ToolExecuteResult("执行看板操作失败: " + e.getMessage(), true);
        }
    }
    
    /**
     * 获取用户的所有看板
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult listBoards(JSONObject input) throws TrelloException {
        String memberId = input.getString("memberId");
        if (StringUtils.isBlank(memberId)) {
            memberId = "me"; // 默认获取当前用户的看板
        }
        
        List<TrelloBoard> boards = trelloClient.getBoards(memberId);
        
        JSONArray boardsArray = new JSONArray();
        for (TrelloBoard board : boards) {
            JSONObject boardSummary = new JSONObject();
            boardSummary.put("id", board.getId());
            boardSummary.put("name", board.getName());
            boardSummary.put("desc", board.getShortDescription());
            boardSummary.put("closed", board.isClosed());
            boardSummary.put("starred", board.isStarred());
            boardSummary.put("url", board.getShortUrl());
            boardsArray.add(boardSummary);
        }
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", String.format("获取到 %d 个看板", boards.size()));
        result.put("boards", boardsArray);
        result.put("count", boards.size());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 获取看板详细信息
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult getBoard(JSONObject input) throws TrelloException {
        String boardId = input.getString("boardId");
        
        if (StringUtils.isBlank(boardId)) {
            return new ToolExecuteResult("看板ID不能为空", true);
        }
        
        TrelloBoard board = trelloClient.getBoard(boardId);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("board", board.toJson());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 获取看板中的列表
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult getLists(JSONObject input) throws TrelloException {
        String boardId = input.getString("boardId");
        
        if (StringUtils.isBlank(boardId)) {
            return new ToolExecuteResult("看板ID不能为空", true);
        }
        
        List<TrelloList> lists = trelloClient.getLists(boardId);
        
        JSONArray listsArray = new JSONArray();
        for (TrelloList list : lists) {
            JSONObject listSummary = new JSONObject();
            listSummary.put("id", list.getId());
            listSummary.put("name", list.getName());
            listSummary.put("closed", list.isClosed());
            listSummary.put("pos", list.getPos());
            listSummary.put("subscribed", list.isSubscribed());
            if (list.hasCardLimit()) {
                listSummary.put("cardLimit", list.getCardLimit());
            }
            listsArray.add(listSummary);
        }
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", String.format("获取到 %d 个列表", lists.size()));
        result.put("lists", listsArray);
        result.put("count", lists.size());
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
    
    /**
     * 获取看板概览（包含列表和卡片统计）
     * 
     * @param input 输入参数
     * @return 执行结果
     */
    private ToolExecuteResult getBoardOverview(JSONObject input) throws TrelloException {
        String boardId = input.getString("boardId");
        
        if (StringUtils.isBlank(boardId)) {
            return new ToolExecuteResult("看板ID不能为空", true);
        }
        
        // 获取看板信息
        TrelloBoard board = trelloClient.getBoard(boardId);
        
        // 获取列表信息
        List<TrelloList> lists = trelloClient.getLists(boardId);
        
        JSONArray listsOverview = new JSONArray();
        int totalCards = 0;
        int openCards = 0;
        int closedCards = 0;
        
        for (TrelloList list : lists) {
            if (!list.isClosed()) { // 只统计非关闭的列表
                JSONObject listInfo = new JSONObject();
                listInfo.put("id", list.getId());
                listInfo.put("name", list.getName());
                listInfo.put("pos", list.getPos());
                
                // 获取列表中的卡片
                List<TrelloCard> cards = trelloClient.getCards(list.getId());
                int listCardCount = cards.size();
                int listOpenCards = 0;
                int listClosedCards = 0;
                
                for (TrelloCard card : cards) {
                    if (card.isClosed()) {
                        listClosedCards++;
                        closedCards++;
                    } else {
                        listOpenCards++;
                        openCards++;
                    }
                }
                
                totalCards += listCardCount;
                listInfo.put("cardCount", listCardCount);
                listInfo.put("openCards", listOpenCards);
                listInfo.put("closedCards", listClosedCards);
                
                if (list.hasCardLimit()) {
                    listInfo.put("cardLimit", list.getCardLimit());
                    listInfo.put("limitUtilization", 
                        String.format("%.1f%%", (double) listCardCount / list.getCardLimit() * 100));
                }
                
                listsOverview.add(listInfo);
            }
        }
        
        // 构建概览结果
        JSONObject overview = new JSONObject();
        overview.put("board", new JSONObject() {{
            put("id", board.getId());
            put("name", board.getName());
            put("desc", board.getDesc());
            put("closed", board.isClosed());
            put("starred", board.isStarred());
            put("url", board.getShortUrl());
        }});
        
        // 创建统计数据对象
        JSONObject statistics = new JSONObject();
        statistics.put("totalLists", lists.size());
        statistics.put("activeLists", (int) lists.stream().filter(l -> !l.isClosed()).count());
        statistics.put("totalCards", totalCards);
        statistics.put("openCards", openCards);
        statistics.put("closedCards", closedCards);
        if (totalCards > 0) {
            statistics.put("completionRate", String.format("%.1f%%", (double) closedCards / totalCards * 100));
        }
        
        overview.put("statistics", statistics);
        
        overview.put("lists", listsOverview);
        
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("message", "看板概览获取成功");
        result.put("overview", overview);
        
        return new ToolExecuteResult(result.toJSONString(), false);
    }
}
