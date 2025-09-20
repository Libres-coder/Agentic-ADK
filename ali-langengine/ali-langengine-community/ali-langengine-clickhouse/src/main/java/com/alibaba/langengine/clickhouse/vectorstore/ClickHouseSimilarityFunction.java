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
package com.alibaba.langengine.clickhouse.vectorstore;


public enum ClickHouseSimilarityFunction {
    
    /**
     * 余弦相似性 - 适用于大多数文本向量
     * 计算两个向量之间的余弦角度，范围 [0, 1]
     */
    COSINE("cosineDistance"),
    
    /**
     * L2欧几里得距离 - 适用于数值向量
     * 计算两个向量之间的欧几里得距离
     */
    L2("L2Distance"),
    
    /**
     * L1曼哈顿距离 - 计算向量的曼哈顿距离
     */
    L1("L1Distance"),
    
    /**
     * Linf无穷范数距离 - 计算向量的无穷范数距离
     */
    LINF("LinfDistance"),
    
    /**
     * 内积相似性 - 计算向量的内积
     */
    DOT_PRODUCT("dotProduct");
    
    private final String functionName;
    
    ClickHouseSimilarityFunction(String functionName) {
        this.functionName = functionName;
    }
    
    /**
     * 获取ClickHouse中使用的函数名
     */
    public String getFunctionName() {
        return functionName;
    }
    
    /**
     * 从字符串值创建枚举
     */
    public static ClickHouseSimilarityFunction fromValue(String value) {
        if (value == null) {
            return COSINE; // 默认值
        }
        
        for (ClickHouseSimilarityFunction function : values()) {
            if (function.functionName.equalsIgnoreCase(value) || 
                function.name().equalsIgnoreCase(value)) {
                return function;
            }
        }
        
        throw new IllegalArgumentException("Unsupported similarity function: " + value + 
            ". Supported values are: cosine, l2, l1, linf, dot_product");
    }
    
    /**
     * 判断是否为距离函数（值越小越相似）
     */
    public boolean isDistanceFunction() {
        return this != DOT_PRODUCT;
    }
    
    @Override
    public String toString() {
        return functionName;
    }
}
