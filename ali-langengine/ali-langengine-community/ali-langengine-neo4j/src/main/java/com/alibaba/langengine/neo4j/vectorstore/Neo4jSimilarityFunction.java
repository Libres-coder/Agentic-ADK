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
package com.alibaba.langengine.neo4j.vectorstore;


public enum Neo4jSimilarityFunction {
    
    /**
     * 余弦相似性 - 适用于大多数文本向量
     * 计算两个向量之间的余弦角度，范围 [0, 1]
     */
    COSINE("cosine"),
    
    /**
     * 欧几里得距离 - 适用于数值向量
     * 计算两个向量之间的欧几里得距离
     */
    EUCLIDEAN("euclidean"),
    
    /**
     * 点积相似性 - 适用于归一化向量
     * 计算两个向量的点积
     */
    DOT("dot");
    
    private final String value;
    
    Neo4jSimilarityFunction(String value) {
        this.value = value;
    }
    
    /**
     * 获取Neo4j中使用的字符串值
     */
    public String getValue() {
        return value;
    }
    
    /**
     * 从字符串值创建枚举
     */
    public static Neo4jSimilarityFunction fromValue(String value) {
        if (value == null) {
            return COSINE; // 默认值
        }
        
        for (Neo4jSimilarityFunction function : values()) {
            if (function.value.equalsIgnoreCase(value)) {
                return function;
            }
        }
        
        throw new IllegalArgumentException("Unsupported similarity function: " + value + 
            ". Supported values are: cosine, euclidean, dot");
    }
    
    @Override
    public String toString() {
        return value;
    }
}
