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
package com.alibaba.langengine.cassandra;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class CassandraConfiguration {

    public static String CASSANDRA_CONTACT_POINTS = WorkPropertiesUtils.get("cassandra_contact_points");
    
    public static String CASSANDRA_LOCAL_DATACENTER = WorkPropertiesUtils.get("cassandra_local_datacenter");
    
    public static String CASSANDRA_USERNAME = WorkPropertiesUtils.get("cassandra_username");
    
    public static String CASSANDRA_PASSWORD = WorkPropertiesUtils.get("cassandra_password");
    
    public static String CASSANDRA_KEYSPACE = WorkPropertiesUtils.get("cassandra_keyspace");

}
