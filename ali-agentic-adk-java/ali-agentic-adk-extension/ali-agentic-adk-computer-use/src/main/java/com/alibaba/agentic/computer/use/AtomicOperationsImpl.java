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
package com.alibaba.agentic.computer.use;

import com.alibaba.agentic.computer.use.configuration.AdkBrowserUseProperties;
import com.alibaba.agentic.computer.use.domain.BrowserUseRequest;
import com.alibaba.agentic.computer.use.domain.BrowserUseResponse;
import com.alibaba.agentic.computer.use.enums.ScriptExecuteStatusEnum;
import com.alibaba.agentic.computer.use.service.EcdCommandService;
import com.alibaba.agentic.computer.use.service.dto.DesktopCommandResponse;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.ecd20200930.models.DescribeInvocationsRequest;
import com.aliyun.ecd20200930.models.RunCommandRequest;
import com.aliyun.tea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component("atomicOperations")
public class AtomicOperationsImpl implements AtomicOperations{

    public static final int DEFAULT_TIMEOUT_SECONDS = 60;

    @Autowired
    private EcdCommandService ecdCommandService;

    @Autowired
    private AdkBrowserUseProperties aliAdkProperties;

    @Override
    public String doScriptUpload(String scriptStr, String savePath) {
        return null;
    }

    @Override
    public BrowserUseResponse doScriptExecute(BrowserUseRequest request) {
        log.info("atomicOperations doScriptExecute in, request: {}", JSONObject.toJSONString(request));
        RunCommandRequest runCommandRequest = new RunCommandRequest();
        int timeout = request.getTimeout() == null ? DEFAULT_TIMEOUT_SECONDS : request.getTimeout();
        String regionId = StringUtils.isEmpty(request.getRegionId()) ? aliAdkProperties.getRegionId() : request.getRegionId();
        runCommandRequest.setRegionId(regionId);
        String computerResourceId = StringUtils.isEmpty(request.getComputerResourceId()) ? aliAdkProperties.getComputerResourceId() : request.getComputerResourceId();
        String endpoint = request.getEndpoint();
        runCommandRequest.setDesktopId(Collections.singletonList(computerResourceId));
        runCommandRequest.setContentEncoding("Base64");
        runCommandRequest.setType("RunPowerShellScript");
        //设置终端用户
        runCommandRequest.setEndUserId(aliAdkProperties.getUserId());
        String encodedContent = Base64.getEncoder().encodeToString(request.getCommand().getBytes());
        runCommandRequest.setCommandContent(encodedContent);
        String invokeId = ecdCommandService.runCommand(runCommandRequest, endpoint);

        long startClock = System.currentTimeMillis();
        DescribeInvocationsRequest describeInvocationsRequest = new DescribeInvocationsRequest();
        describeInvocationsRequest.setRegionId(regionId);
        describeInvocationsRequest.setInvokeId(invokeId);
        while (System.currentTimeMillis() - startClock < timeout * 1000L) {
            List<DesktopCommandResponse> response = ecdCommandService.getCommandResult(describeInvocationsRequest, endpoint);
            String currentStatus = response.get(0).getInvocationStatus();
            if (ScriptExecuteStatusEnum.SUCCESS.getMessage().equals(currentStatus)) {
                String originalOutput = response.get(0).getOutput();
                String output = StringUtils.isEmpty(originalOutput) ? "" : new String(Base64.getDecoder().decode(originalOutput));
                Integer dropped = response.get(0).getDropped();
                return new BrowserUseResponse(true, currentStatus, output, dropped);
            }
            if (ScriptExecuteStatusEnum.FAILED.getMessage().equals(currentStatus)) {
                return new BrowserUseResponse(false, currentStatus);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("atomicOperations doScriptExecute sleep error", e);
            }
        }
        return new BrowserUseResponse(false, ScriptExecuteStatusEnum.TIMEOUT.getMessage());
    }
}
