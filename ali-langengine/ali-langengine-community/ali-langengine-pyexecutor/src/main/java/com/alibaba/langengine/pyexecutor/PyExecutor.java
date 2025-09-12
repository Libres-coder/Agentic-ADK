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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Main entry point for Python code execution.
 * <p>
 * This class provides two primary execution models:
 * <ul>
 * <li><b>Oneshot Execution:</b> {@link #executeOnce(String, PyExecutionOptions)} creates a temporary, isolated
 * Python process for each request. It's suitable for stateless tasks where security and isolation are paramount.</li>
 * <li><b>Session-based Execution:</b> {@link #execute(String, String, PyExecutionOptions)} uses a pool of persistent
 * Python daemon processes managed by {@link SessionManager}. This model is ideal for stateful operations where
 * variables and context must be preserved across multiple calls.</li>
 * </ul>
 * Both models are configured via a {@link PyExecutionPolicy} which defines security constraints and resource limits.
 */
public class PyExecutor {

    private static final String PROTO_PREFIX = "[[PYEXEC]]";

    private final PyExecutionPolicy policy;
    private final SessionManager sessionManager;

    /**
     * Constructs a PyExecutor for oneshot execution only.
     *
     * @param policy The global security and resource policy. If null, a default policy is used.
     */
    public PyExecutor(PyExecutionPolicy policy) {
        this(policy, null);
    }

    /**
     * Constructs a PyExecutor with support for both oneshot and session-based execution.
     *
     * @param policy        The global security and resource policy. If null, a default policy is used.
     * @param sessionConfig The configuration for the session manager. If null, session mode is disabled.
     */
    public PyExecutor(PyExecutionPolicy policy, SessionConfig sessionConfig) {
        this.policy = (policy == null ? new PyExecutionPolicy() : policy);
        this.sessionManager = (sessionConfig == null ? null : new SessionManager(this.policy, sessionConfig));
    }

    // =================================================================
    // Oneshot Execution
    // =================================================================

    /**
     * Executes a Python code snippet in a new, isolated process.
     * This method is ideal for stateless tasks.
     *
     * @param code    The Python code to execute. Must not be null.
     * @param options Per-execution options, such as environment variables or a working directory. Can be null.
     * @return A {@link PyExecutionResult} containing the exit code, stdout, stderr, and any parsed final expression value.
     * @throws Exception If an I/O error occurs or the process times out.
     */
    public PyExecutionResult executeOnce(String code, PyExecutionOptions options) throws Exception {
        Objects.requireNonNull(code, "code");

        // Create a temporary directory and a Python driver script for the execution.
        Path tmp = Files.createTempDirectory("pyexec_once_");
        Path driverFile = tmp.resolve("once.py");
        Files.writeString(driverFile, PreludeBuilderOnce.render(policy), StandardCharsets.UTF_8);

        // Build the command to execute the Python script.
        List<String> cmd = new ArrayList<>();
        cmd.add(policy.getPythonBin());
        if (policy.isIsolateSite()) {
            cmd.add("-I");
            cmd.add("-S");
        }
        cmd.add(driverFile.toString());

        // Determine the working directory for the process.
        Path workDir = tmp;
        if (options != null && options.getWorkingDirRelative() != null) {
            Path base = Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath();
            Path candidate = base.resolve(options.getWorkingDirRelative());
            if (Files.exists(candidate)) {
                workDir = candidate;
            } else {
                workDir = tmp.resolve(options.getWorkingDirRelative());
                Files.createDirectories(workDir);
            }
        }

        // Configure the process environment based on the execution policy.
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(workDir.toFile());
        Map<String, String> env = pb.environment();
        env.clear();
        env.put("PYTHONIOENCODING", "utf-8");
        env.put("PY_DISABLE_NET", policy.isDisableNetworking() ? "1" : "0");
        env.put("PY_DISABLE_OPEN", policy.isDisableOpen() ? "1" : (policy.isAllowReadonlyOpen() ? "RO" : "0"));
        env.put("PY_IMPORT_MODE", policy.isUseImportWhitelist() ? "WL" : "BL");
        env.put("PY_BLOCK_DUNDER", policy.isBlockDunderImports() ? "1" : "0");
        env.put("PY_PRINT_LAST_EXPR", policy.isPrintLastExpression() ? "1" : "0");
        env.put("PY_ALLOWED", String.join(",", policy.getAllowedImports()));
        env.put("PY_BANNED", String.join(",", policy.getBannedImports()));
        if (policy.getCpuTimeSeconds() != null) env.put("PY_CPU_LIMIT", String.valueOf(policy.getCpuTimeSeconds()));
        if (policy.getAddressSpaceBytes() != null) env.put("PY_AS_LIMIT", String.valueOf(policy.getAddressSpaceBytes()));
        if (policy.getMaxOpenFiles() != null) env.put("PY_FD_LIMIT", String.valueOf(policy.getMaxOpenFiles()));

        // Add any extra environment variables specified in the options.
        boolean hasExtraEnv = options != null && options.getExtraEnv() != null && !options.getExtraEnv().isEmpty();
        if (hasExtraEnv) {
            env.put("PY_EXTRA_ENV_PRESENT", "1");
        }
        if (options != null && options.getExtraEnv() != null) {
            for (Map.Entry<String, String> e : options.getExtraEnv().entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    env.put(e.getKey(), e.getValue());
                }
            }
        }

        Process proc = pb.start();

        // Send the code and options to the Python process via stdin.
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("op", "exec");
        msg.put("code", code);
        Map<String, Object> opt = new LinkedHashMap<>();
        if (options != null && options.getPrintLastExpression() != null) {
            opt.put("printLastExpression", options.getPrintLastExpression());
        }
        if (options != null && options.getWorkingDirRelative() != null) {
            opt.put("cwd", workDir.toString());
        }
        msg.put("opt", opt);

        try (PrintWriter stdin = new PrintWriter(new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8), true)) {
            stdin.println(JSON.toJSONString(msg));
            stdin.flush();
        }

        // Set up buffers and threads to capture stdout and stderr.
        final long maxStdout = policy.getMaxStdoutBytes();
        final long maxStderr = policy.getMaxStderrBytes();

        TruncatingStreams.LimitedBuffer outBuf = new TruncatingStreams.LimitedBuffer(maxStdout);
        TruncatingStreams.LimitedBuffer errBuf = new TruncatingStreams.LimitedBuffer(maxStderr);
        Thread errPump = TruncatingStreams.pump(proc.getErrorStream(), errBuf);
        BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));

        String lastValue = null;
        String errorRepr = null;
        boolean seenOversizeLine = false;
        boolean doneSeen = false;
        long afterDoneDrainUntilNanos = -1L;

        // Main loop to read output from the Python process until it's done or times out.
        long deadline = System.nanoTime() + policy.getTimeout().toNanos();
        while (System.nanoTime() < deadline) {
            if (!br.ready()) {
                if (!proc.isAlive()) break;
                // After receiving a "_done" message, wait briefly for any remaining stdout to flush.
                if (doneSeen) {
                    if (System.nanoTime() < afterDoneDrainUntilNanos) {
                        Thread.sleep(5);
                        continue;
                    } else {
                        break; // Drain window finished.
                    }
                }
                Thread.sleep(10);
                continue;
            }
            String raw = br.readLine();
            if (raw == null) break;

            String line = raw.trim();
            if (line.isEmpty()) continue;

            // Check if the line is a protocol message or regular output.
            boolean isProto = false;
            if (line.startsWith(PROTO_PREFIX)) {
                isProto = true;
                String js = line.substring(PROTO_PREFIX.length());
                try {
                    JSONObject obj = JSON.parseObject(js);
                    String type = obj.getString("type");
                    if ("_done".equals(type)) {
                        doneSeen = true;
                        // Start a 200ms drain window for any final output.
                        afterDoneDrainUntilNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(200);
                    } else if ("value".equals(type)) {
                        lastValue = obj.getString("repr");
                        errorRepr = null;
                    } else if ("error".equals(type)) {
                        errorRepr = obj.getString("error");
                    }
                } catch (Exception ignore) {
                    isProto = false; // Treat malformed protocol lines as regular output.
                }
            }

            // If not a protocol message, append it to the stdout buffer.
            if (!isProto) {
                byte[] bytes = (raw + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
                if (maxStdout > 0 && bytes.length > maxStdout) {
                    seenOversizeLine = true;
                }
                outBuf.writeLimited(bytes, 0, bytes.length);
            }
        }

        // Handle timeout by forcibly terminating the process.
        if (System.nanoTime() >= deadline) {
            try { proc.destroy(); } catch (Exception ignore) {}
            try { proc.waitFor(200, TimeUnit.MILLISECONDS); } catch (Exception ignore) {}
            try { proc.destroyForcibly(); } catch (Exception ignore) {}
            throw new java.util.concurrent.TimeoutException("oneshot execution timed out");
        }

        // Wait for the process to exit and get its exit code.
        proc.waitFor(100, TimeUnit.MILLISECONDS);
        int exit = 0;
        try {
            exit = proc.exitValue();
        } catch (IllegalThreadStateException ignore) {
            // Process might still be running in rare cases.
        }

        // Finalize stream collection.
        try { errPump.interrupt(); } catch (Exception ignore) {}
        try { errPump.join(200); } catch (InterruptedException ignore) {}

        String stdoutStr = outBuf.asString();
        // Determine if stdout was truncated.
        boolean stdoutTrunc = outBuf.isTruncated()
                || (maxStdout > 0
                && stdoutStr != null
                && stdoutStr.getBytes(StandardCharsets.UTF_8).length >= maxStdout)
                || seenOversizeLine;

        // Construct and return the final result.
        return new PyExecutionResult(
                exit,
                stdoutStr, stdoutTrunc,
                errBuf.asString(), errBuf.isTruncated(),
                0L,
                lastValue,
                errorRepr
        );
    }

    // =================================================================
    // Session-based Execution
    // =================================================================

    /**
     * Executes code within a persistent session, preserving state between calls.
     * Throws an exception if session mode was not enabled during construction.
     *
     * @param sessionId A unique identifier for the session.
     * @param code      The Python code to execute. Must not be null.
     * @param options   Per-execution options, like a temporary working directory. Can be null.
     * @return A {@link PyExecutionResult}.
     * @throws Exception If the session is not enabled, fails to start, or the execution times out.
     */
    public PyExecutionResult execute(String sessionId, String code, PyExecutionOptions options) throws Exception {
        if (sessionManager == null) {
            throw new IllegalStateException("session mode not enabled");
        }
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(code, "code");

        // Get or create the session.
        DaemonSession s = sessionManager.getOrCreate(sessionId);

        // Ensure options object is not null.
        PyExecutionOptions opts = (options == null) ? new PyExecutionOptions() : options;

        // Security check for working directory to prevent path traversal.
        String rel = opts.getWorkingDirRelative();
        if (rel != null && !rel.isBlank()) {
            Path p = Paths.get(rel);
            if (p.isAbsolute() || rel.contains("..") || rel.contains(":\\") || rel.startsWith("\\") || rel.startsWith("/")) {
                throw new IllegalArgumentException("outside sandbox: " + rel);
            }
            // Normalize the path to clean it up (e.g., "a//b" -> "a/b").
            String norm = p.normalize().toString();
            if (!norm.equals(rel)) {
                opts.setWorkingDirRelative(norm);
            }
        }

        // Set the printLastExpression option for this specific call, using the policy default if not provided.
        if (opts.getPrintLastExpression() == null) {
            opts.setPrintLastExpression(policy.isPrintLastExpression());
        }

        // Delegate the execution to the daemon session.
        return s.exec(code, opts, policy);
    }


    /**
     * Closes and terminates a specific session.
     *
     * @param sessionId The ID of the session to close.
     */
    public void closeSession(String sessionId) {
        if (sessionManager != null) {
            sessionManager.close(sessionId);
        }
    }

    /**
     * Closes and terminates all active sessions.
     */
    public void closeAll() {
        if (sessionManager != null) {
            sessionManager.closeAll();
        }
    }
}