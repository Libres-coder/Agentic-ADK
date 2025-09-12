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
package com.alibaba.agentic.computer.use.tool;


import com.alibaba.agentic.computer.use.configuration.AdkBrowserUseProperties;
import com.alibaba.agentic.computer.use.service.BrowserUseServiceCaller;
import com.alibaba.agentic.computer.use.service.EcdCommandService;
import com.alibaba.agentic.computer.use.service.dto.DesktopCommandResponse;
import com.alibaba.agentic.computer.use.utils.CodeToBatConverter;
import com.alibaba.agentic.computer.use.utils.StringBasedLetterSnowflake;
import com.alibaba.agentic.core.utils.ApplicationContextUtil;
import com.aliyun.ecd20200930.models.DescribeInvocationsRequest;
import com.aliyun.ecd20200930.models.RunCommandRequest;
import com.google.adk.tools.Annotations;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class BrowserUseTool {

    private static final String pythonExecuteCommand = "& \"C:\\Users\\%s\\AppData\\Local\\Programs\\Python\\Python313\\python.exe\" \"D:\\scripts\\%s\"";

    //脚本下发
    public static String createPythonFile(String pythonScript, String fileName) {
        EcdCommandService  ecdCommandService = (EcdCommandService) ApplicationContextUtil.getBean(EcdCommandService.class);
        AdkBrowserUseProperties adkBrowserUseProperties = (AdkBrowserUseProperties) ApplicationContextUtil.getBean(AdkBrowserUseProperties.class);

        RunCommandRequest runCommandRequest = new RunCommandRequest();
        runCommandRequest.setDesktopId(Collections.singletonList(adkBrowserUseProperties.getComputerResourceId()));
        runCommandRequest.setContentEncoding("Base64");
        runCommandRequest.setType("RunBatScript");

        String command = CodeToBatConverter.convertCodeToBat(pythonScript, adkBrowserUseProperties.getPath(), fileName);
        String encodedContent = Base64.getEncoder().encodeToString(command.getBytes());
        log.warn("createPythonFile final command: {}, encodedContent: {}", command, encodedContent);
        runCommandRequest.setCommandContent(encodedContent);

        return ecdCommandService.runCommand(runCommandRequest);
    }

    public static String runScript(String fileName) {
        EcdCommandService  ecdCommandService = (EcdCommandService) ApplicationContextUtil.getBean(EcdCommandService.class);
        AdkBrowserUseProperties adkBrowserUseProperties = (AdkBrowserUseProperties) ApplicationContextUtil.getBean(AdkBrowserUseProperties.class);

        RunCommandRequest runCommandRequest = new RunCommandRequest();
        runCommandRequest.setDesktopId(Collections.singletonList(adkBrowserUseProperties.getComputerResourceId()));
        runCommandRequest.setContentEncoding("Base64");
        runCommandRequest.setType("RunPowerShellScript");
        //设置终端用户
        runCommandRequest.setEndUserId(adkBrowserUseProperties.getUserId());
        String command = String.format(pythonExecuteCommand, adkBrowserUseProperties.getUserId(), fileName);

        String encodedContent = Base64.getEncoder().encodeToString(command.getBytes());
        runCommandRequest.setCommandContent(encodedContent);

        log.warn("runScript final command: {}, encodedContent: {}", command, encodedContent);

        return ecdCommandService.runCommand(runCommandRequest);
    }

    public static Map<String, String> runCommand(
            @Annotations.Schema(description = "the python script used to run") String pythonScript) {

        StringBasedLetterSnowflake snowflake = (StringBasedLetterSnowflake) ApplicationContextUtil.getBean(StringBasedLetterSnowflake.class);
        //生成一个随机的文件名
        String fileName = snowflake.nextId() + ".py";

        String invokeId = createPythonFile(pythonScript, fileName);

        DesktopCommandResponse response = getCommandResult(invokeId);
        if(Objects.isNull(response)) {
            return Map.of("result", "任务下发失败，任务id: " + invokeId);
        }
        while (StringUtils.equalsIgnoreCase(response.getInvocationStatus(), "Pending")
                || StringUtils.equalsIgnoreCase(response.getInvocationStatus(), "Running")) {
            response = getCommandResult(invokeId);
            if(Objects.isNull(response)) {
                return Map.of("result", "任务下发失败，任务id: " + invokeId);
            }
        }


        if(!StringUtils.equalsIgnoreCase(response.getInvocationStatus(), "Success")) {
            return Map.of("result", "任务下发失败，output: " + response.getOutput());
        }
        return Map.of("result", runScript(fileName));
    }

    //脚本下发的执行结果查询
    public static DesktopCommandResponse getCommandResult(String invokeId) {
        EcdCommandService  ecdCommandService = (EcdCommandService) ApplicationContextUtil.getBean(EcdCommandService.class);

        DescribeInvocationsRequest request = new DescribeInvocationsRequest();
        request.setInvokeId(invokeId);

        List<DesktopCommandResponse> response = ecdCommandService.getCommandResult(request);
        if(CollectionUtils.isNotEmpty(response)) {
            return response.get(0);
        }
        return null;
    }


    //创建一个浏览器任务
    public static Map<String, String> startBrowserMission() {
        return Map.of(
                "result", createPythonFile(PythonScriptUtils.startBrowserMissionScript, "start_browser.py")
        );
    }

    //关闭一个浏览器任务
    public static Map<String, String> closeBrowserMission() {
        return Map.of(
                "result", createPythonFile(PythonScriptUtils.closeBrowserMissionScript, "close_browser.py")
        );
    }


    public static void innerOpenBrowser(String url) {
        EcdCommandService  ecdCommandService = (EcdCommandService) ApplicationContextUtil.getBean(EcdCommandService.class);
        AdkBrowserUseProperties adkBrowserUseProperties = (AdkBrowserUseProperties) ApplicationContextUtil.getBean(AdkBrowserUseProperties.class);
        String command = String.format(pythonExecuteCommand, adkBrowserUseProperties.getUserId(), "red_book_open.py");

        RunCommandRequest runCommandRequest = new RunCommandRequest();
        runCommandRequest.setDesktopId(Collections.singletonList(adkBrowserUseProperties.getComputerResourceId()));
        runCommandRequest.setContentEncoding("Base64");
        runCommandRequest.setType("RunPowerShellScript");
        //设置终端用户
        runCommandRequest.setEndUserId(adkBrowserUseProperties.getUserId());

        String encodedContent = Base64.getEncoder().encodeToString(command.getBytes());
        runCommandRequest.setCommandContent(encodedContent);

        log.warn("innerOpenBrowser encodedContent: {}", encodedContent);
        ecdCommandService.runCommand(runCommandRequest);
    }


    public static Map<String, String> openBrowser(@Annotations.Schema(description = "the url browser need to open") String url) {
        BrowserUseServiceCaller browserUseServiceCaller = (BrowserUseServiceCaller) ApplicationContextUtil.getBean(BrowserUseServiceCaller.class);
        browserUseServiceCaller.call("browserOperate", () -> innerOpenBrowser(url));
        return Map.of("result", "browserOperate");
    }

    public static void innerLoginFinish() {
        EcdCommandService  ecdCommandService = (EcdCommandService) ApplicationContextUtil.getBean(EcdCommandService.class);
        AdkBrowserUseProperties adkBrowserUseProperties = (AdkBrowserUseProperties) ApplicationContextUtil.getBean(AdkBrowserUseProperties.class);

        RunCommandRequest runCommandRequest = new RunCommandRequest();
        runCommandRequest.setDesktopId(Collections.singletonList(adkBrowserUseProperties.getComputerResourceId()));
        runCommandRequest.setContentEncoding("Base64");
        runCommandRequest.setType("RunBatScript");

        String command = CodeToBatConverter.convertCodeToBat("True", "D:\\scripts\\operate", "login.txt", false);
        String encodedContent = Base64.getEncoder().encodeToString(command.getBytes());
        log.warn("loginFinish final command: {}, encodedContent: {}", command, encodedContent);
        runCommandRequest.setCommandContent(encodedContent);

        ecdCommandService.runCommand(runCommandRequest);
    }

    public static Map<String, String> loginFinish() {
        BrowserUseServiceCaller browserUseServiceCaller = (BrowserUseServiceCaller) ApplicationContextUtil.getBean(BrowserUseServiceCaller.class);
        browserUseServiceCaller.call("browserOperate", BrowserUseTool::innerLoginFinish);
        return Map.of("result", "已完成登录操作");
    }

    //给operate agent来生成新的操作代码的html页面获取方法
    public static Map<String, Object> getHtml(@Annotations.Schema(description = "requestId used to get html info")String requestId) {
        BrowserUseServiceCaller browserUseServiceCaller = (BrowserUseServiceCaller) ApplicationContextUtil.getBean(BrowserUseServiceCaller.class);
        String result = browserUseServiceCaller.getByRequestId(requestId);
        if(StringUtils.isEmpty(result)) {
            return Map.of("result", "html info is not exist");
        }
        return Map.of("html", result);
    }

    public static String innerOperateBrowser(String browserCommand) {
        EcdCommandService  ecdCommandService = (EcdCommandService) ApplicationContextUtil.getBean(EcdCommandService.class);
        AdkBrowserUseProperties adkBrowserUseProperties = (AdkBrowserUseProperties) ApplicationContextUtil.getBean(AdkBrowserUseProperties.class);

        RunCommandRequest runCommandRequest = new RunCommandRequest();
        runCommandRequest.setDesktopId(Collections.singletonList(adkBrowserUseProperties.getComputerResourceId()));
        runCommandRequest.setContentEncoding("Base64");
        runCommandRequest.setType("RunBatScript");

        String command = CodeToBatConverter.convertCodeToBat(browserCommand, "D:\\scripts\\commands", "browserCommand.txt", false);
        String encodedContent = Base64.getEncoder().encodeToString(command.getBytes());
        log.warn("operateBrowser final command: {}, encodedContent: {}", command, encodedContent);
        runCommandRequest.setCommandContent(encodedContent);

        return ecdCommandService.runCommand(runCommandRequest);
    }


    public static Map<String, Object> operateBrowser(@Annotations.Schema(description = "command used to operate operate") String browserCommand) {
        BrowserUseServiceCaller browserUseServiceCaller = (BrowserUseServiceCaller) ApplicationContextUtil.getBean(BrowserUseServiceCaller.class);
        browserUseServiceCaller.call("browserOperate", () -> innerOperateBrowser(browserCommand));
        return Map.of("requestId", "browserOperate");
    }

}
