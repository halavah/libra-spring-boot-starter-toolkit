# Libra Toolkit Demo

这个模块包含使用 `libra-spring-boot-starter-toolkit` 的演示示例。

## 目录结构

*   **src/test/java/.../test/blog**: 博客系统示例（基础 CRUD, Redis 排行榜）
*   **src/test/java/.../test/iot**: 物联网设备监控示例（高频写入, 实时状态）
*   **src/test/java/.../test/erp**: 简易 ERP 系统示例（事务管理, 批量操作, 复杂报表）
*   **.http 文件**: 配套的 HTTP 请求文件位于对应代码包内，可直接在 IntelliJ IDEA 中运行测试。

## 如何运行示例

所有的示例都是自包含的 `main` 方法启动类。它们使用 H2 内存数据库，因此不需要额外部署 MySQL。如果需要测试 Redis 功能，请确保本地 Redis 运行在默认端口 `6379`，否则 Redis 相关功能会被自动忽略。

1.  打开任意示例类，例如 `BlogSystemExample.java`。
2.  运行 `main` 方法。
3.  控制台会显示服务启动端口（例如 `http://localhost:8081`）。
4.  打开对应的 `.http` 文件（例如 `BlogSystem.http`），点击运行请求以测试接口。

## 关键代码说明

### DatabaseUtil 使用

本演示展示了 `DatabaseUtil` 的正确用法：

*   **初始化**: 使用 `DatabaseUtil.DataSourceCore.register(...)` 注册数据源。
*   **DDL 操作**: 使用 `DatabaseUtil.executeDDL(...)` 建表。
*   **增删改查**:
    *   使用 `DatabaseUtil.executeByMpEnhanced(sql, dto)` 进行对象参数的插入/更新。
    *   使用 `DatabaseUtil.selectListByMp(sql, null)` 进行无参查询。
    *   使用 `DatabaseUtil.selectListByMpEnhanced` 进行结果集自动映射。
*   **事务管理**:
    ```java
    DatabaseUtil.DatabaseAccessor db = DatabaseUtil.accessor();
    try {
        db.setTransaction();
        db.executeByMpEnhanced(...);
        db.commit();
    } catch (Exception e) {
        db.rollback();
    }
    ```

### PostmanUtil 使用

展示了如何通过简单的注解快速构建 RESTful API：

*   `@PostmanController`: 定义控制器。
*   `@PostmanMapping`: 定义路由。
*   `PostmanUtil.HttpCore.startServer(...)`: 启动内嵌 HTTP 服务器。
