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


import com.alibaba.agentic.computer.use.dto.DomCallbackRequest;
import com.alibaba.agentic.computer.use.service.BrowserUseServiceCaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/adk/browser/_callback")
@RestController
public class BrowserUseCallbackController {

    @Autowired
    private BrowserUseServiceCaller browserUseServiceCaller;

    @PostMapping("/dom")
    public void domCallback(@RequestBody DomCallbackRequest request ) {
//        log.warn("domCallback, fileName: {}, domJson: {}", request.getFileName(), request.getDomJson());
        browserUseServiceCaller.handleCallback(request.getFileName(), request.getDomJson());
    }

}
