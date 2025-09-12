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

package com.alibaba.langengine.pyexecutor.spring;

import com.alibaba.langengine.pyexecutor.PyExecutor;
import com.alibaba.langengine.pyexecutor.PyExecutionPolicy;
import com.alibaba.langengine.pyexecutor.SessionConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the PyExecutor.
 * <p>
 * This class enables the automatic setup of the {@link PyExecutor} bean within a Spring
 * application context. It binds the external configuration from {@link PyExecutorProperties}
 * and uses it to construct the executor. This auto-configuration is triggered when
 * the library is on the classpath and no other {@code PyExecutor} bean has been manually
 * defined.
 * <p>
 * This library does not provide any controllers; it only handles the parameter mapping
 * and bean creation.
 */
@AutoConfiguration
@EnableConfigurationProperties(PyExecutorProperties.class)
public class PyExecutorAutoConfiguration {

    /**
     * Creates a {@link PyExecutor} bean if one does not already exist in the context.
     * <p>
     * This method uses the properties loaded into {@link PyExecutorProperties} to create
     * configured instances of {@link PyExecutionPolicy} and {@link SessionConfig},
     * which are then used to instantiate the {@code PyExecutor}.
     *
     * @param props The configuration properties bound from the application's environment.
     * @return A configured instance of {@code PyExecutor}.
     */
    @Bean
    @ConditionalOnMissingBean
    public PyExecutor pyExecutor(PyExecutorProperties props) {
        return new PyExecutor(props.toPolicy(), props.toSessionConfig());
    }
}