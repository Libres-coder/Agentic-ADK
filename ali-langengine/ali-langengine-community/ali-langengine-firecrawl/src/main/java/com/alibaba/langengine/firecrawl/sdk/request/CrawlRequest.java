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
public class CrawlRequest {

	@JsonProperty("url")
	private String url;

	@JsonProperty("prompt")
	private String prompt;

	@JsonProperty("excludePaths")
	private List<String> excludePaths;

	@JsonProperty("includePaths")
	private List<String> includePaths;

	@JsonProperty("maxDiscoveryDepth")
	private Integer maxDiscoveryDepth;

	@JsonProperty("sitemap")
	private String sitemap;

	@JsonProperty("ignoreQueryParameters")
	private Boolean ignoreQueryParameters;

	@JsonProperty("limit")
	private Integer limit;

	@JsonProperty("crawlEntireDomain")
	private Boolean crawlEntireDomain;

	@JsonProperty("allowExternalLinks")
	private Boolean allowExternalLinks;

	@JsonProperty("allowSubdomains")
	private Boolean allowSubdomains;

	@JsonProperty("delay")
	private Number delay;

	@JsonProperty("maxConcurrency")
	private Integer maxConcurrency;

	@JsonProperty("webhook")
	private Webhook webhook;

	@JsonProperty("scrapeOptions")
	private ScrapeOptions scrapeOptions;

	@JsonProperty("zeroDataRetention")
	private Boolean zeroDataRetention;

	// Getters and setters
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public List<String> getExcludePaths() {
		return excludePaths;
	}

	public void setExcludePaths(List<String> excludePaths) {
		this.excludePaths = excludePaths;
	}

	public List<String> getIncludePaths() {
		return includePaths;
	}

	public void setIncludePaths(List<String> includePaths) {
		this.includePaths = includePaths;
	}

	public Integer getMaxDiscoveryDepth() {
		return maxDiscoveryDepth;
	}

	public void setMaxDiscoveryDepth(Integer maxDiscoveryDepth) {
		this.maxDiscoveryDepth = maxDiscoveryDepth;
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

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
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

	public Number getDelay() {
		return delay;
	}

	public void setDelay(Number delay) {
		this.delay = delay;
	}

	public Integer getMaxConcurrency() {
		return maxConcurrency;
	}

	public void setMaxConcurrency(Integer maxConcurrency) {
		this.maxConcurrency = maxConcurrency;
	}

	public Webhook getWebhook() {
		return webhook;
	}

	public void setWebhook(Webhook webhook) {
		this.webhook = webhook;
	}

	public ScrapeOptions getScrapeOptions() {
		return scrapeOptions;
	}

	public void setScrapeOptions(ScrapeOptions scrapeOptions) {
		this.scrapeOptions = scrapeOptions;
	}

	public Boolean getZeroDataRetention() {
		return zeroDataRetention;
	}

	public void setZeroDataRetention(Boolean zeroDataRetention) {
		this.zeroDataRetention = zeroDataRetention;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Webhook {

		@JsonProperty("url")
		private String url;

		@JsonProperty("headers")
		private Map<String, String> headers;

		@JsonProperty("metadata")
		private Object metadata;

		@JsonProperty("events")
		private List<String> events;

		// Getters and setters
		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}

		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}

		public Object getMetadata() {
			return metadata;
		}

		public void setMetadata(Object metadata) {
			this.metadata = metadata;
		}

		public List<String> getEvents() {
			return events;
		}

		public void setEvents(List<String> events) {
			this.events = events;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ScrapeOptions {

		@JsonProperty("formats")
		private List<Object> formats;

		@JsonProperty("onlyMainContent")
		private Boolean onlyMainContent;

		@JsonProperty("includeTags")
		private List<String> includeTags;

		@JsonProperty("excludeTags")
		private List<String> excludeTags;

		@JsonProperty("maxAge")
		private Integer maxAge;

		@JsonProperty("headers")
		private Object headers;

		@JsonProperty("waitFor")
		private Integer waitFor;

		@JsonProperty("mobile")
		private Boolean mobile;

		@JsonProperty("skipTlsVerification")
		private Boolean skipTlsVerification;

		@JsonProperty("timeout")
		private Integer timeout;

		@JsonProperty("parsers")
		private List<String> parsers;

		@JsonProperty("actions")
		private List<Object> actions;

		@JsonProperty("location")
		private Location location;

		@JsonProperty("removeBase64Images")
		private Boolean removeBase64Images;

		@JsonProperty("blockAds")
		private Boolean blockAds;

		@JsonProperty("proxy")
		private String proxy;

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

			@JsonProperty("country")
			private String country;

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
