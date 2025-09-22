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
package com.alibaba.langengine.vald.vectorstore.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValdSearchRequest {

    /**
     * 查询向量
     */
    private List<Double> vector;

    /**
     * 返回的近邻数量
     */
    private int k;

    /**
     * 最大距离阈值（可选）
     */
    private Double maxDistance;

    /**
     * 搜索半径（可选）
     */
    private Double radius;

    /**
     * 搜索精度参数（可选）
     */
    private Double epsilon;

    public ValdSearchRequest(List<Double> vector, int k) {
        this.vector = vector;
        this.k = k;
    }

    public ValdSearchRequest(List<Double> vector, int k, Double maxDistance) {
        this.vector = vector;
        this.k = k;
        this.maxDistance = maxDistance;
    }

}