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
package com.alibaba.langengine.zilliz;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class ZillizConfiguration {

    /**
     * zilliz cloud cluster endpoint
     */
    public static String ZILLIZ_CLUSTER_ENDPOINT = WorkPropertiesUtils.get("zilliz_cluster_endpoint");

    /**
     * zilliz cloud api key
     */
    public static String ZILLIZ_API_KEY = WorkPropertiesUtils.get("zilliz_api_key");

    /**
     * zilliz cloud database name
     */
    public static String ZILLIZ_DATABASE_NAME = WorkPropertiesUtils.get("zilliz_database_name", "default");

}