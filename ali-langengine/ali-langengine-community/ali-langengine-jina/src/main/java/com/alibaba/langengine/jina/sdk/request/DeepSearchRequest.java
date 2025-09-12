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
import java.util.Map;

/**
 * Request object for deep search API
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeepSearchRequest {

	/**
	 * Model identifier
	 */
	@JsonProperty("model")
	private String model;

	/**
	 * Whether to stream the response
	 */
	@JsonProperty("stream")
	private Boolean stream;

	/**
	 * Reasoning effort level
	 */
	@JsonProperty("reasoning_effort")
	private String reasoningEffort;

	/**
	 * Token budget for the search
	 */
	@JsonProperty("budget_tokens")
	private Integer budgetTokens;

	/**
	 * Maximum number of attempts
	 */
	@JsonProperty("max_attempts")
	private Integer maxAttempts;

	/**
	 * Whether to force further thinking
	 */
	@JsonProperty("no_direct_answer")
	private Boolean noDirectAnswer;

	/**
	 * Maximum number of URLs to return
	 */
	@JsonProperty("max_returned_urls")
	private Integer maxReturnedUrls;

	/**
	 * Response format specification
	 */
	@JsonProperty("response_format")
	private ResponseFormat responseFormat;

	/**
	 * Hostnames to boost
	 */
	@JsonProperty("boost_hostnames")
	private List<String> boostHostnames;

	/**
	 * Hostnames to exclude
	 */
	@JsonProperty("bad_hostnames")
	private List<String> badHostnames;

	/**
	 * Hostnames to include exclusively
	 */
	@JsonProperty("only_hostnames")
	private List<String> onlyHostnames;

	/**
	 * Conversation messages
	 */
	@JsonProperty("messages")
	private List<Message> messages;

	// Getters and setters
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Boolean getStream() {
		return stream;
	}

	public void setStream(Boolean stream) {
		this.stream = stream;
	}

	public String getReasoningEffort() {
		return reasoningEffort;
	}

	public void setReasoningEffort(String reasoningEffort) {
		this.reasoningEffort = reasoningEffort;
	}

	public Integer getBudgetTokens() {
		return budgetTokens;
	}

	public void setBudgetTokens(Integer budgetTokens) {
		this.budgetTokens = budgetTokens;
	}

	public Integer getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(Integer maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public Boolean getNoDirectAnswer() {
		return noDirectAnswer;
	}

	public void setNoDirectAnswer(Boolean noDirectAnswer) {
		this.noDirectAnswer = noDirectAnswer;
	}

	public Integer getMaxReturnedUrls() {
		return maxReturnedUrls;
	}

	public void setMaxReturnedUrls(Integer maxReturnedUrls) {
		this.maxReturnedUrls = maxReturnedUrls;
	}

	public ResponseFormat getResponseFormat() {
		return responseFormat;
	}

	public void setResponseFormat(ResponseFormat responseFormat) {
		this.responseFormat = responseFormat;
	}

	public List<String> getBoostHostnames() {
		return boostHostnames;
	}

	public void setBoostHostnames(List<String> boostHostnames) {
		this.boostHostnames = boostHostnames;
	}

	public List<String> getBadHostnames() {
		return badHostnames;
	}

	public void setBadHostnames(List<String> badHostnames) {
		this.badHostnames = badHostnames;
	}

	public List<String> getOnlyHostnames() {
		return onlyHostnames;
	}

	public void setOnlyHostnames(List<String> onlyHostnames) {
		this.onlyHostnames = onlyHostnames;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	/**
	 * Response format specification
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ResponseFormat {

		/**
		 * Format type
		 */
		@JsonProperty("type")
		private String type;

		/**
		 * JSON schema for output
		 */
		@JsonProperty("json_schema")
		private Map<String, Object> jsonSchema;

		// Getters and setters
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Map<String, Object> getJsonSchema() {
			return jsonSchema;
		}

		public void setJsonSchema(Map<String, Object> jsonSchema) {
			this.jsonSchema = jsonSchema;
		}

	}

	/**
	 * Message in the conversation
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Message {

		/**
		 * Role of the message sender
		 */
		@JsonProperty("role")
		private String role;

		/**
		 * Content of the message
		 */
		@JsonProperty("content")
		private Object content;

		// Getters and setters
		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}

		public Object getContent() {
			return content;
		}

		public void setContent(Object content) {
			this.content = content;
		}

	}

}
