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
package com.alibaba.langengine.linkedin.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.linkedin.sdk.LinkedInClient;
import com.alibaba.langengine.linkedin.sdk.LinkedInException;
import com.alibaba.langengine.linkedin.sdk.UserResponse;
import com.alibaba.langengine.linkedin.sdk.CompanyResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class LinkedInUserTool extends BaseTool {

    private LinkedInClient linkedInClient;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"action\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"操作类型：profile（获取当前用户信息）或 company（获取公司信息）\",\n" +
            "\t\t\t\"enum\": [\"profile\", \"company\"],\n" +
            "\t\t\t\"default\": \"profile\"\n" +
            "\t\t},\n" +
            "\t\t\"companyId\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"当action为company时，需要提供公司ID\"\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"action\"]\n" +
            "}";

    public LinkedInUserTool() {
        setName("linkedin_user");
        setHumanName("LinkedIn用户工具");
        setDescription("获取LinkedIn当前用户信息或公司详细信息");
        setParameters(PARAMETERS);
        this.linkedInClient = new LinkedInClient();
    }

    public LinkedInUserTool(LinkedInClient linkedInClient) {
        setName("linkedin_user");
        setHumanName("LinkedIn用户工具");
        setDescription("获取LinkedIn当前用户信息或公司详细信息");
        setParameters(PARAMETERS);
        this.linkedInClient = linkedInClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("LinkedInUserTool toolInput:" + toolInput);

        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String action = (String) toolInputMap.get("action");
            String companyId = (String) toolInputMap.get("companyId");

            if (action == null || action.trim().isEmpty()) {
                return new ToolExecuteResult("错误：操作类型不能为空");
            }

            if ("profile".equals(action)) {
                return getCurrentUserProfile();
            } else if ("company".equals(action)) {
                if (companyId == null || companyId.trim().isEmpty()) {
                    return new ToolExecuteResult("错误：获取公司信息时需要提供公司ID");
                }
                return getCompanyInfo(companyId);
            } else {
                return new ToolExecuteResult("错误：不支持的操作类型，请使用 'profile' 或 'company'");
            }

        } catch (LinkedInException e) {
            log.error("LinkedIn API调用失败", e);
            return new ToolExecuteResult("LinkedIn API调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("LinkedIn用户工具执行失败", e);
            return new ToolExecuteResult("操作失败: " + e.getMessage());
        }
    }

    private ToolExecuteResult getCurrentUserProfile() throws LinkedInException {
        UserResponse response = linkedInClient.getCurrentUser();
        
        StringBuilder result = new StringBuilder();
        result.append("当前用户信息:\n");
        result.append("用户ID: ").append(response.getId()).append("\n");
        
        if (response.getFirstName() != null && response.getFirstName().getLocalized() != null) {
            result.append("名字: ").append(response.getFirstName().getLocalized().getEnUs()).append("\n");
        }
        
        if (response.getLastName() != null && response.getLastName().getLocalized() != null) {
            result.append("姓氏: ").append(response.getLastName().getLocalized().getEnUs()).append("\n");
        }
        
        result.append("职位: ").append(response.getHeadline() != null ? response.getHeadline() : "无").append("\n");
        result.append("简介: ").append(response.getSummary() != null ? response.getSummary() : "无").append("\n");
        
        if (response.getLocation() != null) {
            result.append("位置: ").append(response.getLocation().getName()).append("\n");
        }
        
        result.append("行业: ").append(response.getIndustry() != null ? response.getIndustry() : "无").append("\n");

        return new ToolExecuteResult(result.toString());
    }

    private ToolExecuteResult getCompanyInfo(String companyId) throws LinkedInException {
        CompanyResponse response = linkedInClient.getCompany(companyId);
        
        StringBuilder result = new StringBuilder();
        result.append("公司信息:\n");
        result.append("公司ID: ").append(response.getId()).append("\n");
        result.append("公司名称: ").append(response.getName()).append("\n");
        result.append("描述: ").append(response.getDescription() != null ? response.getDescription() : "无").append("\n");
        result.append("网站: ").append(response.getWebsiteUrl() != null ? response.getWebsiteUrl() : "无").append("\n");
        result.append("行业: ").append(response.getIndustry() != null ? response.getIndustry() : "无").append("\n");
        result.append("公司规模: ").append(response.getCompanySize() != null ? response.getCompanySize() : "无").append("\n");
        result.append("Logo: ").append(response.getLogoUrl() != null ? response.getLogoUrl() : "无").append("\n");

        return new ToolExecuteResult(result.toString());
    }

    public LinkedInClient getLinkedInClient() {
        return linkedInClient;
    }

    public void setLinkedInClient(LinkedInClient linkedInClient) {
        this.linkedInClient = linkedInClient;
    }
}