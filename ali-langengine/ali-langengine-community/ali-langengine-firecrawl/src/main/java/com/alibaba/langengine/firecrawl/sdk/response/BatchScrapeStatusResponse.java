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

package com.alibaba.langengine.firecrawl.sdk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response object for batch scrape status
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchScrapeStatusResponse {

	/**
	 * The current status of the batch scrape. Can be `scraping`, `completed`, or
	 * `failed`.
	 */
	@JsonProperty("status")
	private String status;

	/**
	 * The total number of pages that were attempted to be scraped.
	 */
	@JsonProperty("total")
	private Integer total;

	/**
	 * The number of pages that have been successfully scraped.
	 */
	@JsonProperty("completed")
	private Integer completed;

	/**
	 * The number of credits used for the batch scrape.
	 */
	@JsonProperty("creditsUsed")
	private Integer creditsUsed;

	/**
	 * The date and time when the batch scrape will expire.
	 */
	@JsonProperty("expiresAt")
	private String expiresAt;

	/**
	 * The URL to retrieve the next 10MB of data. Returned if the batch scrape is not
	 * completed or if the response is larger than 10MB.
	 */
	@JsonProperty("next")
	private String next;

	/**
	 * The data of the batch scrape.
	 */
	@JsonProperty("data")
	private List<DataItem> data;

	// Getters and setters
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getCompleted() {
		return completed;
	}

	public void setCompleted(Integer completed) {
		this.completed = completed;
	}

	public Integer getCreditsUsed() {
		return creditsUsed;
	}

	public void setCreditsUsed(Integer creditsUsed) {
		this.creditsUsed = creditsUsed;
	}

	public String getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(String expiresAt) {
		this.expiresAt = expiresAt;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public List<DataItem> getData() {
		return data;
	}

	public void setData(List<DataItem> data) {
		this.data = data;
	}

	/**
	 * Data item containing scraped content
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DataItem {

		/**
		 * Markdown version of the content
		 */
		@JsonProperty("markdown")
		private String markdown;

		/**
		 * HTML version of the content on page if `includeHtml` is true
		 */
		@JsonProperty("html")
		private String html;

		/**
		 * Raw HTML content of the page if `includeRawHtml` is true
		 */
		@JsonProperty("rawHtml")
		private String rawHtml;

		/**
		 * List of links on the page if `includeLinks` is true
		 */
		@JsonProperty("links")
		private List<String> links;

		/**
		 * Screenshot of the page if `includeScreenshot` is true
		 */
		@JsonProperty("screenshot")
		private String screenshot;

		/**
		 * Metadata information about the page
		 */
		@JsonProperty("metadata")
		private Metadata metadata;

		// Getters and setters
		public String getMarkdown() {
			return markdown;
		}

		public void setMarkdown(String markdown) {
			this.markdown = markdown;
		}

		public String getHtml() {
			return html;
		}

		public void setHtml(String html) {
			this.html = html;
		}

		public String getRawHtml() {
			return rawHtml;
		}

		public void setRawHtml(String rawHtml) {
			this.rawHtml = rawHtml;
		}

		public List<String> getLinks() {
			return links;
		}

		public void setLinks(List<String> links) {
			this.links = links;
		}

		public String getScreenshot() {
			return screenshot;
		}

		public void setScreenshot(String screenshot) {
			this.screenshot = screenshot;
		}

		public Metadata getMetadata() {
			return metadata;
		}

		public void setMetadata(Metadata metadata) {
			this.metadata = metadata;
		}

		/**
		 * Metadata information for a scraped page
		 */
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Metadata {

			/**
			 * Title of the page
			 */
			@JsonProperty("title")
			private String title;

			/**
			 * Description of the page
			 */
			@JsonProperty("description")
			private String description;

			/**
			 * Language of the page content
			 */
			@JsonProperty("language")
			private String language;

			/**
			 * Original URL of the page
			 */
			@JsonProperty("sourceURL")
			private String sourceURL;

			/**
			 * HTTP status code of the page
			 */
			@JsonProperty("statusCode")
			private Integer statusCode;

			/**
			 * Error message if scraping failed for this page
			 */
			@JsonProperty("error")
			private String error;

			// Getters and setters
			public String getTitle() {
				return title;
			}

			public void setTitle(String title) {
				this.title = title;
			}

			public String getDescription() {
				return description;
			}

			public void setDescription(String description) {
				this.description = description;
			}

			public String getLanguage() {
				return language;
			}

			public void setLanguage(String language) {
				this.language = language;
			}

			public String getSourceURL() {
				return sourceURL;
			}

			public void setSourceURL(String sourceURL) {
				this.sourceURL = sourceURL;
			}

			public Integer getStatusCode() {
				return statusCode;
			}

			public void setStatusCode(Integer statusCode) {
				this.statusCode = statusCode;
			}

			public String getError() {
				return error;
			}

			public void setError(String error) {
				this.error = error;
			}

		}

	}

}
