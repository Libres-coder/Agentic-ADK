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
package com.alibaba.agentic.computer.use.controller;

import com.alibaba.agentic.computer.use.configuration.AdkBrowserUseProperties;
import com.alibaba.agentic.computer.use.configuration.BrowserAgentRegister;
import com.alibaba.agentic.computer.use.configuration.BrowserRunnerService;
import com.alibaba.agentic.computer.use.dto.*;
import com.alibaba.agentic.computer.use.service.AliyunRPAProvider;
import com.alibaba.agentic.computer.use.service.EcdCommandService;
import com.aliyun.ecd20201002.models.GetLoginTokenRequest;
import com.aliyun.eds_aic20230930.models.DescribeAndroidInstancesRequest;
import com.aliyun.eds_aic20230930.models.DescribeAndroidInstancesResponseBody;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.BaseSessionService;
import com.google.adk.sessions.ListSessionsResponse;
import com.google.adk.sessions.Session;
import com.google.common.base.Preconditions;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/adk/browser/_dev")
@ConditionalOnProperty(name = "ali.adk.browser.use.properties.enable", havingValue = "true")
public class BrowserUseDevController {

    private final ExecutorService sseExecutor = Executors.newCachedThreadPool();

    @Autowired
    private AliyunRPAProvider aliyunRPAProvider;
    @Autowired
    private AdkBrowserUseProperties adkBrowserUseProperties;
    @Autowired
    @Qualifier("baseSessionService")
    private BaseSessionService sessionService;
    @Autowired
    private BrowserRunnerService browserRunnerService;
    @Autowired
    private EcdCommandService ecdCommandService;

    @GetMapping("/getComputerDetail")
    public ResultDO<ComputerDetailDTO> getComputerDetail() {
        ComputerDetailDTO detail = new ComputerDetailDTO();
        detail.setDesktopId(adkBrowserUseProperties.getComputerResourceId());
        detail.setRegionId(StringUtils.isEmpty(adkBrowserUseProperties.getRegionId()) ? "cn-hangzhou" : adkBrowserUseProperties.getRegionId());
        detail.setLoginToken(this.getLoginToken());

        return ResultDO.success(detail);
    }

    @GetMapping("/getMobileDetail")
    public ResultDO<MobileDetailDTO> getMobileDetail() {
        MobileDetailDTO detail = new MobileDetailDTO();
        detail.setDesktopId(adkBrowserUseProperties.getMobileResourceId());
        this.enrichMobileDetail(detail);
        return ResultDO.success(detail);
    }

    private void enrichMobileDetail(MobileDetailDTO detail) {
        String authCode = ecdCommandService.getAuthCode(adkBrowserUseProperties.getUserId());
        detail.setAuthCode(authCode);

        DescribeAndroidInstancesRequest request = new DescribeAndroidInstancesRequest();
        request.setInstanceGroupId(adkBrowserUseProperties.getInstanceGroupId());
        request.setAndroidInstanceIds(Collections.singletonList(detail.getDesktopId()));

        List<DescribeAndroidInstancesResponseBody.DescribeAndroidInstancesResponseBodyInstanceModel> models = ecdCommandService.getAndroidInstance(request);
        if(CollectionUtils.isEmpty(models)) {
            return;
        }
        DescribeAndroidInstancesResponseBody.DescribeAndroidInstancesResponseBodyInstanceModel model = models.get(0);
        detail.setResourceId(model.persistentAppInstanceId);
    }

    private String getLoginToken() {
        GetLoginTokenRequest request = new GetLoginTokenRequest();
        request.setRegionId(adkBrowserUseProperties.getRegionId());
        request.setClientId(UUID.randomUUID().toString());
        request.setOfficeSiteId(adkBrowserUseProperties.getOfficeSiteId());
        request.setEndUserId(adkBrowserUseProperties.getUserId());
        request.setPassword(adkBrowserUseProperties.getPassword());

        return ecdCommandService.getLoginToken(request);
    }


    @GetMapping("/latestSession")
    public ResultDO<String> latestSession(@RequestParam String appName, @RequestParam String user) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(appName));
        Preconditions.checkArgument(StringUtils.isNotEmpty(user));

        Single<ListSessionsResponse> sessionsResponseSingle =  sessionService.listSessions(appName, user);
        ListSessionsResponse response = sessionsResponseSingle.blockingGet();
        if(Objects.isNull(response)) {
            return ResultDO.success();
        }
        if(CollectionUtils.isEmpty(response.sessions())) {
            return ResultDO.success();
        }
        Session session = response.sessions()
                .stream()
                .sorted(Comparator.comparing(Session::getLastUpdateTimeAsDouble).reversed())
                .findFirst()
                .orElse(null);

        if(Objects.isNull(session)) {
            return ResultDO.success();
        }
        return ResultDO.success(session.id());
    }

    @GetMapping("/list-apps")
    public ResultDO<List<String>> listApps() {
        log.info("Listing apps from dynamic registry. Found: {}", BrowserAgentRegister.getAgents().keySet());
        List<String> appNames = new ArrayList<>(BrowserAgentRegister.getAgents().keySet());
        Collections.sort(appNames);
        return ResultDO.success(appNames);
    }


    @PostMapping(value = "/run_sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter agentRunSse(@RequestBody BrowserAgentRunRequest request) {
        SseEmitter emitter = new SseEmitter();

        if (request.appName == null || request.appName.trim().isEmpty()) {
            log.warn(
                    "appName cannot be null or empty in SseEmitter request for appName: {}, session: {}",
                    request.appName,
                    request.sessionId);
            emitter.completeWithError(
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "appName cannot be null or empty"));
            return emitter;
        }
        if (request.sessionId == null || request.sessionId.trim().isEmpty()) {
            log.warn(
                    "sessionId cannot be null or empty in SseEmitter request for appName: {}, session: {}",
                    request.appName,
                    request.sessionId);
            emitter.completeWithError(
                    new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "sessionId cannot be null or empty"));
            return emitter;
        }

        log.info(
                "SseEmitter Request received for POST /run_sse_emitter for session: {}",
                request.sessionId);

        final String sessionId = request.sessionId;
        sseExecutor.execute(
                () -> {
                    Runner runner;
                    try {
                        runner = browserRunnerService.getRunner(request.appName);
                    } catch (ResponseStatusException e) {
                        log.warn(
                                "Setup failed for SseEmitter request for session {}: {}",
                                sessionId,
                                e.getMessage());
                        try {
                            emitter.completeWithError(e);
                        } catch (Exception ex) {
                            log.warn(
                                    "Error completing emitter after setup failure for session {}: {}",
                                    sessionId,
                                    ex.getMessage());
                        }
                        return;
                    }

                    final RunConfig runConfig =
                            RunConfig.builder()
                                    .setStreamingMode(
                                            request.getStreaming() ? RunConfig.StreamingMode.SSE : RunConfig.StreamingMode.NONE)
                                    .build();

                    Flowable<Event> eventFlowable =
                            runner.runAsync(request.userId, request.sessionId, request.newMessage, runConfig);

                    Disposable disposable =
                            eventFlowable
                                    .observeOn(Schedulers.io())
                                    .subscribe(
                                            event -> {
                                                try {
                                                    log.debug(
                                                            "SseEmitter: Sending event {} for session {}",
                                                            event.id(),
                                                            sessionId);
                                                    emitter.send(SseEmitter.event().data(event.toJson()));
                                                } catch (IOException e) {
                                                    log.error(
                                                            "SseEmitter: IOException sending event for session {}: {}",
                                                            sessionId,
                                                            e.getMessage());
                                                    throw new RuntimeException("Failed to send event", e);
                                                } catch (Exception e) {
                                                    log.error(
                                                            "SseEmitter: Unexpected error sending event for session {}: {}",
                                                            sessionId,
                                                            e.getMessage(),
                                                            e);
                                                    throw new RuntimeException("Unexpected error sending event", e);
                                                }
                                            },
                                            error -> {
                                                log.error(
                                                        "SseEmitter: Stream error for session {}: {}",
                                                        sessionId,
                                                        error.getMessage(),
                                                        error);
                                                try {
                                                    emitter.completeWithError(error);
                                                } catch (Exception ex) {
                                                    log.warn(
                                                            "Error completing emitter after stream error for session {}: {}",
                                                            sessionId,
                                                            ex.getMessage());
                                                }
                                            },
                                            () -> {
                                                log.debug(
                                                        "SseEmitter: Stream completed normally for session: {}", sessionId);
                                                try {
                                                    emitter.complete();
                                                } catch (Exception ex) {
                                                    log.warn(
                                                            "Error completing emitter after normal completion for session {}:"
                                                                    + " {}",
                                                            sessionId,
                                                            ex.getMessage());
                                                }
                                            });
                    emitter.onCompletion(
                            () -> {
                                log.debug(
                                        "SseEmitter: onCompletion callback for session: {}. Disposing subscription.",
                                        sessionId);
                                if (!disposable.isDisposed()) {
                                    disposable.dispose();
                                }
                            });
                    emitter.onTimeout(
                            () -> {
                                log.debug(
                                        "SseEmitter: onTimeout callback for session: {}. Disposing subscription and"
                                                + " completing.",
                                        sessionId);
                                if (!disposable.isDisposed()) {
                                    disposable.dispose();
                                }
                                emitter.complete();
                            });
                });

        log.debug("SseEmitter: Returning emitter for session: {}", sessionId);
        return emitter;
    }

    @PostMapping("/createSession")
    public ResultDO<Session> createSession(@RequestBody BrowserAgentSessionRequest request) {

        log.info(
                "Request received for POST createSession: name: {}, userId; {}",
                request.getAppName(),
                request.getUser());

        Map<String, Object> initialState = Collections.emptyMap();
        try {

            Session createdSession =
                    sessionService
                            .createSession(request.getAppName(), request.getUser(), new ConcurrentHashMap<>(initialState), null)
                            .blockingGet();

            if (createdSession == null) {
                log.error(
                        "Session creation call completed without error but returned null session for user {}",
                        request.getUser());
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create session (null result)");
            }
            log.info("Session created successfully with generated id: {}", createdSession.id());
            return ResultDO.success(createdSession);
        } catch (Exception e) {
            log.error("Error creating session for user {}", request.getUser(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error creating session", e);
        }
    }

    @GetMapping("/getSession")
    public ResultDO<Session> getSession(
            @RequestParam String appName, @RequestParam String user, @RequestParam String sessionId) {
        log.info(
                "Request received for GET getSession, name: {}, userID: {}, sessionId: {}", appName, user, sessionId);
        return ResultDO.success(findSession(appName, user, sessionId));
    }

    private Session findSession(String appName, String userId, String sessionId) {
        Maybe<Session> maybeSession =
                sessionService.getSession(appName, userId, sessionId, Optional.empty());

        Session session = maybeSession.blockingGet();

        if (session == null) {
            log.warn(
                    "Session not found for appName={}, userId={}, sessionId={}",
                    appName,
                    userId,
                    sessionId);
            return null;
        }

        if (!Objects.equals(session.appName(), appName)
                || !Objects.equals(session.userId(), userId)) {
            log.warn(
                    "Session ID {} found but appName/userId mismatch (Expected: {}/{}, Found: {}/{}) -"
                            + " Treating as not found.",
                    sessionId,
                    appName,
                    userId,
                    session.appName(),
                    session.userId());

            return null;
        }
        log.debug("Found session: {}", sessionId);
        return session;
    }

}
