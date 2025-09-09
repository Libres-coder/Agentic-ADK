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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains unit tests for {@link CxxExecutor} using the NsJail backend.
 */
public class CxxExecutorNsjailTests {

    private CxxExecutor createExecutor() {
        return new CxxExecutor();
    }

    /**
     * Tests a successful execution of a simple C program.
     */
    @Test
    void nsjail_run_ok() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        String code = String.join("\n",
                "#include <stdio.h>",
                "int main(){ puts(\"NSJ OK\"); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(false).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.getStdout().contains("NSJ OK"));
    }

    /**
     * Tests that a syntax error during compilation is correctly reported.
     */
    @Test
    void nsjail_compile_error() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code("int main(){ BROKEN }").isCpp(false).policy(policy).build());
        assertEquals("compile", result.getPhase());
        assertFalse(result.isOk());
    }

    /**
     * Tests that an infinite loop correctly results in a runtime timeout.
     */
    @Test
    void nsjail_run_timeout() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        policy.setExtraCompileFlags(new String[]{"-O0"});
        policy.setRunTimeoutMs(500);
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code("int main(){for(;;){} }").isCpp(false).policy(policy).build());
        assertFalse(result.isOk());
    }

    /**
     * Tests that accessing a restricted absolute path (e.g., /etc/passwd) is denied by the sandbox.
     */
    @Test
    void nsjail_access_absolute_path_denied() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        String code = String.join("\n",
                "#include <stdio.h>",
                "int main(){ FILE* f=fopen(\"/etc/passwd\",\"r\"); if(!f){ puts(\"DENIED\"); return 7;} fclose(f); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(false).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertFalse(result.isOk());
        assertTrue(result.getStdout().contains("DENIED"));
    }

    /**
     * Tests that the include guard correctly denies a blacklisted header.
     */
    @Test
    void nsjail_guard_socket_header_denied() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        String code = String.join("\n",
                "#include <sys/socket.h>",
                "int main(){return 0;}");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(false).policy(policy).build());
        assertEquals("compile", result.getPhase());
        assertFalse(result.isOk());
    }

    /**
     * Tests that stdout is correctly truncated when it exceeds the specified limit.
     */
    @Test
    void nsjail_stdout_truncate() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        policy.setMaxStdoutBytes(64);
        String code = String.join("\n",
                "#include <stdio.h>",
                "int main(){ for(int i=0;i<1000;i++) putchar('A'); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(false).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.isStdoutTruncated());
    }

    /**
     * Tests that a non-zero exit code is correctly reported.
     */
    @Test
    void nsjail_nonzero_exit() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code("int main(){return 13;}").isCpp(false).policy(policy).build());
        assertEquals("run", result.getPhase());
        assertEquals(13, result.getExitCode());
    }

    /**
     * Tests that standard input is correctly passed to and echoed by the program.
     */
    @Test
    void nsjail_stdin_echo() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        String code = String.join("\n",
                "#include <stdio.h>",
                "int main(){ int c; while((c=getchar())!=EOF) putchar(c); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(false).stdin("echo\n").policy(policy).build());
        assertEquals("run", result.getPhase());
        assertTrue(result.getStdout().contains("echo"));
    }

    /**
     * Tests that the current working directory inside the sandbox is /work.
     */
    @Test
    void nsjail_cwd_is_work() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        String code = String.join("\n",
                "#include <stdio.h>",
                "#include <unistd.h>",
                "#include <limits.h>",
                "int main(){ char b[PATH_MAX];",
                "  if(!getcwd(b,sizeof(b))){ perror(\"getcwd\"); return 2; }",
                "  puts(b); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(false).policy(policy).build());
        Assertions.assertEquals("run", result.getPhase());
        Assertions.assertTrue(result.getStdout().contains("/work"));
        Assertions.assertTrue(result.isOk());
    }

    /**
     * Tests that files can be written to and read from the /work directory.
     */
    @Test
    void nsjail_workdir_write_read_ok() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){",
                "  FILE* f=fopen(\"hello.txt\",\"w\"); if(!f){perror(\"w\"); return 2;}",
                "  fputs(\"HI\",f); fclose(f);",
                "  f=fopen(\"hello.txt\",\"r\"); if(!f){perror(\"r\"); return 3;}",
                "  char b[8]={0}; fgets(b,8,f); fclose(f);",
                "  puts(b); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        Assertions.assertEquals("run", result.getPhase());
        Assertions.assertTrue(result.getStdout().contains("HI"));
        Assertions.assertTrue(result.isOk());
    }

    /**
     * Tests that the /tmp directory is writable inside the sandbox.
     */
    @Test
    void nsjail_tmp_write_ok() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        String code = String.join("\n",
                "#include <cstdio>",
                "int main(){",
                "  FILE* f=fopen(\"/tmp/nsj_tmp.txt\",\"w\"); if(!f){perror(\"w\"); return 2;}",
                "  fputs(\"TMP\",f); fclose(f);",
                "  f=fopen(\"/tmp/nsj_tmp.txt\",\"r\"); if(!f){perror(\"r\"); return 3;}",
                "  char b[8]={0}; fgets(b,8,f); fclose(f);",
                "  puts(b); return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(true).policy(policy).build());
        Assertions.assertEquals("run", result.getPhase());
        Assertions.assertTrue(result.getStdout().contains("TMP"));
        Assertions.assertTrue(result.isOk());
    }

    /**
     * Tests that attempts to escape the sandbox via symlinks fail as expected.
     */
    @Test
    void nsjail_symlink_escape_denied() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        String code = String.join("\n",
                "#include <stdio.h>",
                "#include <unistd.h>",
                "#include <errno.h>",
                "int main(){",
                "  unlink(\"escape\");",
                "  if (symlink(\"/\", \"escape\")!=0) { perror(\"symlink\"); return 2; }",
                "  FILE* f=fopen(\"escape/etc/hosts\", \"r\");",
                "  if (f){ fclose(f); return 3; }",
                "  return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(false).policy(policy).build());
        Assertions.assertEquals("run", result.getPhase());
        Assertions.assertEquals(0, result.getExitCode());
    }

    /**
     * Tests that stderr is correctly truncated when it exceeds the specified limit.
     */
    @Test
    void nsjail_stderr_truncate() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        policy.setMaxStderrBytes(8 * 1024);
        String code = String.join("\n",
                "#include <stdio.h>",
                "int main(){",
                "  for(int i=0;i<200000;i++) fputc('E', stderr);",
                "  return 0; }");
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder().code(code).isCpp(false).policy(policy).build());
        Assertions.assertEquals("run", result.getPhase());
        Assertions.assertTrue(result.isOk());
        Assertions.assertTrue(result.getStderr().length() < 200000);
        Assertions.assertTrue(result.getStderr().length() >= 1);
    }

    /**
     * Tests that a large stdin stream correctly triggers stdout truncation when echoed.
     */
    @Test
    void nsjail_large_stdin_echo_truncated() {
        CxxExecutionPolicy policy = TestSupport.nsjailPolicyOrSkip();
        policy.setMaxStdoutBytes(1024);
        String code = String.join("\n",
                "#include <stdio.h>",
                "int main(){ int c; while((c=getchar())!=EOF) putchar(c); return 0; }");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100_000; i++) sb.append('A');
        var result = createExecutor().executeOnce(CxxExecutionOptions.builder()
                .code(code).isCpp(false).stdin(sb.toString()).policy(policy).build());
        Assertions.assertEquals("run", result.getPhase());
        Assertions.assertTrue(result.getStdout().length() < 100_000);
        Assertions.assertTrue(result.getStdout().length() <= 1025);
    }
}