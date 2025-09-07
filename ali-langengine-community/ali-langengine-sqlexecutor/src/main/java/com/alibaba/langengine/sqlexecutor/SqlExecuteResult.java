package com.alibaba.langengine.sqlexecutor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Holds the result of a SQL execution.
 * It contains metadata about the execution (e.g., driver, duration) and the actual
 * data returned, which varies depending on the type of statement (QUERY, UPDATE, or DDL).
 * Also includes utility methods to format query results into common formats like CSV or JSON.
 */
public final class SqlExecuteResult {

    /**
     * The name and version of the JDBC driver used.
     */
    private String driver;

    /**
     * The guessed SQL dialect based on the JDBC URL (e.g., "mysql", "postgres").
     */
    private String dialect;

    /**
     * The SHA-256 hash of the executed SQL statement, used for identification.
     */
    private String sqlHash;

    /**
     * The total time taken for the execution, in milliseconds.
     */
    private long elapsedMs;

    /**
     * The type of the executed SQL statement.
     */
    private StatementType type;

    // Fields for QUERY results
    /**
     * Metadata for the columns in the result set. Only populated for QUERY statements.
     */
    private List<ColumnMeta> columns;

    /**
     * The data rows returned by a QUERY. Each inner list represents a single row.
     */
    private List<List<Object>> rows;

    /**
     * A flag indicating whether the result set was truncated because it exceeded `maxRows`.
     */
    private boolean truncated;

    // Fields for UPDATE / DDL results
    /**
     * The number of rows affected by an UPDATE, INSERT, or DELETE statement.
     */
    private Integer updateCount;

    /**
     * A list of auto-generated keys returned by an INSERT statement, if any.
     * Each map represents the keys for a single inserted row.
     */
    private List<Map<String, Object>> generatedKeys;

    /**
     * Enumerates the possible types of SQL statements.
     */
    public enum StatementType {
        QUERY, UPDATE, DDL
    }

    /**
     * A simple data class to hold metadata for a single column in a result set.
     */
    public static final class ColumnMeta {
        /**
         * The label or alias of the column.
         */
        private String label;
        /**
         * The database-specific type name of the column.
         */
        private String typeName;

        public ColumnMeta() {}

        public ColumnMeta(String label, String typeName) {
            this.label = label;
            this.typeName = typeName;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    // --- Getters and Setters ---

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getSqlHash() {
        return sqlHash;
    }

    public void setSqlHash(String sqlHash) {
        this.sqlHash = sqlHash;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }

    public StatementType getType() {
        return type;
    }

    public void setType(StatementType type) {
        this.type = type;
    }

    public List<ColumnMeta> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnMeta> columns) {
        this.columns = columns;
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public void setRows(List<List<Object>> rows) {
        this.rows = rows;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public Integer getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(Integer updateCount) {
        this.updateCount = updateCount;
    }

    public List<Map<String, Object>> getGeneratedKeys() {
        return generatedKeys;
    }

    public void setGeneratedKeys(List<Map<String, Object>> generatedKeys) {
        this.generatedKeys = generatedKeys;
    }

    // --- Utility Methods ---

    /**
     * Formats the query result as a CSV (Comma-Separated Values) string.
     * The first line is the header row with column labels.
     * @return A string containing the data in CSV format, or an empty string if the result is not from a query.
     */
    public String toCsvString() {
        if (this.type != StatementType.QUERY || this.columns == null || this.rows == null) {
            return "";
        }

        StringBuilder csv = new StringBuilder();

        String header = this.columns.stream()
                .map(col -> escapeCsvField(col.getLabel()))
                .collect(Collectors.joining(","));
        csv.append(header).append("\n");

        for (List<Object> row : this.rows) {
            String rowStr = row.stream()
                    .map(this::escapeCsvField)
                    .collect(Collectors.joining(","));
            csv.append(rowStr).append("\n");
        }

        return csv.toString();
    }

    /**
     * Formats the query result as a Markdown table.
     * @return A string containing the data as a Markdown table, or a message if the result is not from a query.
     */
    public String toMarkdownTable() {
        if (this.type != StatementType.QUERY || this.columns == null || this.rows == null) {
            return "Not a query result.";
        }

        StringBuilder table = new StringBuilder();

        // Header
        table.append("| ");
        for (ColumnMeta col : this.columns) {
            table.append(col.getLabel()).append(" | ");
        }
        table.append("\n");

        // Separator
        table.append("|");
        for (int i = 0; i < this.columns.size(); i++) {
            table.append(" --- |");
        }
        table.append("\n");

        // Rows
        for (List<Object> row : this.rows) {
            table.append("| ");
            for (Object cell : row) {
                table.append(cell == null ? "" : String.valueOf(cell)).append(" | ");
            }
            table.append("\n");
        }

        return table.toString();
    }

    /**
     * Formats the query result as a JSON array of objects.
     * @return A JSON string representing an array of row objects, or an empty array "[]" if not a query result.
     */
    public String toJsonString() {
        if (this.type != StatementType.QUERY || this.columns == null || this.rows == null) {
            return "[]";
        }

        List<String> labels = this.columns.stream().map(ColumnMeta::getLabel).collect(Collectors.toList());
        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < this.rows.size(); i++) {
            List<Object> row = this.rows.get(i);
            json.append("  {");

            List<String> fields = new ArrayList<>();
            for (int j = 0; j < labels.size(); j++) {
                String key = "\"" + escapeJsonString(labels.get(j)) + "\"";
                String value = formatJsonValue(row.get(j));
                fields.add(key + ": " + value);
            }
            json.append(String.join(", ", fields));

            json.append("}");
            if (i < this.rows.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]");
        return json.toString();
    }

    /**
     * Converts the query result into a List of Maps. Each map represents a row,
     * with column labels as keys and cell contents as values.
     * @return A List of Maps representing the rows, or an empty list if not a query result.
     */
    public List<Map<String, Object>> getRowsAsListOfMaps() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (this.type != StatementType.QUERY || this.columns == null || this.rows == null) {
            return resultList;
        }

        List<String> labels = this.columns.stream().map(ColumnMeta::getLabel).collect(Collectors.toList());

        for (List<Object> row : this.rows) {
            Map<String, Object> mapRow = new LinkedHashMap<>();
            for (int i = 0; i < labels.size(); i++) {
                mapRow.put(labels.get(i), row.get(i));
            }
            resultList.add(mapRow);
        }

        return resultList;
    }

    // --- Private Helper Methods ---

    /**
     * Escapes a field value for CSV format, quoting it if it contains commas, quotes, or newlines.
     * @param field The object to be formatted.
     * @return The CSV-safe string representation.
     */
    private String escapeCsvField(Object field) {
        if (field == null) {
            return "";
        }
        String s = String.valueOf(field);
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    /**
     * Escapes special characters in a string for use in a JSON string value.
     * @param value The object to be escaped.
     * @return The JSON-safe string.
     */
    private String escapeJsonString(Object value) {
        if (value == null) return "";
        return String.valueOf(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Formats a Java object into its corresponding JSON value representation (e.g., string, number, null).
     * @param value The object to format.
     * @return A string containing the JSON representation of the value.
     */
    private String formatJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "\"" + escapeJsonString(value) + "\"";
    }
}