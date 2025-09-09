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

import java.util.List;

/**
 * Request object for creating embeddings
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmbeddingsRequest {

	/**
	 * Identifier of the model to use
	 */
	@JsonProperty("model")
	private String model;

	/**
	 * Array of input strings or objects to be embedded
	 */
	@JsonProperty("input")
	private List<Object> input;

	/**
	 * The format of the returned embeddings
	 */
	@JsonProperty("embedding_type")
	private Object embeddingType;

	/**
	 * Specifies the intended downstream application to optimize embedding output
	 */
	@JsonProperty("task")
	private String task;

	/**
	 * Truncates output embeddings to the specified size if set
	 */
	@JsonProperty("dimensions")
	private Integer dimensions;

	/**
	 * If true, concatenates all sentences in input and treats as a single input for late
	 * chunking
	 */
	@JsonProperty("late_chunking")
	private Boolean lateChunking;

	/**
	 * If true, the model will automatically drop the tail that extends beyond the maximum
	 * context length
	 */
	@JsonProperty("truncate")
	private Boolean truncate;

	/**
	 * If true, the model will return NxD multi-vector embeddings for every document
	 */
	@JsonProperty("return_multivector")
	private Boolean returnMultivector;

	// Getters and setters
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public List<Object> getInput() {
		return input;
	}

	public void setInput(List<Object> input) {
		this.input = input;
	}

	public Object getEmbeddingType() {
		return embeddingType;
	}

	public void setEmbeddingType(Object embeddingType) {
		this.embeddingType = embeddingType;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public Integer getDimensions() {
		return dimensions;
	}

	public void setDimensions(Integer dimensions) {
		this.dimensions = dimensions;
	}

	public Boolean getLateChunking() {
		return lateChunking;
	}

	public void setLateChunking(Boolean lateChunking) {
		this.lateChunking = lateChunking;
	}

	public Boolean getTruncate() {
		return truncate;
	}

	public void setTruncate(Boolean truncate) {
		this.truncate = truncate;
	}

	public Boolean getReturnMultivector() {
		return returnMultivector;
	}

	public void setReturnMultivector(Boolean returnMultivector) {
		this.returnMultivector = returnMultivector;
	}

}
