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

package com.alibaba.langengine.jina.sdk;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.jina.JinaConfiguration.JINA_API_KEY;
import static com.alibaba.langengine.jina.sdk.JinaConstant.BASE_URL;
import static com.alibaba.langengine.jina.sdk.JinaConstant.DEEPSEARCH_URL;
import static com.alibaba.langengine.jina.sdk.JinaConstant.READER_URL;
import static com.alibaba.langengine.jina.sdk.JinaConstant.SEARCH_URL;
import static com.alibaba.langengine.jina.sdk.JinaConstant.SEGMENTER_URL;
import static com.alibaba.langengine.jina.sdk.JinaConstant.TIME_OUT;

/**
 * Jina Crawler Java Client Service Get your Jina AI API key for free:
 * https://jina.ai/?sui=apikey
 */
public class JinaClient {

	private final OkHttpClient client;

	private final String apiKey;

	public JinaClient(String apiKey, int timeout, TimeUnit timeUnit) {
		this.apiKey = apiKey;
		this.client = new OkHttpClient.Builder().connectTimeout(timeout, timeUnit)
			.readTimeout(timeout, timeUnit)
			.writeTimeout(timeout, timeUnit)
			.build();
	}

    public JinaClient(int timeout, TimeUnit timeUnit) {
        this(JINA_API_KEY, timeout, timeUnit);
    }

	public JinaClient(String apiKey) {
		this(apiKey, TIME_OUT, TimeUnit.SECONDS);
	}

    public JinaClient() {
        this(JINA_API_KEY);
    }

	// Embeddings API
	public EmbeddingsResponse createEmbeddings(EmbeddingsRequest request) throws JinaException {
		try {
			RequestBody body = RequestBody.create(new ObjectMapper().writeValueAsString(request),
					MediaType.get("application/json"));

			Request httpRequest = new Request.Builder().url(BASE_URL + "/embeddings")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json")
				.addHeader("Accept", "application/json")
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new JinaException("Unexpected code " + response);
				}
				return new ObjectMapper().readValue(Objects.requireNonNull(response.body()).charStream(),
						EmbeddingsResponse.class);
			}
		}
		catch (IOException e) {
			throw new JinaException("Error while creating embeddings", e);
		}
	}

	// Reranker API
	public RerankResponse rerankDocuments(RerankRequest request) throws JinaException {
		try {
			RequestBody body = RequestBody.create(new ObjectMapper().writeValueAsString(request),
					MediaType.get("application/json"));

			Request httpRequest = new Request.Builder().url(BASE_URL + "/rerank")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json")
				.addHeader("Accept", "application/json")
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new JinaException("Unexpected code " + response);
				}
				return new ObjectMapper().readValue(Objects.requireNonNull(response.body()).charStream(),
						RerankResponse.class);
			}
		}
		catch (IOException e) {
			throw new JinaException("Error while reranking documents", e);
		}
	}

	// Reader API
	public ReaderResponse readUrl(ReaderRequest request, Map<String, String> headers) throws JinaException {
		try {
			RequestBody body = RequestBody.create(new ObjectMapper().writeValueAsString(request),
					MediaType.get("application/json"));

			Request.Builder requestBuilder = new Request.Builder().url(READER_URL + "/")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json")
				.addHeader("Accept", "application/json");

			// Add optional headers
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					requestBuilder.addHeader(entry.getKey(), entry.getValue());
				}
			}

			Request httpRequest = requestBuilder.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new JinaException("Unexpected code " + response);
				}
				return new ObjectMapper().readValue(Objects.requireNonNull(response.body()).charStream(),
						ReaderResponse.class);
			}
		}
		catch (IOException e) {
			throw new JinaException("Error while reading URL", e);
		}
	}

	// Search API
	public SearchResponse search(SearchRequest request, Map<String, String> headers) throws JinaException {
		try {
			RequestBody body = RequestBody.create(new ObjectMapper().writeValueAsString(request),
					MediaType.get("application/json"));

			Request.Builder requestBuilder = new Request.Builder().url(SEARCH_URL + "/")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json")
				.addHeader("Accept", "application/json");

			// Add optional headers
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					requestBuilder.addHeader(entry.getKey(), entry.getValue());
				}
			}

			Request httpRequest = requestBuilder.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new JinaException("Unexpected code " + response);
				}
				return new ObjectMapper().readValue(Objects.requireNonNull(response.body()).charStream(),
						SearchResponse.class);
			}
		}
		catch (IOException e) {
			throw new JinaException("Error while searching", e);
		}
	}

	// DeepSearch API
	public DeepSearchResponse deepSearch(DeepSearchRequest request) throws JinaException {
		try {
			RequestBody body = RequestBody.create(new ObjectMapper().writeValueAsString(request),
					MediaType.get("application/json"));

			Request httpRequest = new Request.Builder().url(DEEPSEARCH_URL + "/chat/completions")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json")
				.addHeader("Accept", "application/json")
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new JinaException("Unexpected code " + response);
				}
				return new ObjectMapper().readValue(Objects.requireNonNull(response.body()).charStream(),
						DeepSearchResponse.class);
			}
		}
		catch (IOException e) {
			throw new JinaException("Error while performing deep search", e);
		}
	}

	// Segmenter API
	public SegmenterResponse segmentText(SegmenterRequest request) throws JinaException {
		try {
			RequestBody body = RequestBody.create(new ObjectMapper().writeValueAsString(request),
					MediaType.get("application/json"));

			Request httpRequest = new Request.Builder().url(SEGMENTER_URL + "/")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json")
				.addHeader("Accept", "application/json")
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new JinaException("Unexpected code " + response);
				}
				return new ObjectMapper().readValue(Objects.requireNonNull(response.body()).charStream(),
						SegmenterResponse.class);
			}
		}
		catch (IOException e) {
			throw new JinaException("Error while segmenting text", e);
		}
	}

	// Classifier API
	public ClassifierResponse classify(ClassifierRequest request) throws JinaException {
		try {
			RequestBody body = RequestBody.create(new ObjectMapper().writeValueAsString(request),
					MediaType.get("application/json"));

			Request httpRequest = new Request.Builder().url(BASE_URL + "/classify")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json")
				.addHeader("Accept", "application/json")
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new JinaException("Unexpected code " + response);
				}
				return new ObjectMapper().readValue(Objects.requireNonNull(response.body()).charStream(),
						ClassifierResponse.class);
			}
		}
		catch (IOException e) {
			throw new JinaException("Error while classifying", e);
		}
	}

}
