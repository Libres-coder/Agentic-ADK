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
package com.alibaba.langengine.lancedb.client.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;


@Slf4j
public class LoggingInterceptor implements Interceptor {
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startTime = System.currentTimeMillis();
        
        log.debug("Sending {} request to: {}", request.method(), request.url());
        
        Response response = chain.proceed(request);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        if (response.isSuccessful()) {
            log.debug("Received successful response {} in {}ms", response.code(), duration);
        } else {
            log.warn("Received error response {} in {}ms", response.code(), duration);
        }
        
        return response;
    }
}
