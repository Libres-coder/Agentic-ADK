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
    private String url;

    /**
     * The username for the database connection.
     */
    private String username;

    /**
     * The password for the database connection.
     */
    private String password;

    /**
     * The SQL statement to be executed.
     */
    private String sql;

    /**
     * A list of parameter values to be bound to positional placeholders ('?') in the SQL statement.
     * The order of objects in the list corresponds to the order of the '?' placeholders.
     */
    private List<Object> positional;

    /**
     * A map of parameter names to values for named placeholders (e.g., ':name') in the SQL statement.
     */
    private Map<String, Object> named;

    /**
     * The timeout for the query execution in milliseconds. Defaults to 5000ms.
     */
    private Integer timeoutMs = 5000;

    /**
     * The maximum number of rows to return for a SELECT query. Defaults to 1000.
     */
    private Integer maxRows = 1000;

    /**
     * The maximum number of rows that can be affected by an UPDATE, INSERT, or DELETE statement.
     * This acts as a safeguard against unintentional mass updates. Defaults to 200,000.
     */
    private Integer maxUpdateRows = 200_000;

    /**
     * The maximum size (in characters) for a single field's value.
     * String values longer than this will be truncated. Defaults to 1,000,000.
     */
    private Integer maxFieldSize = 1_000_000;

    // --- Getters and Setters ---

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Object> getPositional() {
        return positional;
    }

    public void setPositional(List<Object> positional) {
        this.positional = positional;
    }

    public Map<String, Object> getNamed() {
        return named;
    }

    public void setNamed(Map<String, Object> named) {
        this.named = named;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Integer getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }

    public Integer getMaxUpdateRows() {
        return maxUpdateRows;
    }

    public void setMaxUpdateRows(Integer maxUpdateRows) {
        this.maxUpdateRows = maxUpdateRows;
    }

    public Integer getMaxFieldSize() {
        return maxFieldSize;
    }

    public void setMaxFieldSize(Integer maxFieldSize) {
        this.maxFieldSize = maxFieldSize;
    }
}