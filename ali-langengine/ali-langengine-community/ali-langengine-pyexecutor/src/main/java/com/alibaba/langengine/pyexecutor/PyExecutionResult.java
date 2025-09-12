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

/**
 * Represents the result of a Python code execution.
 * <p>
 * This class is an immutable data holder that encapsulates all outcomes of an execution,
 * including raw text output (stdout, stderr), structured parsed values from the last
 * expression, and any error information. It also includes metadata like the exit code
 * and whether the output streams were truncated.
 */
public class PyExecutionResult {

    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final boolean stdoutTruncated;
    private final boolean stderrTruncated;
    private final long elapsedMs;

    // Structured parsing results
    private final String lastValueRepr; // repr of the last expression if enabled
    private final String errorRepr;     // error message if an exception was thrown

    /**
     * Constructs a new PyExecutionResult.
     *
     * @param exitCode        The exit code of the Python process.
     * @param stdout          The raw standard output, including any protocol lines.
     * @param stdoutTruncated True if the standard output was truncated.
     * @param stderr          The raw standard error output.
     * @param stderrTruncated True if the standard error was truncated.
     * @param elapsedMs       The execution time in milliseconds.
     * @param lastValueRepr   The string representation (`repr`) of the last evaluated expression.
     * @param errorRepr       A structured error message if an exception occurred.
     */
    public PyExecutionResult(int exitCode, String stdout, boolean stdoutTruncated,
                             String stderr, boolean stderrTruncated, long elapsedMs,
                             String lastValueRepr, String errorRepr) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
        this.stdoutTruncated = stdoutTruncated;
        this.stderrTruncated = stderrTruncated;
        this.elapsedMs = elapsedMs;
        this.lastValueRepr = lastValueRepr;
        this.errorRepr = errorRepr;
    }

    /**
     * Gets the exit code of the Python process. 0 typically indicates success.
     *
     * @return The process exit code.
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Gets the complete standard output from the execution.
     *
     * @return The stdout content, or null if empty.
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * Gets the complete standard error output from the execution.
     *
     * @return The stderr content, or null if empty.
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * Checks if the standard output was truncated due to size limits.
     *
     * @return true if stdout was truncated, false otherwise.
     */
    public boolean isStdoutTruncated() {
        return stdoutTruncated;
    }

    /**
     * Checks if the standard error was truncated due to size limits.
     *
     * @return true if stderr was truncated, false otherwise.
     */
    public boolean isStderrTruncated() {
        return stderrTruncated;
    }

    /**
     * Gets the execution time in milliseconds.
     *
     * @return The elapsed time in ms.
     */
    public long getElapsedMs() {
        return elapsedMs;
    }

    /**
     * Gets the Python `repr()` string of the last expression's value, if available.
     *
     * @return The string representation of the last value, or null.
     */
    public String getLastValueRepr() {
        return lastValueRepr;
    }

    /**
     * Gets a structured error message if the Python code raised an exception.
     *
     * @return The error representation string, or null if no error was reported.
     */
    public String getErrorRepr() {
        return errorRepr;
    }
}