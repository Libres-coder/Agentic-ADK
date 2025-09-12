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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Component
public class BrowserUseServiceCaller {

    private Map<String, CompletableFuture<String>> pendingRequest = new ConcurrentHashMap<>();

    public String callAndWait(String requestId, Runnable runnable) {
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequest.put(requestId, future);

        //执行下发
        runnable.run();

        try {
            return future.get(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("callAndWait fail, requestId: {}",requestId, e);
            return null;
        }
    }

    public void call(String requestId, Runnable runnable) {
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequest.put(requestId, future);
        //执行下发
        runnable.run();
    }

    public void handleCallback(String requestId, String result) {
        CompletableFuture<String> future = pendingRequest.get(requestId);
        if (future != null && !future.isDone()) {
            future.complete(result);
        }
    }

    public String getByRequestId(String requestId) {
        if(MapUtils.isEmpty(pendingRequest) || !pendingRequest.containsKey(requestId)) {
            return null;
        }
        try {
            return pendingRequest.get(requestId).get(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("getByRequestId fail, requestId: {}",requestId, e);
            return null;
        }
    }

}
