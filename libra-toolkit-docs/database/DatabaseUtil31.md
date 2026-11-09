好的，我已经理解了您的需求。对您提供的文档进行重构，使其目录结构更清晰，内容表达更精炼，逻辑更顺畅。

以下是重构后的文档。

---

# SimpleSqlParser 使用指南

## 目录

- [一、简介](#一简介)
- [二、核心功能](#二核心功能)
  - [1. 核心标签 (`<sql>` 和 `<if>)`](#1-核心标签-sql-和-if)
  - [2. 占位符 (`#{}` 和 `${}`)](#2-占位符--和-)
  - [3. `<if>` 标签详解](#3-if-标签详解)
- [三、如何使用](#三如何使用)
- [四、与 MyBatis 的核心差异](#四与-mybatis-的核心差异)
  - [1. `<if>` 标签：OGNL vs. 简单表达式](#1-if-标签ognl-vs-简单表达式)
  - [2. 参数与返回类型：约定优于配置](#2-参数与返回类型约定优于配置)
- [五、综合示例](#五综合示例)
- [六、适用场景与选型建议](#六适用场景与选型建议)

---

## 一、简介

`SimpleSqlParser` 是一个轻量级、零依赖的Java动态SQL解析工具。它旨在为不希望引入完整MyBatis框架的项目提供基础的、高性能的动态SQL功能。

它的核心设计哲学是**简单直接**，专注于处理最常见的动态SQL场景，如基于参数存在性的条件判断。

---

## 二、核心功能

### 1. 核心标签 (`<sql>` 和 `<if>`)

`SimpleSqlParser` 目前支持两种最基本的动态SQL标签：

*   **`<sql>` 标签**
    *   **用途**: 定义可重用的SQL代码片段。
    *   **属性**: `id` (必需)，作为片段的唯一标识。
    *   **示例**:
        ```xml
        <sql id="userColumns">
            id, name, email, status
        </sql>
        ```

*   **`<if>` 标签**
    *   **用途**: 进行条件判断。只有当 `test` 属性中的表达式求值为 `true` 时，其内部的SQL片段才会被解析。
    *   **属性**: `test` (必需)，用于定义条件表达式。
    *   **示例**:
        ```xml
        <if test="status != null">
            AND status = #{status}
        </if>
        ```

### 2. 占位符 (`#{}` 和 `${}`)

*   **`#{paramName}` (安全占位符)**
    *   **行为**: 替换为JDBC预编译语句的 `?` 问号。
    *   **安全性**: **推荐使用，可有效防止SQL注入**。
    *   **用途**: 用于传递查询条件值、插入或更新的数据。

*   **`${paramName}` (文本替换占位符)**
    *   **行为**: 直接将参数值拼接到SQL语句中。
    *   **安全性**: **存在SQL注入风险，请谨慎使用**。
    *   **用途**: 仅在无法使用 `#{}` 的场景下使用，例如动态替换表名、列名或 `ORDER BY` 子句。**必须确保其来源安全可控**。

### 3. `<if>` 标签详解

`test` 属性支持一套简单而强大的表达式语法。

*   **支持的操作符**:
    *   `==` (等于)
    *   `!=` (不等于)

*   **支持的比较值**:
    *   `null`: 检查参数是否存在。例如: `user != null`
    *   空字符串: 支持单引号 `''` 和双引号 `""`。例如: `name != ''`
    *   字符串字面量: 与给定的字符串值比较。例如: `type == 'ADMIN'`

*   **逻辑连接符**:
    *   支持 `and` 和 `or` 进行多条件组合。
    *   **优先级**: `and` 的优先级高于 `or`。表达式 `a or b and c` 会被解释为 `a or (b and c)`。
    *   **示例**: `<if test="name != null and name != ''">`

*   **无操作符的简单条件**:
    *   如果 `test` 属性中只有一个变量名，如 `<if test="isAdmin">`，则当参数Map中存在键 `isAdmin` 且其值不为 `null` 时，条件为 `true`。

*   **嵌套支持**:
    *   `<if>` 标签可以相互嵌套，以实现更复杂的逻辑。
        ```xml
        <if test="status != null">
            AND status = #{status}
            <if test="name != null and name != ''">
                AND name LIKE #{name}
            </if>
        </if>
        ```
---

## 三、如何使用

通过 `SimpleSqlParameterConverter` 的静态方法调用解析器：

1.  `convertDirectSql(String sql, Map<String, Object> params)`:
    *   直接解析一个包含动态SQL标签的字符串。

2.  `convertXmlFile(String xmlFileName, String sqlId, Map<String, Object> params)`:
    *   从指定的XML文件中，根据 `sqlId` 查找并解析SQL。每次调用都会重新读取文件。

3.  `convertXmlAuto(String sqlId, Map<String, Object> params)`:
    *   在预设的资源目录 (如 `src/main/resources/sql/`) 中自动查找所有XML文件，并从缓存中解析 `sqlId` 对应的SQL。

---

## 四、与 MyBatis 的核心差异

`SimpleSqlParser` 的设计目标是轻量化，因此其功能是MyBatis的严格子集。理解这些差异是正确使用本工具的关键。

### 1. `<if>` 标签：OGNL vs. 简单表达式

| 特性 / 维度 | `SimpleSqlParser` | MyBatis |
| :--- | :--- | :--- |
| **核心引擎** | 手动实现的简单字符串解析 | **OGNL** (功能强大的表达式语言) |
| **设计哲学** | **轻量、零依赖、简单直接** | **功能全面、灵活、富有表现力** |
| **表达式能力** | 仅支持简单的布尔和字符串比较 | 支持完整的OGNL表达式 |
| **操作符** | `==`, `!=` | `==`, `!=`, `>`, `<`, `>=`, `<=`, `and`, `or`, `not` |
| **类型处理** | 仅能正确处理 `null` 和字符串。所有比较都按 **字符串** 进行。 | **强类型感知**，能正确比较数字、布尔值等。 |
| **方法调用** | **不支持** (如 `list.size() > 0`) | **完全支持** (如 `list.isEmpty()`, `name.startsWith('A')`) |
| **典型场景** | `<if test="name != ''">` | `<if test="age > 18 and !list.isEmpty()">` |

### 2. 参数与返回类型：约定优于配置

在与 `DatabaseUtil` 等工具类结合使用时，`SimpleSqlParser` 遵循一套与标准MyBatis不同的设计哲学。

*   **标准 MyBatis 的行为**:
    *   **强类型**：通常需要为 `parameterType` 和 `resultType` 指定具体的Java类（如 `com.example.UserDTO`），MyBatis负责对象与SQL参数/结果集的映射。
    *   **配置驱动**：开发者必须明确配置类型信息，否则框架无法工作。

*   **`DatabaseUtil` + `SimpleSqlParser` 的行为**:
    *   **约定优于配置**：工具类的API设计决定了其行为模式。
    *   **入参默认为 `Map`**: 所有数据通过 `Map<String, Object>` 传入。因此，在XML中 `parameterType="map"` 是默认行为，可以省略。
    *   **出参默认为 `Map`**: 所有查询结果都封装为 `List<Map<String, Object>>` 或 `Map<String, Object>`。因此，`resultType="map"` 也是默认行为，可以省略。

*   **设计理念对比**:

| 维度 | 标准 MyBatis | `DatabaseUtil` + `SimpleSqlParser` |
|:---|:---|:---|
| **设计目标** | 完整的ORM框架，支持强类型对象映射 | 轻量级数据库工具，追求最大化灵活性和最简化配置 |
| **类型安全** | 编译时类型检查，更安全 | 运行时动态访问，更灵活 |
| **开发效率** | 前期需定义DTO/VO类，配置稍繁琐 | 无需定义额外类，配置极简，适合快速开发 |

*   **最佳实践建议**:
    1.  **明确指定**: 虽然可以省略，但建议在XML中明确写出 `parameterType="map"` 和 `resultType="map"`，以增强代码的可读性。
    2.  **拥抱Map**: 充分利用Map的灵活性，简化动态SQL的条件构建。
    3.  **注意键名**: 在处理返回的`Map`结果时，`key` 的名称对应SQL查询中的列名或别名。

---

## 五、综合示例

以下示例展示了嵌套 `<if>`、`and` 和 `or` 逻辑的综合运用。

**XML文件 (`user-sqls.xml`):**
```xml
<sql id="findUsersAdvanced">
    SELECT id, name, email, role, status
    FROM ${tableName}
    WHERE 1=1

    <!-- 场景1: 嵌套if与and逻辑 -->
    <if test="status != null and status != ''">
        AND status = #{status}
        <if test="isAdmin">
            AND is_admin_flag = 1
        </if>
    </if>

    <!-- 场景2: or逻辑 -->
    <if test="role == 'ADMIN' or role == 'OWNER'">
        AND security_level > 5
    </if>

    <!-- 场景3: 关键词搜索 -->
    <if test="keyword != null and keyword != ''">
        AND (name LIKE #{keyword} OR email LIKE #{keyword})
    </if>
</sql>
```

**Java 调用:**
```java
Map<String, Object> params = new HashMap<>();
params.put("tableName", "users");
params.put("status", "ACTIVE");
params.put("isAdmin", true);
params.put("role", "ADMIN");
params.put("keyword", "%test%");

// 假设调用 convertXmlAuto 方法
SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlAuto("findUsersAdvanced", params);

/*
预期生成的SQL:
SELECT id, name, email, role, status
FROM users
WHERE 1=1
AND status = ?
AND is_admin_flag = 1
AND security_level > 5
AND (name LIKE ? OR email LIKE ?)

预期生成的参数列表:
["ACTIVE", true, "%test%", "%test%"]
*/
```

---

## 六、适用场景与选型建议

*   **何时应选择 `SimpleSqlParser`**:
    *   项目**未引入MyBatis**，但需要基础的动态SQL功能。
    *   动态SQL的条件**仅涉及参数存在性 (`!= null`) 或字符串比较**。
    *   追求**极致的轻量化和零依赖**，且性能要求高。

*   **何时必须使用 MyBatis (或 `MyBatisSqlParser`)**:
    *   需要进行**数字大小比较** (如 `age > 18`)。
    *   需要检查**集合是否为空** (如 `list.size() > 0` 或 `!list.isEmpty()`)。
    *   需要在表达式中**调用对象的方法** (如 `name.startsWith('A')`)。
    *   项目已经或计划深度使用MyBatis生态。

总而言之，`SimpleSqlParser` 是一个专注、高效的“瑞士军刀”，适用于简单场景；而MyBatis则是一个功能完备的“工具箱”，能应对各种复杂的持久化需求。请根据项目实际情况进行选择。

---