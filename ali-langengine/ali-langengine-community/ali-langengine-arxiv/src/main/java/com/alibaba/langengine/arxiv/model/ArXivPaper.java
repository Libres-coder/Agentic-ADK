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
package com.alibaba.langengine.arxiv.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;


@Data
@EqualsAndHashCode
public class ArXivPaper {
    
    /**
     * ArXiv paper ID (e.g., "2301.12345")
     */
    private String id;
    
    /**
     * Paper title
     */
    private String title;
    
    /**
     * Paper abstract/summary
     */
    private String summary;
    
    /**
     * List of authors
     */
    private List<String> authors;
    
    /**
     * List of subject categories
     */
    private List<String> categories;
    
    /**
     * Primary subject category
     */
    private String primaryCategory;
    
    /**
     * Published date
     */
    private LocalDateTime published;
    
    /**
     * Last updated date
     */
    private LocalDateTime updated;
    
    /**
     * ArXiv URL for the paper
     */
    private String arxivUrl;
    
    /**
     * PDF download URL
     */
    private String pdfUrl;
    
    /**
     * DOI (Digital Object Identifier)
     */
    private String doi;
    
    /**
     * Journal reference
     */
    private String journalRef;
    
    /**
     * Comments from authors
     */
    private String comment;
    
    /**
     * Paper version (e.g., "v1", "v2")
     */
    private String version;
    
    /**
     * Get the formatted citation
     */
    public String getCitation() {
        StringBuilder citation = new StringBuilder();
        
        if (authors != null && !authors.isEmpty()) {
            if (authors.size() == 1) {
                citation.append(authors.get(0));
            } else if (authors.size() <= 3) {
                citation.append(String.join(", ", authors));
            } else {
                citation.append(authors.get(0)).append(" et al.");
            }
            citation.append(". ");
        }
        
        if (title != null) {
            citation.append("\"").append(title).append(".\" ");
        }
        
        if (published != null) {
            citation.append("arXiv preprint arXiv:").append(id)
                    .append(" (").append(published.getYear()).append(").");
        }
        
        return citation.toString().trim();
    }
    
    /**
     * Get a shortened summary (first 200 characters)
     */
    public String getShortSummary() {
        if (summary == null || summary.length() <= 200) {
            return summary;
        }
        return summary.substring(0, 197) + "...";
    }
    
    /**
     * Check if this is a recent paper (published within last 30 days)
     */
    public boolean isRecent() {
        if (published == null) {
            return false;
        }
        return published.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * Get the main category name (first category)
     */
    public String getMainCategory() {
        if (categories != null && !categories.isEmpty()) {
            return categories.get(0);
        }
        return primaryCategory;
    }
    
    /**
     * Get a formatted string representation of the paper
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArXiv Paper [").append(id).append("]\n");
        sb.append("Title: ").append(title).append("\n");
        if (authors != null && !authors.isEmpty()) {
            sb.append("Authors: ").append(String.join(", ", authors)).append("\n");
        }
        if (categories != null && !categories.isEmpty()) {
            sb.append("Categories: ").append(String.join(", ", categories)).append("\n");
        }
        if (published != null) {
            sb.append("Published: ").append(published).append("\n");
        }
        if (summary != null) {
            sb.append("Summary: ").append(getShortSummary()).append("\n");
        }
        if (arxivUrl != null) {
            sb.append("URL: ").append(arxivUrl).append("\n");
        }
        if (pdfUrl != null) {
            sb.append("PDF: ").append(pdfUrl);
        }
        return sb.toString();
    }
}
