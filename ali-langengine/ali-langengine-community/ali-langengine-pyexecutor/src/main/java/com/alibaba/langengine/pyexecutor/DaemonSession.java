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
 * Manages a single Python daemon process for stateful, session-based execution.
 * <p>
 * This class is responsible for starting, communicating with, and shutting down
 * a long-running Python process. It handles sending code for execution and parsing
 * the structured JSON output from the daemon's stdout. All instance variables are
 * private to ensure proper encapsulation, with access provided through public methods.
 */
public class DaemonSession {

    private static final String PROTO_PREFIX = "[[PYEXEC]]";

    private Process process;
    private BufferedReader out;
    private PrintWriter in;
    private Thread stderrPump;
    private TruncatingStreams.LimitedBuffer stderrBuffer;

    private Path cwd;
    private volatile long lastUsedEpochMs;
    private final long creationEpochMs;

    /**
     * Constructs a new DaemonSession, capturing its creation timestamp for hard TTL management.
     */
    public DaemonSession() {
        this.creationEpochMs = System.currentTimeMillis();
    }

    /**
     * Starts the Python daemon process if it is not already running.
     * <p>
     * This method prepares a Python driver script, configures the process environment
     * based on the security policy, starts the process, and waits for a "ready"
     * message from the daemon.
     *
     * @param policy     The execution policy defining security and resource limits.
     * @param sessionCwd The root working directory for this session.
     * @throws Exception if the process fails to start or does not become ready within the timeout.
     */
    public synchronized void start(PyExecutionPolicy policy, Path sessionCwd) throws Exception {
        if (process != null && process.isAlive()) {
            return;
        }

        Path tmp = Files.createTempDirectory("pyexec_daemon_");
        Path driverFile = tmp.resolve("daemon.py");
        Files.writeString(driverFile, PreludeBuilderDaemon.render(policy), StandardCharsets.UTF_8);

        List<String> cmd = new ArrayList<>();
        cmd.add(policy.getPythonBin());
        if (policy.isIsolateSite()) {
            cmd.add("-I");
            cmd.add("-S");
        }
        cmd.add(driverFile.toString());

        Files.createDirectories(sessionCwd);
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(sessionCwd.toFile());

        Map<String, String> env = pb.environment();
        env.clear();
        env.put("PYTHONIOENCODING", "utf-8");
        env.put("PY_SESSION_MODE", "1");
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

        process = pb.start();
        out = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        in = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8), true);

        stderrBuffer = new TruncatingStreams.LimitedBuffer(policy.getMaxStderrBytes());
        stderrPump = TruncatingStreams.pump(process.getErrorStream(), stderrBuffer);

        long deadline = System.nanoTime() + policy.getTimeout().toNanos();
        boolean ready = false;
        while (System.nanoTime() < deadline) {
            String line = readLineNonBlocking(out, 20);
            if (line == null) {
                if (!process.isAlive()) break;
                continue;
            }
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith(PROTO_PREFIX)) {
                String js = line.substring(PROTO_PREFIX.length());
                try {
                    JSONObject obj = JSON.parseObject(js);
                    if ("_meta".equals(obj.getString("type")) &&
                            "ready".equals(obj.getString("event"))) {
                        ready = true;
                        break;
                    }
                } catch (Exception ignore) {
                }
            }
        }
        if (!ready) {
            throw new IllegalStateException("python daemon not ready");
        }
        this.cwd = sessionCwd.toAbsolutePath();
        this.lastUsedEpochMs = System.currentTimeMillis();
    }

    /**
     * Executes code in the running daemon process.
     *
     * @param code    The Python code to execute.
     * @param opts    Per-call execution options.
     * @param policy  The global execution policy.
     * @return A {@link PyExecutionResult} containing the execution outcome.
     * @throws Exception if the daemon is not running or the execution times out.
     */
    public synchronized PyExecutionResult exec(String code, PyExecutionOptions opts, PyExecutionPolicy policy) throws Exception {
        if (process == null || !process.isAlive()) {
            throw new IllegalStateException("python daemon not running");
        }

        this.lastUsedEpochMs = System.currentTimeMillis();

        Path runCwd = this.cwd;
        if (opts != null && opts.getWorkingDirRelative() != null) {
            Path requestedPath = this.cwd.resolve(opts.getWorkingDirRelative()).normalize();
            if (requestedPath.toAbsolutePath().startsWith(this.cwd.toAbsolutePath())) {
                runCwd = requestedPath;
                Files.createDirectories(runCwd);
            } else {
                return new PyExecutionResult(
                        -1, "", false,
                        "Security Error: Attempted to access directory outside of sandbox.", true,
                        0L, null, "PermissionError: invalid cwd"
                );
            }
        }

        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("op", "exec");
        msg.put("code", code == null ? "" : code);
        Map<String, Object> opt = new LinkedHashMap<>();
        opt.put("cwd", runCwd.toString());
        if (opts != null && opts.getPrintLastExpression() != null) {
            opt.put("printLastExpression", opts.getPrintLastExpression());
        }
        msg.put("opt", opt);
        in.println(JSON.toJSONString(msg));
        in.flush();

        TruncatingStreams.LimitedBuffer outBuf = new TruncatingStreams.LimitedBuffer(policy.getMaxStdoutBytes());
        String lastValue = null;
        String errorRepr = null;
        boolean seenOversizeLine = false;
        boolean sawError = false;
        Integer doneExit = null;

        long deadline = System.nanoTime() + policy.getTimeout().toNanos();
        while (System.nanoTime() < deadline) {
            String raw = readLineNonBlocking(out, 20);
            if (raw == null) {
                if (!process.isAlive()) break;
                continue;
            }
            String line = raw.trim();
            if (line.isEmpty()) continue;

            boolean isProto = false;
            if (line.startsWith(PROTO_PREFIX)) {
                isProto = true;
                String js = line.substring(PROTO_PREFIX.length());
                try {
                    JSONObject obj = JSON.parseObject(js);
                    String type = obj.getString("type");
                    if ("_done".equals(type)) {
                        if (obj.containsKey("exit")) {
                            try {
                                doneExit = obj.getInteger("exit");
                            } catch (Exception ignore) {
                            }
                        }
                        break;
                    } else if ("value".equals(type)) {
                        lastValue = obj.getString("repr");
                        errorRepr = null;
                    } else if ("error".equals(type)) {
                        errorRepr = obj.getString("error");
                        sawError = true;
                    }
                } catch (Exception ignore) {
                    isProto = false;
                }
            }
            if (!isProto) {
                byte[] bytes = (raw + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
                if (bytes.length > policy.getMaxStdoutBytes()) seenOversizeLine = true;
                outBuf.write(bytes, 0, bytes.length);
            }
        }

        if (System.nanoTime() >= deadline) {
            throw new java.util.concurrent.TimeoutException("session execution timed out");
        }

        this.lastUsedEpochMs = System.currentTimeMillis();

        Integer procExit = null;
        if (!process.isAlive()) {
            try {
                process.waitFor(100, TimeUnit.MILLISECONDS);
                procExit = process.exitValue();
            } catch (Exception ignore) {
            }
        }
        int exitCode;
        if (doneExit != null) {
            exitCode = doneExit;
        } else if (procExit != null) {
            exitCode = procExit;
        } else {
            exitCode = sawError ? 1 : 0;
        }

        String stdoutStr = outBuf.asString();
        boolean stdoutTrunc = outBuf.isTruncated()
                || (stdoutStr != null && stdoutStr.getBytes(StandardCharsets.UTF_8).length > policy.getMaxStdoutBytes())
                || seenOversizeLine;

        String stderrStr = (stderrBuffer == null) ? "" : stderrBuffer.asString();
        boolean stderrTrunc = (stderrBuffer != null && stderrBuffer.isTruncated());

        return new PyExecutionResult(
                exitCode,
                (stdoutStr == null || stdoutStr.isEmpty()) ? null : stdoutStr, stdoutTrunc,
                (stderrStr == null || stderrStr.isEmpty()) ? null : stderrStr, stderrTrunc,
                0L,
                lastValue,
                errorRepr
        );
    }

    /**
     * Shuts down the daemon process and cleans up resources.
     * This method attempts to terminate the process gracefully before forcing it.
     */
    public synchronized void shutdown() {
        try {
            if (in != null) {
                in.println(JSON.toJSONString(Collections.singletonMap("op", "shutdown")));
                in.flush();
            }
        } catch (Exception ignore) {
        }
        try {
            if (process != null) process.destroy();
        } catch (Exception ignore) {
        }
        try {
            if (process != null) process.waitFor(500, TimeUnit.MILLISECONDS);
        } catch (Exception ignore) {
        }
        try {
            if (process != null) process.destroyForcibly();
        } catch (Exception ignore) {
        }
        try {
            if (stderrPump != null) stderrPump.interrupt();
        } catch (Exception ignore) {
        }
        try {
            if (out != null) out.close();
        } catch (Exception ignore) {
        }
        try {
            if (in != null) in.close();
        } catch (Exception ignore) {
        }

        process = null;
        out = null;
        in = null;
        stderrPump = null;
        stderrBuffer = null;
    }

    /**
     * Reads a line from a BufferedReader with a timeout.
     *
     * @param br        The reader to read from.
     * @param maxWaitMs The maximum time to wait in milliseconds.
     * @return The line read, or null if no line was available within the timeout.
     * @throws Exception if an interruption or I/O error occurs.
     */
    private static String readLineNonBlocking(BufferedReader br, long maxWaitMs) throws Exception {
        long end = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(maxWaitMs);
        while (System.nanoTime() < end) {
            if (br.ready()) {
                return br.readLine();
            }
            Thread.sleep(5);
        }
        return null;
    }

    // =================================================================
    // Getters and Setters
    // =================================================================

    /**
     * Checks if the underlying daemon process is currently running.
     *
     * @return true if the process is alive, false otherwise.
     */
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    /**
     * Gets the underlying {@link Process} object for the daemon.
     *
     * @return The process object.
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Gets the timestamp of the last use of this session.
     *
     * @return Milliseconds since epoch of the last use.
     */
    public long getLastUsedEpochMs() {
        return lastUsedEpochMs;
    }

    /**
     * Sets the timestamp of the last use of this session.
     *
     * @param lastUsedEpochMs Milliseconds since epoch of the last use.
     */
    public void setLastUsedEpochMs(long lastUsedEpochMs) {
        this.lastUsedEpochMs = lastUsedEpochMs;
    }

    /**
     * Gets the creation timestamp of this session.
     *
     * @return Milliseconds since epoch of the session creation.
     */
    public long getCreationEpochMs() {
        return creationEpochMs;
    }
}