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

package com.alibaba.langengine.jina.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request object for search API
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest {

	/**
	 * Search query
	 */
	@JsonProperty("q")
	private String query;

	/**
	 * Country code for the search
	 */
	@JsonProperty("gl")
	private String country;

	/**
	 * Location for the search
	 */
	@JsonProperty("location")
	private String location;

	/**
	 * Language code for the search
	 */
	@JsonProperty("hl")
	private String language;

	/**
	 * Maximum number of results
	 */
	@JsonProperty("num")
	private Integer numResults;

	/**
	 * Page offset for pagination
	 */
	@JsonProperty("page")
	private Integer page;

	// Getters and setters
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Integer getNumResults() {
		return numResults;
	}

	public void setNumResults(Integer numResults) {
		this.numResults = numResults;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

}
