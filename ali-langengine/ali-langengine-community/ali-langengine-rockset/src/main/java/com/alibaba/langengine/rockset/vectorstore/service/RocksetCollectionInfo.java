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
package com.alibaba.langengine.rockset.vectorstore.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class RocksetCollectionInfo {

    @JsonProperty("name")
    private String name;

    @JsonProperty("workspace")
    private String workspace;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private String status;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("retention_secs")
    private Long retentionSecs;

    @JsonProperty("storage_compression_type")
    private String storageCompressionType;

    @JsonProperty("field_mappings")
    private List<FieldMapping> fieldMappings;

    @JsonProperty("sources")
    private List<Source> sources;

    @Data
    public static class FieldMapping {
        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private String type;

        @JsonProperty("is_drop")
        private Boolean isDrop;
    }

    @Data
    public static class Source {
        @JsonProperty("integration_name")
        private String integrationName;

        @JsonProperty("object_count_total")
        private Long objectCountTotal;

        @JsonProperty("object_count_downloaded")
        private Long objectCountDownloaded;
    }
}
