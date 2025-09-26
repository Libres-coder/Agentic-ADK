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
package com.alibaba.agentic.core.engine.delegation;

import com.alibaba.agentic.core.engine.delegation.domain.AgentRequest;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.google.adk.agents.InvocationContext;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * DESCRIPTION
 *
 * @author baliang.smy
 * @date 2025/7/17 10:13
 */
@Component
@Slf4j
public class DelegationAgent extends FrameworkDelegationBase {

    @Override
    public Flowable<Result> invoke(SystemContext systemContext, Request request) throws Throwable {
        try {
            AgentRequest agentRequest = new JSONObject(request.getParam()).toJavaObject(AgentRequest.class);
            
            // TODO coordinate
            return Flowable.just(Result.success(null));
            
        } catch (Throwable throwable) {
            log.error("agent invoke error", throwable);
            return Flowable.fromCallable(() -> Result.fail(throwable));
        }
    }

    @Override
    protected Map<String, Object> generateRequest(ExecutionContext executionContext, String activityId) {
        AgentRequest request = new AgentRequest();
        Map<String, Object> properties = super.generateRequest(executionContext, activityId);
        
        if (MapUtils.isEmpty(properties)) {
            return serializeRequest(request);
        }
        
        // TODO: 根据properties构建InvocationContext
        // if (properties.containsKey("invocationContext")) {
        //     Object contextValue = properties.get("invocationContext");
        //     InvocationContext context = buildInvocationContext(contextValue);
        //     request.setInvocationContext(context);
        // }
        
        return serializeRequest(request);
    }

    private Map<String, Object> serializeRequest(AgentRequest request) {
        try {
            if (request.getInvocationContext() == null) {
                return JSONObject.parseObject(JSONObject.toJSONString(request));
            }
            
            // TODO: 实现InvocationContext的安全序列化
            log.warn("InvocationContext serialization not yet implemented, using placeholder");
            AgentRequest placeholderRequest = new AgentRequest();
            return JSONObject.parseObject(JSONObject.toJSONString(placeholderRequest));
            
        } catch (Exception e) {
            log.error("Failed to serialize AgentRequest", e);
            AgentRequest fallbackRequest = new AgentRequest();
            return JSONObject.parseObject(JSONObject.toJSONString(fallbackRequest));
        }
    }

}
