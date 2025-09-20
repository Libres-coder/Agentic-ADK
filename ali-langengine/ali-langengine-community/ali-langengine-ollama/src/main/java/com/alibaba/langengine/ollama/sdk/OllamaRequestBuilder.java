package com.alibaba.langengine.ollama.sdk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for creating Ollama requests with a fluent API
 */
public class OllamaRequestBuilder {
    
    /**
     * Create a builder for GenerateRequest
     *
     * @return GenerateRequest builder
     */
    public static GenerateRequestBuilder generateRequest() {
        return new GenerateRequestBuilder();
    }
    
    /**
     * Create a builder for ChatRequest
     *
     * @return ChatRequest builder
     */
    public static ChatRequestBuilder chatRequest() {
        return new ChatRequestBuilder();
    }
    
    /**
     * Create a builder for PullRequest
     *
     * @return PullRequest builder
     */
    public static PullRequestBuilder pullRequest() {
        return new PullRequestBuilder();
    }
    
    /**
     * Create a builder for CreateRequest
     *
     * @return CreateRequest builder
     */
    public static CreateRequestBuilder createRequest() {
        return new CreateRequestBuilder();
    }
    
    /**
     * Builder for GenerateRequest
     */
    public static class GenerateRequestBuilder {
        private final GenerateRequest request = new GenerateRequest();
        
        public GenerateRequestBuilder model(String model) {
            request.setModel(model);
            return this;
        }
        
        public GenerateRequestBuilder prompt(String prompt) {
            request.setPrompt(prompt);
            return this;
        }
        
        public GenerateRequestBuilder suffix(String suffix) {
            request.setSuffix(suffix);
            return this;
        }
        
        public GenerateRequestBuilder images(List<String> images) {
            request.setImages(images);
            return this;
        }
        
        public GenerateRequestBuilder options(Map<String, Object> options) {
            request.setOptions(options);
            return this;
        }
        
        public GenerateRequestBuilder option(String key, Object value) {
            if (request.getOptions() == null) {
                request.setOptions(new HashMap<>());
            }
            request.getOptions().put(key, value);
            return this;
        }
        
        public GenerateRequestBuilder system(String system) {
            request.setSystem(system);
            return this;
        }
        
        public GenerateRequestBuilder template(String template) {
            request.setTemplate(template);
            return this;
        }
        
        public GenerateRequestBuilder stream(Boolean stream) {
            request.setStream(stream);
            return this;
        }
        
        public GenerateRequestBuilder context(List<Integer> context) {
            request.setContext(context);
            return this;
        }
        
        public GenerateRequestBuilder keepAlive(String keepAlive) {
            request.setKeep_alive(keepAlive);
            return this;
        }
        
        public GenerateRequest build() {
            return request;
        }
    }
    
    /**
     * Builder for ChatRequest
     */
    public static class ChatRequestBuilder {
        private final ChatRequest request = new ChatRequest();
        
        public ChatRequestBuilder model(String model) {
            request.setModel(model);
            return this;
        }
        
        public ChatRequestBuilder messages(List<ChatRequest.Message> messages) {
            request.setMessages(messages);
            return this;
        }
        
        public ChatRequestBuilder message(String role, String content) {
            ChatRequest.Message message = new ChatRequest.Message();
            message.setRole(role);
            message.setContent(content);
            
            if (request.getMessages() == null) {
                request.setMessages(new java.util.ArrayList<>());
            }
            request.getMessages().add(message);
            return this;
        }
        
        public ChatRequestBuilder options(Map<String, Object> options) {
            request.setOptions(options);
            return this;
        }
        
        public ChatRequestBuilder option(String key, Object value) {
            if (request.getOptions() == null) {
                request.setOptions(new HashMap<>());
            }
            request.getOptions().put(key, value);
            return this;
        }
        
        public ChatRequestBuilder system(String system) {
            request.setSystem(system);
            return this;
        }
        
        public ChatRequestBuilder template(String template) {
            request.setTemplate(template);
            return this;
        }
        
        public ChatRequestBuilder stream(Boolean stream) {
            request.setStream(stream);
            return this;
        }
        
        public ChatRequestBuilder keepAlive(String keepAlive) {
            request.setKeep_alive(keepAlive);
            return this;
        }
        
        public ChatRequestBuilder format(String format) {
            request.setFormat(format);
            return this;
        }
        
        public ChatRequest build() {
            return request;
        }
    }
    
    /**
     * Builder for PullRequest
     */
    public static class PullRequestBuilder {
        private final PullRequest request = new PullRequest();
        
        public PullRequestBuilder name(String name) {
            request.setName(name);
            return this;
        }
        
        public PullRequestBuilder stream(Boolean stream) {
            request.setStream(stream);
            return this;
        }
        
        public PullRequestBuilder insecure(Boolean insecure) {
            request.setInsecure(insecure);
            return this;
        }
        
        public PullRequestBuilder username(String username) {
            request.setUsername(username);
            return this;
        }
        
        public PullRequestBuilder password(String password) {
            request.setPassword(password);
            return this;
        }
        
        public PullRequest build() {
            return request;
        }
    }
    
    /**
     * Builder for CreateRequest
     */
    public static class CreateRequestBuilder {
        private final CreateRequest request = new CreateRequest();
        
        public CreateRequestBuilder name(String name) {
            request.setName(name);
            return this;
        }
        
        public CreateRequestBuilder path(String path) {
            request.setPath(path);
            return this;
        }
        
        public CreateRequestBuilder modelfile(String modelfile) {
            request.setModelfile(modelfile);
            return this;
        }
        
        public CreateRequestBuilder stream(Boolean stream) {
            request.setStream(stream);
            return this;
        }
        
        public CreateRequest build() {
            return request;
        }
    }
}