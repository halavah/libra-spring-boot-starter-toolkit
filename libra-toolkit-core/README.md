# Libra Toolkit - 静态工具箱模块

一个功能强大的纯静态工具箱模块，提供数据库操作、HTTP服务器、Redis缓存等实用工具类。

## 📦 模块结构

```
libra-spring-boot-starter-toolkit/
├── database/          # 数据库工具（DatabaseUtil 系列 - 11个版本）
├── http/              # HTTP 服务器工具（PostmanUtil）
└── redis/             # Redis 工具（RedissonUtil - Lettuce + Redisson 双引擎）
```

---

## 🎯 功能特性

### 1. 数据库工具（Database）

提供 11 个版本的 DatabaseUtil，从基础到高级，满足不同场景需求：

| 工具类 | 版本 | 主要特性 |
|--------|------|----------|
| `DatabaseUtil.java` | v0 | 基础版本 |
| `DatabaseUtil11.java` | v1.1 | HikariCP 连接池 + 基础 CRUD |
| `DatabaseUtil12.java` | v1.2 | 增强版 + 更多特性 |
| `DatabaseUtil21.java` | v2.1 | XML SQL 支持 + 动态 SQL 解析 |
| `DatabaseUtil22.java` | v2.2 | XML SQL 增强版 |
| `DatabaseUtil31.java` | v3.1 | 物理分页支持 |
| `DatabaseUtil32.java` | v3.2 | DTO/VO 对象映射 |
| `DatabaseUtil33.java` | v3.3 | 完整版（3.1 + 3.2） |
| `DatabaseUtil41.java` | v4.1 | UPSERT 操作支持 |
| `DatabaseUtil42.java` | v4.2 | 批量 UPSERT |
| `DatabaseUtil43.java` | v4.3 | 终极版（所有功能） |

**核心功能**：
- ✅ 多数据源动态管理
- ✅ HikariCP 高性能连接池
- ✅ MyBatis 风格的动态 SQL（XML文件）
- ✅ 声明式事务管理
- ✅ 批量操作（批量插入、更新、删除）
- ✅ 物理分页查询
- ✅ 跨数据库 UPSERT 操作
- ✅ DTO/VO 对象自动映射

**推荐版本**：
- **简单场景**：使用 `DatabaseUtil11` 或 `DatabaseUtil21`
- **复杂场景**：使用 `DatabaseUtil43`（功能最全）

---

### 2. HTTP 服务器工具（HTTP）

轻量级嵌入式 HTTP 服务器，无需 Spring Boot 即可快速构建 REST API。

**核心功能**：
- ✅ 注解驱动路由（@PostmanController, @PostmanMapping）
- ✅ 自动包扫描
- ✅ 统一响应格式（Result<T>）
- ✅ 可插拔认证（Bearer Token, HMAC Signature）
- ✅ 请求日志 + curl 命令生成
- ✅ BaseController 优雅参数访问
- ✅ ThreadLocal 请求上下文

**适用场景**：
- 快速原型开发
- 单文件微服务
- 独立工具服务
- 测试环境快速验证

---

### 3. Redis 工具（Redis）

双引擎 Redis 操作工具，同时支持 Lettuce 和 Redisson。

**核心功能**：
- ✅ **Lettuce 引擎**：用于数据操作（String、Hash、List、Set、ZSet）
- ✅ **Redisson 引擎**：用于分布式锁、分布式对象
- ✅ 统一的 API 设计
- ✅ 连接池管理
- ✅ 支持 Redis Cluster、Sentinel、单机模式
- ✅ 批量操作（Pipeline）
- ✅ 发布订阅（Pub/Sub）
- ✅ 分布式锁（公平锁、读写锁）
- ✅ 分布式集合（Map、Set、List、Queue）

**适用场景**：
- 缓存管理
- 分布式锁
- 分布式计数器
- 限流器
- 会话存储

---

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>cn.myslayers</groupId>
    <artifactId>libra-spring-boot-starter-toolkit</artifactId>
    <version>${revision}</version>
</dependency>
```

---

## 📚 使用示例

### 示例 1：数据库操作（DatabaseUtil）

#### 1.1 基础查询（DatabaseUtil11）

```java
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil11;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil11.DatabaseConfig;

public class DatabaseExample {

    public static void main(String[] args) {
        // 1. 注册数据源
        DatabaseConfig config = new DatabaseConfig(
            "jdbc:mysql://localhost:3306/test?useSSL=false",
            "root",
            "password",
            "com.mysql.cj.jdbc.Driver"
        );
        DatabaseUtil11.registerDatabase("testdb", config);
        DatabaseUtil11.setDefaultDatabase("testdb");

        // 2. 简单查询
        List<Map<String, Object>> users = DatabaseUtil11.selectList(
            "SELECT * FROM users WHERE age > ?",
            18
        );
        users.forEach(System.out::println);

        // 3. 查询单条记录
        Map<String, Object> user = DatabaseUtil11.selectOne(
            "SELECT * FROM users WHERE id = ?",
            1
        );
        System.out.println(user);

        // 4. 插入数据
        int rows = DatabaseUtil11.insert(
            "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
            "John Doe", 25, "john@example.com"
        );
        System.out.println("Inserted " + rows + " rows");

        // 5. 更新数据
        int updated = DatabaseUtil11.update(
            "UPDATE users SET email = ? WHERE id = ?",
            "newemail@example.com", 1
        );
        System.out.println("Updated " + updated + " rows");

        // 6. 删除数据
        int deleted = DatabaseUtil11.delete(
            "DELETE FROM users WHERE id = ?",
            1
        );
        System.out.println("Deleted " + deleted + " rows");
    }
}
```

#### 1.2 事务操作

```java
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil11;

public class TransactionExample {

    public static void main(String[] args) {
        // 声明式事务
        Boolean result = DatabaseUtil11.executeInTransaction(connection -> {
            // 插入用户
            DatabaseUtil11.insert(
                "INSERT INTO users (name, age) VALUES (?, ?)",
                "Alice", 30
            );

            // 更新账户余额
            DatabaseUtil11.update(
                "UPDATE accounts SET balance = balance - 100 WHERE user_id = ?",
                1
            );

            // 如果一切正常，提交事务
            return true;
        });

        System.out.println("Transaction result: " + result);
    }
}
```

#### 1.3 批量操作

```java
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil11;
import java.util.*;

public class BatchExample {

    public static void main(String[] args) {
        // 准备批量数据
        List<List<Object>> batchParams = Arrays.asList(
            Arrays.asList("User1", 20, "user1@example.com"),
            Arrays.asList("User2", 25, "user2@example.com"),
            Arrays.asList("User3", 30, "user3@example.com")
        );

        // 批量插入
        int[] results = DatabaseUtil11.executeBatch(
            "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
            batchParams
        );

        System.out.println("Batch insert affected rows: " + Arrays.toString(results));
    }
}
```

#### 1.4 XML SQL 查询（DatabaseUtil21）

首先创建 XML SQL 文件 `src/main/resources/sql/UserMapper.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="user">

    <!-- 根据年龄查询用户 -->
    <select id="findByAge" resultType="map">
        SELECT * FROM users
        <where>
            <if test="minAge != null">
                AND age &gt;= #{minAge}
            </if>
            <if test="maxAge != null">
                AND age &lt;= #{maxAge}
            </if>
        </where>
        ORDER BY age DESC
    </select>

    <!-- 动态更新用户 -->
    <update id="updateUser">
        UPDATE users
        <set>
            <if test="name != null">name = #{name},</if>
            <if test="email != null">email = #{email},</if>
            <if test="age != null">age = #{age}</if>
        </set>
        WHERE id = #{id}
    </update>

</mapper>
```

使用 XML SQL：

```java
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil21;
import java.util.*;

public class XmlSqlExample {

    public static void main(String[] args) {
        // 注册数据源
        DatabaseUtil21.registerDatabase("testdb",
            new DatabaseUtil21.DatabaseConfig(/* ... */));
        DatabaseUtil21.setDefaultDatabase("testdb");

        // 使用 XML SQL 查询
        Map<String, Object> params = new HashMap<>();
        params.put("minAge", 18);
        params.put("maxAge", 30);

        List<Map<String, Object>> users = DatabaseUtil21.selectList(
            "user.findByAge",  // namespace.sqlId
            params
        );

        users.forEach(System.out::println);

        // 使用 XML SQL 更新
        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("id", 1);
        updateParams.put("name", "Updated Name");
        updateParams.put("email", "updated@example.com");

        int rows = DatabaseUtil21.update("user.updateUser", updateParams);
        System.out.println("Updated " + rows + " rows");
    }
}
```

#### 1.5 分页查询（DatabaseUtil31）

```java
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.Page;

public class PaginationExample {

    public static void main(String[] args) {
        // 注册数据源
        DatabaseUtil31.registerDatabase("testdb", /* ... */);
        DatabaseUtil31.setDefaultDatabase("testdb");

        // 分页查询
        Page<Map<String, Object>> page = DatabaseUtil31.selectPage(
            "SELECT * FROM users WHERE age > ?",  // SQL
            1,                                     // 当前页（从 1 开始）
            10,                                    // 每页大小
            18                                     // SQL 参数
        );

        System.out.println("Total records: " + page.getTotal());
        System.out.println("Total pages: " + page.getPages());
        System.out.println("Current page: " + page.getCurrent());
        System.out.println("Page size: " + page.getSize());
        System.out.println("Records: ");
        page.getRecords().forEach(System.out::println);
    }
}
```

---

### 示例 2：HTTP 服务器（PostmanUtil）

#### 2.1 简单的 REST API

```java
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.*;
import java.util.*;

// 1. 定义控制器
@PostmanController("/api")
public class UserController extends BaseController {

    // GET /api/users - 查询用户列表
    @PostmanMapping(value = "/users", method = RequestMethod.GET)
    public Result<List<User>> getUsers() {
        // 获取查询参数
        Map<String, String> params = getParams();
        String ageStr = params.get("age");

        // 业务逻辑
        List<User> users = userService.findByAge(Integer.parseInt(ageStr));

        return Result.ok(users);
    }

    // POST /api/users - 创建用户
    @PostmanMapping(value = "/users", method = RequestMethod.POST)
    public Result<User> createUser() {
        // 获取请求体
        UserDTO dto = getBody(UserDTO.class);

        // 业务逻辑
        User user = userService.create(dto);

        return Result.ok(user);
    }

    // PUT /api/users/{id} - 更新用户
    @PostmanMapping(value = "/users/{id}", method = RequestMethod.PUT)
    public Result<User> updateUser() {
        Map<String, String> params = getParams();
        Long id = Long.parseLong(params.get("id"));

        UserDTO dto = getBody(UserDTO.class);

        User user = userService.update(id, dto);

        return Result.ok(user);
    }

    // DELETE /api/users/{id} - 删除用户
    @PostmanMapping(value = "/users/{id}", method = RequestMethod.DELETE)
    public Result<Void> deleteUser() {
        Map<String, String> params = getParams();
        Long id = Long.parseLong(params.get("id"));

        userService.delete(id);

        return Result.ok();
    }
}

// 2. 启动服务器
public class Application {
    public static void main(String[] args) {
        // 设置端口
        PostmanUtil.setPort(5176);

        // 启动服务器并扫描控制器
        PostmanUtil.HttpCore.startServer(UserController.class);

        System.out.println("Server started at http://localhost:5176");
    }
}
```

#### 2.2 带认证的 API

```java
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.*;

public class Application {
    public static void main(String[] args) {
        // 设置端口
        PostmanUtil.setPort(5176);

        // 启用 Bearer Token 认证
        PostmanUtil.AuthManager.enableBearerToken("your-secret-token-here");

        // 或者启用 HMAC 签名认证
        // PostmanUtil.AuthManager.enableHmacAuth("your-secret-key");

        // 启动服务器
        PostmanUtil.HttpCore.startServer(UserController.class);
    }
}
```

客户端请求示例：
```bash
# 使用 Bearer Token
curl -H "Authorization: Bearer your-secret-token-here" \
     http://localhost:5176/api/users
```

#### 2.3 单文件微服务示例

完整的 CRUD API，所有代码在一个文件中：

```java
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.*;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil11;
import java.util.*;

/**
 * 单文件微服务示例
 * 包含：Controller + Service + DAO + DTO
 */
public class SingleFileService {

    // ========== DTO ==========
    static class UserDTO {
        public String name;
        public Integer age;
        public String email;
    }

    // ========== DAO ==========
    static class UserDAO {
        public List<Map<String, Object>> findAll() {
            return DatabaseUtil11.selectList("SELECT * FROM users");
        }

        public Map<String, Object> findById(Long id) {
            return DatabaseUtil11.selectOne("SELECT * FROM users WHERE id = ?", id);
        }

        public int insert(String name, Integer age, String email) {
            return DatabaseUtil11.insert(
                "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
                name, age, email
            );
        }

        public int update(Long id, String name, Integer age, String email) {
            return DatabaseUtil11.update(
                "UPDATE users SET name = ?, age = ?, email = ? WHERE id = ?",
                name, age, email, id
            );
        }

        public int delete(Long id) {
            return DatabaseUtil11.delete("DELETE FROM users WHERE id = ?", id);
        }
    }

    // ========== Service ==========
    static class UserService {
        private final UserDAO userDAO = new UserDAO();

        public List<Map<String, Object>> getAllUsers() {
            return userDAO.findAll();
        }

        public Map<String, Object> getUserById(Long id) {
            return userDAO.findById(id);
        }

        public boolean createUser(UserDTO dto) {
            int rows = userDAO.insert(dto.name, dto.age, dto.email);
            return rows > 0;
        }

        public boolean updateUser(Long id, UserDTO dto) {
            int rows = userDAO.update(id, dto.name, dto.age, dto.email);
            return rows > 0;
        }

        public boolean deleteUser(Long id) {
            int rows = userDAO.delete(id);
            return rows > 0;
        }
    }

    // ========== Controller ==========
    @PostmanController("/api")
    static class UserController extends BaseController {
        private final UserService userService = new UserService();

        @PostmanMapping(value = "/users", method = RequestMethod.GET)
        public Result<List<Map<String, Object>>> listUsers() {
            return Result.ok(userService.getAllUsers());
        }

        @PostmanMapping(value = "/users/{id}", method = RequestMethod.GET)
        public Result<Map<String, Object>> getUser() {
            Long id = Long.parseLong(getParams().get("id"));
            return Result.ok(userService.getUserById(id));
        }

        @PostmanMapping(value = "/users", method = RequestMethod.POST)
        public Result<String> createUser() {
            UserDTO dto = getBody(UserDTO.class);
            boolean success = userService.createUser(dto);
            return success ? Result.ok("Created") : Result.error("Failed");
        }

        @PostmanMapping(value = "/users/{id}", method = RequestMethod.PUT)
        public Result<String> updateUser() {
            Long id = Long.parseLong(getParams().get("id"));
            UserDTO dto = getBody(UserDTO.class);
            boolean success = userService.updateUser(id, dto);
            return success ? Result.ok("Updated") : Result.error("Failed");
        }

        @PostmanMapping(value = "/users/{id}", method = RequestMethod.DELETE)
        public Result<String> deleteUser() {
            Long id = Long.parseLong(getParams().get("id"));
            boolean success = userService.deleteUser(id);
            return success ? Result.ok("Deleted") : Result.error("Failed");
        }
    }

    // ========== 启动入口 ==========
    public static void main(String[] args) {
        // 初始化数据库
        DatabaseUtil11.registerDatabase("testdb", new DatabaseUtil11.DatabaseConfig(
            "jdbc:mysql://localhost:3306/test",
            "root",
            "password",
            "com.mysql.cj.jdbc.Driver"
        ));
        DatabaseUtil11.setDefaultDatabase("testdb");

        // 启动 HTTP 服务器
        PostmanUtil.setPort(5176);
        PostmanUtil.HttpCore.startServer(UserController.class);

        System.out.println("Single-file microservice started at http://localhost:5176");
        System.out.println("Try: curl http://localhost:5176/api/users");
    }
}
```

---

### 示例 3：Redis 操作（RedissonUtil）

#### 3.1 基础字符串操作（Lettuce 引擎）

```java
import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;

public class RedisBasicExample {

    public static void main(String[] args) {
        // 初始化 Redis 连接
        RedissonUtil.init("redis://localhost:6379");

        // 字符串操作
        RedissonUtil.set("user:1:name", "Alice");
        RedissonUtil.setex("session:abc", 3600, "session-data");  // 带过期时间

        String name = RedissonUtil.get("user:1:name");
        System.out.println("Name: " + name);

        // 数值操作
        RedissonUtil.incr("page:views");
        RedissonUtil.incrBy("score", 10);
        Long views = RedissonUtil.getLong("page:views");

        // 删除
        RedissonUtil.del("user:1:name");

        // 检查存在
        boolean exists = RedissonUtil.exists("session:abc");
    }
}
```

#### 3.2 Hash 操作

```java
import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import java.util.*;

public class RedisHashExample {

    public static void main(String[] args) {
        RedissonUtil.init("redis://localhost:6379");

        // Hash 操作
        Map<String, String> user = new HashMap<>();
        user.put("name", "Alice");
        user.put("age", "25");
        user.put("email", "alice@example.com");

        RedissonUtil.hmset("user:1", user);

        // 获取单个字段
        String name = RedissonUtil.hget("user:1", "name");

        // 获取所有字段
        Map<String, String> userData = RedissonUtil.hgetAll("user:1");

        // 字段自增
        RedissonUtil.hincrBy("user:1", "age", 1);

        // 删除字段
        RedissonUtil.hdel("user:1", "email");
    }
}
```

#### 3.3 分布式锁（Redisson 引擎）

```java
import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.redisson.api.RLock;

public class RedisLockExample {

    public static void main(String[] args) {
        RedissonUtil.init("redis://localhost:6379");

        // 获取锁
        RLock lock = RedissonUtil.getLock("order:lock:12345");

        try {
            // 尝试加锁，最多等待 10 秒，锁定 30 秒后自动释放
            if (lock.tryLock(10, 30, java.util.concurrent.TimeUnit.SECONDS)) {
                try {
                    // 执行业务逻辑
                    System.out.println("Processing order 12345...");
                    Thread.sleep(2000);
                    System.out.println("Order processed!");
                } finally {
                    // 释放锁
                    lock.unlock();
                }
            } else {
                System.out.println("Could not acquire lock");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#### 3.4 分布式集合

```java
import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import org.redisson.api.*;
import java.util.*;

public class RedisCollectionExample {

    public static void main(String[] args) {
        RedissonUtil.init("redis://localhost:6379");

        // 分布式 Map
        RMap<String, User> userMap = RedissonUtil.getMap("users");
        userMap.put("user1", new User("Alice", 25));
        userMap.put("user2", new User("Bob", 30));
        User user = userMap.get("user1");

        // 分布式 Set
        RSet<String> tags = RedissonUtil.getSet("article:tags");
        tags.add("java");
        tags.add("redis");
        tags.add("database");

        // 分布式 List
        RList<String> logs = RedissonUtil.getList("system:logs");
        logs.add("Log entry 1");
        logs.add("Log entry 2");

        // 分布式队列
        RQueue<Task> taskQueue = RedissonUtil.getQueue("tasks");
        taskQueue.offer(new Task("Process payment"));
        Task task = taskQueue.poll();
    }
}
```

#### 3.5 缓存管理示例

```java
import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RedisCacheExample {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        RedissonUtil.init("redis://localhost:6379");

        // 缓存对象
        User user = new User("Alice", 25);
        String userJson = mapper.writeValueAsString(user);
        RedissonUtil.setex("cache:user:1", 3600, userJson);  // 缓存 1 小时

        // 读取缓存
        String cached = RedissonUtil.get("cache:user:1");
        if (cached != null) {
            User cachedUser = mapper.readValue(cached, User.class);
            System.out.println("From cache: " + cachedUser.getName());
        }

        // 缓存失效
        RedissonUtil.del("cache:user:1");
    }

    static class User {
        private String name;
        private int age;

        public User() {}

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
    }
}
```

---

## 🔧 配置说明

### 数据库配置（YAML 方式）

创建 `database.yml` 配置文件：

```yaml
databases:
  - name: testdb
    url: jdbc:mysql://localhost:3306/test?useSSL=false
    username: root
    password: password
    driver: com.mysql.cj.jdbc.Driver
    pool:
      maximumPoolSize: 10
      minimumIdle: 5
      connectionTimeout: 30000

  - name: proddb
    url: jdbc:mysql://prod-server:3306/production
    username: prod_user
    password: prod_password
    driver: com.mysql.cj.jdbc.Driver
```

加载配置：

```java
// 从 YAML 文件加载数据源配置
DatabaseUtil11.loadFromYaml("database.yml");
DatabaseUtil11.setDefaultDatabase("testdb");
```

---

## 📖 完整示例项目

查看 `src/test/java/cn/myslayers/libra/framework/toolkit/examples/` 目录下的完整示例代码：

- `DatabaseExamples.java` - 数据库操作完整示例
- `HttpServerExamples.java` - HTTP 服务器完整示例
- `OfficeExamples.java` - Office 文档处理完整示例
- `SingleFileMicroservice.java` - 单文件微服务完整示例

---

## 🎓 推荐实践

### 1. 选择合适的 DatabaseUtil 版本

- **快速原型**：使用 `DatabaseUtil11`（简单快捷）
- **XML SQL**：使用 `DatabaseUtil21`（支持动态 SQL）
- **分页查询**：使用 `DatabaseUtil31`（物理分页）
- **对象映射**：使用 `DatabaseUtil32`（DTO/VO）
- **生产环境**：使用 `DatabaseUtil43`（功能最全）

### 2. HTTP 服务器使用场景

- ✅ **适合**：快速原型、单文件微服务、独立工具
- ❌ **不适合**：大型应用（请使用 Spring Boot）

### 3. Office 工具选择

- **开源方案**：使用 Apache POI 工具类（`ApacheExcelUtil` 等）
- **商业方案**：使用 Aspose 工具类（`AsposeExcelUtil` 等，需要许可证）

---

## 📝 注意事项

1. **数据库连接池**：DatabaseUtil 使用 HikariCP，性能优秀，推荐生产使用
2. **HTTP 服务器**：PostmanUtil 基于 Java 内置 HttpServer，适合轻量级场景
3. **Office 工具**：Aspose 工具需要商业许可证，Apache POI 工具开源免费
4. **线程安全**：所有工具类都是线程安全的
5. **资源释放**：使用完毕后记得关闭数据库连接和文件流

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

---

## 📄 License

MIT License

---

## 📮 联系方式

- 作者：Libra Team
- 邮箱：support@myslayers.cn
- 项目地址：https://github.com/myslayers/libra-boot-plus
