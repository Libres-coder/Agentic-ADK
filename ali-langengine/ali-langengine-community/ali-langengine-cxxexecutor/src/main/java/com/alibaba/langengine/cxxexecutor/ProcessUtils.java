/*
 * Copyright 2024 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law of an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.cxxexecutor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A utility class for executing external processes.
 * <p>
 * This class provides methods to run system commands, manage timeouts,
 * and capture standard output and error streams with truncation support.
 */
class ProcessUtils {

    /**
     * Represents the output of an executed process.
     * <p>
     * This is an immutable class that holds the exit code, standard output, standard error,
     * and other metadata from a completed process.
     */
    static class ExecOut {
        final int code;
        final String out;
        final String err;
        final boolean outTrunc;
        final boolean errTrunc;
        final long millis;

        ExecOut(int code, String out, String err, boolean outTrunc, boolean errTrunc, long millis) {
            this.code = code;
            this.out = out;
            this.err = err;
            this.outTrunc = outTrunc;
            this.errTrunc = errTrunc;
            this.millis = millis;
        }
    }

    /**
     * Executes an external command and waits for it to complete.
     *
     * @param cmd              The command and its arguments to execute.
     * @param workDir          The working directory for the process.
     * @param stdin            The standard input to be written to the process.
     * @param env              A map of environment variables to set for the process.
     * @param timeoutMs        The timeout in milliseconds for the process to complete.
     * @param maxStdoutBytes   The maximum number of bytes to capture from standard output.
     * @param maxStderrBytes   The maximum number of bytes to capture from standard error.
     * @param hardKillGraceMs The grace period in milliseconds before forcefully killing the process after a timeout.
     * @return An {@link ExecOut} object containing the result of the execution.
     * @throws IOException      if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted while waiting.
     * @throws TimeoutException if the process does not complete within the specified timeout.
     */
    static ExecOut exec(List<String> cmd, Path workDir, String stdin, Map<String, String> env,
                        int timeoutMs, long maxStdoutBytes, long maxStderrBytes, long hardKillGraceMs)
            throws IOException, InterruptedException, TimeoutException {

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(workDir.toFile());
        if (env != null) {
            pb.environment().putAll(env);
        }

        Process p = pb.start();

        if (stdin != null && !stdin.isEmpty()) {
            try (OutputStream os = p.getOutputStream()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            p.getOutputStream().close();
        }

        TruncatingBuffer stdoutBuf = new TruncatingBuffer(maxStdoutBytes);
        TruncatingBuffer stderrBuf = new TruncatingBuffer(maxStderrBytes);

        Thread t1 = pipe(p.getInputStream(), stdoutBuf);
        Thread t2 = pipe(p.getErrorStream(), stderrBuf);

        long start = System.nanoTime();
        boolean finished;
        if (timeoutMs > 0) {
            finished = p.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } else {
            p.waitFor();
            finished = true;
        }
        long dur = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        if (!finished) {
            // Attempt a graceful destroy first.
            p.destroy();
            try {
                Thread.sleep(Math.max(0, hardKillGraceMs));
            } catch (InterruptedException ignored) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
            if (p.isAlive()) {
                p.destroyForcibly();
            }
            throw new TimeoutException("Process did not complete within the specified timeout.");
        }
        t1.join();
        t2.join();

        return new ExecOut(
                p.exitValue(), stdoutBuf.string(), stderrBuf.string(),
                stdoutBuf.truncated(), stderrBuf.truncated(), dur
        );
    }

    /**
     * Pipes an InputStream to a TruncatingBuffer in a separate thread.
     *
     * @param is  The InputStream to read from.
     * @param buf The TruncatingBuffer to write to.
     * @return The thread that is performing the piping operation.
     */
    private static Thread pipe(InputStream is, TruncatingBuffer buf) {
        Thread t = new Thread(() -> {
            try (InputStream in = is) {
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) != -1) {
                    buf.write(b, 0, n);
                }
            } catch (IOException ignored) {
                // Ignored.
            }
        });
        t.setDaemon(true);
        t.start();
        return t;
    }

    /**
     * A thread-safe buffer that truncates its content if it exceeds a specified limit.
     */
    static class TruncatingBuffer {
        private final long limit;
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private boolean truncated = false;

        TruncatingBuffer(long limit) {
            this.limit = limit;
        }

        /**
         * Writes a sequence of bytes to the buffer, truncating if the limit is exceeded.
         *
         * @param b   The data.
         * @param off The start offset in the data.
         * @param len The number of bytes to write.
         */
        synchronized void write(byte[] b, int off, int len) {
            if (limit <= 0) {
                baos.write(b, off, len);
                return;
            }
            long remaining = limit - baos.size();
            if (remaining <= 0) {
                truncated = true;
                return;
            }
            int w = (int) Math.min(remaining, len);
            baos.write(b, off, w);
            if (w < len) {
                truncated = true;
            }
        }

        /**
         * Returns the contents of the buffer as a string.
         *
         * @return The buffered content, decoded using UTF-8.
         */
        synchronized String string() {
            return baos.toString(StandardCharsets.UTF_8);
        }

        /**
         * Checks if the buffer's content has been truncated.
         *
         * @return {@code true} if truncation occurred, {@code false} otherwise.
         */
        synchronized boolean truncated() {
            return truncated;
        }
    }
}