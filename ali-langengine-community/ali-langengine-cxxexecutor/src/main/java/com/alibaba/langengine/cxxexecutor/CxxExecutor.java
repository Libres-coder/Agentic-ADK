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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Provides functionality to securely compile and execute C/C++ code.
 */
public class CxxExecutor {

    /**
     * Executes a single C/C++ code request based on the provided options.
     * <p>
     * This method orchestrates the entire lifecycle of a code execution, including:
     * 1. Pre-flight checks (e.g., include guards).
     * 2. Setting up a temporary working directory.
     * 3. Compiling the source code using the specified backend (WASI or NsJail).
     * 4. Running the compiled artifact in a sandboxed environment.
     * 5. Cleaning up the temporary directory.
     *
     * @param opt The {@link CxxExecutionOptions} containing the source code and execution policy.
     * @return A {@link CxxExecutionResult} with the outcome of the execution.
     */
    public CxxExecutionResult executeOnce(CxxExecutionOptions opt) {
        Objects.requireNonNull(opt, "Options must not be null.");
        CxxExecutionPolicy policy = Objects.requireNonNull(opt.getPolicy(), "Policy must not be null.");
        String code = Objects.requireNonNull(opt.getCode(), "Source code must not be null.");

        // Pre-check: validate #include directives against the policy's whitelist/blacklist.
        try {
            SourceGuards.checkIncludes(code, policy.getAllowedIncludePatterns(), policy.getDeniedIncludePatterns());
        } catch (IllegalArgumentException ex) {
            return CxxExecutionResult.compileError(ex.getMessage(), false, 0);
        }

        Path baseDir = Paths.get(Optional.ofNullable(policy.getWorkDir()).orElse("/tmp/cxxexec"));
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            return CxxExecutionResult.internalError("Cannot create working directory: " + e.getMessage());
        }

        Path workDir = null;
        try {
            workDir = Files.createTempDirectory(baseDir, "cxx_");
            String extension = opt.isCpp() ? ".cpp" : ".c";
            String filename = Optional.ofNullable(opt.getFilenameHint()).orElse("main") + extension;
            Path srcPath = workDir.resolve(filename);
            Files.writeString(srcPath, code, StandardCharsets.UTF_8);

            // 1) Compilation Phase
            ProcessUtils.ExecOut compileOutput;
            long compileMs;
            if (policy.getBackend() == Backend.WASI) {
                compileOutput = WasmToolchain.compileWasi(
                        workDir, policy.getClangPath(), policy.getWasiSysroot(),
                        opt.isCpp(), srcPath.getFileName().toString(), policy.getExtraCompileFlags(),
                        policy.getCompileTimeoutMs(), policy.getMaxStdoutBytes(), policy.getMaxStderrBytes(),
                        policy.getHardKillGraceMs());
            } else {
                List<String> compileCommand = NsjailSandbox.compileNativeCmd(policy, srcPath.getFileName().toString());
                compileOutput = ProcessUtils.exec(compileCommand, workDir, null, opt.getEnv(), policy.getCompileTimeoutMs(),
                        policy.getMaxStdoutBytes(), policy.getMaxStderrBytes(), policy.getHardKillGraceMs());
            }
            compileMs = compileOutput.millis;
            if (compileOutput.code != 0) {
                return CxxExecutionResult.compileError(compileOutput.err, compileOutput.errTrunc, compileMs);
            }

            // 2) Execution Phase
            ProcessUtils.ExecOut runOutput;
            if (policy.getBackend() == Backend.WASI) {
                runOutput = WasmToolchain.runWasi(
                        workDir,
                        policy.getWasmtimePath(),
                        opt.getStdin(),
                        policy.getRunTimeoutMs(),
                        policy.getMaxStdoutBytes(),
                        policy.getMaxStderrBytes(),
                        policy.getHardKillGraceMs(),
                        opt.getEnv(),
                        policy.getMaxWasmStackBytes()
                );
            } else {
                List<String> runCommand = NsjailSandbox.wrapRun(policy, workDir, "/work/a.out");
                runOutput = ProcessUtils.exec(runCommand, workDir, opt.getStdin(), opt.getEnv(), policy.getRunTimeoutMs(),
                        policy.getMaxStdoutBytes(), policy.getMaxStderrBytes(), policy.getHardKillGraceMs());
            }

            return CxxExecutionResult.success(
                    runOutput.out, runOutput.outTrunc,
                    runOutput.err, runOutput.errTrunc,
                    runOutput.code, compileMs, runOutput.millis
            );
        } catch (TimeoutException te) {
            return CxxExecutionResult.internalError("Execution timed out: " + te.getMessage());
        } catch (Exception e) {
            return CxxExecutionResult.internalError("An internal error occurred: " + e.getMessage());
        } finally {
            if (workDir != null) {
                try {
                    deleteRecursively(workDir);
                } catch (IOException ignored) {
                    // Ignored.
                }
            }
        }
    }

    /**
     * Recursively deletes a directory and its contents.
     *
     * @param path The path to the directory to be deleted.
     * @throws IOException if an I/O error occurs.
     */
    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // Ignored.
                }
            });
        }
    }
}