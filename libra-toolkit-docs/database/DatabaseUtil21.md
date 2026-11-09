# `DataSourceCore` 深度解析与最佳实践

## 目录

- [1. 引言 (Introduction)](#1-引言-introduction)
- [2. 核心概念 (Core Concepts)](#2-核心概念-core-concepts)
  - [2.1. 数据源标识 (`dbName`)](#21-数据源标识-dbname)
  - [2.2. 全局默认数据源](#22-全局默认数据源)
  - [2.3. 线程级数据源 (Thread-Local)](#23-线程级数据源-thread-local)
- [3. API 详解 (API Reference)](#3-api-详解-api-reference)
  - [3.1. 注册与注销](#31-注册与注销)
  - [3.2. 连接获取](#32-连接获取)
  - [3.3. 状态与查询](#33-状态与查询)
  - [3.4. 数据源切换](#34-数据源切换)
- [4. 最佳实践与使用场景](#4-最佳实践与使用场景)
  - [4.1. 场景一：单一数据源应用](#41-场景一单一数据源应用)
  - [4.2. 场景二：多数据源读写分离](#42-场景二多数据源读写分离)
  - [4.3. 场景三：动态租户切换](#43-场景三动态租户切换)

---

## 1. 引言 (Introduction)

`DataSourceCore` 是 `DatabaseUtil` 工具集的心脏，它不仅仅是一个数据源的容器，更是一个高度灵活、线程安全、支持动态切换的多数据源管理中枢。它的设计旨在以最简单的方式提供最强大的功能。

**核心特性:**

*   **多数据源生命周期管理**: 从通过代码或YML文件注册，到配置、使用和安全注销，全面管理一个或多个数据源。
*   **高性能连接获取**: 通过内置的轻量级 `SimpleDataSource` 实现和优化的读写锁机制 (`StampedLock`)，在保证线程安全的同时提供高效的数据库连接。
*   **无缝动态切换**: 支持全局默认数据源和基于线程的动态数据源切换，是实现读写分离、多租户SaaS架构的关键基石。

---

## 2. 核心概念 (Core Concepts)

在深入API之前，理解以下几个核心概念至关重要。

### 2.1. 数据源标识 (`dbName`)

在 `DataSourceCore` 中，每一个注册的数据源都必须有一个唯一的字符串名称，即 `dbName`。它是在整个工具中识别和操作特定数据源的唯一句柄，无论是获取连接、测试状态还是进行切换，都依赖于这个 `dbName`。

### 2.2. 全局默认数据源

系统中存在一个“全局默认数据源”的概念，由一个 `AtomicReference<String>` 类型的 `ATOMIC_DEFAULT_SOURCE` 变量维护。当代码中任何地方调用数据库操作而未明确指定 `dbName` 时，系统会自动使用这个默认数据源。通常，名为 `master` 的数据源会被自动设为默认。

### 2.3. 线程级数据源 (Thread-Local)

这是 `DataSourceCore` 最强大的特性之一。通过 `ThreadLocal<String>` 类型的 `THREAD_DATASOURCE_CONTEXT`，它允许为当前执行线程临时指定一个数据源。

*   **工作原理**: 当调用 `setThreadDataSourceContext(dbName)` 后，该线程后续的所有数据库操作都会强制使用指定的 `dbName`，无视全局默认设置。
*   **压倒性优先级**: 线程级数据源的优先级最高。
*   **风险与责任**: 这是一个有状态的操作。一旦使用，**必须**在操作完成后的 `finally` 块中调用 `clearThreadDataSourceContext()` 来清理。否则，该线程会被“污染”，后续被线程池复用时可能会错误地连接到非预期的数据库，导致数据错乱。

---

## 3. API 详解 (API Reference)

### 3.1. 注册与注销

#### `register(String dbName, String dbUrl, String username, String password, String driver)`
*   **功能描述**: 手动注册一个新的数据源到系统中。
*   **参数**:
    *   `dbName` (String): 数据源的唯一标识名称。
    *   `dbUrl` (String): JDBC URL。
    *   `username` (String): 数据库用户名。
    *   `password` (String): 数据库密码。
    *   `driver` (String): JDBC驱动的完整类名。
*   **返回**: `DatabaseAccessor` - 一个绑定到新注册数据源的访问器实例，方便进行链式调用。
*   **使用示例**:
    ```java
    DatabaseUtil21.DataSourceCore.register(
        "backup_db",
        "jdbc:mysql://localhost:3306/backup",
        "user", "pass", "com.mysql.cj.jdbc.Driver"
    );
    ```
*   **注意事项**: 如果注册一个已存在的 `dbName`，旧的数据源配置会被覆盖。

#### `registerYml(String dbName)`
*   **功能描述**: 从项目的 `application-dev.yml` 配置文件中读取并注册一个数据源。
*   **参数**: `dbName` (String) - YML文件中 `spring.datasource.dynamic.datasource` 下对应的键名。
*   **返回**: `DatabaseAccessor` - 绑定到新数据源的访问器。
*   **使用示例**:
    ```java
    // 假设YML中有 'second' 数据源的配置
    DatabaseUtil21.DataSourceCore.registerYml("second");
    ```

#### `closeDatabase(String dbName)`
*   **功能描述**: 关闭并注销一个指定的数据源。
*   **参数**: `dbName` (String) - 要关闭的数据源名称。
*   **注意事项**: 如果关闭的是当前的默认数据源，系统会自动选择一个剩余的数据源作为新的默认源。

#### `closeAllDatabases()`
*   **功能描述**: 关闭并注销所有已注册的数据源，清空整个数据源管理器。

### 3.2. 连接获取

#### `getConnection()`
*   **功能描述**: 获取当前生效的数据源的数据库连接。生效顺序为：线程级数据源 > 全局默认数据源。
*   **返回**: `java.sql.Connection` - 一个新的数据库连接。
*   **使用示例**:
    ```java
    try (Connection conn = DatabaseUtil21.DataSourceCore.getConnection()) {
        // ...
    }
    ```

#### `getConnection(String dbName)`
*   **功能描述**: 获取指定 `dbName` 的数据库连接。此方法会**忽略**线程级和全局默认设置。
*   **参数**: `dbName` (String) - 要连接的数据源名称。
*   **返回**: `java.sql.Connection` - 一个新的数据库连接。
*   **使用示例**:
    ```java
    try (Connection conn = DatabaseUtil21.DataSourceCore.getConnection("backup_db")) {
        // ...
    }
    ```

### 3.3. 状态与查询

#### `getRegisteredDatabases()`
*   **功能描述**: 获取所有当前已注册的数据源名称列表。
*   **返回**: `List<String>`。

#### `isDatabaseRegistered(String dataSourceName)`
*   **功能描述**: 检查指定名称的数据源是否已经被注册。
*   **返回**: `boolean`。

#### `testConnection()`
*   **功能描述**: 测试与当前生效的默认数据源的连接是否通畅。
*   **返回**: `boolean`。

#### `testConnection(String dbName)`
*   **功能描述**: 测试与指定 `dbName` 的数据源的连接是否通畅。
*   **返回**: `boolean`。

### 3.4. 数据源切换

#### `setDefaultDataSource(String dataSourceName)`
*   **功能描述**: 设置一个新的全局默认数据源。
*   **参数**: `dataSourceName` (String) - 必须是一个已经注册的数据源名称。
*   **返回**: `boolean` - 如果设置成功返回 `true`。

#### `getDefaultDatabaseName()`
*   **功能描述**: 获取当前全局默认数据源的名称。
*   **返回**: `String`。

#### `setThreadDataSourceContext(String dataSourceName)`
*   **功能描述**: 为当前线程设置一个特定的数据源。一旦设置，该线程后续所有数据库操作都将使用此数据源，直到被清除。
*   **参数**: `dataSourceName` (String) - 要为当前线程绑定的数据源名称。
*   **注意事项**: **极其重要！** 必须在 `try...finally` 结构中使用，并在 `finally` 块中调用 `clearThreadDataSourceContext()`。

#### `clearThreadDataSourceContext()`
*   **功能描述**: 清除当前线程的数据源设置，使其恢复到使用全局默认数据源的模式。

#### `getCurrentDataSourceName()`
*   **功能描述**: 获取当前操作最终将要使用的数据源名称。它会按“线程级 > 全局默认”的优先级顺序返回结果。
*   **返回**: `String`。

---

## 4. 最佳实践与使用场景

### 4.1. 场景一：单一数据源应用

对于大多数简单应用，您无需任何额外操作。工具类在启动时会自动注册YML或代码中配置的数据源，并设置默认源。

```java
// 直接使用默认数据源执行查询
List<Map<String, Object>> users = DatabaseUtil21.selectList("SELECT * FROM users", new HashMap<>());
```

### 4.2. 场景二：多数据源读写分离

假设 `master` 是主库（写），`slave` 是从库（读）。

```java
// 写入操作，使用默认的 master 库
DatabaseUtil21.accessor()
    .insert("INSERT INTO logs (message) VALUES (#{msg})", Map.of("msg", "user login"))
    .commit();

// 读取操作，显式指定 slave 库
List<Map<String, Object>> logs = DatabaseUtil21.accessor("slave")
    .selectList("SELECT * FROM logs", new HashMap<>());
```

### 4.3. 场景三：动态租户切换

在Web应用的请求拦截器或Filter中，可以根据用户的身份动态切换数据源。

```java
// 在请求处理前 (例如 Filter 的 doFilter 方法)
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    String tenantId = request.getParameter("tenantId"); // "tenant_a_db"
    try {
        // 为当前请求线程绑定租户的数据库
        DatabaseUtil21.DataSourceCore.setThreadDataSourceContext(tenantId);
        chain.doFilter(request, response);
    } finally {
        // 请求处理完毕后，必须清理线程上下文！
        DatabaseUtil21.DataSourceCore.clearThreadDataSourceContext();
    }
}

// 在Service层，代码无需关心数据源切换，直接执行即可
@Service
public class ProductService {
    public List<Map<String, Object>> getProducts() {
        // 这里的查询会自动路由到当前线程绑定的租户数据库
        return DatabaseUtil21.selectList("SELECT * FROM products", new HashMap<>());
    }
}
