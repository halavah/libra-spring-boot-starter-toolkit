# RedissonUtil.java 使用指南

本指南旨在通过实际场景和代码示例，帮助开发者快速掌握 `RedissonUtil` 工具类的核心用法。

## 📚 目录

1.  [核心概念与设计](#1-核心概念与设计)
    -   [1.1. 双引擎驱动：Lettuce 与 Redisson](#11-双引擎驱动lettuce-与-redisson)
    -   [1.2. 懒加载机制](#12-懒加载机制)
    -   [1.3. 两种调用模式：静态API vs 链式访问器](#13-两种调用模式静态api-vs-链式访问器)
2.  [基础配置与调试](#2-基础配置与调试)
    -   [2.1. 日志开关控制](#21-日志开关控制)
    -   [2.2. 数据源配置](#22-数据源配置)
3.  [数据源管理 (RedisDataCore)](#3-数据源管理-redisdatacore)
    -   [3.1. 动态注册数据源](#31-动态注册数据源)
    -   [3.2. 数据源上下文管理](#32-数据源上下文管理)
        -   [3.2.1. 设置默认数据源](#321-设置默认数据源)
        -   [3.2.2. 设置线程级数据源](#322-设置线程级数据源)
    -   [3.3. 场景示例：临时访问不同的Redis数据库](#33-场景示例临时访问不同的redis数据库)
4.  [链式访问器 (RedisAccessor)](#4-链式访问器-redisaccessor)
    -   [4.1. 获取访问器](#41-获取访问器)
    -   [4.2. 事务管理 (MULTI/EXEC)](#42-事务管理-multiexec)
    -   [4.3. 自动资源管理 (try-with-resources)](#43-自动资源管理-try-with-resources)
5.  [数据类型操作 (Ops)](#5-数据类型操作-ops)
    -   [5.1. Key (键操作)](#51-key-键操作)
    -   [5.2. String (字符串)](#52-string-字符串)
    -   [5.3. Hash (哈希)](#53-hash-哈希)
    -   [5.4. List (列表)](#54-list-列表)
    -   [5.5. Set (集合)](#55-set-集合)
    -   [5.6. ZSet (有序集合)](#56-zset-有序集合)
    -   [5.7. HyperLogLog (基数估算)](#57-hyperloglog-基数估算)
    -   [5.8. Geospatial (地理空间)](#58-geospatial-地理空间)
    -   [5.9. Streams (消息流)](#59-streams-消息流)
6.  [分布式锁与同步器](#6-分布式锁与同步器)
    -   [6.1. 可重入锁 (ReentrantLockOps)](#61-可重入锁-reentrantlockops)
    -   [6.2. 公平锁 (FairLockOps)](#62-公平锁-fairlockops)
    -   [6.3. 读写锁 (ReadWriteLockOps)](#63-读写锁-readwritelockops)
    -   [6.4. 信号量 (SemaphoreOps)](#64-信号量-semaphoreops)
    -   [6.5. 倒计时器 (CountDownLatchOps)](#65-倒计时器-countdownlatchops)
    -   [6.6. 布隆过滤器 (BloomFilterOps)](#66-布隆过滤器-bloomfilterops)
    -   [6.7. 场景示例：保护共享资源](#67-场景示例保护共享资源)
7.  [附录：Redisson 核心功能概览](#7-附录redisson-核心功能概览)
    -   [7.1. 分布式锁与同步器](#71-分布式锁与同步器)
    -   [7.2. 分布式集合](#72-分布式集合)
    -   [7.3. 分布式 Java 对象](#73-分布式-java-对象)
    -   [7.4. 分布式服务](#74-分布式服务)

---

## 1. 核心概念与设计

在深入了解如何使用 `RedissonUtil` 之前，理解其核心设计理念至关重要。这将帮助您更高效、更安全地在项目中使用它。

### 1.1. 双引擎驱动：Lettuce 与 Redisson

`RedissonUtil` 巧妙地集成了两个业界顶级的 Redis Java 客户端，并让它们各司其职：

-   **Lettuce**: 负责处理所有的**数据操作**（如 `GET`, `SET`, `HGETALL` 等）。Lettuce 以其卓越的性能和基于 Netty 的异步、响应式模型而闻名。在 `RedissonUtil` 中，我们通过一个高性能的连接池来管理 Lettuce 连接，确保高并发场景下的稳定与高效。

-   **Redisson**: 专门负责提供**分布式锁**等高级功能。Redisson 在分布式对象的实现上非常成熟，特别是其可重入锁（`RLock`）内置了“看门狗”（Watchdog）机制，能够自动延长锁的过期时间，有效防止了因业务逻辑执行时间过长而导致的锁失效问题，极大地提升了分布式锁的安全性。

通过这种“双引擎”设计，`RedissonUtil` 兼具了 Lettuce 的高性能和 Redisson 的功能完备性。

### 1.2. 懒加载机制

`RedissonUtil` 中所有的连接池（无论是 Lettuce 还是 Redisson）都采用了**懒加载（Lazy Loading）**模式。

-   **工作原理**: 工具类在启动时，仅仅是加载了数据源的配置信息，并不会立即创建任何到 Redis 的网络连接。只有当您的代码**第一次**尝试从某个数据源获取连接时（例如，调用 `RedissonUtil.StringOps.get("mykey")`），`RedissonUtil` 才会真正初始化对应数据源的连接池。

-   **核心优势**:
    -   **提升应用启动速度**: 避免了在应用启动阶段因连接多个 Redis 实例而造成的延迟。
    -   **节省资源**: 对于那些在某些场景下才会被用到的数据源，懒加载避免了不必要的资源占用。

### 1.3. 两种调用模式：静态API vs 链式访问器

`RedissonUtil` 提供了两种风格的API，以适应不同的开发需求。

-   **静态API (`RedissonUtil.StringOps`, `RedissonUtil.HashOps`, ...)**
    -   **特点**: 调用简单、代码简洁。
    -   **工作方式**: 每个静态方法内部都会自动从连接池获取一个连接，执行操作，然后立即将连接归还。
    -   **适用场景**:
        -   执行单个、独立的 Redis 命令。
        -   不涉及事务的简单操作。
    -   **示例**:
        ```java
        // 简单、直接
        String value = RedissonUtil.StringOps.get("user:101");
        RedissonUtil.HashOps.hPut("cart:101", "item:5", "2");
        ```

-   **链式访问器 (`RedissonUtil.accessor()`)**
    -   **特点**: 功能强大、支持事务、性能更高。
    -   **工作方式**: 通过 `try-with-resources` 语句创建一个 `RedisAccessor` 实例。在 `try` 块的整个生命周期内，它会**持有并复用同一个 Redis 连接**。
    -   **适用场景**:
        -   需要在一个事务中执行多个命令。
        -   需要连续执行多个操作，希望减少连接获取和释放的开销。
    -   **示例**:
        ```java
        // 功能强大，支持事务和连接复用
        try (RedisAccessor accessor = RedissonUtil.accessor("db1")) {
            accessor.multi(); // 开启事务
            accessor.StringOps.set("key1", "value1");
            accessor.HashOps.hPut("hash1", "field1", "value1");
            accessor.exec(); // 执行事务
        }
        ```

---

## 2. 基础配置与调试

### 2.1. 日志开关控制

`RedissonUtil` 内置了一个简单的日志系统，方便在调试时观察其内部行为。

-   **核心API**: `RedissonUtil.setDebugLogEnabled(boolean enabled)`
-   **功能**: 全局开启或关闭 `RedissonUtil` 的调试日志。
-   **建议**: 在开发和调试阶段开启此功能，在生产环境关闭以避免不必要的日志输出。

```java
// 在应用启动时或调试前开启日志
RedissonUtil.setDebugLogEnabled(true);

// ... 执行 Redis 操作 ...

// 在应用关闭时或调试后关闭
RedissonUtil.setDebugLogEnabled(false);
```

### 2.2. 数据源配置

`RedissonUtil` 通过一个二维静态字符串数组 `REDIS_MANAGE_CONNECTIONS` 来集中管理所有需要连接的 Redis 实例。

-   **配置位置**: `RedissonUtil.java` 文件内部。
-   **格式**: `{"唯一标识名", "主机地址", "端口", "密码", "数据库索引", "最大连接数", "最大空闲连接", "最小空闲连接"}`

**示例配置**:
```java
private static final String[][] REDIS_MANAGE_CONNECTIONS = {
    // 默认数据源 (db 0)
    {"default_redis", "127.0.0.1", "6379", "", "0", "8", "8", "0"},
    // 用于测试的数据源 (db 1)
    {"db1_for_test", "127.0.0.1", "6379", "", "1", "8", "8", "0"}
};
```

-   **`唯一标识名`**: 这是在代码中引用该数据源的 `key`，例如 `RedissonUtil.accessor("db1_for_test")`。
-   **`密码`**: 如果没有密码，请留空字符串 `""`。
-   **`数据库索引`**: Redis 的数据库编号，通常为 0-15。
-   **连接池参数**: `最大连接数`, `最大空闲连接`, `最小空闲连接` 用于配置 Lettuce 连接池的性能。

在应用启动时，`RedissonUtil` 会自动读取此配置并准备好所有数据源的元数据，等待被“懒加载”。

---

## 3. 数据源管理 (RedisDataCore)

`RedisDataCore` 是 `RedissonUtil` 的“心脏”，它不仅负责存储数据源的配置信息，还提供了一套强大的API，用于在运行时动态地注册、管理和切换数据源。

### 3.1. 动态注册数据源

虽然推荐使用静态数组 `REDIS_MANAGE_CONNECTIONS` 进行配置，但在某些场景下，您可能需要连接一个在程序启动时未知的 Redis 实例。`RedisDataCore.register(...)` 方法就是为此设计的。

-   **核心API**:
    -   `RedisDataCore.register(key, host, port, password, database)`: 使用默认连接池参数注册。
    -   `RedisDataCore.register(key, host, port, password, database, maxTotal, maxIdle, minIdle)`: 可自定义连接池参数。

-   **返回值**: 返回一个 `RedisDataCore` 实例，您可以继续在其上进行链式调用。

**示例：动态连接一个新的Redis实例**
```java
// 动态注册一个用于临时任务的Redis实例 (db 5)
RedisDataCore tempDataSource = RedissonUtil.RedisDataCore.register(
    "temp_task_redis",
    "10.0.1.100",
    6379,
    "temp_password",
    5
);

System.out.println("数据源 'temp_task_redis' 注册成功！");

// 可以直接在该实例上进行链式调用，获取一个访问器
try (RedisAccessor accessor = tempDataSource.accessor()) {
    // 使用这个临时数据源执行操作
    accessor.StringOps.set("task:status", "processing");
}
```

### 3.2. 数据源上下文管理

`RedissonUtil` 支持两种级别的数据源切换：**全局默认**和**线程级临时切换**。线程级设置的优先级高于全局默认。

#### 3.2.1. 设置默认数据源

-   **核心API**: `RedisDataCore.setDefaultKey(String key)`
-   **功能**: 改变整个应用的默认数据源。所有未明确指定数据源的静态API调用，以及未在线程上下文中指定数据源的 `accessor()` 调用，都将使用这个新的默认数据源。

```java
// 假设初始默认是 "default_redis"
System.out.println("当前默认数据源: " + RedissonUtil.RedisDataCore.getDefaultKey()); // 输出 "default_redis"

// 将默认数据源切换到 "db1_for_test"
RedissonUtil.RedisDataCore.setDefaultKey("db1_for_test");

System.out.println("新的默认数据源: " + RedissonUtil.RedisDataCore.getDefaultKey()); // 输出 "db1_for_test"

// 现在，所有静态调用都将自动在 db1 上执行
RedissonUtil.StringOps.set("new_default_test", "hello"); // 此操作在 db1 上执行
```

#### 3.2.2. 设置线程级数据源

这是处理多数据源场景最安全、最推荐的方式。它利用 `ThreadLocal` 来确保数据源的切换仅在当前线程生效，不会影响其他线程。

-   **核心API**:
    -   `LettuceCore.use(String key)`: **【推荐】** 将当前线程的数据源切换到指定的 `key`。
    -   `LettuceCore.clear()`: **【重要】** 清除当前线程的数据源设置，使其恢复使用全局默认数据源。**必须在 `finally` 块中调用此方法**，以防止线程池中的线程状态污染。

### 3.3. 场景示例：临时访问不同的Redis数据库

假设我们的主业务逻辑使用默认的 Redis 数据库（db 0），但在某个特定的业务流程中，需要从另一个专门用于缓存的 Redis 数据库（db 1，名为 `db1_for_test`）读取一些数据。

```java
// 1. 主业务逻辑，使用默认数据源 (default_redis, db 0)
System.out.println("主流程开始，当前使用数据源: " + RedissonUtil.LettuceCore.getEffectiveDbKey());
RedissonUtil.StringOps.set("main:user:1", "user_data_in_db0");

// 2. 进入需要访问缓存的业务流程
try {
    // 切换当前线程的数据源到 "db1_for_test"
    RedissonUtil.LettuceCore.use("db1_for_test");
    System.out.println("进入缓存流程，当前使用数据源: " + RedissonUtil.LettuceCore.getEffectiveDbKey());

    // 此时，所有操作都将自动在 db1 上执行
    String cachedValue = RedissonUtil.StringOps.get("cache:config");
    System.out.println("从 db1 读取到缓存: " + cachedValue);

    // 也可以通过 accessor 在 db1 上执行更复杂的操作
    try (RedisAccessor cacheAccessor = RedissonUtil.accessor()) { // 无需指定key，自动使用线程上下文
        cacheAccessor.HashOps.hPut("cache:session:xyz", "last_access", String.valueOf(System.currentTimeMillis()));
    }

} finally {
    // 3. 业务流程结束，必须在 finally 块中清除线程上下文
    RedissonUtil.LettuceCore.clear();
    System.out.println("缓存流程结束，清除线程上下文。");
}

// 4. 回到主业务逻辑，操作自动恢复到默认数据源
System.out.println("主流程继续，当前使用数据源: " + RedissonUtil.LettuceCore.getEffectiveDbKey());
String mainValue = RedissonUtil.StringOps.get("main:user:1"); // 此操作在 db0 上执行
System.out.println("从 db0 读取到主数据: " + mainValue);
```

---

## 4. 链式访问器 (RedisAccessor)

`RedisAccessor` 是 `RedissonUtil` 的高级用法核心，它提供了事务支持和连接复用能力，是处理复杂业务逻辑的首选。

### 4.1. 获取访问器

-   `RedissonUtil.accessor()`: 获取一个操作**默认数据源**的访问器。
-   `RedissonUtil.accessor(String dbKey)`: 获取一个操作**指定数据源**的访问器。

### 4.2. 事务管理 (MULTI/EXEC)

`RedisAccessor` 提供了对 Redis 事务的完整支持，可以确保一组命令的原子性执行。

#### 4.2.1. 核心API

-   `.multi()`: 开启一个新事务。此后的所有命令都会被放入队列，而不是立即执行。
-   `.exec()`: 执行队列中的所有命令，并返回一个包含所有命令执行结果的 `List<Object>`。
-   `.discard()`: 放弃事务，清空命令队列。

#### 4.2.2. 场景示例：原子性更新多个键

假设我们需要在一个原子操作中，同时更新用户信息和其对应的统计数据。

```java
// 获取默认数据源的访问器
try (RedisAccessor accessor = RedissonUtil.accessor()) {
    try {
        // 1. 开启事务
        accessor.multi();

        // 2. 将多个命令加入队列
        accessor.StringOps.set("user:101:profile", "{\"name\":\"Alice\",\"email\":\"alice@example.com\"}");
        accessor.HashOps.hIncrBy("user:101:stats", "logins", 1);
        accessor.ListOps.lLeftPush("user:101:actions", "updated profile");

        // 3. 执行事务
        List<Object> results = accessor.exec();

        System.out.println("事务执行成功！");
        // results 列表将包含每个命令的执行结果，例如: ["OK", 1, 1]
        System.out.println("事务结果: " + results);

    } catch (Exception e) {
        // 如果在准备或执行事务时发生异常，可以手动放弃
        accessor.discard();
        System.err.println("事务执行失败，已放弃: " + e.getMessage());
    }
}
```

### 4.3. 自动资源管理 (try-with-resources)

`RedisAccessor` 实现了 `java.lang.AutoCloseable` 接口，这意味着它可以、也**应该**与 `try-with-resources` 语句一同使用。

-   **工作原理**: 当 `try` 块执行完毕（无论是正常结束还是因异常退出），`RedisAccessor` 的 `.close()` 方法会被自动调用。
-   **核心优势**:
    -   **确保连接归还**: `.close()` 方法会负责将持有的 Redis 连接安全地归还到连接池，有效避免了连接泄漏。
    -   **自动放弃事务**: 如果在 `try` 块中开启了事务但没有显式调用 `.exec()` 或 `.discard()`，`.close()` 方法会自动调用 `.discard()` 来放弃未完成的事务，防止连接状态污染。
    -   **代码更简洁**: 无需手动编写 `finally` 块来归还连接。

**正确用法**:
```java
// 正确：使用 try-with-resources
try (RedisAccessor accessor = RedissonUtil.accessor()) {
    accessor.StringOps.set("mykey", "myvalue");
    // try块结束时，连接会自动归还
}
```

**错误用法**:
```java
// 错误：忘记关闭，可能导致连接泄漏
RedisAccessor accessor = RedissonUtil.accessor();
accessor.StringOps.set("mykey", "myvalue");
// accessor.close(); // 如果忘记这行，连接将不会被归还
```

---

## 5. 数据类型操作 (Ops)

`RedissonUtil` 将所有 Redis 命令按照数据类型分装在不同的 `Ops` 内部类中，方便查找和使用。本章节将列出常用命令，更多API请直接查阅 `RedissonUtil.java` 源码。

### 5.1. Key (键操作)

-   `KeyOps.delete(key)`: 删除一个键。
-   `KeyOps.hasKey(key)`: 检查键是否存在。
-   `KeyOps.expire(key, timeout, unit)`: 设置键的过期时间。
-   `KeyOps.getExpire(key)`: 获取键的剩余过期时间（秒）。
-   `KeyOps.keys(pattern)`: 查找匹配的键（**注意：在生产环境慎用，可能阻塞Redis**）。
-   `KeyOps.type(key)`: 获取键存储的值的类型。

```java
// 设置一个带过期时间的键
RedissonUtil.KeyOps.expire("session:xyz", 30, TimeUnit.MINUTES);

// 检查键是否存在
if (RedissonUtil.KeyOps.hasKey("session:xyz")) {
    System.out.println("Session key exists.");
}
```

### 5.2. String (字符串)

-   `StringOps.get(key)`: 获取值。
-   `StringOps.set(key, value)`: 设置值。
-   `StringOps.setEx(key, value, timeout, unit)`: 设置带过期时间的值。
-   `StringOps.setIfAbsent(key, value)`: 如果键不存在，则设置值（`SETNX`）。
-   `StringOps.incrBy(key, increment)`: 将键的值增加指定的整数。
-   `StringOps.decrBy(key, decrement)`: 将键的值减少指定的整数。

```java
// 用作分布式计数器
long currentCount = RedissonUtil.StringOps.incrBy("page:views:article:123", 1);
System.out.println("文章123的浏览量: " + currentCount);
```

### 5.3. Hash (哈希)

-   `HashOps.hGet(key, field)`: 获取哈希中指定字段的值。
-   `HashOps.hGetAll(key)`: 获取哈希中所有的字段和值。
-   `HashOps.hPut(key, field, value)`: 设置哈希中指定字段的值。
-   `HashOps.hPutAll(key, map)`: 一次性设置多个字段。
-   `HashOps.hDelete(key, ...fields)`: 删除一个或多个字段。
-   `HashOps.hExists(key, field)`: 检查字段是否存在。
-   `HashOps.hIncrBy(key, field, increment)`: 将字段的值增加指定的整数。
-   `HashOps.hKeys(key)`: 获取所有字段名。
-   `HashOps.hValues(key)`: 获取所有值。

```java
// 存储用户信息
Map<String, String> userInfo = new HashMap<>();
userInfo.put("name", "Bob");
userInfo.put("age", "30");
userInfo.put("city", "New York");
RedissonUtil.HashOps.hPutAll("user:102", userInfo);

// 获取并更新
String name = RedissonUtil.HashOps.hGet("user:102", "name");
RedissonUtil.HashOps.hIncrBy("user:102", "login_count", 1);
```

### 5.4. List (列表)

-   `ListOps.lLeftPush(key, value)`: 从列表左侧插入一个值。
-   `ListOps.lRightPush(key, value)`: 从列表右侧插入一个值。
-   `ListOps.lLeftPop(key)`: 从列表左侧弹出一个值。
-   `ListOps.lRightPop(key)`: 从列表右侧弹出一个值。
-   `ListOps.lRange(key, start, end)`: 获取列表指定范围内的所有值。
-   `ListOps.lLen(key)`: 获取列表的长度。
-   `ListOps.lRemove(key, count, value)`: 从列表中移除指定数量的匹配值。

```java
// 用作消息队列
RedissonUtil.ListOps.lLeftPush("tasks", "task1");
RedissonUtil.ListOps.lLeftPush("tasks", "task2");

// 消费者处理任务
String task = RedissonUtil.ListOps.lRightPop("tasks");
while (task != null) {
    System.out.println("正在处理任务: " + task);
    task = RedissonUtil.ListOps.lRightPop("tasks");
}
```

### 5.5. Set (集合)

-   `SetOps.sAdd(key, ...values)`: 向集合中添加一个或多个成员。
-   `SetOps.sMembers(key)`: 获取集合中的所有成员。
-   `SetOps.sIsMember(key, value)`: 判断成员是否存在于集合中。
-   `SetOps.sRemove(key, ...values)`: 移除一个或多个成员。
-   `SetOps.sSize(key)`: 获取集合的成员数量。
-   `SetOps.sIntersect(key1, key2)`: 获取两个集合的交集。
-   `SetOps.sUnion(key1, key2)`: 获取两个集合的并集。
-   `SetOps.sDifference(key1, key2)`: 获取两个集合的差集。

```java
// 用作标签系统
RedissonUtil.SetOps.sAdd("post:1:tags", "java", "redis", "guide");
RedissonUtil.SetOps.sAdd("post:2:tags", "redis", "performance");

// 查找同时拥有 'redis' 和 'guide' 标签的文章
Set<String> commonTags = RedissonUtil.SetOps.sIntersect("post:1:tags", "post:2:tags"); // 结果是 {"redis"}
```

### 5.6. ZSet (有序集合)

-   `ZSetOps.zAdd(key, value, score)`: 向有序集合中添加一个成员，并指定其分数。
-   `ZSetOps.zRange(key, start, end)`: 按分数从低到高，返回指定排名范围的成员。
-   `ZSetOps.zReverseRange(key, start, end)`: 按分数从高到低，返回指定排名范围的成员。
-   `ZSetOps.zRangeWithScores(key, start, end)`: 返回成员及其分数。
-   `ZSetOps.zScore(key, value)`: 获取指定成员的分数。
-   `ZSetOps.zRank(key, value)`: 获取指定成员的排名（从低到高）。
-   `ZSetOps.zIncrementScore(key, value, increment)`: 为成员的分数增加指定值。
-   `ZSetOps.zRemove(key, ...values)`: 移除一个或多个成员。

```java
// 用作排行榜
RedissonUtil.ZSetOps.zAdd("leaderboard", "player1", 1500);
RedissonUtil.ZSetOps.zAdd("leaderboard", "player2", 2200);
RedissonUtil.ZSetOps.zAdd("leaderboard", "player3", 1800);

// 玩家2获得100分
RedissonUtil.ZSetOps.zIncrementScore("leaderboard", "player2", 100);

// 获取排行榜前3名
Set<String> top3 = RedissonUtil.ZSetOps.zReverseRange("leaderboard", 0, 2);
System.out.println("排行榜 Top 3: " + top3); // 输出: [player2, player3, player1]
```

### 5.7. HyperLogLog (基数估算)

HyperLogLog 是一种概率性数据结构，专门用于估算大型数据集的基数（不重复元素的数量）。它能够在使用极少内存的情况下，处理数百万甚至数十亿的元素。

#### 核心 API

-   `HyperLogLogOps.pfAdd(key, elements...)`: 向 HyperLogLog 添加一个或多个元素
-   `HyperLogLogOps.pfCount(keys...)`: 返回一个或多个 HyperLogLog 的基数估算值
-   `HyperLogLogOps.pfMerge(destKey, sourceKeys...)`: 将多个 HyperLogLog 合并为一个

#### 场景示例：网站独立访客统计 (UV)

```java
// 记录用户访问
RedissonUtil.HyperLogLogOps.pfAdd("page:home:uv", "user1", "user2", "user3");
RedissonUtil.HyperLogLogOps.pfAdd("page:about:uv", "user2", "user3", "user4");
RedissonUtil.HyperLogLogOps.pfAdd("page:contact:uv", "user1", "user4", "user5");

// 统计各页面独立访客数
long homeUV = RedissonUtil.HyperLogLogOps.pfCount("page:home:uv");
long aboutUV = RedissonUtil.HyperLogLogOps.pfCount("page:about:uv");
System.out.println("首页 UV: " + homeUV + ", 关于页 UV: " + aboutUV);

// 统计网站总的独立访客数（自动去重）
long totalUV = RedissonUtil.HyperLogLogOps.pfCount("page:home:uv", "page:about:uv", "page:contact:uv");
System.out.println("网站总 UV: " + totalUV); // 结果约为 5（去重后）

// 合并多个页面的访客数据到总计中
RedissonUtil.HyperLogLogOps.pfMerge("site:total:uv", "page:home:uv", "page:about:uv", "page:contact:uv");
```

### 5.8. Geospatial (地理空间)

Redis 地理空间功能允许存储地理位置信息并进行各种地理空间计算，广泛应用于 LBS（基于位置的服务）场景。

#### 核心 API

-   `GeoOps.geoAdd(key, longitude, latitude, member)`: 添加地理位置
-   `GeoOps.geoPos(key, members...)`: 获取成员的坐标
-   `GeoOps.geoDist(key, member1, member2, unit)`: 计算两点间距离
-   `GeoOps.geoRadius(key, longitude, latitude, radius, unit)`: 基于坐标的半径查询
-   `GeoOps.geoRadiusByMember(key, member, radius, unit)`: 基于成员的半径查询

#### 场景示例：附近商家查找

```java
// 添加商家位置（经度, 纬度, 商家ID）
RedissonUtil.GeoOps.geoAdd("shops", 116.397128, 39.916527, "shop:beijing");
RedissonUtil.GeoOps.geoAdd("shops", 121.473701, 31.230416, "shop:shanghai");
RedissonUtil.GeoOps.geoAdd("shops", 113.264385, 23.129163, "shop:guangzhou");
RedissonUtil.GeoOps.geoAdd("shops", 116.407526, 39.904030, "shop:tiananmen");

// 计算两个商家间的距离
Double distance = RedissonUtil.GeoOps.geoDist("shops", "shop:beijing", "shop:shanghai", GeoUnit.KILOMETERS);
System.out.println("北京到上海距离: " + distance + " 公里");

// 查找用户位置附近 50 公里内的商家
List<Object> nearbyShops = RedissonUtil.GeoOps.geoRadius("shops", 116.4, 39.9, 50, GeoUnit.KILOMETERS, 10);
System.out.println("附近商家: " + nearbyShops);

// 查找指定商家附近的其他商家
List<Object> nearbyBeijing = RedissonUtil.GeoOps.geoRadiusByMember("shops", "shop:beijing", 100, GeoUnit.KILOMETERS, 5);
System.out.println("北京商家附近: " + nearbyBeijing);
```

### 5.9. Streams (消息流)

Redis Streams 是一个高性能的消息队列和日志数据结构，支持消息持久化、消费者组、消息确认等高级功能。

#### 核心 API

-   `StreamOps.add(key, field, value)`: 添加消息到流
-   `StreamOps.addAll(key, fields)`: 添加多字段消息
-   `StreamOps.read(key, startId)`: 读取消息
-   `StreamOps.createGroup(key, groupName)`: 创建消费者组
-   `StreamOps.readGroup(key, groupName, consumerName)`: 消费者组读取
-   `StreamOps.ack(key, groupName, ids...)`: 确认消息处理完成

#### 场景示例：用户行为事件流处理

```java
// 添加用户行为事件
StreamMessageId loginEvent = RedissonUtil.StreamOps.add("user_events", "action", "login");
StreamMessageId purchaseEvent = RedissonUtil.StreamOps.addAll("user_events",
    Map.of("action", "purchase", "product", "laptop", "amount", "5999", "userId", "12345"));

// 创建分析消费者组
RedissonUtil.StreamOps.createGroup("user_events", "analytics_group");

// 消费者读取事件进行分析
try (RedisAccessor accessor = RedissonUtil.accessor()) {
    Map<StreamMessageId, Map<String, Object>> events =
        accessor.StreamOps.readGroup("user_events", "analytics_group", "analyzer1", 10);

    for (Map.Entry<StreamMessageId, Map<String, Object>> entry : events.entrySet()) {
        StreamMessageId msgId = entry.getKey();
        Map<String, Object> eventData = entry.getValue();

        // 处理事件数据
        String action = (String) eventData.get("action");
        System.out.println("处理事件: " + action + " - " + eventData);

        // 确认消息已处理
        accessor.StreamOps.ack("user_events", "analytics_group", msgId);
    }
}

// 查看流的长度
long eventCount = RedissonUtil.StreamOps.length("user_events");
System.out.println("事件总数: " + eventCount);

// 修剪旧事件，保留最新1000条
RedissonUtil.StreamOps.trim("user_events", 1000);
```

---

## 6. 分布式锁与同步器

`RedissonUtil` 通过集成 Redisson，提供了完整的分布式锁与同步器功能，支持多种锁类型以满足不同的并发控制需求。

### 6.1. 可重入锁 (ReentrantLockOps)

可重入锁是最基础的分布式锁类型，支持同一线程多次获取同一把锁。

#### 核心API

-   `lock(key)`: 获取一个阻塞式、可重入的锁。如果锁已被其他线程持有，当前线程将一直等待，直到获取到锁。
-   `lock(key, leaseTime, unit)`: 获取一个带租约时间（Lease Time）的锁。锁会在指定时间后自动释放，这可以防止因服务宕机而导致的死锁。**注意**：此模式下 Redisson 的“看门狗”机制将**不会**生效。
-   `tryLock(key)`: 尝试非阻塞地获取锁。立即返回 `true`（获取成功）或 `false`（获取失败），不会等待。
-   `tryLock(key, waitTime, leaseTime, unit)`: 在指定的时间内（`waitTime`）尝试获取锁。如果在等待时间内获取成功，则返回 `true`，否则返回 `false`。获取到的锁将在 `leaseTime` 后自动释放。
-   `unlock(key)`: 释放锁。**必须在 `finally` 块中调用**，以确保即使在业务逻辑发生异常时，锁也能被正确释放。

#### 使用示例

```java
// 静态 API 使用
boolean acquired = RedissonUtil.ReentrantLockOps.tryLock("order:lock", 5, 30, TimeUnit.SECONDS);
if (acquired) {
    try {
        // 执行业务逻辑
    } finally {
        RedissonUtil.ReentrantLockOps.unlock("order:lock");
    }
}

// 链式 API 使用
try (RedissonUtil.RedisAccessor accessor = RedissonUtil.accessor()) {
    if (accessor.ReentrantLockOps.tryLock("order:lock")) {
        try {
            // 执行业务逻辑
        } finally {
            accessor.ReentrantLockOps.unlock("order:lock");
        }
    }
}
```

### 6.2. 公平锁 (FairLockOps)

公平锁保证等待时间最长的线程优先获取锁，避免线程饥饿问题。

#### 核心API

-   `lock(key)`: 获取一个阻塞式公平锁
-   `lock(key, leaseTime, unit)`: 获取一个带租约时间的公平锁
-   `tryLock(key)`: 尝试非阻塞地获取公平锁
-   `tryLock(key, waitTime, leaseTime, unit)`: 在指定时间内尝试获取公平锁
-   `unlock(key)`: 释放公平锁

#### 使用示例

```java
// 确保按照请求顺序获取锁
boolean acquired = RedissonUtil.FairLockOps.tryLock("fair:lock", 10, 60, TimeUnit.SECONDS);
if (acquired) {
    try {
        // 执行需要公平性保证的业务逻辑
    } finally {
        RedissonUtil.FairLockOps.unlock("fair:lock");
    }
}
```

### 6.3. 读写锁 (ReadWriteLockOps)

读写锁允许多个读操作并发执行，但写操作与任何其他操作互斥。

#### 核心API

**读锁操作**：
-   `readLock(key)`: 获取阻塞式读锁
-   `readLock(key, leaseTime, unit)`: 获取带租约时间的读锁
-   `tryReadLock(key)`: 尝试非阻塞地获取读锁
-   `tryReadLock(key, waitTime, leaseTime, unit)`: 在指定时间内尝试获取读锁
-   `unlockRead(key)`: 释放读锁

**写锁操作**：
-   `writeLock(key)`: 获取阻塞式写锁
-   `writeLock(key, leaseTime, unit)`: 获取带租约时间的写锁
-   `tryWriteLock(key)`: 尝试非阻塞地获取写锁
-   `tryWriteLock(key, waitTime, leaseTime, unit)`: 在指定时间内尝试获取写锁
-   `unlockWrite(key)`: 释放写锁

#### 使用示例

```java
// 读操作
boolean readAcquired = RedissonUtil.ReadWriteLockOps.tryReadLock("data:lock", 5, 30, TimeUnit.SECONDS);
if (readAcquired) {
    try {
        // 执行读操作，可以与其他读操作并发
        String data = RedissonUtil.StringOps.get("shared:data");
    } finally {
        RedissonUtil.ReadWriteLockOps.unlockRead("data:lock");
    }
}

// 写操作
boolean writeAcquired = RedissonUtil.ReadWriteLockOps.tryWriteLock("data:lock", 5, 30, TimeUnit.SECONDS);
if (writeAcquired) {
    try {
        // 执行写操作，与所有其他操作互斥
        RedissonUtil.StringOps.set("shared:data", "new_value");
    } finally {
        RedissonUtil.ReadWriteLockOps.unlockWrite("data:lock");
    }
}
```

### 6.4. 信号量 (SemaphoreOps)

信号量用于控制同时访问某个资源的线程数量，实现资源池的并发控制。

#### 核心API

-   `trySetPermits(key, permits)`: 设置信号量的许可证数量
-   `acquire(key)`: 获取一个许可证（阻塞式）
-   `acquire(key, permits)`: 获取多个许可证（阻塞式）
-   `tryAcquire(key)`: 尝试获取一个许可证（非阻塞）
-   `tryAcquire(key, permits)`: 尝试获取多个许可证（非阻塞）
-   `tryAcquire(key, timeout, unit)`: 在指定时间内尝试获取许可证
-   `release(key)`: 释放一个许可证
-   `release(key, permits)`: 释放多个许可证

#### 使用示例

```java
// 设置资源池大小为 5
RedissonUtil.SemaphoreOps.trySetPermits("connection:pool", 5);

// 获取资源
boolean acquired = RedissonUtil.SemaphoreOps.tryAcquire("connection:pool", 2, 10, TimeUnit.SECONDS);
if (acquired) {
    try {
        // 使用资源（最多5个线程可以同时执行）
        System.out.println("使用连接池资源...");
    } finally {
        RedissonUtil.SemaphoreOps.release("connection:pool");
    }
}
```

### 6.5. 倒计时器 (CountDownLatchOps)

倒计时器用于多线程或多服务间的同步，等待多个任务完成后再继续执行。

#### 核心API

-   `trySetCount(key, count)`: 设置倒计时器的初始计数
-   `await(key)`: 等待计数器归零（阻塞式）
-   `await(key, timeout, unit)`: 在指定时间内等待计数器归零
-   `countDown(key)`: 将计数器减1
-   `getCount(key)`: 获取当前计数值

#### 使用示例

```java
// 设置等待3个任务完成
RedissonUtil.CountDownLatchOps.trySetCount("startup:latch", 3);

// 工作线程完成任务后递减计数
RedissonUtil.CountDownLatchOps.countDown("startup:latch");

// 主线程等待所有任务完成
boolean completed = RedissonUtil.CountDownLatchOps.await("startup:latch", 30, TimeUnit.SECONDS);
if (completed) {
    System.out.println("所有任务已完成，继续执行...");
}
```

### 6.6. 布隆过滤器 (BloomFilterOps)

布隆过滤器是一种概率性数据结构，用于高效判断元素是否可能存在于集合中。

#### 核心API

-   `tryInit(key, expectedElements, falsePositiveProbability)`: 初始化布隆过滤器
-   `add(key, element)`: 添加元素到布隆过滤器
-   `addAll(key, elements)`: 批量添加元素
-   `contains(key, element)`: 检查元素是否可能存在
-   `count(key)`: 获取已添加的元素数量（估算值）
-   `getExpectedElements(key)`: 获取预期元素数量
-   `getFalsePositiveProbability(key)`: 获取误判率

#### 使用示例

```java
// 初始化布隆过滤器，预期100万元素，误判率0.01
RedissonUtil.BloomFilterOps.tryInit("user:visited", 1000000, 0.01);

// 添加用户访问记录
RedissonUtil.BloomFilterOps.add("user:visited", "user:12345");

// 检查用户是否可能访问过
boolean maybeVisited = RedissonUtil.BloomFilterOps.contains("user:visited", "user:12345");
if (maybeVisited) {
    // 可能访问过，需要进一步确认
    // 如果返回false，则一定没有访问过
}
```

### 6.7. 场景示例：保护共享资源

假设在一个电商应用中，当用户下单时，我们需要从库存中扣减商品数量。这是一个典型的需要锁来保护的“临界区”，以防止因高并发而导致的“超卖”问题。

```java
// 商品ID
String productId = "product:123";
// 锁的key
String lockKey = "lock:inventory:" + productId;

// 尝试在10秒内获取锁，如果成功，锁的有效期为60秒
boolean isLocked = false;
try {
    // 1. 尝试获取可重入锁
    isLocked = RedissonUtil.ReentrantLockOps.tryLock(lockKey, 10, 60, TimeUnit.SECONDS);

    if (isLocked) {
        System.out.println(Thread.currentThread().getName() + " 成功获取到锁！");

        // 2. 执行核心业务逻辑（临界区）
        // 从数据库或Redis中读取当前库存
        int currentStock = Integer.parseInt(RedissonUtil.StringOps.get("inventory:" + productId));

        if (currentStock > 0) {
            // 模拟业务处理耗时
            Thread.sleep(100);
            // 库存充足，执行扣减
            RedissonUtil.StringOps.incrBy("inventory:" + productId, -1);
            System.out.println(Thread.currentThread().getName() + " 库存扣减成功！");
            // 创建订单等后续操作...
        } else {
            System.out.println(Thread.currentThread().getName() + " 库存不足，无法下单。");
        }

    } else {
        System.out.println(Thread.currentThread().getName() + " 未能获取到锁，可能当前有其他操作正在进行。");
    }

} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    System.err.println("线程在等待锁期间被中断。");
} catch (Exception e) {
    System.err.println("业务逻辑发生异常: " + e.getMessage());
} finally {
    // 3. 无论业务是否成功，只要获取到了锁，就必须在 finally 块中释放它
    if (isLocked) {
        RedissonUtil.ReentrantLockOps.unlock(lockKey);
        System.out.println(Thread.currentThread().getName() + " 锁已释放。");
    }
}
```

---

## 7. 附录：Redisson 核心功能概览

本章节旨在提供 Redisson 框架本身核心功能的概览，以帮助开发者理解 `RedissonUtil` 中部分高级功能的底层实现和扩展可能性。

### 7.1. 分布式锁与同步器

这是 Redisson 最负盛名的功能，提供了比原生 `SETNX` 更强大、更安全（例如，通过看门狗机制自动续期）的锁实现。

- **核心对象**:
  - `RLock`: 可重入锁，功能最常用。
  - `RFairLock`: 公平锁，保证等待最久的线程先获取锁。
  - `RReadWriteLock`: 读写锁，允许多个读线程，但只允许一个写线程。
  - `RSemaphore`: 分布式信号量，用于控制并发访问资源的数量。
  - `RCountDownLatch`: 分布式倒计时器，用于多线程/多服务间的同步。

### 7.2. 分布式集合

它将 Redis 的数据结构（如 Hashes, Lists, Sets）封装成了与 `java.util.Collection` 框架完全兼容的接口。

- **核心对象**:
  - `RMap<K, V>`: 对应 Redis 的 Hash，实现了 `java.util.concurrent.ConcurrentMap` 接口。
  - `RSet<V>`: 对应 Redis 的 Set，实现了 `java.util.Set` 接口。
  - `RList<V>`: 对应 Redis 的 List，实现了 `java.util.List` 接口。
  - `RQueue<V>`: 对应 Redis 的 Queue，实现了 `java.util.Queue` 接口。
  - `RScoredSortedSet<V>`: 对应 Redis 的 ZSet (Sorted Set)，用于排序和排名场景。

### 7.3. 分布式 Java 对象

将一些常用的 Java 对象或数据结构进行了分布式实现。

- **核心对象**:
  - `RBucket<V>`: 通用对象桶，用于存储任何类型的单个对象（序列化后），是最基础的 `GET/SET` 操作的封装。
  - `RAtomicLong`: 分布式原子长整型，用于实现分布式计数器等。
  - `RBloomFilter<V>`: 分布式布隆过滤器，用于高效判断“一个元素一定不存在，或者可能存在”。
  - `RHyperLogLog<V>`: 用于基数估计算法。

### 7.4. 分布式服务

提供了更高级的分布式协调能力。

- **核心对象**:
  - `RTopic`: 实现了发布/订阅（Pub/Sub）模式，用于服务间的消息广播。
  - `RExecutorService`: 分布式执行服务，允许将任务提交到 Redis，由多个 Worker 节点执行。
```
