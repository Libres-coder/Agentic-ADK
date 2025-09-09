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

package com.alibaba.langengine.cxxexecutor;

import java.util.Map;

/**
 * Represents the options for a single C++ code execution request.
 * <p>
 * This class encapsulates all the necessary parameters for executing a piece of C++ code,
 * including the source code, execution policy, standard input, and environment variables.
 * Use the {@link Builder} to construct an instance.
 */
public class CxxExecutionOptions {

    private final String code;
    private final boolean isCpp;
    private final String stdin;
    private final Map<String, String> env;
    private final String filenameHint;
    private final CxxExecutionPolicy policy;

    /**
     * Private constructor to be used by the {@link Builder}.
     *
     * @param builder The builder instance.
     */
    private CxxExecutionOptions(Builder builder) {
        this.code = builder.code;
        this.isCpp = builder.isCpp;
        this.stdin = builder.stdin;
        this.env = builder.env;
        this.filenameHint = builder.filenameHint;
        this.policy = builder.policy;
    }

    /**
     * Gets the source code to be executed.
     *
     * @return The source code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Checks if the code is C++ or C.
     *
     * @return {@code true} if the code is C++, {@code false} if it is C.
     */
    public boolean isCpp() {
        return isCpp;
    }

    /**
     * Gets the standard input to be provided to the execution.
     *
     * @return The standard input string, or {@code null} if not provided.
     */
    public String getStdin() {
        return stdin;
    }

    /**
     * Gets the environment variables for the execution.
     *
     * @return A map of environment variables, or {@code null} if not provided.
     */
    public Map<String, String> getEnv() {
        return env;
    }

    /**
     * Gets the hint for the source file name.
     *
     * @return The filename hint, used for naming temporary files or logging.
     */
    public String getFilenameHint() {
        return filenameHint;
    }

    /**
     * Gets the execution policy.
     *
     * @return The {@link CxxExecutionPolicy} to be applied.
     */
    public CxxExecutionPolicy getPolicy() {
        return policy;
    }

    /**
     * Creates a new builder for {@link CxxExecutionOptions}.
     *
     * @return A new {@link Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating {@link CxxExecutionOptions} instances.
     */
    public static class Builder {
        private String code;
        private boolean isCpp;
        private String stdin;
        private Map<String, String> env;
        private String filenameHint;
        private CxxExecutionPolicy policy;

        private Builder() {}

        /**
         * Sets the source code.
         *
         * @param code The source code to execute.
         * @return This builder instance.
         */
        public Builder code(String code) {
            this.code = code;
            return this;
        }

        /**
         * Sets whether the code is C++ or C.
         *
         * @param isCpp {@code true} for C++, {@code false} for C. Defaults to {@code false}.
         * @return This builder instance.
         */
        public Builder isCpp(boolean isCpp) {
            this.isCpp = isCpp;
            return this;
        }

        /**
         * Sets the standard input for the execution.
         *
         * @param stdin The string to be used as standard input.
         * @return This builder instance.
         */
        public Builder stdin(String stdin) {
            this.stdin = stdin;
            return this;
        }

        /**
         * Sets the environment variables for the execution.
         *
         * @param env A map of environment variables.
         * @return This builder instance.
         */
        public Builder env(Map<String, String> env) {
            this.env = env;
            return this;
        }

        /**
         * Sets a hint for the filename, which can be used for temporary file creation.
         *
         * @param filenameHint A string to be used as a filename hint.
         * @return This builder instance.
         */
        public Builder filenameHint(String filenameHint) {
            this.filenameHint = filenameHint;
            return this;
        }

        /**
         * Sets the execution policy.
         *
         * @param policy The {@link CxxExecutionPolicy} to apply.
         * @return This builder instance.
         */
        public Builder policy(CxxExecutionPolicy policy) {
            this.policy = policy;
            return this;
        }

        /**
         * Builds and returns a new {@link CxxExecutionOptions} instance.
         *
         * @return A new {@link CxxExecutionOptions} instance.
         */
        public CxxExecutionOptions build() {
            return new CxxExecutionOptions(this);
        }
    }
}