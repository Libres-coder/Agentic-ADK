/*
 * Copyright 2024 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law of a or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.pyexecutor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A utility class for handling streams with size limits.
 * <p>
 * Provides functionality to pump data from an {@link InputStream} to a size-limited
 * buffer in a separate thread, preventing the main thread from blocking on I/O.
 * The buffer automatically truncates the stream when its capacity is reached.
 */
class TruncatingStreams {

    /**
     * Starts a daemon thread to pump all bytes from an InputStream into a LimitedBuffer.
     * The thread will terminate when the end of the stream is reached, an IOException occurs,
     * or the buffer signals that it has been truncated.
     *
     * @param in  The InputStream to read from. It will be closed automatically.
     * @param buf The LimitedBuffer to write to.
     * @return The started daemon thread that is performing the pumping operation.
     */
    static Thread pump(InputStream in, LimitedBuffer buf) {
        Thread t = new Thread(() -> {
            byte[] tmp = new byte[8192];
            try (in) {
                int n;
                while ((n = in.read(tmp)) != -1) {
                    // Stop pumping if the buffer is full and has been truncated.
                    if (!buf.writeLimited(tmp, 0, n)) {
                        break;
                    }
                }
            } catch (IOException ignored) {
                // Ignore I/O errors, which typically occur when the process is terminated.
            }
        }, "pyexec-pump");
        t.setDaemon(true);
        t.start();
        return t;
    }

    /**
     * A {@link ByteArrayOutputStream} that enforces a maximum size limit.
     * <p>
     * When writing data, if the total size would exceed the configured maximum,
     * it writes only enough data to reach the limit, sets a truncated flag,
     * and then rejects further writes.
     */
    static final class LimitedBuffer extends ByteArrayOutputStream {
        private final long max;
        private volatile boolean truncated = false;

        /**
         * Constructs a LimitedBuffer with a specified maximum size.
         *
         * @param max The maximum number of bytes to store. If non-positive, a default of 1,000,000 is used.
         */
        LimitedBuffer(long max) {
            this.max = max <= 0 ? 1_000_000 : max;
        }

        /**
         * Tries to write a byte array to the buffer.
         * If the write would exceed the maximum size, it writes a partial chunk to fill
         * the buffer, sets the truncated flag, and returns false.
         *
         * @param b   the data.
         * @param off the start offset in the data.
         * @param len the number of bytes to write.
         * @return {@code true} if the write was successful and the limit was not exceeded,
         * {@code false} if the buffer was truncated.
         */
        boolean writeLimited(byte[] b, int off, int len) {
            long will = (long) size() + len;
            if (will > max) {
                int keep = (int) (max - size());
                if (keep > 0) {
                    super.write(b, off, keep);
                }
                truncated = true;
                return false;
            } else {
                super.write(b, off, len);
                return true;
            }
        }

        /**
         * Converts the buffer's contents into a string using UTF-8 encoding.
         *
         * @return The string representation of the buffer's content.
         */
        String asString() {
            return toString(StandardCharsets.UTF_8);
        }

        /**
         * Checks if the buffer has been truncated.
         *
         * @return true if the size limit was reached and data was dropped, false otherwise.
         */
        boolean isTruncated() {
            return truncated;
        }
    }
}