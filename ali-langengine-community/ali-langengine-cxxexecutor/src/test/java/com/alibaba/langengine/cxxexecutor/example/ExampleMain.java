/*
 * Copyright 2024 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.cxxexecutor.example;

import com.alibaba.langengine.cxxexecutor.Backend;
import com.alibaba.langengine.cxxexecutor.CxxExecutionOptions;
import com.alibaba.langengine.cxxexecutor.CxxExecutionPolicy;
import com.alibaba.langengine.cxxexecutor.CxxExecutionResult;
import com.alibaba.langengine.cxxexecutor.CxxExecutor;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An example demonstrating the use of {@link CxxExecutor} to run a C++ quicksort algorithm.
 */
public class ExampleMain {

    /**
     * The main entry point for the example application.
     * <p>
     * This method sets up a WASI execution environment, defines a C++ quicksort implementation,
     * executes it using the CxxExecutor, and prints the result to the console.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        String home = System.getProperty("user.home");
        String sdk = System.getenv().getOrDefault("WASI_SDK_PATH", "/opt/wasi-sdk");
        String sysroot = sdk + "/share/wasi-sysroot";
        String clangpp = sdk + "/bin/clang++";
        String wasmtime = System.getenv().getOrDefault("WASMTIME_PATH", home + "/.wasmtime/bin/wasmtime");

        if (!Files.exists(Path.of(clangpp))) {
            System.err.println("SKIPPING: WASI clang++ not found at: " + clangpp);
            return;
        }
        if (!Files.exists(Path.of(sysroot))) {
            System.err.println("SKIPPING: WASI sysroot not found at: " + sysroot);
            return;
        }
        if (!Files.exists(Path.of(wasmtime))) {
            System.err.println("SKIPPING: wasmtime not found at: " + wasmtime);
            return;
        }

        CxxExecutionPolicy policy = new CxxExecutionPolicy();
        policy.setBackend(Backend.WASI);
        policy.setClangPath(clangpp);
        policy.setWasiSysroot(sysroot);
        policy.setWasmtimePath(wasmtime);
        policy.setCompileTimeoutMs(8000);
        policy.setRunTimeoutMs(1000);
        policy.setExtraCompileFlags(new String[]{
                "-O0", "-std=c++17",
                "-fno-exceptions"
        });

        // C++ source code for a quicksort algorithm.
        String code = String.join("\n",
                "#include <iostream>",
                "#include <vector>",
                "#include <algorithm>",
                "using namespace std;",
                "",
                "int partition(vector<int>& arr, int low, int high) {",
                "    int pivot = arr[high];",
                "    int i = low - 1;",
                "    for (int j = low; j < high; j++) {",
                "        if (arr[j] <= pivot) {",
                "            i++;",
                "            swap(arr[i], arr[j]);",
                "        }",
                "    }",
                "    swap(arr[i + 1], arr[high]);",
                "    return i + 1;",
                "}",
                "",
                "void quickSort(vector<int>& arr, int low, int high) {",
                "    if (low < high) {",
                "        int pi = partition(arr, low, high);",
                "        quickSort(arr, low, pi - 1);",
                "        quickSort(arr, pi + 1, high);",
                "    }",
                "}",
                "",
                "int main() {",
                "    vector<int> arr = {10, 7, 8, 9, 1, 5};",
                "    quickSort(arr, 0, arr.size() - 1);",
                "    for (int x : arr) cout << x << ' ';",
                "    cout << \"\\n\";",
                "    return 0;",
                "}"
        );

        CxxExecutor executor = new CxxExecutor();
        CxxExecutionOptions opts = CxxExecutionOptions.builder()
                .code(code)
                .isCpp(true)
                .policy(policy)
                .build();

        CxxExecutionResult result = executor.executeOnce(opts);

        System.out.println("=== WASI QuickSort Example ===");
        System.out.println("Phase        : " + result.getPhase());
        System.out.println("Exit Code    : " + result.getExitCode());
        System.out.println("Success      : " + result.isOk());
        System.out.println("Stdout       :\n" + result.getStdout());
        System.out.println("Stderr       :\n" + result.getStderr());
    }
}