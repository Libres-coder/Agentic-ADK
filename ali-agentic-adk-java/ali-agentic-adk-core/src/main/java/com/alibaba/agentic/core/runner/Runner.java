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
package com.alibaba.agentic.core.runner;

import com.alibaba.agentic.core.engine.constants.ExecutionConstant;
import com.alibaba.agentic.core.engine.dto.FlowDefinition;
import com.alibaba.agentic.core.engine.node.FlowCanvas;
import com.alibaba.agentic.core.executor.InvokeMode;
import com.alibaba.agentic.core.executor.Request;
import com.alibaba.agentic.core.executor.Result;
import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.flows.service.AgentProcessService;
import com.alibaba.agentic.core.flows.service.domain.TaskInstance;
import com.alibaba.agentic.core.flows.service.impl.FlowProcessService;
import com.alibaba.agentic.core.flows.storage.FlowStorageService;
import com.alibaba.agentic.core.runner.pipeline.PipelineRequest;
import com.alibaba.agentic.core.runner.pipeline.PipelineUtil;
import com.alibaba.agentic.core.utils.AssertUtils;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.ReplayProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 负责根据流程画布或流程定义启动执行，并协调同步/双工（BIDI）调用与异步回调的衔接。
 * <p>
 * - 当为同步调用时，直接按流程定义执行并返回结果流。
 * - 当为双工（BIDI）调用时，订阅外部事件流，将每次事件转化为一次流程请求并汇聚结果。
 * </p>
 *
 * @author baliang.smy
 * @date 2025/7/10 14:30
 */
@Service
@Slf4j
public class Runner {


    private static AgentProcessService agentProcessService;

    private static FlowProcessService flowProcessService;

    public Runner() {
    }

    @Autowired
    public void setAgentProcessService(AgentProcessService agentProcessService, FlowProcessService flowProcessService) {
        Runner.agentProcessService = agentProcessService;
        Runner.flowProcessService = flowProcessService;
    }

    /**
     * 通过流程画布运行。
     *
     * @param canvas  流程画布定义
     * @param request 执行请求，包含调用模式与参数
     * @return 结果流，按执行进度推送 {@link Result}
     */
    public Flowable<Result> run(FlowCanvas canvas, Request request) {
        AssertUtils.assertNotNull(request.getInvokeMode());
        FlowableProcessor<Result> resultClient = ReplayProcessor.create();
        FlowDefinition flowDefinition = agentProcessService.deploy(canvas);
        log.info("flowDefinition: " + flowDefinition);
        if (InvokeMode.BIDI.equals(request.getInvokeMode())) {
            request.getProcessor().subscribe(event -> {
                        try {
                            run(flowDefinition, new Request().setInvokeMode(InvokeMode.BIDI).setParam(event)).subscribe(resultClient::onNext);
                        } catch (Throwable throwable) {
                            resultClient.onNext(Result.fail(throwable));
                        }
                    },
                    throwable -> resultClient.onNext(Result.fail(throwable)),
                    resultClient::onComplete);
            return resultClient;
        }
        return run(flowDefinition, request);

    }

    /**
     * 异步任务回调入口。
     * <p>
     * 框架在外部异步任务完成后，通过该方法将结果注入到对应流程实例与节点，
     * 继续驱动流程向后执行。
     * </p>
     *
     * @param taskInstance 对应的任务实例（包含流程实例与节点信息）
     * @param result       外部任务返回的结果
     */
    public void signal(TaskInstance taskInstance, Result result) {
        flowProcessService.signal(taskInstance.getProcessInstance(),
                taskInstance.getActivityId(),
                Map.of(ExecutionConstant.ORIGIN_REQUEST, taskInstance.getRequest(),
                        ExecutionConstant.SYSTEM_CONTEXT, taskInstance.getSystemContext()),
                Map.of(ExecutionConstant.CALLBACK_RESULT, result));
    }


    /**
     * 按流程定义运行。
     *
     * @param flowDefinition 流程定义
     * @param request        执行请求
     * @return 结果流
     */
    private Flowable<Result> run(FlowDefinition flowDefinition, Request request) {
        return PipelineUtil.doPipe(PipelineRequest.builder()
                .flowDefinition(flowDefinition)
                .request(Map.of(ExecutionConstant.ORIGIN_REQUEST, request,
                        ExecutionConstant.SYSTEM_CONTEXT, new SystemContext()
                                .setInvokeMode(request.getInvokeMode())
                                .setRequestParameter(request.getParam()))).build());
    }

}
