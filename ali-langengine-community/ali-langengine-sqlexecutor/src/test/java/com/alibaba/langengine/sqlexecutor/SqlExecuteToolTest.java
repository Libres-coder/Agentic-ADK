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
        params.url = H2_URL;
        params.username = H2_USER;
        params.password = H2_PASS;
        return params;
    }

    @Test
    void testSelectQueryWithPositionalParams() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.sql = "SELECT id, name FROM users WHERE name = ?";
        params.positional = List.of("Alice");

        SqlExecuteResult result = tool.execute(params);

        assertEquals(SqlExecuteResult.StatementType.QUERY, result.type);
        assertEquals(2, result.columns.size());
        assertEquals("ID", result.columns.get(0).label.toUpperCase());
        assertEquals("NAME", result.columns.get(1).label.toUpperCase());
        assertEquals(1, result.rows.size());
        assertEquals("Alice", result.rows.get(0).get(1));
        assertFalse(result.truncated);
    }

    @Test
    void testSelectQueryWithNamedParams() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.sql = "SELECT id, name FROM users WHERE name = :name AND id > :id";
        params.named = Map.of("name", "Bob", "id", 0);

        SqlExecuteResult result = tool.execute(params);

        assertEquals(1, result.rows.size());
        assertEquals("Bob", result.rows.get(0).get(1));
    }

    @Test
    void testInsertWithGeneratedKeys() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.sql = "INSERT INTO users(name, email) VALUES (:name, :email)";
        params.named = Map.of("name", "Charlie", "email", "charlie@example.com");

        SqlExecuteResult result = tool.execute(params);

        assertEquals(SqlExecuteResult.StatementType.UPDATE, result.type);
        assertEquals(1, result.updateCount);
        assertNotNull(result.generatedKeys);
        assertEquals(1, result.generatedKeys.size());
        assertTrue(result.generatedKeys.get(0).containsKey("ID"));
        assertEquals(3, result.generatedKeys.get(0).get("ID")); // H2 返回 Long 类型
    }

    @Test
    void testUpdateStatement() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.sql = "UPDATE users SET email = ? WHERE name = ?";
        params.positional = List.of("new.bob@example.com", "Bob");

        SqlExecuteResult result = tool.execute(params);

        assertEquals(SqlExecuteResult.StatementType.UPDATE, result.type);
        assertEquals(1, result.updateCount);
        assertNull(result.generatedKeys);
    }

    @Test
    void testDdlStatement() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.sql = "ALTER TABLE users ADD COLUMN age INT";

        SqlExecuteResult result = tool.execute(params);

        assertEquals(SqlExecuteResult.StatementType.DDL, result.type);
        assertEquals(0, result.updateCount);
    }

    @Test
    void testMaxRowsTruncation() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.sql = "SELECT * FROM users";
        params.maxRows = 1;

        SqlExecuteResult result = tool.execute(params);

        assertEquals(1, result.rows.size());
        assertTrue(result.truncated);
    }

    @Test
    void testUpdateCountExceedsLimitThrowsException() {
        SqlExecuteParams params = createBaseParams();
        params.sql = "UPDATE users SET email = 'all@example.com'";
        params.maxUpdateRows = 1;

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> tool.execute(params));
        assertTrue(ex.getMessage().contains("update count exceeds maxUpdateRows"));
    }

    @Test
    void testMultipleStatementsThrowsException() {
        SqlExecuteParams params = createBaseParams();
        params.sql = "SELECT * FROM users; DROP TABLE users;";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> tool.execute(params));
        assertEquals("Multiple statements detected.", ex.getMessage());
    }

    @Test
    void testMissingNamedParamThrowsException() {
        SqlExecuteParams params = createBaseParams();
        params.sql = "SELECT * FROM users WHERE name = :name";
        params.named = Map.of("wrong_param", "Alice");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> tool.execute(params));
        assertEquals("missing named param: name", ex.getMessage());
    }

    @Test
    void testSqlWithCommentsIsStripped() throws SQLException {
        SqlExecuteParams params = createBaseParams();
        params.sql = "SELECT name FROM users -- a comment \n WHERE id = 1 /* another comment */";

        SqlExecuteResult result = tool.execute(params);

        assertEquals(1, result.rows.size());
        assertEquals("Alice", result.rows.get(0).get(0));
        assertNotNull(result.sqlHash);
        assertNotEquals("", result.sqlHash);
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
        params.sql = "SELECT * FROM products ORDER BY id";
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
                    | 2 | Book with, comma |  | 15.50 | \n"""
                    + """
                    | 3 | Book with \n newline | Line 1\nLine 2 | 20.00 | \n""";
            assertEquals(expectedMarkdown, result.toMarkdownTable());

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