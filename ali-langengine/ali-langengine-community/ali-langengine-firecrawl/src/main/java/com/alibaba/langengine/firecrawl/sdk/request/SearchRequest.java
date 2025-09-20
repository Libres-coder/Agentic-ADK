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

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest {

	/**
	 * The search query
	 */
	@JsonProperty("query")
	private String query;

	/**
	 * Maximum number of results to return (1-100, default: 5)
	 */
	@JsonProperty("limit")
	private Integer limit;

	/**
	 * Sources to search (web, images, news)
	 */
	@JsonProperty("sources")
	private Object[] sources;

	/**
	 * Categories to filter results by (github, research)
	 */
	@JsonProperty("categories")
	private Object[] categories;

	/**
	 * Time-based search parameter
	 */
	@JsonProperty("tbs")
	private String tbs;

	/**
	 * Location parameter for search results
	 */
	@JsonProperty("location")
	private String location;

	/**
	 * Timeout in milliseconds (default: 60000)
	 */
	@JsonProperty("timeout")
	private Integer timeout;

	/**
	 * Excludes invalid URLs for other Firecrawl endpoints (default: false)
	 */
	@JsonProperty("ignoreInvalidURLs")
	private Boolean ignoreInvalidURLs;

	/**
	 * Options for scraping search results
	 */
	@JsonProperty("scrapeOptions")
	private ScrapeOptions scrapeOptions;

	// Getters and setters
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Object[] getSources() {
		return sources;
	}

	public void setSources(Object[] sources) {
		this.sources = sources;
	}

	public Object[] getCategories() {
		return categories;
	}

	public void setCategories(Object[] categories) {
		this.categories = categories;
	}

	public String getTbs() {
		return tbs;
	}

	public void setTbs(String tbs) {
		this.tbs = tbs;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Boolean getIgnoreInvalidURLs() {
		return ignoreInvalidURLs;
	}

	public void setIgnoreInvalidURLs(Boolean ignoreInvalidURLs) {
		this.ignoreInvalidURLs = ignoreInvalidURLs;
	}

	public ScrapeOptions getScrapeOptions() {
		return scrapeOptions;
	}

	public void setScrapeOptions(ScrapeOptions scrapeOptions) {
		this.scrapeOptions = scrapeOptions;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ScrapeOptions {

		/**
		 * Output formats to include in the response
		 */
		@JsonProperty("formats")
		private Object[] formats;

		/**
		 * Only return main content excluding headers, navs, footers (default: true)
		 */
		@JsonProperty("onlyMainContent")
		private Boolean onlyMainContent;

		/**
		 * Tags to include in the output
		 */
		@JsonProperty("includeTags")
		private String[] includeTags;

		/**
		 * Tags to exclude from the output
		 */
		@JsonProperty("excludeTags")
		private String[] excludeTags;

		/**
		 * Returns cached version if younger than this age in milliseconds (default:
		 * 172800000)
		 */
		@JsonProperty("maxAge")
		private Integer maxAge;

		/**
		 * Headers to send with the request
		 */
		@JsonProperty("headers")
		private Object headers;

		/**
		 * Delay in milliseconds before fetching content (default: 0)
		 */
		@JsonProperty("waitFor")
		private Integer waitFor;

		/**
		 * Emulate scraping from a mobile device (default: false)
		 */
		@JsonProperty("mobile")
		private Boolean mobile;

		/**
		 * Skip TLS certificate verification (default: true)
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
		private String[] parsers;

		/**
		 * Actions to perform on the page before grabbing content
		 */
		@JsonProperty("actions")
		private Object[] actions;

		/**
		 * Location settings for the request
		 */
		@JsonProperty("location")
		private Location location;

		/**
		 * Removes all base64 images from output (default: true)
		 */
		@JsonProperty("removeBase64Images")
		private Boolean removeBase64Images;

		/**
		 * Enables ad-blocking and cookie popup blocking (default: true)
		 */
		@JsonProperty("blockAds")
		private Boolean blockAds;

		/**
		 * Specifies the type of proxy to use (basic, stealth, auto)
		 */
		@JsonProperty("proxy")
		private String proxy;

		/**
		 * Store page in Firecrawl index and cache (default: true)
		 */
		@JsonProperty("storeInCache")
		private Boolean storeInCache;

		// Getters and setters
		public Object[] getFormats() {
			return formats;
		}

		public void setFormats(Object[] formats) {
			this.formats = formats;
		}

		public Boolean getOnlyMainContent() {
			return onlyMainContent;
		}

		public void setOnlyMainContent(Boolean onlyMainContent) {
			this.onlyMainContent = onlyMainContent;
		}

		public String[] getIncludeTags() {
			return includeTags;
		}

		public void setIncludeTags(String[] includeTags) {
			this.includeTags = includeTags;
		}

		public String[] getExcludeTags() {
			return excludeTags;
		}

		public void setExcludeTags(String[] excludeTags) {
			this.excludeTags = excludeTags;
		}

		public Integer getMaxAge() {
			return maxAge;
		}

		public void setMaxAge(Integer maxAge) {
			this.maxAge = maxAge;
		}

		public Object getHeaders() {
			return headers;
		}

		public void setHeaders(Object headers) {
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

		public String[] getParsers() {
			return parsers;
		}

		public void setParsers(String[] parsers) {
			this.parsers = parsers;
		}

		public Object[] getActions() {
			return actions;
		}

		public void setActions(Object[] actions) {
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
			 * ISO 3166-1 alpha-2 country code (default: US)
			 */
			@JsonProperty("country")
			private String country;

			/**
			 * Preferred languages and locales for the request
			 */
			@JsonProperty("languages")
			private String[] languages;

			// Getters and setters
			public String getCountry() {
				return country;
			}

			public void setCountry(String country) {
				this.country = country;
			}

			public String[] getLanguages() {
				return languages;
			}

			public void setLanguages(String[] languages) {
				this.languages = languages;
			}

		}

	}

}
