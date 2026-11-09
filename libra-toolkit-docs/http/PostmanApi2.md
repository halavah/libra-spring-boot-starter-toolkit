# PostmanUtil 增强设计方案：优雅地获取请求参数

**目录**

- [1. 方案概述](#1-方案概述)
  - [1.1. 设计目标](#11-设计目标)
  - [1.2. 核心思想](#12-核心思想)
  - [1.3. 最终效果预览](#13-最终效果预览)
- [2. 核心组件设计](#2-核心组件设计)
  - [2.1. `RequestContextHolder`：请求上下文持有者](#21-requestcontextholder请求上下文持有者)
    - [2.1.1. 作用与原理](#211-作用与原理)
    - [2.1.2. `ThreadLocal` 的使用](#212-threadlocal-的使用)
    - [2.1.3. 关键代码实现](#213-关键代码实现)
  - [2.2. `BaseController`：可继承的控制器基类](#22-basecontroller可继承的控制器基类)
    - [2.2.1. 设计定位](#221-设计定位)
    - [2.2.2. 核心 API 详解](#222-核心-api-详解)
    - [2.2.3. 完整代码实现](#223-完整代码实现)
- [3. 实施步骤](#3-实施步骤)
  - [3.1. 第一步：添加 FastJSON 依赖](#31-第一步添加-fastjson-依赖)
  - [3.2. 第二步：改造 `PostmanUtil` 核心逻辑](#32-第二步改造-postmanutil-核心逻辑)
    - [3.2.1. 定位修改点](#321-定位修改点)
    - [3.2.2. 植入 `RequestContextHolder`](#322-植入-requestcontextholder)
  - [3.3. 第三步：应用到业务 Controller](#33-第三步应用到业务-controller)
    - [3.3.1. 继承基类](#331-继承基类)
    - [3.3.2. 使用新方法重构代码](#332-使用新方法重构代码)
- [4. 完整示例代码](#4-完整示例代码)
  - [4.1. `PostmanUtil.java` (修改后)](#41-postmanutiljava-修改后)
  - [4.2. `ContractController.java` (应用后)](#42-contractcontrollerjava-应用后)

---

## 1. 方案概述

### 1.1. 设计目标
当前 `PostmanUtil` 的 Controller 方法需要显式接收 `Map<String, String>` 或 `String` 类型的参数，导致业务代码与参数获取逻辑耦合。本方案旨在通过借鉴主流 Web 框架的设计思想，对 `PostmanUtil` 进行改造，实现一个更优雅、自动化的参数获取机制。

最终目标是让 Controller 方法的签名保持干净（无参数），通过继承一个基类来透明地获取和解析请求参数。

### 1.2. 核心思想
利用 `ThreadLocal` 在单次请求的处理线程中传递和保存请求的上下文信息（包括查询参数和请求体）。`PostmanUtil` 在接收到 HTTP 请求并分发到具体的 Controller 方法之前，将解析好的参数存入 `ThreadLocal` 变量。然后，在 Controller 的基类中提供 `getParams()` 和 `getBody()` 等方法，从这个 `ThreadLocal` 中读取数据。

### 1.3. 最终效果预览
**改造前：**
```java
// GET
@PostmanMapping(method = RequestMethod.GET)
public Result<?> handleGet(Map<String, String> params) {
    String id = params.get("id");
    // ...
}

// POST
@PostmanMapping(method = RequestMethod.POST)
public Result<?> handlePost(String requestBody) {
    MyDTO dto = JSON.parseObject(requestBody, MyDTO.class);
    // ...
}
```

**改造后：**
```java
public static class MyEndpoint extends BaseController {
    // GET
    @PostmanMapping(method = RequestMethod.GET)
    public Result<?> handleGet() {
        Map<String, String> params = getParams();
        String id = params.get("id");
        // ...
    }

    // POST
    @PostmanMapping(method = RequestMethod.POST)
    public Result<?> handlePost() {
        MyDTO dto = getBody(MyDTO.class);
        // ...
    }
}
```

## 2. 核心组件设计

### 2.1. `RequestContextHolder`：请求上下文持有者

#### 2.1.1. 作用与原理
`RequestContextHolder` 是一个静态内部类，作为请求数据的中转站。它利用 `ThreadLocal` 为每个请求线程提供独立的数据存储空间，从而安全地传递请求参数。

#### 2.1.2. `ThreadLocal` 的使用
- `queryParamsThreadLocal`: `ThreadLocal<Map<String, String>>`，用于存放 GET/DELETE 等请求的 URL 查询参数。
- `requestBodyThreadLocal`: `ThreadLocal<String>`，用于存放 POST/PUT/PATCH 等请求的原始请求体字符串。

#### 2.1.3. 关键代码实现
```java
public static class RequestContextHolder {
    private static final ThreadLocal<Map<String, String>> queryParamsThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> requestBodyThreadLocal = new ThreadLocal<>();

    public static void setQueryParams(Map<String, String> queryParams) {
        queryParamsThreadLocal.set(queryParams);
    }

    public static Map<String, String> getQueryParams() {
        return queryParamsThreadLocal.get();
    }

    public static void setRequestBody(String requestBody) {
        requestBodyThreadLocal.set(requestBody);
    }

    public static String getRequestBody() {
        return requestBodyThreadLocal.get();
    }

    public static void clear() {
        queryParamsThreadLocal.remove();
        requestBodyThreadLocal.remove();
    }
}
```

### 2.2. `BaseController`：可继承的控制器基类

#### 2.2.1. 设计定位
这是一个抽象基类，业务 Controller 通过继承它，即可获得 `getParams()` 和 `getBody()` 的能力。它将参数获取的细节完全封装，让业务代码更专注于业务逻辑。

#### 2.2.2. 核心 API 详解

- **`protected Map<String, String> getParams()`**
  - **作用**: 获取 URL 查询参数。
  - **适用请求**: GET, DELETE, OPTIONS 等主要通过 URL 传递参数的请求。
  - **返回值**: 一个包含所有查询参数的 `Map`。

- **`protected <T> T getBody(Class<T> clazz)`**
  - **作用**: 获取并解析请求体。
  - **适用请求**: POST, PUT, PATCH 等包含请求体的请求。
  - **参数**: `Class<T> clazz` - 目标 DTO 的 Class 对象。
  - **返回值**: 一个被 FastJSON 解析并填充好的 DTO 对象实例。

#### 2.2.3. 完整代码实现
```java
public static abstract class BaseController {
    /**
     * 获取 URL 查询参数 (适用于 GET, DELETE 等方法)。
     * @return 包含所有查询参数的 Map。
     */
    protected Map<String, String> getParams() {
        return RequestContextHolder.getQueryParams();
    }

    /**
     * 获取并解析请求体 (适用于 POST, PUT, PATCH 等方法)。
     * @param clazz 目标 DTO 的 Class 对象。
     * @param <T>   目标 DTO 的类型。
     * @return 解析后的 DTO 对象。
     */
    protected <T> T getBody(Class<T> clazz) {
        String json = RequestContextHolder.getRequestBody();
        if (json == null || json.isEmpty()) {
            return null;
        }
        // 确保 fastjson 依赖已添加
        return com.alibaba.fastjson.JSON.parseObject(json, clazz);
    }
}
```

## 3. 实施步骤

### 3.1. 第一步：添加 FastJSON 依赖
检查并确保 `pom.xml` 中存在 `com.alibaba:fastjson` 依赖。
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.83</version>
</dependency>
```

### 3.2. 第二步：改造 `PostmanUtil` 核心逻辑

#### 3.2.1. 定位修改点
需要找到 `PostmanUtil` 中处理 HTTP 请求的核心方法（例如 `HttpCore.RequestHandler.handle(HttpExchange exchange)`）。

#### 3.2.2. 植入 `RequestContextHolder`
在该核心方法的 `try...finally` 块中，进行如下改造：
- **`try` 块开始处**: 根据请求方法（`GET` 或 `POST` 等），解析参数并调用 `RequestContextHolder.setQueryParams()` 或 `RequestContextHolder.setRequestBody()`。
- **`finally` 块中**: 必须调用 `RequestContextHolder.clear()` 来清理 `ThreadLocal`，防止内存泄漏。

### 3.3. 第三步：应用到业务 Controller

#### 3.3.1. 继承基类
让业务 Controller（如 `ContractWebEndpoint`）继承 `PostmanUtil.BaseController`。
```java
public static class ContractWebEndpoint extends BaseController {
    // ...
}
```

#### 3.3.2. 使用新方法重构代码
移除 Controller 方法中的所有参数，并在方法体内调用 `getParams()` 或 `getBody()` 来获取数据。

## 4. 完整示例代码

### 4.1. `PostmanUtil.java` (修改后)
```java
// ... (原有 PostmanUtil 代码) ...

// ++ 新增 RequestContextHolder ++
public static class RequestContextHolder {
    // ... (代码如上) ...
}

// ++ 新增 BaseController ++
public static abstract class BaseController {
    // ... (代码如上) ...
}

public static class HttpCore {
    // ...
    static class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            long startTime = System.currentTimeMillis();
            // ...
            try {
                // 根据请求方法，设置上下文
                String requestMethod = exchange.getRequestMethod().toUpperCase();
                if ("GET".equalsIgnoreCase(requestMethod) || "DELETE".equalsIgnoreCase(requestMethod) || "OPTIONS".equalsIgnoreCase(requestMethod)) {
                    params = parseQueryParams(exchange.getRequestURI().getRawQuery());
                    RequestContextHolder.setQueryParams(params);
                } else if ("POST".equalsIgnoreCase(requestMethod) || "PUT".equalsIgnoreCase(requestMethod) || "PATCH".equalsIgnoreCase(requestMethod)) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                        requestBody = reader.lines().collect(Collectors.joining("\n"));
                        RequestContextHolder.setRequestBody(requestBody);
                    }
                }

                // ... (原有的路由匹配和方法调用逻辑) ...
                // 调用目标方法
                responseData = handlerMethod.invoke(); // 不再传递参数

            } catch (Exception e) {
                // ...
            } finally {
                // ...
                // 清理上下文
                RequestContextHolder.clear();
            }
        }
    }
    // ...
}
```
*(注：以上为伪代码，展示了在真实 `handle` 方法中植入上下文和清理逻辑的核心思想)*

### 4.2. `ContractController.java` (应用后)
```java
public class ContractController {
    // ... (main 方法等) ...

    @PostmanController("/contract")
    public static class ContractWebEndpoint extends PostmanUtil.BaseController {
        private final ContractService contractService = new ContractService();

        @PostmanMapping(value = "/queryByEmpNo", method = RequestMethod.POST)
        public Result<List<ContractVO>> queryContractByEmpNo() {
            try {
                HrEmployeeDTO employeeDTO = getBody(HrEmployeeDTO.class);
                // ... (后续校验和业务逻辑) ...
                return Result.success(contractService.queryContractByEmpId(employeeDTO));
            } catch (Exception e) {
                return Result.error(500, e.getMessage());
            }
        }
    }
    // ... (Service, Mapper, DTO, VO 定义) ...
}
```
