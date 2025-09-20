package com.alibaba.langengine.sqlexecutor;

import java.util.*;

/**
 * A package-private utility class for compiling SQL with named parameters into
 * SQL with positional placeholders ('?'). It parses the SQL string, identifies
 * named parameters (e.g., ":my_param"), and replaces them with '?', while
 * collecting the corresponding parameter values in the correct order. The parser
 * correctly handles and ignores comments and string literals.
 */
final class NamedParamCompiler {
    /**
     * A record to hold the result of the compilation.
     * @param sql The compiled SQL string with '?' placeholders.
     * @param ordered The ordered list of parameter values corresponding to the placeholders.
     */
    record Compiled(String sql, List<Object> ordered) {}

    /**
     * Compiles a SQL string containing named parameters.
     *
     * @param sql The SQL string with named parameters (e.g., "SELECT * FROM users WHERE id = :userId").
     * @param named A map where keys are parameter names (without the colon) and values are the corresponding parameter values.
     * @return A {@link Compiled} object containing the JDBC-compliant SQL and the ordered list of values.
     * @throws IllegalArgumentException if the named parameter map is null/empty or a parameter in the SQL is not found in the map.
     */
    static Compiled compile(String sql, Map<String,Object> named) {
        if (named == null || named.isEmpty())
            throw new IllegalArgumentException("named params required");

        StringBuilder out = new StringBuilder(sql.length());
        List<Object> ordered = new ArrayList<>();

        boolean inS=false, inD=false, inLine=false, inBlock=false; // State flags for parsing
        for (int i=0;i<sql.length();i++){
            char c=sql.charAt(i), n=(i+1<sql.length()?sql.charAt(i+1):'\0');

            // --- Comment Handling ---
            if (!inS && !inD) {
                if (!inBlock && c=='-'&&n=='-'){inLine=true; out.append(c); continue;}
                if (inLine && c=='\n'){inLine=false; out.append(c); continue;}
                if (!inLine && !inBlock && c=='/'&&n=='*'){inBlock=true; out.append("/*"); i++; continue;}
                if (inBlock && c=='*'&&n=='/'){inBlock=false; out.append("*/"); i++; continue;}
            }
            if (inLine || inBlock) { out.append(c); continue; }

            // --- String Literal Handling ---
            if (!inD && c=='\''){inS=!inS; out.append(c); continue;}
            if (!inS && c=='"'){inD=!inD; out.append(c); continue;}
            if (inS || inD) { out.append(c); continue; }

            // --- Named Parameter Handling ---
            if (c==':' && Character.isJavaIdentifierStart(n)) {
                int j=i+1;
                while (j<sql.length()) {
                    char cj=sql.charAt(j);
                    // A simple check for valid characters in a parameter name
                    if (!(Character.isJavaIdentifierPart(cj) || cj=='$' || cj=='_')) break;
                    j++;
                }
                String name = sql.substring(i+1, j);
                if (!named.containsKey(name))
                    throw new IllegalArgumentException("missing named param: " + name);

                out.append('?');
                ordered.add(named.get(name));
                i=j-1; // Move cursor to the end of the parameter name
                continue;
            }
            out.append(c);
        }
        return new Compiled(out.toString(), ordered);
    }
}