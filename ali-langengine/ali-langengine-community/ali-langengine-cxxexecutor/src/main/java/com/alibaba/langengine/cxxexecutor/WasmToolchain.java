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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A utility class for compiling and running C/C++ code using a WASI-based toolchain.
 * <p>
 * This class encapsulates the logic for invoking the clang compiler to produce a
 * {@code .wasm} file and then executing it using the wasmtime runtime.
 */
class WasmToolchain {

    /**
     * Compiles C/C++ source code into a WebAssembly module using a WASI-compatible clang.
     *
     * @param work        The working directory where the compilation will take place.
     * @param clang       The path to the clang/clang++ executable (typically from the WASI SDK).
     * @param sysroot     The path to the WASI sysroot.
     * @param isCpp       {@code true} if the source is C++, {@code false} if it's C.
     * @param srcFile     The name of the source file to compile.
     * @param extraFlags  Additional command-line flags for the compiler.
     * @param timeoutMs   The timeout for the compilation process in milliseconds.
     * @param maxOut      The maximum size of standard output in bytes.
     * @param maxErr      The maximum size of standard error in bytes.
     * @param grace       The grace period for forceful termination after a timeout.
     * @return An {@link ProcessUtils.ExecOut} object containing the result of the compilation.
     * @throws Exception if an error occurs during the execution.
     */
    static ProcessUtils.ExecOut compileWasi(Path work, String clang, String sysroot,
                                            boolean isCpp, String srcFile, String[] extraFlags,
                                            int timeoutMs, long maxOut, long maxErr, long grace)
            throws Exception {
        List<String> cc = new ArrayList<>();
        // Use the clang/clang++ from the WASI SDK, which includes its own sysroot.
        cc.add(clang);
        cc.add("--target=wasm32-wasi");
        if (sysroot != null && !sysroot.isBlank()) {
            cc.add("--sysroot=" + sysroot);
        }
        cc.addAll(List.of("-O2", "-s"));
        if (extraFlags != null) {
            cc.addAll(Arrays.asList(extraFlags));
        }
        cc.add("-o");
        cc.add("prog.wasm");
        cc.add(srcFile);

        return ProcessUtils.exec(cc, work, null, null, timeoutMs, maxOut, maxErr, grace);
    }

    /**
     * Runs a compiled WebAssembly module using the wasmtime runtime.
     *
     * @param work               The working directory where the module is located.
     * @param wasmtime           The path to the wasmtime executable.
     * @param stdin              The standard input to be passed to the WebAssembly module.
     * @param timeoutMs          The timeout for the execution in milliseconds.
     * @param maxOut             The maximum size of standard output in bytes.
     * @param maxErr             The maximum size of standard error in bytes.
     * @param grace              The grace period for forceful termination after a timeout.
     * @param env                Environment variables to be passed to the guest module.
     * @param maxWasmStackBytes The maximum stack size for the WASM guest in bytes.
     * @return An {@link ProcessUtils.ExecOut} object containing the result of the execution.
     * @throws Exception if an error occurs during the execution.
     */
    static ProcessUtils.ExecOut runWasi(
            Path work, String wasmtime, String stdin,
            int timeoutMs, long maxOut, long maxErr, long grace,
            Map<String, String> env, Integer maxWasmStackBytes
    ) throws Exception {
        List<String> run = new ArrayList<>();
        run.add(wasmtime);

        // Explicitly use the 'run' subcommand for consistency across different wasmtime versions.
        run.add("run");

        // WASM-related flags must be specified before the .wasm file path.
        if (maxWasmStackBytes != null && maxWasmStackBytes > 0) {
            run.add("-W");
            run.add("max-wasm-stack=" + maxWasmStackBytes);
        }

        // Pass environment variables to the guest module.
        if (env != null) {
            for (Map.Entry<String, String> e : env.entrySet()) {
                run.add("--env");
                run.add(e.getKey() + "=" + (e.getValue() == null ? "" : e.getValue()));
            }
        }

        // Mount the working directory for file system access.
        run.add("--dir=.");

        // The path to the WASM module must be specified last.
        run.add("prog.wasm");

        return ProcessUtils.exec(run, work, stdin, null, timeoutMs, maxOut, maxErr, grace);
    }
}