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
package com.alibaba.langengine.singlestore;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class SingleStoreConfiguration {

    /**
     * singlestore server url
     */
    public static String SINGLESTORE_SERVER_URL = WorkPropertiesUtils.get("singlestore_server_url");

    /**
     * singlestore database name
     */
    public static String SINGLESTORE_DATABASE = WorkPropertiesUtils.get("singlestore_database");

    /**
     * singlestore username
     */
    public static String SINGLESTORE_USERNAME = WorkPropertiesUtils.get("singlestore_username");

    /**
     * singlestore password
     */
    public static String SINGLESTORE_PASSWORD = WorkPropertiesUtils.get("singlestore_password");

}