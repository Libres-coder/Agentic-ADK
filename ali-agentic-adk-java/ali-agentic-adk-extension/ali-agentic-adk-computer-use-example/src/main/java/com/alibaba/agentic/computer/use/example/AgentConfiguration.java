package com.alibaba.agentic.computer.use.example;

import com.alibaba.agentic.computer.use.configuration.BrowserAgentRegister;
import com.alibaba.agentic.computer.use.service.BrowserUseServiceCaller;
import com.alibaba.agentic.computer.use.tool.BrowserUseTool;
import com.alibaba.agentic.core.utils.PromptUtils;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.tools.FunctionTool;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AgentConfiguration {

    @Autowired
    private BrowserUseServiceCaller browserUseServiceCaller;

    @Bean
    public LlmAgent exampleAgent(@Qualifier("loadedAgentRegistry") Map<String, BaseAgent> loadedAgentRegistry) {

        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .apiKey("lviizlCZ2Jadj3aA7kNPJXYlzdg5I5lZ2DC7D514FB8011ED829E2AD9CF8852D1")
                .modelName("qwen-plus")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        LangChain4j langChain4j = new LangChain4j(openAiChatModel);

        LlmAgent agent = LlmAgent.builder()
                .name("exampleAgent")
                .model(langChain4j)
                .instruction("你是阿里巴巴aib部门研究的一个叫marcos的机器人")
                .build();

        BrowserAgentRegister.register(agent, loadedAgentRegistry);
        return agent;

    }

    @Bean
    public LlmAgent browserOperateAgent(@Qualifier("loadedAgentRegistry") Map<String, BaseAgent> loadedAgentRegistry) {

        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .apiKey("lviizlCZ2Jadj3aA7kNPJXYlzdg5I5lZ2DC7D514FB8011ED829E2AD9CF8852D1")
                .modelName("qwen-plus")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        LangChain4j langChain4j = new LangChain4j(openAiChatModel);

        LlmAgent agent = LlmAgent.builder()
                .name("browserOperateAgent")
                .model(langChain4j)
                .instruction(PromptUtils.generatePrompt(PromptConstant.browserOperateAgentPrompt, this::getHtmlInfo))
                .tools(FunctionTool.create(BrowserUseTool.class, "operateBrowser"),
                        FunctionTool.create(BrowserUseTool.class, "openBrowser"),
                        FunctionTool.create(BrowserUseTool.class, "loginFinish")
                )
                .build();

        BrowserAgentRegister.register(agent, loadedAgentRegistry);
        return agent;
    }

    private Map<String, String> getHtmlInfo() {
        String html = browserUseServiceCaller.getByRequestId("browserOperate");
        if(StringUtils.isEmpty(html)) {
            return Map.of("htmlInfo", "None");
        }
        return Map.of("htmlInfo", html);
    }

}
