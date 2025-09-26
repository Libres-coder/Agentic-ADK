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
package com.alibaba.agentic.core.engine.node.sub;

import com.alibaba.agentic.core.engine.constants.NodeType;
import com.alibaba.agentic.core.engine.delegation.DelegationAgent;
import com.alibaba.agentic.core.engine.delegation.domain.AgentRequest;
import com.alibaba.agentic.core.engine.node.FlowNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AgentFlowNode extends FlowNode {

    private AgentRequest agentRequest;

    public AgentFlowNode() {
        this.name = "agentNode";
    }

    public AgentFlowNode(AgentRequest agentRequest) {
        this();
        this.agentRequest = agentRequest;
    }

    @Override
    protected String getNodeType() {
        return NodeType.AGENT;
    }

    @Override
    protected String getDelegationClassName() {
        return DelegationAgent.class.getName();
    }
}
