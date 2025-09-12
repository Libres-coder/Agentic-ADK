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

package com.alibaba.langengine.jina.sdk.response;

import com.alibaba.langengine.jina.sdk.Usage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response object for search API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {

	/**
	 * Response code
	 */
	@JsonProperty("code")
	private Integer code;

	/**
	 * Status code
	 */
	@JsonProperty("status")
	private Integer status;

	/**
	 * Search results
	 */
	@JsonProperty("data")
	private List<SearchResult> data;

	// Getters and setters
	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public List<SearchResult> getData() {
		return data;
	}

	public void setData(List<SearchResult> data) {
		this.data = data;
	}

	/**
	 * Individual search result
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SearchResult {

		/**
		 * Title of the result
		 */
		@JsonProperty("title")
		private String title;

		/**
		 * Description of the result
		 */
		@JsonProperty("description")
		private String description;

		/**
		 * URL of the result
		 */
		@JsonProperty("url")
		private String url;

		/**
		 * Content of the result
		 */
		@JsonProperty("content")
		private String content;

		/**
		 * Usage information
		 */
		@JsonProperty("usage")
		private Usage usage;

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

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public Usage getUsage() {
			return usage;
		}

		public void setUsage(Usage usage) {
			this.usage = usage;
		}

	}

}
