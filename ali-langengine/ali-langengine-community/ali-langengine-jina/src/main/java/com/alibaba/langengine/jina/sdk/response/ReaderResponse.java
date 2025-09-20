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

import java.util.Map;

/**
 * Response object for reader API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReaderResponse {

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
	 * Response data
	 */
	@JsonProperty("data")
	private ReaderData data;

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

	public ReaderData getData() {
		return data;
	}

	public void setData(ReaderData data) {
		this.data = data;
	}

	/**
	 * Data container
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ReaderData {

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
		 * URL of the page
		 */
		@JsonProperty("url")
		private String url;

		/**
		 * Content of the page
		 */
		@JsonProperty("content")
		private String content;

		/**
		 * Images on the page
		 */
		@JsonProperty("images")
		private Map<String, String> images;

		/**
		 * Links on the page
		 */
		@JsonProperty("links")
		private Map<String, String> links;

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

		public Map<String, String> getImages() {
			return images;
		}

		public void setImages(Map<String, String> images) {
			this.images = images;
		}

		public Map<String, String> getLinks() {
			return links;
		}

		public void setLinks(Map<String, String> links) {
			this.links = links;
		}

		public Usage getUsage() {
			return usage;
		}

		public void setUsage(Usage usage) {
			this.usage = usage;
		}

	}

}
