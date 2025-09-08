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

package com.alibaba.langengine.pyexecutor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defines optional, per-execution overrides for a Python execution call.
 * <p>
 * These options allow for temporary adjustments to the global {@link PyExecutionPolicy}
 * for a single execution, catering to user- привычки (user habits) or specific needs
 * of a particular code snippet. This includes setting a temporary working directory,
 * injecting environment variables, or overriding the policy for printing the last expression.
 */
public class PyExecutionOptions {

    /**
     * Specifies a working directory for the execution.
     * It can be an absolute path or a relative path. If relative, resolution is handled
     * by the executor (see {@code PyExecutor.executeOnce}).
     */
    private String workingDirRelative;

    /**
     * A map of extra environment variables to inject into the Python process.
     * These take precedence over any default environment variables set by the policy.
     */
    private final Map<String, String> extraEnv = new LinkedHashMap<>();

    /**
     * Temporarily overrides the global policy on whether to print the value of the
     * last expression in the script.
     */
    private Boolean printLastExpression;

    // =================================================================
    // Getters and Setters
    // =================================================================

    /**
     * Gets the relative working directory path.
     *
     * @return The relative working directory string.
     */
    public String getWorkingDirRelative() {
        return workingDirRelative;
    }

    /**
     * Sets the relative working directory for this execution.
     *
     * @param workingDirRelative The relative path of the working directory.
     * @return this {@code PyExecutionOptions} instance for chaining.
     */
    public PyExecutionOptions setWorkingDirRelative(String workingDirRelative) {
        this.workingDirRelative = workingDirRelative;
        return this;
    }

    /**
     * Gets the map of extra environment variables.
     *
     * @return A map containing the extra environment variables.
     */
    public Map<String, String> getExtraEnv() {
        return extraEnv;
    }

    /**
     * Adds or updates an environment variable for this execution.
     *
     * @param key The name of the environment variable.
     * @param val The value of the environment variable.
     * @return this {@code PyExecutionOptions} instance for chaining.
     */
    public PyExecutionOptions putEnv(String key, String val) {
        this.extraEnv.put(key, val);
        return this;
    }

    /**
     * Gets the temporary override for the "print last expression" setting.
     *
     * @return A {@code Boolean} which is {@code true} if printing is forced, {@code false} if suppressed,
     * or {@code null} if the global policy default should be used.
     */
    public Boolean getPrintLastExpression() {
        return printLastExpression;
    }

    /**
     * Sets the temporary override for the "print last expression" setting.
     *
     * @param printLastExpression {@code true} to force printing, {@code false} to suppress,
     * or {@code null} to use the global policy default.
     * @return this {@code PyExecutionOptions} instance for chaining.
     */
    public PyExecutionOptions setPrintLastExpression(Boolean printLastExpression) {
        this.printLastExpression = printLastExpression;
        return this;
    }
}