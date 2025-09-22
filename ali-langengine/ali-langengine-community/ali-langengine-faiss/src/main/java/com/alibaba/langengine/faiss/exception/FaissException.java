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
package com.alibaba.langengine.faiss.exception;

/**
 * FAISS 异常类
 * 
 * @author langengine
 */
public class FaissException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public FaissException(String message) {
        super(message);
    }
    
    public FaissException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FaissException(Throwable cause) {
        super(cause);
    }
    
    /**
     * 创建索引异常
     */
    public static FaissException indexCreationError(String message, Throwable cause) {
        return new FaissException("Failed to create FAISS index: " + message, cause);
    }
    
    /**
     * 创建索引异常
     */
    public static FaissException indexCreationError(String message) {
        return new FaissException("Failed to create FAISS index: " + message);
    }
    
    /**
     * 向量添加异常
     */
    public static FaissException vectorAddError(String message, Throwable cause) {
        return new FaissException("Failed to add vector: " + message, cause);
    }
    
    /**
     * 向量添加异常
     */
    public static FaissException vectorAddError(String message) {
        return new FaissException("Failed to add vector: " + message);
    }
    
    /**
     * 搜索异常
     */
    public static FaissException searchError(String message, Throwable cause) {
        return new FaissException("Failed to perform search: " + message, cause);
    }
    
    /**
     * 搜索异常
     */
    public static FaissException searchError(String message) {
        return new FaissException("Failed to perform search: " + message);
    }
    
    /**
     * 维度不匹配异常
     */
    public static FaissException dimensionMismatch(int expected, int actual) {
        return new FaissException(String.format("Vector dimension mismatch. Expected: %d, Got: %d", 
            expected, actual));
    }
    
    /**
     * 索引文件异常
     */
    public static FaissException indexFileError(String message, Throwable cause) {
        return new FaissException("Index file error: " + message, cause);
    }
    
    /**
     * 索引文件异常
     */
    public static FaissException indexFileError(String message) {
        return new FaissException("Index file error: " + message);
    }
    
    /**
     * GPU相关异常
     */
    public static FaissException gpuError(String message, Throwable cause) {
        return new FaissException("GPU error: " + message, cause);
    }
    
    /**
     * GPU相关异常
     */
    public static FaissException gpuError(String message) {
        return new FaissException("GPU error: " + message);
    }
    
    /**
     * 内存不足异常
     */
    public static FaissException outOfMemoryError(String message) {
        return new FaissException("Out of memory: " + message);
    }
    
    /**
     * 参数验证异常
     */
    public static FaissException invalidParameter(String parameter, String value) {
        return new FaissException(String.format("Invalid parameter '%s': %s", parameter, value));
    }
}
