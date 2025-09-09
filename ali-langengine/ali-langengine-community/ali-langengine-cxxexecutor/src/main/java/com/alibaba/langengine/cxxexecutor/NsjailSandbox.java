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
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for creating and managing NsJail sandboxed environments.
 * <p>
 * This class handles the dynamic generation of NsJail configurations and
 * the construction of command-line arguments for compiling and running code
 * within the sandbox.
 */
class NsjailSandbox {

    /**
     * Constructs the command-line arguments for compiling native C/C++ code.
     *
     * @param p       The execution policy containing compiler settings.
     * @param srcFile The name of the source file to be compiled.
     * @return A list of strings representing the compiler command and its arguments.
     */
    static List<String> compileNativeCmd(CxxExecutionPolicy p, String srcFile) {
        List<String> cc = new ArrayList<>();
        cc.add(p.getClangPath());
        // Default to static linking and optimization. Allow override by extraCompileFlags.
        cc.add("-O2");
        cc.add("-static");
        cc.add("-s");
        if (p.getExtraCompileFlags() != null) {
            for (String f : p.getExtraCompileFlags()) {
                cc.add(f);
            }
        }
        cc.add("-o");
        cc.add("a.out");
        cc.add(srcFile);
        return cc;
    }

    /**
     * Builds a temporary, minimal NsJail configuration file for a single execution.
     * <p>
     * The generated configuration specifies:
     * <ul>
     * <li>New user, PID, network, namespace, IPC, and UTS namespaces.</li>
     * <li>Read-only mounts for /bin, /usr, /lib, /lib64, and /dev/null.</li>
     * <li>Read-write mount for /tmp.</li>
     * <li>The working directory is mounted via a command-line argument.</li>
     * <li>Basic resource limits (rlimit).</li>
     * </ul>
     *
     * @param p The execution policy containing timeout and resource limits.
     * @return The path to the temporary configuration file.
     * @throws RuntimeException if the temporary file cannot be created.
     */
    private static Path buildTempConfig(CxxExecutionPolicy p) {
        // The policy primarily provides a baseline for time and resource limits.
        // Network is disabled by default via CLONE_NEWNET.
        String cfg = ""
                + "name: \"cxxexec\"\n"
                + "mode: ONCE\n"
                + "clone_newuser: true\n"
                + "clone_newpid: true\n"
                + "clone_newnet: true\n"
                + "clone_newns: true\n"
                + "clone_newipc: true\n"
                + "clone_newuts: true\n"
                + "mount_proc: true\n"
                + "time_limit: " + Math.max(1, (int) Math.ceil(p.getRunTimeoutMs() / 1000.0)) + "\n"
                + "rlimit_as: 536870912\n"     // 512MB
                + "rlimit_fsize: 10485760\n"   // 10MB
                + "rlimit_nofile: 64\n"
                + "mount: { src: \"/bin\",   dst: \"/bin\",   is_bind: true, rw: false }\n"
                + "mount: { src: \"/usr\",   dst: \"/usr\",   is_bind: true, rw: false }\n"
                + "mount: { src: \"/lib\",   dst: \"/lib\",   is_bind: true, rw: false }\n"
                + "mount: { src: \"/lib64\", dst: \"/lib64\", is_bind: true, rw: false }\n"
                + "mount: { src: \"/dev/null\", dst: \"/dev/null\", is_bind: true, rw: false }\n"
                + "mount: { src: \"/tmp\", dst: \"/tmp\", is_bind: true, rw: true }\n";

        try {
            Path cfgFile = Files.createTempFile("nsjail-cxxexec-", ".cfg");
            Files.writeString(cfgFile, cfg, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            cfgFile.toFile().deleteOnExit();
            return cfgFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create NsJail temporary config", e);
        }
    }

    /**
     * Wraps a command with the necessary NsJail arguments for sandboxed execution.
     * <p>
     * The final command structure will be:
     * {@code nsjail -Mo --config <tmp.cfg> --cwd /work --bindmount {workDir}:/work -- ./a.out}
     *
     * @param p       The execution policy.
     * @param workDir The host directory to be mounted as the sandbox's working directory.
     * @param exe     The path to the executable within the sandbox (e.g., "/work/a.out").
     * @return A list of strings representing the full NsJail command.
     */
    static List<String> wrapRun(CxxExecutionPolicy p, Path workDir, String exe) {
        Path cfg = buildTempConfig(p);

        List<String> cmd = new ArrayList<>();
        cmd.add(p.getNsjailPath());
        cmd.add("-Mo"); // Corresponds to mode: ONCE
        cmd.add("--config");
        cmd.add(cfg.toString());
        cmd.add("--cwd");
        cmd.add("/work");
        cmd.add("--bindmount");
        cmd.add(workDir.toString() + ":/work");
        cmd.add("--");
        cmd.add(exe);
        return cmd;
    }
}