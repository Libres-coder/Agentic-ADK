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
package com.alibaba.langengine.onedrive.client;

import com.alibaba.langengine.onedrive.OneDriveConfiguration;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.httpcore.AuthenticationHandler;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 * OneDrive 认证提供者
 * 
 * @author AIDC-AI
 */
public class OneDriveAuthProvider implements IAuthenticationProvider {
    
    private final OneDriveConfiguration configuration;
    
    /**
     * 构造函数
     * 
     * @param configuration OneDrive 配置
     */
    public OneDriveAuthProvider(OneDriveConfiguration configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public CompletableFuture<String> getAuthorizationTokenAsync(String requestUrl) {
        return CompletableFuture.supplyAsync(() -> {
            String accessToken = configuration.getEffectiveAccessToken();
            if (StringUtils.isBlank(accessToken)) {
                throw new RuntimeException("No valid access token available");
            }
            return "Bearer " + accessToken;
        });
    }
    
    @Override
    public CompletableFuture<Request> authenticateRequestAsync(Request request) {
        return getAuthorizationTokenAsync(request.url().toString())
                .thenApply(token -> request.newBuilder()
                        .addHeader("Authorization", token)
                        .build());
    }
}
