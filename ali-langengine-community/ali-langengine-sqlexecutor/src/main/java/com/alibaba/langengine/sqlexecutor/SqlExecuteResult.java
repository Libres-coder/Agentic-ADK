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
    public String driver;
    /**
     * The guessed SQL dialect based on the JDBC URL (e.g., "mysql", "postgres").
     */
    public String dialect;
    /**
     * The SHA-256 hash of the executed SQL statement, used for identification.
     */
    public String sqlHash;
    /**
     * The total time taken for the execution, in milliseconds.
     */
    public long   elapsedMs;

    /**
     * The type of the executed SQL statement.
     */
    public StatementType type;

    // Fields for QUERY results
    /**
     * Metadata for the columns in the result set. Only populated for QUERY statements.
     */
    public List<ColumnMeta> columns;
    /**
     * The data rows returned by a QUERY. Each inner list represents a single row.
     */
    public List<List<Object>> rows;
    /**
     * A flag indicating whether the result set was truncated because it exceeded `maxRows`.
     */
    public boolean truncated;

    // Fields for UPDATE / DDL results
    /**
     * The number of rows affected by an UPDATE, INSERT, or DELETE statement.
     */
    public Integer updateCount;
    /**
     * A list of auto-generated keys returned by an INSERT statement, if any.
     * Each map represents the keys for a single inserted row.
     */
    public List<Map<String,Object>> generatedKeys;

    /**
     * Enumerates the possible types of SQL statements.
     */
    public enum StatementType { QUERY, UPDATE, DDL }

    /**
     * A simple data class to hold metadata for a single column in a result set.
     */
    public static final class ColumnMeta {
        /**
         * The label or alias of the column.
         */
        public String label;
        /**
         * The database-specific type name of the column.
         */
        public String typeName;
        public ColumnMeta() {}
        public ColumnMeta(String label, String typeName) { this.label = label; this.typeName = typeName; }
    }

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
                .map(col -> escapeCsvField(col.label))
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
            table.append(col.label).append(" | ");
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

        List<String> labels = this.columns.stream().map(c -> c.label).collect(Collectors.toList());
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

        List<String> labels = this.columns.stream().map(c -> c.label).collect(Collectors.toList());

        for (List<Object> row : this.rows) {
            Map<String, Object> mapRow = new LinkedHashMap<>();
            for (int i = 0; i < labels.size(); i++) {
                mapRow.put(labels.get(i), row.get(i));
            }
            resultList.add(mapRow);
        }

        return resultList;
    }

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