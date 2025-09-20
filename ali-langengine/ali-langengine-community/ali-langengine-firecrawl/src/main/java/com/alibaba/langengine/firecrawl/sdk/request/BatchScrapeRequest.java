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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchScrapeRequest {

	@JsonProperty("urls")
	private List<String> urls;

	@JsonProperty("webhook")
	private Webhook webhook;

	@JsonProperty("maxConcurrency")
	private Integer maxConcurrency;

	@JsonProperty("ignoreInvalidURLs")
	private Boolean ignoreInvalidURLs;

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
	private Map<String, String> headers;

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
	private List<Action> actions;

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

	@JsonProperty("zeroDataRetention")
	private Boolean zeroDataRetention;

	// Getters and setters
	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public Webhook getWebhook() {
		return webhook;
	}

	public void setWebhook(Webhook webhook) {
		this.webhook = webhook;
	}

	public Integer getMaxConcurrency() {
		return maxConcurrency;
	}

	public void setMaxConcurrency(Integer maxConcurrency) {
		this.maxConcurrency = maxConcurrency;
	}

	public Boolean getIgnoreInvalidURLs() {
		return ignoreInvalidURLs;
	}

	public void setIgnoreInvalidURLs(Boolean ignoreInvalidURLs) {
		this.ignoreInvalidURLs = ignoreInvalidURLs;
	}

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

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
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
		private Map<String, Object> metadata;

		@JsonProperty("events")
		private List<String> events;

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

		public Map<String, Object> getMetadata() {
			return metadata;
		}

		public void setMetadata(Map<String, Object> metadata) {
			this.metadata = metadata;
		}

		public List<String> getEvents() {
			return events;
		}

		public void setEvents(List<String> events) {
			this.events = events;
		}

	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
	@JsonSubTypes({ @JsonSubTypes.Type(value = WaitAction.class, name = "wait"),
			@JsonSubTypes.Type(value = ScreenshotAction.class, name = "screenshot"),
			@JsonSubTypes.Type(value = ClickAction.class, name = "click"),
			@JsonSubTypes.Type(value = WriteAction.class, name = "write"),
			@JsonSubTypes.Type(value = PressAction.class, name = "press"),
			@JsonSubTypes.Type(value = ScrollAction.class, name = "scroll"),
			@JsonSubTypes.Type(value = ScrapeAction.class, name = "scrape"),
			@JsonSubTypes.Type(value = ExecuteJavascriptAction.class, name = "executeJavascript"),
			@JsonSubTypes.Type(value = PdfAction.class, name = "pdf") })
	public static abstract class Action {

		@JsonProperty("type")
		private String type;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class WaitAction extends Action {

		@JsonProperty("milliseconds")
		private Integer milliseconds;

		@JsonProperty("selector")
		private String selector;

		public Integer getMilliseconds() {
			return milliseconds;
		}

		public void setMilliseconds(Integer milliseconds) {
			this.milliseconds = milliseconds;
		}

		public String getSelector() {
			return selector;
		}

		public void setSelector(String selector) {
			this.selector = selector;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ScreenshotAction extends Action {

		@JsonProperty("fullPage")
		private Boolean fullPage;

		@JsonProperty("quality")
		private Integer quality;

		@JsonProperty("viewport")
		private Viewport viewport;

		public Boolean getFullPage() {
			return fullPage;
		}

		public void setFullPage(Boolean fullPage) {
			this.fullPage = fullPage;
		}

		public Integer getQuality() {
			return quality;
		}

		public void setQuality(Integer quality) {
			this.quality = quality;
		}

		public Viewport getViewport() {
			return viewport;
		}

		public void setViewport(Viewport viewport) {
			this.viewport = viewport;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ClickAction extends Action {

		@JsonProperty("selector")
		private String selector;

		@JsonProperty("all")
		private Boolean all;

		public String getSelector() {
			return selector;
		}

		public void setSelector(String selector) {
			this.selector = selector;
		}

		public Boolean getAll() {
			return all;
		}

		public void setAll(Boolean all) {
			this.all = all;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class WriteAction extends Action {

		@JsonProperty("text")
		private String text;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class PressAction extends Action {

		@JsonProperty("key")
		private String key;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ScrollAction extends Action {

		@JsonProperty("direction")
		private String direction;

		@JsonProperty("selector")
		private String selector;

		public String getDirection() {
			return direction;
		}

		public void setDirection(String direction) {
			this.direction = direction;
		}

		public String getSelector() {
			return selector;
		}

		public void setSelector(String selector) {
			this.selector = selector;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ScrapeAction extends Action {

		// No additional fields

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ExecuteJavascriptAction extends Action {

		@JsonProperty("script")
		private String script;

		public String getScript() {
			return script;
		}

		public void setScript(String script) {
			this.script = script;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class PdfAction extends Action {

		@JsonProperty("format")
		private String format;

		@JsonProperty("landscape")
		private Boolean landscape;

		@JsonProperty("scale")
		private Double scale;

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public Boolean getLandscape() {
			return landscape;
		}

		public void setLandscape(Boolean landscape) {
			this.landscape = landscape;
		}

		public Double getScale() {
			return scale;
		}

		public void setScale(Double scale) {
			this.scale = scale;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Viewport {

		@JsonProperty("width")
		private Integer width;

		@JsonProperty("height")
		private Integer height;

		public Integer getWidth() {
			return width;
		}

		public void setWidth(Integer width) {
			this.width = width;
		}

		public Integer getHeight() {
			return height;
		}

		public void setHeight(Integer height) {
			this.height = height;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Location {

		@JsonProperty("country")
		private String country;

		@JsonProperty("languages")
		private List<String> languages;

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
