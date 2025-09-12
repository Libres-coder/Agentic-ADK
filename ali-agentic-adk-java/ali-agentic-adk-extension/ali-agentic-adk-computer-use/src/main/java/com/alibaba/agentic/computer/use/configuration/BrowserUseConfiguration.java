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
package com.alibaba.agentic.computer.use.configuration;

import com.alibaba.agentic.computer.use.utils.StringBasedLetterSnowflake;
import com.google.adk.artifacts.InMemoryArtifactService;
import com.google.adk.sessions.BaseSessionService;
import com.google.adk.sessions.InMemorySessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@ConditionalOnProperty(name = "ali.adk.browser.use.properties.enable", havingValue = "true")
@Configuration
public class BrowserUseConfiguration {

    @Autowired
    private AdkBrowserUseProperties aliAdkProperties;

    @Bean
    public com.aliyun.ecd20200930.Client client() {
        try {
            return createClient(aliAdkProperties.getEndpoint());
        } catch (Exception e) {
            log.error("init com.aliyun.ecd20200930.Client error", e);
            return null;
        }
    }

    @Bean
    public com.aliyun.ecd20200930.Client clientJp() {
        try {
            return createClient("ecd.ap-northeast-1.aliyuncs.com");
        } catch (Exception e) {
            log.error("init com.aliyun.ecd20200930.Client error", e);
            return null;
        }
    }

    @Bean
    public com.aliyun.ecd20201002.Client client1002() {
        try {
            return createClient1002(aliAdkProperties.getEndpoint());
        } catch (Exception e) {
            log.error("init com.aliyun.ecd20200930.Client error", e);
            return null;
        }
    }

    @ConditionalOnProperty(name = "ali.adk.browser.use.properties.mobileEndPoint")
    @Bean
    public com.aliyun.appstream_center20210218.Client appStreamClient() {
        try {
            return createAppStreamClient(aliAdkProperties.getAppStreamEndPoint());
        } catch (Exception e) {
            log.error("init com.aliyun.ecd20200930.Client error", e);
            return null;
        }
    }

    @ConditionalOnProperty(name = "ali.adk.browser.use.properties.appStreamEndPoint")
    @Bean
    public com.aliyun.eds_aic20230930.Client mobileClient() {
        try {
            return createMobileClient(aliAdkProperties.getMobileEndPoint());
        } catch (Exception e) {
            log.error("init com.aliyun.ecd20200930.Client error", e);
            return null;
        }
    }

    @Bean
    @ConditionalOnProperty(name = "ali.adk.browser.use.properties.computerResourceId")
    public StringBasedLetterSnowflake simplifiedLetterSnowflake() {
        return new StringBasedLetterSnowflake(aliAdkProperties.getComputerResourceId());
    }

    private com.aliyun.ecd20200930.Client createClient(String endPoint) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(aliAdkProperties.getAk())
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(aliAdkProperties.getSk());
        config.endpoint = endPoint;
        return new com.aliyun.ecd20200930.Client(config);
    }

    private com.aliyun.ecd20201002.Client createClient1002(String endPoint) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(aliAdkProperties.getAk())
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(aliAdkProperties.getSk());
        config.endpoint = endPoint;
        return new com.aliyun.ecd20201002.Client(config);
    }

    private com.aliyun.appstream_center20210218.Client createAppStreamClient(String endPoint) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(aliAdkProperties.getAk())
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(aliAdkProperties.getSk());
        config.endpoint = endPoint;

        return new com.aliyun.appstream_center20210218.Client(config);
    }

    private com.aliyun.eds_aic20230930.Client createMobileClient(String endPoint) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(aliAdkProperties.getAk())
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(aliAdkProperties.getSk());
        config.endpoint = endPoint;
        return new com.aliyun.eds_aic20230930.Client(config);
    }


    @Bean
    public BrowserRunnerService browserRunnerService(@Qualifier("baseSessionService") BaseSessionService baseSessionService) {
        return new BrowserRunnerService(new InMemoryArtifactService(), baseSessionService);
    }

    @ConditionalOnMissingBean(name = "baseSessionService")
    @Bean
    public BaseSessionService baseSessionService() {
        return new InMemorySessionService();
    }


}
