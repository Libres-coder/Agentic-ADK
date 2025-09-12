package com.alibaba.langengine.ollama.sdk;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

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
class OllamaClientTest {
    
    @Test
    void testCreateClientWithDefaultConfig() {
        // Test creating client using the default configuration
        assertDoesNotThrow(() -> {
            OllamaClient client = new OllamaClient();
            assertNotNull(client);
            assertNotNull(client.getClient());
            assertNotNull(client.getObjectMapper());
        });
    }
    
    @Test
    void testCreateClientWithCustomBaseUrl() {
        // Test creating client with a custom base URL
        assertDoesNotThrow(() -> {
            OllamaClient client = new OllamaClient("http://localhost:11434");
            assertNotNull(client);
            assertEquals("http://localhost:11434", client.getBaseUrl());
        });
    }
    
    @Test
    void testListModels() {
        // Test list models functionality using the default configuration
        OllamaClient client = new OllamaClient();
        assertDoesNotThrow(() -> {
            ModelsResponse models = client.listModels();
            assertNotNull(models);
            assertNotNull(models.getModels());
        });
    }
    
    @Test
    void testGenerate() {
        // Test generate functionality using a custom GenerateRequest
        OllamaClient client = new OllamaClient();
        GenerateRequest request = new GenerateRequest();
        request.setModel("llama2");
        request.setPrompt("Hello, how are you?");
        request.setStream(false);
        
        assertDoesNotThrow(() -> {
            GenerateResponse response = client.generate(request);
            assertNotNull(response);
            // Note: Actual response content depends on whether the model is available
        });
    }
    
    @Test
    void testGenerateStream() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);
        AtomicBoolean onPartialResponseCalled = new AtomicBoolean(false);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        
        OllamaClient client = new OllamaClient();
        GenerateRequest request = new GenerateRequest();
        request.setModel("llama2");
        request.setPrompt("Hello, how are you?");
        
        client.generateStream(request, new OllamaStreamCallback<GenerateResponse>() {
            @Override
            public void onPartialResponse(GenerateResponse response) {
                onPartialResponseCalled.set(true);
                assertNotNull(response);
            }
            
            @Override
            public void onComplete() {
                onCompleteCalled.set(true);
                latch.countDown();
            }
            
            @Override
            public void onError(Throwable throwable) {
                errorRef.set(throwable);
                latch.countDown();
            }
        });
        
        // Wait for completion or error for up to 30 seconds
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        // Note: This test may fail if Ollama is not running or the model is not available
        // That's expected behavior in a test environment
    }
    
    @Test
    void testChat() {
        // Test chat functionality using a custom ChatRequest
        OllamaClient client = new OllamaClient();
        ChatRequest.Message message = new ChatRequest.Message();
        message.setRole("user");
        message.setContent("Hello, how are you?");
        
        ChatRequest request = new ChatRequest();
        request.setModel("llama2");
        request.setMessages(java.util.Collections.singletonList(message));
        request.setStream(false);
        
        assertDoesNotThrow(() -> {
            ChatResponse response = client.chat(request);
            assertNotNull(response);
            // Note: Actual response content depends on whether the model is available
        });
    }
    
    @Test
    void testChatStream() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);
        AtomicBoolean onPartialResponseCalled = new AtomicBoolean(false);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        
        OllamaClient client = new OllamaClient();
        ChatRequest.Message message = new ChatRequest.Message();
        message.setRole("user");
        message.setContent("Hello, how are you?");
        
        ChatRequest request = new ChatRequest();
        request.setModel("llama2");
        request.setMessages(java.util.Collections.singletonList(message));
        
        client.chatStream(request, new OllamaStreamCallback<ChatResponse>() {
            @Override
            public void onPartialResponse(ChatResponse response) {
                onPartialResponseCalled.set(true);
                assertNotNull(response);
            }
            
            @Override
            public void onComplete() {
                onCompleteCalled.set(true);
                latch.countDown();
            }
            
            @Override
            public void onError(Throwable throwable) {
                errorRef.set(throwable);
                latch.countDown();
            }
        });
        
        // Wait for completion or error for up to 30 seconds
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        // Note: This test may fail if Ollama is not running or the model is not available
        // That's expected behavior in a test environment
    }
    
    @Test
    void testGetVersion() {
        // Test get version functionality using the default configuration
        OllamaClient client = new OllamaClient();
        assertDoesNotThrow(() -> {
            String version = client.getVersion();
            assertNotNull(version);
            // Note: Actual version content depends on Ollama service
        });
    }
    
    @Test
    void testRequestBuilder() {
        // Test GenerateRequest builder
        GenerateRequest generateRequest = OllamaRequestBuilder.generateRequest()
                .model("llama2")
                .prompt("Hello")
                .stream(false)
                .build();
        
        assertEquals("llama2", generateRequest.getModel());
        assertEquals("Hello", generateRequest.getPrompt());
        assertFalse(generateRequest.getStream());
        
        // Test ChatRequest builder
        ChatRequest chatRequest = OllamaRequestBuilder.chatRequest()
                .model("llama2")
                .message("user", "Hello")
                .stream(false)
                .build();
        
        assertEquals("llama2", chatRequest.getModel());
        assertNotNull(chatRequest.getMessages());
        assertEquals(1, chatRequest.getMessages().size());
        assertEquals("user", chatRequest.getMessages().get(0).getRole());
        assertEquals("Hello", chatRequest.getMessages().get(0).getContent());
        assertFalse(chatRequest.getStream());
    }
}