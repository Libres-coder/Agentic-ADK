/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.computer.use.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ConditionalOnProperty(name = "ali.adk.browser.use.properties.enable", havingValue = "true")
@RestController
public class BrowserUseResourceController {

    @GetMapping("/container1")
    public ResponseEntity<String> restPage() throws IOException {
        Resource resource = new ClassPathResource("template/containerView.html");
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header("Content-Security-Policy", "frame-ancestors *")
                .header("X-Frame-Options", "")
                .body(content);
    }

    @GetMapping("/sdk/ASP/container.html")
    public ResponseEntity<String> restMobilePage() throws IOException {
        Resource resource = new ClassPathResource("static/alibaba/mobile/sdk/ASP/container.html");
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header("Content-Security-Policy", "frame-ancestors *")
                .header("X-Frame-Options", "")
                .body(content);
    }

    @GetMapping("/alibaba/desktop")
    public ResponseEntity<String> index() throws IOException {
        Resource resource = new ClassPathResource("static/alibaba/index.html");
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }

    @GetMapping("/alibaba/mobile")
    public ResponseEntity<String> index2() throws IOException {
        Resource resource = new ClassPathResource("static/alibaba/mobile/index.html");
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }


}
