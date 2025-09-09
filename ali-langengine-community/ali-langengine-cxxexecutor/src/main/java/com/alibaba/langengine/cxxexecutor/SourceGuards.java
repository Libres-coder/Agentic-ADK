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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides lightweight source code validation.
 * <p>
 * This class performs basic checks on the source code, such as verifying
 * that {@code #include} directives comply with a whitelist and blacklist.
 * It is not a full-featured AST-level parser and is intended as a first line of defense.
 */
final class SourceGuards {

    /**
     * Private constructor to prevent instantiation.
     */
    private SourceGuards() {}

    /**
     * Regular expression to match {@code #include <...>} and {@code #include "..."} directives.
     */
    private static final Pattern INCLUDE_PATTERN =
            Pattern.compile("^\\s*#\\s*include\\s*(<[^>]+>|\"[^\"]+\")");

    /**
     * Checks all {@code #include} directives in the given source code against
     * allowed and denied patterns.
     *
     * @param code           The source code to check.
     * @param allowedPatterns An array of regex patterns for whitelisted includes. If provided, an include must match one of these.
     * @param deniedPatterns  An array of regex patterns for blacklisted includes. Any match will cause an exception.
     * @throws IllegalArgumentException if an include directive violates the policy.
     */
    static void checkIncludes(String code, String[] allowedPatterns, String[] deniedPatterns) {
        if (code == null) {
            return;
        }

        String[] lines = code.split("\\r?\\n");
        for (String line : lines) {
            Matcher matcher = INCLUDE_PATTERN.matcher(line);
            if (!matcher.find()) {
                continue;
            }

            String header = matcher.group(1);

            // Check against the blacklist first.
            if (deniedPatterns != null) {
                for (String deniedPattern : deniedPatterns) {
                    if (header.matches(deniedPattern)) {
                        throw new IllegalArgumentException("Denied include: " + header);
                    }
                }
            }

            // If a whitelist is provided, the header must match at least one pattern.
            if (allowedPatterns != null && allowedPatterns.length > 0) {
                boolean isAllowed = false;
                for (String allowedPattern : allowedPatterns) {
                    if (header.matches(allowedPattern)) {
                        isAllowed = true;
                        break;
                    }
                }
                if (!isAllowed) {
                    throw new IllegalArgumentException("Include not allowed by policy: " + header);
                }
            }
        }
    }
}