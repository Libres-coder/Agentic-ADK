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
 * Response object containing errors from batch scrape
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchScrapeErrorsResponse {

	/**
	 * Errored scrape jobs and error details
	 */
	@JsonProperty("errors")
	private List<ErrorItem> errors;

	/**
	 * List of URLs that were attempted in scraping but were blocked by robots.txt
	 */
	@JsonProperty("robotsBlocked")
	private List<String> robotsBlocked;

	// Getters and setters
	public List<ErrorItem> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorItem> errors) {
		this.errors = errors;
	}

	public List<String> getRobotsBlocked() {
		return robotsBlocked;
	}

	public void setRobotsBlocked(List<String> robotsBlocked) {
		this.robotsBlocked = robotsBlocked;
	}

	/**
	 * Details of a specific error in scraping
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ErrorItem {

		/**
		 * ID of the failed scrape job
		 */
		@JsonProperty("id")
		private String id;

		/**
		 * ISO timestamp of failure
		 */
		@JsonProperty("timestamp")
		private String timestamp;

		/**
		 * Scraped URL that failed
		 */
		@JsonProperty("url")
		private String url;

		/**
		 * Error message explaining the failure
		 */
		@JsonProperty("error")
		private String error;

		// Getters and setters
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

	}

}
