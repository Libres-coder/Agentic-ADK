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

@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawlParamsPreviewResponse {

	@JsonProperty("success")
	private Boolean success;

	@JsonProperty("data")
	private Data data;

	// Getters and setters
	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Data {

		@JsonProperty("url")
		private String url;

		@JsonProperty("includePaths")
		private List<String> includePaths;

		@JsonProperty("excludePaths")
		private List<String> excludePaths;

		@JsonProperty("maxDepth")
		private Integer maxDepth;

		@JsonProperty("maxDiscoveryDepth")
		private Integer maxDiscoveryDepth;

		@JsonProperty("crawlEntireDomain")
		private Boolean crawlEntireDomain;

		@JsonProperty("allowExternalLinks")
		private Boolean allowExternalLinks;

		@JsonProperty("allowSubdomains")
		private Boolean allowSubdomains;

		@JsonProperty("sitemap")
		private String sitemap;

		@JsonProperty("ignoreQueryParameters")
		private Boolean ignoreQueryParameters;

		@JsonProperty("deduplicateSimilarURLs")
		private Boolean deduplicateSimilarURLs;

		@JsonProperty("delay")
		private Number delay;

		@JsonProperty("limit")
		private Integer limit;

		// Getters and setters
		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public List<String> getIncludePaths() {
			return includePaths;
		}

		public void setIncludePaths(List<String> includePaths) {
			this.includePaths = includePaths;
		}

		public List<String> getExcludePaths() {
			return excludePaths;
		}

		public void setExcludePaths(List<String> excludePaths) {
			this.excludePaths = excludePaths;
		}

		public Integer getMaxDepth() {
			return maxDepth;
		}

		public void setMaxDepth(Integer maxDepth) {
			this.maxDepth = maxDepth;
		}

		public Integer getMaxDiscoveryDepth() {
			return maxDiscoveryDepth;
		}

		public void setMaxDiscoveryDepth(Integer maxDiscoveryDepth) {
			this.maxDiscoveryDepth = maxDiscoveryDepth;
		}

		public Boolean getCrawlEntireDomain() {
			return crawlEntireDomain;
		}

		public void setCrawlEntireDomain(Boolean crawlEntireDomain) {
			this.crawlEntireDomain = crawlEntireDomain;
		}

		public Boolean getAllowExternalLinks() {
			return allowExternalLinks;
		}

		public void setAllowExternalLinks(Boolean allowExternalLinks) {
			this.allowExternalLinks = allowExternalLinks;
		}

		public Boolean getAllowSubdomains() {
			return allowSubdomains;
		}

		public void setAllowSubdomains(Boolean allowSubdomains) {
			this.allowSubdomains = allowSubdomains;
		}

		public String getSitemap() {
			return sitemap;
		}

		public void setSitemap(String sitemap) {
			this.sitemap = sitemap;
		}

		public Boolean getIgnoreQueryParameters() {
			return ignoreQueryParameters;
		}

		public void setIgnoreQueryParameters(Boolean ignoreQueryParameters) {
			this.ignoreQueryParameters = ignoreQueryParameters;
		}

		public Boolean getDeduplicateSimilarURLs() {
			return deduplicateSimilarURLs;
		}

		public void setDeduplicateSimilarURLs(Boolean deduplicateSimilarURLs) {
			this.deduplicateSimilarURLs = deduplicateSimilarURLs;
		}

		public Number getDelay() {
			return delay;
		}

		public void setDelay(Number delay) {
			this.delay = delay;
		}

		public Integer getLimit() {
			return limit;
		}

		public void setLimit(Integer limit) {
			this.limit = limit;
		}

	}

}
