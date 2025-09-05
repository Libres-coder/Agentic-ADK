package com.alibaba.langengine.sqlexecutor;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates all parameters required to execute a SQL statement.
 * This includes database connection details, the SQL query itself, bind parameters,
 * and various execution constraints like timeouts and row limits.
 */
public final class SqlExecuteParams {
    /**
     * The JDBC URL for the database connection.
     */
    public String url;
    /**
     * The username for the database connection.
     */
    public String username;
    /**
     * The password for the database connection.
     */
    public String password;

    /**
     * The SQL statement to be executed.
     */
    public String sql;

    /**
     * A list of parameter values to be bound to positional placeholders ('?') in the SQL statement.
     * The order of objects in the list corresponds to the order of the '?' placeholders.
     */
    public List<Object> positional;         // Corresponds to the order of '?' in the SQL
    /**
     * A map of parameter names to values for named placeholders (e.g., ':name') in the SQL statement.
     */
    public Map<String, Object> named;       // Corresponds to ':name' in the SQL

    /**
     * The timeout for the query execution in milliseconds. Defaults to 5000ms.
     */
    public Integer timeoutMs   = 5000;
    /**
     * The maximum number of rows to return for a SELECT query. Defaults to 1000.
     */
    public Integer maxRows     = 1000;
    /**
     * The maximum number of rows that can be affected by an UPDATE, INSERT, or DELETE statement.
     * This acts as a safeguard against unintentional mass updates. Defaults to 200,000.
     */
    public Integer maxUpdateRows = 200_000;
    /**
     * The maximum size (in characters) for a single field's value.
     * String values longer than this will be truncated. Defaults to 1,000,000.
     */
    public Integer maxFieldSize = 1_000_000;
}