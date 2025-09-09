package com.alibaba.langengine.ollama;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import com.alibaba.langengine.ollama.sdk.OllamaConstant;

public class OllamaConfiguration {
    /**
     * Ollama API key, if required
     */
    public static String OLLAMA_API_KEY = WorkPropertiesUtils.get("ollama_api_key");

    /**
     * Ollama API base URL, defaults to the constant DEFAULT_BASE_URL if not configured
     */
    public static String OLLAMA_API_URL = WorkPropertiesUtils.get("ollama_api_url", OllamaConstant.DEFAULT_BASE_URL);
    
    /**
     * Ollama API connect timeout in milliseconds
     */
    public static Long OLLAMA_CONNECT_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("ollama_connect_timeout", "30000"));
    
    /**
     * Ollama API read timeout in milliseconds
     */
    public static Long OLLAMA_READ_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("ollama_read_timeout", "30000"));
    
    /**
     * Ollama API write timeout in milliseconds
     */
    public static Long OLLAMA_WRITE_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("ollama_write_timeout", "30000"));
}