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
package com.alibaba.langengine.tuargpg;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class TuargpgConfiguration {

    public static String TUARGPG_SERVER_URL = WorkPropertiesUtils.get("tuargpg_server_url");

    public static String TUARGPG_USERNAME = WorkPropertiesUtils.get("tuargpg_username");

    public static String TUARGPG_PASSWORD = WorkPropertiesUtils.get("tuargpg_password");

    public static String TUARGPG_DATABASE = WorkPropertiesUtils.get("tuargpg_database");

    public static String TUARGPG_SCHEMA = WorkPropertiesUtils.get("tuargpg_schema");

    public static String TUARGPG_TABLE = WorkPropertiesUtils.get("tuargpg_table");

}