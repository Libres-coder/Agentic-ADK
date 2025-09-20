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
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScrapeResponse {

	@JsonProperty("success")
	private Boolean success;

	@JsonProperty("data")
	private Data data;

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

		@JsonProperty("markdown")
		private String markdown;

		@JsonProperty("summary")
		private String summary;

		@JsonProperty("html")
		private String html;

		@JsonProperty("rawHtml")
		private String rawHtml;

		@JsonProperty("screenshot")
		private String screenshot;

		@JsonProperty("links")
		private List<String> links;

		@JsonProperty("actions")
		private Actions actions;

		@JsonProperty("metadata")
		private Metadata metadata;

		@JsonProperty("warning")
		private String warning;

		@JsonProperty("changeTracking")
		private ChangeTracking changeTracking;

		public String getMarkdown() {
			return markdown;
		}

		public void setMarkdown(String markdown) {
			this.markdown = markdown;
		}

		public String getSummary() {
			return summary;
		}

		public void setSummary(String summary) {
			this.summary = summary;
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

		public String getScreenshot() {
			return screenshot;
		}

		public void setScreenshot(String screenshot) {
			this.screenshot = screenshot;
		}

		public List<String> getLinks() {
			return links;
		}

		public void setLinks(List<String> links) {
			this.links = links;
		}

		public Actions getActions() {
			return actions;
		}

		public void setActions(Actions actions) {
			this.actions = actions;
		}

		public Metadata getMetadata() {
			return metadata;
		}

		public void setMetadata(Metadata metadata) {
			this.metadata = metadata;
		}

		public String getWarning() {
			return warning;
		}

		public void setWarning(String warning) {
			this.warning = warning;
		}

		public ChangeTracking getChangeTracking() {
			return changeTracking;
		}

		public void setChangeTracking(ChangeTracking changeTracking) {
			this.changeTracking = changeTracking;
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class Actions {

			@JsonProperty("screenshots")
			private List<String> screenshots;

			@JsonProperty("scrapes")
			private List<ScrapeResult> scrapes;

			@JsonProperty("javascriptReturns")
			private List<JavascriptReturn> javascriptReturns;

			@JsonProperty("pdfs")
			private List<String> pdfs;

			public List<String> getScreenshots() {
				return screenshots;
			}

			public void setScreenshots(List<String> screenshots) {
				this.screenshots = screenshots;
			}

			public List<ScrapeResult> getScrapes() {
				return scrapes;
			}

			public void setScrapes(List<ScrapeResult> scrapes) {
				this.scrapes = scrapes;
			}

			public List<JavascriptReturn> getJavascriptReturns() {
				return javascriptReturns;
			}

			public void setJavascriptReturns(List<JavascriptReturn> javascriptReturns) {
				this.javascriptReturns = javascriptReturns;
			}

			public List<String> getPdfs() {
				return pdfs;
			}

			public void setPdfs(List<String> pdfs) {
				this.pdfs = pdfs;
			}

			@JsonIgnoreProperties(ignoreUnknown = true)
			public static class ScrapeResult {

				@JsonProperty("url")
				private String url;

				@JsonProperty("html")
				private String html;

				public String getUrl() {
					return url;
				}

				public void setUrl(String url) {
					this.url = url;
				}

				public String getHtml() {
					return html;
				}

				public void setHtml(String html) {
					this.html = html;
				}

			}

			@JsonIgnoreProperties(ignoreUnknown = true)
			public static class JavascriptReturn {

				@JsonProperty("type")
				private String type;

				@JsonProperty("value")
				private JsonNode value;

				public String getType() {
					return type;
				}

				public void setType(String type) {
					this.type = type;
				}

				public JsonNode getValue() {
					return value;
				}

				public void setValue(JsonNode value) {
					this.value = value;
				}

			}

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

		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class ChangeTracking {

			@JsonProperty("previousScrapeAt")
			private String previousScrapeAt;

			@JsonProperty("changeStatus")
			private String changeStatus;

			@JsonProperty("visibility")
			private String visibility;

			@JsonProperty("diff")
			private String diff;

			@JsonProperty("json")
			private JsonNode json;

			public String getPreviousScrapeAt() {
				return previousScrapeAt;
			}

			public void setPreviousScrapeAt(String previousScrapeAt) {
				this.previousScrapeAt = previousScrapeAt;
			}

			public String getChangeStatus() {
				return changeStatus;
			}

			public void setChangeStatus(String changeStatus) {
				this.changeStatus = changeStatus;
			}

			public String getVisibility() {
				return visibility;
			}

			public void setVisibility(String visibility) {
				this.visibility = visibility;
			}

			public String getDiff() {
				return diff;
			}

			public void setDiff(String diff) {
				this.diff = diff;
			}

			public JsonNode getJson() {
				return json;
			}

			public void setJson(JsonNode json) {
				this.json = json;
			}

		}

	}

}
