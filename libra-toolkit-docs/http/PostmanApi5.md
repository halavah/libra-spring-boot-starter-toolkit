# PostmanUtil 实战指南

- [1. 请求控制器](#1-请求控制器)
  - [1.1. 核心方法简介](#11-核心方法简介)
  - [1.2. 核心方法详解](#12-核心方法详解)
  - [1.3 示例Controller代码](#13-示例controller代码)
- [2. API接口测试示例](#2-api接口测试示例)
  - [2.1. 接口1: `GET /demo/query`](#21-接口1-get-demoquery)
  - [2.2. 接口2: `POST /demo/create-user`](#22-接口2-post-democreate-user)
  - [2.3. 接口3: `PUT /demo/update-settings`](#23-接口3-put-demoupdate-settings)
  - [2.4. 接口4: `PATCH /demo/raw-patch`](#24-接口4-patch-demoraw-patch)
- [3. 常用框架API配置与使用](#3-常用框架api配置与使用)
  - [3.1. 服务器配置](#31-服务器配置)
  - [3.2. 控制器注册](#32-控制器注册)
  - [3.3. 认证配置与使用](#33-认证配置与使用)
  - [3.4. 构建标准响应](#34-构建标准响应)

---

本文档提供了一系列具体的代码示例，旨在演示如何利用 `PostmanUtil` 的 `BaseController` 及其辅助方法，优雅地处理不同HTTP请求方法（GET, POST, PUT, PATCH）的参数。

## 1. 请求控制器

### 1.1. 核心方法简介

- GET: 使用 `getParams()` 获取URL查询参数。
- POST: 使用 `getBody(UserDTO.class)` 将JSON自动映射到DTO。
- PUT: 使用 `getBody(Map.class)` 将JSON映射到通用的Map。
- PATCH: 使用 `getRequestBody()` 获取原始请求体字符串。

### 1.2. 核心方法详解

- **`getParams()`**: 用于获取 `GET` 或 `DELETE` 请求中通过 URL 查询字符串 (`?key=value&...`) 传递的参数。返回一个 `Map<String, String>`。
- **`getBody(Class<T> clazz)`**: 用于获取 `POST`, `PUT`, `PATCH` 请求的 `application/json` 请求体，并将其自动反序列化为指定的Java对象（DTO或Map）。
- **`getRequestBody()`**: 用于获取 `POST`, `PUT`, `PATCH` 请求的原始请求体字符串，适用于需要手动解析或处理非JSON格式数据的场景。

### 1.3 示例Controller代码

为了演示这些功能，我们创建一个名为 `ParameterDemoController` 的单文件微服务。您可以直接运行其 `main` 方法来启动并测试所有接口。

```java
package cn.myslayers.app.controller;

import cn.myslayers.app.utils.PostmanUtil;
import cn.myslayers.app.utils.PostmanUtil.*;
import java.io.Serializable;
import java.util.Map;

/**
 * @title ParameterDemoController
 * @description 演示如何使用BaseController处理不同类型的HTTP请求参数。
 */
public class ParameterDemoController {

    public static void main(String[] args) {
        System.out.println("=== 参数演示服务器启动 ===");
        PostmanUtil.setPort(5177);
        PostmanUtil.setStartupLogEnabled(true);
        PostmanUtil.setRequestDetailLogEnabled(true);
        HttpCore.startServer(DemoEndpoint.class);
    }

    @PostmanController("/demo")
    public static class DemoEndpoint extends BaseController {

        /**
         * 1. GET 请求：处理URL查询参数
         * - 使用 getParams() 获取所有查询参数。
         */
        @PostmanMapping(value = "/query", method = RequestMethod.GET)
        public Result<Map<String, String>> handleGetQuery() {
            Map<String, String> params = getParams();
            System.out.println("收到的GET参数: " + params);
            return Result.ok(params);
        }

        /**
         * 2. POST 请求：自动映射到DTO对象
         * - 使用 getBody(UserDTO.class) 将JSON请求体直接转换为UserDTO对象。
         */
        @PostmanMapping(value = "/create-user", method = RequestMethod.POST)
        public Result<UserDTO> handlePostWithDTO() {
            UserDTO user = getBody(UserDTO.class);
            System.out.println("创建新用户: " + user.getName() + ", 年龄: " + user.getAge());
            // 可以在此调用Service层进行持久化...
            user.setId("user-" + System.currentTimeMillis());
            return Result.ok(user);
        }

        /**
         * 3. PUT 请求：映射到通用的Map对象
         * - 使用 getBody(Map.class) 处理结构灵活或未知的JSON。
         */
        @PostmanMapping(value = "/update-settings", method = RequestMethod.PUT)
        public Result<Map> handlePutWithMap() {
            Map<String, Object> settings = getBody(Map.class);
            System.out.println("更新设置: " + settings);
            // 可以在此根据Map中的key-value对动态更新配置...
            settings.put("status", "updated");
            return Result.ok(settings);
        }

        /**
         * 4. PATCH 请求：获取原始请求体
         * - 使用 getRequestBody() 获取未经处理的原始字符串。
         * - 适用于非JSON格式（如XML, CSV）或需要手动处理JSON的场景。
         */
        @PostmanMapping(value = "/raw-patch", method = RequestMethod.PATCH)
        public Result<String> handlePatchWithRawBody() {
            String rawBody = getRequestBody();
            System.out.println("收到原始PATCH数据: " + rawBody);
            // 假设我们只是记录日志并返回确认信息
            String responseMessage = "已收到您的原始数据，长度为: " + rawBody.length();
            return Result.ok(responseMessage);
        }
    }

    /**
     * 用于演示的DTO对象
     */
    public static class UserDTO implements Serializable {
        private String id;
        private String name;
        private int age;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
```

---

## 2. API接口测试示例

您可以使用 `curl` 或任何API测试工具（如 Postman）来调用这些接口。

### 2.1. 接口1: `GET /demo/query`

**功能**: 测试获取URL查询参数。

**Curl 命令**:
```bash
curl -X GET "http://localhost:5177/demo/query?name=Alice&role=admin&status=active"
```

**预期响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {
    "name": "Alice",
    "role": "admin",
    "status": "active"
  },
  "timestamp": ...
}
```

### 2.2. 接口2: `POST /demo/create-user`

**功能**: 测试JSON请求体到DTO的自动映射。

**Curl 命令**:
```bash
curl -X POST http://localhost:5177/demo/create-user \
-H "Content-Type: application/json" \
-d '{
  "name": "Bob",
  "age": 30
}'
```

**预期响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {
    "id": "user-...",
    "name": "Bob",
    "age": 30
  },
  "timestamp": ...
}
```

### 2.3. 接口3: `PUT /demo/update-settings`

**功能**: 测试JSON请求体到通用 `Map` 的映射。

**Curl 命令**:
```bash
curl -X PUT http://localhost:5177/demo/update-settings \
-H "Content-Type: application/json" \
-d '{
  "theme": "dark",
  "notifications": {
    "email": true,
    "sms": false
  },
  "timeout": 3600
}'
```

**预期响应**:
```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {
    "theme": "dark",
    "notifications": {
      "email": true,
      "sms": false
    },
    "timeout": 3600,
    "status": "updated"
  },
  "timestamp": ...
}
```

### 2.4. 接口4: `PATCH /demo/raw-patch`

**功能**: 测试获取原始请求体字符串。

**Curl 命令**:
```bash
# 示例1: 发送一个简单的字符串
curl -X PATCH http://localhost:5177/demo/raw-patch \
-H "Content-Type: text/plain" \
-d "This is a raw text payload."

# 示例2: 发送一个XML片段
curl -X PATCH http://localhost:5177/demo/raw-patch \
-H "Content-Type: application/xml" \
-d "<update><field name='price'>99.99</field></update>"
```

**预期响应 (示例1)**:
```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": "已收到您的原始数据，长度为: 27",
  "timestamp": ...
}
```

---

## 3. 常用框架API配置与使用

除了处理请求参数，`PostmanUtil` 还提供了一套丰富的API用于配置服务器行为、注册控制器和管理认证。

### 3.1. 服务器配置

在启动服务器之前，您可以通过一系列静态方法来定制服务器的行为。

```java
public static void main(String[] args) {
    // 设置服务器监听的端口
    PostmanUtil.setPort(8080);

    // 开启核心调试日志 (服务器启动、扫描等)
    PostmanUtil.setDebugLogEnabled(true);

    // 开启启动时在控制台打印已注册的接口列表
    PostmanUtil.setStartupLogEnabled(true);

    // 开启详细的请求日志 (包括curl命令、出入参等)
    PostmanUtil.setRequestDetailLogEnabled(true);

    // ... 启动服务器 ...
}
```

### 3.2. 控制器注册

`PostmanUtil` 支持两种灵活的控制器注册方式：

#### 方式一：通过包扫描自动注册 (推荐用于大型项目)

使用 `ManualRegistry.scan()` 指定一个或多个包路径，`PostmanUtil` 会自动查找并注册所有被 `@PostmanController` 注解的类。

```java
public static void main(String[] args) {
    // 指定扫描 'cn.myslayers.app.controller' 包下的所有控制器
    ManualRegistry.scan("cn.myslayers.app.controller");

    // 启动服务器，此时会自动加载扫描到的所有控制器
    HttpCore.startServer();
}
```

#### 方式二：手动指定类注册 (推荐用于单文件微服务或测试)

直接在 `startServer()` 方法中传入一个或多个具体的Controller类。这种方式可以精确控制本次启动加载的接口，非常适合快速测试和独立功能开发。

```java
public static void main(String[] args) {
    // 启动服务器，并只注册指定的这一个控制器
    HttpCore.startServer(MySpecificController.class);
}
```

### 3.3. 认证配置与使用

`PostmanUtil` 内置了两种常用的认证策略：Bearer Token 和 HMAC 签名。

#### 步骤1：配置密钥

在启动服务器前，设置认证所需的密钥。

```java
public static void main(String[] args) {
    // 设置有效的 Bearer Token
    AuthManager.setBearerToken("my-super-secret-token-12345");

    // 设置用于HMAC签名的共享密钥
    AuthManager.setSignatureSecret("my-hmac-signature-secret-key");

    // ... 启动服务器 ...
}
```

#### 步骤2：在接口上启用认证

使用 `@RequiresAuth` 注解来保护需要认证的接口。可以指定一种或多种认证策略。

```java
@PostmanController("/secure")
public static class SecureEndpoint extends BaseController {

    // 此接口仅需要有效的 Bearer Token 即可访问
    @PostmanMapping(value = "/data", method = RequestMethod.GET)
    @RequiresAuth(AuthManager.BearerTokenStrategy.class)
    public Result<String> getSensitiveData() {
        return Result.ok("这是受保护的敏感数据。");
    }

    // 此接口需要同时通过 Bearer Token 和 HMAC 签名两种认证
    @PostmanMapping(value = "/critical-action", method = RequestMethod.POST)
    @RequiresAuth({AuthManager.BearerTokenStrategy.class, AuthManager.SignatureStrategy.class})
    public Result<String> performCriticalAction() {
        // ... 执行关键操作 ...
        return Result.ok("关键操作已成功执行。");
    }
}
```

### 3.4. 构建标准响应

使用内置的 `Result<T>` 类来构建统一、规范的JSON响应。

- **成功响应**: `Result.ok(data)`
- **失败响应**: `Result.error(code, message)`

```java
@PostmanMapping(value = "/user/{id}", method = RequestMethod.GET)
public Result<UserDTO> getUserById() {
    String userId = getParams().get("id");
    UserService userService = new UserService();
    UserDTO user = userService.findById(userId);

    if (user != null) {
        // 如果找到用户，返回成功响应，并将用户信息作为数据负载
        return Result.ok(user);
    } else {
        // 如果未找到用户，返回一个带有404状态码和错误消息的失败响应
        return Result.error(404, "用户未找到，ID: " + userId);
    }
}
