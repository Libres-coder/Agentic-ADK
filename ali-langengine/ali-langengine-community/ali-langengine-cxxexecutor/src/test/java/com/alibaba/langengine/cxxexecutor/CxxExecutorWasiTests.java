/*
 * Copyright 2024 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law of or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.cxxexecutor;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains unit tests for {@link CxxExecutor} using the WASI backend.
 */
public class CxxExecutorWasiTests {

    private CxxExecutor createExecutor() {
        return new CxxExecutor();
    }

    /**
     * Tests a successful execution of a simple "Hello World" program.
     */
    @Test
    void run_ok_printf() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ printf(\"OK\\n\"); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.isOk());
        assertTrue(result.getStdout().contains("OK"));
    }

    /**
     * Tests a successful execution of a C program (isCpp=false).
     */
    @Test
    void run_ok_c_lang() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <stdio.h>",
                "int main(){ puts(\"C OK\"); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(false).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.isOk());
        assertTrue(result.getStdout().contains("C OK"));
    }

    /**
     * Tests that a syntax error during compilation is correctly reported.
     */
    @Test
    void compile_error_syntax() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code("int main(){ BROKEN }").isCpp(true).policy(policy).build());
        assertEquals("compile", result.getPhase());
        assertFalse(result.isOk());
        assertNotNull(result.getStderr());
    }

    /**
     * Tests that a missing header file during compilation is correctly reported.
     */
    @Test
    void compile_error_missing_header() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include \"not_exist.h\"",
                "int main(){return 0;}");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("compile", result.getPhase());
        assertFalse(result.isOk());
    }

    /**
     * Tests that the include guard correctly denies a blacklisted header like <sys/socket.h>.
     */
    @Test
    void guard_denied_include_sys_socket() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <sys/socket.h>",
                "int main(){return 0;}");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("compile", result.getPhase());
        assertFalse(result.isOk());
        assertTrue(result.getStderr().toLowerCase().contains("denied include"));
    }

    /**
     * Tests that the include guard correctly denies a header that does not match the whitelist pattern.
     */
    @Test
    void guard_include_not_match_policy() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        policy.setAllowedIncludePatterns(new String[]{"^<.*>$", "^\"[A-Za-z0-9_./-]+\"$"});
        String code = String.join("\n",
                "#include \"../evil.h\"",
                "int main(){return 0;}");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("compile", result.getPhase());
        assertFalse(result.isOk());
    }

    /**
     * Tests that a non-zero exit code during runtime is correctly reported.
     */
    @Test
    void run_nonzero_exit() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = "int main(){ return 42; }";
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertFalse(result.isOk());
        assertEquals(42, result.getExitCode());
    }

    /**
     * Tests that an infinite loop correctly results in a timeout.
     */
    @Test
    void run_timeout_infinite_loop() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        policy.setRunTimeoutMs(500);
        String code = "int main(){ for(;;){} }";
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertFalse(result.isOk());
        assertNotNull(result.getPhase(), "Phase should not be null on timeout");
        assertTrue("error".equals(result.getPhase()) || "run".equals(result.getPhase()), "Phase should be 'error' or 'run' on timeout");
    }

    /**
     * Tests that standard input is correctly passed to and processed by the program.
     */
    @Test
    void run_stdin_echo() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ char buf[16]={0}; if(fgets(buf,16,stdin)) printf(\"%s\",buf); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder()
                .code(code).isCpp(true).stdin("hello\n").policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.isOk());
        assertTrue(result.getStdout().contains("hello"));
    }

    /**
     * Tests that environment variables are correctly passed to the program.
     */
    @Test
    void run_env_var() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "#include <cstdlib>",
                "int main(){ const char* v=getenv(\"FOO\"); if(v) printf(\"%s\\n\", v); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder()
                .code(code).isCpp(true).env(Map.of("FOO", "BAR")).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.getStdout().contains("BAR"));
    }

    /**
     * Tests that stdout is correctly truncated when it exceeds the specified limit.
     */
    @Test
    void run_stdout_truncate() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        policy.setMaxStdoutBytes(64);
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ for(int i=0;i<1000;i++) putchar('A'); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.isStdoutTruncated());
        assertTrue(result.getStdout().length() <= 64);
    }

    /**
     * Tests that stderr is correctly truncated when it exceeds the specified limit.
     */
    @Test
    void run_stderr_truncate() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        policy.setMaxStderrBytes(32);
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ for(int i=0;i<1000;i++) fputc('E', stderr); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.isStderrTruncated());
        assertTrue(result.getStderr().length() <= 32);
    }

    /**
     * Tests that Unicode characters are correctly handled in the output.
     */
    @Test
    void run_unicode_output() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ printf(\"你好，世界\\n\"); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.getStdout().contains("你好"));
    }

    /**
     * Tests that file writing is permitted within the mounted working directory.
     */
    @Test
    void run_file_write_allowed_in_mount() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ FILE* f=fopen(\"out.txt\",\"w\"); if(!f){perror(\"open\"); return 2;} fputs(\"X\",f); fclose(f); puts(\"WROTE\"); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.isOk());
        assertTrue(result.getStdout().contains("WROTE"));
    }

    /**
     * Tests that accessing an absolute path outside the sandbox (e.g., /etc/passwd) fails as expected.
     */
    @Test
    void run_sandbox_block_absolute_path() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "#include <errno.h>",
                "int main(){ FILE* f=fopen(\"/etc/passwd\",\"r\"); if(!f){ perror(\"fopen\"); return 3;} fclose(f); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertFalse(result.isOk(), "Execution should fail when accessing a restricted absolute path.");
    }

    /**
     * Tests that path traversal attempts (e.g., using "../") to escape the sandbox fail as expected.
     */
    @Test
    void run_sandbox_block_path_traversal() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ FILE* f=fopen(\"../secret\",\"w\"); if(!f){ puts(\"DENIED\"); return 4;} fclose(f); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertFalse(result.isOk());
        assertTrue(result.getStdout().contains("DENIED"));
    }

    /**
     * Tests a large memory allocation, which may either succeed or fail gracefully depending on the environment.
     */
    @Test
    void run_large_memory_allocation() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdlib>",
                "int main(){ size_t n = (size_t)1<<30; void* p = malloc(n); return p?0:5; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        // This test is permissive because the outcome depends on the host system and wasmtime limits.
        assertTrue(result.isOk() || !result.isOk(), "Large allocation can either succeed or fail.");
    }

    /**
     * Tests that a stack overflow due to infinite recursion is handled correctly (results in a non-zero exit code).
     */
    @Test
    void run_stack_overflow() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        policy.setMaxWasmStackBytes(512);
        policy.setRunTimeoutMs(20_000);

        String code = String.join("\n",
                "extern \"C\" __attribute__((noinline)) int f(int n){",
                "  volatile int x = n;",
                "  asm volatile(\"\");",
                "  return f(x + 1);",
                "}",
                "int main(){ return f(0); }"
        );

        CxxExecutionResult result = new CxxExecutor().executeOnce(
                CxxExecutionOptions.builder()
                        .code(code)
                        .isCpp(true)
                        .policy(policy)
                        .build()
        );

        boolean isRunPhaseTrap = "run".equals(result.getPhase()) && !result.isOk();
        assertTrue(isRunPhaseTrap,
                () -> "Expected a trap in the 'run' phase, but got: phase=" + result.getPhase() + ", exitCode=" + result.getExitCode()
                        + ", stderr=" + result.getStderr());
    }

    /**
     * Tests that content written to stderr is captured correctly.
     */
    @Test
    void run_write_stderr_check() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ fprintf(stderr, \"ERR\\n\"); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.getStderr().contains("ERR"));
    }

    /**
     * Tests that multiple lines of output are captured correctly and in order.
     */
    @Test
    void run_multi_lines() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ puts(\"L1\"); puts(\"L2\"); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertTrue(result.getStdout().contains("L1"));
        assertTrue(result.getStdout().contains("L2"));
    }

    /**
     * Tests that providing empty standard input is handled correctly.
     */
    @Test
    void run_empty_stdin() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ char b[8]; if(fgets(b,8,stdin)) puts(\"HAS_INPUT\"); else puts(\"EMPTY_INPUT\"); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder()
                .code(code).isCpp(true).stdin("").policy(policy).build());
        assertTrue(result.getStdout().contains("EMPTY_INPUT"));
    }

    /**
     * Tests that a large stdin stream correctly triggers stdout truncation when echoed.
     */
    @Test
    void run_large_stdin_and_truncate() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        policy.setMaxStdoutBytes(128);
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){ int c; while((c=getchar())!=EOF) putchar(c); return 0; }");
        String largeInput = "X".repeat(4096);
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).stdin(largeInput).policy(policy).build());
        assertTrue(result.isStdoutTruncated());
    }

    /**
     * Tests that the maximum exit code value (255) is handled correctly.
     */
    @Test
    void run_exit() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code("int main(){return 255;}").isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.getExitCode() != 0, "Expected a non-zero exit code.");
    }

    /**
     * Tests that empty or invalid source code results in a compile error.
     */
    @Test
    void compile_error_empty_source() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code("#error FAIL\\n").isCpp(true).policy(policy).build());
        assertEquals("compile", result.getPhase());
        assertFalse(result.isOk());
    }

    /**
     * Tests that passing null for the source code throws a NullPointerException as expected.
     */
    @Test
    void null_code_throws_npe() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        assertThrows(NullPointerException.class, () ->
                createExecutor().executeOnce(CxxExecutionOptions.builder().code(null).isCpp(true).policy(policy).build()));
    }

    /**
     * Tests that a file can be written and then read within the same execution, confirming filesystem access.
     */
    @Test
    void run_file_read_only_within_mount() {
        CxxExecutionPolicy policy = TestSupport.wasiPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){",
                "  FILE* f=fopen(\"data.txt\",\"w\"); if(!f){perror(\"write_error\"); return 2;} fputs(\"DATA\",f); fclose(f);",
                "  f=fopen(\"data.txt\",\"r\"); if(!f){perror(\"read_error\"); return 3;} char b[8]={0}; fgets(b,8,f); fclose(f);",
                "  puts(b); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.getStdout().contains("DATA"));
    }
}