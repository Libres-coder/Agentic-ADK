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
package com.alibaba.agentic.core.engine.node;


import com.alibaba.agentic.core.engine.behavior.ConditionRegistry;
import com.alibaba.agentic.core.engine.constants.NodeIdConstant;
import com.alibaba.agentic.core.engine.constants.PropertyConstant;
import com.alibaba.agentic.core.engine.node.sub.ConditionalContainer;
import com.alibaba.agentic.core.engine.node.sub.NopFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ParallelFlowNode;
import com.alibaba.agentic.core.engine.node.sub.ReferenceFlowNode;
import com.alibaba.agentic.core.engine.utils.XmlUtils;
import com.alibaba.agentic.core.exceptions.BaseException;
import com.alibaba.agentic.core.exceptions.ErrorEnum;
import com.alibaba.agentic.core.utils.AssertUtils;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * 流程节点抽象基类。
 * <p>
 * 定义流程中节点的基本结构与行为，支持线性连接、条件分支等流程控制。
 * 每个节点可配置后续节点（next）或条件分支（conditionalContainerList），
 * 但两者不能同时存在。
 * </p>
 *
 * @author 框架团队
 */
@Data
@Accessors(chain = true)
public abstract class FlowNode {

    /**
     * 节点唯一标识。
     */
    private String id;

    /**
     * 节点名称。
     */
    private String name;

    /**
     * 线性后续节点。
     * <p>
     * 注意：next 与 conditionalContainerList 字段仅允许至多一者非空。
     * 用于不存在分支条件情况下的唯一后续节点。
     * </p>
     */
    private FlowNode next;

    /**
     * 条件分支容器列表。
     * <p>
     * 包含所有分支条件与对应的后续节点，用于实现条件路由。
     * </p>
     */
    private List<ConditionalContainer> conditionalContainerList;

    /**
     * 分支条件的兜底后续节点。
     * <p>
     * 当 conditionalContainerList 中的条件均不命中时，走该兜底逻辑
     * 以保证流程正常运行。只有在 conditionalContainerList 非空时，
     * elseNext 才有效。默认连接一个空操作节点并连向结束节点。
     * </p>
     */
    private FlowNode elseNext;

    /**
     * 网关实例，在一个 FlowNode 实例内单例。
     */
    private Gateway gateway;

    // 节点类型
    protected abstract String getNodeType();

    // delegation的class name
    protected abstract String getDelegationClassName();

    // 检查在串联节点、选择节点、并行节点等类型中是否至多配置一种类型
    protected boolean checkAtMostOneTypeOfNextNode() {
        int count = 0;
        if (Objects.nonNull(this.next)) {
            count++;
        }
        if (CollectionUtils.isNotEmpty(this.conditionalContainerList)) {
            count++;
        }
        return count <= 1;
    }

    // 生成当前节点的bpmn xml文件
    protected void generate(Element processElement) {
        if (!checkAtMostOneTypeOfNextNode()) {
            throw new BaseException(
                    String.format("next: %s, conditionalFancyNodeList: %s, only allow one type of next node.", this.next, this.conditionalContainerList),
                    ErrorEnum.FLOW_CONFIG_ERROR
            );
        }

        // 构建gateway(ExclusiveGateway)
        if (CollectionUtils.isNotEmpty(this.conditionalContainerList)) {
            if (Objects.isNull(this.gateway)) {
                throw new BaseException("gateway is null during generation of xml.", ErrorEnum.SYSTEM_ERROR);
            }
            Element exclusiveGateway = processElement.addElement(QName.get("exclusiveGateway", Namespace.NO_NAMESPACE));
            exclusiveGateway.addAttribute("id", this.gateway.getGatewayId());
            exclusiveGateway.addAttribute("name", this.gateway.getGatewayName());
            XmlUtils.genEdge(processElement, this.getId(), this.gateway.getGatewayId());
            this.conditionalContainerList.forEach(node -> {
                if (Objects.nonNull(node.getFlowNode())) {
                    XmlUtils.genEdge(processElement,
                            this.gateway.getGatewayId(),
                            node.getFlowNode().getId()
                    );
                }
            });
            if (Objects.nonNull(this.elseNext)) {
                Element exclusiveGateway2ElseNextEdge = XmlUtils.genEdge(processElement,
                        this.gateway.getGatewayId(),
                        this.elseNext.getId(),
                        Map.of(PropertyConstant.SYMBOL_KEY, PropertyConstant.SYMBOL_VALUE_CONDITION_DEFAULT_FLOW)
                );
                exclusiveGateway.addAttribute(PropertyConstant.DEFAULT, exclusiveGateway2ElseNextEdge.attributeValue("id"));
            }
        }

        // 构建节点自身
        Element serviceTask = processElement.addElement(QName.get("serviceTask", Namespace.NO_NAMESPACE));
        serviceTask.addAttribute("id", getId());
        serviceTask.addAttribute("name", name);
        serviceTask.addAttribute("smart:class", getDelegationClassName());

        addProperties(serviceTask);

        // 如果next不为空，则构建指向next的边
        if (Objects.nonNull(this.next)) {
            String sourceCode = getId();
            String targetCode = this.next.getId();
            XmlUtils.genEdge(processElement, sourceCode, targetCode);
        }
    }

    protected void addProperties(Element serviceTask) {

    }

    // 设置自己的下一个节点
    public FlowNode next(FlowNode node) {
        this.next = node;
        return this;
    }

    public FlowNode nextOnCondition(ConditionalContainer conditionalContainer) {
        if (Objects.isNull(this.gateway)) {
            this.gateway = new FlowNode.ExclusiveGateway();
        }
        if (CollectionUtils.isEmpty(this.conditionalContainerList)) {
            this.conditionalContainerList = new ArrayList<>();
        }
        this.conditionalContainerList.add(conditionalContainer);
        if (Objects.isNull(this.elseNext)) {
            this.elseNext = new NopFlowNode();
        }
        return this;
    }

    public FlowNode nextOnCondition(List<ConditionalContainer> conditionalContainerList) {
        if (Objects.isNull(this.gateway)) {
            this.gateway = new FlowNode.ExclusiveGateway();
        }
        if (CollectionUtils.isEmpty(this.conditionalContainerList)) {
            this.conditionalContainerList = new ArrayList<>();
        }
        this.conditionalContainerList.addAll(conditionalContainerList);
        if (Objects.isNull(this.elseNext)) {
            this.elseNext = new NopFlowNode();
        }
        return this;
    }

    public FlowNode nextOnElse(FlowNode node) {
        this.elseNext = node;
        return this;
    }

    public FlowNode nextOnParallel(List<FlowNode> nodeList, ExecutorService executorService) {
        ParallelFlowNode flowNode = new ParallelFlowNode();
        flowNode.setParallelNodeList(nodeList);
        flowNode.setExecutorService(executorService);
        return nextOnParallel(flowNode);
    }

    public FlowNode nextOnParallel(ParallelFlowNode node) {
        throw new IllegalArgumentException("only support on parallel node");
    }

    public FlowNode next(FlowCanvas canvas) {
        ReferenceFlowNode flowNode = new ReferenceFlowNode();
        flowNode.setCanvas(canvas);
        return next(flowNode);
    }

    public FlowNode next(ReferenceFlowNode node) {
        throw new IllegalArgumentException("only support on reference node");
    }

    // 转移到下一个节点
    public FlowNode toNext() {
        return next;
    }

    // 按照next conditionalFancyNodeList的顺序选择第一个name相同的node作为返回
    // 支持返回node的真实格式
    public FlowNode toNext(String name) {
        return this;
    }

    public String getId() {
        if (StringUtils.isBlank(id)) {
            id = name + "_" + UUID.randomUUID();
        }
        return id;
    }

    public FlowNode setId(String id) {
        this.id = id;
        AssertUtils.assertNotIn(id, NodeIdConstant.RESERVED_NODE_ID);
        return this;
    }

    public void registerCondition(ConditionalContainer conditionalContainer) {
        if (Objects.nonNull(conditionalContainer.getFlowNode())) {
            ConditionRegistry.register(gateway.getGatewayId(), conditionalContainer);
        } else {
            throw new BaseException(String.format("The instance of ConditionFlowNode has no block after branch decision. " +
                    "Please configure its field of flowNode. Predecessor Node: %s, Current Node: %s", this, conditionalContainer), ErrorEnum.PROPERTY_CONFIG_ERROR);
        }
    }

    protected abstract class Gateway {

        @Getter
        protected String gatewayId;

        @Getter
        protected String gatewayName;

        @Getter
        protected String gatewayType;

        protected void initGatewayIdAndName() {
            this.gatewayName = this.gatewayType;
            this.gatewayId = this.gatewayName + "_" + UUID.randomUUID();
        }

    }

    protected class ExclusiveGateway extends Gateway {
        ExclusiveGateway() {
            this.gatewayType = "exclusiveGateway";
            initGatewayIdAndName();
        }

    }

    protected class ParallelGateway extends Gateway {
        ParallelGateway() {
            this.gatewayType = "parallelGateway";
            initGatewayIdAndName();
        }

    }


}
