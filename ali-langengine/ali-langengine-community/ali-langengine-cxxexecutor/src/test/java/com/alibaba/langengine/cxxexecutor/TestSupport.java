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

import org.junit.jupiter.api.Assumptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A utility class providing common setup and policies for JUnit tests.
 * <p>
 * This class includes methods to create pre-configured execution policies
 * for different backends, automatically skipping tests if the required
 * dependencies (like WASI SDK or NsJail) are not available.
 */
class TestSupport {

    /**
     * Creates a {@link CxxExecutionPolicy} for the WASI backend.
     * <p>
     * This method checks for the existence of the WASI SDK and wasmtime runtime.
     * If they are not found, the calling test will be skipped.
     *
     * @return A configured {@code CxxExecutionPolicy} for WASI tests.
     */
    static CxxExecutionPolicy wasiPolicyOrSkip() {
        String home = System.getProperty("user.home");
        String sdk = System.getenv().getOrDefault("WASI_SDK_PATH", "/opt/wasi-sdk");
        String sysroot = sdk + "/share/wasi-sysroot";
        String clangpp = sdk + "/bin/clang++";
        String wasmtime = System.getenv().getOrDefault("WASMTIME_PATH", home + "/.wasmtime/bin/wasmtime");

        Assumptions.assumeTrue(Files.exists(Path.of(clangpp)), "WASI clang++ not found, skipping test: " + clangpp);
        Assumptions.assumeTrue(Files.exists(Path.of(sysroot)), "WASI sysroot not found, skipping test: " + sysroot);
        Assumptions.assumeTrue(Files.exists(Path.of(wasmtime)), "Wasmtime not found, skipping test: " + wasmtime);

        CxxExecutionPolicy p = new CxxExecutionPolicy();
        p.setBackend(Backend.WASI);
        p.setClangPath(clangpp);
        p.setWasiSysroot(sysroot);
        p.setWasmtimePath(wasmtime);
        p.setCompileTimeoutMs(8000);
        p.setRunTimeoutMs(1500);
        p.setMaxStdoutBytes(64 * 1024);
        p.setMaxStderrBytes(16 * 1024);
        return p;
    }

    /**
     * Creates a {@link CxxExecutionPolicy} for the NsJail backend.
     * <p>
     * This method checks for the NsJail executable and verifies that user namespaces are enabled.
     * If the environment is not suitable for NsJail, the calling test will be skipped.
     *
     * @return A configured {@code CxxExecutionPolicy} for NsJail tests.
     */
    static CxxExecutionPolicy nsjailPolicyOrSkip() {
        // 1) Find the nsjail executable.
        String env = System.getenv("NSJAIL_PATH");
        Path bin = null;
        if (env != null && !env.isBlank()) {
            bin = Paths.get(env);
        }
        if (bin == null || !Files.isExecutable(bin)) {
            Path[] candidates = new Path[]{
                    Paths.get("/usr/bin/nsjail"),
                    Paths.get("/usr/local/bin/nsjail")
            };
            for (Path c : candidates) {
                if (Files.isExecutable(c)) {
                    bin = c;
                    break;
                }
            }
        }
        Assumptions.assumeTrue(bin != null && Files.isExecutable(bin), "NsJail executable not found, skipping test.");

        // 2) Check if user namespaces are enabled.
        Path userns = Paths.get("/proc/sys/kernel/unprivileged_userns_clone");
        if (Files.isReadable(userns)) {
            try {
                String v = Files.readString(userns).trim();
                Assumptions.assumeTrue(!"0".equals(v), "User namespaces are disabled, skipping NsJail test.");
            } catch (Exception ignored) {
                // Ignore failure to read the file.
            }
        }

        // 3) Assemble the policy.
        CxxExecutionPolicy p = new CxxExecutionPolicy();
        p.setBackend(Backend.NSJAIL);
        p.setClangPath("clang++");
        p.setNsjailPath(bin.toString());
        p.setCompileTimeoutMs(8000);
        p.setRunTimeoutMs(1000);
        p.setMaxStdoutBytes(64 * 1024);
        p.setMaxStderrBytes(16 * 1024);
        // This flag is important to prevent the compiler from optimizing away infinite loops,
        // ensuring that timeout tests work as expected.
        p.setExtraCompileFlags(new String[]{"-O0"});

        return p;
    }
}