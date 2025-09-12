/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.computer.use.service;

import com.alibaba.agentic.computer.use.service.dto.DesktopCommandResponse;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.appstream_center20210218.models.GetAuthCodeRequest;
import com.aliyun.appstream_center20210218.models.GetAuthCodeResponse;
import com.aliyun.ecd20200930.Client;
import com.aliyun.ecd20200930.models.*;
import com.aliyun.ecd20201002.models.GetLoginTokenRequest;
import com.aliyun.ecd20201002.models.GetLoginTokenResponse;
import com.aliyun.eds_aic20230930.models.DescribeAndroidInstancesRequest;
import com.aliyun.eds_aic20230930.models.DescribeAndroidInstancesResponse;
import com.aliyun.eds_aic20230930.models.DescribeAndroidInstancesResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Slf4j
@Component
@ConditionalOnProperty(name = "ali.adk.browser.use.properties.enable", havingValue = "true")
public class EcdCommandServiceImpl implements EcdCommandService{

    @Autowired
    private Client client;
    @Autowired
    private com.aliyun.ecd20201002.Client client1002;
    @Autowired
    private Client clientJp;
    @Autowired
    private com.aliyun.appstream_center20210218.Client appStreamClient;
    @Autowired
    private com.aliyun.eds_aic20230930.Client mobileClient;

//    @Autowired
//    private Map<String, com.aliyun.ecd20200930.Client> clientMap;

    @Override
    public String uploadScript(CreateCdsFileRequest createCdsFileRequest) {
        try {
            createCdsFileRequest.setFileType("file");
            log.warn("uploadScript request in, request: {}", JSONObject.toJSONString(createCdsFileRequest));
            CreateCdsFileResponse response = client.createCdsFile(createCdsFileRequest);
            if (response.getBody() == null || response.getBody().getFileModel() == null) {
                log.warn("uploadScript response is null, request: {}, response: {}", JSONObject.toJSONString(createCdsFileRequest), JSONObject.toJSONString(response));
                return null;
            }
            return response.getBody().getFileModel().getFileId();
        } catch (Exception e) {
            log.error("AliyunEcdServiceImpl uploadScript error, request: {}", JSONObject.toJSONString(createCdsFileRequest), e);
            return null;
        }
    }

    @Override
    public String runCommand(RunCommandRequest request) {
        try {
            // request.setRegionId("cn-hangzhou");
            log.warn("runCommand request in, request: {}", JSONObject.toJSONString(request));
            RunCommandResponse response = client.runCommand(request);
            return response.getBody().invokeId;
        } catch (Exception e) {
            log.error("AliyunEcdServiceImpl runCommand error, request: {}", JSONObject.toJSONString(request), e);
            return null;
        }
    }

    @Override
    public String runCommand(RunCommandRequest request, String endpoint) {
        try {
            // request.setRegionId("cn-hangzhou");
            // log.info("runCommand clientMap size: {}, clientMap: {}", clientMap.size(), JSONObject.toJSONString(clientMap));
            // com.aliyun.ecd20200930.Client client = clientMap.get(endpoint);
            log.warn("runCommand request in, request: {}, endpoint: {}", JSONObject.toJSONString(request), endpoint);
            RunCommandResponse response = null;
            if ("ecd.ap-northeast-1.aliyuncs.com".equals(endpoint)) {
                response = clientJp.runCommand(request);
            } else if ("ecd-share.cn-hangzhou.aliyuncs.com".equals(endpoint)) {
                response = client.runCommand(request);
            }
            log.info("runCommand request in, response: {}", JSONObject.toJSONString(response));
            return response.getBody().invokeId;
        } catch (Exception e) {
            log.error("AliyunEcdServiceImpl runCommand error, request: {}", JSONObject.toJSONString(request), e);
            return null;
        }
    }

    @Override
    public List<DesktopCommandResponse> getCommandResult(DescribeInvocationsRequest request) {
        try {
            // request.setRegionId("cn-hangzhou");
            request.setIncludeOutput(Boolean.TRUE);
            DescribeInvocationsResponse response = client.describeInvocations(request);
            log.info("AliyunEcdServiceImpl getCommandResult success, request: {}, original response: {}", JSONObject.toJSONString(request), JSONObject.toJSONString(response));
            return this.convert(response.getBody().invocations);
        } catch (Exception e) {
            log.error("AliyunEcdServiceImpl getCommandResult error, request: {}", JSONObject.toJSONString(request), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<DesktopCommandResponse> getCommandResult(DescribeInvocationsRequest request, String endpoint) {
        try {
            // request.setRegionId("cn-hangzhou");
//            com.aliyun.ecd20200930.Client client = clientMap.get(endpoint);
            request.setIncludeOutput(Boolean.TRUE);
            DescribeInvocationsResponse response = null;
            if ("ecd.ap-northeast-1.aliyuncs.com".equals(endpoint)) {
                response = clientJp.describeInvocations(request);
            } else if ("ecd-share.cn-hangzhou.aliyuncs.com".equals(endpoint)) {
                response = client.describeInvocations(request);
            }
            log.info("AliyunEcdServiceImpl getCommandResult success, request: {}, endpoint: {}, original response: {}", JSONObject.toJSONString(request), endpoint, JSONObject.toJSONString(response));
            return this.convert(response.getBody().invocations);
        } catch (Exception e) {
            log.error("AliyunEcdServiceImpl getCommandResult error, request: {}", JSONObject.toJSONString(request), e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getLoginToken(GetLoginTokenRequest request) {
        try {
            GetLoginTokenResponse response = client1002.getLoginToken(request);
            return response.getBody().getLoginToken();
        } catch (Exception e) {
            log.error("AliyunEcdServiceImpl getLoginToken error, request: {}", JSONObject.toJSONString(request), e);
            return "";
        }
    }

    @Override
    public String getAuthCode(String userId) {
        GetAuthCodeRequest request = new GetAuthCodeRequest();
        request.setEndUserId(userId);
        try {
            GetAuthCodeResponse response = appStreamClient.getAuthCode(request);
            return response.getBody().getAuthModel().getAuthCode();
        } catch (Exception e) {
            log.error("AliyunEcdServiceImpl getAuthCode error, userId: {}", userId, e);
        }
        return "";
    }

    @Override
    public List<DescribeAndroidInstancesResponseBody.DescribeAndroidInstancesResponseBodyInstanceModel> getAndroidInstance(DescribeAndroidInstancesRequest request) {
        try {
            DescribeAndroidInstancesResponse response = mobileClient.describeAndroidInstances(request);
            return response.getBody().instanceModel;
        } catch (Exception e) {
            log.error("getAndroidInstance getAuthCode error, request: {}", JSONObject.toJSONString(request), e);
            throw new RuntimeException("getAndroidInstance getAuthCode error");
        }
    }

    private List<DesktopCommandResponse> convert(List<DescribeInvocationsResponseBody.DescribeInvocationsResponseBodyInvocations> invocations) {
        List<DesktopCommandResponse> responseList = new ArrayList<>();
        for (DescribeInvocationsResponseBody.DescribeInvocationsResponseBodyInvocationsInvokeDesktops invokeDesktop : invocations.get(0).getInvokeDesktops()) {
            DesktopCommandResponse response = new DesktopCommandResponse();
            response.setOutput(invokeDesktop.getOutput());
            response.setComputerId(invokeDesktop.getDesktopId());
            response.setFinishTime(invokeDesktop.getFinishTime());
            response.setInvocationStatus(invocations.get(0).getInvocationStatus());
            response.setDropped(invokeDesktop.getDropped());
            responseList.add(response);
        }
        return responseList;
    }
}
