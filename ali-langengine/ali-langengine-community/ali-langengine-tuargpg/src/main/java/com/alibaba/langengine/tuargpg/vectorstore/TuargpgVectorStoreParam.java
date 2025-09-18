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
package com.alibaba.langengine.tuargpg.vectorstore;

import lombok.Data;

@Data
public class TuargpgVectorStoreParam {

    private String serverUrl;

    private String username;

    private String password;

    private String database;

    private String schema = "public";

    private String tableName;

    private Integer vectorDimension = 1536;

    private String distanceFunction = "cosine";

    private Integer maxConnections = 10;

    private Integer connectionTimeoutMs = 30000;

    private Integer queryTimeoutMs = 60000;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TuargpgVectorStoreParam param = new TuargpgVectorStoreParam();

        public Builder serverUrl(String serverUrl) {
            param.setServerUrl(serverUrl);
            return this;
        }

        public Builder username(String username) {
            param.setUsername(username);
            return this;
        }

        public Builder password(String password) {
            param.setPassword(password);
            return this;
        }

        public Builder database(String database) {
            param.setDatabase(database);
            return this;
        }

        public Builder schema(String schema) {
            param.setSchema(schema);
            return this;
        }

        public Builder tableName(String tableName) {
            param.setTableName(tableName);
            return this;
        }

        public Builder vectorDimension(Integer vectorDimension) {
            param.setVectorDimension(vectorDimension);
            return this;
        }

        public Builder distanceFunction(String distanceFunction) {
            param.setDistanceFunction(distanceFunction);
            return this;
        }

        public Builder maxConnections(Integer maxConnections) {
            param.setMaxConnections(maxConnections);
            return this;
        }

        public Builder connectionTimeoutMs(Integer connectionTimeoutMs) {
            param.setConnectionTimeoutMs(connectionTimeoutMs);
            return this;
        }

        public Builder queryTimeoutMs(Integer queryTimeoutMs) {
            param.setQueryTimeoutMs(queryTimeoutMs);
            return this;
        }

        public TuargpgVectorStoreParam build() {
            return param;
        }
    }
}