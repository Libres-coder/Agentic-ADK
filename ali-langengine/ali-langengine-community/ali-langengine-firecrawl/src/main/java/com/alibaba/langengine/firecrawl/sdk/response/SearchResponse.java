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

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {

	/**
	 * Indicates if the request was successful
	 */
	@JsonProperty("success")
	private Boolean success;

	/**
	 * Contains the search results data
	 */
	@JsonProperty("data")
	private Data data;

	/**
	 * Warning message if any issues occurred
	 */
	@JsonProperty("warning")
	private String warning;

	/**
	 * Error message if request failed
	 */
	@JsonProperty("error")
	private String error;

	/**
	 * Error code if request failed
	 */
	@JsonProperty("code")
	private String code;

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

	public String getWarning() {
		return warning;
	}

	public void setWarning(String warning) {
		this.warning = warning;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Data {

		/**
		 * Web search results
		 */
		@JsonProperty("web")
		private WebResult[] web;

		/**
		 * Image search results
		 */
		@JsonProperty("images")
		private ImageResult[] images;

		/**
		 * News search results
		 */
		@JsonProperty("news")
		private NewsResult[] news;

		// Getters and setters
		public WebResult[] getWeb() {
			return web;
		}

		public void setWeb(WebResult[] web) {
			this.web = web;
		}

		public ImageResult[] getImages() {
			return images;
		}

		public void setImages(ImageResult[] images) {
			this.images = images;
		}

		public NewsResult[] getNews() {
			return news;
		}

		public void setNews(NewsResult[] news) {
			this.news = news;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class WebResult {

		/**
		 * Title from search result
		 */
		@JsonProperty("title")
		private String title;

		/**
		 * Description from search result
		 */
		@JsonProperty("description")
		private String description;

		/**
		 * URL of the search result
		 */
		@JsonProperty("url")
		private String url;

		/**
		 * Markdown content if scraping was requested
		 */
		@JsonProperty("markdown")
		private String markdown;

		/**
		 * HTML content if requested in formats
		 */
		@JsonProperty("html")
		private String html;

		/**
		 * Raw HTML content if requested in formats
		 */
		@JsonProperty("rawHtml")
		private String rawHtml;

		/**
		 * Links found if requested in formats
		 */
		@JsonProperty("links")
		private String[] links;

		/**
		 * Screenshot URL if requested in formats
		 */
		@JsonProperty("screenshot")
		private String screenshot;

		/**
		 * Metadata about the result
		 */
		@JsonProperty("metadata")
		private Metadata metadata;

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

		public String[] getLinks() {
			return links;
		}

		public void setLinks(String[] links) {
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

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ImageResult {

		/**
		 * Title from search result
		 */
		@JsonProperty("title")
		private String title;

		/**
		 * URL of the image
		 */
		@JsonProperty("imageUrl")
		private String imageUrl;

		/**
		 * Width of the image
		 */
		@JsonProperty("imageWidth")
		private Integer imageWidth;

		/**
		 * Height of the image
		 */
		@JsonProperty("imageHeight")
		private Integer imageHeight;

		/**
		 * URL of the search result
		 */
		@JsonProperty("url")
		private String url;

		/**
		 * Position of the search result
		 */
		@JsonProperty("position")
		private Integer position;

		// Getters and setters
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getImageUrl() {
			return imageUrl;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public Integer getImageWidth() {
			return imageWidth;
		}

		public void setImageWidth(Integer imageWidth) {
			this.imageWidth = imageWidth;
		}

		public Integer getImageHeight() {
			return imageHeight;
		}

		public void setImageHeight(Integer imageHeight) {
			this.imageHeight = imageHeight;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Integer getPosition() {
			return position;
		}

		public void setPosition(Integer position) {
			this.position = position;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class NewsResult {

		/**
		 * Title of the article
		 */
		@JsonProperty("title")
		private String title;

		/**
		 * Snippet from the article
		 */
		@JsonProperty("snippet")
		private String snippet;

		/**
		 * URL of the article
		 */
		@JsonProperty("url")
		private String url;

		/**
		 * Date of the article
		 */
		@JsonProperty("date")
		private String date;

		/**
		 * Image URL of the article
		 */
		@JsonProperty("imageUrl")
		private String imageUrl;

		/**
		 * Position of the article
		 */
		@JsonProperty("position")
		private Integer position;

		/**
		 * Markdown content if scraping was requested
		 */
		@JsonProperty("markdown")
		private String markdown;

		/**
		 * HTML content if requested in formats
		 */
		@JsonProperty("html")
		private String html;

		/**
		 * Raw HTML content if requested in formats
		 */
		@JsonProperty("rawHtml")
		private String rawHtml;

		/**
		 * Links found if requested in formats
		 */
		@JsonProperty("links")
		private String[] links;

		/**
		 * Screenshot URL if requested in formats
		 */
		@JsonProperty("screenshot")
		private String screenshot;

		/**
		 * Metadata about the result
		 */
		@JsonProperty("metadata")
		private Metadata metadata;

		// Getters and setters
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getSnippet() {
			return snippet;
		}

		public void setSnippet(String snippet) {
			this.snippet = snippet;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getImageUrl() {
			return imageUrl;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public Integer getPosition() {
			return position;
		}

		public void setPosition(Integer position) {
			this.position = position;
		}

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

		public String[] getLinks() {
			return links;
		}

		public void setLinks(String[] links) {
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

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Metadata {

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
		 * Source URL of the page
		 */
		@JsonProperty("sourceURL")
		private String sourceURL;

		/**
		 * HTTP status code
		 */
		@JsonProperty("statusCode")
		private Integer statusCode;

		/**
		 * Error message if any
		 */
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
