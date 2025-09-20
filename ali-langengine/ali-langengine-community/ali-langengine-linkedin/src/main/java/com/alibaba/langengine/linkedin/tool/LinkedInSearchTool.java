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
import com.alibaba.langengine.linkedin.sdk.CompanySearchResponse;
import com.alibaba.langengine.linkedin.sdk.PeopleSearchResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class LinkedInSearchTool extends BaseTool {

    private LinkedInClient linkedInClient;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"keyword\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"搜索关键词\"\n" +
            "\t\t},\n" +
            "\t\t\"type\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"搜索类型：company（公司）或 people（人员）\",\n" +
            "\t\t\t\"enum\": [\"company\", \"people\"],\n" +
            "\t\t\t\"default\": \"company\"\n" +
            "\t\t},\n" +
            "\t\t\"maxResults\": {\n" +
            "\t\t\t\"type\": \"integer\",\n" +
            "\t\t\t\"description\": \"最大返回结果数，默认10，最大100\",\n" +
            "\t\t\t\"default\": 10\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"keyword\"]\n" +
            "}";

    public LinkedInSearchTool() {
        setName("linkedin_search");
        setHumanName("LinkedIn搜索工具");
        setDescription("搜索LinkedIn上的公司或人员信息");
        setParameters(PARAMETERS);
        this.linkedInClient = new LinkedInClient();
    }

    public LinkedInSearchTool(LinkedInClient linkedInClient) {
        setName("linkedin_search");
        setHumanName("LinkedIn搜索工具");
        setDescription("搜索LinkedIn上的公司或人员信息");
        setParameters(PARAMETERS);
        this.linkedInClient = linkedInClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("LinkedInSearchTool toolInput:" + toolInput);

        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String keyword = (String) toolInputMap.get("keyword");
            String type = (String) toolInputMap.getOrDefault("type", "company");
            Integer maxResults = (Integer) toolInputMap.getOrDefault("maxResults", 10);

            if (keyword == null || keyword.trim().isEmpty()) {
                return new ToolExecuteResult("错误：搜索关键词不能为空");
            }

            if ("company".equals(type)) {
                return searchCompanies(keyword, maxResults);
            } else if ("people".equals(type)) {
                return searchPeople(keyword, maxResults);
            } else {
                return new ToolExecuteResult("错误：不支持搜索类型，请使用 'company' 或 'people'");
            }

        } catch (LinkedInException e) {
            log.error("LinkedIn API调用失败", e);
            return new ToolExecuteResult("LinkedIn API调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("LinkedIn搜索工具执行失败", e);
            return new ToolExecuteResult("搜索失败: " + e.getMessage());
        }
    }

    private ToolExecuteResult searchCompanies(String keyword, int maxResults) throws LinkedInException {
        CompanySearchResponse response = linkedInClient.searchCompanies(keyword, maxResults);
        
        if (response.getElements() == null || response.getElements().isEmpty()) {
            return new ToolExecuteResult("未找到相关公司");
        }

        StringBuilder result = new StringBuilder();
        result.append("找到 ").append(response.getElements().size()).append(" 家公司：\n\n");
        
        for (CompanySearchResponse.Company company : response.getElements()) {
            result.append("公司ID: ").append(company.getId()).append("\n");
            result.append("公司名称: ").append(company.getName()).append("\n");
            result.append("描述: ").append(company.getDescription() != null ? company.getDescription() : "无").append("\n");
            result.append("网站: ").append(company.getWebsiteUrl() != null ? company.getWebsiteUrl() : "无").append("\n");
            result.append("行业: ").append(company.getIndustry() != null ? company.getIndustry() : "无").append("\n");
            result.append("公司规模: ").append(company.getCompanySize() != null ? company.getCompanySize() : "无").append("\n");
            result.append("---\n");
        }

        return new ToolExecuteResult(result.toString());
    }

    private ToolExecuteResult searchPeople(String keyword, int maxResults) throws LinkedInException {
        PeopleSearchResponse response = linkedInClient.searchPeople(keyword, maxResults);
        
        if (response.getElements() == null || response.getElements().isEmpty()) {
            return new ToolExecuteResult("未找到相关人员");
        }

        StringBuilder result = new StringBuilder();
        result.append("找到 ").append(response.getElements().size()).append(" 位人员：\n\n");
        
        for (PeopleSearchResponse.Person person : response.getElements()) {
            result.append("人员ID: ").append(person.getId()).append("\n");
            result.append("姓名: ").append(person.getFirstName()).append(" ").append(person.getLastName()).append("\n");
            result.append("职位: ").append(person.getHeadline() != null ? person.getHeadline() : "无").append("\n");
            result.append("位置: ").append(person.getLocation() != null ? person.getLocation() : "无").append("\n");
            result.append("行业: ").append(person.getIndustry() != null ? person.getIndustry() : "无").append("\n");
            result.append("---\n");
        }

        return new ToolExecuteResult(result.toString());
    }

    public LinkedInClient getLinkedInClient() {
        return linkedInClient;
    }

    public void setLinkedInClient(LinkedInClient linkedInClient) {
        this.linkedInClient = linkedInClient;
    }
}