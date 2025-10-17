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
package com.alibaba.langengine.tile38.vectorstore;


public class Tile38Exception extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String errorCode;

    public Tile38Exception(String message) {
        super(message);
    }

    public Tile38Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Tile38Exception(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public Tile38Exception(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}