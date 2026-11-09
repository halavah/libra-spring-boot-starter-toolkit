## 版本说明

### DatabaseUtil 拷贝自 DatabaseUtil42.java

```text
1. 日志调试：LoggerCore.setDebugLogEnabled(true);
2. 数据源管理：SimpleDataSource，register() 返回 DatabaseAccessor 对象
3. 事务管理：setTransaction() / commit() / rollback() / result()
4. 分页查询：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
5. 批量操作：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
6. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
7. 支持MP标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
```

### DatabaseUtil11.java 原名 DatabaseUtil11 工具类

```text
1. 多数据源管理 - 支持同时连接多个数据库
2. 连接池管理 - 基于HikariCP的高性能连接池
3. SQL注入防护 - 使用预编译语句确保安全
4. 事务管理 - 支持编程式事务控制
5. 自动资源管理 - 自动关闭连接和释放资源
6. 配置模式 - 支持自定义配置和YAML配置文件
7. 线程安全 - 所有操作均为线程安全
```

### DatabaseUtil12.java 原名 DatabaseUtil12 工具类

```text
1. 支持多数据源配置和动态切换
2. HikariCP连接池，提供卓越的性能
3. XML文件SQL解析和执行
4. 完整的CRUD操作API
5. 事务管理和回调机制
6. 详细的中文注释和使用示例
```

### DatabaseUtil21.java - SimpleDataSource数据源

```text
1. 日志调试：LoggerCore.setDebugLogEnabled(true);
2. 数据源管理：SimpleDataSource，register() 返回 DatabaseAccessor 对象
3. 事务管理：setTransaction() / commit() / rollback() / result()
4. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
```

### DatabaseUtil22.java - DruidConnectionPool连接池

```text
1. 日志调试：LoggerCore.setDebugLogEnabled(true);
2. 数据源管理：DruidConnectionPool，register() 返回 DatabaseAccessor 对象
3. 事务管理：setTransaction() / commit() / rollback() / result()
4. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
```

### DatabaseUtil31.java - SimpleDataSource数据源（异步初始化） 不支持 YML，普通版本 + MP版本

```text
1. 日志调试：LoggerCore.setDebugLogEnabled(true);
2. 数据源管理：SimpleDataSource，register() 返回 DatabaseAccessor 对象
3. 事务管理：setTransaction() / commit() / rollback() / result()
4. 分页查询：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
5. 批量操作：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
6. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
7. 支持MP标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
```

### DatabaseUtil32.java - DruidConnectionPool连接池（同步阻塞初始化） 不支持 YML，普通版本 + MP版本

```text
1. 日志调试：LoggerCore.setDebugLogEnabled(true);
2. 数据源管理：DruidConnectionPool，register() 返回 DatabaseAccessor 对象
3. 事务管理：setTransaction() / commit() / rollback() / result()
4. 分页查询：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
5. 批量操作：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
6. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
7. 支持MP标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
```

### DatabaseUtil33.java - DruidConnectionPool连接池（同步阻塞初始化） 可以支持 YML，普通版本 + MP版本

```text
1. 日志调试：LoggerCore.setDebugLogEnabled(true);
2. 数据源管理：DruidConnectionPool，register() 返回 DatabaseAccessor 对象
3. 事务管理：setTransaction() / commit() / rollback() / result()
4. 分页查询：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
5. 批量操作：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
6. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
7. 支持MP标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
```

### DatabaseUtil41.java - SimpleDataSource数据源（懒加载初始化） 不支持 YML，普通版本(无) + MP版本 + 对象参数

```text
1. 日志调试：LoggerCore.setDebugLogEnabled(true);
2. 数据源管理：SimpleDataSource，register() 返回 DatabaseAccessor 对象
3. 事务管理：setTransaction() / commit() / rollback() / result()
4. 分页查询：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
5. 批量操作：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
6. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
7. 支持MP标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
```

### DatabaseUtil42.java - DruidConnectionPool连接池（显式注册与即时初始化） 不支持 YML，普通版本(无) + MP版本 + 对象参数

```text
1. 日志调试：LoggerCore.setDebugLogEnabled(true);
2. 数据源管理：DruidConnectionPool，register() 返回 DatabaseAccessor 对象
3. 事务管理：setTransaction() / commit() / rollback() / result()
4. 分页查询：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
5. 批量操作：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
6. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
7. 支持MP标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
```

### DatabaseUtil43.java - DruidConnectionPool连接池（显式注册与即时初始化） 可以支持 YML，普通版本(无) + MP版本 + 对象参数

```text
1. 日志调试：LoggerCore.setDebugLogEnabled(true);
2. 数据源管理：DruidConnectionPool，register() 返回 DatabaseAccessor 对象
3. 事务管理：setTransaction() / commit() / rollback() / result()
4. 分页查询：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
5. 批量操作：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
6. 支持普通标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
7. 支持MP标签：直接使用SQL / 指定具体XML / 自动搜索XML / 链式访问器
```
