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
 * Response object for segmenter API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SegmenterResponse {

	/**
	 * Number of tokens
	 */
	@JsonProperty("num_tokens")
	private Integer numTokens;

	/**
	 * Tokenizer used
	 */
	@JsonProperty("tokenizer")
	private String tokenizer;

	/**
	 * Usage information
	 */
	@JsonProperty("usage")
	private Usage usage;

	/**
	 * Number of chunks
	 */
	@JsonProperty("num_chunks")
	private Integer numChunks;

	/**
	 * Chunk positions
	 */
	@JsonProperty("chunk_positions")
	private List<List<Integer>> chunkPositions;

	/**
	 * Tokens
	 */
	@JsonProperty("tokens")
	private List<List<List<Object>>> tokens;

	/**
	 * Chunks
	 */
	@JsonProperty("chunks")
	private List<String> chunks;

	// Getters and setters
	public Integer getNumTokens() {
		return numTokens;
	}

	public void setNumTokens(Integer numTokens) {
		this.numTokens = numTokens;
	}

	public String getTokenizer() {
		return tokenizer;
	}

	public void setTokenizer(String tokenizer) {
		this.tokenizer = tokenizer;
	}

	public Usage getUsage() {
		return usage;
	}

	public void setUsage(Usage usage) {
		this.usage = usage;
	}

	public Integer getNumChunks() {
		return numChunks;
	}

	public void setNumChunks(Integer numChunks) {
		this.numChunks = numChunks;
	}

	public List<List<Integer>> getChunkPositions() {
		return chunkPositions;
	}

	public void setChunkPositions(List<List<Integer>> chunkPositions) {
		this.chunkPositions = chunkPositions;
	}

	public List<List<List<Object>>> getTokens() {
		return tokens;
	}

	public void setTokens(List<List<List<Object>>> tokens) {
		this.tokens = tokens;
	}

	public List<String> getChunks() {
		return chunks;
	}

	public void setChunks(List<String> chunks) {
		this.chunks = chunks;
	}

}
