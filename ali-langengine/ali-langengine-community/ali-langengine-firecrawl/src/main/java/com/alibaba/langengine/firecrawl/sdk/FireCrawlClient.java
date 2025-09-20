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

package com.alibaba.langengine.firecrawl.sdk;

import com.alibaba.langengine.firecrawl.sdk.request.BatchScrapeRequest;
import com.alibaba.langengine.firecrawl.sdk.request.CrawlParamsPreviewRequest;
import com.alibaba.langengine.firecrawl.sdk.request.CrawlRequest;
import com.alibaba.langengine.firecrawl.sdk.request.ExtractRequest;
import com.alibaba.langengine.firecrawl.sdk.request.MapRequest;
import com.alibaba.langengine.firecrawl.sdk.request.ScrapeRequest;
import com.alibaba.langengine.firecrawl.sdk.request.SearchRequest;
import com.alibaba.langengine.firecrawl.sdk.response.BatchScrapeErrorsResponse;
import com.alibaba.langengine.firecrawl.sdk.response.BatchScrapeResponse;
import com.alibaba.langengine.firecrawl.sdk.response.BatchScrapeStatusResponse;
import com.alibaba.langengine.firecrawl.sdk.response.CancelBatchScrapeResponse;
import com.alibaba.langengine.firecrawl.sdk.response.CancelCrawlResponse;
import com.alibaba.langengine.firecrawl.sdk.response.CrawlErrorsResponse;
import com.alibaba.langengine.firecrawl.sdk.response.CrawlParamsPreviewResponse;
import com.alibaba.langengine.firecrawl.sdk.response.CrawlResponse;
import com.alibaba.langengine.firecrawl.sdk.response.CrawlStatusResponse;
import com.alibaba.langengine.firecrawl.sdk.response.CreditUsageResponse;
import com.alibaba.langengine.firecrawl.sdk.response.ErrorResponse;
import com.alibaba.langengine.firecrawl.sdk.response.ExtractResponse;
import com.alibaba.langengine.firecrawl.sdk.response.ExtractStatusResponse;
import com.alibaba.langengine.firecrawl.sdk.response.GetActiveCrawlsResponse;
import com.alibaba.langengine.firecrawl.sdk.response.MapResponse;
import com.alibaba.langengine.firecrawl.sdk.response.ScrapeResponse;
import com.alibaba.langengine.firecrawl.sdk.response.SearchResponse;
import com.alibaba.langengine.firecrawl.sdk.response.TokenUsageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.UUID;

import static com.alibaba.langengine.firecrawl.FireCrawlConfiguration.FIRE_CRAWL_API_KEY;
import static com.alibaba.langengine.firecrawl.sdk.FireCrawlConstant.FIRE_CRAWL_BASE_URL;

public class FireCrawlClient {

	private final OkHttpClient client;

	private final ObjectMapper objectMapper;

	private final String apiKey;

	public FireCrawlClient(String apiKey) {
		this.client = new OkHttpClient();
		this.objectMapper = new ObjectMapper();
		this.apiKey = apiKey;
	}

    public FireCrawlClient() {
        this(FIRE_CRAWL_API_KEY);
    }

	/**
	 * Scrape a URL using FireCrawl service
	 * @param request The scrape request parameters
	 * @return The scrape response
	 * @throws FireCrawlException if an error occurs during the request
	 */
	public ScrapeResponse scrape(ScrapeRequest request) throws FireCrawlException {
		try {
			String json = objectMapper.writeValueAsString(request);
			RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

			Request httpRequest = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/scrape")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					handleErrorResponse(response);
				}

				ResponseBody responseBody = response.body();
				if (responseBody == null) {
					throw new FireCrawlException("Empty response body");
				}

				return objectMapper.readValue(responseBody.string(), ScrapeResponse.class);
			}
		}
		catch (IOException e) {
			throw new FireCrawlException("Error occurred while scraping", e);
		}
	}

	private void handleErrorResponse(Response response) throws IOException, FireCrawlException {
		ResponseBody responseBody = response.body();
		if (responseBody != null) {
			String errorBody = responseBody.string();
			try {
				ErrorResponse errorResponse = objectMapper.readValue(errorBody, ErrorResponse.class);
				throw new FireCrawlException(
						"API Error: " + errorResponse.getError() + " (Code: " + response.code() + ")");
			}
			catch (Exception e) {
				throw new FireCrawlException("HTTP Error " + response.code() + ": " + errorBody);
			}
		}
		else {
			throw new FireCrawlException("HTTP Error " + response.code());
		}
	}

	public BatchScrapeResponse batchScrape(BatchScrapeRequest request) throws FireCrawlException {
		try {
			String json = objectMapper.writeValueAsString(request);
			RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

			Request httpRequest = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/batch/scrape")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json")
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					String errorMessage = response.body() != null ? response.body().string() : "Unknown error";
					throw new FireCrawlException(
							"API request failed with code: " + response.code() + ", message: " + errorMessage);
				}

				if (response.body() == null) {
					throw new FireCrawlException("Empty response body");
				}

				String responseBody = response.body().string();
				return objectMapper.readValue(responseBody, BatchScrapeResponse.class);
			}
		}
		catch (IOException e) {
			throw new FireCrawlException("Failed to execute batch scrape request", e);
		}
	}

	/**
	 * Get the status of a batch scrape job
	 * @param id The ID of the batch scrape job
	 * @return BatchScrapeStatusResponse containing the status information
	 * @throws FireCrawlException if the request fails
	 */
	public BatchScrapeStatusResponse getBatchScrapeStatus(String id) throws FireCrawlException {
		Request request = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/batch/scrape/" + id)
			.addHeader("Authorization", "Bearer " + apiKey)
			.get()
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new FireCrawlException("Failed to get batch scrape status: " + response.code());
			}

			if (response.body() == null) {
				throw new FireCrawlException("Empty response body");
			}

			return objectMapper.readValue(response.body().string(), BatchScrapeStatusResponse.class);
		}
		catch (IOException e) {
			throw new FireCrawlException("Error getting batch scrape status", e);
		}
	}

	/**
	 * Get errors from a batch scrape job
	 * @param id The ID of the batch scrape job
	 * @return BatchScrapeErrorsResponse containing error information
	 * @throws FireCrawlException if the request fails
	 */
	public BatchScrapeErrorsResponse getBatchScrapeErrors(String id) throws FireCrawlException {
		Request request = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/batch/scrape/" + id + "/errors")
			.addHeader("Authorization", "Bearer " + apiKey)
			.get()
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new FireCrawlException("Failed to get batch scrape errors: " + response.code());
			}

			if (response.body() == null) {
				throw new FireCrawlException("Empty response body");
			}

			return objectMapper.readValue(response.body().string(), BatchScrapeErrorsResponse.class);
		}
		catch (IOException e) {
			throw new FireCrawlException("Error getting batch scrape errors", e);
		}
	}

	/**
	 * Cancel a batch scrape job
	 * @param id The ID of the batch scrape job
	 * @return CancelBatchScrapeResponse containing the result of the cancellation
	 * @throws FireCrawlException if the request fails
	 */
	public CancelBatchScrapeResponse cancelBatchScrape(String id) throws FireCrawlException {
		Request request = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/batch/scrape/" + id)
			.addHeader("Authorization", "Bearer " + apiKey)
			.delete()
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new FireCrawlException("Failed to cancel batch scrape: " + response.code());
			}

			if (response.body() == null) {
				throw new FireCrawlException("Empty response body");
			}

			return objectMapper.readValue(response.body().string(), CancelBatchScrapeResponse.class);
		}
		catch (IOException e) {
			throw new FireCrawlException("Error canceling batch scrape", e);
		}
	}

	/**
	 * Perform a search using the FireCrawl Search API
	 * @param request The search request parameters
	 * @return The search response with results
	 * @throws FireCrawlException if there's an error with the request
	 */
	public SearchResponse search(SearchRequest request) throws FireCrawlException {
		try {
			String json = objectMapper.writeValueAsString(request);
			RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
			Request httpRequest = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/search")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new FireCrawlException("Unexpected response code: " + response);
				}

				ResponseBody responseBody = response.body();
				if (responseBody == null) {
					throw new FireCrawlException("Empty response body");
				}

				return objectMapper.readValue(responseBody.string(), SearchResponse.class);
			}
		}
		catch (IOException e) {
			throw new FireCrawlException("Error executing search request", e);
		}
	}

	/**
	 * Map endpoint - discover URLs from a website
	 * @param request The map request parameters
	 * @return MapResponse containing discovered URLs
	 * @throws FireCrawlException if the request fails
	 */
	public MapResponse map(MapRequest request) throws FireCrawlException {
		try {
			String json = objectMapper.writeValueAsString(request);
			RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

			Request httpRequest = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/map")
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new FireCrawlException("Request failed with code: " + response.code());
				}

				ResponseBody responseBody = response.body();
				if (responseBody == null) {
					throw new FireCrawlException("Empty response body");
				}

				return objectMapper.readValue(responseBody.string(), MapResponse.class);
			}
		}
		catch (IOException e) {
			throw new FireCrawlException("Error executing map request", e);
		}
	}

	/**
	 * Get active crawls for the team
	 * @return GetActiveCrawlsResponse
	 * @throws FireCrawlException if request fails
	 */
	public GetActiveCrawlsResponse getActiveCrawls() throws FireCrawlException {
		Request request = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/crawl/active")
			.addHeader("Authorization", "Bearer " + apiKey)
			.get()
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new FireCrawlException("Failed to get active crawls: " + response.code());
			}
			String responseBody = response.body().string();
			return objectMapper.readValue(responseBody, GetActiveCrawlsResponse.class);
		}
		catch (IOException e) {
			throw new FireCrawlException("Error getting active crawls", e);
		}
	}

	/**
	 * Start a new crawl job
	 * @param request Crawl request parameters
	 * @return CrawlResponse with job ID
	 * @throws FireCrawlException if request fails
	 */
	public CrawlResponse crawl(CrawlRequest request) throws FireCrawlException {
		try {
			String json = objectMapper.writeValueAsString(request);
			Request httpRequest = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/crawl")
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json")
				.post(RequestBody.create(json, MediaType.get("application/json")))
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new FireCrawlException("Failed to start crawl: " + response.code());
				}
				String responseBody = response.body().string();
				return objectMapper.readValue(responseBody, CrawlResponse.class);
			}
		}
		catch (IOException e) {
			throw new FireCrawlException("Error starting crawl", e);
		}
	}

	/**
	 * Get status of a crawl job
	 * @param id ID of the crawl job
	 * @return CrawlStatusResponse with current status and data
	 * @throws FireCrawlException if request fails
	 */
	public CrawlStatusResponse getCrawlStatus(UUID id) throws FireCrawlException {
		Request request = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/crawl/" + id.toString())
			.addHeader("Authorization", "Bearer " + apiKey)
			.get()
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new FireCrawlException("Failed to get crawl status: " + response.code());
			}
			String responseBody = response.body().string();
			return objectMapper.readValue(responseBody, CrawlStatusResponse.class);
		}
		catch (IOException e) {
			throw new FireCrawlException("Error getting crawl status", e);
		}
	}

	/**
	 * Cancel a crawl job
	 * @param id ID of the crawl job to cancel
	 * @return CancelCrawlResponse with status
	 * @throws FireCrawlException if request fails
	 */
	public CancelCrawlResponse cancelCrawl(UUID id) throws FireCrawlException {
		Request request = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/crawl/" + id.toString())
			.addHeader("Authorization", "Bearer " + apiKey)
			.delete()
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new FireCrawlException("Failed to cancel crawl: " + response.code());
			}
			String responseBody = response.body().string();
			return objectMapper.readValue(responseBody, CancelCrawlResponse.class);
		}
		catch (IOException e) {
			throw new FireCrawlException("Error cancelling crawl", e);
		}
	}

	/**
	 * Get errors for a crawl job
	 * @param id ID of the crawl job
	 * @return CrawlErrorsResponse with error details
	 * @throws FireCrawlException if request fails
	 */
	public CrawlErrorsResponse getCrawlErrors(UUID id) throws FireCrawlException {
		Request request = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/crawl/" + id.toString() + "/errors")
			.addHeader("Authorization", "Bearer " + apiKey)
			.get()
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new FireCrawlException("Failed to get crawl errors: " + response.code());
			}
			String responseBody = response.body().string();
			return objectMapper.readValue(responseBody, CrawlErrorsResponse.class);
		}
		catch (IOException e) {
			throw new FireCrawlException("Error getting crawl errors", e);
		}
	}

	/**
	 * Preview crawl parameters based on natural language prompt
	 * @param request CrawlParamsPreviewRequest with URL and prompt
	 * @return CrawlParamsPreviewResponse with generated parameters
	 * @throws FireCrawlException if request fails
	 */
	public CrawlParamsPreviewResponse crawlParamsPreview(CrawlParamsPreviewRequest request) throws FireCrawlException {
		try {
			String json = objectMapper.writeValueAsString(request);
			Request httpRequest = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/crawl/params-preview")
				.addHeader("Authorization", "Bearer " + apiKey)
				.addHeader("Content-Type", "application/json")
				.post(RequestBody.create(json, MediaType.get("application/json")))
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new FireCrawlException("Failed to preview crawl params: " + response.code());
				}
				String responseBody = response.body().string();
				return objectMapper.readValue(responseBody, CrawlParamsPreviewResponse.class);
			}
		}
		catch (IOException e) {
			throw new FireCrawlException("Error previewing crawl params", e);
		}
	}

	/**
	 * Extract data from URLs
	 * @param request The extract request parameters
	 * @return Extract response with job ID
	 * @throws FireCrawlException if the request fails
	 */
	public ExtractResponse extract(ExtractRequest request) throws FireCrawlException {
		try {
			String url = FIRE_CRAWL_BASE_URL + "/extract";
			String json = objectMapper.writeValueAsString(request);
			RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
			Request httpRequest = new Request.Builder().url(url)
				.post(body)
				.addHeader("Authorization", "Bearer " + apiKey)
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new FireCrawlException("Unexpected response code: " + response);
				}

				ResponseBody responseBody = response.body();
				if (responseBody == null) {
					throw new FireCrawlException("Empty response body");
				}

				return objectMapper.readValue(responseBody.string(), ExtractResponse.class);
			}
		}
		catch (IOException e) {
			throw new FireCrawlException("Failed to extract data", e);
		}
	}

	/**
	 * Get the status of an extract job
	 * @param id The ID of the extract job
	 * @return Extract status response
	 * @throws FireCrawlException if the request fails
	 */
	public ExtractStatusResponse getExtractStatus(String id) throws FireCrawlException {
		try {
			String url = FIRE_CRAWL_BASE_URL + "/extract/" + id;
			Request httpRequest = new Request.Builder().url(url)
				.get()
				.addHeader("Authorization", "Bearer " + apiKey)
				.build();

			try (Response response = client.newCall(httpRequest).execute()) {
				if (!response.isSuccessful()) {
					throw new FireCrawlException("Unexpected response code: " + response);
				}

				ResponseBody responseBody = response.body();
				if (responseBody == null) {
					throw new FireCrawlException("Empty response body");
				}

				return objectMapper.readValue(responseBody.string(), ExtractStatusResponse.class);
			}
		}
		catch (IOException e) {
			throw new FireCrawlException("Failed to get extract status", e);
		}
	}

	/**
	 * Get token usage for the team
	 * @return TokenUsageResponse containing remaining tokens
	 * @throws FireCrawlException if API call fails
	 */
	public TokenUsageResponse getTokenUsage() throws FireCrawlException {
		Request request = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/team/token-usage")
			.addHeader("Authorization", "Bearer " + apiKey)
			.get()
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new FireCrawlException("API request failed with code: " + response.code());
			}

			ResponseBody responseBody = response.body();
			if (responseBody == null) {
				throw new FireCrawlException("Empty response body");
			}

			return objectMapper.readValue(responseBody.string(), TokenUsageResponse.class);
		}
		catch (IOException e) {
			throw new FireCrawlException("Failed to get token usage", e);
		}
	}

	/**
	 * Get credit usage for the team
	 * @return CreditUsageResponse containing remaining credits
	 * @throws FireCrawlException if API call fails
	 */
	public CreditUsageResponse getCreditUsage() throws FireCrawlException {
		Request request = new Request.Builder().url(FIRE_CRAWL_BASE_URL + "/team/credit-usage")
			.addHeader("Authorization", "Bearer " + apiKey)
			.get()
			.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new FireCrawlException("API request failed with code: " + response.code());
			}

			ResponseBody responseBody = response.body();
			if (responseBody == null) {
				throw new FireCrawlException("Empty response body");
			}

			return objectMapper.readValue(responseBody.string(), CreditUsageResponse.class);
		}
		catch (IOException e) {
			throw new FireCrawlException("Failed to get credit usage", e);
		}
	}

}
