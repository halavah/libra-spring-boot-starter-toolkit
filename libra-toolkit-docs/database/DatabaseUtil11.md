# DatabaseUtil11 多数据源数据库工具类

## 概述

DatabaseUtil11 是一个基于 HikariCP 的高性能多数据源数据库工具类，版本号为 2.0.0。它专注于提供企业级的数据库连接池管理和多数据源切换功能，支持自定义配置和 YAML 配置文件两种配置模式，是高并发环境下的理想数据库访问解决方案。

## 核心特性

### 🚀 高性能连接池
- **HikariCP 集成** - 采用业界最快的数据库连接池 HikariCP
- **连接池优化** - 精心调优的连接池参数，确保最佳性能
- **连接复用** - 高效的连接复用机制，降低连接创建开销
- **监控支持** - 内置连接池监控和命名，便于性能调优

### 🔄 多数据源管理
- **动态数据源** - 支持运行时动态注册和切换数据源
- **智能路由** - 灵活的数据源选择和默认数据源管理
- **配置隔离** - 每个数据源独立配置，互不影响
- **资源管理** - 完善的数据源生命周期管理

### ⚙️ 双配置模式
- **自定义配置模式** - 通过 DatabaseConfig 对象进行编程式配置
- **YAML 配置模式** - 从 application-dev.yml 文件读取配置
- **配置切换** - 支持配置模式的动态切换
- **参数验证** - 完整的配置参数校验机制

### 🛡️ 企业级特性
- **线程安全** - 所有操作均为线程安全，支持高并发访问
- **事务支持** - 完整的事务管理功能
- **异常处理** - 统一的异常处理和错误报告
- **资源自动清理** - 自动管理连接和资源释放

## 技术架构

```
DatabaseUtil11 架构图
├── 配置管理层
│   ├── DatabaseConfig (数据库配置类)
│   ├── YmlDataSourceInfo (YAML配置解析)
│   └── 配置模式切换管理
├── 连接池管理层
│   ├── HikariCP 连接池集成
│   ├── 数据源缓存管理 (ConcurrentHashMap)
│   └── 连接池生命周期管理
├── 多数据源路由层
│   ├── 默认数据源管理
│   ├── 数据源选择策略
│   └── 动态数据源注册
├── 事务管理层
│   ├── TransactionManager (事务管理器)
│   ├── TransactionCallback (事务回调)
│   └── 自动提交/回滚机制
└── SQL执行层
    ├── SqlExecutor (SQL执行器)
    ├── 参数处理和类型转换
    └── 结果集映射处理
```

## 快速开始

### 1. 自定义配置模式

```java
// 创建数据库配置
DatabaseConfig config = new DatabaseConfig(
    "jdbc:mysql://localhost:3306/testdb",
    "root",
    "password",
    "com.mysql.cj.jdbc.Driver"
);

// 注册数据库（自动启用自定义配置模式）
DatabaseUtil11.registerDatabase("testdb", config);

// 执行数据库操作
List<Map<String, Object>> users = DatabaseUtil11.selectList(
    "SELECT * FROM users WHERE status = ?", "active"
);
```

### 2. YAML 配置模式

**application-dev.yml 配置文件：**
```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/main_db
          username: root
          password: root123
          driver-class-name: com.mysql.cj.jdbc.Driver
        slave:
          url: jdbc:mysql://localhost:3306/slave_db
          username: reader
          password: reader123
          driver-class-name: com.mysql.cj.jdbc.Driver
```

**Java 代码：**
```java
// 从YAML配置注册数据库（自动启用YAML配置模式）
DatabaseUtil11.registerDatabaseFromYml("master");
DatabaseUtil11.registerDatabaseFromYml("slave");

// 切换到指定数据源执行操作
List<Map<String, Object>> masterData = DatabaseUtil11.SqlExecutor.selectList(
    "master", "SELECT * FROM orders", null
);

List<Map<String, Object>> slaveData = DatabaseUtil11.SqlExecutor.selectList(
    "slave", "SELECT * FROM reports", null
);
```

### 3. 高级连接池配置

```java
// 创建高性能连接池配置
DatabaseConfig config = new DatabaseConfig(
    "jdbc:mysql://localhost:3306/prod_db",
    "produser",
    "prodpassword",
    "com.mysql.cj.jdbc.Driver",
    20,      // 最大连接数
    10,      // 最小空闲连接数
    30000,   // 连接超时时间(30秒)
    600000,  // 空闲超时时间(10分钟)
    1800000  // 连接最大生命周期(30分钟)
);

DatabaseUtil11.registerDatabase("production", config);

// 设置为默认数据源
DatabaseUtil11.setDefaultDatabase("production");
```

### 4. 事务管理

```java
// 单数据源事务
Integer result = DatabaseUtil11.executeInTransaction(conn -> {
    int orderInsert = DatabaseUtil11.insert(
        "INSERT INTO orders (user_id, amount) VALUES (?, ?)",
        userId, orderAmount
    );

    int inventoryUpdate = DatabaseUtil11.update(
        "UPDATE inventory SET quantity = quantity - ? WHERE product_id = ?",
        quantity, productId
    );

    return orderInsert + inventoryUpdate;
});

// 指定数据源事务
Integer result = DatabaseUtil11.TransactionManager.executeInTransaction("master", conn -> {
    // 在master数据源中执行事务
    return DatabaseUtil11.SqlExecutor.insert("master", sql, params);
});
```

## API 详细说明

### 数据源管理

#### 注册数据源
```java
// 自定义配置注册
public static void registerDatabase(String dbName, DatabaseConfig config)

// YAML配置注册
public static void registerDatabaseFromYml(String dataSourceName)
```

#### 数据源控制
```java
// 设置默认数据库
public static void setDefaultDatabase(String dbName)

// 获取默认数据库名称
public static String getDefaultDatabase()

// 关闭指定数据库
public static void closeDatabase(String dbName)

// 关闭所有数据库
public static void closeAllDatabases()
```

#### 连接获取
```java
// 获取指定数据库连接
public static Connection getConnection(String dbName) throws SQLException

// 获取默认数据库连接
public static Connection getConnection() throws SQLException
```

### SQL 执行操作

#### 查询操作
```java
// 多行查询（默认数据库）
public static List<Map<String, Object>> selectList(String sql, Object... params)

// 单行查询（默认数据库）
public static Map<String, Object> selectOne(String sql, Object... params)

// 指定数据库查询
public static List<Map<String, Object>> SqlExecutor.selectList(String dbName, String sql, Object... params)
```

#### 更新操作
```java
// 插入操作
public static int insert(String sql, Object... params)

// 更新操作
public static int update(String sql, Object... params)

// 删除操作
public static int delete(String sql, Object... params)

// 批量操作
public static int[] executeBatch(String sql, List<Object[]> paramsList)
```

### 事务管理

#### 事务执行
```java
// 默认数据库事务
public static <T> T executeInTransaction(TransactionCallback<T> callback)

// 指定数据库事务
public static <T> T TransactionManager.executeInTransaction(String dbName, TransactionCallback<T> callback)
```

#### 事务回调接口
```java
@FunctionalInterface
public interface TransactionCallback<T> {
    T doInTransaction(Connection conn) throws SQLException;
}
```

## 配置参数详解

### DatabaseConfig 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `url` | String | 必填 | 数据库连接URL |
| `username` | String | 必填 | 数据库用户名 |
| `password` | String | 可选 | 数据库密码 |
| `driverClass` | String | 必填 | JDBC驱动类名 |
| `maximumPoolSize` | int | 10 | 连接池最大连接数 |
| `minimumIdle` | int | 5 | 连接池最小空闲连接数 |
| `connectionTimeout` | long | 30000 | 连接超时时间(毫秒) |
| `idleTimeout` | long | 600000 | 空闲连接超时时间(毫秒) |
| `maxLifetime` | long | 1800000 | 连接最大生命周期(毫秒) |

### HikariCP 优化建议

```java
// 生产环境推荐配置
DatabaseConfig prodConfig = new DatabaseConfig(
    jdbcUrl,
    username,
    password,
    driverClass,
    50,       // 最大连接数：根据应用并发量调整
    10,       // 最小空闲连接：保证基础性能
    20000,    // 连接超时：20秒，避免长时间等待
    300000,   // 空闲超时：5分钟，及时释放空闲连接
    1200000   // 最大生命周期：20分钟，定期刷新连接
);
```

## 配置模式管理

### 自定义配置模式
```java
// 启用自定义配置模式
DatabaseUtil11.useCustomConfig();

// 通过代码配置数据源
DatabaseConfig config = new DatabaseConfig(...);
DatabaseUtil11.registerDatabase("mydb", config);
```

### YAML 配置模式
```java
// 启用YAML配置模式
DatabaseUtil11.useYamlConfig();

// 从配置文件读取数据源
DatabaseUtil11.registerDatabaseFromYml("master");
```

### 配置模式特点

| 配置模式 | 优势 | 适用场景 |
|----------|------|----------|
| 自定义配置 | 灵活度高，支持动态配置 | 需要运行时动态配置的场景 |
| YAML配置 | 配置统一，便于管理 | 标准的Spring Boot项目 |

## 性能调优指南

### 连接池参数调优

1. **maximumPoolSize（最大连接数）**
   - 建议值：CPU核心数 × 2 + 磁盘数量
   - 过大：占用过多资源
   - 过小：可能出现连接等待

2. **minimumIdle（最小空闲连接）**
   - 建议值：最大连接数的 20%-50%
   - 保证基础性能响应时间

3. **connectionTimeout（连接超时）**
   - 建议值：20-30秒
   - 避免长时间等待，快速失败

### 批量操作优化

```java
// 大量数据插入优化
List<Object[]> batchData = new ArrayList<>();
for (User user : users) {
    batchData.add(new Object[]{user.getName(), user.getAge(), user.getEmail()});
}

// 使用批量操作提升性能
int[] results = DatabaseUtil11.executeBatch(
    "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
    batchData
);
```

### 事务边界优化

```java
// 优化事务边界，减少事务持有时间
DatabaseUtil11.executeInTransaction(conn -> {
    // 1. 准备数据（在事务外完成）
    // 2. 快速执行数据库操作
    // 3. 避免在事务中调用外部服务
    return result;
});
```

## 监控和诊断

### 连接池监控

HikariCP 提供了丰富的监控指标：

```java
// 连接池命名便于监控
// 格式：DatabaseUtil11-{dbName}-Pool
// 示例：DatabaseUtil11-master-Pool, DatabaseUtil11-slave-Pool
```

### 性能指标

- **连接获取时间**：监控连接获取的平均时间
- **活跃连接数**：监控当前活跃的连接数量
- **空闲连接数**：监控空闲连接池大小
- **连接创建速率**：监控新连接的创建频率

## 最佳实践

### 1. 数据源命名规范
```java
// 推荐使用有意义的数据源名称
DatabaseUtil11.registerDatabase("user-service-master", masterConfig);
DatabaseUtil11.registerDatabase("user-service-slave", slaveConfig);
DatabaseUtil11.registerDatabase("order-service-master", orderConfig);
```

### 2. 连接资源管理
```java
// 推荐：使用try-with-resources
try (Connection conn = DatabaseUtil11.getConnection("master")) {
    // 数据库操作
} catch (SQLException e) {
    // 异常处理
}
```

### 3. 事务设计原则
```java
// 推荐：事务应该简短、集中
DatabaseUtil11.executeInTransaction(conn -> {
    // 仅包含必要的数据库操作
    // 避免外部API调用
    // 避免复杂的业务逻辑
    return result;
});
```

### 4. 错误处理策略
```java
try {
    List<Map<String, Object>> results = DatabaseUtil11.selectList(sql, params);
    // 处理结果
} catch (RuntimeException e) {
    // 记录日志
    logger.error("数据库查询失败", e);
    // 根据业务需要决定是否重试或返回默认值
}
```

## 常见问题解决

### Q1: 连接池连接耗尽
**症状**：出现连接获取超时异常
**解决方案**：
- 检查是否有连接泄漏
- 适当增加最大连接数
- 优化查询性能减少连接持有时间

### Q2: 数据库连接失败
**症状**：无法建立数据库连接
**解决方案**：
- 检查数据库URL、用户名、密码
- 确认数据库服务状态
- 检查网络连接和防火墙设置

### Q3: 事务回滚频繁
**症状**：事务经常失败回滚
**解决方案**：
- 检查事务中的异常处理
- 优化事务边界设计
- 分析数据库锁竞争情况

### Q4: 性能问题
**症状**：数据库操作响应慢
**解决方案**：
- 优化SQL查询语句
- 合理设置连接池参数
- 使用批量操作处理大量数据

## 版本信息

- **当前版本**：2.0.0
- **依赖组件**：HikariCP 连接池、SnakeYAML
- **Java版本**：Java 8+
- **数据库支持**：MySQL 5.7+, PostgreSQL 10+, Oracle 11g+
- **更新时间**：2024年

## 兼容性说明

### 数据库驱动支持
- **MySQL**：`com.mysql.cj.jdbc.Driver`
- **PostgreSQL**：`org.postgresql.Driver`
- **Oracle**：`oracle.jdbc.driver.OracleDriver`
- **SQL Server**：`com.microsoft.sqlserver.jdbc.SQLServerDriver`

### Spring Boot 集成
DatabaseUtil11 可以很好地集成到 Spring Boot 项目中：
```java
@Configuration
public class DatabaseConfig {

    @PostConstruct
    public void initDatabases() {
        DatabaseUtil11.registerDatabaseFromYml("master");
        DatabaseUtil11.registerDatabaseFromYml("slave");
    }
}
```

---

*DatabaseUtil11 专注于提供高性能、易使用的多数据源管理解决方案。通过 HikariCP 的强大性能和精心设计的 API，为企业应用提供可靠的数据库访问基础设施。*
