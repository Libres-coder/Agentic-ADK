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
package com.alibaba.langengine.brave.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the response from the Brave Search API.
 */
public class SearchResponse {
    @JsonProperty("query")
    private SearchQuery query;

    @JsonProperty("results")
    private List<SearchResult> results;

    @JsonProperty("mixed")
    private Mixed mixed;

    // Getters and Setters

    public SearchQuery getQuery() {
        return query;
    }

    public void setQuery(SearchQuery query) {
        this.query = query;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }

    public Mixed getMixed() {
        return mixed;
    }

    public void setMixed(Mixed mixed) {
        this.mixed = mixed;
    }

    /**
     * Represents query information in the response.
     */
    public static class SearchQuery {
        @JsonProperty("original")
        private String original;

        @JsonProperty("show_strict_warning")
        private Boolean showStrictWarning;

        @JsonProperty("safesearch")
        private Boolean safesearch;

        @JsonProperty("country")
        private String country;

        @JsonProperty("bad_results")
        private Boolean badResults;

        // Getters and Setters

        public String getOriginal() {
            return original;
        }

        public void setOriginal(String original) {
            this.original = original;
        }

        public Boolean getShowStrictWarning() {
            return showStrictWarning;
        }

        public void setShowStrictWarning(Boolean showStrictWarning) {
            this.showStrictWarning = showStrictWarning;
        }

        public Boolean getSafesearch() {
            return safesearch;
        }

        public void setSafesearch(Boolean safesearch) {
            this.safesearch = safesearch;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public Boolean getBadResults() {
            return badResults;
        }

        public void setBadResults(Boolean badResults) {
            this.badResults = badResults;
        }
    }

    /**
     * Represents mixed content information in the response.
     */
    public static class Mixed {
        @JsonProperty("type")
        private String type;

        @JsonProperty("main")
        private List<Main> main;

        @JsonProperty("top")
        private List<Top> top;

        @JsonProperty("side")
        private List<Side> side;

        // Getters and Setters

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Main> getMain() {
            return main;
        }

        public void setMain(List<Main> main) {
            this.main = main;
        }

        public List<Top> getTop() {
            return top;
        }

        public void setTop(List<Top> top) {
            this.top = top;
        }

        public List<Side> getSide() {
            return side;
        }

        public void setSide(List<Side> side) {
            this.side = side;
        }

        /**
         * Represents main content information.
         */
        public static class Main {
            @JsonProperty("type")
            private String type;

            @JsonProperty("index")
            private Integer index;

            @JsonProperty("all")
            private Boolean all;

            // Getters and Setters

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public Integer getIndex() {
                return index;
            }

            public void setIndex(Integer index) {
                this.index = index;
            }

            public Boolean getAll() {
                return all;
            }

            public void setAll(Boolean all) {
                this.all = all;
            }
        }

        /**
         * Represents top content information.
         */
        public static class Top {
            @JsonProperty("type")
            private String type;

            @JsonProperty("index")
            private Integer index;

            // Getters and Setters

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public Integer getIndex() {
                return index;
            }

            public void setIndex(Integer index) {
                this.index = index;
            }
        }

        /**
         * Represents side content information.
         */
        public static class Side {
            @JsonProperty("type")
            private String type;

            @JsonProperty("index")
            private Integer index;

            // Getters and Setters

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public Integer getIndex() {
                return index;
            }

            public void setIndex(Integer index) {
                this.index = index;
            }
        }
    }
}