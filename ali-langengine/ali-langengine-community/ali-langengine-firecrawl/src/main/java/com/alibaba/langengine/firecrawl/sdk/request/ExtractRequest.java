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

package com.alibaba.langengine.firecrawl.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtractRequest {

	/**
	 * The URLs to extract data from. URLs should be in glob format.
	 */
	@JsonProperty("urls")
	private List<String> urls;

	/**
	 * Prompt to guide the extraction process
	 */
	@JsonProperty("prompt")
	private String prompt;

	/**
	 * Schema to define the structure of the extracted data. Must conform to JSON Schema.
	 */
	@JsonProperty("schema")
	private Map<String, Object> schema;

	/**
	 * When true, the extraction will use web search to find additional data
	 */
	@JsonProperty("enableWebSearch")
	private Boolean enableWebSearch;

	/**
	 * When true, sitemap.xml files will be ignored during website scanning
	 */
	@JsonProperty("ignoreSitemap")
	private Boolean ignoreSitemap;

	/**
	 * When true, subdomains of the provided URLs will also be scanned
	 */
	@JsonProperty("includeSubdomains")
	private Boolean includeSubdomains;

	/**
	 * When true, the sources used to extract the data will be included in the response as
	 * [sources](file:///home/vlsmb/IdeaProjects/Agentic-ADK-Test/firecrawl/src/main/java/com/alibaba/langengine/firecrawl/sdk/request/SearchRequest.java#L23-L24)
	 * key
	 */
	@JsonProperty("showSources")
	private Boolean showSources;

	/**
	 * Options for scraping
	 */
	@JsonProperty("scrapeOptions")
	private ScrapeOptions scrapeOptions;

	/**
	 * If invalid URLs are specified in the urls array, they will be ignored.
	 */
	@JsonProperty("ignoreInvalidURLs")
	private Boolean ignoreInvalidURLs;

	// Getters and setters
	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public Map<String, Object> getSchema() {
		return schema;
	}

	public void setSchema(Map<String, Object> schema) {
		this.schema = schema;
	}

	public Boolean getEnableWebSearch() {
		return enableWebSearch;
	}

	public void setEnableWebSearch(Boolean enableWebSearch) {
		this.enableWebSearch = enableWebSearch;
	}

	public Boolean getIgnoreSitemap() {
		return ignoreSitemap;
	}

	public void setIgnoreSitemap(Boolean ignoreSitemap) {
		this.ignoreSitemap = ignoreSitemap;
	}

	public Boolean getIncludeSubdomains() {
		return includeSubdomains;
	}

	public void setIncludeSubdomains(Boolean includeSubdomains) {
		this.includeSubdomains = includeSubdomains;
	}

	public Boolean getShowSources() {
		return showSources;
	}

	public void setShowSources(Boolean showSources) {
		this.showSources = showSources;
	}

	public ScrapeOptions getScrapeOptions() {
		return scrapeOptions;
	}

	public void setScrapeOptions(ScrapeOptions scrapeOptions) {
		this.scrapeOptions = scrapeOptions;
	}

	public Boolean getIgnoreInvalidURLs() {
		return ignoreInvalidURLs;
	}

	public void setIgnoreInvalidURLs(Boolean ignoreInvalidURLs) {
		this.ignoreInvalidURLs = ignoreInvalidURLs;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ScrapeOptions {

		/**
		 * Output formats to include in the response
		 */
		@JsonProperty("formats")
		private List<Object> formats;

		/**
		 * Only return the main content of the page excluding headers, navs, footers, etc.
		 */
		@JsonProperty("onlyMainContent")
		private Boolean onlyMainContent;

		/**
		 * Tags to include in the output
		 */
		@JsonProperty("includeTags")
		private List<String> includeTags;

		/**
		 * Tags to exclude from the output
		 */
		@JsonProperty("excludeTags")
		private List<String> excludeTags;

		/**
		 * Returns a cached version of the page if it is younger than this age in
		 * milliseconds
		 */
		@JsonProperty("maxAge")
		private Integer maxAge;

		/**
		 * Headers to send with the request
		 */
		@JsonProperty("headers")
		private Map<String, String> headers;

		/**
		 * Specify a delay in milliseconds before fetching the content
		 */
		@JsonProperty("waitFor")
		private Integer waitFor;

		/**
		 * Set to true if you want to emulate scraping from a mobile device
		 */
		@JsonProperty("mobile")
		private Boolean mobile;

		/**
		 * Skip TLS certificate verification when making requests
		 */
		@JsonProperty("skipTlsVerification")
		private Boolean skipTlsVerification;

		/**
		 * Timeout in milliseconds for the request
		 */
		@JsonProperty("timeout")
		private Integer timeout;

		/**
		 * Controls how files are processed during scraping
		 */
		@JsonProperty("parsers")
		private List<String> parsers;

		/**
		 * Actions to perform on the page before grabbing the content
		 */
		@JsonProperty("actions")
		private List<Object> actions;

		/**
		 * Location settings for the request
		 */
		@JsonProperty("location")
		private Location location;

		/**
		 * Removes all base 64 images from the output
		 */
		@JsonProperty("removeBase64Images")
		private Boolean removeBase64Images;

		/**
		 * Enables ad-blocking and cookie popup blocking
		 */
		@JsonProperty("blockAds")
		private Boolean blockAds;

		/**
		 * Specifies the type of proxy to use
		 */
		@JsonProperty("proxy")
		private String proxy;

		/**
		 * If true, the page will be stored in the Firecrawl index and cache
		 */
		@JsonProperty("storeInCache")
		private Boolean storeInCache;

		// Getters and setters
		public List<Object> getFormats() {
			return formats;
		}

		public void setFormats(List<Object> formats) {
			this.formats = formats;
		}

		public Boolean getOnlyMainContent() {
			return onlyMainContent;
		}

		public void setOnlyMainContent(Boolean onlyMainContent) {
			this.onlyMainContent = onlyMainContent;
		}

		public List<String> getIncludeTags() {
			return includeTags;
		}

		public void setIncludeTags(List<String> includeTags) {
			this.includeTags = includeTags;
		}

		public List<String> getExcludeTags() {
			return excludeTags;
		}

		public void setExcludeTags(List<String> excludeTags) {
			this.excludeTags = excludeTags;
		}

		public Integer getMaxAge() {
			return maxAge;
		}

		public void setMaxAge(Integer maxAge) {
			this.maxAge = maxAge;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}

		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}

		public Integer getWaitFor() {
			return waitFor;
		}

		public void setWaitFor(Integer waitFor) {
			this.waitFor = waitFor;
		}

		public Boolean getMobile() {
			return mobile;
		}

		public void setMobile(Boolean mobile) {
			this.mobile = mobile;
		}

		public Boolean getSkipTlsVerification() {
			return skipTlsVerification;
		}

		public void setSkipTlsVerification(Boolean skipTlsVerification) {
			this.skipTlsVerification = skipTlsVerification;
		}

		public Integer getTimeout() {
			return timeout;
		}

		public void setTimeout(Integer timeout) {
			this.timeout = timeout;
		}

		public List<String> getParsers() {
			return parsers;
		}

		public void setParsers(List<String> parsers) {
			this.parsers = parsers;
		}

		public List<Object> getActions() {
			return actions;
		}

		public void setActions(List<Object> actions) {
			this.actions = actions;
		}

		public Location getLocation() {
			return location;
		}

		public void setLocation(Location location) {
			this.location = location;
		}

		public Boolean getRemoveBase64Images() {
			return removeBase64Images;
		}

		public void setRemoveBase64Images(Boolean removeBase64Images) {
			this.removeBase64Images = removeBase64Images;
		}

		public Boolean getBlockAds() {
			return blockAds;
		}

		public void setBlockAds(Boolean blockAds) {
			this.blockAds = blockAds;
		}

		public String getProxy() {
			return proxy;
		}

		public void setProxy(String proxy) {
			this.proxy = proxy;
		}

		public Boolean getStoreInCache() {
			return storeInCache;
		}

		public void setStoreInCache(Boolean storeInCache) {
			this.storeInCache = storeInCache;
		}

		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static class Location {

			/**
			 * ISO 3166-1 alpha-2 country code
			 */
			@JsonProperty("country")
			private String country;

			/**
			 * Preferred languages and locales for the request
			 */
			@JsonProperty("languages")
			private List<String> languages;

			// Getters and setters
			public String getCountry() {
				return country;
			}

			public void setCountry(String country) {
				this.country = country;
			}

			public List<String> getLanguages() {
				return languages;
			}

			public void setLanguages(List<String> languages) {
				this.languages = languages;
			}

		}

	}

}
