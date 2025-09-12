package com.alibaba.langengine.sqlexecutor.examples;

import com.alibaba.langengine.sqlexecutor.SqlExecuteParams;
import com.alibaba.langengine.sqlexecutor.SqlExecuteResult;
import com.alibaba.langengine.sqlexecutor.SqlExecuteTool;

import java.sql.SQLException;
import java.util.Map;

/**
 * A runnable example demonstrating the core features of the {@link SqlExecuteTool}.
 * <p>
 * This program showcases:
 * <ul>
 * <li>Executing a DDL statement to create a table.</li>
 * <li>Running a parameterized INSERT statement that is safe for repeated executions.</li>
 * <li>Performing a parameterized SELECT query to fetch data.</li>
 * <li>Processing the {@link SqlExecuteResult} and formatting it as a Markdown table.</li>
 * <li>Handling common exceptions.</li>
 * </ul>
 */
public class ExecutorExample {

    public static void main(String[] args) {
        // The SqlExecuteTool is the main entry point for all operations.
        SqlExecuteTool tool = new SqlExecuteTool();

        // Use a file-based SQLite database. The file will be created if it doesn't exist.
        String jdbcUrl = "jdbc:sqlite:example.db";
        // The tool requires a username, even if the underlying driver (like SQLite) doesn't.
        String username = "default";

        try {
            // 1. Execute a DDL statement to ensure the 'users' table exists.
            System.out.println("--- Running DDL Setup ---");
            SqlExecuteParams createTableParams = new SqlExecuteParams();
            createTableParams.setUrl(jdbcUrl);
            createTableParams.setUsername(username);
            createTableParams.setSql(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  name TEXT NOT NULL," +
                            "  email TEXT UNIQUE NOT NULL," + // Corrected "NOT- NULL" to "NOT NULL"
                            "  status TEXT" +
                            ");"
            );
            tool.execute(createTableParams);
            System.out.println("Table 'users' created or already exists.\n");


            // 2. Execute a parameterized INSERT statement.
            System.out.println("--- Running INSERT Example ---");
            SqlExecuteParams insertParams = new SqlExecuteParams();
            insertParams.setUrl(jdbcUrl);
            insertParams.setUsername(username);
            // Use "INSERT OR IGNORE" to make the operation idempotent. This allows the example
            // to be run multiple times without causing a UNIQUE constraint violation. The row
            // is only inserted if the email doesn't already exist.
            insertParams.setSql("INSERT OR IGNORE INTO users (name, email, status) VALUES (:name, :email, :status)");
            insertParams.setNamed(Map.of(
                    "name", "Alice",
                    "email", "alice@example.com",
                    "status", "active"
            ));

            SqlExecuteResult insertResult = tool.execute(insertParams);
            System.out.println("Statement type: " + insertResult.getType());
            // On the first run, updateCount will be 1. On subsequent runs, it will be 0.
            System.out.println("Rows affected: " + insertResult.getUpdateCount());
            if (insertResult.getGeneratedKeys() != null) {
                System.out.println("Generated Keys: " + insertResult.getGeneratedKeys());
            }
            System.out.println();


            // 3. Execute a parameterized SELECT statement.
            System.out.println("--- Running SELECT Example ---");
            SqlExecuteParams selectParams = new SqlExecuteParams();
            selectParams.setUrl(jdbcUrl);
            selectParams.setUsername(username);
            selectParams.setSql("SELECT id, name, email FROM users WHERE status = :status_filter");
            selectParams.setNamed(Map.of("status_filter", "active"));

            SqlExecuteResult selectResult = tool.execute(selectParams);
            System.out.println("Query executed in " + selectResult.getElapsedMs() + " ms.");
            System.out.println("Found " + selectResult.getRows().size() + " active users.");

            // The result object includes convenient formatters for the output.
            System.out.println("\nQuery Result (Markdown Format):");
            System.out.println(selectResult.toMarkdownTable());

        } catch (SQLException e) {
            // Catches database-specific errors (e.g., connection issues, syntax errors).
            System.err.println("Database Error: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Catches errors from the tool itself (e.g., invalid parameters, constraint violations).
            System.err.println("Execution Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}