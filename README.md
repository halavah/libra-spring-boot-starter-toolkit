
<div align="center">

# 🚀 Libra Spring Boot Starter Toolkit

**一个功能强大的 Java 静态工具箱，让开发更简单**

[![License](https://img.shields.io/badge/license-AGPL--3.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.5.14-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![GitHub release](https://img.shields.io/github/v/release/halavah/libra-spring-boot-starter-toolkit)](https://github.com/halavah/libra-spring-boot-starter-toolkit/releases)
[![GitHub stars](https://img.shields.io/github/stars/halavah/libra-spring-boot-starter-toolkit?style=social)](https://github.com/halavah/libra-spring-boot-starter-toolkit)

[English](README_EN.md) | **简体中文**

</div>

> **本文遵循开源说明规范**：结构与徽章按统一体例，徽章仅指向公开可访问资源，版本信息指向发布渠道动态页。


---

## 📖 目录

- [项目简介](#-项目简介)
- [核心特性](#-核心特性)
- [快速开始](#-快速开始)
- [功能模块](#-功能模块)
  - [数据库工具 DatabaseUtil](#1-数据库工具-databaseutil)
  - [HTTP 服务器工具 PostmanUtil](#2-http-服务器工具-postmanutil)
  - [Redis 工具 RedissonUtil](#3-redis-工具-redissonutil)
- [使用示例](#-使用示例)
- [技术栈](#-技术栈)
- [贡献指南](#-贡献指南)
- [开源协议](#-开源协议)
- [联系我们](#-联系我们)

---

## 🎯 项目简介

**Libra Spring Boot Starter Toolkit** 是一个高效、易用的 Java 工具包，旨在简化日常开发中的常见任务。无论你是在开发企业级应用还是快速原型，这个工具包都能为你提供强大的支持。

### 💡 解决什么问题？

- ❌ **繁琐的数据库操作**：手动管理连接池、编写重复的 JDBC 代码？
- ❌ **复杂的 HTTP 服务搭建**：需要 Spring Boot 才能运行一个简单的 REST API？
- ❌ **混乱的 Redis 客户端**：在 Lettuce 和 Redisson 之间难以抉择？

### ✅ 我们的解决方案

- ✅ **统一的数据库工具**：一行代码完成 CRUD，支持事务、分页、批量操作
- ✅ **轻量级 HTTP 服务器**：无需 Spring Boot，单文件即可启动 REST API
- ✅ **双引擎 Redis 工具**：Lettuce + Redisson 完美融合，既能操作数据又能分布式锁

---

## ⭐ 核心特性

### 🔥 简单易用

```java
// 只需一行代码，完成数据库查询
List<Map<String, Object>> users = DatabaseUtil.selectList("SELECT * FROM users WHERE age > ?", 18);

// 启动 HTTP 服务器，只需两行
PostmanUtil.setPort(8080);
PostmanUtil.HttpCore.startServer(UserController.class);

// Redis 操作，简单直接
RedissonUtil.set("key", "value");
String value = RedissonUtil.get("key");
```

### ⚡ 高性能

- 🚀 **HikariCP 连接池**：业界最快的数据库连接池
- 🚀 **Lettuce Redis 客户端**：基于 Netty 的高性能异步客户端
- 🚀 **零依赖 HTTP 服务器**：基于 Java 原生 HttpServer，启动速度快

### 🛡️ 生产级别

- ✅ 完善的异常处理
- ✅ 线程安全设计
- ✅ 详细的日志输出
- ✅ 全面的单元测试

---

## 🚀 快速开始

### 前置要求

- **Java 8** 或更高版本
- **Maven 3.6+** 或 **Gradle 6.0+**

### 1. 添加依赖

#### Maven

在你的 `pom.xml` 中添加仓库配置：

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/halavah/libra-spring-boot-starter-toolkit</url>
    </repository>
</repositories>
```

在 `~/.m2/settings.xml` 中配置 GitHub 认证（GitHub Packages 需要认证才能下载）：

```xml
<servers>
    <server>
        <id>github</id>
        <username>你的GitHub用户名</username>
        <password>你的GitHub-Token</password>
    </server>
</servers>
```

> **提示**：如何创建 GitHub Token？
> 1. 访问 [GitHub Settings → Developer settings → Personal access tokens](https://github.com/settings/tokens)
> 2. 点击 **Generate new token (classic)**
> 3. 勾选 `read:packages` 权限
> 4. 生成并复制 Token

添加依赖：

```xml
<dependency>
    <groupId>cn.myslayers</groupId>
    <artifactId>libra-toolkit-core</artifactId>
    <version>X.X.X</version>
</dependency>
```

#### Gradle

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/halavah/libra-spring-boot-starter-toolkit")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation 'cn.myslayers:libra-toolkit-core:X.X.X'
}
```

### 2. 快速体验

创建一个简单的数据库查询示例：

```java
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil;
import java.util.List;
import java.util.Map;

public class QuickStart {
    public static void main(String[] args) {
        // 注册数据源
        DatabaseUtil.registerDatabase("mydb",
            new DatabaseUtil.DatabaseConfig(
                "jdbc:mysql://localhost:3306/test",
                "root",
                "password",
                "com.mysql.cj.jdbc.Driver"
            )
        );
        DatabaseUtil.setDefaultDatabase("mydb");

        // 查询数据
        List<Map<String, Object>> users = DatabaseUtil.selectList(
            "SELECT * FROM users WHERE age > ?", 18
        );

        // 输出结果
        users.forEach(System.out::println);
    }
}
```

---

## 📦 功能模块

### 1. 数据库工具 (DatabaseUtil)

> 💾 **告别繁琐的 JDBC 代码，一行搞定数据库操作**

#### 🌟 核心功能

- ✅ **多数据源管理**：支持同时连接多个数据库
- ✅ **HikariCP 连接池**：性能卓越，配置简单
- ✅ **简化的 CRUD 操作**：`selectList()`、`insert()`、`update()`、`delete()`
- ✅ **事务管理**：声明式事务，自动回滚
- ✅ **批量操作**：高效的批量插入、更新、删除
- ✅ **物理分页**：支持 MySQL、PostgreSQL、Oracle 等
- ✅ **动态 SQL**：支持 MyBatis 风格的 XML SQL
- ✅ **对象映射**：自动将查询结果映射为 Java 对象

#### 💡 使用场景

- ✅ 企业级应用的数据访问层
- ✅ 数据迁移脚本
- ✅ 批量数据处理
- ✅ 快速原型开发

#### 📚 快速示例

**基础查询**

```java
// 查询列表
List<Map<String, Object>> users = DatabaseUtil.selectList(
    "SELECT * FROM users WHERE age > ?", 18
);

// 查询单条
Map<String, Object>> user = DatabaseUtil.selectOne(
    "SELECT * FROM users WHERE id = ?", 1
);

// 查询数量
Long count = DatabaseUtil.selectCount(
    "SELECT COUNT(*) FROM users WHERE status = ?", "active"
);
```

**插入、更新、删除**

```java
// 插入
int rows = DatabaseUtil.insert(
    "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
    "Alice", 25, "alice@example.com"
);

// 更新
int updated = DatabaseUtil.update(
    "UPDATE users SET email = ? WHERE id = ?",
    "newemail@example.com", 1
);

// 删除
int deleted = DatabaseUtil.delete(
    "DELETE FROM users WHERE id = ?", 1
);
```

**事务管理**

```java
// 声明式事务
Boolean success = DatabaseUtil.executeInTransaction(connection -> {
    // 所有操作在同一个事务中
    DatabaseUtil.insert("INSERT INTO users (name, age) VALUES (?, ?)", "Bob", 30);
    DatabaseUtil.update("UPDATE accounts SET balance = balance - 100 WHERE user_id = ?", 1);
    return true; // 返回 true 提交，返回 false 或抛异常则回滚
});
```

**批量操作**

```java
List<List<Object>> batchParams = Arrays.asList(
    Arrays.asList("User1", 20, "user1@example.com"),
    Arrays.asList("User2", 25, "user2@example.com"),
    Arrays.asList("User3", 30, "user3@example.com")
);

int[] results = DatabaseUtil.executeBatch(
    "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
    batchParams
);
```

**分页查询**

```java
Page<Map<String, Object>> page = DatabaseUtil.selectPage(
    "SELECT * FROM users WHERE age > ?",  // SQL
    1,                                     // 当前页（从 1 开始）
    10,                                    // 每页大小
    18                                     // SQL 参数
);

System.out.println("总记录数: " + page.getTotal());
System.out.println("总页数: " + page.getPages());
page.getRecords().forEach(System.out::println);
```

> 📖 **更多示例**：查看 [数据库工具完整文档](docs/database/README.md)

---

### 2. HTTP 服务器工具 (PostmanUtil)

> 🌐 **无需 Spring Boot，单文件即可启动 REST API**

#### 🌟 核心功能

- ✅ **注解驱动路由**：`@PostmanController`、`@PostmanMapping`
- ✅ **自动包扫描**：无需手动注册控制器
- ✅ **统一响应格式**：`Result<T>` 封装响应数据
- ✅ **请求参数解析**：自动解析 Query、Path、Body 参数
- ✅ **认证机制**：支持 Bearer Token、HMAC 签名
- ✅ **请求日志**：自动记录请求信息和 curl 命令
- ✅ **BaseController**：优雅访问请求上下文

#### 💡 使用场景

- ✅ 快速原型开发
- ✅ 单文件微服务
- ✅ 独立工具服务
- ✅ 测试环境快速验证
- ⚠️ **不推荐**：大型生产应用（请使用 Spring Boot）

#### 📚 快速示例

**简单的 REST API**

```java
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil;
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.*;

// 1. 定义控制器
@PostmanController("/api")
public class UserController extends BaseController {

    @PostmanMapping(value = "/users", method = RequestMethod.GET)
    public Result<List<User>> getUsers() {
        // 获取查询参数
        String age = getParam("age");

        // 业务逻辑
        List<User> users = userService.findByAge(Integer.parseInt(age));

        return Result.ok(users);
    }

    @PostmanMapping(value = "/users", method = RequestMethod.POST)
    public Result<User> createUser() {
        // 获取请求体
        UserDTO dto = getBody(UserDTO.class);

        // 业务逻辑
        User user = userService.create(dto);

        return Result.ok(user);
    }
}

// 2. 启动服务器
public class Application {
    public static void main(String[] args) {
        PostmanUtil.setPort(8080);
        PostmanUtil.HttpCore.startServer(UserController.class);
        System.out.println("Server started at http://localhost:8080");
    }
}
```

**单文件微服务（Controller + Service + DAO 全在一个文件）**

```java
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.*;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil;

public class SingleFileService {

    // Controller
    @PostmanController("/api")
    static class UserController extends BaseController {
        private final UserService service = new UserService();

        @PostmanMapping(value = "/users", method = RequestMethod.GET)
        public Result<List<Map<String, Object>>> listUsers() {
            return Result.ok(service.getAllUsers());
        }
    }

    // Service
    static class UserService {
        public List<Map<String, Object>> getAllUsers() {
            return DatabaseUtil.selectList("SELECT * FROM users");
        }
    }

    // 启动
    public static void main(String[] args) {
        // 初始化数据库
        DatabaseUtil.registerDatabase("db", new DatabaseUtil.DatabaseConfig(/* ... */));

        // 启动服务器
        PostmanUtil.setPort(8080);
        PostmanUtil.HttpCore.startServer(UserController.class);
    }
}
```

**带认证的 API**

```java
public class Application {
    public static void main(String[] args) {
        // 启用 Bearer Token 认证
        PostmanUtil.AuthManager.enableBearerToken("your-secret-token");

        // 启动服务器
        PostmanUtil.setPort(8080);
        PostmanUtil.HttpCore.startServer(UserController.class);
    }
}

// 客户端请求
// curl -H "Authorization: Bearer your-secret-token" http://localhost:8080/api/users
```

> 📖 **更多示例**：查看 [HTTP 服务器完整文档](docs/http/README.md)

---

### 3. Redis 工具 (RedissonUtil)

> 🔴 **双引擎 Redis 工具，Lettuce + Redisson 完美融合**

#### 🌟 核心功能

- ✅ **Lettuce 引擎**：用于数据操作（String、Hash、List、Set、ZSet）
- ✅ **Redisson 引擎**：用于分布式锁、分布式对象
- ✅ **统一 API 设计**：一套接口，两种引擎
- ✅ **连接池管理**：自动管理连接池
- ✅ **多模式支持**：单机、Cluster、Sentinel
- ✅ **批量操作**：Pipeline 批量执行
- ✅ **发布订阅**：Pub/Sub 消息通知
- ✅ **分布式锁**：公平锁、读写锁、红锁
- ✅ **分布式集合**：Map、Set、List、Queue

#### 💡 使用场景

- ✅ 缓存管理
- ✅ 分布式锁
- ✅ 分布式计数器
- ✅ 限流器
- ✅ 会话存储
- ✅ 消息队列

#### 📚 快速示例

**基础字符串操作**

```java
import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;

// 初始化连接
RedissonUtil.init("redis://localhost:6379");

// 字符串操作
RedissonUtil.set("user:1:name", "Alice");
RedissonUtil.setex("session:abc", 3600, "session-data");  // 带过期时间

String name = RedissonUtil.get("user:1:name");

// 数值操作
RedissonUtil.incr("page:views");
RedissonUtil.incrBy("score", 10);

// 删除
RedissonUtil.del("user:1:name");
```

**Hash 操作**

```java
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
```

**分布式锁**

```java
import org.redisson.api.RLock;

// 获取锁
RLock lock = RedissonUtil.getLock("order:lock:12345");

try {
    // 尝试加锁，最多等待 10 秒，锁定 30 秒后自动释放
    if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
        try {
            // 执行业务逻辑
            System.out.println("Processing order...");
        } finally {
            lock.unlock();  // 释放锁
        }
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

**分布式集合**

```java
import org.redisson.api.*;

// 分布式 Map
RMap<String, User> userMap = RedissonUtil.getMap("users");
userMap.put("user1", new User("Alice", 25));

// 分布式 Set
RSet<String> tags = RedissonUtil.getSet("article:tags");
tags.add("java");
tags.add("redis");

// 分布式队列
RQueue<Task> taskQueue = RedissonUtil.getQueue("tasks");
taskQueue.offer(new Task("Process payment"));
Task task = taskQueue.poll();
```

> 📖 **更多示例**：查看 [Redis 工具完整文档](docs/redis/README.md)

---

## 💻 使用示例

### 综合示例：单文件完整的 CRUD 服务

这是一个完整的例子，展示了如何在单个文件中构建一个包含数据库操作和 HTTP API 的微服务：

```java
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.*;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil;
import java.util.*;

public class CompleteCRUDService {

    // ========== Controller ==========
    @PostmanController("/api")
    static class UserController extends BaseController {
        private final UserService service = new UserService();

        @PostmanMapping(value = "/users", method = RequestMethod.GET)
        public Result<List<Map<String, Object>>> listUsers() {
            return Result.ok(service.getAllUsers());
        }

        @PostmanMapping(value = "/users/{id}", method = RequestMethod.GET)
        public Result<Map<String, Object>> getUser() {
            Long id = Long.parseLong(getParam("id"));
            return Result.ok(service.getUserById(id));
        }

        @PostmanMapping(value = "/users", method = RequestMethod.POST)
        public Result<String> createUser() {
            UserDTO dto = getBody(UserDTO.class);
            boolean success = service.createUser(dto);
            return success ? Result.ok("创建成功") : Result.error("创建失败");
        }

        @PostmanMapping(value = "/users/{id}", method = RequestMethod.DELETE)
        public Result<String> deleteUser() {
            Long id = Long.parseLong(getParam("id"));
            boolean success = service.deleteUser(id);
            return success ? Result.ok("删除成功") : Result.error("删除失败");
        }
    }

    // ========== Service ==========
    static class UserService {
        public List<Map<String, Object>> getAllUsers() {
            return DatabaseUtil.selectList("SELECT * FROM users");
        }

        public Map<String, Object> getUserById(Long id) {
            return DatabaseUtil.selectOne("SELECT * FROM users WHERE id = ?", id);
        }

        public boolean createUser(UserDTO dto) {
            int rows = DatabaseUtil.insert(
                "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
                dto.name, dto.age, dto.email
            );
            return rows > 0;
        }

        public boolean deleteUser(Long id) {
            int rows = DatabaseUtil.delete("DELETE FROM users WHERE id = ?", id);
            return rows > 0;
        }
    }

    // ========== DTO ==========
    static class UserDTO {
        public String name;
        public Integer age;
        public String email;
    }

    // ========== 启动入口 ==========
    public static void main(String[] args) {
        // 初始化数据库
        DatabaseUtil.registerDatabase("mydb", new DatabaseUtil.DatabaseConfig(
            "jdbc:mysql://localhost:3306/test",
            "root",
            "password",
            "com.mysql.cj.jdbc.Driver"
        ));
        DatabaseUtil.setDefaultDatabase("mydb");

        // 启动 HTTP 服务器
        PostmanUtil.setPort(8080);
        PostmanUtil.HttpCore.startServer(UserController.class);

        System.out.println("✅ 服务启动成功！");
        System.out.println("📍 访问: http://localhost:8080/api/users");
    }
}
```

**测试 API**

```bash
# 查询所有用户
curl http://localhost:8080/api/users

# 查询单个用户
curl http://localhost:8080/api/users/1

# 创建用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","age":25,"email":"alice@example.com"}'

# 删除用户
curl -X DELETE http://localhost:8080/api/users/1
```

---

## 🛠️ 技术栈

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **核心框架** | Spring Boot | 2.5.14 | 依赖管理 |
| **Java** | JDK | 8+ | 运行环境 |
| **数据库连接池** | HikariCP | 4.0.3 | 高性能连接池 |
| **数据库驱动** | MySQL Connector | 8.0.28 | MySQL 驱动 |
| **SQL 解析** | JSqlParser | 4.2 | SQL 解析器 |
| **Redis 客户端** | Lettuce | 6.1.8 | 异步 Redis 客户端 |
| **Redis 框架** | Redisson | 3.17.7 | 分布式锁和对象 |
| **JSON 处理** | Jackson | 2.12.7 | JSON 序列化 |
| **工具库** | Hutool | 5.7.22 | Java 工具集 |
| **代码简化** | Lombok | 1.18.24 | 注解处理器 |

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！无论是报告 bug、提交功能请求，还是直接提交代码。

### 如何贡献？

1. **Fork** 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 **Pull Request**

### 报告问题

如果你发现了 bug 或有功能建议，请：

1. 访问 [Issues 页面](https://github.com/halavah/libra-spring-boot-starter-toolkit/issues)
2. 点击 **New Issue**
3. 选择合适的模板（Bug Report 或 Feature Request）
4. 填写详细信息

### 代码规范

- 使用 **4 个空格**缩进
- 遵循 **阿里巴巴 Java 开发手册**
- 添加必要的**注释**和 **Javadoc**
- 确保所有**测试通过**

---

## 📄 开源协议

本项目采用 **AGPL-3.0 License** 开源协议。

### ⚠️ 重要声明

根据 **AGPL-3.0** 协议：

- ✅ **允许**：商业使用、修改、分发、专利使用
- ⚠️ **要求**：
  - **源代码公开**：修改后的代码必须开源
  - **协议传递**：衍生作品必须使用相同协议
  - **网络服务开源**：如果将本项目用于网络服务（SaaS），必须向用户提供源代码

### 📖 协议解读

AGPL-3.0 是 GPL-3.0 的加强版，特别强调了**网络服务**的开源要求：

> 如果你修改了本项目，并将其作为**网络服务**（如 Web 应用、API 服务）提供给用户，你必须向用户提供修改后的源代码。

这意味着：

- ❌ **不允许**：将本项目修改后作为闭源 SaaS 服务出售
- ✅ **允许**：作为内部工具使用（无需开源）
- ✅ **允许**：作为开源项目的一部分使用

### 💡 为什么选择 AGPL-3.0？

我们选择 AGPL-3.0 是为了：

1. **保护开源精神**：确保所有改进都能回馈社区
2. **防止闭源滥用**：防止他人将本项目闭源商业化
3. **鼓励协作**：促进开发者之间的合作与分享

如果你对协议有任何疑问，欢迎通过 [Issues](https://github.com/halavah/libra-spring-boot-starter-toolkit/issues) 联系我们。

> 📄 **完整协议**：查看 [LICENSE](LICENSE) 文件

---

## 📮 联系我们

### 🙋 开发团队

- **项目维护者**：Halavah
- **GitHub**：[@halavah](https://github.com/halavah)
- **邮箱**：halavah@126.com

### 🔗 相关链接

- **项目主页**：[https://github.com/halavah/libra-spring-boot-starter-toolkit](https://github.com/halavah/libra-spring-boot-starter-toolkit)
- **问题反馈**：[Issues](https://github.com/halavah/libra-spring-boot-starter-toolkit/issues)
- **讨论区**：[Discussions](https://github.com/halavah/libra-spring-boot-starter-toolkit/discussions)
- **发布记录**：[Releases](https://github.com/halavah/libra-spring-boot-starter-toolkit/releases)

### 💬 社区支持

如果你在使用过程中遇到问题：

1. 📖 查看 [文档](docs/)
2. 🔍 搜索 [已关闭的 Issues](https://github.com/halavah/libra-spring-boot-starter-toolkit/issues?q=is%3Aissue+is%3Aclosed)
3. 💬 在 [Discussions](https://github.com/halavah/libra-spring-boot-starter-toolkit/discussions) 发起讨论
4. 🐛 [提交新的 Issue](https://github.com/halavah/libra-spring-boot-starter-toolkit/issues/new)

---

## ⭐ Star History

如果这个项目对你有帮助，请给我们一个 ⭐ Star！你的支持是我们最大的动力！

[![Star History Chart](https://api.star-history.com/svg?repos=halavah/libra-spring-boot-starter-toolkit&type=Date)](https://star-history.com/#halavah/libra-spring-boot-starter-toolkit&Date)

---

---

<div align="center">

**用 ❤️ 构建 | Made with ❤️**

如果这个项目帮助到了你，请考虑给我们一个 ⭐ Star

[⬆ 回到顶部](#-libra-spring-boot-starter-toolkit)

</div>

