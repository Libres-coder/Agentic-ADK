/*
 * Copyright 2024 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.cxxexecutor.autoconfig;

import com.alibaba.langengine.cxxexecutor.Backend;
import com.alibaba.langengine.cxxexecutor.CxxBackendDetector;
import com.alibaba.langengine.cxxexecutor.CxxExecutionPolicy;
import com.alibaba.langengine.cxxexecutor.CxxExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the CxxExecutor.
 * <p>
 * This class automatically configures the necessary beans for C++ code execution
 * when {@code CxxExecutor} is on the classpath. It uses properties defined in
 * {@link CxxExecutionProperties}.
 */
@AutoConfiguration
@ConditionalOnClass(CxxExecutor.class)
@EnableConfigurationProperties(CxxExecutionProperties.class)
public class CxxExecutorAutoConfiguration {

    /**
     * Provides a {@link CxxBackendDetector} bean if one is not already present.
     *
     * @return A new {@code CxxBackendDetector} instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public CxxBackendDetector cxxBackendDetector() {
        return new CxxBackendDetector();
    }

    /**
     * Provides a {@link CxxExecutionPolicy} bean configured from application properties.
     * <p>
     * This bean encapsulates the execution policy, which is determined by a combination
     * of auto-detection and explicit configuration properties.
     *
     * @param props    The configuration properties bound from the application environment.
     * @param detector The backend detector to use if no backend is explicitly configured.
     * @return A configured {@code CxxExecutionPolicy} instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public CxxExecutionPolicy cxxExecutionPolicy(CxxExecutionProperties props,
                                                 CxxBackendDetector detector) {
        CxxExecutionPolicy policy = new CxxExecutionPolicy();

        // Prefer the backend specified in properties; otherwise, auto-detect.
        Backend backend = props.getBackend() != null ? props.getBackend() : detector.detect();
        policy.setBackend(backend);

        // Configure security and resource limits.
        policy.setDisableNetwork(props.isDisableNetwork());
        policy.setMaxStdoutBytes(props.getMaxStdoutBytes());
        policy.setMaxStderrBytes(props.getMaxStderrBytes());
        policy.setCompileTimeoutMs(props.getCompileTimeoutMs());
        policy.setRunTimeoutMs(props.getRunTimeoutMs());
        policy.setHardKillGraceMs(props.getHardKillGraceMs());

        // Configure toolchain paths.
        if (props.getClangPath() != null) {
            policy.setClangPath(props.getClangPath());
        }
        if (props.getWasmtimePath() != null) {
            policy.setWasmtimePath(props.getWasmtimePath());
        }
        if (props.getWasiSysroot() != null) {
            policy.setWasiSysroot(props.getWasiSysroot());
        }
        if (props.getWorkDir() != null) {
            policy.setWorkDir(props.getWorkDir());
        }
        if (props.getNsjailPath() != null) {
            policy.setNsjailPath(props.getNsjailPath());
        }

        // Configure compilation flags.
        if (props.getExtraCompileFlags() != null && !props.getExtraCompileFlags().isEmpty()) {
            policy.setExtraCompileFlags(props.getExtraCompileFlags().toArray(String[]::new));
        }

        // Configure include patterns (optional).
        if (!props.getAllowedIncludePatterns().isEmpty()) {
            policy.setAllowedIncludePatterns(props.getAllowedIncludePatterns().toArray(String[]::new));
        }
        if (!props.getDeniedIncludePatterns().isEmpty()) {
            policy.setDeniedIncludePatterns(props.getDeniedIncludePatterns().toArray(String[]::new));
        }

        // Configure WASI-specific settings.
        if (props.getMaxWasmStackBytes() != null) {
            policy.setMaxWasmStackBytes(props.getMaxWasmStackBytes());
        }

        return policy;
    }

    /**
     * Provides a {@link CxxExecutor} bean if one is not already present.
     *
     * @return A new {@code CxxExecutor} instance.
     */
    @Bean
    @ConditionalOnMissingBean
    public CxxExecutor cxxExecutor() {
        return new CxxExecutor();
    }
}