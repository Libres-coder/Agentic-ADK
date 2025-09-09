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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Detects the most suitable backend for C++ execution.
 * <p>
 * This class provides a non-intrusive mechanism to determine the preferred backend.
 * It prioritizes NSJAIL if it is available and runnable, falling back to WASI otherwise.
 */
public class CxxBackendDetector {

    /**
     * Detects and returns the recommended backend.
     *
     * @return The detected {@link Backend}, either {@code NSJAIL} or {@code WASI}.
     */
    public Backend detect() {
        if (isNsjailRunnable()) {
            return Backend.NSJAIL;
        }
        // If wasmtime is available in the system's PATH, assume WASI is usable.
        if (commandExists("wasmtime")) {
            return Backend.WASI;
        }
        return Backend.WASI;
    }

    /**
     * Checks if the nsjail sandbox is properly configured and executable.
     *
     * @return {@code true} if nsjail is available and runnable, {@code false} otherwise.
     */
    private boolean isNsjailRunnable() {
        String nsjailPathEnv = System.getenv("NSJAIL_PATH");
        Path nsjailBinary = null;

        if (nsjailPathEnv != null && !nsjailPathEnv.isBlank()) {
            nsjailBinary = Paths.get(nsjailPathEnv);
        }

        if (nsjailBinary == null || !Files.isExecutable(nsjailBinary)) {
            Path[] candidatePaths = new Path[]{
                    Paths.get("/usr/bin/nsjail"),
                    Paths.get("/usr/local/bin/nsjail")
            };
            for (Path candidate : candidatePaths) {
                if (Files.isExecutable(candidate)) {
                    nsjailBinary = candidate;
                    break;
                }
            }
        }

        if (nsjailBinary == null || !Files.isExecutable(nsjailBinary)) {
            return false;
        }

        // Check if unprivileged user namespaces are enabled, which is required by nsjail.
        Path usernsPath = Paths.get("/proc/sys/kernel/unprivileged_userns_clone");
        if (Files.isReadable(usernsPath)) {
            try {
                String usernsValue = Files.readString(usernsPath).trim();
                if ("0".equals(usernsValue)) {
                    return false;
                }
            } catch (Exception ignored) {
                // Ignore exceptions during file read.
            }
        }
        return true;
    }

    /**
     * Checks if a command exists in the system's PATH.
     *
     * @param command The command to check.
     * @return {@code true} if the command is found, {@code false} otherwise.
     */
    private boolean commandExists(String command) {
        try {
            Process process = new ProcessBuilder("bash", "-lc", "command -v " + command)
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}