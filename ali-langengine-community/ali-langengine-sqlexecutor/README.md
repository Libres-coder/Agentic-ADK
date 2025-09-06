# SQL Executor

A SQL execution tool tailored for AI Agents. It provides a secure and convenient wrapper around standard JDBC functionalities, allowing an agent to execute SQL statements easily and safely. Core features include named parameters, automatic resource management, and built-in protection against SQL injection, ensuring the agent's stability and security when interacting with a database.
## Features

-   **Secure by Default**:
    -   Uses `PreparedStatement` to prevent SQL injection.
    -   Automatically strips comments from SQL statements.
    -   Enforces the execution of a single SQL statement at a time to prevent batch injection attacks.
    -   Provides configurable limits for max rows returned, max rows updated, and max field size to prevent accidental large-scale operations.

-   **Convenient API**:
    -   **Named Parameters**: Write cleaner SQL with named parameters (e.g., `:userId`) instead of traditional `?` placeholders. The library compiles them to JDBC-compliant SQL automatically.
    -   **Automatic Resource Management**: The library internally uses `try-with-resources` to handle the lifecycle of `Connection`, `PreparedStatement`, and `ResultSet` objects, ensuring they are always closed correctly.
    -   **Flexible Parameter Binding**: Supports both named (`Map<String, Object>`) and positional (`List<Object>`) parameter binding.
    -   **Rich Result Object**: The `SqlExecuteResult` object contains comprehensive information about the execution, including:
        -   Query results (`List<List<Object>>`) and column metadata.
        -   Update counts for `INSERT`, `UPDATE`, `DELETE` statements.
        -   Auto-generated keys for `INSERT` statements.
        -   Execution metadata like driver version, dialect, and query duration.

-   **Utility Converters**:
    -   Easily format query results into common formats directly from the result object:
        -   CSV
        -   JSON
        -   Markdown Table
        -   List of Maps (`List<Map<String, Object>>`)

## Core Components

-   **`SqlExecuteTool`**: The main entry point for executing SQL. It takes a `SqlExecuteParams` object and returns a `SqlExecuteResult`.
-   **`SqlExecuteParams`**: An object that encapsulates all parameters for an execution, including database credentials, the SQL statement, bind parameters, and safety limits.
-   **`SqlExecuteResult`**: A container for the results and metadata of an execution. It provides utility methods to format the output.
-   **`SafeSql`**: An internal utility class for preprocessing and sanitizing SQL strings, including stripping comments and ensuring a single statement.
-   **`NamedParamCompiler`**: An internal utility that transparently compiles SQL with named parameters into JDBC-compliant SQL with positional `?` placeholders.

## Usage Example

### 1. Add Your Database Driver

This library uses the standard JDBC API, making it compatible with any database that has a JDBC driver. It does not include any specific drivers itself.

You, as the user of this library, must add the appropriate JDBC driver for your target database to your project's dependencies.

**For example:**

* **PostgreSQL:**
  ```xml
  <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
  </dependency>
  ```

* **MySQL:**
  ```xml
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
  </dependency>
  ```

* **SQLite:**
  ```xml
  <dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.43.0.0</version>
  </dependency>
  ```

### 2. Basic Query with Named Parameters

Here's how to execute a `SELECT` statement using named parameters. The `url` parameter will determine which driver is used.

```java
import com.alibaba.langengine.sqlexecutor.SqlExecuteParams;
import com.alibaba.langengine.sqlexecutor.SqlExecuteTool;
import com.alibaba.langengine.sqlexecutor.SqlExecuteResult;

import java.sql.SQLException;
import java.util.Map;

public class Example {
public static void main(String[] args) {
// 1. Configure the execution parameters
    SqlExecuteParams params = new SqlExecuteParams();
    // The URL determines which database and driver to use
    params.setUrl("jdbc:postgresql://localhost:5432/mydatabase");
    params.setUsername("myuser");
    params.setPassword("mypassword");
    params.setSql("SELECT id, name, email FROM users WHERE status = :status AND registration_year > :year");
    params.setNamed(Map.of(
    "status", "active",
    "year", 2023
    ));

        // 2. Create the tool and execute
    SqlExecuteTool tool = new SqlExecuteTool();
    try {
        SqlExecuteResult result = tool.execute(params);

        // 3. Process the results
        System.out.println("Query executed in " + result.getElapsedMs() + " ms.");
        System.out.println(result.toMarkdownTable());

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Execution error: " + e.getMessage());
        }
    }
}
```

#### Example Output:

```
Query executed in 45 ms.
| id | name | email |
| --- | --- | --- |
| 101 | Alice | alice@example.com |
| 105 | Bob | bob@example.com |
```

### 3. `INSERT` Statement with Generated Keys

```java
import com.alibaba.langengine.sqlexecutor.SqlExecuteParams;
import com.alibaba.langengine.sqlexecutor.SqlExecuteTool;
import com.alibaba.langengine.sqlexecutor.SqlExecuteResult;
import java.sql.SQLException;
import java.util.Map;

public class InsertExample {

    public static void main(String[] args) {
        // 1. Create a new SqlExecuteParams object and configure the connection details.
        SqlExecuteParams params = new SqlExecuteParams();
        params.setUrl("jdbc:mysql://localhost:3306/mydatabase");
        params.setUsername("myuser");
        params.setPassword("mypassword");

        // 2. Set the SQL statement and named parameters for the INSERT operation.
        params.setSql("INSERT INTO products (name, price) VALUES (:name, :price)");
        params.setNamed(Map.of(
                "name", "Wireless Mouse",
                "price", 49.99
        ));

        // 3. Execute the SQL.
        SqlExecuteTool tool = new SqlExecuteTool();
        try {
            SqlExecuteResult result = tool.execute(params);

            // 4. Print the results.
            System.out.println("Statement type: " + result.getType());
            System.out.println("Rows affected: " + result.getUpdateCount());

            if (result.getGeneratedKeys() != null) {
                System.out.println("Generated Keys: " + result.getGeneratedKeys());
            }

        } catch (SQLException e) {
            // Handle potential database access errors.
            System.err.println("Database error: " + e.getMessage());
            // Optionally log the exception or take other actions.
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Handle invalid parameters or execution violations.
            System.err.println("Execution error: " + e.getMessage());
        }
    }
}
```

#### Example Output:

```
Statement type: UPDATE
Rows affected: 1
Generated Keys: [{GENERATED_KEY=15}]
```

## Security and Preprocessing

The library automatically performs several sanitization steps on the input SQL before execution:

1.  **Comment Stripping**: It removes all `/* ... */` and `--` style comments.
2.  **Trailing Semicolon Removal**: It removes any trailing semicolons and whitespace.
3.  **Single Statement Enforcement**: It validates that the input string contains only one executable statement.

These steps happen transparently to provide a safer execution environment.