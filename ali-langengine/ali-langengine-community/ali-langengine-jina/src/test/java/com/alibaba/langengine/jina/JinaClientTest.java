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

package com.alibaba.langengine.jina;

import com.alibaba.langengine.jina.sdk.JinaClient;
import com.alibaba.langengine.jina.sdk.request.ClassifierRequest;
import com.alibaba.langengine.jina.sdk.request.DeepSearchRequest;
import com.alibaba.langengine.jina.sdk.request.EmbeddingsRequest;
import com.alibaba.langengine.jina.sdk.request.ReaderRequest;
import com.alibaba.langengine.jina.sdk.request.RerankRequest;
import com.alibaba.langengine.jina.sdk.request.SearchRequest;
import com.alibaba.langengine.jina.sdk.request.SegmenterRequest;
import com.alibaba.langengine.jina.sdk.response.ClassifierResponse;
import com.alibaba.langengine.jina.sdk.response.DeepSearchResponse;
import com.alibaba.langengine.jina.sdk.response.EmbeddingsResponse;
import com.alibaba.langengine.jina.sdk.response.ReaderResponse;
import com.alibaba.langengine.jina.sdk.response.RerankResponse;
import com.alibaba.langengine.jina.sdk.response.SearchResponse;
import com.alibaba.langengine.jina.sdk.response.SegmenterResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for JinaCrawlerService These tests make actual network calls to Jina AI APIs
 * Get your Jina AI API key for free: https://jina.ai/?sui=apikey
 */
@EnabledIfEnvironmentVariable(named = "JINA_API_KEY", matches = ".*")
public class JinaClientTest {

	private JinaClient jinaClient;

	@Before
	public void setUp() {
		// Get API key from environment variable
		String apiKey = System.getenv("JINA_API_KEY");
		assertNotNull("JINA_API_KEY environment variable must be set", apiKey);
		jinaClient = new JinaClient(apiKey);
	}

	@Test
	public void testCreateEmbeddingsTextInput() {
		// Given
		EmbeddingsRequest request = new EmbeddingsRequest();
		request.setModel("jina-embeddings-v3");
		request.setTask("text-matching");
		request.setInput(Arrays.asList("Hello, world!", "Jina AI is awesome"));

		// When
		EmbeddingsResponse response = jinaClient.createEmbeddings(request);

		// Then
		assertNotNull(response);
		assertNotNull(response.getData());
		assertFalse(response.getData().isEmpty());
		assertNotNull(response.getUsage());
		assertTrue(response.getUsage().getTotalTokens() > 0);
		assertNotNull(response.getData().get(0).getEmbedding());
	}

	@Test
	public void testRerankDocumentsTextReranker() {
		// Given
		RerankRequest request = new RerankRequest();
		request.setModel("jina-reranker-v2-base-multilingual");
		request.setQuery("Jina AI search foundation");
		request.setDocuments(Arrays.asList("Jina AI offers state-of-the-art search models",
				"The weather is sunny today", "Jina AI provides embeddings and rerankers for search applications"));

		// When
		RerankResponse response = jinaClient.rerankDocuments(request);

		// Then
		assertNotNull(response);
		assertNotNull(response.getResults());
		assertFalse(response.getResults().isEmpty());
		assertNotNull(response.getUsage());
		assertTrue(response.getUsage().getTotalTokens() > 0);

		// Check that results are sorted by relevance score (descending)
		for (int i = 0; i < response.getResults().size() - 1; i++) {
			assertTrue(response.getResults().get(i).getRelevanceScore() >= response.getResults()
				.get(i + 1)
				.getRelevanceScore());
		}
	}

	@Test
	public void testReadUrl() {
		// Given
		ReaderRequest request = new ReaderRequest();
		request.setUrl("https://jina.ai");

		Map<String, String> headers = new HashMap<>();
		headers.put("X-With-Links-Summary", "true");
		headers.put("X-With-Images-Summary", "true");

		// When
		ReaderResponse response = jinaClient.readUrl(request, headers);

		// Then
		assertNotNull(response);
		assertEquals(Integer.valueOf(200), response.getCode());
		assertNotNull(response.getData());
		assertNotNull(response.getData().getTitle());
		assertNotNull(response.getData().getContent());
		assertNotNull(response.getData().getUrl());
		assertNotNull(response.getData().getUsage());
	}

	@Test
	public void testSearch() {
		// Given
		SearchRequest request = new SearchRequest();
		request.setQuery("Jina AI founding date");

		Map<String, String> headers = new HashMap<>();
		headers.put("X-No-Cache", "true");

		// When
		SearchResponse response = jinaClient.search(request, headers);

		// Then
		assertNotNull(response);
		assertEquals(Integer.valueOf(200), response.getCode());
		assertNotNull(response.getData());
		assertFalse(response.getData().isEmpty());
		assertNotNull(response.getData().get(0).getTitle());
		assertNotNull(response.getData().get(0).getUrl());
		assertNotNull(response.getData().get(0).getContent());
		assertNotNull(response.getData().get(0).getUsage());
	}

	@Test
	@Disabled("由于网络原因可能导致超过默认TimeOut，用户实际使用时可以增大TimeOut的值")
	public void testDeepSearch() {
		// Given
		DeepSearchRequest request = new DeepSearchRequest();
		request.setModel("jina-deepsearch-v1");

		List<DeepSearchRequest.Message> messages = new ArrayList<>();
		DeepSearchRequest.Message userMessage = new DeepSearchRequest.Message();
		userMessage.setRole("user");
		userMessage.setContent("What is the latest news about Jina AI?");
		messages.add(userMessage);

		request.setMessages(messages);
		request.setReasoningEffort("medium");
		request.setMaxAttempts(1);
		request.setStream(false);

		// When
		DeepSearchResponse response = jinaClient.deepSearch(request);

		// Then
		assertNotNull(response);
		// Note: DeepSearch responses can vary, so we mainly check that we get a response
	}

	@Test
	public void testSegmentText() throws IOException {
		// Given
		SegmenterRequest request = new SegmenterRequest();
		request.setContent("Jina AI provides state-of-the-art search foundation models. "
				+ "These include embeddings, rerankers, and reader models. "
				+ "They are designed for multilingual and multimodal applications.");
		request.setReturnChunks(true);
		request.setReturnTokens(true);
		request.setMaxChunkLength(100);

		// When
		SegmenterResponse response = jinaClient.segmentText(request);

		// Then
		assertNotNull(response);
		assertNotNull(response.getNumTokens());
		assertTrue(response.getNumTokens() > 0);
		assertNotNull(response.getNumChunks());
		assertTrue(response.getNumChunks() > 0);
		assertNotNull(response.getChunks());
		assertFalse(response.getChunks().isEmpty());
		assertNotNull(response.getTokens());
		assertFalse(response.getTokens().isEmpty());
	}

	@Test
	public void testClassifyText() throws IOException {
		// Given
		ClassifierRequest request = new ClassifierRequest();
		request.setModel("jina-embeddings-v3");
		request.setInput(Arrays.asList("Running a marathon", "Writing a novel", "Simple walk in the park"));
		request.setLabels(Arrays.asList("Intensive activity", "Creative work", "Light activity"));

		// When
		ClassifierResponse response = jinaClient.classify(request);

		// Then
		assertNotNull(response);
		assertNotNull(response.getData());
		assertEquals(3, response.getData().size());
		assertNotNull(response.getUsage());
		assertTrue(response.getUsage().getTotalTokens() > 0);

		for (ClassifierResponse.ClassificationData data : response.getData()) {
			assertNotNull(data.getPrediction());
			assertNotNull(data.getScore());
			assertNotNull(data.getPredictions());
			assertFalse(data.getPredictions().isEmpty());
		}
	}

}
