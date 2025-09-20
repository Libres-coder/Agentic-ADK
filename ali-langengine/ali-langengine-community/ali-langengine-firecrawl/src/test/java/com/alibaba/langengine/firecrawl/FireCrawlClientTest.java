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

package com.alibaba.langengine.firecrawl;

import com.alibaba.langengine.firecrawl.sdk.FireCrawlClient;
import com.alibaba.langengine.firecrawl.sdk.FireCrawlException;
import com.alibaba.langengine.firecrawl.sdk.request.BatchScrapeRequest;
import com.alibaba.langengine.firecrawl.sdk.request.CrawlParamsPreviewRequest;
import com.alibaba.langengine.firecrawl.sdk.request.ExtractRequest;
import com.alibaba.langengine.firecrawl.sdk.request.MapRequest;
import com.alibaba.langengine.firecrawl.sdk.request.ScrapeRequest;
import com.alibaba.langengine.firecrawl.sdk.request.SearchRequest;
import com.alibaba.langengine.firecrawl.sdk.response.BatchScrapeResponse;
import com.alibaba.langengine.firecrawl.sdk.response.CrawlParamsPreviewResponse;
import com.alibaba.langengine.firecrawl.sdk.response.CreditUsageResponse;
import com.alibaba.langengine.firecrawl.sdk.response.ExtractResponse;
import com.alibaba.langengine.firecrawl.sdk.response.ExtractStatusResponse;
import com.alibaba.langengine.firecrawl.sdk.response.GetActiveCrawlsResponse;
import com.alibaba.langengine.firecrawl.sdk.response.MapResponse;
import com.alibaba.langengine.firecrawl.sdk.response.ScrapeResponse;
import com.alibaba.langengine.firecrawl.sdk.response.SearchResponse;
import com.alibaba.langengine.firecrawl.sdk.response.TokenUsageResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@EnabledIfEnvironmentVariable(named = "FIRECRAWL_API_KEY", matches = ".*")
public class FireCrawlClientTest {

	private FireCrawlClient client;

	private static final String TEST_URL = "https://example.com";

	@BeforeEach
	public void setUp() {
		String apiKey = System.getenv("FIRECRAWL_API_KEY");
		if (apiKey == null || apiKey.isEmpty()) {
			// Disable tests if API key is not set
			throw new IllegalStateException("FIRECRAWL_API_KEY environment variable must be set to run tests");
		}
		client = new FireCrawlClient(apiKey);
	}

	@Test
	public void testScrapeWithDefaultOptions() throws FireCrawlException {
		ScrapeRequest request = new ScrapeRequest();
		request.setUrl(TEST_URL);

		ScrapeResponse response = client.scrape(request);

		Assertions.assertNotNull(response);
		Assertions.assertTrue(response.getSuccess());
		Assertions.assertNotNull(response.getData());
		Assertions.assertNotNull(response.getData().getMarkdown());
		Assertions.assertFalse(response.getData().getMarkdown().isEmpty());
		Assertions.assertNotNull(response.getData().getMetadata());
		Assertions.assertNotNull(response.getData().getMetadata().getTitle());
	}

	@Test
	public void testScrapeWithRawHtmlFormat() throws FireCrawlException {
		ScrapeRequest request = new ScrapeRequest();
		request.setUrl(TEST_URL);

		// Set format to rawHtml
		List<Object> formats = new ArrayList<>();
		formats.add("rawHtml");
		request.setFormats(formats);

		ScrapeResponse response = client.scrape(request);

		Assertions.assertNotNull(response);
		Assertions.assertTrue(response.getSuccess());
		Assertions.assertNotNull(response.getData());
		Assertions.assertNotNull(response.getData().getRawHtml());
		Assertions.assertFalse(response.getData().getRawHtml().isEmpty());
	}

	@Test
	public void testScrapeWithLinksFormat() throws FireCrawlException {
		ScrapeRequest request = new ScrapeRequest();
		request.setUrl(TEST_URL);

		// Set format to links
		List<Object> formats = new ArrayList<>();
		formats.add("links");
		request.setFormats(formats);

		ScrapeResponse response = client.scrape(request);

		Assertions.assertNotNull(response);
		Assertions.assertTrue(response.getSuccess());
		Assertions.assertNotNull(response.getData());
		Assertions.assertNotNull(response.getData().getLinks());
	}

	@Test
	public void testScrapeWithMultipleFormats() throws FireCrawlException {
		ScrapeRequest request = new ScrapeRequest();
		request.setUrl(TEST_URL);

		// Set multiple formats
		List<Object> formats = new ArrayList<>();
		formats.add("markdown");
		formats.add("html");
		formats.add("links");
		request.setFormats(formats);

		ScrapeResponse response = client.scrape(request);

		Assertions.assertNotNull(response);
		Assertions.assertTrue(response.getSuccess());
		Assertions.assertNotNull(response.getData());
		// Check that all formats are returned
		Assertions.assertNotNull(response.getData().getMarkdown());
		Assertions.assertFalse(response.getData().getMarkdown().isEmpty());
		Assertions.assertNotNull(response.getData().getHtml());
		Assertions.assertFalse(response.getData().getHtml().isEmpty());
		Assertions.assertNotNull(response.getData().getLinks());
	}

	@Test
	public void testBatchScrapeWithFormats() throws FireCrawlException {

		BatchScrapeRequest request = new BatchScrapeRequest();
		request.setUrls(Collections.singletonList("https://example.com"));
		request.setFormats(Arrays.asList("markdown", "html"));
		request.setIgnoreInvalidURLs(true);

		BatchScrapeResponse response = client.batchScrape(request);

		Assertions.assertNotNull(response, "Response should not be null");
		Assertions.assertTrue(response.getSuccess(), "Response should be successful");
		Assertions.assertNotNull(response.getId(), "Response ID should not be null");
	}

	@Test
	public void testGetBatchScrapeStatusWithInvalidId() {
		try {
			// Test with an invalid ID to check error handling
			client.getBatchScrapeStatus("invalid-id");
			Assertions.fail("Expected FireCrawlException to be thrown");
		}
		catch (FireCrawlException e) {
			// Expected exception
			Assertions.assertNotNull(e);
		}
	}

	@Test
	public void testCancelBatchScrapeWithInvalidId() {
		try {
			// Test with an invalid ID to check error handling
			client.cancelBatchScrape("invalid-id");
			Assertions.fail("Expected FireCrawlException to be thrown");
		}
		catch (FireCrawlException e) {
			// Expected exception
			Assertions.assertNotNull(e);
		}
	}

	@Test
	public void testSearchWeb() throws FireCrawlException {
		SearchRequest request = new SearchRequest();
		request.setQuery("firecrawl");
		request.setLimit(5);

		SearchResponse response = client.search(request);

		Assertions.assertNotNull(response, "Response should not be null");
		Assertions.assertTrue(response.getSuccess(), "Request should be successful");
		Assertions.assertNotNull(response.getData(), "Data should not be null");
		Assertions.assertNotNull(response.getData().getWeb(), "Web results should not be null");
		Assertions.assertTrue(response.getData().getWeb().length > 0, "Should have at least one web result");

		SearchResponse.WebResult firstResult = response.getData().getWeb()[0];
		Assertions.assertNotNull(firstResult.getTitle(), "First result title should not be null");
		Assertions.assertNotNull(firstResult.getUrl(), "First result URL should not be null");
		Assertions.assertNotNull(firstResult.getDescription(), "First result description should not be null");
	}

	@Test
	public void testSearchWithScrapeOptions() throws FireCrawlException {
		SearchRequest request = new SearchRequest();
		request.setQuery("firecrawl java sdk");
		request.setLimit(3);

		SearchRequest.ScrapeOptions scrapeOptions = new SearchRequest.ScrapeOptions();
		scrapeOptions.setOnlyMainContent(true);
		request.setScrapeOptions(scrapeOptions);

		SearchResponse response = client.search(request);

		Assertions.assertNotNull(response, "Response should not be null");
		Assertions.assertTrue(response.getSuccess(), "Request should be successful");
		Assertions.assertNotNull(response.getData(), "Data should not be null");
		Assertions.assertNotNull(response.getData().getWeb(), "Web results should not be null");
	}

	@Test
	public void testMap() throws FireCrawlException {
		MapRequest request = new MapRequest();
		request.setUrl("https://firecrawl.dev");
		request.setLimit(10);

		MapResponse response = client.map(request);

		Assertions.assertNotNull(response, "Response should not be null");
		Assertions.assertTrue(response.getSuccess(), "Request should be successful");
		Assertions.assertNotNull(response.getLinks(), "Links should not be null");
		Assertions.assertFalse(response.getLinks().isEmpty(), "Links should not be empty");

		MapResponse.Link firstLink = response.getLinks().get(0);
		Assertions.assertNotNull(firstLink.getUrl(), "URL should not be null");
	}

	@Test
	public void testGetActiveCrawls() throws FireCrawlException {
		GetActiveCrawlsResponse response = client.getActiveCrawls();
		Assertions.assertNotNull(response);
		Assertions.assertNotNull(response.getSuccess());
	}

	@Test
	public void testCrawlParamsPreview() throws FireCrawlException {
		CrawlParamsPreviewRequest request = new CrawlParamsPreviewRequest();
		request.setUrl("https://example.com");
		request.setPrompt("Crawl all product pages");

		CrawlParamsPreviewResponse response = client.crawlParamsPreview(request);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(true, response.getSuccess());
		Assertions.assertNotNull(response.getData());
	}

	@Test
	public void testExtract() throws FireCrawlException {
		ExtractRequest request = new ExtractRequest();
		request.setUrls(Arrays.asList("https://firecrawl.dev"));
		request.setPrompt("Extract the title and description of the website");

		ExtractResponse response = client.extract(request);
		Assertions.assertNotNull(response);
		Assertions.assertTrue(response.getSuccess());
		Assertions.assertNotNull(response.getId());
		System.out.println("Extract job ID: " + response.getId());
	}

	@Test
	@Disabled("Wait for a long time")
	public void testGetExtractStatus() throws FireCrawlException, InterruptedException {
		// First create an extract job
		ExtractRequest request = new ExtractRequest();
		request.setUrls(Arrays.asList("https://firecrawl.dev"));
		request.setPrompt("Extract the title and description of the website");

		ExtractResponse response = client.extract(request);
		Assertions.assertNotNull(response.getId());

		// Wait a bit for processing
		Thread.sleep(10000);

		// Check the status
		ExtractStatusResponse statusResponse = client.getExtractStatus(response.getId());
		Assertions.assertNotNull(statusResponse);
		Assertions.assertTrue(statusResponse.getSuccess());
		Assertions.assertNotNull(statusResponse.getStatus());
		System.out.println("Extract job status: " + statusResponse.getStatus());
	}

	@Test
	public void testGetTokenUsage() {
		TokenUsageResponse response = client.getTokenUsage();
		Assertions.assertNotNull(response);
		// If API key is valid, success should be true
		if (response.getSuccess() != null && response.getSuccess()) {
			Assertions.assertNotNull(response.getData());
			Assertions.assertNotNull(response.getData().getRemainingTokens());
			Assertions.assertTrue(response.getData().getRemainingTokens() >= 0);
		}
	}

	@Test
	public void testGetCreditUsage() {
		CreditUsageResponse response = client.getCreditUsage();
		Assertions.assertNotNull(response);
		// If API key is valid, success should be true
		if (response.getSuccess() != null && response.getSuccess()) {
			Assertions.assertNotNull(response.getData());
			Assertions.assertNotNull(response.getData().getRemainingCredits());
			Assertions.assertTrue(response.getData().getRemainingCredits() >= 0);
		}
	}

}
