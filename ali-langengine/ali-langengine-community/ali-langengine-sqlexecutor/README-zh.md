# SQL 执行器

一个为 AI 智能体（Agent）量身打造的 SQL 执行工具。它通过一个安全、便捷的封装层来调用 JDBC，让智能体可以轻松、安全地执行 SQL 语句。其核心特性包括命名参数、自动资源管理和内置的 SQL 注入防护，确保了智能体在与数据库交互时的稳定性和安全性。

## 功能特性

-   **默认安全**:
    -   使用 `PreparedStatement` 防止 SQL 注入。
    -   自动从 SQL 语句中剥离注释。
    -   强制每次只执行一条 SQL 语句，以防止批量注入攻击。
    -   提供可配置的最大返回行数、最大更新行数和最大字段大小限制，以防止意外的大规模操作。

-   **便捷的 API**:
    -   **命名参数**: 使用命名参数（例如 `:userId`）代替传统的 `?` 占位符来编写更清晰的 SQL。本库会自动将其编译为兼容 JDBC 的 SQL。
    -   **自动资源管理**: 本库内部使用 `try-with-resources` 语句来管理 `Connection`、`PreparedStatement` 和 `ResultSet` 对象的生命周期，确保它们总是被正确关闭。
    -   **灵活的参数绑定**: 同时支持命名参数 (`Map<String, Object>`) 和位置参数 (`List<Object>`) 的绑定方式。
    -   **丰富的结果对象**: `SqlExecuteResult` 对象包含有关执行的全面信息，包括：
        -   查询结果 (`List<List<Object>>`) 和列元数据。
        -   `INSERT`、`UPDATE`、`DELETE` 语句的更新计数。
        -   `INSERT` 语句的自增主键。
        -   执行元数据，如驱动版本、数据库方言和查询耗时。

-   **实用转换器**:
    -   直接从结果对象轻松地将查询结果格式化为常用格式：
        -   CSV
        -   JSON
        -   Markdown 表格
        -   `List<Map<String, Object>>`

---

## 核心组件

-   **`SqlExecuteTool`**: 执行 SQL 的主入口。它接收一个 `SqlExecuteParams` 对象并返回一个 `SqlExecuteResult`。
-   **`SqlExecuteParams`**: 一个封装了所有执行所需参数的对象，包括数据库凭证、SQL 语句、绑定参数和安全限制。
-   **`SqlExecuteResult`**: 一个用于存放执行结果和元数据的容器。它提供了格式化输出的实用方法。
-   **`SafeSql`**: 一个用于预处理和净化 SQL 字符串的内部工具类，功能包括剥离注释和确保语句单一性。
-   **`NamedParamCompiler`**: 一个内部工具，可将带有命名参数的 SQL 透明地编译为使用 `?` 占位符的、兼容 JDBC 的 SQL。

---

## 使用示例

### 1. 添加你的数据库驱动

本库使用标准的 JDBC API，因此与任何提供 JDBC 驱动的数据库都兼容。它本身不包含任何特定的数据库驱动。

您作为库的使用者，**必须**将目标数据库的 JDBC 驱动添加到您项目的依赖中。

**例如：**

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

### 2. 使用命名参数进行基本查询

以下是如何使用命名参数执行一条 `SELECT` 语句。`url` 参数将决定使用哪个驱动。

```java
import com.alibaba.langengine.sqlexecutor.SqlExecuteParams;
import com.alibaba.langengine.sqlexecutor.SqlExecuteTool;
import com.alibaba.langengine.sqlexecutor.SqlExecuteResult;

import java.sql.SQLException;
import java.util.Map;

public class Example {
public static void main(String[] args) {
    // 1. 配置执行参数
    SqlExecuteParams params = new SqlExecuteParams();
    // URL 决定了使用哪个数据库和驱动
    params.setUrl("jdbc:postgresql://localhost:5432/mydatabase");
    params.setUsername("myuser");
    params.setPassword("mypassword");
    params.setSql("SELECT id, name, email FROM users WHERE status = :status AND registration_year > :year");
    params.setNamed(Map.of(
    "status", "active",
    "year", 2023
    ));

        // 2. 创建工具并执行
    SqlExecuteTool tool = new SqlExecuteTool();
    try {
        SqlExecuteResult result = tool.execute(params);

            // 3. 处理结果
        System.out.println("查询耗时 " + result.getElapsedMs() + " 毫秒.");
        System.out.println(result.toMarkdownTable());

        } catch (SQLException e) {
            System.err.println("数据库错误: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("执行错误: " + e.getMessage());
        }
    }
}
```

#### 输出示例：

```
查询耗时 45 毫秒.
| id  | name  | email             |
| --- | ---   | ---               |
| 101 | Alice | alice@example.com |
| 105 | Bob   | bob@example.com   |
```

### 3. `INSERT` 语句与生成的主键

```java
import com.alibaba.langengine.sqlexecutor.SqlExecuteParams;
import com.alibaba.langengine.sqlexecutor.SqlExecuteTool;
import com.alibaba.langengine.sqlexecutor.SqlExecuteResult;
import java.sql.SQLException;
import java.util.Map;

public class InsertExample {
public static void main(String[] args) {
// 1. 创建一个新的 SqlExecuteParams 对象并配置连接详情
SqlExecuteParams params = new SqlExecuteParams();
params.setUrl("jdbc:mysql://localhost:3306/mydatabase");
params.setUsername("myuser");
params.setPassword("mypassword");

        // 2. 为 INSERT 操作设置 SQL 语句和命名参数
        params.setSql("INSERT INTO products (name, price) VALUES (:name, :price)");
        params.setNamed(Map.of(
                "name", "Wireless Mouse",
                "price", 49.99
        ));

        // 3. 执行 SQL
        SqlExecuteTool tool = new SqlExecuteTool();
        try {
            SqlExecuteResult result = tool.execute(params);

            // 4. 打印结果
            System.out.println("语句类型: " + result.getType());
            System.out.println("影响行数: " + result.getUpdateCount());

            if (result.getGeneratedKeys() != null) {
                System.out.println("生成的主键: " + result.getGeneratedKeys());
            }

        } catch (SQLException e) {
            // 处理潜在的数据库访问错误
            System.err.println("数据库错误: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 处理无效参数或执行违规
            System.err.println("执行错误: " + e.getMessage());
        }
    }
}
```

#### 输出示例：

```
语句类型: UPDATE
影响行数: 1
生成的主键: [{GENERATED_KEY=15}]
```

---

##  安全与预处理

在执行之前，本库会自动对输入的 SQL 执行几个净化步骤：

1.  **剥离注释**: 移除所有 `/* ... */` 和 `--` 风格的注释。
2.  **移除末尾分号**: 移除任何末尾的分号和空白字符。
3.  **强制单一语句**: 验证输入字符串是否只包含一条可执行的语句。

这些步骤会透明地进行，以提供一个更安全的执行环境。