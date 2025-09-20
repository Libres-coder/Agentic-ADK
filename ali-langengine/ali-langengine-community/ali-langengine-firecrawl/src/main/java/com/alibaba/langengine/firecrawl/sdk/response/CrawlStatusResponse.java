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

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawlStatusResponse {

	@JsonProperty("status")
	private String status;

	@JsonProperty("total")
	private Integer total;

	@JsonProperty("completed")
	private Integer completed;

	@JsonProperty("creditsUsed")
	private Integer creditsUsed;

	@JsonProperty("expiresAt")
	private LocalDateTime expiresAt;

	@JsonProperty("next")
	private String next;

	@JsonProperty("data")
	private List<Data> data;

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

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public List<Data> getData() {
		return data;
	}

	public void setData(List<Data> data) {
		this.data = data;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Data {

		@JsonProperty("markdown")
		private String markdown;

		@JsonProperty("html")
		private String html;

		@JsonProperty("rawHtml")
		private String rawHtml;

		@JsonProperty("links")
		private List<String> links;

		@JsonProperty("screenshot")
		private String screenshot;

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

		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Metadata {

			@JsonProperty("title")
			private String title;

			@JsonProperty("description")
			private String description;

			@JsonProperty("language")
			private String language;

			@JsonProperty("sourceURL")
			private String sourceURL;

			@JsonProperty("statusCode")
			private Integer statusCode;

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
