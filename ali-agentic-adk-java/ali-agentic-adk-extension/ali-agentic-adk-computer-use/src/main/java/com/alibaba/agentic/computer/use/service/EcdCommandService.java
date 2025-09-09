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
import com.aliyun.ecd20200930.models.CreateCdsFileRequest;
import com.aliyun.ecd20200930.models.DescribeInvocationsRequest;
import com.aliyun.ecd20200930.models.RunCommandRequest;
import com.aliyun.ecd20201002.models.GetLoginTokenRequest;
import com.aliyun.eds_aic20230930.models.DescribeAndroidInstancesRequest;
import com.aliyun.eds_aic20230930.models.DescribeAndroidInstancesResponseBody;

import java.util.List;

public interface EcdCommandService {

    String uploadScript(CreateCdsFileRequest createCdsFileRequest);

    /**
     * 下发脚本命令
     */
    String runCommand(RunCommandRequest request);

    String runCommand(RunCommandRequest request, String regionId);

    List<DesktopCommandResponse> getCommandResult(DescribeInvocationsRequest request);

    List<DesktopCommandResponse> getCommandResult(DescribeInvocationsRequest request, String regionId);

    String getLoginToken(GetLoginTokenRequest request);

    String getAuthCode(String userId);

    List<DescribeAndroidInstancesResponseBody.DescribeAndroidInstancesResponseBodyInstanceModel> getAndroidInstance(DescribeAndroidInstancesRequest request);
}
