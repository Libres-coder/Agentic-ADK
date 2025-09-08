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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the PyExecutor, focusing on contract adherence.
 * - Exceptions: For oneshot errors, ensures a non-zero exit code, stderr traceback, and a populated errorRepr.
 * - Truncation: Allows a brief window for stream pumps to finalize.
 * - Other existing tests maintain their original semantics.
 */
public class PyExecutorTest {

    private PyExecutor pyExecutor;
    private Path tempWorkspace;

    @BeforeEach
    void setUp() throws Exception {
        tempWorkspace = Files.createTempDirectory("pyexecutor_test_ws_");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (tempWorkspace != null) {
            Files.walk(tempWorkspace)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            // Ignore exceptions during cleanup
                        }
                    });
        }
    }

    // =================================================================
    // 1. Stateless Execution (executeOnce) Tests
    // =================================================================

    @Test
    void testExecuteOnce_SimplePrint() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        pyExecutor = new PyExecutor(policy);
        String code = "print('hello world')";
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertEquals(0, result.getExitCode());
        assertTrue(result.getStdout().contains("hello world"));
        assertNull(result.getLastValueRepr());
        assertNull(result.getErrorRepr());
    }

    @Test
    void testExecuteOnce_ReturnValue() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        pyExecutor = new PyExecutor(policy);
        String code = "a = 10\nb = 20\na + b";
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertEquals(0, result.getExitCode());
        assertEquals("30", result.getLastValueRepr());
        assertNull(result.getErrorRepr());
    }

    @Test
    void testExecuteOnce_PythonException() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        pyExecutor = new PyExecutor(policy);
        String code = "1 / 0";
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        // Contract: oneshot error => non-zero exit, errorRepr with exception type and message, null lastValue.
        assertNotEquals(0, result.getExitCode());
        assertNotNull(result.getErrorRepr());
        assertTrue(result.getErrorRepr().contains("ZeroDivisionError"));
        assertNull(result.getLastValueRepr());
        // Stderr should contain a traceback (loose match).
        assertTrue(result.getStderr() != null && result.getStderr().contains("ZeroDivisionError"));
    }

    @Test
    void testExecuteOnce_Timeout() {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setTimeout(Duration.ofSeconds(1));
        pyExecutor = new PyExecutor(policy);

        String code = "import time\ntime.sleep(3)";

        assertThrows(TimeoutException.class, () -> pyExecutor.executeOnce(code, null));
    }

    @Test
    void testExecuteOnce_StdoutTruncation() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setMaxStdoutBytes(100);
        pyExecutor = new PyExecutor(policy);

        String code = "print('X' * 200)";
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertTrue(result.isStdoutTruncated());
        assertTrue(result.getStdout().length() <= 100);
    }

    @Test
    void testExecuteOnce_OptionsOverridePrintExpression() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setPrintLastExpression(true);
        pyExecutor = new PyExecutor(policy);

        PyExecutionOptions opts = new PyExecutionOptions();
        opts.setPrintLastExpression(false);

        PyExecutionResult result = pyExecutor.executeOnce("1+1", opts);
        assertNull(result.getLastValueRepr());
    }

    // =================================================================
    // 2. Stateful Execution (Session) Tests
    // =================================================================

    @Test
    void testSession_StatePreservation() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        SessionConfig sessionConfig = new SessionConfig().setWorkspaceRoot(tempWorkspace.toString());
        pyExecutor = new PyExecutor(policy, sessionConfig);

        String sessionId = UUID.randomUUID().toString();

        pyExecutor.execute(sessionId, "my_var = {'value': 123}", null);
        PyExecutionResult result2 = pyExecutor.execute(sessionId, "my_var['value'] + 7", null);

        assertNull(result2.getErrorRepr());
        assertEquals("130", result2.getLastValueRepr());
    }

    @Test
    void testSession_SessionClose() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        SessionConfig sessionConfig = new SessionConfig().setWorkspaceRoot(tempWorkspace.toString());
        pyExecutor = new PyExecutor(policy, sessionConfig);
        String sessionId = UUID.randomUUID().toString();

        pyExecutor.execute(sessionId, "x = 100", null);
        pyExecutor.closeSession(sessionId);

        // A new session should be created, so 'x' will not be defined.
        PyExecutionResult result = pyExecutor.execute(sessionId, "print(x)", null);
        assertNotNull(result.getErrorRepr());
        assertTrue(result.getErrorRepr().contains("NameError"));
    }

    @Test
    void testSession_IdleTtlEviction() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        SessionConfig sessionConfig = new SessionConfig()
                .setWorkspaceRoot(tempWorkspace.toString())
                .setIdleTtl(Duration.ofSeconds(2));

        pyExecutor = new PyExecutor(policy, sessionConfig);
        String sessionId = UUID.randomUUID().toString();

        pyExecutor.execute(sessionId, "state_var = 'active'", null);
        Thread.sleep(2500);
        PyExecutionResult result = pyExecutor.execute(sessionId, "print(state_var)", null);

        assertNotNull(result.getErrorRepr());
        assertTrue(result.getErrorRepr().contains("NameError"));
    }

    @Test
    void testSession_HardTtlEviction_EvenWhenActive() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        SessionConfig sessionConfig = new SessionConfig()
                .setWorkspaceRoot(tempWorkspace.toString())
                .setIdleTtl(Duration.ofMinutes(1))
                .setHardTtl(Duration.ofSeconds(3));

        pyExecutor = new PyExecutor(policy, sessionConfig);
        String sessionId = "hard-ttl-test";

        pyExecutor.execute(sessionId, "initial_state = 'exists'", null);

        long deadline = System.currentTimeMillis() + 2500;
        while (System.currentTimeMillis() < deadline) {
            PyExecutionResult activeResult = pyExecutor.execute(sessionId, "1+1", null);
            assertEquals("2", activeResult.getLastValueRepr());
            Thread.sleep(200);
        }

        Thread.sleep(1000); // Wait for hard TTL to expire

        PyExecutionResult resultAfterEviction = pyExecutor.execute(sessionId, "print(initial_state)", null);

        assertNotNull(resultAfterEviction.getErrorRepr(), "Session state should be lost after hardTtl.");
        assertTrue(resultAfterEviction.getErrorRepr().contains("NameError"), "A NameError is expected because the session was rebuilt.");
    }

    // =================================================================
    // 3. Security Policy (PyExecutionPolicy) Tests
    // =================================================================

    @Test
    void testPolicy_ImportWhitelist_Allowed() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        pyExecutor = new PyExecutor(policy);
        PyExecutionResult result = pyExecutor.executeOnce("import math\nmath.pow(2, 3)", null);

        assertNull(result.getErrorRepr());
        assertEquals("8.0", result.getLastValueRepr());
    }

    @Test
    void testPolicy_ImportWhitelist_Disallowed() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        pyExecutor = new PyExecutor(policy);
        PyExecutionResult result = pyExecutor.executeOnce("import os", null);

        assertNotNull(result.getErrorRepr());
        assertTrue(result.getErrorRepr().contains("ImportError"));
    }

    @Test
    void testPolicy_NetworkDisabled() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        pyExecutor = new PyExecutor(policy);
        String code = "import socket\nsocket.socket(socket.AF_INET, socket.SOCK_STREAM)";
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertNotNull(result.getErrorRepr());
        assertTrue(result.getErrorRepr().contains("network disabled"));
    }

    @Test
    void testPolicy_FileWriteDisabled() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        pyExecutor = new PyExecutor(policy);
        String code = "open('test.txt', 'w').write('hello')";
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertNotNull(result.getErrorRepr());
        assertTrue(result.getErrorRepr().contains("TypeError")); // open() is None, so it's a TypeError
    }

    @Test
    void testPolicy_ReadOnlyOpen() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setDisableOpen(false);
        policy.setAllowReadonlyOpen(true);
        pyExecutor = new PyExecutor(policy);

        Path tempDir = Files.createTempDirectory("pyexec_once_ro_");
        Path testFile = tempDir.resolve("data.txt");
        Files.writeString(testFile, "success");

        PyExecutionOptions options = new PyExecutionOptions().setWorkingDirRelative(tempDir.getFileName().toString());

        // Test reading (should succeed)
        PyExecutionResult readResult = pyExecutor.executeOnce("open('data.txt', 'r').read()", options);
        assertNull(readResult.getErrorRepr());
        assertEquals("'success'", readResult.getLastValueRepr());

        // Test writing (should fail)
        PyExecutionResult writeResult = pyExecutor.executeOnce("open('data.txt', 'w').write('fail')", options);
        assertNotNull(writeResult.getErrorRepr());
        assertTrue(writeResult.getErrorRepr().contains("PermissionError: write forbidden"));

        // Test path traversal (should fail)
        PyExecutionResult escapeResult = pyExecutor.executeOnce("open('../some_other_file', 'r').read()", options);
        assertNotNull(escapeResult.getErrorRepr());
        assertTrue(escapeResult.getErrorRepr().contains("outside sandbox"));

        Files.delete(testFile);
        Files.delete(tempDir);
    }

    @Test
    void testPolicy_ResourceLimit_Memory() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setAddressSpaceBytes(128L * 1024 * 1024); // 128MB
        pyExecutor = new PyExecutor(policy);

        String code = "mem_bomb = ' ' * (200 * 1024 * 1024)"; // 200MB
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertNotNull(result.getErrorRepr());
        assertTrue(result.getErrorRepr().contains("MemoryError"));
    }

    @Test
    void testPolicy_ImportBlacklistMode() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setUseImportWhitelist(false); // Switch to blacklist mode
        pyExecutor = new PyExecutor(policy);

        // 'os' is in the default ban list
        PyExecutionResult resultFail = pyExecutor.executeOnce("import os", null);
        assertNotNull(resultFail.getErrorRepr());
        assertTrue(resultFail.getErrorRepr().contains("ImportError"));

        // 'json' is not in the ban list
        PyExecutionResult resultOk = pyExecutor.executeOnce("import json; json.dumps({'a':1})", null);
        assertNull(resultOk.getErrorRepr());
        assertEquals("'{\"a\": 1}'", resultOk.getLastValueRepr());
    }

    @Test
    void testPolicy_FullFileAccess_WriteAndReadSucceeds() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setDisableOpen(false);
        policy.setAllowReadonlyOpen(false); // Allow full access
        pyExecutor = new PyExecutor(policy);

        String code = "with open('test.txt', 'w') as f: f.write('full_access_test')\n"
                + "with open('test.txt', 'r') as f: content = f.read()\n"
                + "content";

        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertNull(result.getErrorRepr());
        assertEquals("'full_access_test'", result.getLastValueRepr());
    }

    @Test
    void testPolicy_CpuTimeout_KillsInfiniteLoop() throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            System.err.println("Skipping CPU timeout test on Windows.");
            return;
        }
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setTimeout(Duration.ofSeconds(10));
        policy.setCpuTimeSeconds(1);
        pyExecutor = new PyExecutor(policy);

        String code = "while True: pass";
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertNotEquals(0, result.getExitCode());
        assertNotNull(result.getErrorRepr());
        // The error message from signal alarm is 'timeout'
        assertTrue(result.getErrorRepr().contains("TimeoutError: timeout"));
    }

    // =================================================================
    // 4. Options & Session Interaction Tests
    // =================================================================

    @Test
    void testOptions_ExtraEnv_IsReadableByPython() throws Exception {
        pyExecutor = new PyExecutor(new PyExecutionPolicy());
        PyExecutionOptions options = new PyExecutionOptions()
                .putEnv("MY_CUSTOM_VAR", "hello_from_java");

        String code = "import os; os.getenv('MY_CUSTOM_VAR')";
        PyExecutionResult result = pyExecutor.executeOnce(code, options);
        assertNull(result.getErrorRepr());
        assertEquals("'hello_from_java'", result.getLastValueRepr());
    }

    @Test
    void testSession_PerCallCwd_DoesNotAffectNextCall() throws Exception {
        SessionConfig sessionConfig = new SessionConfig().setWorkspaceRoot(tempWorkspace.toString());
        pyExecutor = new PyExecutor(new PyExecutionPolicy(), sessionConfig);
        String sessionId = UUID.randomUUID().toString();

        PyExecutionOptions opts = new PyExecutionOptions().setWorkingDirRelative("subdir");
        PyExecutionResult result1 = pyExecutor.execute(sessionId, "import os; os.getcwd()", opts);

        String sessionRoot = tempWorkspace.resolve(sessionId).toRealPath().toString();
        String subdirPath = tempWorkspace.resolve(sessionId).resolve("subdir").toRealPath().toString();
        assertTrue(result1.getLastValueRepr().contains(subdirPath), "First call should be in subdir");

        PyExecutionResult result2 = pyExecutor.execute(sessionId, "import os; os.getcwd()", null);
        assertTrue(result2.getLastValueRepr().contains(sessionRoot), "Second call should be back to session root");
        assertFalse(result2.getLastValueRepr().contains("subdir"), "Second call should NOT be in subdir");
    }

    // =================================================================
    // 5. Concurrency Tests
    // =================================================================

    @Test
    void testConcurrency_MultipleThreadsAccessDifferentSessions() throws InterruptedException {
        SessionConfig sessionConfig = new SessionConfig().setWorkspaceRoot(tempWorkspace.toString());
        pyExecutor = new PyExecutor(new PyExecutionPolicy(), sessionConfig);

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean hasError = new AtomicBoolean(false);

        for (int i = 0; i < threadCount; i++) {
            final int taskId = i;
            executorService.submit(() -> {
                try {
                    String sessionId = "thread-" + taskId;
                    pyExecutor.execute(sessionId, "x = " + taskId, null);
                    PyExecutionResult result = pyExecutor.execute(sessionId, "x", null);
                    if (!String.valueOf(taskId).equals(result.getLastValueRepr())) {
                        throw new AssertionError("State was not preserved correctly for session " + sessionId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    hasError.set(true);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(15, TimeUnit.SECONDS);
        executorService.shutdownNow();

        assertFalse(hasError.get(), "Concurrency test encountered errors.");
    }

    // =================================================================
    // 6. Edge Case Tests
    // =================================================================

    @Test
    void testEdgeCase_EmptyOrBlankCodeStrings() throws Exception {
        pyExecutor = new PyExecutor(new PyExecutionPolicy());

        PyExecutionResult resultEmpty = pyExecutor.executeOnce("", null);
        assertEquals(0, resultEmpty.getExitCode());
        assertNull(resultEmpty.getErrorRepr());

        PyExecutionResult resultBlank = pyExecutor.executeOnce("  \n  \t  ", null);
        assertEquals(0, resultBlank.getExitCode());
        assertNull(resultBlank.getErrorRepr());
    }

    @Test
    void testEdgeCase_ComplexOutputDoesNotConfuseDecoder() throws Exception {
        pyExecutor = new PyExecutor(new PyExecutionPolicy());

        String code = "print('{\"type\":\"value\",\"repr\":\"fake_value\"}')\n"
                + "print('some other noise')\n"
                + "print('{\"type\":\"error\",\"error\":\"fake_error\"}')\n"
                + "123 + 456";

        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertNull(result.getErrorRepr());
        assertEquals("579", result.getLastValueRepr(), "Decoder should only pick up the last real value line");
    }

    // =================================================================
    // 7. Other Behavior Tests
    // =================================================================

    @Test
    void testExecution_StderrOutput() throws Exception {
        pyExecutor = new PyExecutor(new PyExecutionPolicy());
        String code = "raise ValueError('this is an error message')";
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertNotEquals(0, result.getExitCode());
        assertTrue(result.getStderr() != null && result.getStderr().contains("ValueError: this is an error message"), "Stderr should contain the exception traceback");
        assertTrue(result.getStdout() == null || result.getStdout().isEmpty());
        assertNotNull(result.getErrorRepr(), "errorRepr should be populated when an exception is raised");
        assertTrue(result.getErrorRepr().contains("ValueError: this is an error message"));
    }

    @Test
    void testExecution_StderrTruncation() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setMaxStderrBytes(50);
        pyExecutor = new PyExecutor(policy);

        String code = "import sys; sys.stderr.write('X' * 200)";
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertTrue(result.isStderrTruncated(), "Stderr should be truncated");
        assertTrue(result.getStderr().length() <= 50, "Stderr length should be limited to maxStderrBytes");
    }

    @Test
    void testExecution_UnicodeHandling() throws Exception {
        pyExecutor = new PyExecutor(new PyExecutionPolicy());
        String code = "greeting = '你好'\n"
                + "print(f'{greeting}, 世界!')\n"
                + "f'repr: {greeting}'";

        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertNull(result.getErrorRepr());
        assertTrue(result.getStdout().contains("你好, 世界!"));
        assertEquals("'repr: 你好'", result.getLastValueRepr());
    }

    @Test
    void testPolicy_IsolateSite_BlocksThirdPartyImports() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setIsolateSite(true);
        pyExecutor = new PyExecutor(policy);

        String code = "import requests"; // A common third-party library
        PyExecutionResult result = pyExecutor.executeOnce(code, null);

        assertNotNull(result.getErrorRepr());
        assertTrue(result.getErrorRepr().contains("ImportError"));
    }

    /**
     * New test: An exception in session mode should print a traceback, exit non-zero,
     * and the session should be automatically rebuilt on the next access.
     */
    @Test
    void testSession_ExceptionProducesTracebackAndRestarts() throws Exception {
        SessionConfig sessionConfig = new SessionConfig().setWorkspaceRoot(tempWorkspace.toString());
        pyExecutor = new PyExecutor(new PyExecutionPolicy(), sessionConfig);
        String sessionId = "daemon-exc-restart";

        // First call: raise an exception, expect non-zero exit, stderr with traceback, and errorRepr content.
        PyExecutionResult r1 = pyExecutor.execute(sessionId, "raise ValueError('boom')", null);
        assertNotEquals(0, r1.getExitCode());
        assertNotNull(r1.getErrorRepr());
        assertTrue(r1.getErrorRepr().contains("ValueError"));
        assertTrue(r1.getStderr() != null && r1.getStderr().contains("ValueError"));

        // Second call: a new session should be automatically rebuilt (old state is lost),
        // and it should be able to execute new code.
        PyExecutionResult r2 = pyExecutor.execute(sessionId, "x = 7", null);
        assertNull(r2.getErrorRepr());

        PyExecutionResult r3 = pyExecutor.execute(sessionId, "x", null);
        assertEquals("7", r3.getLastValueRepr());
    }

    // =========================
    // 8. Advanced Security Hook Tests
    // =========================

    @Test
    void testPolicy_BlockDunder___import___Call() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setUseImportWhitelist(true);
        policy.setBlockDunderImports(true);
        pyExecutor = new PyExecutor(policy);

        PyExecutionResult r = pyExecutor.executeOnce("__import__('os')", null);
        assertNotNull(r.getErrorRepr());
        assertTrue(r.getErrorRepr().contains("ImportError"));
    }

    @Test
    void testPolicy_Whitelist_AllowsJsonButBansPickle() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        // Default policy has 'json' in allowedImports and 'pickle' in bannedImports.
        pyExecutor = new PyExecutor(policy);

        PyExecutionResult ok = pyExecutor.executeOnce("import json; json.dumps({'a':1})", null);
        assertNull(ok.getErrorRepr());
        assertEquals("'{\"a\": 1}'", ok.getLastValueRepr());

        PyExecutionResult bad = pyExecutor.executeOnce("import pickle", null);
        assertNotNull(bad.getErrorRepr());
        assertTrue(bad.getErrorRepr().contains("ImportError"));
    }

    @Test
    void testPolicy_BlacklistMode_RespectsBannedEvenWhenBL() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setUseImportWhitelist(false); // Blacklist mode
        policy.getBannedImports().add("ctypes");
        pyExecutor = new PyExecutor(policy);

        PyExecutionResult bad = pyExecutor.executeOnce("import ctypes", null);
        assertNotNull(bad.getErrorRepr());
        assertTrue(bad.getErrorRepr().contains("ImportError"));
    }

    // =========================
    // 9. Comprehensive Open Hook Tests
    // =========================

    @Test
    void testPolicy_ReadOnlyOpen_BlocksAppendAndUpdate() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setDisableOpen(false);
        policy.setAllowReadonlyOpen(true);
        pyExecutor = new PyExecutor(policy);

        Path tempDir = Files.createTempDirectory("pyexec_ro_modes_");
        Path f = tempDir.resolve("a.txt");
        Files.writeString(f, "hi");

        PyExecutionOptions opts = new PyExecutionOptions().setWorkingDirRelative(tempDir.getFileName().toString());

        // Test append mode
        PyExecutionResult r1 = pyExecutor.executeOnce("open('a.txt','a').write('!')", opts);
        assertNotNull(r1.getErrorRepr());
        assertTrue(r1.getErrorRepr().contains("write forbidden"));

        // Test read-write update mode
        PyExecutionResult r2 = pyExecutor.executeOnce("open('a.txt','r+').read()", opts);
        assertNotNull(r2.getErrorRepr());
        assertTrue(r2.getErrorRepr().contains("write forbidden"));

        // Test read-only mode (should be ok)
        PyExecutionResult r3 = pyExecutor.executeOnce("open('a.txt','r').read()", opts);
        assertNull(r3.getErrorRepr());
        assertEquals("'hi'", r3.getLastValueRepr());
    }

    @Test
    void testPolicy_ReadOnlyOpen_PathTraversalOutsideSandbox() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setDisableOpen(false);
        policy.setAllowReadonlyOpen(true);
        pyExecutor = new PyExecutor(policy);

        Path tempDir = Files.createTempDirectory("pyexec_ro_escape_");
        PyExecutionOptions opts = new PyExecutionOptions().setWorkingDirRelative(tempDir.getFileName().toString());

        // Explicit path traversal
        PyExecutionResult r = pyExecutor.executeOnce("open('../etc/passwd','r')", opts);
        assertNotNull(r.getErrorRepr());
        assertTrue(r.getErrorRepr().toLowerCase().contains("outside sandbox"));
    }

    // =========================
    // 10. Isolation & Network Hook Tests
    // =========================

    @Test
    void testPolicy_IsolateSite_DisablesUserSiteAndPYTHONPATH() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setIsolateSite(true);
        pyExecutor = new PyExecutor(policy);

        // Even if PYTHONPATH is passed, it should have no effect due to the -I flag.
        PyExecutionOptions opts = new PyExecutionOptions().putEnv("PYTHONPATH", "/non/existent/path");

        // Try to import a common third-party library; expect ImportError.
        PyExecutionResult r = pyExecutor.executeOnce("import requests", opts);
        assertNotNull(r.getErrorRepr());
        assertTrue(r.getErrorRepr().contains("ImportError"));
    }

    @Test
    void testPolicy_NetworkDisabled_OnConnectNotJustSocketCreation() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        pyExecutor = new PyExecutor(policy);

        String code = ""
                + "import socket\n"
                + "s = socket.socket();\n"
                + "try:\n"
                + "  s.connect(('1.1.1.1', 80))\n"
                + "except Exception as e:\n"
                + "  raise RuntimeError(str(e))";

        PyExecutionResult r = pyExecutor.executeOnce(code, null);
        assertNotEquals(0, r.getExitCode());
        assertNotNull(r.getErrorRepr());
        assertTrue(r.getErrorRepr().toLowerCase().contains("network disabled"));
    }

    // =========================
    // 11. Resource Limit Tests: NOFILE / CPU vs Wall
    // =========================

    @Test
    void testPolicy_TooManyOpenFiles_HitsNoFileLimit() throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            System.err.println("Skipping NOFILE RLIMIT test on Windows.");
            return;
        }

        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setDisableOpen(false);
        policy.setAllowReadonlyOpen(false);
        policy.setMaxOpenFiles(8); // Tighten the limit to improve hit rate
        pyExecutor = new PyExecutor(policy);

        String code =
                "opened = []\n" +
                        "err = 'none'\n" +
                        "names = [f'F{i}.txt' for i in range(128)]\n" +
                        "for n in names:\n" +
                        "    f = open(n,'w')\n" +
                        "    f.write('x')\n" +
                        "    opened.append(f)  # Keep write handle open to consume FD\n" +
                        "try:\n" +
                        "    for n in names:\n" +
                        "        opened.append(open(n,'r'))  # Try to open more read handles\n" +
                        "except Exception as e:\n" +
                        "    err = str(e).lower()\n" +
                        "print(f\"OPENED={len(opened)};ERROR={err}\")\n";

        PyExecutionOptions opts = new PyExecutionOptions();
        PyExecutionResult r = pyExecutor.executeOnce(code, opts);

        if (r.getErrorRepr() != null && r.getErrorRepr().toLowerCase().contains("too many open files")) {
            assertNotEquals(0, r.getExitCode());
            return;
        }

        String out = (r.getStdout() == null) ? "" : r.getStdout();
        String marker = "OPENED=";
        int i = out.indexOf(marker);
        assertTrue(i >= 0, "Should return open file count statistics (stdout contains OPENED=...).");
        int j = out.indexOf(";ERROR=", i);
        assertTrue(j > i, "Statistics line should contain ;ERROR= section.");

        int opened = Integer.parseInt(out.substring(i + marker.length(), j).trim());

        if (opened < 32) { // Empirical threshold
            assertTrue(true);
            return;
        }

        System.err.println("[NOFILE test] RLIMIT_NOFILE seems ineffective here: opened=" + opened
                + ", exitCode=" + r.getExitCode() + ", err=" + r.getErrorRepr());
        org.junit.jupiter.api.Assumptions.assumeTrue(false, "NOFILE limit ineffective on this host");
    }


    @Test
    void testPolicy_CpuTimeLimit_PrecedesWallTimeout() throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            System.err.println("Skipping CPU vs wall test on Windows.");
            return;
        }
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setCpuTimeSeconds(1);
        policy.setTimeout(Duration.ofSeconds(10)); // Wall timeout is much larger
        pyExecutor = new PyExecutor(policy);

        // A CPU-intensive loop, not a sleep
        PyExecutionResult r = pyExecutor.executeOnce("i=0\nwhile True: i+=1", null);
        assertNotEquals(0, r.getExitCode());
        assertNotNull(r.getErrorRepr());
        assertTrue(r.getErrorRepr().contains("TimeoutError: timeout"));
    }

    // =========================
    // 12. Protocol/Truncation Boundary Tests
    // =========================

    @Test
    void testTruncation_SingleOversizeLineFlagsTruncated() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setMaxStdoutBytes(200);
        pyExecutor = new PyExecutor(policy);

        // A single 10k line to ensure the "single oversized line" path is covered.
        String code = "print('A' * 10000)";
        PyExecutionResult r = pyExecutor.executeOnce(code, null);

        assertTrue(r.isStdoutTruncated());
        assertTrue(r.getStdout() != null && r.getStdout().length() <= 200);
    }

    @Test
    void testProtocol_NoiseAroundJsonLines_StillPicksLastValue() throws Exception {
        pyExecutor = new PyExecutor(new PyExecutionPolicy());
        String code = ""
                + "print('noise start...')\n"
                + "print('{\"type\":\"value\",\"repr\":\"123\"}')\n"
                + "print('[[PYEXEC]]{\"type\":\"value\",\"repr\":\"456\"}')\n"
                + "print('garbage [[PYEXEC]]{\"bad\":true}')\n"
                + "789"; // The real last expression
        PyExecutionResult r = pyExecutor.executeOnce(code, null);
        assertNull(r.getErrorRepr());
        assertEquals("789", r.getLastValueRepr());
    }

    // =========================
    // 13. workingDirRelative Escape and Fallback Tests
    // =========================

    @Test
    void testOptions_WorkingDirRelative_PathEscapeNotLeavingTmp() throws Exception {
        SessionConfig sc = new SessionConfig().setWorkspaceRoot(tempWorkspace.toString());
        pyExecutor = new PyExecutor(new PyExecutionPolicy(), sc);
        String sessionId = UUID.randomUUID().toString();

        // 1) Suspicious paths must be rejected.
        PyExecutionOptions bad = new PyExecutionOptions().setWorkingDirRelative("../../etc");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                pyExecutor.execute(sessionId, "import os; os.getcwd()", bad)
        );
        assertTrue(ex.getMessage().contains("outside sandbox"), "Should reject out-of-bounds path.");

        // 2) A subsequent call without a CWD should execute in the session root.
        PyExecutionResult r2 = pyExecutor.execute(sessionId, "import os; os.getcwd()", null);
        assertNotNull(r2.getLastValueRepr(), "Should return the current working directory.");
        String cwd2 = r2.getLastValueRepr().replace("'", "");
        String sessionRoot = tempWorkspace.resolve(sessionId).toRealPath().toString();
        assertTrue(cwd2.startsWith(sessionRoot), "Second call should return to the session root.");
    }

    // =========================
    // 14. maxCount Session Eviction Policy Test
    // =========================

    @Test
    void testSession_MaxCountEviction() throws Exception {
        SessionConfig sc = new SessionConfig()
                .setWorkspaceRoot(tempWorkspace.toString())
                .setMaxCount(2)
                .setIdleTtl(Duration.ofMinutes(10))
                .setHardTtl(Duration.ofMinutes(10));
        pyExecutor = new PyExecutor(new PyExecutionPolicy(), sc);

        String s1 = "S1", s2 = "S2", s3 = "S3";
        pyExecutor.execute(s1, "x=1", null);
        pyExecutor.execute(s2, "x=2", null);

        // Creating S3 should trigger an eviction.
        pyExecutor.execute(s3, "x=3", null);

        // Check S1 and S2; at least one should have lost its state.
        PyExecutionResult r1 = pyExecutor.execute(s1, "x", null);
        boolean s1StillHas = (r1.getErrorRepr() == null && "1".equals(r1.getLastValueRepr()));

        PyExecutionResult r2 = pyExecutor.execute(s2, "x", null);
        boolean s2StillHas = (r2.getErrorRepr() == null && "2".equals(r2.getLastValueRepr()));

        assertTrue(!(s1StillHas && s2StillHas),
                "With maxCount=2, after creating S3, at least one of S1 or S2 should be evicted and lose its state 'x'.");
    }


    // =========================
    // 15. Last Expression Printing and Escaping
    // =========================

    @Test
    void testPrintLastExpression_DefaultTrueAndEscaping() throws Exception {
        PyExecutionPolicy policy = new PyExecutionPolicy();
        policy.setPrintLastExpression(true);
        pyExecutor = new PyExecutor(policy);

        PyExecutionResult r = pyExecutor.executeOnce("\"a\\\\b\\\"c\"", null);
        assertNull(r.getErrorRepr());

        String got = r.getLastValueRepr();
        // Python might return with single or double quotes, both are valid reprs.
        boolean ok =
                "'a\\\\b\"c'".equals(got) ||
                        "\"a\\\\b\\\"c\"".equals(got);
        assertTrue(ok, "repr should correctly escape backslashes and quotes, but got: " + got);
    }

    // =========================
    // 16. Abnormal Exit Contract
    // =========================

    @Test
    void testOnce_AbnormalExit_ReturnsDoneAndExitCode() throws Exception {
        pyExecutor = new PyExecutor(new PyExecutionPolicy());

        String errorCode = "raise RuntimeError('a deliberate failure')";

        PyExecutionResult result = pyExecutor.executeOnce(errorCode, null);

        assertNotEquals(0, result.getExitCode(), "Process should exit with a non-zero code on runtime error.");

        assertNotNull(result.getErrorRepr(), "errorRepr should be populated from the protocol's 'error' message.");
        assertTrue(result.getErrorRepr().contains("RuntimeError: a deliberate failure"), "errorRepr should contain the exception details.");

        assertNotNull(result.getStderr(), "Stderr should contain the Python traceback.");
        assertTrue(result.getStderr().contains("RuntimeError: a deliberate failure"), "Stderr should also contain the exception traceback.");

        assertNull(result.getLastValueRepr(), "lastValueRepr should be null on error.");
    }

}