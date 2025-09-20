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
package com.alibaba.langengine.arxiv.sdk;

import com.alibaba.langengine.arxiv.model.ArXivPaper;
import com.alibaba.langengine.arxiv.model.ArXivSearchRequest;
import com.alibaba.langengine.arxiv.model.ArXivSearchResponse;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import lombok.extern.slf4j.Slf4j;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.arxiv.ArXivConfiguration.ARXIV_API_URL;
import static com.alibaba.langengine.arxiv.sdk.ArXivConstant.*;


@Slf4j
public class ArXivClient {
    
    private final OkHttpClient client;
    private final String baseUrl;
    
    /**
     * Constructs an ArXivClient using the default configuration.
     */
    public ArXivClient() {
        this.baseUrl = ARXIV_API_URL;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Constructs an ArXivClient with a custom OkHttpClient.
     *
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public ArXivClient(OkHttpClient okHttpClient) {
        this.baseUrl = ARXIV_API_URL;
        this.client = okHttpClient;
    }
    
    /**
     * Constructs an ArXivClient with a custom base URL and OkHttpClient.
     *
     * @param baseUrl the base URL for the ArXiv API
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public ArXivClient(String baseUrl, OkHttpClient okHttpClient) {
        this.baseUrl = baseUrl;
        this.client = okHttpClient;
    }
    
    /**
     * Executes a search request to the ArXiv API.
     *
     * @param request the search request parameters
     * @return the search response result
     * @throws ArXivException thrown when the API call fails
     */
    public ArXivSearchResponse search(ArXivSearchRequest request) throws ArXivException {
        long startTime = System.currentTimeMillis();
        
        try {
            // Build the HTTP URL with query parameters
            HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + QUERY_ENDPOINT).newBuilder();
            
            // Add search query
            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                urlBuilder.addQueryParameter("search_query", buildSearchQuery(request));
            } else {
                throw new ArXivException("Search query cannot be empty");
            }
            
            // Add pagination parameters
            if (request.getStart() != null && request.getStart() >= 0) {
                urlBuilder.addQueryParameter("start", request.getStart().toString());
            }
            
            if (request.getMaxResults() != null && request.getMaxResults() > 0) {
                int maxResults = Math.min(request.getMaxResults(), MAX_RESULTS_LIMIT);
                urlBuilder.addQueryParameter("max_results", String.valueOf(maxResults));
            }
            
            // Add sorting parameters
            if (request.getSortBy() != null && request.getSortOrder() != null) {
                String sortBy = request.getSortBy();
                String sortOrder = request.getSortOrder();
                urlBuilder.addQueryParameter("sortBy", sortBy);
                urlBuilder.addQueryParameter("sortOrder", sortOrder);
            }
            
            // Create the HTTP request
            Request httpRequest = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/atom+xml")
                    .addHeader("User-Agent", "ArXiv-Java-Client/1.0")
                    .get()
                    .build();
            
            log.info("Executing ArXiv search: {}", httpRequest.url());
            
            // Execute the request
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new ArXivException("API request failed: " + response.code() + " " + response.message());
                }
                
                ResponseBody body = response.body();
                if (body == null) {
                    throw new ArXivException("API returned empty response");
                }
                
                // Parse the XML response
                String xmlContent = body.string();
                ArXivSearchResponse searchResponse = parseXmlResponse(xmlContent, request);
                
                // Set execution time
                long executionTime = System.currentTimeMillis() - startTime;
                searchResponse.setExecutionTimeMs(executionTime);
                
                log.info("ArXiv search completed: {} papers found in {}ms", 
                        searchResponse.getReturnedCount(), executionTime);
                
                return searchResponse;
            }
        } catch (IOException e) {
            throw new ArXivException("Error occurred during API call", e);
        }
    }
    
    /**
     * Simplified search method using query string.
     *
     * @param query the search query string
     * @return the search response result
     * @throws ArXivException thrown when the API call fails
     */
    public ArXivSearchResponse search(String query) throws ArXivException {
        ArXivSearchRequest request = new ArXivSearchRequest(query);
        return search(request);
    }
    
    /**
     * Simplified search method with query string and result count.
     *
     * @param query the search query string
     * @param maxResults the maximum number of results to return
     * @return the search response result
     * @throws ArXivException thrown when the API call fails
     */
    public ArXivSearchResponse search(String query, int maxResults) throws ArXivException {
        ArXivSearchRequest request = new ArXivSearchRequest();
        request.setQuery(query);
        request.setMaxResults(maxResults);
        return search(request);
    }
    
    /**
     * Builds the search query string from the request parameters.
     */
    private String buildSearchQuery(ArXivSearchRequest request) {
        StringBuilder queryBuilder = new StringBuilder();
        
        // Add main query
        String mainQuery = request.getQuery().trim();
        if (mainQuery.contains(":")) {
            // Already has field specifiers
            queryBuilder.append(mainQuery);
        } else {
            // Add to all fields
            queryBuilder.append("all:").append(mainQuery);
        }
        
        // Add category filters
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            for (String category : request.getCategories()) {
                queryBuilder.append(" AND cat:").append(category);
            }
        }
        
        // Add author filters
        if (request.getAuthors() != null && !request.getAuthors().isEmpty()) {
            for (String author : request.getAuthors()) {
                queryBuilder.append(" AND au:\"").append(author).append("\"");
            }
        }
        
        // Add date range filters
        if (request.getStartDate() != null || request.getEndDate() != null) {
            queryBuilder.append(" AND submittedDate:[");
            queryBuilder.append(request.getStartDate() != null ? request.getStartDate() : "*");
            queryBuilder.append(" TO ");
            queryBuilder.append(request.getEndDate() != null ? request.getEndDate() : "*");
            queryBuilder.append("]");
        }
        
        return queryBuilder.toString();
    }
    
    /**
     * Parses the XML response from ArXiv API.
     */
    private ArXivSearchResponse parseXmlResponse(String xmlContent, ArXivSearchRequest request) throws ArXivException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
            
            // Get root element
            Element root = doc.getDocumentElement();
            
            // Parse total results
            NodeList totalResultsNodes = root.getElementsByTagNameNS(OPENSEARCH_NAMESPACE, "totalResults");
            Integer totalResults = 0;
            if (totalResultsNodes.getLength() > 0) {
                totalResults = Integer.parseInt(totalResultsNodes.item(0).getTextContent());
            }
            
            // Parse start index
            NodeList startIndexNodes = root.getElementsByTagNameNS(OPENSEARCH_NAMESPACE, "startIndex");
            Integer startIndex = 0;
            if (startIndexNodes.getLength() > 0) {
                startIndex = Integer.parseInt(startIndexNodes.item(0).getTextContent());
            }
            
            // Parse items per page
            NodeList itemsPerPageNodes = root.getElementsByTagNameNS(OPENSEARCH_NAMESPACE, "itemsPerPage");
            Integer itemsPerPage = 10;
            if (itemsPerPageNodes.getLength() > 0) {
                itemsPerPage = Integer.parseInt(itemsPerPageNodes.item(0).getTextContent());
            }
            
            // Parse entries (papers)
            List<ArXivPaper> papers = new ArrayList<>();
            NodeList entries = root.getElementsByTagNameNS(ARXIV_NAMESPACE, "entry");
            
            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);
                ArXivPaper paper = parseEntry(entry);
                if (paper != null) {
                    papers.add(paper);
                }
            }
            
            return new ArXivSearchResponse(papers, totalResults, startIndex, itemsPerPage, request.getQuery());
            
        } catch (Exception e) {
            throw new ArXivException("Failed to parse XML response", e);
        }
    }
    
    /**
     * Parses a single entry (paper) from the XML.
     */
    private ArXivPaper parseEntry(Element entry) {
        try {
            ArXivPaper paper = new ArXivPaper();
            
            // Parse ID
            NodeList idNodes = entry.getElementsByTagNameNS(ARXIV_NAMESPACE, "id");
            if (idNodes.getLength() > 0) {
                String fullId = idNodes.item(0).getTextContent();
                paper.setId(extractArxivId(fullId));
                paper.setArxivUrl(fullId);
                paper.setPdfUrl(fullId.replace("/abs/", "/pdf/") + ".pdf");
            }
            
            // Parse title
            NodeList titleNodes = entry.getElementsByTagNameNS(ARXIV_NAMESPACE, "title");
            if (titleNodes.getLength() > 0) {
                paper.setTitle(titleNodes.item(0).getTextContent().trim());
            }
            
            // Parse summary
            NodeList summaryNodes = entry.getElementsByTagNameNS(ARXIV_NAMESPACE, "summary");
            if (summaryNodes.getLength() > 0) {
                paper.setSummary(summaryNodes.item(0).getTextContent().trim());
            }
            
            // Parse authors
            List<String> authors = new ArrayList<>();
            NodeList authorNodes = entry.getElementsByTagNameNS(ARXIV_NAMESPACE, "author");
            for (int i = 0; i < authorNodes.getLength(); i++) {
                Element authorElement = (Element) authorNodes.item(i);
                NodeList nameNodes = authorElement.getElementsByTagNameNS(ARXIV_NAMESPACE, "name");
                if (nameNodes.getLength() > 0) {
                    authors.add(nameNodes.item(0).getTextContent().trim());
                }
            }
            paper.setAuthors(authors);
            
            // Parse categories
            List<String> categories = new ArrayList<>();
            NodeList categoryNodes = entry.getElementsByTagNameNS(ARXIV_NAMESPACE, "category");
            for (int i = 0; i < categoryNodes.getLength(); i++) {
                Element categoryElement = (Element) categoryNodes.item(i);
                String term = categoryElement.getAttribute("term");
                if (term != null && !term.isEmpty()) {
                    categories.add(term);
                }
            }
            paper.setCategories(categories);
            if (!categories.isEmpty()) {
                paper.setPrimaryCategory(categories.get(0));
            }
            
            // Parse published date
            NodeList publishedNodes = entry.getElementsByTagNameNS(ARXIV_NAMESPACE, "published");
            if (publishedNodes.getLength() > 0) {
                String publishedStr = publishedNodes.item(0).getTextContent();
                paper.setPublished(parseDateTime(publishedStr));
            }
            
            // Parse updated date
            NodeList updatedNodes = entry.getElementsByTagNameNS(ARXIV_NAMESPACE, "updated");
            if (updatedNodes.getLength() > 0) {
                String updatedStr = updatedNodes.item(0).getTextContent();
                paper.setUpdated(parseDateTime(updatedStr));
            }
            
            // Parse DOI if available
            NodeList linkNodes = entry.getElementsByTagNameNS(ARXIV_NAMESPACE, "link");
            for (int i = 0; i < linkNodes.getLength(); i++) {
                Element linkElement = (Element) linkNodes.item(i);
                String title = linkElement.getAttribute("title");
                if ("doi".equals(title)) {
                    paper.setDoi(linkElement.getAttribute("href"));
                    break;
                }
            }
            
            // Parse comment if available
            NodeList commentNodes = entry.getElementsByTagNameNS("http://arxiv.org/schemas/atom", "comment");
            if (commentNodes.getLength() > 0) {
                paper.setComment(commentNodes.item(0).getTextContent().trim());
            }
            
            return paper;
            
        } catch (Exception e) {
            log.warn("Failed to parse paper entry: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extracts the ArXiv ID from the full URL.
     */
    private String extractArxivId(String fullId) {
        if (fullId != null && fullId.contains("/abs/")) {
            return fullId.substring(fullId.lastIndexOf("/") + 1);
        }
        return fullId;
    }
    
    /**
     * Parses date-time string from ArXiv API.
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            // ArXiv uses ISO format: 2023-01-15T10:30:00Z
            return LocalDateTime.parse(dateTimeStr.replace("Z", ""), 
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse date-time: {}", dateTimeStr);
            return null;
        }
    }
}
