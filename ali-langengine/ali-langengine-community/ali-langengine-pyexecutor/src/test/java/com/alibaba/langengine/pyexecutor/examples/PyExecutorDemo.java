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

package com.alibaba.langengine.pyexecutor.examples;

import com.alibaba.langengine.pyexecutor.PyExecutor;
import com.alibaba.langengine.pyexecutor.PyExecutionPolicy;
import com.alibaba.langengine.pyexecutor.PyExecutionResult;
import com.alibaba.langengine.pyexecutor.SessionConfig;

/**
 * A standalone example demonstrating the usage of PyExecutor without Spring Boot.
 * This class includes a main method that showcases stateless, stateful, and error handling scenarios.
 */
public class PyExecutorDemo {

    public static void main(String[] args) {
        // --- 1. Configure the Execution Policy ---
        // PyExecutionPolicy defines security and resource limits.
        PyExecutionPolicy policy = new PyExecutionPolicy();
        // Specify the path to the Python interpreter. Use an absolute path if 'python3' is not in the system's PATH.
        policy.setPythonBin("python3");
        // Set a global timeout of 10 seconds.
        policy.setTimeout(java.time.Duration.ofSeconds(10));
        // Allow file access in read-only mode but forbid writing.
        policy.setDisableOpen(false);
        policy.setAllowReadonlyOpen(true);


        // --- 2. Configure the Session Manager ---
        // SessionConfig is used for stateful session-based execution.
        SessionConfig sessionConfig = new SessionConfig();
        // Set the working directory for sessions in the system's temp folder to avoid clutter.
        sessionConfig.setWorkspaceRoot(System.getProperty("java.io.tmpdir"));


        // --- 3. Create the PyExecutor Instance ---
        // Pass both policy and sessionConfig to enable both execution modes.
        PyExecutor pyExecutor = new PyExecutor(policy, sessionConfig);

        System.out.println("PyExecutor Demo Started.\n");

        // --- 4. Run All Examples ---
        runStatelessExample(pyExecutor);
        runStatefulSessionExample(pyExecutor);
        runErrorHandlingExample(pyExecutor);

        System.out.println("\nPyExecutor Demo Finished.");
    }

    /**
     * Demonstrates oneshot (stateless) execution.
     * A new, isolated Python process is started for each call.
     *
     * @param pyExecutor The executor instance to use.
     */
    public static void runStatelessExample(PyExecutor pyExecutor) {
        System.out.println("--- Running Stateless (executeOnce) Example ---");
        try {
            String code = "a = 10\nb = 20\na + b";
            PyExecutionResult result = pyExecutor.executeOnce(code, null);

            if (result.getExitCode() == 0) {
                System.out.println("Execution successful. Last expression value: " + result.getLastValueRepr());
                System.out.println("   Full stdout: " + result.getStdout());
            } else {
                System.err.println("Execution failed with exit code: " + result.getExitCode());
                System.err.println("   Error details: " + result.getErrorRepr());
                System.err.println("   Stderr: " + result.getStderr());
            }
        } catch (Exception e) {
            System.err.println("An exception occurred during executeOnce: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("-------------------------------------------------\n");
    }

    /**
     * Demonstrates session-based (stateful) execution.
     * Multiple calls with the same session ID share variables and state.
     *
     * @param pyExecutor The executor instance to use.
     */
    public static void runStatefulSessionExample(PyExecutor pyExecutor) {
        System.out.println("--- Running Stateful (Session) Example ---");
        String sessionId = "my-unique-session-123";
        try {
            // First call: Define a variable in the session.
            System.out.println("Step 1: Defining variable 'x' in session '" + sessionId + "'");
            PyExecutionResult result1 = pyExecutor.execute(sessionId, "x = 150", null);
            if (result1.getExitCode() != 0) {
                System.err.println("Failed to define variable.");
                return;
            }
            System.out.println("   Variable 'x' set.");

            // Second call: Reference the variable in the same session.
            System.out.println("Step 2: Using variable 'x' in the same session.");
            PyExecutionResult result2 = pyExecutor.execute(sessionId, "x * 2", null);

            if (result2.getExitCode() == 0) {
                System.out.println("Execution successful. Result of 'x * 2': " + result2.getLastValueRepr());
            } else {
                System.err.println("Execution failed: " + result2.getErrorRepr());
            }

        } catch (Exception e) {
            System.err.println("An exception occurred during session execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Good practice: Close the session after use to release resources.
            System.out.println("Step 3: Closing session '" + sessionId + "'");
            pyExecutor.closeSession(sessionId);
        }
        System.out.println("--------------------------------------------\n");
    }

    /**
     * Demonstrates error handling.
     * The executor catches Python exceptions and reports them in the result object.
     *
     * @param pyExecutor The executor instance to use.
     */
    public static void runErrorHandlingExample(PyExecutor pyExecutor) {
        System.out.println("--- Running Error Handling Example ---");
        try {
            String badCode = "print('About to divide by zero...')\n1 / 0";
            PyExecutionResult result = pyExecutor.executeOnce(badCode, null);

            System.out.println("Execution finished. Analyzing results...");
            System.out.println("   Exit Code: " + result.getExitCode());
            System.out.println("   Stdout: " + result.getStdout());
            System.out.println("   Error Repr: " + result.getErrorRepr());
            System.out.println("   Stderr contains traceback: " + (result.getStderr() != null && !result.getStderr().isEmpty()));

        } catch (Exception e) {
            System.err.println("This part should not be reached if the library handles Python errors gracefully.");
        }
        System.out.println("--------------------------------------\n");
    }
}