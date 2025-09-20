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
 * Specifies the execution backend for C++ code.
 */
public enum Backend {
    /**
     * WebAssembly System Interface (WASI) backend.
     * <p>
     * This backend compiles C++ code to WebAssembly and runs it in a WASI-compatible runtime.
     * It provides a sandboxed environment with limited access to the host system.
     */
    WASI,

    /**
     * NsJail backend.
     * <p>
     * This backend uses the NsJail sandboxing tool to execute native C++ code.
     * It offers strong isolation by leveraging Linux namespaces and control groups.
     */
    NSJAIL
}