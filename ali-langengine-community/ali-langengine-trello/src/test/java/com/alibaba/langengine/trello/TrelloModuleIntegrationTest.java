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
package com.alibaba.langengine.trello;

import com.alibaba.langengine.trello.client.TrelloClient;
import com.alibaba.langengine.trello.model.*;
import com.alibaba.langengine.trello.tools.*;
import com.alibaba.fastjson.JSONObject;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class TrelloModuleIntegrationTest {
    
    private TrelloConfiguration config;
    private TrelloClient client;
    private TrelloToolFactory factory;
    
    @Before
    public void setUp() {
        // 使用测试配置
        config = new TrelloConfiguration("test-api-key", "test-token");
        client = new TrelloClient(config);
        factory = new TrelloToolFactory(config);
    }
    
    @Test
    public void testTrelloConfiguration() {
        // 测试配置创建
        assertNotNull("配置对象不能为空", config);
        assertEquals("API Key应该正确", "test-api-key", config.getApiKey());
        assertEquals("Token应该正确", "test-token", config.getToken());
        
        // 测试配置验证
        assertTrue("配置应该有效", config.isValid());
        
        // 测试空配置
        TrelloConfiguration emptyConfig = new TrelloConfiguration("", "");
        assertFalse("空配置应该无效", emptyConfig.isValid());
    }
    
    @Test
    public void testTrelloClient() {
        // 测试客户端创建
        assertNotNull("客户端不能为空", client);
        assertNotNull("客户端配置不能为空", client.getConfiguration());
        
        // 测试客户端基本功能
        assertEquals("客户端配置应该匹配", config, client.getConfiguration());
    }
    
    @Test
    public void testTrelloModels() {
        // 测试TrelloCard模型
        TrelloCard card = TrelloCard.builder()
            .id("test-card-id")
            .name("测试卡片")
            .desc("测试描述")
            .idList("test-list-id")
            .build();
            
        assertNotNull("卡片不能为空", card);
        assertEquals("卡片ID应该正确", "test-card-id", card.getId());
        assertEquals("卡片名称应该正确", "测试卡片", card.getName());
        assertEquals("卡片描述应该正确", "测试描述", card.getDesc());
        
        // 测试JSON序列化
        JSONObject cardJson = card.toJson();
        assertNotNull("卡片JSON不能为空", cardJson);
        assertEquals("JSON中的名称应该正确", "测试卡片", cardJson.getString("name"));
        
        // 测试TrelloBoard模型
        TrelloBoard board = TrelloBoard.builder()
            .id("test-board-id")
            .name("测试看板")
            .desc("测试看板描述")
            .build();
            
        assertNotNull("看板不能为空", board);
        assertEquals("看板ID应该正确", "test-board-id", board.getId());
        assertEquals("看板名称应该正确", "测试看板", board.getName());
        
        // 测试TrelloLabel模型
        TrelloLabel label = TrelloLabel.builder()
            .id("test-label-id")
            .name("测试标签")
            .color("red")
            .build();
            
        assertNotNull("标签不能为空", label);
        assertEquals("标签ID应该正确", "test-label-id", label.getId());
        assertEquals("标签名称应该正确", "测试标签", label.getName());
        assertEquals("标签颜色应该正确", "red", label.getColor());
    }
    
    @Test
    public void testTrelloToolFactory() {
        // 测试工具工厂创建
        assertNotNull("工具工厂不能为空", factory);
        
        // 测试创建卡片管理工具
        TrelloCardManageTool cardTool = factory.createCardManageTool();
        assertNotNull("卡片管理工具不能为空", cardTool);
        assertEquals("工具名称应该正确", "trello_card_manage", cardTool.getName());
        assertNotNull("工具描述不能为空", cardTool.getDescription());
        
        // 测试创建看板操作工具
        TrelloBoardOperationTool boardTool = factory.createBoardOperationTool();
        assertNotNull("看板操作工具不能为空", boardTool);
        assertEquals("工具名称应该正确", "trello_board_operation", boardTool.getName());
        assertNotNull("工具描述不能为空", boardTool.getDescription());
        
        // 测试创建团队协作工具
        TrelloTeamCollaborationTool teamTool = factory.createTeamCollaborationTool();
        assertNotNull("团队协作工具不能为空", teamTool);
        assertEquals("工具名称应该正确", "trello_team_collaboration", teamTool.getName());
        assertNotNull("工具描述不能为空", teamTool.getDescription());
    }
    
    @Test
    public void testToolParameters() {
        // 测试卡片管理工具参数
        TrelloCardManageTool cardTool = factory.createCardManageTool();
        
        // 测试创建卡片参数
        JSONObject createCardParams = new JSONObject();
        createCardParams.put("name", "新建测试卡片");
        createCardParams.put("desc", "测试卡片描述");
        createCardParams.put("listId", "test-list-id");
        
        // 验证参数不为空
        assertNotNull("创建卡片参数不能为空", createCardParams);
        assertEquals("卡片名称参数应该正确", "新建测试卡片", createCardParams.getString("name"));
        
        // 测试看板操作工具参数
        TrelloBoardOperationTool boardTool = factory.createBoardOperationTool();
        
        JSONObject createBoardParams = new JSONObject();
        createBoardParams.put("name", "新建测试看板");
        createBoardParams.put("desc", "测试看板描述");
        
        assertNotNull("创建看板参数不能为空", createBoardParams);
        assertEquals("看板名称参数应该正确", "新建测试看板", createBoardParams.getString("name"));
    }
    
    @Test
    public void testModelValidation() {
        // 测试模型字段验证
        TrelloCard validCard = TrelloCard.builder()
            .id("valid-id")
            .name("Valid Card")
            .build();
        
        assertNotNull("有效卡片不能为空", validCard);
        assertNotNull("卡片ID不能为空", validCard.getId());
        assertNotNull("卡片名称不能为空", validCard.getName());
        
        // 测试equals和hashCode
        TrelloCard sameCard = TrelloCard.builder()
            .id("valid-id")
            .name("Valid Card")
            .build();
        
        assertEquals("相同的卡片应该相等", validCard, sameCard);
        assertEquals("相同的卡片hashCode应该相等", validCard.hashCode(), sameCard.hashCode());
        
        // 测试toString
        String cardString = validCard.toString();
        assertNotNull("toString不能为空", cardString);
        assertTrue("toString应该包含类名", cardString.contains("TrelloCard"));
    }
    
    @Test
    public void testJSONConversion() {
        // 测试JSON转换功能
        TrelloCard card = TrelloCard.builder()
            .id("json-test-id")
            .name("JSON测试卡片")
            .desc("JSON测试描述")
            .build();
        
        // 转换为JSON
        JSONObject json = card.toJson();
        assertNotNull("JSON对象不能为空", json);
        assertEquals("JSON中的ID应该正确", "json-test-id", json.getString("id"));
        assertEquals("JSON中的名称应该正确", "JSON测试卡片", json.getString("name"));
        assertEquals("JSON中的描述应该正确", "JSON测试描述", json.getString("desc"));
        
        // 测试JSON字符串
        String jsonString = json.toJSONString();
        assertNotNull("JSON字符串不能为空", jsonString);
        assertTrue("JSON字符串应该包含ID", jsonString.contains("json-test-id"));
        assertTrue("JSON字符串应该包含名称", jsonString.contains("JSON测试卡片"));
    }
    
    @Test
    public void testClientConfiguration() {
        // 测试客户端配置
        assertEquals("客户端应该使用正确的配置", config, client.getConfiguration());
        
        // 测试不同的配置
        TrelloConfiguration newConfig = new TrelloConfiguration("new-key", "new-token");
        TrelloClient newClient = new TrelloClient(newConfig);
        
        assertNotEquals("不同的客户端应该有不同的配置", client.getConfiguration(), newClient.getConfiguration());
        assertEquals("新客户端应该使用新配置", newConfig, newClient.getConfiguration());
    }
    
    @Test
    public void testToolIntegration() {
        // 测试工具集成
        TrelloCardManageTool cardTool = factory.createCardManageTool();
        TrelloBoardOperationTool boardTool = factory.createBoardOperationTool();
        TrelloTeamCollaborationTool teamTool = factory.createTeamCollaborationTool();
        
        // 验证所有工具都有客户端
        assertNotNull("卡片管理工具应该有客户端", cardTool.getTrelloClient());
        assertNotNull("看板操作工具应该有客户端", boardTool.getTrelloClient());
        assertNotNull("团队协作工具应该有客户端", teamTool.getTrelloClient());
        
        // 验证工具名称不重复
        assertNotEquals("工具名称应该不同", cardTool.getName(), boardTool.getName());
        assertNotEquals("工具名称应该不同", boardTool.getName(), teamTool.getName());
        assertNotEquals("工具名称应该不同", cardTool.getName(), teamTool.getName());
    }
    
    @Test
    public void testModuleCompleteness() {
        // 测试模块完整性
        assertNotNull("配置类应该存在", config);
        assertNotNull("客户端类应该存在", client);
        assertNotNull("工厂类应该存在", factory);
        
        // 测试核心功能是否都实现
        assertNotNull("卡片管理工具应该实现", factory.createCardManageTool());
        assertNotNull("看板操作工具应该实现", factory.createBoardOperationTool());
        assertNotNull("团队协作工具应该实现", factory.createTeamCollaborationTool());
        
        // 测试基本的模型类
        TrelloCard card = new TrelloCard();
        TrelloBoard board = new TrelloBoard();
        TrelloLabel label = new TrelloLabel();
        TrelloList list = new TrelloList();
        TrelloMember member = new TrelloMember();
        
        assertNotNull("卡片模型应该可创建", card);
        assertNotNull("看板模型应该可创建", board);
        assertNotNull("标签模型应该可创建", label);
        assertNotNull("列表模型应该可创建", list);
        assertNotNull("成员模型应该可创建", member);
    }
}
