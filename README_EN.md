<div align="center">

# 🚀 Libra Spring Boot Starter Toolkit

**A Powerful Java Utility Toolkit to Simplify Your Development**

[![License](https://img.shields.io/badge/license-AGPL--3.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.5.14-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![GitHub release](https://img.shields.io/github/v/release/halavah/libra-spring-boot-starter-toolkit)](https://github.com/halavah/libra-spring-boot-starter-toolkit/releases)
[![GitHub stars](https://img.shields.io/github/stars/halavah/libra-spring-boot-starter-toolkit?style=social)](https://github.com/halavah/libra-spring-boot-starter-toolkit)

**English** | [简体中文](README.md)

</div>

> **This README follows the open-source documentation spec**: uniform structure, badges to public resources only.


---

## 📖 Table of Contents

- [Introduction](#-introduction)
- [Core Features](#-core-features)
- [Quick Start](#-quick-start)
- [Modules](#-modules)
  - [Database Util](#1-database-util-databaseutil)
  - [HTTP Server Util](#2-http-server-util-postmanutil)
  - [Redis Util](#3-redis-util-redissonutil)
- [Tech Stack](#-tech-stack)
- [Contributing](#-contributing)
- [License](#-license)
- [Contact](#-contact)

---

## 🎯 Introduction

**Libra Spring Boot Starter Toolkit** is an efficient and easy-to-use Java utility package designed to simplify common tasks in daily development. Whether you're building enterprise applications or rapid prototypes, this toolkit provides powerful support.

### 💡 What Problems Does It Solve?

- ❌ **Tedious Database Operations**: Manually managing connection pools and writing repetitive JDBC code?
- ❌ **Complex HTTP Service Setup**: Need Spring Boot just to run a simple REST API?
- ❌ **Confusing Redis Clients**: Struggling to choose between Lettuce and Redisson?

### ✅ Our Solutions

- ✅ **Unified Database Utilities**: Complete CRUD with one line of code, supporting transactions, pagination, and batch operations
- ✅ **Lightweight HTTP Server**: Start REST APIs in a single file without Spring Boot
- ✅ **Dual-Engine Redis Utilities**: Perfect integration of Lettuce + Redisson for both data operations and distributed locks

---

## ⭐ Core Features

### 🔥 Simple & Easy to Use

```java
// Database query in one line
List<Map<String, Object>> users = DatabaseUtil.selectList("SELECT * FROM users WHERE age > ?", 18);

// Start HTTP server in two lines
PostmanUtil.setPort(8080);
PostmanUtil.HttpCore.startServer(UserController.class);

// Redis operations, simple and direct
RedissonUtil.set("key", "value");
String value = RedissonUtil.get("key");
```

### ⚡ High Performance

- 🚀 **HikariCP Connection Pool**: Industry's fastest database connection pool
- 🚀 **Lettuce Redis Client**: High-performance async client based on Netty
- 🚀 **Zero-Dependency HTTP Server**: Based on Java native HttpServer, fast startup

### 🛡️ Production-Ready

- ✅ Comprehensive exception handling
- ✅ Thread-safe design
- ✅ Detailed logging
- ✅ Extensive unit tests

---

## 🚀 Quick Start

### Prerequisites

- **Java 8** or higher
- **Maven 3.6+** or **Gradle 6.0+**

### 1. Add Dependency

#### Maven

Add repository configuration to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/halavah/libra-spring-boot-starter-toolkit</url>
    </repository>
</repositories>
```

Configure GitHub authentication in `~/.m2/settings.xml` (GitHub Packages requires authentication for downloading):

```xml
<servers>
    <server>
        <id>github</id>
        <username>YourGitHubUsername</username>
        <password>YourGitHubToken</password>
    </server>
</servers>
```

> **Tip**: How to create a GitHub Token?
> 1. Visit [GitHub Settings → Developer settings → Personal access tokens](https://github.com/settings/tokens)
> 2. Click **Generate new token (classic)**
> 3. Check `read:packages` permission
> 4. Generate and copy the token

Add dependency:

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

### 2. Quick Demo

Create a simple database query example:

```java
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil;
import java.util.List;
import java.util.Map;

public class QuickStart {
    public static void main(String[] args) {
        // Register datasource
        DatabaseUtil.registerDatabase("mydb",
            new DatabaseUtil.DatabaseConfig(
                "jdbc:mysql://localhost:3306/test",
                "root",
                "password",
                "com.mysql.cj.jdbc.Driver"
            )
        );
        DatabaseUtil.setDefaultDatabase("mydb");

        // Query data
        List<Map<String, Object>> users = DatabaseUtil.selectList(
            "SELECT * FROM users WHERE age > ?", 18
        );

        // Print results
        users.forEach(System.out::println);
    }
}
```

---

## 📦 Modules

### 1. Database Util (DatabaseUtil)

> 💾 **Say Goodbye to Tedious JDBC Code, One Line Does It All**

#### 🌟 Core Features

- ✅ **Multi-Datasource Management**: Support connecting to multiple databases simultaneously
- ✅ **HikariCP Connection Pool**: Excellent performance, simple configuration
- ✅ **Simplified CRUD Operations**: `selectList()`, `insert()`, `update()`, `delete()`
- ✅ **Transaction Management**: Declarative transactions with automatic rollback
- ✅ **Batch Operations**: Efficient batch insert, update, and delete
- ✅ **Physical Pagination**: Support for MySQL, PostgreSQL, Oracle, etc.
- ✅ **Dynamic SQL**: Support MyBatis-style XML SQL
- ✅ **Object Mapping**: Automatically map query results to Java objects

#### 💡 Use Cases

- ✅ Data access layer for enterprise applications
- ✅ Data migration scripts
- ✅ Batch data processing
- ✅ Rapid prototyping

#### 📚 Quick Examples

**Basic Queries**

```java
// Query list
List<Map<String, Object>> users = DatabaseUtil.selectList(
    "SELECT * FROM users WHERE age > ?", 18
);

// Query single record
Map<String, Object>> user = DatabaseUtil.selectOne(
    "SELECT * FROM users WHERE id = ?", 1
);

// Query count
Long count = DatabaseUtil.selectCount(
    "SELECT COUNT(*) FROM users WHERE status = ?", "active"
);
```

**Insert, Update, Delete**

```java
// Insert
int rows = DatabaseUtil.insert(
    "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
    "Alice", 25, "alice@example.com"
);

// Update
int updated = DatabaseUtil.update(
    "UPDATE users SET email = ? WHERE id = ?",
    "newemail@example.com", 1
);

// Delete
int deleted = DatabaseUtil.delete(
    "DELETE FROM users WHERE id = ?", 1
);
```

**Transaction Management**

```java
// Declarative transaction
Boolean success = DatabaseUtil.executeInTransaction(connection -> {
    // All operations in the same transaction
    DatabaseUtil.insert("INSERT INTO users (name, age) VALUES (?, ?)", "Bob", 30);
    DatabaseUtil.update("UPDATE accounts SET balance = balance - 100 WHERE user_id = ?", 1);
    return true; // Return true to commit, false or throw exception to rollback
});
```

> 📖 **More Examples**: See [Database Util Complete Documentation](docs/database/README.md)

---

### 2. HTTP Server Util (PostmanUtil)

> 🌐 **Start REST APIs in a Single File Without Spring Boot**

#### 🌟 Core Features

- ✅ **Annotation-Driven Routing**: `@PostmanController`, `@PostmanMapping`
- ✅ **Auto Package Scanning**: No manual controller registration needed
- ✅ **Unified Response Format**: `Result<T>` encapsulates response data
- ✅ **Request Parameter Parsing**: Auto-parse Query, Path, and Body parameters
- ✅ **Authentication Mechanisms**: Support Bearer Token, HMAC Signature
- ✅ **Request Logging**: Auto-log request info and curl commands
- ✅ **BaseController**: Elegant access to request context

#### 💡 Use Cases

- ✅ Rapid prototyping
- ✅ Single-file microservices
- ✅ Standalone tool services
- ✅ Quick verification in test environments
- ⚠️ **Not Recommended**: Large production applications (use Spring Boot instead)

#### 📚 Quick Examples

**Simple REST API**

```java
import cn.myslayers.libra.framework.toolkit.http.PostmanUtil.*;

// 1. Define controller
@PostmanController("/api")
public class UserController extends BaseController {

    @PostmanMapping(value = "/users", method = RequestMethod.GET)
    public Result<List<User>> getUsers() {
        String age = getParam("age");
        List<User> users = userService.findByAge(Integer.parseInt(age));
        return Result.ok(users);
    }

    @PostmanMapping(value = "/users", method = RequestMethod.POST)
    public Result<User> createUser() {
        UserDTO dto = getBody(UserDTO.class);
        User user = userService.create(dto);
        return Result.ok(user);
    }
}

// 2. Start server
public class Application {
    public static void main(String[] args) {
        PostmanUtil.setPort(8080);
        PostmanUtil.HttpCore.startServer(UserController.class);
        System.out.println("Server started at http://localhost:8080");
    }
}
```

> 📖 **More Examples**: See [HTTP Server Complete Documentation](docs/http/README.md)

---

### 3. Redis Util (RedissonUtil)

> 🔴 **Dual-Engine Redis Utility, Perfect Integration of Lettuce + Redisson**

#### 🌟 Core Features

- ✅ **Lettuce Engine**: For data operations (String, Hash, List, Set, ZSet)
- ✅ **Redisson Engine**: For distributed locks and objects
- ✅ **Unified API Design**: One interface, two engines
- ✅ **Connection Pool Management**: Auto-manage connection pools
- ✅ **Multi-Mode Support**: Standalone, Cluster, Sentinel
- ✅ **Batch Operations**: Pipeline batch execution
- ✅ **Pub/Sub**: Message notification
- ✅ **Distributed Locks**: Fair locks, read-write locks, RedLock
- ✅ **Distributed Collections**: Map, Set, List, Queue

#### 💡 Use Cases

- ✅ Cache management
- ✅ Distributed locks
- ✅ Distributed counters
- ✅ Rate limiters
- ✅ Session storage
- ✅ Message queues

#### 📚 Quick Examples

**Basic String Operations**

```java
import cn.myslayers.libra.framework.toolkit.redis.RedissonUtil;

// Initialize connection
RedissonUtil.init("redis://localhost:6379");

// String operations
RedissonUtil.set("user:1:name", "Alice");
RedissonUtil.setex("session:abc", 3600, "session-data");  // With expiration

String name = RedissonUtil.get("user:1:name");

// Numeric operations
RedissonUtil.incr("page:views");
RedissonUtil.incrBy("score", 10);

// Delete
RedissonUtil.del("user:1:name");
```

**Distributed Lock**

```java
import org.redisson.api.RLock;

// Get lock
RLock lock = RedissonUtil.getLock("order:lock:12345");

try {
    // Try to acquire lock, wait up to 10 seconds, auto-release after 30 seconds
    if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
        try {
            // Business logic
            System.out.println("Processing order...");
        } finally {
            lock.unlock();  // Release lock
        }
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

> 📖 **More Examples**: See [Redis Util Complete Documentation](docs/redis/README.md)

---

## 🛠️ Tech Stack

| Component | Technology | Version | Description |
|-----------|------------|---------|-------------|
| **Core Framework** | Spring Boot | 2.5.14 | Dependency management |
| **Java** | JDK | 8+ | Runtime environment |
| **Connection Pool** | HikariCP | 4.0.3 | High-performance pool |
| **Database Driver** | MySQL Connector | 8.0.28 | MySQL driver |
| **SQL Parser** | JSqlParser | 4.2 | SQL parser |
| **Redis Client** | Lettuce | 6.1.8 | Async Redis client |
| **Redis Framework** | Redisson | 3.17.7 | Distributed locks/objects |
| **JSON Processing** | Jackson | 2.12.7 | JSON serialization |
| **Utility Library** | Hutool | 5.7.22 | Java utilities |
| **Code Simplification** | Lombok | 1.18.24 | Annotation processor |

---

## 🤝 Contributing

We welcome all forms of contributions! Whether reporting bugs, submitting feature requests, or directly contributing code.

### How to Contribute?

1. **Fork** this repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a **Pull Request**

### Report Issues

If you find a bug or have a feature suggestion:

1. Visit [Issues Page](https://github.com/halavah/libra-spring-boot-starter-toolkit/issues)
2. Click **New Issue**
3. Select appropriate template (Bug Report or Feature Request)
4. Fill in detailed information

### Code Standards

- Use **4 spaces** for indentation
- Follow **Alibaba Java Coding Guidelines**
- Add necessary **comments** and **Javadoc**
- Ensure all **tests pass**

---

## 📄 License

This project is licensed under the **AGPL-3.0 License**.

### ⚠️ Important Notice

Under the **AGPL-3.0** license:

- ✅ **Permitted**: Commercial use, modification, distribution, patent use
- ⚠️ **Required**:
  - **Source Code Disclosure**: Modified code must be open-sourced
  - **License Propagation**: Derivative works must use the same license
  - **Network Service Disclosure**: If using this project for network services (SaaS), you must provide source code to users

### 📖 License Interpretation

AGPL-3.0 is an enhanced version of GPL-3.0, emphasizing **network service** open-source requirements:

> If you modify this project and provide it as a **network service** (such as web applications, API services) to users, you must provide the modified source code to users.

This means:

- ❌ **Not Allowed**: Selling this project as closed-source SaaS after modification
- ✅ **Allowed**: Using as an internal tool (no need to open-source)
- ✅ **Allowed**: Using as part of an open-source project

### 💡 Why Choose AGPL-3.0?

We chose AGPL-3.0 to:

1. **Protect Open-Source Spirit**: Ensure all improvements can be contributed back to the community
2. **Prevent Closed-Source Abuse**: Prevent others from commercializing this project as closed-source
3. **Encourage Collaboration**: Promote cooperation and sharing among developers

If you have any questions about the license, feel free to contact us through [Issues](https://github.com/halavah/libra-spring-boot-starter-toolkit/issues).

> 📄 **Full License**: See [LICENSE](LICENSE) file

---

## 📮 Contact

### 🙋 Development Team

- **Maintainer**: Halavah
- **GitHub**: [@halavah](https://github.com/halavah)
- **Email**: halavah@126.com

### 🔗 Links

- **Project Home**: [https://github.com/halavah/libra-spring-boot-starter-toolkit](https://github.com/halavah/libra-spring-boot-starter-toolkit)
- **Issue Tracker**: [Issues](https://github.com/halavah/libra-spring-boot-starter-toolkit/issues)
- **Discussions**: [Discussions](https://github.com/halavah/libra-spring-boot-starter-toolkit/discussions)
- **Release Notes**: [Releases](https://github.com/halavah/libra-spring-boot-starter-toolkit/releases)

### 💬 Community Support

If you encounter problems during use:

1. 📖 Check [Documentation](docs/)
2. 🔍 Search [Closed Issues](https://github.com/halavah/libra-spring-boot-starter-toolkit/issues?q=is%3Aissue+is%3Aclosed)
3. 💬 Start a discussion in [Discussions](https://github.com/halavah/libra-spring-boot-starter-toolkit/discussions)
4. 🐛 [Submit a new Issue](https://github.com/halavah/libra-spring-boot-starter-toolkit/issues/new)

---

## ⭐ Star History

If this project helps you, please give us a ⭐ Star! Your support is our greatest motivation!

[![Star History Chart](https://api.star-history.com/svg?repos=halavah/libra-spring-boot-starter-toolkit&type=Date)](https://star-history.com/#halavah/libra-spring-boot-starter-toolkit&Date)

---

<div align="center">

**Built with ❤️ | 用 ❤️ 构建**

If this project helped you, please consider giving us a ⭐ Star

[⬆ Back to Top](#-libra-spring-boot-starter-toolkit)

</div>

