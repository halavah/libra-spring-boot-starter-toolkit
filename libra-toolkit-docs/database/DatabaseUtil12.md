# DatabaseUtil12 智能XML SQL解析工具

## 概述

DatabaseUtil12 是一个高性能的 XML SQL 解析工具类（版本 2.1.0），专门设计用于解析和处理动态 SQL 配置文件。它提供了强大的模板引擎功能，支持条件判断、循环遍历、参数替换等高级特性，是构建灵活、可维护的数据访问层的核心组件。

## 核心特性

### 🎯 **智能XML解析**
- **文件自动搜索** - 在多个目录智能搜索 XML 配置文件
- **缓存机制** - 高效的解析结果缓存，避免重复解析
- **热重载** - 开发模式下支持 XML 文件热重载
- **语法验证** - 完整的 XML 格式和语法验证

### 🔧 **动态SQL构建**
- **条件判断** - 支持复杂的条件逻辑构建
- **循环遍历** - 批量操作和 IN 查询支持
- **参数替换** - 安全的参数化查询处理
- **模板继承** - SQL 片段复用和组合

### ⚡ **高性能特性**
- **编译优化** - SQL 模板预编译和优化
- **内存管理** - 智能的内存使用和垃圾回收
- **并发安全** - 完全线程安全的设计
- **快速解析** - 高效的 DOM 解析算法

### 🛡️ **安全保障**
- **SQL注入防护** - 严格的参数化处理
- **类型校验** - 参数类型自动检查和转换
- **XSS防护** - 输入内容安全过滤
- **权限控制** - SQL 操作权限验证

## 技术架构

```
DatabaseUtil12 解析引擎架构
├── XML解析层
│   ├── DocumentLoader (文档加载器)
│   ├── XmlValidator (XML验证器)
│   ├── NamespaceResolver (命名空间解析器)
│   └── ErrorHandler (错误处理器)
├── 模板引擎
│   ├── TemplateCompiler (模板编译器)
│   ├── ConditionEvaluator (条件评估器)
│   ├── LoopProcessor (循环处理器)
│   └── ParameterResolver (参数解析器)
├── SQL构建器
│   ├── SqlBuilder (SQL构建器)
│   ├── ParameterBinder (参数绑定器)
│   ├── TypeConverter (类型转换器)
│   └── SecurityValidator (安全验证器)
├── 缓存管理
│   ├── TemplateCache (模板缓存)
│   ├── ParseResultCache (解析结果缓存)
│   ├── CacheEviction (缓存清理)
│   └── CacheStatistics (缓存统计)
└── 性能监控
    ├── ParseTimeMonitor (解析时间监控)
    ├── CacheHitRateMonitor (缓存命中率监控)
    ├── MemoryUsageMonitor (内存使用监控)
    └── ConcurrencyMonitor (并发性能监控)
```

## 快速开始

### 1. 基础XML SQL配置

**创建 SQL 配置文件 user-queries.xml：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<mapper namespace="user-queries">

    <!-- 基础用户查询 -->
    <sql id="getUserById">
        SELECT
            id, name, email, phone, status,
            created_at, updated_at
        FROM users
        WHERE id = #{id}
    </sql>

    <!-- 条件查询用户列表 -->
    <sql id="getUserList">
        SELECT
            u.id, u.name, u.email, u.status,
            COUNT(o.id) as order_count
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        WHERE 1=1
        <if test="name != null and name != ''">
            AND u.name LIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="email != null and email != ''">
            AND u.email = #{email}
        </if>
        <if test="status != null and status != ''">
            AND u.status = #{status}
        </if>
        <if test="startDate != null">
            AND u.created_at >= #{startDate}
        </if>
        <if test="endDate != null">
            AND u.created_at <= #{endDate}
        </if>
        GROUP BY u.id
        <if test="orderBy != null and orderBy != ''">
            ORDER BY ${orderBy}
            <if test="orderDirection != null">
                ${orderDirection}
            </if>
        </if>
        <if test="limit != null and limit > 0">
            LIMIT #{limit}
        </if>
    </sql>

    <!-- 批量操作示例 -->
    <sql id="batchInsertUsers">
        INSERT INTO users (name, email, phone, status) VALUES
        <foreach item="user" collection="users" separator=",">
            (#{user.name}, #{user.email}, #{user.phone}, #{user.status})
        </foreach>
    </sql>

</mapper>
```

**Java 代码使用：**

```java
// 设置查询参数
Map<String, Object> params = new HashMap<>();
params.put("id", 123);

// 解析并执行SQL
String sql = DatabaseUtil12.parseSql("user-queries", "getUserById", params);
System.out.println("解析后的SQL: " + sql);
// 输出: SELECT id, name, email, phone, status, created_at, updated_at FROM users WHERE id = ?

// 获取参数化查询的参数列表
List<Object> parameters = DatabaseUtil12.getParameters("user-queries", "getUserById", params);
System.out.println("参数列表: " + parameters);
// 输出: [123]
```

### 2. 复杂条件查询

```java
// 设置复杂查询条件
Map<String, Object> searchParams = new HashMap<>();
searchParams.put("name", "张");
searchParams.put("status", "active");
searchParams.put("startDate", "2024-01-01");
searchParams.put("orderBy", "created_at");
searchParams.put("orderDirection", "DESC");
searchParams.put("limit", 20);

// 解析动态SQL
String dynamicSql = DatabaseUtil12.parseSql("user-queries", "getUserList", searchParams);
System.out.println("动态SQL: " + dynamicSql);

/* 输出示例:
SELECT
    u.id, u.name, u.email, u.status,
    COUNT(o.id) as order_count
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE 1=1
    AND u.name LIKE CONCAT('%', ?, '%')
    AND u.status = ?
    AND u.created_at >= ?
GROUP BY u.id
ORDER BY created_at DESC
LIMIT ?
*/

// 获取对应的参数
List<Object> params = DatabaseUtil12.getParameters("user-queries", "getUserList", searchParams);
// 参数: ["张", "active", "2024-01-01", 20]
```

### 3. 批量操作处理

```java
// 准备批量数据
List<Map<String, Object>> userList = new ArrayList<>();
for (int i = 1; i <= 100; i++) {
    Map<String, Object> user = new HashMap<>();
    user.put("name", "用户" + i);
    user.put("email", "user" + i + "@example.com");
    user.put("phone", "1380000" + String.format("%04d", i));
    user.put("status", "active");
    userList.add(user);
}

// 批量插入参数
Map<String, Object> batchParams = new HashMap<>();
batchParams.put("users", userList);

// 解析批量SQL
String batchSql = DatabaseUtil12.parseSql("user-queries", "batchInsertUsers", batchParams);
System.out.println("批量SQL: " + batchSql);

/* 输出示例:
INSERT INTO users (name, email, phone, status) VALUES
    (?, ?, ?, ?),
    (?, ?, ?, ?),
    ... (100个值组)
*/

// 获取所有参数（展平后的参数列表）
List<Object> flatParams = DatabaseUtil12.getParameters("user-queries", "batchInsertUsers", batchParams);
// 400个参数：每个用户4个字段 × 100个用户
```

### 4. 高级模板功能

**创建高级模板 advanced-sql.xml：**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<mapper namespace="advanced-sql">

    <!-- SQL片段定义（可复用） -->
    <sql id="userFields">
        u.id, u.name, u.email, u.phone, u.status,
        u.created_at, u.updated_at
    </sql>

    <sql id="userJoins">
        LEFT JOIN user_profiles p ON u.id = p.user_id
        LEFT JOIN user_settings s ON u.id = s.user_id
    </sql>

    <!-- 使用SQL片段的复杂查询 -->
    <sql id="getDetailedUserInfo">
        SELECT
            <include refid="userFields"/>,
            p.avatar, p.bio, p.location,
            s.theme, s.language, s.notifications
        FROM users u
        <include refid="userJoins"/>
        WHERE u.id = #{userId}
        <if test="includeInactive != null and includeInactive == false">
            AND u.status = 'active'
        </if>
    </sql>

    <!-- 嵌套条件查询 -->
    <sql id="searchUsersWithAdvancedFilters">
        SELECT
            <include refid="userFields"/>,
            COUNT(DISTINCT o.id) as order_count,
            COALESCE(SUM(o.amount), 0) as total_spent,
            MAX(o.created_at) as last_order_date
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        <where>
            <!-- 基础过滤条件 -->
            <if test="filters != null">
                <if test="filters.name != null and filters.name != ''">
                    AND u.name LIKE CONCAT('%', #{filters.name}, '%')
                </if>
                <if test="filters.email != null and filters.email != ''">
                    AND u.email LIKE CONCAT('%', #{filters.email}, '%')
                </if>
                <if test="filters.status != null and filters.status.size() > 0">
                    AND u.status IN (
                    <foreach item="status" collection="filters.status" separator=",">
                        #{status}
                    </foreach>
                    )
                </if>
            </if>

            <!-- 高级过滤条件 -->
            <if test="advanced != null">
                <if test="advanced.minOrderCount != null">
                    AND (SELECT COUNT(*) FROM orders WHERE user_id = u.id) >= #{advanced.minOrderCount}
                </if>
                <if test="advanced.minSpent != null">
                    AND (SELECT COALESCE(SUM(amount), 0) FROM orders WHERE user_id = u.id) >= #{advanced.minSpent}
                </if>
                <if test="advanced.registeredAfter != null">
                    AND u.created_at >= #{advanced.registeredAfter}
                </if>
                <if test="advanced.registeredBefore != null">
                    AND u.created_at <= #{advanced.registeredBefore}
                </if>
            </if>
        </where>
        GROUP BY u.id

        <!-- 结果过滤 -->
        <if test="having != null">
            HAVING 1=1
            <if test="having.minOrderCount != null">
                AND COUNT(DISTINCT o.id) >= #{having.minOrderCount}
            </if>
            <if test="having.minTotalSpent != null">
                AND COALESCE(SUM(o.amount), 0) >= #{having.minTotalSpent}
            </if>
        </if>

        <!-- 排序 -->
        <if test="sort != null and sort.field != null">
            ORDER BY
            <choose>
                <when test="sort.field == 'name'">u.name</when>
                <when test="sort.field == 'email'">u.email</when>
                <when test="sort.field == 'created_at'">u.created_at</when>
                <when test="sort.field == 'order_count'">order_count</when>
                <when test="sort.field == 'total_spent'">total_spent</when>
                <otherwise>u.id</otherwise>
            </choose>
            <if test="sort.direction != null">
                ${sort.direction}
            </if>
        </if>

        <!-- 分页 -->
        <if test="pagination != null">
            <if test="pagination.limit != null and pagination.limit > 0">
                LIMIT #{pagination.limit}
                <if test="pagination.offset != null and pagination.offset > 0">
                    OFFSET #{pagination.offset}
                </if>
            </if>
        </if>
    </sql>

</mapper>
```

**使用高级模板：**

```java
// 复杂查询参数构建
Map<String, Object> params = new HashMap<>();

// 基础过滤条件
Map<String, Object> filters = new HashMap<>();
filters.put("name", "张");
filters.put("status", Arrays.asList("active", "pending"));
params.put("filters", filters);

// 高级过滤条件
Map<String, Object> advanced = new HashMap<>();
advanced.put("minOrderCount", 5);
advanced.put("minSpent", 1000.00);
advanced.put("registeredAfter", "2023-01-01");
params.put("advanced", advanced);

// 聚合结果过滤
Map<String, Object> having = new HashMap<>();
having.put("minOrderCount", 3);
having.put("minTotalSpent", 500.00);
params.put("having", having);

// 排序设置
Map<String, Object> sort = new HashMap<>();
sort.put("field", "total_spent");
sort.put("direction", "DESC");
params.put("sort", sort);

// 分页设置
Map<String, Object> pagination = new HashMap<>();
pagination.put("limit", 50);
pagination.put("offset", 0);
params.put("pagination", pagination);

// 解析高级SQL
String advancedSql = DatabaseUtil12.parseSql("advanced-sql", "searchUsersWithAdvancedFilters", params);
List<Object> sqlParams = DatabaseUtil12.getParameters("advanced-sql", "searchUsersWithAdvancedFilters", params);

System.out.println("高级动态SQL: " + advancedSql);
System.out.println("SQL参数: " + sqlParams);
```

## API 详细参考

### 核心解析 API

| 方法签名 | 功能描述 | 返回类型 | 使用场景 |
|----------|----------|----------|----------|
| `parseSql(String file, String id, Map<String, Object> params)` | 解析XML中的SQL语句 | `String` | 动态SQL生成 |
| `getParameters(String file, String id, Map<String, Object> params)` | 获取参数化查询参数 | `List<Object>` | PreparedStatement参数绑定 |
| `parseAndBind(String file, String id, Map<String, Object> params)` | 解析SQL并绑定参数 | `SqlWithParams` | 一站式SQL处理 |
| `validateSql(String file, String id)` | 验证SQL语法正确性 | `ValidationResult` | 开发阶段验证 |

### 文件管理 API

| 方法签名 | 功能描述 | 返回类型 | 特点 |
|----------|----------|----------|------|
| `loadXmlFile(String fileName)` | 加载XML配置文件 | `Document` | 自动搜索多目录 |
| `reloadXmlFile(String fileName)` | 重新加载XML文件 | `boolean` | 热重载支持 |
| `isFileModified(String fileName)` | 检查文件是否修改 | `boolean` | 缓存失效检测 |
| `getAvailableNamespaces()` | 获取可用命名空间 | `Set<String>` | 配置文件发现 |
| `getSqlIds(String namespace)` | 获取命名空间下SQL ID | `Set<String>` | SQL清单查询 |

### 缓存管理 API

| 方法签名 | 功能描述 | 返回类型 | 说明 |
|----------|----------|----------|------|
| `clearCache()` | 清空所有缓存 | `void` | 缓存清理 |
| `clearCache(String namespace)` | 清空指定命名空间缓存 | `void` | 选择性清理 |
| `getCacheStatistics()` | 获取缓存统计信息 | `CacheStats` | 性能监控 |
| `enableCache(boolean enable)` | 启用/禁用缓存 | `void` | 缓存控制 |

### 调试和监控 API

| 方法签名 | 功能描述 | 返回类型 | 用途 |
|----------|----------|----------|------|
| `enableDebugMode(boolean debug)` | 启用调试模式 | `void` | 开发调试 |
| `getParseStatistics()` | 获取解析统计 | `ParseStats` | 性能分析 |
| `getLastParseTime(String namespace, String id)` | 获取上次解析时间 | `long` | 性能监控 |
| `exportConfiguration()` | 导出当前配置 | `ConfigExport` | 配置备份 |

## 配置和优化

### 解析器配置

```java
// 配置解析器选项
DatabaseUtil12.ConfigBuilder config = DatabaseUtil12.newConfig()
    .enableCache(true)                    // 启用缓存
    .setCacheSize(1000)                   // 设置缓存大小
    .setCacheExpireTime(3600000)          // 缓存过期时间（1小时）
    .enableFileWatcher(true)              // 启用文件监听
    .setFileWatchInterval(5000)           // 文件检查间隔（5秒）
    .enableStrictMode(false)              // 非严格模式（容错）
    .setMaxDepth(10)                      // 最大嵌套深度
    .enableSqlValidation(true)            // 启用SQL验证
    .setThreadPoolSize(5);                // 解析线程池大小

// 应用配置
DatabaseUtil12.applyConfig(config.build());
```

### 性能优化配置

```java
// 高性能配置
DatabaseUtil12.PerformanceConfig perfConfig = new DatabaseUtil12.PerformanceConfig()
    .setParseThreads(Runtime.getRuntime().availableProcessors())  // CPU核心数线程
    .setMaxConcurrentParse(50)                                    // 最大并发解析数
    .setPrecompileTemplates(true)                                 // 预编译模板
    .setLazyLoading(true)                                         // 延迟加载
    .setCompressionEnabled(true)                                  // 启用压缩缓存
    .setMemoryOptimization(true);                                 // 内存优化

DatabaseUtil12.setPerformanceConfig(perfConfig);
```

### 搜索路径配置

```java
// 自定义XML文件搜索路径
String[] customPaths = {
    "src/main/resources/sql/",           // 最高优先级
    "src/main/resources/mapper/",        // MyBatis兼容
    "config/sql/",                       // 配置目录
    "sql/templates/",                    // 模板目录
    "resources/database/"                // 数据库资源目录
};

DatabaseUtil12.setSearchPaths(customPaths);

// 添加额外搜索路径
DatabaseUtil12.addSearchPath("custom/sql/");

// 获取当前搜索路径
String[] currentPaths = DatabaseUtil12.getSearchPaths();
System.out.println("当前搜索路径: " + Arrays.toString(currentPaths));
```

## 高级功能特性

### 1. 条件表达式支持

```xml
<!-- 高级条件表达式 -->
<sql id="advancedConditions">
    SELECT * FROM products WHERE 1=1

    <!-- 数值范围条件 -->
    <if test="price != null">
        <if test="price.min != null and price.max != null">
            AND price BETWEEN #{price.min} AND #{price.max}
        </if>
        <if test="price.min != null and price.max == null">
            AND price >= #{price.min}
        </if>
        <if test="price.min == null and price.max != null">
            AND price <= #{price.max}
        </if>
    </if>

    <!-- 复杂逻辑条件 -->
    <if test="filters != null">
        <if test="filters.category != null and filters.category.size() > 0">
            AND category_id IN (
                <foreach item="cat" collection="filters.category" separator=",">
                    #{cat}
                </foreach>
            )
        </if>

        <!-- 多条件组合 -->
        <if test="(filters.featured != null and filters.featured == true) or (filters.recommended != null and filters.recommended == true)">
            AND (
                <if test="filters.featured != null and filters.featured == true">
                    featured = 1
                </if>
                <if test="filters.featured != null and filters.featured == true and filters.recommended != null and filters.recommended == true">
                    OR
                </if>
                <if test="filters.recommended != null and filters.recommended == true">
                    recommended = 1
                </if>
            )
        </if>
    </if>

    <!-- 动态排序支持 -->
    <if test="sort != null">
        ORDER BY
        <choose>
            <when test="sort.field == 'price'">
                price ${sort.direction}
            </when>
            <when test="sort.field == 'name'">
                name ${sort.direction}
            </when>
            <when test="sort.field == 'popularity'">
                view_count ${sort.direction}, rating ${sort.direction}
            </when>
            <otherwise>
                created_at DESC
            </otherwise>
        </choose>
    </if>
</sql>
```

### 2. 循环和批量处理

```xml
<!-- 高级循环处理 -->
<sql id="batchOperations">
    <!-- 批量插入带冲突处理 -->
    INSERT INTO user_tags (user_id, tag_id, created_at) VALUES
    <foreach item="tag" collection="userTags" separator="," open="(" close=")">
        #{tag.userId}, #{tag.tagId}, NOW()
    </foreach>
    ON DUPLICATE KEY UPDATE updated_at = NOW()

    <!-- 动态WHERE IN条件 -->
    <if test="userIds != null and userIds.size() > 0">
        AND user_id IN (
        <foreach item="userId" collection="userIds" separator=",">
            #{userId}
        </foreach>
        )
    </if>

    <!-- 批量更新（CASE WHEN结构） -->
    UPDATE users SET
        status = CASE id
        <foreach item="update" collection="statusUpdates">
            WHEN #{update.userId} THEN #{update.status}
        </foreach>
        ELSE status
        END,
        updated_at = NOW()
    WHERE id IN (
        <foreach item="update" collection="statusUpdates" separator=",">
            #{update.userId}
        </foreach>
    )
</sql>
```

### 3. SQL片段和模板继承

```xml
<!-- 基础模板定义 -->
<sql id="baseUserFields">
    u.id, u.name, u.email, u.phone, u.status, u.created_at, u.updated_at
</sql>

<sql id="extendedUserFields">
    <include refid="baseUserFields"/>,
    p.avatar, p.bio, p.location,
    COUNT(DISTINCT o.id) as order_count,
    COALESCE(SUM(o.amount), 0) as total_spent
</sql>

<sql id="userJoinTables">
    LEFT JOIN user_profiles p ON u.id = p.user_id
    LEFT JOIN orders o ON u.id = o.user_id
</sql>

<!-- 使用模板组合的查询 -->
<sql id="getUsersWithStatistics">
    SELECT <include refid="extendedUserFields"/>
    FROM users u
    <include refid="userJoinTables"/>
    <where>
        <include refid="commonUserFilters"/>
    </where>
    GROUP BY u.id
    <include refid="commonSorting"/>
    <include refid="commonPagination"/>
</sql>

<!-- 可复用的过滤条件 -->
<sql id="commonUserFilters">
    <if test="status != null and status != ''">
        AND u.status = #{status}
    </if>
    <if test="searchKeyword != null and searchKeyword != ''">
        AND (u.name LIKE CONCAT('%', #{searchKeyword}, '%')
             OR u.email LIKE CONCAT('%', #{searchKeyword}, '%'))
    </if>
    <if test="dateRange != null">
        <if test="dateRange.start != null">
            AND u.created_at >= #{dateRange.start}
        </if>
        <if test="dateRange.end != null">
            AND u.created_at <= #{dateRange.end}
        </if>
    </if>
</sql>
```

### 4. 类型转换和验证

```java
// 启用高级类型处理
DatabaseUtil12.TypeConfig typeConfig = new DatabaseUtil12.TypeConfig()
    .enableAutoTypeConversion(true)           // 自动类型转换
    .enableStrictTypeChecking(false)          // 非严格类型检查
    .setDateFormat("yyyy-MM-dd HH:mm:ss")     // 日期格式
    .setNumberScale(2)                        // 数字精度
    .setStringMaxLength(1000)                 // 字符串最大长度
    .enableSqlInjectionProtection(true);      // SQL注入防护

DatabaseUtil12.setTypeConfig(typeConfig);

// 自定义类型转换器
DatabaseUtil12.registerTypeConverter(Date.class, value -> {
    if (value instanceof String) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse((String) value);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + value);
        }
    }
    return value;
});
```

## 性能监控和分析

### 详细性能指标

```java
// 获取详细性能统计
DatabaseUtil12.PerformanceMetrics metrics = DatabaseUtil12.getPerformanceMetrics();

System.out.println("=== DatabaseUtil12 性能统计 ===");
System.out.println("总解析次数: " + metrics.getTotalParseCount());
System.out.println("平均解析时间: " + metrics.getAverageParseTime() + "ms");
System.out.println("最大解析时间: " + metrics.getMaxParseTime() + "ms");
System.out.println("最小解析时间: " + metrics.getMinParseTime() + "ms");
System.out.println("缓存命中率: " + metrics.getCacheHitRate() + "%");
System.out.println("内存使用量: " + metrics.getMemoryUsage() + "MB");
System.out.println("并发解析峰值: " + metrics.getPeakConcurrentParse());

// 获取热点SQL统计
Map<String, DatabaseUtil12.SqlStats> hotSqls = metrics.getHotSqlStatistics();
hotSqls.entrySet().stream()
    .sorted((e1, e2) -> Long.compare(e2.getValue().getCallCount(), e1.getValue().getCallCount()))
    .limit(10)
    .forEach(entry -> {
        String sqlId = entry.getKey();
        DatabaseUtil12.SqlStats stats = entry.getValue();
        System.out.println(String.format("SQL [%s]: 调用%d次, 平均耗时%dms",
            sqlId, stats.getCallCount(), stats.getAverageTime()));
    });
```

### 实时监控

```java
// 启用实时监控
DatabaseUtil12.enableRealTimeMonitoring(true);

// 设置监控回调
DatabaseUtil12.setMonitoringCallback(new DatabaseUtil12.MonitoringCallback() {
    @Override
    public void onSlowParse(String namespace, String sqlId, long parseTime) {
        if (parseTime > 100) { // 超过100ms的解析
            System.out.println("慢解析警告: " + namespace + "." + sqlId + " 耗时 " + parseTime + "ms");
        }
    }

    @Override
    public void onCacheMiss(String namespace, String sqlId) {
        System.out.println("缓存未命中: " + namespace + "." + sqlId);
    }

    @Override
    public void onError(String namespace, String sqlId, Exception error) {
        System.err.println("解析错误: " + namespace + "." + sqlId + " - " + error.getMessage());
    }
});
```

## 错误处理和调试

### 常见问题诊断

#### 1. XML解析错误

```
🚨 症状：XML格式错误或标签不匹配
🔍 原因分析：
  - XML语法错误
  - 标签未正确闭合
  - 编码问题
  - 特殊字符未转义

🛠️ 解决方案：
  1. 启用详细错误信息
     DatabaseUtil12.enableVerboseErrorReporting(true);

  2. 使用XML验证工具
     ValidationResult result = DatabaseUtil12.validateXmlFile("file.xml");
     if (!result.isValid()) {
         result.getErrors().forEach(System.out::println);
     }

  3. 检查文件编码
     确保文件以UTF-8编码保存
```

#### 2. 参数解析问题

```
🚨 症状：参数未正确替换或类型错误
🔍 原因分析：
  - 参数名拼写错误
  - 参数类型不匹配
  - 空值处理问题
  - 集合参数格式错误

🛠️ 解决方案：
  1. 启用参数调试
     DatabaseUtil12.enableParameterDebug(true);

  2. 验证参数映射
     Map<String, Object> debugInfo = DatabaseUtil12.getParameterDebugInfo(
         "namespace", "sqlId", params
     );

  3. 检查参数类型
     DatabaseUtil12.validateParameters("namespace", "sqlId", params);
```

#### 3. 性能问题排查

```java
// 启用性能分析
DatabaseUtil12.enablePerformanceProfiling(true);

// 分析慢解析
DatabaseUtil12.ProfileResult profile = DatabaseUtil12.profileParse("namespace", "sqlId", params);
System.out.println("解析耗时分布:");
System.out.println("  XML加载: " + profile.getXmlLoadTime() + "ms");
System.out.println("  模板编译: " + profile.getTemplateCompileTime() + "ms");
System.out.println("  条件评估: " + profile.getConditionEvalTime() + "ms");
System.out.println("  参数绑定: " + profile.getParameterBindTime() + "ms");
System.out.println("  总耗时: " + profile.getTotalTime() + "ms");

// 内存使用分析
DatabaseUtil12.MemoryAnalysis memAnalysis = DatabaseUtil12.analyzeMemoryUsage();
System.out.println("内存使用分析:");
System.out.println("  缓存占用: " + memAnalysis.getCacheMemory() + "MB");
System.out.println("  模板占用: " + memAnalysis.getTemplateMemory() + "MB");
System.out.println("  临时对象: " + memAnalysis.getTempObjectMemory() + "MB");
```

## 集成和扩展

### Spring Boot 集成

```java
@Configuration
@EnableConfigurationProperties(DatabaseUtil12Properties.class)
public class DatabaseUtil12Config {

    @Bean
    @ConfigurationProperties(prefix = "mw.xml-util")
    public DatabaseUtil12Properties databaseUtil12Properties() {
        return new DatabaseUtil12Properties();
    }

    @Bean
    public DatabaseUtil12Initializer databaseUtil12Initializer(DatabaseUtil12Properties properties) {
        return new DatabaseUtil12Initializer(properties);
    }
}

// application.yml 配置
/*
mw:
  xml-util:
    cache:
      enabled: true
      size: 1000
      expire-time: 3600000
    file-watcher:
      enabled: true
      interval: 5000
    search-paths:
      - "classpath:sql/"
      - "classpath:mapper/"
    performance:
      parse-threads: 4
      max-concurrent: 50
      precompile: true
*/
```

### 自定义函数扩展

```java
// 注册自定义函数
DatabaseUtil12.registerFunction("formatDate", (params) -> {
    if (params.length >= 2) {
        Date date = (Date) params[0];
        String format = (String) params[1];
        return new SimpleDateFormat(format).format(date);
    }
    return null;
});

DatabaseUtil12.registerFunction("encrypt", (params) -> {
    if (params.length >= 1) {
        String text = (String) params[0];
        // 自定义加密逻辑
        return encryptText(text);
    }
    return null;
});

// 在XML中使用自定义函数
/*
<if test="createdAt != null">
    AND created_at >= #{formatDate(createdAt, 'yyyy-MM-dd')}
</if>

<if test="sensitiveData != null">
    AND data_hash = #{encrypt(sensitiveData)}
</if>
*/
```

## 版本信息和兼容性

### 版本特性

- **当前版本**：2.1.0
- **核心依赖**：DOM解析器（Java内置）
- **Java版本要求**：Java 8+
- **XML规范支持**：XML 1.0、XML 1.1
- **编码支持**：UTF-8、GBK、ISO-8859-1
- **最后更新**：2024年12月

### 版本升级指南

```java
// 从2.0版本升级到2.1
// 1. API兼容性 - 无破坏性变更
// 2. 新增功能
DatabaseUtil12.enableRealTimeMonitoring(true);    // 新增：实时监控
DatabaseUtil12.setPerformanceConfig(config);      // 新增：性能配置
DatabaseUtil12.registerFunction("name", func);    // 新增：自定义函数

// 3. 配置迁移
// 旧版本配置方式仍然支持，建议使用新的配置API
```

### 兼容性矩阵

| 特性 | 2.0版本 | 2.1版本 | 兼容性 |
|------|---------|---------|--------|
| 基础XML解析 | ✅ | ✅ | 完全兼容 |
| 动态SQL构建 | ✅ | ✅ | 完全兼容 |
| 缓存机制 | ✅ | ✅ | 功能增强 |
| 性能监控 | 基础 | 高级 | 向后兼容 |
| 自定义函数 | ❌ | ✅ | 新增功能 |
| 实时监控 | ❌ | ✅ | 新增功能 |

---

*DatabaseUtil12 2.1 提供了强大且灵活的XML SQL解析能力，通过其先进的模板引擎和丰富的功能特性，帮助开发者构建高效、可维护的动态SQL解决方案。无论是简单的条件查询还是复杂的多表关联，DatabaseUtil12 都能提供优雅的解决方案。*
