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
 * 链接消息
 * 
 * @author langengine
 */
@Data
public class LinkMessage {
    
    private String msgtype = "link";
    private LinkContent link;
    
    @Data
    public static class LinkContent {
        private String title;
        private String text;
        private String messageUrl;
        private String picUrl;
    }
    
    public LinkMessage() {
        this.link = new LinkContent();
    }
    
    public void setTitle(String title) {
        this.link.setTitle(title);
    }
    
    public void setText(String text) {
        this.link.setText(text);
    }
    
    public void setMessageUrl(String messageUrl) {
        this.link.setMessageUrl(messageUrl);
    }
    
    public void setPicUrl(String picUrl) {
        this.link.setPicUrl(picUrl);
    }
}
