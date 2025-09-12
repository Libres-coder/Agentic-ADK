package com.alibaba.langengine.sqlexecutor;

/**
 * A package-private utility class providing static methods for SQL string manipulation and validation.
 * These methods are focused on security and preprocessing before the SQL is executed.
 * This includes stripping comments, ensuring only a single statement is present, and detecting statement type.
 */
final class SafeSql {
    private SafeSql() {} // Private constructor to prevent instantiation

    /**
     * Removes SQL comments (both single-line '--' and multi-line '/* ... * /') from a SQL string.
     * This parser correctly handles comments within string literals.
     * @param sql The input SQL string.
     * @return The SQL string with all comments removed.
     */
    static String stripComments(String sql) {
        StringBuilder out = new StringBuilder(sql.length());
        boolean inS=false, inD=false, inLine=false, inBlock=false; // State flags
        for (int i=0;i<sql.length();i++){
            char c=sql.charAt(i), n=(i+1<sql.length()?sql.charAt(i+1):'\0');
            // Check for comment start/end if not inside a string literal
            if (!inS && !inD) {
                if (!inBlock && c=='-'&&n=='-'){inLine=true; i++; continue;}
                if (inLine && c=='\n'){inLine=false; continue;}
                if (!inLine && !inBlock && c=='/'&&n=='*'){inBlock=true; i++; continue;}
                if (inBlock && c=='*'&&n=='/'){inBlock=false; i++; continue;}
            }
            if (inLine || inBlock) continue; // Skip characters inside comments

            // Toggle string literal state
            if (!inD && c=='\''){inS=!inS; out.append(c); continue;}
            if (!inS && c=='"'){inD=!inD; out.append(c); continue;}
            out.append(c);
        }
        return out.toString();
    }

    /**
     * Removes a trailing semicolon (;) from a SQL string, if present.
     * It also trims any trailing whitespace before checking for the semicolon.
     * @param sql The input SQL string.
     * @return The SQL string without a trailing semicolon.
     */
    static String stripTrailingSemicolon(String sql) {
        int i = sql.length()-1;
        while (i>=0 && Character.isWhitespace(sql.charAt(i))) i--;
        if (i>=0 && sql.charAt(i)==';') return sql.substring(0, i);
        return sql;
    }

    /**
     * Ensures that the provided SQL string contains only a single statement.
     * It checks for semicolons that are not inside comments or string literals.
     * If a semicolon is found and is followed by non-whitespace characters, it throws an exception.
     * This is a security measure to prevent SQL injection attacks that append additional commands.
     * @param sql The SQL string to validate.
     * @throws IllegalArgumentException if multiple statements are detected.
     */
    static void ensureSingleStatement(String sql) {
        boolean inS=false, inD=false, inLine=false, inBlock=false; // State flags
        for (int i=0;i<sql.length();i++){
            char c=sql.charAt(i), n=(i+1<sql.length()?sql.charAt(i+1):'\0');
            if (!inS && !inD) {
                if (!inBlock && c=='-'&&n=='-'){inLine=true; i++; continue;}
                if (inLine && c=='\n'){inLine=false; continue;}
                if (!inLine && !inBlock && c=='/'&&n=='*'){inBlock=true; i++; continue;}
                if (inBlock && c=='*'&&n=='/'){inBlock=false; i++; continue;}
            }
            if (inLine || inBlock) continue;
            if (!inD && c=='\''){inS=!inS; continue;}
            if (!inS && c=='"'){inD=!inD; continue;}

            // Check for semicolon outside of comments and strings
            if (!inS && !inD && c==';') {
                // Check if there is any non-whitespace content after the semicolon
                for (int j=i+1;j<sql.length();j++){
                    if (!Character.isWhitespace(sql.charAt(j))) {
                        throw new IllegalArgumentException("Multiple statements detected.");
                    }
                }
            }
        }
    }

    /**
     * Detects the type of SQL statement (QUERY, UPDATE, DDL) based on its starting keyword.
     * @param sqlUpperTrim The upper-cased, trimmed SQL string.
     * @return The detected {@link SqlExecuteResult.StatementType}.
     */
    static SqlExecuteResult.StatementType detectType(String sqlUpperTrim) {
        String s = sqlUpperTrim.stripLeading();
        if (s.startsWith("SELECT") || s.startsWith("WITH ")) return SqlExecuteResult.StatementType.QUERY;
        if (s.startsWith("INSERT") || s.startsWith("UPDATE") || s.startsWith("DELETE") || s.startsWith("MERGE"))
            return SqlExecuteResult.StatementType.UPDATE;
        return SqlExecuteResult.StatementType.DDL;
    }

    /**
     * Guesses the SQL dialect (e.g., "mysql", "postgres") from the JDBC URL string.
     * @param url The JDBC URL.
     * @return The name of the dialect in lowercase, or "unknown" if it cannot be determined.
     */
    static String guessDialect(String url) {
        if (url == null) return "unknown";
        String u = url.toLowerCase();
        if (u.startsWith("jdbc:mysql:")) return "mysql";
        if (u.startsWith("jdbc:postgresql:")) return "postgres";
        if (u.startsWith("jdbc:sqlite:")) return "sqlite";
        return "unknown";
    }
}