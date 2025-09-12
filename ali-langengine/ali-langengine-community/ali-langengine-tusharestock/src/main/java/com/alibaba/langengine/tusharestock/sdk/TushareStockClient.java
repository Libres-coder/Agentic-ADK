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
package com.alibaba.langengine.tusharestock.sdk;

import com.alibaba.langengine.tusharestock.TushareStockConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.tusharestock.TushareStockConfiguration.*;

/**
 * TushareStock API Client for Java
 * This client provides methods to interact with the Tushare stock API.
 *
 * @author disaster
 */
@Slf4j
@Data
public class TushareStockClient {
    
    private final OkHttpClient client;
    
    private final ObjectMapper objectMapper;
    
    private final String baseUrl;
    
    private final String apiToken;
    
    /**
     * Constructs a TushareStockClient with a specified base URL and API token.
     * 
     * @param baseUrl the base URL for the Tushare service
     * @param apiToken the API token for authentication with the Tushare service
     */
    public TushareStockClient(String baseUrl, String apiToken) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.objectMapper = new ObjectMapper();
        
        // Create OkHttpClient with configuration
        this.client = new OkHttpClient.Builder()
                .connectTimeout(TUSHARE_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TUSHARE_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TUSHARE_WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }
    
    /**
     * Constructs a TushareStockClient using the default base URL and API token from configuration.
     */
    public TushareStockClient() {
        this(TUSHARE_API_URL, TUSHARE_API_TOKEN);
    }
    
    /**
     * Constructs a TushareStockClient with a specified base URL, API token and custom OkHttpClient.
     * 
     * @param baseUrl the base URL for the Tushare service
     * @param apiToken the API token for authentication with the Tushare service
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public TushareStockClient(String baseUrl, String apiToken, OkHttpClient okHttpClient) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.client = okHttpClient;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Executes a request to the Tushare API.
     * 
     * @param request the Tushare request parameters
     * @return the Tushare response result
     * @throws TushareStockException thrown when the API call fails
     */
    public TushareResponse execute(TushareRequest request) throws TushareStockException {
        try {
            // Set token if not provided in request
            if (request.getToken() == null) {
                request.setToken(apiToken);
            }
            
            // Convert the request object to JSON
            String jsonBody = objectMapper.writeValueAsString(request);
            
            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(baseUrl)
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new TushareStockException("API request failed: " + response.code() + " " + response.message());
                }
                
                ResponseBody body = response.body();
                if (body == null) {
                    throw new TushareStockException("API returned empty response");
                }
                
                // Parse the response
                TushareResponse tushareResponse = objectMapper.readValue(body.string(), TushareResponse.class);
                
                // Check if the response indicates an error
                if (tushareResponse.getCode() != null && tushareResponse.getCode() != 0) {
                    throw new TushareStockException("API returned error: " + tushareResponse.getMsg());
                }
                
                return tushareResponse;
            }
        } catch (IOException e) {
            throw new TushareStockException("Error occurred during API call", e);
        }
    }
    
    /**
     * Get stock basic information
     * 
     * @param request the stock basic request parameters
     * @return the Tushare response result
     * @throws TushareStockException thrown when the API call fails
     */
    public TushareResponse stockBasic(StockBasicRequest request) throws TushareStockException {
        TushareRequest tushareRequest = new TushareRequest();
        tushareRequest.setApiName(TushareStockConstant.API_NAME_STOCK_BASIC);
        
        Map<String, Object> params = new HashMap<>();
        if (request.getExchange() != null) {
            params.put("exchange", request.getExchange());
        }
        if (request.getListStatus() != null) {
            params.put("list_status", request.getListStatus());
        }
        
        tushareRequest.setParams(params);
        tushareRequest.setFields(request.getFields());
        
        return execute(tushareRequest);
    }
    
    /**
     * Get daily stock information
     * 
     * @param request the daily request parameters
     * @return the Tushare response result
     * @throws TushareStockException thrown when the API call fails
     */
    public TushareResponse daily(DailyRequest request) throws TushareStockException {
        TushareRequest tushareRequest = new TushareRequest();
        tushareRequest.setApiName(TushareStockConstant.API_NAME_DAILY);
        
        Map<String, Object> params = new HashMap<>();
        if (request.getTsCode() != null) {
            params.put("ts_code", request.getTsCode());
        }
        if (request.getTradeDate() != null) {
            params.put("trade_date", request.getTradeDate());
        }
        if (request.getStartDate() != null) {
            params.put("start_date", request.getStartDate());
        }
        if (request.getEndDate() != null) {
            params.put("end_date", request.getEndDate());
        }
        
        tushareRequest.setParams(params);
        tushareRequest.setFields(request.getFields());
        
        return execute(tushareRequest);
    }
    
    /**
     * Simplified stock basic method using default parameters.
     * 
     * @return the Tushare response result
     * @throws TushareStockException thrown when the API call fails
     */
    public TushareResponse stockBasic() throws TushareStockException {
        return stockBasic(new StockBasicRequest());
    }
    
    /**
     * Simplified daily method using default parameters.
     * 
     * @param tsCode the stock code
     * @return the Tushare response result
     * @throws TushareStockException thrown when the API call fails
     */
    public TushareResponse daily(String tsCode) throws TushareStockException {
        DailyRequest request = new DailyRequest();
        request.setTsCode(tsCode);
        return daily(request);
    }
}