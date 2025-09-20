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
package com.alibaba.langengine.cassandra.vectorstore;

import com.alibaba.langengine.cassandra.utils.Constants;
import lombok.Data;


@Data
public class CassandraParam {
    
    private InitParam initParam = new InitParam();
    private String fieldNamePageContent = Constants.DEFAULT_FIELD_NAME_PAGE_CONTENT;
    private String fieldNameUniqueId = Constants.DEFAULT_FIELD_NAME_UNIQUE_ID;
    private String fieldMeta = Constants.DEFAULT_FIELD_META;
    private String fieldNameVector = Constants.DEFAULT_FIELD_NAME_VECTOR;

    @Data
    public static class InitParam {
        private String tableName = Constants.DEFAULT_TABLE_NAME;
        private String vectorSimilarityFunction = Constants.DEFAULT_SIMILARITY_FUNCTION;
        private Integer vectorDimensions = Constants.DEFAULT_VECTOR_DIMENSIONS;
        private Integer replicationFactor = Constants.DEFAULT_REPLICATION_FACTOR;
        
        public String getTableName() {
            return tableName;
        }
    }
}
