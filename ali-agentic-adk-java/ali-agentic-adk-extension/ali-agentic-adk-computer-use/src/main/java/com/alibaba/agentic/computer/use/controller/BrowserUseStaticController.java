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

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class BrowserUseStaticController {

    @GetMapping("/alibaba/desktop/{filename:.+}")
    public ResponseEntity<Resource> serveAlibabaResource(@PathVariable String filename) throws IOException {
        // 假设静态资源位于 classpath:/static/alibaba/ 目录下
        Resource resource = new ClassPathResource("static/alibaba/" + filename);

        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(getMediaTypeForFileName(filename))
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/alibaba/mobile/{filename:.+}")
    public ResponseEntity<Resource> mobileServeAlibabaResource(@PathVariable String filename) throws IOException {
        // 假设静态资源位于 classpath:/static/alibaba/ 目录下
        Resource resource = new ClassPathResource("static/alibaba/mobile/" + filename);

        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(getMediaTypeForFileName(filename))
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private MediaType getMediaTypeForFileName(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "css":
                return MediaType.valueOf("text/css");
            case "js":
                return MediaType.valueOf("application/javascript");
            case "png":
                return MediaType.IMAGE_PNG;
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            // 添加更多文件类型
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

}
