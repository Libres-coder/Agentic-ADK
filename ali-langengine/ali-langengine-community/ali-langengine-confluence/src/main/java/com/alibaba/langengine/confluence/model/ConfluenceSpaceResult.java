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
package com.alibaba.langengine.confluence.model;

import lombok.Data;

import java.util.List;

/**
 * Confluence空间结果模型
 * 
 * @author AIDC-AI
 */
@Data
public class ConfluenceSpaceResult {
    
    /**
     * 空间列表
     */
    private List<ConfluenceSpace> spaces;
    
    /**
     * 构造函数
     */
    public ConfluenceSpaceResult() {
    }
    
    /**
     * 构造函数
     * 
     * @param spaces 空间列表
     */
    public ConfluenceSpaceResult(List<ConfluenceSpace> spaces) {
        this.spaces = spaces;
    }
}
