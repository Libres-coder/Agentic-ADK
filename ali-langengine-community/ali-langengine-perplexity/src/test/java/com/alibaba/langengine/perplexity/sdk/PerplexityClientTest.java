package com.alibaba.langengine.perplexity.sdk;

import com.alibaba.langengine.perplexity.PerplexityConfiguration;
import com.alibaba.langengine.perplexity.sdk.request.*;
import com.alibaba.langengine.perplexity.sdk.response.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PerplexityClient.
 * Tests will automatically run if API key is configured, otherwise they will be skipped.
 * Set your API key in src/test/resources/perplexity.properties to enable API tests.
 */
class PerplexityClientTest {

    private PerplexityClient client;
    private boolean hasApiKey;

    @BeforeEach
    void setUp() {
        // Initialize client with default configuration
        client = new PerplexityClient();
        
        // Check if API key is configured
        hasApiKey = PerplexityConfiguration.PERPLEXITY_API_KEY != null 
                   && !PerplexityConfiguration.PERPLEXITY_API_KEY.trim().isEmpty()
                   && !PerplexityConfiguration.PERPLEXITY_API_KEY.equals("your_perplexity_api_key_here")
                   && !PerplexityConfiguration.PERPLEXITY_API_KEY.equals("your_test_api_key_here");
        
        if (!hasApiKey) {
            System.out.println("API tests skipped: No valid API key configured. " +
                             "Set perplexity_api_key in src/test/resources/application.properties to run API tests.");
        }
    }

    @Test
    void testSyncChatCompletionSimple() {
        Assumptions.assumeTrue(hasApiKey, "Skipping API test: No API key configured");
        
        assertDoesNotThrow(() -> {
            ChatCompletionResponse response = client.chatCompletion("What is artificial intelligence?");
            assertNotNull(response);
            assertNotNull(response.getId());
            assertNotNull(response.getChoices());
            assertFalse(response.getChoices().isEmpty());
            assertNotNull(response.getChoices().get(0).getMessage());
            assertNotNull(response.getChoices().get(0).getMessage().getContent());
            System.out.println("Sync chat completion test passed!");
        });
    }

    @Test
    void testSyncChatCompletionWithModel() {
        Assumptions.assumeTrue(hasApiKey, "Skipping API test: No API key configured");
        
        assertDoesNotThrow(() -> {
            ChatCompletionResponse response = client.chatCompletion(
                "What are the latest developments in machine learning?",
                PerplexityModelConstant.SONAR_PRO
            );
            assertNotNull(response);
            assertNotNull(response.getChoices());
            assertFalse(response.getChoices().isEmpty());
            System.out.println("Sync chat completion with model test passed!");
        });
    }

    @Test
    void testSyncChatCompletionWithFullRequest() {
        Assumptions.assumeTrue(hasApiKey, "Skipping API test: No API key configured");
        
        assertDoesNotThrow(() -> {
            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel(PerplexityModelConstant.SONAR);
            
            Message systemMessage = new Message(Message.Role.SYSTEM, "Be precise and concise.");
            Message userMessage = new Message(Message.Role.USER, "Explain quantum computing");
            request.setMessages(Arrays.asList(systemMessage, userMessage));
            
            request.setSearchMode(ChatCompletionRequest.SearchMode.WEB);
            request.setReturnRelatedQuestions(true);
            request.setReturnImages(false);
            request.setMaxTokens(500);
            request.setTemperature(0.2);
            
            ChatCompletionResponse response = client.chatCompletion(request);
            assertNotNull(response);
            assertNotNull(response.getUsage());
            System.out.println("Full request test passed!");
        });
    }

    @Test
    void testAsyncChatCompletionCreate() {
        Assumptions.assumeTrue(hasApiKey, "Skipping API test: No API key configured");
        
        assertDoesNotThrow(() -> {
            AsyncJobResponse response = client.createAsyncChatCompletion(
                "Provide a comprehensive analysis of renewable energy trends",
                PerplexityModelConstant.SONAR_DEEP_RESEARCH
            );
            assertNotNull(response);
            assertNotNull(response.getId());
            assertNotNull(response.getStatus());
            assertTrue(Arrays.asList(
                AsyncJobResponse.Status.CREATED,
                AsyncJobResponse.Status.IN_PROGRESS
            ).contains(response.getStatus()));
            System.out.println("Async job creation test passed! Job ID: " + response.getId());
        });
    }

    @Test
    void testAsyncChatCompletionCreateWithFullRequest() {
        Assumptions.assumeTrue(hasApiKey, "Skipping API test: No API key configured");
        
        assertDoesNotThrow(() -> {
            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel(PerplexityModelConstant.SONAR_DEEP_RESEARCH);
            
            Message userMessage = new Message(Message.Role.USER, 
                "Conduct a detailed research on the impact of AI on healthcare");
            request.setMessages(Arrays.asList(userMessage));
            
            request.setSearchMode(ChatCompletionRequest.SearchMode.ACADEMIC);
            request.setReasoningEffort(ChatCompletionRequest.ReasoningEffort.HIGH);
            request.setReturnRelatedQuestions(true);
            request.setMaxTokens(2000);
            
            AsyncJobResponse response = client.createAsyncChatCompletion(request);
            assertNotNull(response);
            assertNotNull(response.getId());
            System.out.println("Async full request test passed! Job ID: " + response.getId());
        });
    }

    @Test
    void testAsyncChatCompletionList() {
        Assumptions.assumeTrue(hasApiKey, "Skipping API test: No API key configured");
        
        assertDoesNotThrow(() -> {
            AsyncListResponse response = client.listAsyncChatCompletions();
            assertNotNull(response);
            assertNotNull(response.getRequests());
            System.out.println("Async job list test passed! Found " + response.getRequests().size() + " jobs");
        });
    }

    @Test
    void testAsyncChatCompletionListWithPagination() {
        Assumptions.assumeTrue(hasApiKey, "Skipping API test: No API key configured");
        
        assertDoesNotThrow(() -> {
            AsyncListResponse response = client.listAsyncChatCompletions(5, null);
            assertNotNull(response);
            assertNotNull(response.getRequests());
            
            if (response.getNextToken() != null && !response.getNextToken().trim().isEmpty()) {
                // Test pagination if next token is available
                try {
                    AsyncListResponse nextPage = client.listAsyncChatCompletions(5, response.getNextToken());
                    assertNotNull(nextPage);
                    System.out.println("Pagination test passed!");
                } catch (PerplexityException e) {
                    if (e.getHttpStatusCode() == 400) {
                        System.out.println("Pagination token invalid (expected with some APIs) - test still passed!");
                    } else {
                        throw e;
                    }
                }
            } else {
                System.out.println("Pagination test passed (no next page available)!");
            }
        });
    }

    @Test
    void testAsyncChatCompletionGetWithInvalidId() {
        Assumptions.assumeTrue(hasApiKey, "Skipping API test: No API key configured");
        
        assertThrows(PerplexityException.class, () -> {
            client.getAsyncChatCompletion("invalid-id-that-does-not-exist");
        });
        System.out.println("Invalid ID error handling test passed!");
    }

    @Test
    void testConstructorWithApiKey() {
        assertDoesNotThrow(() -> {
            PerplexityClient customClient = new PerplexityClient("test-api-key");
            assertNotNull(customClient);
        });
    }

    @Test
    void testMissingApiKey() {
        PerplexityClient clientWithoutKey = new PerplexityClient("");
        assertThrows(PerplexityException.class, () -> {
            clientWithoutKey.chatCompletion("test query");
        });
    }

    @Test
    void testModelConstants() {
        assertEquals("sonar", PerplexityModelConstant.SONAR);
        assertEquals("sonar-pro", PerplexityModelConstant.SONAR_PRO);
        assertEquals("sonar-deep-research", PerplexityModelConstant.SONAR_DEEP_RESEARCH);
        assertEquals("sonar-reasoning", PerplexityModelConstant.SONAR_REASONING);
        assertEquals("sonar-reasoning-pro", PerplexityModelConstant.SONAR_REASONING_PRO);
    }

    @Test
    void testRequestBuilding() {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(PerplexityModelConstant.SONAR);
        request.setSearchMode(ChatCompletionRequest.SearchMode.WEB);
        request.setReasoningEffort(ChatCompletionRequest.ReasoningEffort.MEDIUM);
        request.setReturnImages(true);
        request.setReturnRelatedQuestions(true);
        request.setTemperature(0.7);
        request.setMaxTokens(1000);
        
        assertEquals(PerplexityModelConstant.SONAR, request.getModel());
        assertEquals(ChatCompletionRequest.SearchMode.WEB, request.getSearchMode());
        assertEquals(ChatCompletionRequest.ReasoningEffort.MEDIUM, request.getReasoningEffort());
        assertTrue(request.getReturnImages());
        assertTrue(request.getReturnRelatedQuestions());
        assertEquals(0.7, request.getTemperature());
        assertEquals(1000, request.getMaxTokens());
    }

    @Test
    void testMessageCreation() {
        Message textMessage = new Message(Message.Role.USER, "Hello, world!");
        assertEquals(Message.Role.USER, textMessage.getRole());
        assertEquals("Hello, world!", textMessage.getTextContent());
        
        Message systemMessage = new Message(Message.Role.SYSTEM, "Be helpful");
        assertEquals(Message.Role.SYSTEM, systemMessage.getRole());
        assertEquals("Be helpful", systemMessage.getTextContent());
    }

    @Test
    void testWebSearchOptions() {
        WebSearchOptions options = new WebSearchOptions();
        options.setSearchContextSize(WebSearchOptions.SearchContextSize.HIGH);
        options.setImageSearchRelevanceEnhanced(true);
        
        WebSearchOptions.UserLocation location = new WebSearchOptions.UserLocation();
        location.setCountry("US");
        location.setRegion("California");
        location.setCity("San Francisco");
        options.setUserLocation(location);
        
        assertEquals(WebSearchOptions.SearchContextSize.HIGH, options.getSearchContextSize());
        assertTrue(options.getImageSearchRelevanceEnhanced());
        assertNotNull(options.getUserLocation());
        assertEquals("US", options.getUserLocation().getCountry());
    }
}