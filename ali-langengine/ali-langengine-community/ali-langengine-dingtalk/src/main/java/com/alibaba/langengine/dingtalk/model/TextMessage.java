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
package com.alibaba.langengine.dingtalk.model;

import lombok.Data;

/**
 * 文本消息
 * 
 * @author langengine
 */
@Data
public class TextMessage {
    
    private String msgtype = "text";
    private TextContent text;
    
    @Data
    public static class TextContent {
        private String content;
    }
    
    public TextMessage() {
        this.text = new TextContent();
    }
    
    public void setContent(String content) {
        this.text.setContent(content);
    }
}
