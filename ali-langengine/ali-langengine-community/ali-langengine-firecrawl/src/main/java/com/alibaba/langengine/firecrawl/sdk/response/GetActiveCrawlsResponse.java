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
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetActiveCrawlsResponse {

	@JsonProperty("success")
	private Boolean success;

	@JsonProperty("crawls")
	private List<Crawl> crawls;

	// Getters and setters
	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public List<Crawl> getCrawls() {
		return crawls;
	}

	public void setCrawls(List<Crawl> crawls) {
		this.crawls = crawls;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Crawl {

		@JsonProperty("id")
		private UUID id;

		@JsonProperty("teamId")
		private String teamId;

		@JsonProperty("url")
		private String url;

		@JsonProperty("options")
		private Options options;

		// Getters and setters
		public UUID getId() {
			return id;
		}

		public void setId(UUID id) {
			this.id = id;
		}

		public String getTeamId() {
			return teamId;
		}

		public void setTeamId(String teamId) {
			this.teamId = teamId;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Options getOptions() {
			return options;
		}

		public void setOptions(Options options) {
			this.options = options;
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Options {

			@JsonProperty("scrapeOptions")
			private ScrapeOptions scrapeOptions;

			// Getters and setters
			public ScrapeOptions getScrapeOptions() {
				return scrapeOptions;
			}

			public void setScrapeOptions(ScrapeOptions scrapeOptions) {
				this.scrapeOptions = scrapeOptions;
			}

		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
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

		@JsonIgnoreProperties(ignoreUnknown = true)
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
