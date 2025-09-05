package com.alibaba.langengine.sqlexecutor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

/**
 * The main tool for executing SQL statements against a database using JDBC.
 * It handles the entire lifecycle of a SQL execution:
 * 1. Validates input parameters.
 * 2. Preprocesses the SQL for safety (stripping comments, ensuring single statement).
 * 3. Compiles named parameters into positional parameters if necessary.
 * 4. Establishes a database connection.
 * 5. Creates and configures a PreparedStatement (e.g., sets timeout, binds values).
 * 6. Executes the statement and determines if it's a query or an update.
 * 7. Processes the ResultSet for queries or the update count for modifications.
 * 8. Fetches auto-generated keys for INSERT statements.
 * 9. Populates and returns a comprehensive {@link SqlExecuteResult} object.
 *
 * This class is designed to be safe, preventing SQL injection by using PreparedStatements
 * and performing pre-execution checks.
 */
public final class SqlExecuteTool {

    /**
     * Executes a SQL statement based on the provided parameters.
     *
     * @param p An object of {@link SqlExecuteParams} containing all necessary details for execution.
     * @return A {@link SqlExecuteResult} object with the results and metadata of the execution.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalArgumentException if the input parameters are invalid (e.g., missing URL or SQL).
     * @throws IllegalStateException if an execution constraint is violated (e.g., update count exceeds max).
     */
    public SqlExecuteResult execute(SqlExecuteParams p) throws SQLException {
        Objects.requireNonNull(p, "params");
        if (isBlank(p.url) || isBlank(p.username))
            throw new IllegalArgumentException("url/username required");
        if (isBlank(p.sql))
            throw new IllegalArgumentException("sql required");
        if (p.positional != null && p.named != null)
            throw new IllegalArgumentException("positional and named cannot be used together");

        // 1. Preprocess and clean the SQL
        String sql1 = SafeSql.stripComments(p.sql);
        String sql2 = SafeSql.stripTrailingSemicolon(sql1);
        SafeSql.ensureSingleStatement(sql2);

        // 2. Handle named parameters if present
        List<Object> bindValues = p.positional;
        if (p.named != null) {
            NamedParamCompiler.Compiled c = NamedParamCompiler.compile(sql2, p.named);
            sql2 = c.sql();
            bindValues = c.ordered();
        }

        // 3. Apply execution limits from parameters, with defaults
        int timeoutSec = Math.max(1, (p.timeoutMs == null ? 5000 : p.timeoutMs) / 1000);
        int capRows    = (p.maxRows == null ? 1000 : p.maxRows);
        int capUpd     = (p.maxUpdateRows == null ? 200_000 : p.maxUpdateRows);
        int capField   = (p.maxFieldSize == null ? 1_000_000 : p.maxFieldSize);

        SqlExecuteResult.StatementType stype = SafeSql.detectType(sql2.toUpperCase(Locale.ROOT));

        long t0 = System.nanoTime();
        SqlExecuteResult out = new SqlExecuteResult();
        out.dialect = SafeSql.guessDialect(p.url);

        try (Connection conn = DriverManager.getConnection(p.url, p.username, defaultString(p.password))) {
            DatabaseMetaData meta = conn.getMetaData();
            out.driver = meta.getDriverName() + "/" + meta.getDriverVersion();

            try (PreparedStatement ps = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {
                ps.setQueryTimeout(timeoutSec);

                // Bind parameter values to the prepared statement
                if (bindValues != null) {
                    int i = 1; for (Object v : bindValues) ps.setObject(i++, v);
                }

                // 4. Execute the statement
                boolean isResultSet = ps.execute();

                if (isResultSet) {
                    // --- Handle QUERY result ---
                    out.type = SqlExecuteResult.StatementType.QUERY;
                    try (ResultSet rs = ps.getResultSet()) {
                        ResultSetMetaData md = rs.getMetaData();
                        int n = md.getColumnCount();
                        List<SqlExecuteResult.ColumnMeta> cols = new ArrayList<>(n);
                        for (int i=1;i<=n;i++)
                            cols.add(new SqlExecuteResult.ColumnMeta(md.getColumnLabel(i), md.getColumnTypeName(i)));

                        List<List<Object>> rows = new ArrayList<>();
                        int fetched=0; boolean truncated=false;
                        while (rs.next()) {
                            if (fetched >= capRows) { truncated = true; break; }
                            List<Object> row = new ArrayList<>(n);
                            for (int i=1;i<=n;i++) {
                                Object v = rs.getObject(i);
                                // Truncate large string fields
                                if (v instanceof String s && s.length() > capField)
                                    v = s.substring(0, capField);
                                row.add(v);
                            }
                            rows.add(row); fetched++;
                        }
                        out.columns = cols;
                        out.rows = rows;
                        out.truncated = truncated;
                    }
                } else {
                    // --- Handle UPDATE/DDL result ---
                    out.type = (stype == SqlExecuteResult.StatementType.DDL)
                            ? SqlExecuteResult.StatementType.DDL
                            : SqlExecuteResult.StatementType.UPDATE;
                    int upd = ps.getUpdateCount();
                    if (upd > capUpd)
                        throw new IllegalStateException("update count exceeds maxUpdateRows: " + upd + " > " + capUpd);
                    out.updateCount = upd;

                    // Try to fetch generated keys for INSERT statements
                    if (stype == SqlExecuteResult.StatementType.UPDATE && sql2.trim().toUpperCase().startsWith("INSERT")) {
                        List<Map<String,Object>> keys = new ArrayList<>();
                        try (ResultSet gk = ps.getGeneratedKeys()) {
                            if (gk != null) {
                                ResultSetMetaData gmd = gk.getMetaData();
                                int gn = gmd.getColumnCount();
                                while (gk.next()) {
                                    Map<String,Object> m = new LinkedHashMap<>();
                                    for (int i=1;i<=gn;i++) m.put(gmd.getColumnLabel(i), gk.getObject(i));
                                    keys.add(m);
                                }
                            }
                        }
                        out.generatedKeys = keys.isEmpty() ? null : keys;
                    } else {
                        out.generatedKeys = null;
                    }
                }
            }
        } finally {
            out.elapsedMs = (System.nanoTime() - t0) / 1_000_000;
            out.sqlHash = sha256Hex(sql2);
        }
        return out;
    }

    /**
     * Returns the given string or an empty string if it is null.
     * @param s The string to check.
     * @return The original string or "" if null.
     */
    private static String defaultString(String s){ return s==null? "": s; }

    /**
     * Checks if a string is null, empty, or contains only whitespace.
     * @param s The string to check.
     * @return true if the string is blank, false otherwise.
     */
    private static boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }

    /**
     * Computes the SHA-256 hash of a string and returns it as a hex string.
     * @param s The input string.
     * @return The hex representation of the SHA-256 hash, or "NA" if an error occurs.
     */
    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { return "NA"; }
    }
}