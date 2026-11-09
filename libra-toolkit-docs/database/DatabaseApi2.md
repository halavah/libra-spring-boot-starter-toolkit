# DatabaseUtil.java 使用指南

本指南旨在通过实际场景和代码示例，帮助开发者快速掌握 `DatabaseUtil` 工具类的核心用法。

## 📚 目录

1.  [日志调试](#1-日志调试)
    -   [1.1 日志开关控制](#11-日志开关控制)
    -   [1.2 手动日志输出](#12-手动日志输出)
2.  [数据源管理](#2-数据源管理)
    -   [2.1 自动注册 (推荐)](#21-自动注册-推荐)
    -   [2.2 手动代码注册](#22-手动代码注册)
    -   [2.3 便捷的数据源管理API](#23-便捷的数据源管理api)
        -   [2.3.1 数据源注册与注销](#231-数据源注册与注销)
        -   [2.3.2 数据源状态查询](#232-数据源状态查询)
        -   [2.3.3 数据源切换与上下文管理](#233-数据源切换与上下文管理)
3.  [事务管理](#3-事务管理)
    -   [3.1 核心API](#31-核心api)
    -   [3.2 场景示例](#32-场景示例)
        -   [3.2.1 场景一：标准转账操作](#321-场景一标准转账操作)
        -   [3.2.2 场景二：在提交前检查中间结果](#322-场景二在提交前检查中间结果)
4.  [执行无参数SQL](#4-执行无参数sql)
    -   [4.1 执行DDL（返回布尔值）](#41-执行ddl返回布尔值)
    -   [4.2 执行DQL查询（返回列表）](#42-执行dql查询返回列表)
    -   [4.3 执行DML操作（返回影响行数）](#43-执行dml操作返回影响行数)
5.  [物理分页](#5-物理分页)
    -   [5.1 三种SQL提供模式](#51-三种sql提供模式)
    -   [5.2 API 列表](#52-api-列表)
    -   [5.3 使用示例](#53-使用示例)
6.  [批量操作](#6-批量操作)
    -   [6.1 功能状态说明](#61-功能状态说明)
    -   [6.2 API 列表](#62-api-列表)
    -   [6.3 使用示例](#63-使用示例)
    -   [6.4 SQL生成说明](#64-SQL生成说明)
7.  [支持普通SQL](#7-支持普通sql)
    -   [7.1 三种SQL提供模式](#71-三种sql提供模式)
    -   [7.2 链式调用 (DatabaseAccessor)](#72-链式调用-databaseaccessor)
    -   [7.3 全局静态方法 (DatabaseUtil)](#73-全局静态方法-databaseutil)
8.  [支持MP动态标签](#8-支持mp动态标签)
    -   [8.1 三种SQL提供模式 (MP版本)](#81-三种sql提供模式-mp版本)
    -   [8.2 链式调用 (DatabaseAccessor)](#82-链式调用-databaseaccessor)
    -   [8.3 全局静态方法 (DatabaseUtil)](#83-全局静态方法-databaseutil)

---

## 1. 日志调试

### 1.1 日志开关控制
```java
// 启用普通调试日志
DatabaseUtil.LoggerCore.setDebugLogEnabled(true);

// 启用SQL执行详细日志（包含参数和执行时间）
DatabaseUtil.LoggerCore.setSqlDebugLogEnabled(true);
```

### 1.2 手动日志输出
```java
DatabaseUtil.LoggerCore.debugLog("这是一条调试信息");
DatabaseUtil.LoggerCore.debugErrorLog("这是一条错误信息");
try {
    throw new RuntimeException("测试异常");
} catch (Exception e) {
    DatabaseUtil.LoggerCore.logException("测试上下文", e);
}
```

---

## 2. 数据源管理

在使用 `DatabaseUtil` 之前，您需要先注册至少一个数据源。

### 2.1 自动注册 (推荐)

这是最常见的工程化方式。`DatabaseUtil` 会在类加载时，自动读取 `DATABASE_MANAGE_CONNECTIONS` 静态常量并注册所有数据源。

**示例 `DatabaseUtil.java` 中的配置:**
```java
public static final String[][] DATABASE_MANAGE_CONNECTIONS = {
    {"master", "jdbc:mysql://localhost:3306/db_master?useSSL=false", "root", "password", "com.mysql.cj.jdbc.Driver"},
    {"second", "jdbc:mysql://localhost:3306/db_second?useSSL=false", "root", "password", "com.mysql.cj.jdbc.Driver"}
};
```

### 2.2 手动代码注册

如果您需要动态连接一个不在静态常量中定义的数据库，可以使用手动注册。

```java
// 手动注册一个新的Oracle数据源
DatabaseUtil.DataSourceCore.register(
    "temp_oracle",
    "jdbc:oracle:thin:@localhost:1521:xe",
    "user",
    "password",
    "oracle.jdbc.driver.OracleDriver"
);
```

### 2.3 便捷的数据源管理API

`DatabaseUtil.DataSourceCore` 提供了一套丰富的静态API，用于在运行时动态管理、查询和切换数据源。

#### 2.3.1 数据源注册与注销

-   `register(dbName, url, user, pass, driver)`: 动态注册一个新的数据源。
-   `closeDatabase(dbName)`: 注销并关闭指定的数据源。
-   `closeAllDatabases()`: 关闭所有已注册的数据源。

```java
// 动态注册一个PostgreSQL数据源
DatabaseUtil.DataSourceCore.register(
    "pg_db",
    "jdbc:postgresql://localhost:5432/analytics",
    "pg_user",
    "pg_pass",
    "org.postgresql.Driver"
);
System.out.println("PostgreSQL数据源注册成功！");

// 使用完后关闭它
DatabaseUtil.DataSourceCore.closeDatabase("pg_db");
System.out.println("PostgreSQL数据源已关闭。");
```

#### 2.3.2 数据源状态查询

-   `getRegisteredDatabases()`: 获取所有已注册数据源名称的 `List`。
-   `getDataSourceNames()`: 获取所有已注册数据源名称的 `Set`。
-   `isDatabaseRegistered(dbName)`: 检查指定名称的数据源是否存在。
-   `testConnection(dbName)`: 测试指定数据源的连接是否有效。
-   `testConnection()`: 测试默认数据源的连接。
-   `getDataSourceType(dbName)`: 获取数据源的类型（在当前实现中总是 "简单数据源"）。
-   `getAllDataSourceInfo()`: 获取所有数据源的详细信息，包括类型、是否为默认、连接状态等。

```java
// 查询所有已注册的数据源
System.out.println("当前已注册的数据源: " + DatabaseUtil.DataSourceCore.getRegisteredDatabases());

// 检查 "master" 是否存在
if (DatabaseUtil.DataSourceCore.isDatabaseRegistered("master")) {
    // 测试连接
    boolean isOk = DatabaseUtil.DataSourceCore.testConnection("master");
    System.out.println("master数据源连接状态: " + (isOk ? "正常" : "失败"));
}

// 获取所有数据源的详细报告
Map<String, Map<String, Object>> allInfo = DatabaseUtil.DataSourceCore.getAllDataSourceInfo();
allInfo.forEach((name, info) -> {
    System.out.println("--- 数据源: " + name + " ---");
    info.forEach((key, value) -> System.out.println(key + ": " + value));
});
```

#### 2.3.3 数据源切换与上下文管理

`DatabaseUtil` 支持全局默认数据源和线程级数据源的灵活切换。

-   `setDefaultDataSource(dbName)`: 设置全局默认的数据源。
-   `getDefaultDatabaseName()`: 获取当前全局默认数据源的名称。
-   `setThreadDataSourceContext(dbName)`: **【重要】** 在当前线程中临时切换数据源。此设置优先级高于全局默认。
-   `clearThreadDataSourceContext()`: **【重要】** 清除当前线程的数据源设置，恢复使用全局默认。
-   `getCurrentDataSourceName()`: 获取当前操作实际生效的数据源名称（优先返回线程设置，其次是全局默认）。

**场景：在一次请求中临时访问另一个数据库**

```java
System.out.println("当前默认数据源是: " + DatabaseUtil.DataSourceCore.getDefaultDatabaseName()); // 假设是 "master"

// 主业务逻辑使用默认数据源
List<Map<String, Object>> mainData = DatabaseUtil.selectList("SELECT * FROM main_table", null);
System.out.println("从master库查询到 " + mainData.size() + " 条主数据。");

// 需要临时从 "second" 数据源读取日志
try {
    // 切换当前线程的数据源到 "second"
    DatabaseUtil.DataSourceCore.setThreadDataSourceContext("second");
    System.out.println("当前线程实际使用的数据源: " + DatabaseUtil.DataSourceCore.getCurrentDataSourceName()); // 输出 "second"

    // 此时，所有不指定数据源的DB操作都将走 "second"
    List<Map<String, Object>> logData = DatabaseUtil.selectList("SELECT * FROM log_table", null);
    System.out.println("从second库查询到 " + logData.size() + " 条日志数据。");

} finally {
    // 操作完成，必须在finally块中清除线程上下文，避免影响线程池中的下一次请求
    DatabaseUtil.DataSourceCore.clearThreadDataSourceContext();
    System.out.println("线程数据源已清除，恢复默认。");
}

// 线程上下文清除后，操作再次回到默认数据源
System.out.println("当前默认数据源是: " + DatabaseUtil.DataSourceCore.getCurrentDataSourceName()); // 输出 "master"
```

---

## 3. 事务管理

事务管理通过 **链式调用** 的 `DatabaseAccessor` 实现，可以确保一组操作的原子性。

### 3.1 核心API

-   `.setTransaction()`: 开启一个新事务。
-   `.commit()`: 提交事务，并返回所有写操作 **累计影响的总行数**。
-   `.rollback()`: 回滚事务。
-   `.result()`: 在事务中，获取**最近一次**写操作影响的行数。
-   `.resultAll()`: 在事务中，获取当前所有已执行操作的**累计影响总行数**。

### 3.2 场景示例

#### 3.2.1 场景一：标准转账操作

这是一个经典的事务场景：从一个账户扣款，并为另一个账户增款。

```java
DatabaseAccessor accessor = DatabaseUtil.accessor("master");

try {
    // 1. 开启事务
    accessor.setTransaction();

    // 2. 执行一系列数据库操作
    Map<String, Object> debitParams = Map.of("userId", 101, "amount", 99.9);
    accessor.execute("UPDATE users SET balance = balance - #{amount} WHERE id = #{userId}", debitParams);

    Map<String, Object> creditParams = Map.of("userId", 202, "amount", 99.9);
    accessor.execute("UPDATE users SET balance = balance + #{amount} WHERE id = #{userId}", creditParams);

    // 3. 提交事务并获取结果
    int totalAffectedRows = accessor.commit();
    System.out.println("转账成功，事务已提交。");
    System.out.println("累计影响行数: " + totalAffectedRows); // 预期输出: 2

} catch (Exception e) {
    // 4. 如果发生任何异常，执行回滚
    accessor.rollback();
    System.err.println("操作失败，事务已回滚: " + e.getMessage());
}
```

#### 3.2.2 场景二：在提交前检查中间结果

有时，您可能需要在事务提交前，根据已执行操作的结果来决定后续步骤。`.result()` 和 `.resultAll()` 方法就是为此设计的。

假设我们需要创建一个订单，并立即更新商品库存。如果库存更新失败（例如，影响行数为0），我们希望回滚整个操作。

```java
DatabaseAccessor accessor = DatabaseUtil.accessor("master");

try {
    accessor.setTransaction();

    // 1. 创建订单
    Map<String, Object> orderParams = Map.of("userId", 101, "productId", 50, "quantity", 2);
    accessor.execute("INSERT INTO orders (user_id, product_id, quantity) VALUES (#{userId}, #{productId}, #{quantity})", orderParams);
    System.out.println("创建订单影响行数: " + accessor.result()); // 输出: 1
    System.out.println("当前累计影响行数: " + accessor.resultAll()); // 输出: 1

    // 2. 更新库存
    Map<String, Object> stockParams = Map.of("productId", 50, "quantity", 2);
    accessor.execute("UPDATE products SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity}", stockParams);

    // 3. 在提交前检查最近一次操作的结果
    if (accessor.result() > 0) {
        // 4a. 如果库存更新成功，提交事务
        int totalRows = accessor.commit();
        System.out.println("订单创建成功，库存已更新。总影响行数: " + totalRows); // 输出: 2
    } else {
        // 4b. 如果库存不足导致更新失败，回滚事务
        accessor.rollback();
        System.err.println("库存不足，订单创建失败，事务已回滚。");
    }

} catch (Exception e) {
    accessor.rollback();
    System.err.println("发生意外错误，事务已回滚: " + e.getMessage());
}
```

---

## 4. 执行无参数SQL

本章节介绍如何执行不包含 `#{...}` 参数占位符的静态SQL语句。

### 4.1 执行DDL（返回布尔值）

-   **核心方法**: `executeDDL(sql)`
-   **功能**: 主要用于执行DDL语句（如 `CREATE`, `ALTER`, `DROP`）。
-   **返回值**: `boolean`。根据JDBC `Statement.execute()` 的定义，`true` 表示执行的SQL返回了一个结果集（如 `SELECT`），`false` 表示没有结果集或返回的是更新计数。对于DDL操作，通常返回 `false`。

#### 调用方式

-   **全局静态方法**:
    ```java
    boolean result = DatabaseUtil.executeDDL("CREATE TABLE ...");
    ```
-   **链式调用**:
    ```java
    boolean result = DatabaseUtil.accessor().executeDDL("DROP TABLE ...");
    ```

### 4.2 执行DQL查询（返回列表）

-   **核心方法**: `executeDQL(sql)`
-   **功能**: 用于执行无参数的 `SELECT` 查询。
-   **返回值**: `List<Map<String, Object>>`。

#### 调用方式

-   **全局静态方法**:
    ```java
    List<Map<String, Object>> users = DatabaseUtil.executeDQL("SELECT id, name FROM users WHERE status = 'active'");
    ```
-   **链式调用**:
    ```java
    List<Map<String, Object>> logs = DatabaseUtil.accessor("second").executeDQL("SELECT * FROM system_logs");
    ```

### 4.3 执行DML操作（返回影响行数）

-   **核心方法**: `executeDML(sql)`
-   **功能**: 用于执行无参数的DML语句（如 `INSERT`, `UPDATE`, `DELETE`）。
-   **返回值**: `int`，表示受本次操作影响的数据库行数。

#### 调用方式

-   **全局静态方法**:
    ```java
    int deletedRows = DatabaseUtil.executeDML("DELETE FROM users WHERE last_login < '2023-01-01'");
    ```
-   **链式调用**:
    ```java
    int updatedRows = DatabaseUtil.accessor().executeDML("UPDATE products SET on_sale = false WHERE stock = 0");
    ```

---

## 5. 物理分页

本功能支持普通SQL的三种提供模式，可以方便地进行分页查询。

### 5.1 三种SQL提供模式

1.  **直接SQL (`selectPage`)**
    -   **说明**: 直接在Java代码中提供用于分页的SQL字符串。
    -   **示例**: `DatabaseUtil.selectPage("SELECT * FROM users", 1, 10, null)`

2.  **指定XML (`selectPageFromXml`)**
    -   **说明**: 将分页SQL存放在指定的XML文件中，通过文件名和SQL的唯一ID来调用。
    -   **示例**: `DatabaseUtil.selectPageFromXml("user_sqls.xml", "findAllUsers", 1, 10, params)`

3.  **自动搜索XML (`selectPageFromXmlAuto`)**
    -   **说明**: 无需指定文件名，工具类会自动扫描预设目录下的所有XML文件，根据唯一的SQL ID来查找并执行。
    -   **示例**: `DatabaseUtil.selectPageFromXmlAuto("findAllUsers", 1, 10, params)`

### 5.2 API 列表

-   **普通SQL**: `selectPage`, `selectPageFromXml`, `selectPageFromXmlAuto`
-   **MP动态标签**: `selectPageByMp`, `selectPageFromXmlByMp`, `selectPageFromXmlAutoByMp`

### 5.3 使用示例

```java
// 使用全局静态方法进行分页查询
PageResult<Map<String, Object>> pageResult = DatabaseUtil.selectPage(
    "SELECT * FROM users WHERE status = #{status}", 1, 10, params
);

// 使用链式调用进行分页查询
DatabaseAccessor accessor = DatabaseUtil.accessor("second");
PageResult<Map<String, Object>> logPage = accessor.selectPage(
    "SELECT * FROM system_logs WHERE type = #{type}", 1, 5, params
);
```

---

## 6. 批量操作

### 6.1 功能状态说明

-   **推荐使用**: `batchUpsert` 方法。这是一个功能强大且稳定的“合并”(MERGE)或“存在则更新，不存在则插入”(UPSERT)操作。它会根据数据库类型（H2, MySQL, Oracle）在内部自动生成最优的、安全的SQL语句，无需手动编写SQL。
-   **暂停使用**: `batchExecute` 及相关方法 (`batchExecuteFromXml`, `batchExecuteFromXmlAuto`)。这些通用的批量执行方法目前存在逻辑缺陷，在处理不同类型的SQL和参数时会失败，请等待后续版本修复。

### 6.2 API 列表

`batchUpsert` 方法的核心是处理一个数据列表，并根据指定的键（Key）来判断每条数据在数据库中是应该插入新行还是更新旧行。

#### 参数说明

-   `tableName` (String): 需要操作的数据库表名。
-   `entities` (List<Map<String, Object>>): 要处理的数据列表。列表中的每一个 `Map` 代表一行数据，其中 `key` 是列名，`value` 是对应的值。
-   `keyColumns` (String[]): **键列**数组。用于在 `ON` 或 `WHERE` 子句中判断记录是否存在的列，通常是表的主键或唯一索引。
-   `valueColumns` (String[]): **值列**数组。如果记录存在（即键匹配成功），这些列的值将会被更新。
-   `batchSize` (int, 可选): 每次向数据库提交的记录数量。默认值为1000。

### 6.3 使用示例

下面我们将通过两种主要方式来调用 `batchUpsert` 功能：**静态方法调用**和**链式访问器调用**。

#### 示例1：静态方法调用

静态方法调用最简单直接，适合一次性的批量操作。我们直接使用 `BatchOperationHandler` 中的方法。

```java
import cn.myslayers.app.utils.DatabaseUtil31.BatchOperationHandler;
import cn.myslayers.app.utils.DatabaseUtil31.BatchOperationHandler.BatchResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ...

// 1. 准备要操作的数据
String tableName = "test_h2_upsert";
List<Map<String, Object>> entities = new ArrayList<>();

// 添加第一条数据（将用于插入）
Map<String, Object> entity1 = new HashMap<>();
entity1.put("id", 101);
entity1.put("category", "CAT_A");
entity1.put("title", "新发布的文章");
entity1.put("content", "这是文章的初始内容。");
entity1.put("score", 90.5);
entities.add(entity1);

// 添加第二条数据（假设id=102, category='CAT_B'的记录已存在，将用于更新）
Map<String, Object> entity2 = new HashMap<>();
entity2.put("id", 102);
entity2.put("category", "CAT_B");
entity2.put("title", "被更新的文章标题");
entity2.put("content", "内容已被更新。");
entity2.put("score", 95.0);
entities.add(entity2);

// 2. 定义键列和值列
String[] keyColumns = {"id", "category"}; // 使用 id 和 category 共同判断唯一性
String[] valueColumns = {"title", "content", "score"}; // 如果记录存在，则更新这三列

// 3. 执行批量UPSERT操作
// 参数：数据库名, 表名, 数据列表, 键列, 值列, 批次大小
BatchResult result = BatchOperationHandler.batchUpsertSizeWithDbName(
    "your_database_name", // 替换为你的数据源名称
    tableName,
    entities,
    keyColumns,
    valueColumns,
    500 // 每500条记录提交一次
);

// 4. 处理结果
if (result.isSuccess()) {
    System.out.println("批量UPSERT操作成功！");
    System.out.println("总处理记录数: " + result.getTotalRecords());
} else {
    System.out.println("批量UPSERT操作失败！");
    System.out.println("失败记录数: " + result.getFailedRecords());
    System.out.println("错误信息: " + result.getErrorMessages());
}
```

#### 示例2：链式访问器调用（支持事务）

链式访问器更灵活，尤其适合在事务中执行多个数据库操作。

```java
import cn.myslayers.app.utils.DatabaseUtil31;
import cn.myslayers.app.utils.DatabaseUtil31.DatabaseAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ...

// 准备数据（同上）
String tableName = "test_h2_upsert";
List<Map<String, Object>> entities = new ArrayList<>();

// 添加第一条数据（将用于插入）
Map<String, Object> entity1 = new HashMap<>();
entity1.put("id", 101);
entity1.put("category", "CAT_A");
entity1.put("title", "新发布的文章");
entity1.put("content", "这是文章的初始内容。");
entity1.put("score", 90.5);
entities.add(entity1);

// 添加第二条数据（假设id=102, category='CAT_B'的记录已存在，将用于更新）
Map<String, Object> entity2 = new HashMap<>();
entity2.put("id", 102);
entity2.put("category", "CAT_B");
entity2.put("title", "被更新的文章标题");
entity2.put("content", "内容已被更新。");
entity2.put("score", 95.0);
entities.add(entity2);

// 定义键列和值列
String[] keyColumns = {"id", "category"};
String[] valueColumns = {"title", "content", "score"};

// 获取指定数据源的访问器
DatabaseAccessor accessor = DatabaseUtil31.accessor("your_database_name");

try {
    // 1. 开启事务
    accessor.setTransaction();

    // 2. 在事务中执行批量UPSERT
    // 注意：链式API目前使用默认批次大小，如需自定义，请直接使用BatchOperationHandler
    accessor.batchUpsert(tableName, entities, keyColumns, valueColumns);

    // 可以在同一个事务中执行其他操作
    // accessor.execute("UPDATE some_other_table SET status = 'processed' WHERE ...", new HashMap<>());

    // 3. 提交事务
    accessor.commit();
    System.out.println("事务成功提交！");

} catch (Exception e) {
    // 4. 如果发生异常，回滚事务
    System.err.println("操作失败，事务将回滚: " + e.getMessage());
    accessor.rollback();
}
```

### 6.4 SQL生成说明

您问了一个很好的问题。`batchUpsert` 方法最终生成的SQL语句取决于您连接的数据库类型。这个方法的核心优势就在于它的“跨数据库兼容性”，它会智能地为不同数据库生成最优的 `MERGE` 或 `INSERT ... ON DUPLICATE KEY UPDATE` 语句。

#### 1. 如果您连接的是 **MySQL** 或 **H2 (MySQL模式)**

工具会生成一个 `INSERT ... ON DUPLICATE KEY UPDATE` 语句。根据您提供的示例，生成的SQL会是这样的：

```sql
INSERT INTO test_h2_upsert (id, category, title, content, score) 
VALUES (?, ?, ?, ?, ?) 
ON DUPLICATE KEY UPDATE 
  title = VALUES(title), 
  content = VALUES(content), 
  score = VALUES(score)
```

**工作原理**:
*   **`INSERT INTO ...`**: 尝试插入一行新数据。
*   **`ON DUPLICATE KEY UPDATE`**: 如果插入时违反了主键或唯一索引（在这里是 `id` 和 `category` 的组合），MySQL不会报错，而是会执行 `UPDATE` 部分的逻辑。
*   **`VALUES(column_name)`**: 这是一个MySQL特有的函数，代表在 `INSERT` 部分试图插入的值。这样可以确保更新的值就是您最新提供的值。

#### 2. 如果您连接的是 **Oracle**

工具会生成一个标准的 `MERGE` 语句，这是Oracle处理此类问题的标准方式。生成的SQL会是这样的：

```sql
MERGE INTO test_h2_upsert t 
USING (
  SELECT ? AS id, ? AS category, ? AS title, ? AS content, ? AS score FROM DUAL
) s 
ON (t.id = s.id AND t.category = s.category) 
WHEN MATCHED THEN 
  UPDATE SET t.title = s.title, t.content = s.content, t.score = s.score 
WHEN NOT MATCHED THEN 
  INSERT (id, category, title, content, score) 
  VALUES (s.id, s.category, s.title, s.content, s.score)
```

**工作原理**:
*   **`MERGE INTO ...`**: 声明要合并数据的目标表 `t`。
*   **`USING (...) s`**: 创建一个包含单行新数据的虚拟表 `s`。
*   **`ON (...)`**: 定义目标表 `t` 和源表 `s` 之间的匹配条件（基于您提供的 `keyColumns`）。
*   **`WHEN MATCHED THEN UPDATE ...`**: 如果 `ON` 条件满足（即记录已存在），则执行更新操作。
*   **`WHEN NOT MATCHED THEN INSERT ...`**: 如果 `ON` 条件不满足（即记录不存在），则执行插入操作。

#### 3. 如果您连接的是 **H2 (原生模式)**

H2数据库有自己更简洁的 `MERGE` 语法。工具会生成类似这样的SQL：

```sql
MERGE INTO test_h2_upsert KEY(id, category) 
VALUES (?, ?, ?, ?, ?)
```

**工作原理**:
*   **`MERGE INTO ... KEY(...)`**: 这是H2的简化语法，它直接告诉数据库使用 `id` 和 `category` 作为判断记录是否存在的键。
*   **`VALUES (...)`**: 提供要插入或更新的数据。如果键存在，H2会自动更新该行；如果不存在，则插入新行。

---

## 7. 支持普通SQL

本章节介绍使用 `#{param}` 作为参数占位符，并支持简单 `<if>` 标签的SQL执行方式。

### 7.1 三种SQL提供模式

1.  **直接SQL**
    -   **说明**: 直接在Java代码中提供完整的SQL字符串。
    -   **优点**: 简单直接，适合动态构建或一次性的简单查询。
    -   **示例**: `DatabaseUtil.selectList("SELECT * FROM users WHERE id = #{userId}", params)`

2.  **指定XML (`FromXml` 后缀)**
    -   **说明**: 将SQL语句存放在指定的XML文件中，通过文件名和SQL的唯一ID来调用。
    -   **配置**: `public static final String[] XML_DIRECTORIES = {"src/main/resources/sql/", "resources/sql/"};`
    -   **示例**: `DatabaseUtil.selectListFromXml("user_sqls.xml", "findUserById", params)`

3.  **自动搜索XML (`FromXmlAuto` 后缀)**
    -   **说明**: 无需指定文件名，工具类会自动扫描 `XML_DIRECTORIES` 预设目录下的所有XML文件，根据唯一的SQL ID来查找并执行。
    -   **优点**: 极致的简洁，调用时只需关心SQL的ID即可。
    -   **示例**: `DatabaseUtil.selectListFromXmlAuto("findUserById", params)`

### 7.2 链式调用 (DatabaseAccessor)

#### 查询列表
- `accessor.selectList(sql, params)`
- `accessor.selectListFromXml(xmlFile, sqlId, params)`
- `accessor.selectListFromXmlAuto(sqlId, params)`

#### 查询单条
- `accessor.selectOne(sql, params)`
- `accessor.selectOneFromXml(xmlFile, sqlId, params)`
- `accessor.selectOneFromXmlAuto(sqlId, params)`

#### 执行写操作
- `accessor.execute(sql, params)`
- `accessor.executeFromXml(xmlFile, sqlId, params)`
- `accessor.executeFromXmlAuto(sqlId, params)`

### 7.3 全局静态方法 (DatabaseUtil)

#### 查询列表
- `DatabaseUtil.selectList(sql, params)`
- `DatabaseUtil.selectListFromXml(xmlFile, sqlId, params)`
- `DatabaseUtil.selectListFromXmlAuto(sqlId, params)`

#### 查询单条
- `DatabaseUtil.selectOne(sql, params)`
- `DatabaseUtil.selectOneFromXml(xmlFile, sqlId, params)`
- `DatabaseUtil.selectOneFromXmlAuto(sqlId, params)`

#### 执行写操作
- `DatabaseUtil.execute(sql, params)`
- `DatabaseUtil.executeFromXml(xmlFile, sqlId, params)`
- `DatabaseUtil.executeFromXmlAuto(sqlId, params)`

---

## 8. 支持MP动态标签

方法名以 `ByMp` 结尾，完整支持MyBatis所有动态SQL标签，如 `<where>`, `<foreach>`, `<choose>` 等。

### 8.1 三种SQL提供模式 (MP版本)

1.  **直接SQL (`ByMp` 后缀)**
    -   **说明**: 直接在Java代码中提供包含MyBatis标签的完整SQL字符串。
    -   **示例**: `DatabaseUtil.selectListByMp("<select>SELECT * FROM users <if test='id != null'>WHERE id = #{id}</if></select>", params)`

2.  **指定XML (`FromXmlByMp` 后缀)**
    -   **说明**: 将包含MyBatis标签的SQL语句存放在指定的XML文件中。
    -   **配置**: `public static final String[] MYBATIS_XML_DIRECTORIES = {"src/main/java/cn/myslayers/device/mapper/xml/", "src/main/resources/mapper/"};`
    -   **示例**: `DatabaseUtil.selectListFromXmlByMp("user_sqls.xml", "findUserWithDynamicSql", params)`

3.  **自动搜索XML (`FromXmlAutoByMp` 后缀)**
    -   **说明**: 无需指定文件名，工具类会自动扫描 `MYBATIS_XML_DIRECTORIES` 预设目录下的所有XML文件，根据唯一的SQL ID来查找并执行。
    -   **示例**: `DatabaseUtil.selectListFromXmlAutoByMp("findUserWithDynamicSql", params)`

### 8.2 链式调用 (DatabaseAccessor)

#### 查询列表
- `accessor.selectListByMp(sql, params)`
- `accessor.selectListFromXmlByMp(xmlFile, sqlId, params)`
- `accessor.selectListFromXmlAutoByMp(sqlId, params)`

#### 查询单条
- `accessor.selectOneByMp(sql, params)`
- `accessor.selectOneFromXmlByMp(xmlFile, sqlId, params)`
- `accessor.selectOneFromXmlAutoByMp(sqlId, params)`

#### 执行写操作
- `accessor.executeByMp(sql, params)`
- `accessor.executeFromXmlByMp(xmlFile, sqlId, params)`
- `accessor.executeFromXmlAutoByMp(sqlId, params)`

### 8.3 全局静态方法 (DatabaseUtil)

#### 查询列表
- `DatabaseUtil.selectListByMp(sql, params)`
- `DatabaseUtil.selectListFromXmlByMp(xmlFile, sqlId, params)`
- `DatabaseUtil.selectListFromXmlAutoByMp(sqlId, params)`

#### 查询单条
- `DatabaseUtil.selectOneByMp(sql, params)`
- `DatabaseUtil.selectOneFromXmlByMp(xmlFile, sqlId, params)`
- `DatabaseUtil.selectOneFromXmlAutoByMp(sqlId, params)`

#### 执行写操作
- `DatabaseUtil.executeByMp(sql, params)`
- `DatabaseUtil.executeFromXmlByMp(xmlFile, sqlId, params)`
- `DatabaseUtil.executeFromXmlAutoByMp(sqlId, params)`
