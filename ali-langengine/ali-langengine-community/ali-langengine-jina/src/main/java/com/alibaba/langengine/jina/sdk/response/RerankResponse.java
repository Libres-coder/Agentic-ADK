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

import java.util.List;

/**
 * Response object for reranker API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RerankResponse {

	/**
	 * Model identifier
	 */
	@JsonProperty("model")
	private String model;

	/**
	 * Usage information
	 */
	@JsonProperty("usage")
	private Usage usage;

	/**
	 * Reranking results
	 */
	@JsonProperty("results")
	private List<RerankResult> results;

	// Getters and setters
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Usage getUsage() {
		return usage;
	}

	public void setUsage(Usage usage) {
		this.usage = usage;
	}

	public List<RerankResult> getResults() {
		return results;
	}

	public void setResults(List<RerankResult> results) {
		this.results = results;
	}

	/**
	 * Individual rerank result
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RerankResult {

		/**
		 * Index of the document
		 */
		@JsonProperty("index")
		private Integer index;

		/**
		 * Relevance score of the document
		 */
		@JsonProperty("relevance_score")
		private Double relevanceScore;

		// Getters and setters
		public Integer getIndex() {
			return index;
		}

		public void setIndex(Integer index) {
			this.index = index;
		}

		public Double getRelevanceScore() {
			return relevanceScore;
		}

		public void setRelevanceScore(Double relevanceScore) {
			this.relevanceScore = relevanceScore;
		}

	}

}
