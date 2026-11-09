# PostmanUtil 轻量级HTTP服务器框架 - 技术说明文档

## 目录
- [1. 概述 (Overview)](#1-概述-overview)
- [2. 核心组件解析 (Core Components)](#2-核心组件解析-core-components)
  - [`LoggerCore` - 日志系统](#loggercore---日志系统)
    - [介绍](#loggercore-介绍)
    - [不同日志的开关](#loggercore-不同日志的开关)
  - [`Result<T>` - 统一响应模型](#resultt---统一响应模型)
    - [介绍](#resultt-介绍)
    - [根据return响应](#resultt-根据return响应)
  - [`HttpCore` - HTTP服务核心](#httpcore---http服务核心)
    - [介绍](#httpcore-介绍)
    - [动态参数注入](#httpcore-动态参数注入)
    - [源码定位与调试友好](#httpcore-源码定位与调试友好)
    - [多源注册与优先级](#httpcore-多源注册与优先级)
    - [安全的端口强制关闭机制](#httpcore-安全的端口强制关闭机制)
  - [`HandlerCore` - 业务处理器核心](#handlercore---业务处理器核心)
    - [介绍](#handlercore-介绍)
    - [线程安全](#handlercore-线程安全)
    - [性能考量](#handlercore-性能考量)
    - [路径规范化](#handlercore-路径规范化)
    - [默认扫描机制](#handlercore-默认扫描机制)
  - [`AuthManager` - 认证管理器](#authmanager---认证管理器)
    - [介绍](#authmanager-介绍)
    - [`BearerTokenStrategy` 认证规则](#bearertokenstrategy-认证规则)
    - [`SignatureStrategy` 认证规则](#signaturestrategy-认证规则)
    - [`AuthManager` 认证功能使用](#AuthManager-认证功能使用)
  - [`ManualRegistry` - 手动注册器](#manualregistry---手动注册器)
    - [介绍](#manualregistry-介绍)
    - [混合使用与优先级](#manualregistry-混合使用与优先级)
    - [状态的静态性与全局影响](#manualregistry-状态的静态性与全局影响)
    - [与 `startServer` 的关系](#manualregistry-与-startserver-的关系)
---

## 1. 概述 (Overview)

`PostmanUtil` 是一个基于Java内置`HttpServer`构建的、轻量级、零依赖的HTTP服务器框架。其核心设计理念是通过注解驱动的方式，为后端业务模块提供一个快速、独立、可测试的模拟（Mock）和开发环境。

开发者可以为每个业务模块创建独立的Controller类，并通过简单的配置启动一个服务器实例，从而实现并行开发、独立测试和快速原型验证。

**核心特性:**
- **轻量级设计**: 基于JDK内置的`HttpServer`，无需引入Tomcat、Jetty等外部Web服务器，依赖少，启动快。
- **注解驱动**: 使用 `@PostmanController` 和 `@PostmanMapping` 注解，以声明式的方式定义API接口，代码更清晰。
- **自动扫描**: 借助 `org.reflections` 库，能够自动扫描指定包路径下的Controller并注册路由。
- **优雅的参数获取**: 通过 `BaseController` 和 `RequestContextHolder`，实现了Controller方法与HTTP参数的解耦，代码更整洁。
- **可插拔认证**: 内置Bearer Token和HMAC签名两种认证策略，并可通过 `@RequiresAuth` 注解灵活应用于不同接口。
- **统一响应与日志**: 提供标准化的JSON响应格式和可灵活配置的日志系统。
- **请求追踪**: 自动为每个请求生成等效的`curl`命令，极大地方便了调试和复现。

## 2. 核心组件解析 (Core Components)

### `LoggerCore` - 日志系统

#### LoggerCore 介绍

`LoggerCore` 提供了一个简单但有效的日志记录功能。
- **分级日志**: 提供`log()`方法，支持不同级别的日志记录。
- **动态开关**: 可以通过`setDebugLogEnabled()`, `setStartupLogEnabled()`, `setRequestDetailLogEnabled()`在运行时动态地开启或关闭不同类型的日志。

#### LoggerCore 不同日志的开关

```java
// 开启核心调试日志 (服务器启动、扫描等)
PostmanUtil.setDebugLogEnabled(true);
// 开启启动时打印的接口列表
PostmanUtil.setStartupLogEnabled(true);
// 开启请求详情日志 (curl、出入参等)
PostmanUtil.setRequestDetailLogEnabled(true);
```

### `Result<T>` - 统一响应模型

#### `Result<T>` 介绍

`Result<T>` 定义了所有API响应的标准化数据结构。
- **核心字段**:
  - `success` (boolean): 操作是否成功。
  - `code` (Integer): 状态码（如 200, 401, 500）。
  - `message` (String): 操作结果的文字描述。
  - `result` (T): 泛型字段，用于存放核心的业务数据。
  - `timestamp` (long): 服务器响应时的时间戳。
- **特点**: 提供了丰富的静态工厂方法（如 `ok()`, `error()`）来方便地创建`Result`实例。

#### `Result<T>` 根据return响应

- **智能判断**：当 `PostmanUtil` 接收到 Controller 方法的返回值时，它会首先进行一个智能判断：
  - 检查返回的对象是否是 `PostmanUtil.Result` 的实例。
  - 或者，通过反射检查对象的类名是否包含 "Result" 字符串。
- **直接序列化**：如果满足以上任一条件，`PostmanUtil` 会认为这个对象已经是一个完整的、开发者期望的最终响应体。因此，它会**直接将这个对象序列化为JSON**，不再进行任何额外的包装。
- **兼容原始类型**：如果方法的返回值是一个原始数据类型（如 `List`、`Map` 或 `String`），不满足上述条件，`PostmanUtil` 会保持原有的兼容性行为，使用内置的 `Result.ok(data)` 将其包装，以确保所有接口都有一个基本的、一致的响应结构。
- **最终的响应结构完全由您在 Controller 方法中的 `return` 语句决定**：您可以返回 `PostmanUtil` 内置的 `Result<T>`；您也可以在自己的 Controller 中定义一个内部类 `Result<T>`，并返回它的实例；您甚至可以返回任何其他项目中符合您团队规范的、名为 `Result` 的 DTO 对象

### `HttpCore` - HTTP服务核心

#### HttpCore 介绍

`HttpCore` 是整个框架的引擎，封装了所有与底层HTTP协议相关的功能。
- **服务器生命周期管理**: 负责`HttpServer`的创建、启动 (`startServer()`) 和停止 (`stopServer()`)。
- **请求路由**: 内置一个`RouteHandler`，作为所有HTTP请求的统一入口。它会解析请求路径和方法，并从`HandlerCore`中查找对应的处理器进行分发。
- **请求/响应处理**: 负责解析HTTP请求的参数（Query & Body）、设置CORS跨域头、以及将最终的`Result`对象序列化为JSON字符串并发送给客户端。

#### HttpCore 优雅的参数获取机制

`PostmanUtil` 采用了先进的、基于 `ThreadLocal` 的上下文传递机制，取代了传统的方法参数注入，实现了业务逻辑与HTTP请求细节的完美解耦。

- **`RequestContextHolder`**: 这是实现解耦的核心。在处理每个HTTP请求时，`HttpCore` 会首先解析其查询参数（Query Params）和请求体（Request Body），并将这些信息存入 `RequestContextHolder` 中一个与当前线程绑定的 `ThreadLocal` 变量里。
- **`BaseController`**: 业务Controller通过继承这个基类，可以方便地调用 `getParams()` 和 `getBody(Class<T> clazz)` 方法。这些方法会从 `RequestContextHolder` 中安全地取出当前请求的数据，并进行必要的类型转换（如将JSON字符串反序列化为DTO对象）。
- **无参的Controller方法**: 得益于此机制，Controller中的业务方法（如 `@PostmanMapping` 注解的方法）可以不再定义任何参数，使其签名更干净，更专注于业务逻辑本身。
- **生命周期管理**: `HttpCore` 在请求处理的 `finally` 块中确保调用 `RequestContextHolder.clear()`，从而彻底清理线程变量，有效防止了内存泄漏。

#### HttpCore 源码定位与调试友好

`HandlerCore` 在注册每个接口时，会尝试解析其在`.java`源文件中的**确切行号**。这个信息虽然在最新的日志输出中被简化了，但其底层机制依然存在。
- **实现原理**: 它通过获取类的`ProtectionDomain`来定位到编译后的`.class`文件所在的路径（如 `target/classes`），然后智能地推断出对应的源文件路径（如 `src/main/java`），最后通过逐行读取源文件来匹配方法签名。
- **价值**: 这个功能为实现更高级的开发工具（如IDE插件）打下了基础，未来可以实现点击日志直接跳转到Controller方法定义的功能，极大地提升开发效率。

#### HttpCore 多源注册与优先级

`scanAndRegisterHandlers()` 方法的设计支持从多个来源聚合Controller类，其合并逻辑如下：
- **手动指定的类 (`targetClasses`)**: 通过`HttpCore.startServer(MyController.class)`直接传入的类，拥有最高优先级。
- **手动注册的类 (`ManualRegistry.register()`)**: 其次是使用`ManualRegistry`手动注册的类。
- **包扫描 (`ManualRegistry.scan()`)**: 最后是自动扫描包路径找到的类。

#### HttpCore 安全的端口强制关闭机制
为了提升开发体验，`PostmanUtil` 在启动时会尝试自动关闭任何可能占用预设端口的旧进程。然而，一个简单的“通过端口找进程并杀死”的逻辑存在风险，尤其是在复杂的IDE（如IntelliJ IDEA）环境中，可能会错误地将IDE本身或其关键子进程识别为目标，导致编辑器被强制关闭。

为了解决这个问题，`forceClosePort()` 方法实现了一套更安全、更智能的关闭逻辑：
- **自我识别**: 在执行任何关闭操作前，该方法会首先通过 `java.lang.management.ManagementFactory` 获取**当前正在运行的Java程序自身的进程ID (PID)**。
- **安全检查**: 当通过系统命令（如 `lsof` 或 `netstat`）找到一个或多个占用目标端口的PID后，它不会立即执行关闭操作。相反，它会遍历这些找到的PID，并与当前进程的PID进行比较。
- **精确打击**: **只有当一个PID被确认为不属于当前Java进程时**，`forceClosePort()` 才会执行强制关闭命令（如 `kill -9` 或 `taskkill /F`）。如果找到的PID与当前进程的PID相同，它会跳过该操作并打印一条警告日志。

通过这种“先识别，再比对，后清除”的策略，`PostmanUtil` 确保了它只会终止遗留的、外部的旧服务进程，从而彻底避免了误伤开发工具的风险，保证了开发流程的顺畅和稳定。

### `HttpCore` - 响应流处理

#### `void` 返回类型的特殊处理
`PostmanUtil` 对 Controller 方法的返回类型进行了智能识别。当一个使用 `@PostmanMapping` 注解的方法被声明为 `void` 时，`HttpCore` 会认为开发者将完全接管HTTP响应的构建过程。

在这种情况下，`HttpCore` 在调用完该 `void` 方法后，将**不会**执行任何后续操作——它既不会尝试将返回值包装成 `Result<T>` 对象，也不会发送任何默认的JSON响应。这为实现文件下载、图片预览等需要直接操作输出流（OutputStream）的复杂场景提供了可能性。

#### 通过 `RequestContextHolder` 访问 `HttpExchange`
为了在保持 Controller 方法签名简洁的同时，又能让业务逻辑层（如 Service）访问到原始的HTTP请求/响应对象，`PostmanUtil` 将 `com.sun.net.httpserver.HttpExchange` 对象存入了 `RequestContextHolder`。

- **获取方式**: 在请求处理的任何环节，您都可以通过调用静态方法 `PostmanUtil.RequestContextHolder.getHttpExchange()` 来获取当前线程绑定的 `HttpExchange` 实例。
- **核心用途**: 获取到 `HttpExchange` 后，您可以调用其 `getResponseHeaders()` 方法来设置响应头（如 `Content-Type`, `Content-Disposition`），并调用 `getResponseBody()` 来获取用于写入文件字节的 `OutputStream`。

**示例 (在Service层实现文件下载):**
```java
public void downloadFile(String fileId) {
    // ... 根据 fileId 获取文件数据 ...
    byte[] fileBytes = getFileBytesFromDatabase(fileId);

    // 从上下文获取 HttpExchange
    HttpExchange exchange = PostmanUtil.RequestContextHolder.getHttpExchange();

    try {
        // 设置响应头，告知浏览器这是一个需要下载的附件
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"report.pptx\"");

        // 发送响应头和文件长度
        exchange.sendResponseHeaders(200, fileBytes.length);

        // 获取输出流并写入文件内容
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(fileBytes);
        }
    } catch (IOException e) {
        // 处理IO异常
        throw new RuntimeException("文件下载失败", e);
    }
}
```

### `HandlerCore` - 业务处理器核心

#### HandlerCore 介绍

`HandlerCore` 是框架的路由中心，负责扫描、注册和管理所有的API接口。
- **注解扫描**: `scanAndRegisterHandlers()` 方法是核心，它会扫描指定的类或包路径，查找所有被 `@PostmanController` 注解的类。
- **路由构建**: 对于每个Controller类，它会遍历其所有被 `@PostmanMapping` 注解的方法，并根据Controller的根路径和方法的路径，构建出完整的URL路由（如 `GET:/api/user/detail`）。
- **处理器封装**: 每个找到的接口方法都会被封装成一个`HandlerMethod`对象，其中包含了Controller实例、方法本身、以及源文件行号等元数据，用于后续的调用和日志记录。
- **处理器存储**: 内部使用一个`static final Map`来存储路由键（如`"GET:/api/user/detail"`）与`HandlerMethod`之间的映射关系。

#### HandlerCore 线程安全
-  `handlerMethods` 这个核心的路由映射表被声明为 `static final`，并在 `scanAndRegisterHandlers` 方法开始时进行 `clear()` 和重新填充。
-  这个过程通常在服务器启动的单线程环境中完成，因此避免了并发修改的问题。
-  在服务器运行期间，该 Map 是只读的，因此多线程并发请求的读取操作是线程安全的。

#### HandlerCore 性能考量
- **反射与实例化**: `HandlerCore` 在启动时会通过反射创建所有 Controller 的实例。为了性能，每个 Controller 类只会被实例化**一次**，并在整个服务器生命周期内复用。
- **I/O操作**: 源码行号的解析涉及到文件 I/O 操作，这相对耗时。因此，这个操作也仅在服务器启动时执行一次，其结果会被缓存，不会影响后续请求的处理性能。

#### HandlerCore 路径规范化
- 在构建路由时，`HandlerCore` 会自动处理路径中多余的斜杠。
- 例如，无论您在 `@PostmanController` 和 `@PostmanMapping` 中定义的路径是 `"/api"` 和 `"/user"`，还是 `"/api/"` 和 `"user"`，最终生成的路径都会被规范化为 `/api/user`，确保了路径构建的健壮性。

#### HandlerCore 默认扫描机制
- 如果开发者在启动服务器时，既没有通过 `targetClasses` 指定类，也没有通过 `ManualRegistry` 注册任何类或包，`HandlerCore` 会触发一个**后备（Fallback）机制**：它会默认扫描 `"cn.myslayers"` 这个包路径。
- 这是一个为了方便快速上手而设计的“约定优于配置”的体现，但在生产级或复杂项目中，强烈建议明确指定扫描范围。

### `AuthManager` - 认证管理器

#### AuthManager 介绍

`AuthManager` 提供了灵活的认证机制。
- **职责**:
  - **策略管理**: 定义了`Strategy`接口，所有认证逻辑都需要实现此接口。
  - **内置策略**:
    - **`BearerTokenStrategy`**: 校验HTTP头中的`Authorization: Bearer <token>`。
    - **`SignatureStrategy`**: 基于HMAC-SHA256的请求签名校验，包含时间戳和Nonce防重放机制。
  - **配置**: 提供了`setBearerToken()`和`setSignatureSecret()`方法来设置认证所需的密钥。

#### `BearerTokenStrategy` 认证规则

`BearerTokenStrategy` 是一种简单而广泛使用的认证方式，其核心是验证一个静态的令牌（Token）。

1.  **客户端实现**:
    客户端需要在每次请求的HTTP头中，加入一个 `Authorization` 字段，其值的格式必须为 `Bearer <your-token>`。
    ```
    Authorization: Bearer your-secret-token-here
    ```

2.  **服务器端校验**:
    服务器会提取 `Authorization` 头中 `Bearer ` 后面的令牌部分，并与通过 `AuthManager.setBearerToken()` 预设的 `validBearerToken` 进行精确、区分大小写的字符串比对。如果两者完全一致，则认证通过。

#### `SignatureStrategy` 认证规则

`SignatureStrategy` 是一种更安全的、基于HMAC（哈希消息认证码）的认证方式，它可以有效防止数据篡改和重放攻击。

1.  **客户端实现**:
    客户端需要在请求头中包含以下三个字段：
    *   `X-Timestamp`: 当前的Unix时间戳（毫秒），例如 `1672531200000`。
    *   `X-Nonce`: 一个每次请求都不同的随机字符串（例如UUID），用于防止重放攻击。
    *   `X-Api-Signature`: 根据以下规则生成的请求签名。

2.  **签名生成**:
    客户端必须严格按照以下步骤生成签名：
    *   **第一步：构造待签名字符串 (stringToSign)**
        将以下四个元素用换行符 `\n` 连接：
        -   HTTP请求方法 (大写, e.g., `POST`)
        -   请求的URI路径 (e.g., `/api/user/create`)
        -   `X-Timestamp` 的值
        -   `X-Nonce` 的值

        **示例**:
        ```
        POST\n/api/user/create\n1672531200000\nrandom-string-123
        ```

    *   **第二步：计算HMAC-SHA256**
        使用预共享的密钥（`signatureSecret`）和 `HmacSHA256` 算法，对上一步构造的 `stringToSign` 进行加密。

    *   **第三步：Base64编码**
        将上一步生成的二进制哈希结果进行Base64编码，得到最终的签名字符串，并将其放入 `X-Api-Signature` 头中。

3.  **服务器端校验**:
    服务器会执行完全相同的签名生成过程，并额外进行时间戳校验：
    *   **时间戳校验**: 检查客户端传来的 `X-Timestamp` 与服务器当前时间的差距是否在允许的窗口内（默认为5分钟），超出则拒绝请求。
    *   **签名比对**: 将本地生成的签名与客户端传来的 `X-Api-Signature` 进行比对，完全一致则认证通过。

### AuthManager 认证功能使用

要为一个接口启用认证，只需在其上添加 `@RequiresAuth` 注解。

```java
@PostmanController("/api/secure")
public class SecureController {

    @PostmanMapping(value = "/data", method = RequestMethod.GET)
    @RequiresAuth(AuthManager.BearerTokenStrategy.class) // 指定使用Bearer Token认证
    public String getSecureData() {
        return "This is a secret message.";
    }

    @PostmanMapping(value = "/action", method = RequestMethod.POST)
    @RequiresAuth({AuthManager.BearerTokenStrategy.class, AuthManager.SignatureStrategy.class}) // 可指定多个策略
    public String performSecureAction() {
        return "Action performed successfully.";
    }
}
```

在启动服务器前，你需要配置相应的密钥：

```java
PostmanUtil.AuthManager.setBearerToken("your-secret-token-here");
PostmanUtil.AuthManager.setSignatureSecret("your-hmac-secret-key-here");
```

### `ManualRegistry` - 手动注册器

#### ManualRegistry 介绍

在自动扫描之外，`ManualRegistry`提供了更灵活的控制方式。
- **职责**:
  - **手动注册类**: `register(Class<?>... classes)`允许你直接指定一个或多个Controller类进行注册，绕过包扫描。
  - **手动指定包**: `scan(String... packages)`允许你指定一个或多个包路径进行扫描。这在启动类与业务类不在同一父包下时非常有用。

#### ManualRegistry 混合使用与优先级

`register()` 和 `scan()` 方法可以混合使用。`HandlerCore` 在扫描时会聚合所有来源的 Controller 类。这意味着您可以同时注册几个特定的核心 Controller，并扫描一个包含其他通用 Controller 的包。这种混合模式在大型项目中非常有用，可以兼顾精确控制和开发效率。

#### ManualRegistry 状态的静态性与全局影响

`ManualRegistry` 内部使用 `static` 列表来存储注册的类和包路径。这意味着它的状态是**全局共享**的，并且在JVM的整个生命周期内持续存在。
- **优点**: 简单直观，可以在应用的不同地方分步配置。
- **注意事项**: 在一个复杂的应用中，如果有多处代码都在调用 `register()` 或 `scan()`，需要注意它们的调用顺序和最终结果。在测试环境中，如果不同的测试用例需要不同的 Controller 组合，建议在每个测试的 `@Before` 或 `@BeforeAll` 阶段，先调用一个（未来可能实现的）`ManualRegistry.clear()` 方法，以避免测试间的相互干扰。

#### ManualRegistry 与 `startServer` 的关系

`ManualRegistry` 本身只负责**收集配置**，它并不会触发任何实际的扫描或注册动作。真正的扫描和注册过程，是在 `HttpCore.startServer()` 方法被调用时，由 `HandlerCore` 从 `ManualRegistry` 中**拉取**配置信息来完成的。理解这种“配置与执行分离”的模式，有助于更好地组织启动代码。
