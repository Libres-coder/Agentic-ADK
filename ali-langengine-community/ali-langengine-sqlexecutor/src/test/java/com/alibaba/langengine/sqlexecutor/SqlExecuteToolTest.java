package com.alibaba.langengine.sqlexecutor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SqlExecuteToolTest {

    private static final String H2_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String H2_USER = "sa";
    private static final String H2_PASS = "";

    private final SqlExecuteTool tool = new SqlExecuteTool();
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASS);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255))");
            stmt.execute("INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com'), ('Bob', 'bob@example.com')");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE users");
        }
        connection.close();
    }

    private SqlExecuteParams createBaseParams() {
        SqlExecuteParams params = new SqlExecuteParams();
        params.setUrl(H2_URL);
        params.setUsername(H2_USER);
        params.setPassword(H2_PASS);
        return params;
    }

    @Test
    void testSelectQueryWithPositionalParams() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.setSql("SELECT id, name FROM users WHERE name = ?");
        params.setPositional(List.of("Alice"));

        SqlExecuteResult result = tool.execute(params);

        assertEquals(SqlExecuteResult.StatementType.QUERY, result.getType());
        assertEquals(2, result.getColumns().size());
        assertEquals("ID", result.getColumns().get(0).getLabel().toUpperCase());
        assertEquals("NAME", result.getColumns().get(1).getLabel().toUpperCase());
        assertEquals(1, result.getRows().size());
        assertEquals("Alice", result.getRows().get(0).get(1));
        assertFalse(result.isTruncated());
    }

    @Test
    void testSelectQueryWithNamedParams() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.setSql("SELECT id, name FROM users WHERE name = :name AND id > :id");
        params.setNamed(Map.of("name", "Bob", "id", 0));

        SqlExecuteResult result = tool.execute(params);

        assertEquals(1, result.getRows().size());
        assertEquals("Bob", result.getRows().get(0).get(1));
    }

    @Test
    void testInsertWithGeneratedKeys() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.setSql("INSERT INTO users(name, email) VALUES (:name, :email)");
        params.setNamed(Map.of("name", "Charlie", "email", "charlie@example.com"));

        SqlExecuteResult result = tool.execute(params);

        assertEquals(SqlExecuteResult.StatementType.UPDATE, result.getType());
        assertEquals(1, result.getUpdateCount());
        assertNotNull(result.getGeneratedKeys());
        assertEquals(1, result.getGeneratedKeys().size());
        assertTrue(result.getGeneratedKeys().get(0).containsKey("ID"));
        assertEquals(3, result.getGeneratedKeys().get(0).get("ID")); // H2 returns Long type
    }

    @Test
    void testUpdateStatement() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.setSql("UPDATE users SET email = ? WHERE name = ?");
        params.setPositional(List.of("new.bob@example.com", "Bob"));

        SqlExecuteResult result = tool.execute(params);

        assertEquals(SqlExecuteResult.StatementType.UPDATE, result.getType());
        assertEquals(1, result.getUpdateCount());
        assertNull(result.getGeneratedKeys());
    }

    @Test
    void testDdlStatement() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.setSql("ALTER TABLE users ADD COLUMN age INT");

        SqlExecuteResult result = tool.execute(params);

        assertEquals(SqlExecuteResult.StatementType.DDL, result.getType());
        assertEquals(0, result.getUpdateCount());
    }

    @Test
    void testMaxRowsTruncation() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.setSql("SELECT * FROM users");
        params.setMaxRows(1);

        SqlExecuteResult result = tool.execute(params);

        assertEquals(1, result.getRows().size());
        assertTrue(result.isTruncated());
    }

    @Test
    void testUpdateCountExceedsLimitThrowsException() {
        SqlExecuteParams params = createBaseParams();
        params.setSql("UPDATE users SET email = 'all@example.com'");
        params.setMaxUpdateRows(1);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> tool.execute(params));
        assertTrue(ex.getMessage().contains("update count exceeds maxUpdateRows"));
    }

    @Test
    void testMultipleStatementsThrowsException() {
        SqlExecuteParams params = createBaseParams();
        params.setSql("SELECT * FROM users; DROP TABLE users;");

        // Assuming SafeSql.ensureSingleStatement throws this specific exception
        // The original test code might need adjustment if the exception type/message from SafeSql is different
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> tool.execute(params));
        assertTrue(ex.getMessage().toLowerCase().contains("multiple statements"));
    }

    @Test
    void testMissingNamedParamThrowsException() {
        SqlExecuteParams params = createBaseParams();
        params.setSql("SELECT * FROM users WHERE name = :name");
        params.setNamed(Map.of("wrong_param", "Alice"));

        // Assuming NamedParamCompiler throws this specific exception
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> tool.execute(params));
        assertTrue(ex.getMessage().contains("missing named param: name"));
    }

    @Test
    void testSqlWithCommentsIsStripped() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.setSql("SELECT name FROM users -- a comment \n WHERE id = 1 /* another comment */");

        SqlExecuteResult result = tool.execute(params);

        assertEquals(1, result.getRows().size());
        assertEquals("Alice", result.getRows().get(0).get(0));
        assertNotNull(result.getSqlHash());
        assertFalse(result.getSqlHash().isEmpty());
    }

    @Test
    void testExportFormattingMethods() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(255), description VARCHAR(255), price DECIMAL(10, 2))");
            stmt.execute("INSERT INTO products VALUES (1, 'Plain Book', 'A book about \"nothing\".', 9.99)");
            stmt.execute("INSERT INTO products VALUES (2, 'Book with, comma', null, 15.50)");
            stmt.execute("INSERT INTO products VALUES (3, 'Book with \n newline', 'Line 1\nLine 2', 20.00)");
        }

        SqlExecuteParams params = createBaseParams();
        params.setSql("SELECT * FROM products ORDER BY id");
        SqlExecuteResult result;

        try {
            result = tool.execute(params);


            String expectedCsv = """
                    ID,NAME,DESCRIPTION,PRICE
                    1,Plain Book,"A book about ""nothing"".",9.99
                    2,"Book with, comma",,15.50
                    3,"Book with \n newline","Line 1\nLine 2",20.00
                    """;
            assertEquals(expectedCsv, result.toCsvString());

            String expectedMarkdown = """
                    | ID | NAME | DESCRIPTION | PRICE | \n"""
                    + """
                    | --- | --- | --- | --- |\n"""
                    + """
                    | 1 | Plain Book | A book about "nothing". | 9.99 | \n"""
                    + """
                    | 2 | Book with, comma | null | 15.50 | \n"""
                    + """
                    | 3 | Book with \n newline | Line 1\nLine 2 | 20.00 | \n""";
            // Note: Markdown representation of null might vary. Adjust if needed.
            assertEquals(expectedMarkdown.replace(" | null |", " |  |"), result.toMarkdownTable());


            String expectedJson = """
                    [
                      {"ID": 1, "NAME": "Plain Book", "DESCRIPTION": "A book about \\"nothing\\".", "PRICE": 9.99},
                      {"ID": 2, "NAME": "Book with, comma", "DESCRIPTION": null, "PRICE": 15.50},
                      {"ID": 3, "NAME": "Book with \\n newline", "DESCRIPTION": "Line 1\\nLine 2", "PRICE": 20.00}
                    ]""";
            assertEquals(
                    expectedJson.replaceAll("\\s+", ""),
                    result.toJsonString().replaceAll("\\s+", "")
            );

            List<Map<String, Object>> expectedListOfMaps = new ArrayList<>();
            Map<String, Object> row1 = new LinkedHashMap<>();
            row1.put("ID", 1);
            row1.put("NAME", "Plain Book");
            row1.put("DESCRIPTION", "A book about \"nothing\".");
            row1.put("PRICE", new java.math.BigDecimal("9.99"));
            expectedListOfMaps.add(row1);

            Map<String, Object> row2 = new LinkedHashMap<>();
            row2.put("ID", 2);
            row2.put("NAME", "Book with, comma");
            row2.put("DESCRIPTION", null);
            row2.put("PRICE", new java.math.BigDecimal("15.50"));
            expectedListOfMaps.add(row2);

            Map<String, Object> row3 = new LinkedHashMap<>();
            row3.put("ID", 3);
            row3.put("NAME", "Book with \n newline");
            row3.put("DESCRIPTION", "Line 1\nLine 2");
            row3.put("PRICE", new java.math.BigDecimal("20.00"));
            expectedListOfMaps.add(row3);

            assertEquals(expectedListOfMaps, result.getRowsAsListOfMaps());

        } finally {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE products");
            }
        }
    }

}