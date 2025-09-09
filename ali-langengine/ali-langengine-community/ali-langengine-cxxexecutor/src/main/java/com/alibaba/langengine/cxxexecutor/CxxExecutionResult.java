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

/**
 * Represents the result of a C++ code execution.
 * <p>
 * This class encapsulates the outcome of a compilation and execution cycle,
 * including exit codes, standard output/error, and performance metrics.
 * Use the {@link Builder} or static factory methods to construct an instance.
 */
public class CxxExecutionResult {

    private final boolean ok;
    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final boolean stdoutTruncated;
    private final boolean stderrTruncated;
    private final String phase;
    private final long compileMillis;
    private final long runMillis;

    /**
     * Private constructor to be used by the {@link Builder}.
     *
     * @param builder The builder instance.
     */
    private CxxExecutionResult(Builder builder) {
        this.ok = builder.ok;
        this.exitCode = builder.exitCode;
        this.stdout = builder.stdout;
        this.stderr = builder.stderr;
        this.stdoutTruncated = builder.stdoutTruncated;
        this.stderrTruncated = builder.stderrTruncated;
        this.phase = builder.phase;
        this.compileMillis = builder.compileMillis;
        this.runMillis = builder.runMillis;
    }

    /**
     * Checks if the execution was successful (i.e., exit code was 0).
     *
     * @return {@code true} if the execution was successful, {@code false} otherwise.
     */
    public boolean isOk() {
        return ok;
    }

    /**
     * Gets the exit code of the execution.
     *
     * @return The exit code.
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Gets the standard output of the execution.
     *
     * @return The standard output as a string.
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * Gets the standard error of the execution.
     *
     * @return The standard error as a string.
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * Checks if the standard output was truncated.
     *
     * @return {@code true} if stdout was truncated, {@code false} otherwise.
     */
    public boolean isStdoutTruncated() {
        return stdoutTruncated;
    }

    /**
     * Checks if the standard error was truncated.
     *
     * @return {@code true} if stderr was truncated, {@code false} otherwise.
     */
    public boolean isStderrTruncated() {
        return stderrTruncated;
    }

    /**
     * Gets the phase in which the execution terminated.
     *
     * @return The phase, which can be "compile", "run", or "error".
     */
    public String getPhase() {
        return phase;
    }

    /**
     * Gets the compilation time in milliseconds.
     *
     * @return The compilation time in milliseconds.
     */
    public long getCompileMillis() {
        return compileMillis;
    }

    /**
     * Gets the execution time in milliseconds.
     *
     * @return The execution time in milliseconds.
     */
    public long getRunMillis() {
        return runMillis;
    }

    /**
     * Creates a new builder for {@link CxxExecutionResult}.
     *
     * @return A new {@link Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a result for a compilation error.
     *
     * @param err         The error message.
     * @param truncated   Whether the error message was truncated.
     * @param compileMs   The compilation time in milliseconds.
     * @return A new {@link CxxExecutionResult} instance representing a compile error.
     */
    public static CxxExecutionResult compileError(String err, boolean truncated, long compileMs) {
        return new Builder()
                .ok(false)
                .exitCode(1)
                .stdout("")
                .stderr(err)
                .stderrTruncated(truncated)
                .phase("compile")
                .compileMillis(compileMs)
                .build();
    }

    /**
     * Creates a result for a successful or failed run.
     *
     * @param out         The standard output.
     * @param outTrunc    Whether the standard output was truncated.
     * @param err         The standard error.
     * @param errTrunc    Whether the standard error was truncated.
     * @param code        The exit code.
     * @param compileMs   The compilation time in milliseconds.
     * @param runMs       The execution time in milliseconds.
     * @return A new {@link CxxExecutionResult} instance.
     */
    public static CxxExecutionResult success(String out, boolean outTrunc,
                                             String err, boolean errTrunc,
                                             int code, long compileMs, long runMs) {
        return new Builder()
                .ok(code == 0)
                .exitCode(code)
                .stdout(out)
                .stderr(err)
                .stdoutTruncated(outTrunc)
                .stderrTruncated(errTrunc)
                .phase("run")
                .compileMillis(compileMs)
                .runMillis(runMs)
                .build();
    }

    /**
     * Creates a result for an internal error within the executor.
     *
     * @param msg The internal error message.
     * @return A new {@link CxxExecutionResult} instance representing an internal error.
     */
    public static CxxExecutionResult internalError(String msg) {
        return new Builder()
                .ok(false)
                .exitCode(-1)
                .stderr(msg)
                .phase("error")
                .build();
    }

    /**
     * Builder for creating {@link CxxExecutionResult} instances.
     */
    public static class Builder {
        private boolean ok;
        private int exitCode;
        private String stdout;
        private String stderr;
        private boolean stdoutTruncated;
        private boolean stderrTruncated;
        private String phase;
        private long compileMillis;
        private long runMillis;

        private Builder() {}

        /**
         * Sets the success status.
         *
         * @param ok {@code true} if successful.
         * @return This builder instance.
         */
        public Builder ok(boolean ok) {
            this.ok = ok;
            return this;
        }

        /**
         * Sets the exit code.
         *
         * @param exitCode The exit code.
         * @return This builder instance.
         */
        public Builder exitCode(int exitCode) {
            this.exitCode = exitCode;
            return this;
        }

        /**
         * Sets the standard output.
         *
         * @param stdout The standard output.
         * @return This builder instance.
         */
        public Builder stdout(String stdout) {
            this.stdout = stdout;
            return this;
        }

        /**
         * Sets the standard error.
         *
         * @param stderr The standard error.
         * @return This builder instance.
         */
        public Builder stderr(String stderr) {
            this.stderr = stderr;
            return this;
        }

        /**
         * Sets whether the standard output was truncated.
         *
         * @param stdoutTruncated {@code true} if truncated.
         * @return This builder instance.
         */
        public Builder stdoutTruncated(boolean stdoutTruncated) {
            this.stdoutTruncated = stdoutTruncated;
            return this;
        }

        /**
         * Sets whether the standard error was truncated.
         *
         * @param stderrTruncated {@code true} if truncated.
         * @return This builder instance.
         */
        public Builder stderrTruncated(boolean stderrTruncated) {
            this.stderrTruncated = stderrTruncated;
            return this;
        }

        /**
         * Sets the execution phase.
         *
         * @param phase The phase.
         * @return This builder instance.
         */
        public Builder phase(String phase) {
            this.phase = phase;
            return this;
        }

        /**
         * Sets the compilation time.
         *
         * @param compileMillis Time in milliseconds.
         * @return This builder instance.
         */
        public Builder compileMillis(long compileMillis) {
            this.compileMillis = compileMillis;
            return this;
        }

        /**
         * Sets the execution time.
         *
         * @param runMillis Time in milliseconds.
         * @return This builder instance.
         */
        public Builder runMillis(long runMillis) {
            this.runMillis = runMillis;
            return this;
        }

        /**
         * Builds the {@link CxxExecutionResult}.
         *
         * @return A new {@link CxxExecutionResult} instance.
         */
        public CxxExecutionResult build() {
            return new CxxExecutionResult(this);
        }
    }
}