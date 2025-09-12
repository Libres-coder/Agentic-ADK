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

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import lombok.Data;

import java.util.Arrays;
import java.util.List;


@Data
public class CassandraConfiguration {
    
    /**
     * Contact points for Cassandra cluster
     */
    private List<String> contactPoints;
    
    /**
     * Local datacenter name
     */
    private String localDatacenter;
    
    /**
     * Keyspace name
     */
    private String keyspace;
    
    /**
     * Username for authentication
     */
    private String username;
    
    /**
     * Password for authentication
     */
    private String password;

    public CassandraConfiguration() {
        // Default configuration from properties
        init();
    }

    private void init() {
        // Load from properties if available
        String contactPointsStr = WorkPropertiesUtils.get("cassandra.contact.points");
        if (contactPointsStr != null) {
            this.contactPoints = Arrays.asList(contactPointsStr.split(","));
        }
        
        this.localDatacenter = WorkPropertiesUtils.get("cassandra.local.datacenter");
        this.keyspace = WorkPropertiesUtils.get("cassandra.keyspace");
        this.username = WorkPropertiesUtils.get("cassandra.username");
        this.password = WorkPropertiesUtils.get("cassandra.password");
    }
}
