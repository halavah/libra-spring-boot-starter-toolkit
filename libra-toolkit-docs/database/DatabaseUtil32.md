# MyBatisSqlExecutor 内部类 MyBatis 标签支持文档

## 📚 目录

1. [概述](#概述)
   - [调用方式](#调用方式)
2. [支持的 MyBatis 标签列表](#支持的-mybatis-标签列表)
3. [基础查询标签](#基础查询标签)
   - [`<select>`, `<insert>`, `<update>`, `<delete>`](#select-insert-update-delete)
4. [条件判断标签](#条件判断标签)
   - [`<if>` 标签](#if-标签)
   - [`<where>` 标签](#where-标签)
   - [`<set>` 标签](#set-标签)
5. [循环标签](#循环标签)
   - [`<foreach>` 标签](#foreach-标签)
6. [条件选择标签](#条件选择标签)
   - [`<choose>`, `<when>`, `<otherwise>` 标签](#choose-when-otherwise-标签)
7. [SQL片段标签](#sql片段标签)
   - [`<sql>` 和 `<include>` 标签](#sql-和-include-标签)
8. [字符串处理标签](#字符串处理标签)
   - [`<trim>` 标签](#trim-标签)
   - [`<bind>` 标签](#bind-标签)
9. [完整使用示例](#完整使用示例)
   - [复杂业务查询示例](#复杂业务查询示例)
10. [注意事项与最佳实践](#注意事项与最佳实践)
    - [1. 参数类型支持](#1-参数类型支持)
    - [2. 条件表达式语法](#2-条件表达式语法)
    - [3. 性能优化建议](#3-性能优化建议)
    - [4. 错误处理](#4-错误处理)
    - [5. 调试技巧](#5-调试技巧)
11. [高级特性补充说明](#高级特性补充说明)
    - [11. 嵌套动态标签处理](#11-嵌套动态标签处理)
    - [12. 动态字段和表名处理](#12-动态字段和表名处理)
    - [13. 复杂的JEXL表达式应用](#13-复杂的jexl表达式应用)
    - [14. 分页和排序的高级用法](#14-分页和排序的高级用法)
    - [15. 批量操作的高级应用](#15-批量操作的高级应用)
    - [16. 与SimpleSqlExecutor的对比总结](#16-与simplesqlexecutor的对比总结)
    - [17. 性能优化和最佳实践](#17-性能优化和最佳实践)
12. [实战案例集合](#实战案例集合)
    - [18. 常见业务场景实战](#18-常见业务场景实战)
      - [18.1 用户权限管理系统](#181-用户权限管理系统)
      - [18.2 电商订单查询系统](#182-电商订单查询系统)
      - [18.3 数据统计分析系统](#183-数据统计分析系统)
    - [19. 实际项目集成指南](#19-实际项目集成指南)
      - [19.1 Spring Boot集成](#191-spring-boot集成)
      - [19.2 Service层封装](#192-service层封装)
      - [19.3 错误处理和监控](#193-错误处理和监控)
    - [20. 迁移指南和版本升级](#20-迁移指南和版本升级)
      - [20.1 从MyBatis迁移](#201-从mybatis迁移)
      - [20.2 性能对比测试](#202-性能对比测试)
    - [21. 常见问题解答 (FAQ)](#21-常见问题解答-faq)
13. [附录](#附录)
    - [A. 完整的标签参考表](#a-完整的标签参考表)
    - [B. JEXL表达式参考](#b-jexl表达式参考)
    - [C. 调试和日志配置](#c-调试和日志配置)

---

## 概述

`DatabaseUtil31.MyBatisSqlExecutor` 内部类通过强大的 `MyBatisSqlParser` 解析器，完整支持 MyBatis 的动态 SQL 语法。该解析器使用 JEXL 表达式引擎来评估条件，提供了与标准 MyBatis 几乎完全兼容的动态 SQL 功能。

### 调用方式

```java
// 方式1: 全局静态方法调用
List<Map<String, Object>> result = DatabaseUtil31.selectListByMp(sql, params);

// 方式2: 链式调用
DatabaseAccessor accessor = DatabaseUtil31.accessor("database_name");
List<Map<String, Object>> result = accessor.selectListByMp(sql, params);

// 方式3: 从XML文件执行
List<Map<String, Object>> result = DatabaseUtil31.selectListFromXmlByMp("Demo1Mapper.xml", "sqlId", params);

// 方式4: 自动搜索XML
List<Map<String, Object>> result = DatabaseUtil31.selectListFromXmlAutoByMp("sqlId", params);
```

---

## 支持的 MyBatis 标签列表

| 标签名称 | 功能描述 | 支持状态 |
|---------|---------|---------|
| `<if>` | 条件判断 | ✅ 完全支持 |
| `<where>` | 动态WHERE子句 | ✅ 完全支持 |
| `<set>` | 动态SET子句 | ✅ 完全支持 |
| `<choose>` | 多条件选择 | ✅ 完全支持 |
| `<when>` | choose中的条件分支 | ✅ 完全支持 |
| `<otherwise>` | choose中的默认分支 | ✅ 完全支持 |
| `<foreach>` | 循环遍历 | ✅ 完全支持 |
| `<trim>` | 字符串修剪 | ✅ 完全支持 |
| `<sql>` | SQL片段定义 | ✅ 完全支持 |
| `<include>` | 引用SQL片段 | ✅ 完全支持 |
| `<bind>` | 变量绑定 | ✅ 完全支持 |

---

## 基础查询标签

### `<select>`, `<insert>`, `<update>`, `<delete>`

这些是基础的 SQL 操作标签，用于包装具体的 SQL 语句。

```xml
<!-- 基础查询示例 -->
<select id="selectUserById" parameterType="map" resultType="map">
    SELECT id, name, email, status, create_time
    FROM users
    WHERE id = #{id}
</select>

<!-- 基础插入示例 -->
<insert id="insertUser" parameterType="map">
    INSERT INTO users (name, age, email, phone, status, create_time)
    VALUES (#{name}, #{age}, #{email}, #{phone}, #{status}, #{createTime})
</insert>

<!-- 基础更新示例 -->
<update id="updateUser" parameterType="map">
    UPDATE users
    SET name = #{name}, age = #{age}, email = #{email}
    WHERE id = #{id}
</update>

<!-- 基础删除示例 -->
<delete id="deleteUser" parameterType="map">
    DELETE FROM users WHERE id = #{id}
</delete>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("id", 1001L);

// 执行查询
List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUserById", params);

// 执行插入
params.put("name", "张三");
params.put("age", 25);
params.put("email", "zhangsan@example.com");
int insertedRows = DatabaseUtil31.executeFromXmlAutoByMp("insertUser", params);
```

---

## 条件判断标签

### `<if>` 标签

根据条件动态包含或排除 SQL 片段。

**属性：**
- `test`: 条件表达式，支持 JEXL 语法

**示例：**
```xml
<select id="selectUsersByCondition" parameterType="map" resultType="map">
    SELECT * FROM users WHERE 1=1
    <if test="name != null and name != ''">
        AND name LIKE CONCAT('%', #{name}, '%')
    </if>
    <if test="age != null">
        AND age = #{age}
    </if>
    <if test="email != null and email != ''">
        AND email = #{email}
    </if>
    <if test="status != null">
        AND status = #{status}
    </if>
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("name", "张");          // 会添加name条件
params.put("age", 25);            // 会添加age条件
// email为null，不会添加email条件
params.put("status", "active");   // 会添加status条件

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersByCondition", params);
// 生成的SQL: SELECT * FROM users WHERE 1=1 AND name LIKE CONCAT('%', ?, '%') AND age = ? AND status = ?
```

### `<where>` 标签

自动管理 WHERE 子句，智能处理 AND/OR 前缀。

**功能：**
- 自动添加 WHERE 关键字
- 自动移除开头的 AND 或 OR
- 如果内容为空，不添加 WHERE 子句

**示例：**
```xml
<select id="selectUsersWithWhere" parameterType="map" resultType="map">
    SELECT * FROM users
    <where>
        <if test="name != null and name != ''">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="age != null">
            AND age = #{age}
        </if>
        <if test="email != null and email != ''">
            AND email = #{email}
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
    </where>
    ORDER BY create_time DESC
</select>
```

**Java调用示例：**
```java
// 场景1: 有条件参数
Map<String, Object> params1 = new HashMap<>();
params1.put("name", "张");
params1.put("status", "active");

List<Map<String, Object>> users1 = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersWithWhere", params1);
// 生成SQL: SELECT * FROM users WHERE name LIKE CONCAT('%', ?, '%') AND status = ? ORDER BY create_time DESC

// 场景2: 无条件参数
Map<String, Object> params2 = new HashMap<>();
List<Map<String, Object>> users2 = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersWithWhere", params2);
// 生成SQL: SELECT * FROM users ORDER BY create_time DESC
```

### `<set>` 标签

自动管理 SET 子句，智能处理逗号。

**功能：**
- 自动添加 SET 关键字
- 自动移除结尾的逗号
- 如果内容为空，不添加 SET 子句

**示例：**
```xml
<update id="updateUserSelective" parameterType="map">
    UPDATE users
    <set>
        <if test="name != null and name != ''">
            name = #{name},
        </if>
        <if test="age != null">
            age = #{age},
        </if>
        <if test="email != null and email != ''">
            email = #{email},
        </if>
        <if test="phone != null and phone != ''">
            phone = #{phone},
        </if>
        <if test="status != null">
            status = #{status},
        </if>
        update_time = NOW()
    </set>
    WHERE id = #{id}
</update>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("id", 1001L);
params.put("name", "张三丰");
params.put("email", "zhangsanfeng@example.com");
// age和phone为null，不会更新这两个字段

int updatedRows = DatabaseUtil31.executeFromXmlAutoByMp("updateUserSelective", params);
// 生成SQL: UPDATE users SET name = ?, email = ?, update_time = NOW() WHERE id = ?
```

---

## 循环标签

### `<foreach>` 标签

用于遍历集合，生成重复的 SQL 片段。

**属性：**
- `collection`: 要遍历的集合名称
- `item`: 当前元素的变量名
- `index`: 当前索引的变量名（可选）
- `open`: 开始字符串
- `close`: 结束字符串
- `separator`: 分隔符

**示例1：批量查询（IN子句）**
```xml
<select id="selectUsersByIds" parameterType="map" resultType="map">
    SELECT * FROM users WHERE id IN
    <foreach collection="ids" item="id" open="(" close=")" separator=",">
        #{id}
    </foreach>
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("ids", Arrays.asList(1001L, 1002L, 1003L, 1004L));

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersByIds", params);
// 生成SQL: SELECT * FROM users WHERE id IN (?, ?, ?, ?)
```

**示例2：批量插入**
```xml
<insert id="batchInsertUsers" parameterType="map">
    INSERT INTO users (name, age, email, phone, status, create_time)
    VALUES
    <foreach collection="users" item="user" separator=",">
        (#{user.name}, #{user.age}, #{user.email}, #{user.phone}, #{user.status}, #{user.createTime})
    </foreach>
</insert>
```

**Java调用示例：**
```java
List<Map<String, Object>> userList = new ArrayList<>();

Map<String, Object> user1 = new HashMap<>();
user1.put("name", "张三");
user1.put("age", 25);
user1.put("email", "zhangsan@example.com");
user1.put("phone", "13800000001");
user1.put("status", "active");
user1.put("createTime", new Date());
userList.add(user1);

Map<String, Object> user2 = new HashMap<>();
user2.put("name", "李四");
user2.put("age", 28);
user2.put("email", "lisi@example.com");
user2.put("phone", "13800000002");
user2.put("status", "active");
user2.put("createTime", new Date());
userList.add(user2);

Map<String, Object> params = new HashMap<>();
params.put("users", userList);

int insertedRows = DatabaseUtil31.executeFromXmlAutoByMp("batchInsertUsers", params);
// 生成SQL: INSERT INTO users (name, age, email, phone, status, create_time) VALUES (?, ?, ?, ?, ?, ?), (?, ?, ?, ?, ?, ?)
```

**示例3：复杂条件组合**
```xml
<select id="selectUsersByComplexCondition" parameterType="map" resultType="map">
    SELECT * FROM users WHERE 1=1
    <if test="names != null and names.size() > 0">
        AND name IN
        <foreach collection="names" item="name" open="(" close=")" separator=",">
            #{name}
        </foreach>
    </if>
    <if test="ageRange != null and ageRange.size() > 0">
        AND (
        <foreach collection="ageRange" item="age" separator=" OR ">
            age = #{age}
        </foreach>
        )
    </if>
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("names", Arrays.asList("张三", "李四", "王五"));
params.put("ageRange", Arrays.asList(25, 30, 35));

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersByComplexCondition", params);
// 生成SQL: SELECT * FROM users WHERE 1=1 AND name IN (?, ?, ?) AND (age = ? OR age = ? OR age = ?)
```

---

## 条件选择标签

### `<choose>`, `<when>`, `<otherwise>` 标签

实现类似 switch-case 的条件选择逻辑。

**功能：**
- `<choose>`: 选择容器
- `<when test="条件">`: 条件分支（可以有多个）
- `<otherwise>`: 默认分支（可选）

**示例：**
```xml
<select id="selectUsersByType" parameterType="map" resultType="map">
    SELECT * FROM users WHERE 1=1
    <choose>
        <when test="searchType == 'name' and searchValue != null">
            AND name LIKE CONCAT('%', #{searchValue}, '%')
        </when>
        <when test="searchType == 'email' and searchValue != null">
            AND email = #{searchValue}
        </when>
        <when test="searchType == 'phone' and searchValue != null">
            AND phone = #{searchValue}
        </when>
        <otherwise>
            AND status = 'active'
        </otherwise>
    </choose>
    ORDER BY create_time DESC
</select>
```

**Java调用示例：**
```java
// 场景1: 按姓名搜索
Map<String, Object> params1 = new HashMap<>();
params1.put("searchType", "name");
params1.put("searchValue", "张");

List<Map<String, Object>> users1 = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersByType", params1);
// 生成SQL: SELECT * FROM users WHERE 1=1 AND name LIKE CONCAT('%', ?, '%') ORDER BY create_time DESC

// 场景2: 按邮箱搜索
Map<String, Object> params2 = new HashMap<>();
params2.put("searchType", "email");
params2.put("searchValue", "zhangsan@example.com");

List<Map<String, Object>> users2 = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersByType", params2);
// 生成SQL: SELECT * FROM users WHERE 1=1 AND email = ? ORDER BY create_time DESC

// 场景3: 未指定搜索类型，使用默认条件
Map<String, Object> params3 = new HashMap<>();
List<Map<String, Object>> users3 = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersByType", params3);
// 生成SQL: SELECT * FROM users WHERE 1=1 AND status = 'active' ORDER BY create_time DESC
```

---

## SQL片段标签

### `<sql>` 和 `<include>` 标签

用于定义和复用 SQL 片段，提高代码复用性。

**`<sql>` 标签属性：**
- `id`: 片段的唯一标识符

**`<include>` 标签属性：**
- `refid`: 要引用的 SQL 片段 ID

**示例：**
```xml
<!-- SQL片段定义 -->
<sql id="userColumns">
    id, name, age, email, phone, status, create_time, update_time
</sql>

<sql id="dynamicConditions">
    <where>
        <if test="status != null">
            AND status = #{status}
        </if>
        <if test="name != null and name != ''">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="startDate != null">
            AND created_at >= #{startDate}
        </if>
    </where>
</sql>

<!-- 使用SQL片段 -->
<select id="selectUsersWithFragment" parameterType="map" resultType="map">
    SELECT <include refid="userColumns"/>
    FROM users
    <include refid="dynamicConditions"/>
    ORDER BY create_time DESC
</select>

<!-- 带参数的include -->
<select id="selectUsersWithProperties" parameterType="map" resultType="map">
    SELECT <include refid="userColumns"/>
    FROM users
    <include refid="dynamicConditions">
        <property name="tableName" value="users"/>
        <property name="orderBy" value="create_time DESC"/>
    </include>
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("status", "active");
params.put("name", "张");

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersWithFragment", params);
// 生成SQL: SELECT id, name, age, email, phone, status, create_time, update_time FROM users WHERE status = ? AND name LIKE CONCAT('%', ?, '%') ORDER BY create_time DESC
```

---

## 字符串处理标签

### `<trim>` 标签

提供更灵活的字符串修剪功能。

**属性：**
- `prefix`: 添加前缀
- `suffix`: 添加后缀
- `prefixOverrides`: 移除的前缀（用 | 分隔多个）
- `suffixOverrides`: 移除的后缀（用 | 分隔多个）

**示例：**
```xml
<select id="selectUsersWithTrim" parameterType="map" resultType="map">
    SELECT * FROM users
    <trim prefix="WHERE" prefixOverrides="AND |OR ">
        <if test="name != null and name != ''">
            AND name = #{name}
        </if>
        <if test="age != null">
            OR age = #{age}
        </if>
        <if test="email != null and email != ''">
            AND email = #{email}
        </if>
    </trim>
</select>

<update id="updateUserWithTrim" parameterType="map">
    UPDATE users
    <trim prefix="SET" suffixOverrides=",">
        <if test="name != null">
            name = #{name},
        </if>
        <if test="age != null">
            age = #{age},
        </if>
        <if test="email != null">
            email = #{email},
        </if>
    </trim>
    WHERE id = #{id}
</update>
```

**Java调用示例：**
```java
// 查询示例
Map<String, Object> queryParams = new HashMap<>();
queryParams.put("name", "张三");
queryParams.put("age", 25);

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersWithTrim", queryParams);
// 生成SQL: SELECT * FROM users WHERE name = ? OR age = ?
// 注意：自动移除了第一个AND，保留了OR

// 更新示例
Map<String, Object> updateParams = new HashMap<>();
updateParams.put("id", 1001L);
updateParams.put("name", "张三丰");
updateParams.put("email", "zhangsanfeng@example.com");

int updatedRows = DatabaseUtil31.executeFromXmlAutoByMp("updateUserWithTrim", updateParams);
// 生成SQL: UPDATE users SET name = ?, email = ? WHERE id = ?
// 注意：自动移除了最后的逗号
```

### `<bind>` 标签

用于创建变量并在 SQL 中使用。

**属性：**
- `name`: 变量名称
- `value`: 变量值（JEXL表达式）

**示例：**
```xml
<select id="searchUsersWithBind" parameterType="map" resultType="map">
    <bind name="pattern" value="'%' + keyword + '%'"/>
    <bind name="upperKeyword" value="keyword.toUpperCase()"/>

    SELECT * FROM users
    WHERE name LIKE #{pattern}
       OR UPPER(email) LIKE #{upperKeyword}
    ORDER BY create_time DESC
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("keyword", "zhang");

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("searchUsersWithBind", params);
// bind标签会创建：
// pattern = "%zhang%"
// upperKeyword = "ZHANG"
// 生成SQL: SELECT * FROM users WHERE name LIKE ? OR UPPER(email) LIKE ? ORDER BY create_time DESC
```

---

## 完整使用示例

### 复杂业务查询示例

```xml
<select id="searchUsersAdvanced" parameterType="map" resultType="map">
    <bind name="namePattern" value="'%' + (searchKeyword != null ? searchKeyword : '') + '%'"/>

    SELECT
        u.id, u.name, u.email, u.age, u.status,
        d.name as department_name,
        r.name as role_name
    FROM users u
    <if test="includeDepartment != null and includeDepartment == true">
        LEFT JOIN departments d ON u.department_id = d.id
    </if>
    <if test="includeRole != null and includeRole == true">
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
    </if>

    <where>
        <if test="searchKeyword != null and searchKeyword != ''">
            AND (u.name LIKE #{namePattern}
                 OR u.email LIKE #{namePattern}
                 OR u.phone LIKE #{namePattern})
        </if>

        <if test="filters != null">
            <if test="filters.ageRange != null">
                <if test="filters.ageRange.min != null">
                    AND u.age >= #{filters.ageRange.min}
                </if>
                <if test="filters.ageRange.max != null">
                    AND u.age &lt;= #{filters.ageRange.max}
                </if>
            </if>

            <if test="filters.statuses != null and filters.statuses.size() > 0">
                AND u.status IN
                <foreach collection="filters.statuses" item="status" open="(" close=")" separator=",">
                    #{status}
                </foreach>
            </if>

            <if test="filters.departmentIds != null and filters.departmentIds.size() > 0">
                AND u.department_id IN
                <foreach collection="filters.departmentIds" item="deptId" open="(" close=")" separator=",">
                    #{deptId}
                </foreach>
            </if>
        </if>

        <if test="excludeIds != null and excludeIds.size() > 0">
            AND u.id NOT IN
            <foreach collection="excludeIds" item="excludeId" open="(" close=")" separator=",">
                #{excludeId}
            </foreach>
        </if>
    </where>

    <choose>
        <when test="sorting != null and sorting.size() > 0">
            ORDER BY
            <foreach collection="sorting" item="sort" separator=",">
                <choose>
                    <when test="sort.field == 'name'">
                        u.name ${sort.direction}
                    </when>
                    <when test="sort.field == 'age'">
                        u.age ${sort.direction}
                    </when>
                    <when test="sort.field == 'createTime'">
                        u.create_time ${sort.direction}
                    </when>
                    <otherwise>
                        u.id ${sort.direction}
                    </otherwise>
                </choose>
            </foreach>
        </when>
        <otherwise>
            ORDER BY u.create_time DESC
        </otherwise>
    </choose>
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();

// 基础搜索
params.put("searchKeyword", "张");
params.put("includeDepartment", true);
params.put("includeRole", true);

// 过滤条件
Map<String, Object> filters = new HashMap<>();

// 年龄范围
Map<String, Object> ageRange = new HashMap<>();
ageRange.put("min", 25);
ageRange.put("max", 40);
filters.put("ageRange", ageRange);

// 状态列表
filters.put("statuses", Arrays.asList("active", "pending"));

// 部门ID列表
filters.put("departmentIds", Arrays.asList(1L, 2L, 3L));

params.put("filters", filters);

// 排除的用户ID
params.put("excludeIds", Arrays.asList(100L, 200L));

// 排序
List<Map<String, Object>> sorting = new ArrayList<>();
Map<String, Object> sort1 = new HashMap<>();
sort1.put("field", "name");
sort1.put("direction", "ASC");
sorting.add(sort1);

Map<String, Object> sort2 = new HashMap<>();
sort2.put("field", "age");
sort2.put("direction", "DESC");
sorting.add(sort2);

params.put("sorting", sorting);

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("searchUsersAdvanced", params);
```

---

## 注意事项与最佳实践

### 1. 参数类型支持

**支持的参数类型：**
- 基本类型：`String`, `Integer`, `Long`, `Double`, `Boolean`
- 集合类型：`List`, `Set`, `Array`
- 复杂对象：`Map`, 自定义Java对象
- 日期类型：`Date`, `LocalDate`, `LocalDateTime`

### 2. 条件表达式语法

**JEXL支持的表达式：**
```java
// 空值检查
test="name != null"
test="name != null and name != ''"

// 数值比较
test="age > 18"
test="age >= 18 and age <= 65"

// 字符串比较
test="status == 'active'"
test="name.startsWith('张')"

// 集合检查
test="ids != null and ids.size() > 0"
test="names.contains('张三')"

// 复杂对象属性
test="user.profile.age > 18"
test="filters.dateRange.startDate != null"
```

### 3. 性能优化建议

**避免不必要的条件判断：**
```xml
<!-- 好的做法 -->
<if test="name != null and name != ''">
    AND name LIKE CONCAT('%', #{name}, '%')
</if>

<!-- 避免的做法：过度嵌套 -->
<if test="user != null">
    <if test="user.name != null">
        <if test="user.name != ''">
            AND name LIKE CONCAT('%', #{user.name}, '%')
        </if>
    </if>
</if>
```

**合理使用索引列：**
```xml
<!-- 确保WHERE条件中包含索引列 -->
<where>
    AND status = #{status}  <!-- 假设status有索引 -->
    <if test="name != null and name != ''">
        AND name LIKE CONCAT('%', #{name}, '%')
    </if>
</where>
```

### 4. 错误处理

**常见错误及解决方案：**

1. **标签未闭合**
```xml
<!-- 错误 -->
<if test="name != null">
    AND name = #{name}
<!-- 缺少 </if> -->

<!-- 正确 -->
<if test="name != null">
    AND name = #{name}
</if>
```

2. **表达式语法错误**
```xml
<!-- 错误：使用了Java语法 -->
<if test="name != null && name != ''">

<!-- 正确：使用JEXL语法 -->
<if test="name != null and name != ''">
```

3. **参数名不匹配**
```java
// Java代码
params.put("userName", "张三");
```
```xml
<!-- XML中必须使用相同的参数名 -->
<if test="userName != null">
    AND name = #{userName}
</if>
```

### 5. 调试技巧

**启用SQL调试日志：**
```java
// 启用SQL执行详细日志
DatabaseUtil31.LoggerCore.setSqlDebugLogEnabled(true);

// 执行查询，可以在日志中看到完整的SQL和参数
List<Map<String, Object>> result = DatabaseUtil31.selectListByMp(sql, params);
```

**分步调试复杂SQL：**
```java
// 先测试简单版本
String simpleSql = "SELECT * FROM users WHERE status = #{status}";
List<Map<String, Object>> simpleResult = DatabaseUtil31.selectListByMp(simpleSql, params);

// 再逐步添加复杂条件
String complexSql = "SELECT * FROM users <where>...</where>";
List<Map<String, Object>> complexResult = DatabaseUtil31.selectListByMp(complexSql, params);
```

---

## 高级特性补充说明

### 11. 嵌套动态标签处理

`DatabaseUtil31.MyBatisSqlExecutor` 支持无限深度的标签嵌套，可以在任意标签内部嵌套其他标签。

**示例：多层嵌套条件**
```xml
<select id="selectUsersWithNestedConditions" parameterType="map" resultType="map">
    SELECT * FROM users
    <where>
        <if test="user != null">
            <!-- 第一层嵌套 -->
            <if test="user.basicInfo != null">
                <!-- 第二层嵌套 -->
                <if test="user.basicInfo.name != null and user.basicInfo.name != ''">
                    AND name LIKE CONCAT('%', #{user.basicInfo.name}, '%')
                </if>
                <if test="user.basicInfo.age != null">
                    <!-- 第三层嵌套：根据年龄范围选择不同的过滤策略 -->
                    <choose>
                        <when test="user.basicInfo.age &lt; 18">
                            AND age &lt; 18 AND status = 'minor'
                        </when>
                        <when test="user.basicInfo.age >= 18 and user.basicInfo.age &lt; 65">
                            AND age BETWEEN 18 AND 64 AND status IN ('active', 'inactive')
                        </when>
                        <otherwise>
                            AND age >= 65 AND status = 'senior'
                        </otherwise>
                    </choose>
                </if>
            </if>

            <!-- 条件分组内的foreach嵌套 -->
            <if test="user.permissions != null and user.permissions.roleIds != null">
                AND EXISTS (
                    SELECT 1 FROM user_roles ur
                    WHERE ur.user_id = users.id
                    AND ur.role_id IN
                    <foreach collection="user.permissions.roleIds" item="roleId" open="(" close=")" separator=",">
                        #{roleId}
                    </foreach>
                )
            </if>
        </if>

        <!-- 另一个独立的条件分组 -->
        <if test="filters != null">
            <choose>
                <when test="filters.dateRange != null">
                    <if test="filters.dateRange.startDate != null">
                        AND create_time >= #{filters.dateRange.startDate}
                    </if>
                    <if test="filters.dateRange.endDate != null">
                        AND create_time &lt;= #{filters.dateRange.endDate}
                    </if>
                </when>
                <otherwise>
                    <!-- 如果没有日期范围，默认查询最近30天 -->
                    AND create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                </otherwise>
            </choose>
        </if>
    </where>
    ORDER BY create_time DESC
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();

// 构建复杂的用户信息对象
Map<String, Object> user = new HashMap<>();
Map<String, Object> basicInfo = new HashMap<>();
basicInfo.put("name", "张");
basicInfo.put("age", 25);
user.put("basicInfo", basicInfo);

Map<String, Object> permissions = new HashMap<>();
permissions.put("roleIds", Arrays.asList(1L, 2L, 3L));
user.put("permissions", permissions);

params.put("user", user);

// 构建过滤器对象
Map<String, Object> filters = new HashMap<>();
Map<String, Object> dateRange = new HashMap<>();
dateRange.put("startDate", "2024-01-01");
dateRange.put("endDate", "2024-12-31");
filters.put("dateRange", dateRange);

params.put("filters", filters);

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersWithNestedConditions", params);
```

### 12. 动态字段和表名处理

使用 `${}` 占位符可以动态指定字段名、表名或SQL片段（注意：这种方式需要谨慎使用，避免SQL注入）。

**示例：动态字段查询**
```xml
<select id="selectUsersDynamicFields" parameterType="map" resultType="map">
    SELECT
    <if test="fields != null and fields.size() > 0">
        <foreach collection="fields" item="field" separator=",">
            ${field}
        </foreach>
    </if>
    <if test="fields == null or fields.size() == 0">
        *
    </if>
    FROM ${tableName}
    <where>
        <if test="conditions != null">
            <foreach collection="conditions" item="condition" separator=" AND ">
                ${condition.field} ${condition.operator} #{condition.value}
            </foreach>
        </if>
    </where>
    <if test="orderBy != null and orderBy != ''">
        ORDER BY ${orderBy}
    </if>
    <if test="orderDirection != null and orderDirection != ''">
        ${orderDirection}
    </if>
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("tableName", "users");
params.put("fields", Arrays.asList("id", "name", "email", "create_time"));
params.put("orderBy", "create_time");
params.put("orderDirection", "DESC");

// 动态条件构建
List<Map<String, Object>> conditions = new ArrayList<>();
Map<String, Object> condition1 = new HashMap<>();
condition1.put("field", "status");
condition1.put("operator", "=");
condition1.put("value", "active");
conditions.add(condition1);

Map<String, Object> condition2 = new HashMap<>();
condition2.put("field", "age");
condition2.put("operator", ">");
condition2.put("value", 18);
conditions.add(condition2);

params.put("conditions", conditions);

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersDynamicFields", params);
// 生成SQL: SELECT id,name,email,create_time FROM users WHERE status = ? AND age > ? ORDER BY create_time DESC
```

### 13. 复杂的JEXL表达式应用

`MyBatisSqlExecutor` 使用JEXL引擎，支持复杂的表达式计算和方法调用。

**示例：JEXL表达式高级用法**
```xml
<select id="selectUsersWithJexlExpressions" parameterType="map" resultType="map">
    <bind name="currentYear" value="@java.time.Year@now().getValue()"/>
    <bind name="adultAge" value="currentYear - 18"/>
    <bind name="searchPattern" value="searchKeyword != null ? ('%' + searchKeyword.trim().toLowerCase() + '%') : null"/>

    SELECT *,
           (${currentYear} - YEAR(birth_date)) as calculated_age
    FROM users
    <where>
        <!-- 使用JEXL的数学运算 -->
        <if test="ageFilters != null">
            <if test="ageFilters.minAge != null">
                AND (${currentYear} - YEAR(birth_date)) >= #{ageFilters.minAge}
            </if>
            <if test="ageFilters.maxAge != null">
                AND (${currentYear} - YEAR(birth_date)) &lt;= #{ageFilters.maxAge}
            </if>
        </if>

        <!-- 使用JEXL的字符串方法 -->
        <if test="searchKeyword != null and searchKeyword.trim() != ''">
            AND (
                LOWER(name) LIKE #{searchPattern}
                OR LOWER(email) LIKE #{searchPattern}
            )
        </if>

        <!-- 使用JEXL的复杂条件判断 -->
        <if test="userType != null">
            <choose>
                <when test="userType == 'adult' and adultAge > 0">
                    AND (${currentYear} - YEAR(birth_date)) >= 18
                </when>
                <when test="userType == 'minor'">
                    AND (${currentYear} - YEAR(birth_date)) &lt; 18
                </when>
                <when test="userType == 'senior'">
                    AND (${currentYear} - YEAR(birth_date)) >= 65
                </when>
            </choose>
        </if>

        <!-- 使用JEXL的集合操作 -->
        <if test="excludeStatuses != null and excludeStatuses.size() > 0">
            AND status NOT IN
            <foreach collection="excludeStatuses" item="status" open="(" close=")" separator=",">
                #{status}
            </foreach>
        </if>

        <!-- 使用JEXL的条件表达式 -->
        <if test="priorityUsers != null and priorityUsers == true">
            AND (
                level = 'VIP'
                OR score >= ${(scoreThreshold != null ? scoreThreshold : 1000)}
                OR create_time >= DATE_SUB(NOW(), INTERVAL ${(recentDays != null ? recentDays : 30)} DAY)
            )
        </if>
    </where>
    ORDER BY
    <choose>
        <when test="sortBy != null and sortBy == 'age'">
            calculated_age ${sortDirection != null ? sortDirection : 'ASC'}
        </when>
        <when test="sortBy != null and sortBy == 'name'">
            name ${sortDirection != null ? sortDirection : 'ASC'}
        </when>
        <otherwise>
            create_time DESC
        </otherwise>
    </choose>
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("searchKeyword", "  张三  ");  // 测试trim()功能
params.put("userType", "adult");
params.put("priorityUsers", true);
params.put("scoreThreshold", 1500);
params.put("recentDays", 60);
params.put("sortBy", "age");
params.put("sortDirection", "DESC");

Map<String, Object> ageFilters = new HashMap<>();
ageFilters.put("minAge", 25);
ageFilters.put("maxAge", 45);
params.put("ageFilters", ageFilters);

params.put("excludeStatuses", Arrays.asList("deleted", "suspended"));

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersWithJexlExpressions", params);
```

### 14. 分页和排序的高级用法

结合动态SQL实现复杂的分页和排序逻辑。

**示例：动态分页和多字段排序**
```xml
<select id="selectUsersWithPagination" parameterType="map" resultType="map">
    <bind name="offset" value="(pageNum - 1) * pageSize"/>

    SELECT
        u.*,
        d.name as department_name,
        COUNT(*) OVER() as total_count
    FROM users u
    <if test="includeDepartment != null and includeDepartment == true">
        LEFT JOIN departments d ON u.department_id = d.id
    </if>
    <where>
        <if test="filters != null">
            <if test="filters.keyword != null and filters.keyword != ''">
                AND (
                    u.name LIKE CONCAT('%', #{filters.keyword}, '%')
                    OR u.email LIKE CONCAT('%', #{filters.keyword}, '%')
                    <if test="includeDepartment != null and includeDepartment == true">
                        OR d.name LIKE CONCAT('%', #{filters.keyword}, '%')
                    </if>
                )
            </if>

            <if test="filters.statusList != null and filters.statusList.size() > 0">
                AND u.status IN
                <foreach collection="filters.statusList" item="status" open="(" close=")" separator=",">
                    #{status}
                </foreach>
            </if>

            <if test="filters.createTimeRange != null">
                <if test="filters.createTimeRange.start != null">
                    AND u.create_time >= #{filters.createTimeRange.start}
                </if>
                <if test="filters.createTimeRange.end != null">
                    AND u.create_time &lt;= #{filters.createTimeRange.end}
                </if>
            </if>
        </if>
    </where>

    <!-- 动态多字段排序 -->
    <if test="sorting != null and sorting.size() > 0">
        ORDER BY
        <foreach collection="sorting" item="sort" separator=",">
            <choose>
                <when test="sort.field == 'name'">
                    u.name ${sort.direction}
                </when>
                <when test="sort.field == 'email'">
                    u.email ${sort.direction}
                </when>
                <when test="sort.field == 'createTime'">
                    u.create_time ${sort.direction}
                </when>
                <when test="sort.field == 'departmentName' and includeDepartment == true">
                    d.name ${sort.direction}
                </when>
                <otherwise>
                    u.id ${sort.direction}
                </otherwise>
            </choose>
        </foreach>
    </if>
    <if test="sorting == null or sorting.size() == 0">
        ORDER BY u.create_time DESC
    </if>

    <!-- 分页限制 -->
    <if test="pageSize != null and pageSize > 0">
        LIMIT #{pageSize}
        <if test="pageNum != null and pageNum > 0">
            OFFSET ${offset}
        </if>
    </if>
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("pageNum", 2);
params.put("pageSize", 20);
params.put("includeDepartment", true);

// 过滤条件
Map<String, Object> filters = new HashMap<>();
filters.put("keyword", "张");
filters.put("statusList", Arrays.asList("active", "pending"));

Map<String, Object> createTimeRange = new HashMap<>();
createTimeRange.put("start", "2024-01-01 00:00:00");
createTimeRange.put("end", "2024-12-31 23:59:59");
filters.put("createTimeRange", createTimeRange);

params.put("filters", filters);

// 多字段排序
List<Map<String, Object>> sorting = new ArrayList<>();
Map<String, Object> sort1 = new HashMap<>();
sort1.put("field", "departmentName");
sort1.put("direction", "ASC");
sorting.add(sort1);

Map<String, Object> sort2 = new HashMap<>();
sort2.put("field", "name");
sort2.put("direction", "ASC");
sorting.add(sort2);

Map<String, Object> sort3 = new HashMap<>();
sort3.put("field", "createTime");
sort3.put("direction", "DESC");
sorting.add(sort3);

params.put("sorting", sorting);

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersWithPagination", params);
```

### 15. 批量操作的高级应用

结合动态SQL实现复杂的批量操作。

**示例：条件批量更新**
```xml
<update id="batchUpdateUsers" parameterType="map">
    <foreach collection="users" item="user" separator=";">
        UPDATE users
        <set>
            <if test="user.name != null and user.name != ''">
                name = #{user.name},
            </if>
            <if test="user.email != null and user.email != ''">
                email = #{user.email},
            </if>
            <if test="user.phone != null and user.phone != ''">
                phone = #{user.phone},
            </if>
            <if test="user.status != null">
                status = #{user.status},
            </if>
            <if test="updateTime != null">
                update_time = #{updateTime}
            </if>
            <if test="updateTime == null">
                update_time = NOW()
            </if>
        </set>
        WHERE id = #{user.id}
        <if test="user.version != null">
            AND version = #{user.version}
        </if>
    </foreach>
</update>
```

### 16. 与SimpleSqlExecutor的对比总结

| 特性对比 | SimpleSqlExecutor | MyBatisSqlExecutor |
|---------|------------------|-------------------|
| **参数占位符** | `#{}`, `${}` | `#{}`, `${}` |
| **条件标签** | 简单 `<if>` | 完整MyBatis标签集 |
| **表达式引擎** | 简单字符串比较 | Apache Commons JEXL |
| **嵌套支持** | 有限 | 无限层级嵌套 |
| **片段复用** | 不支持 | `<sql>` + `<include>` |
| **循环处理** | 不支持 | `<foreach>` 完整支持 |
| **条件分支** | 不支持 | `<choose>/<when>/<otherwise>` |
| **字符串处理** | 基础 | `<trim>`, `<where>`, `<set>` |
| **变量绑定** | 不支持 | `<bind>` 支持JEXL表达式 |
| **性能表现** | 更快（解析简单） | 功能强大（解析复杂） |
| **学习成本** | 低 | 中等（需了解MyBatis语法） |
| **适用场景** | 简单查询和轻量级应用 | 复杂业务逻辑和企业级应用 |
| **调试支持** | 基础日志 | 详细的SQL生成和参数日志 |

### 17. 性能优化和最佳实践

**MyBatisSqlExecutor性能优化建议：**

1. **合理使用缓存**
```java
// SQL片段会自动缓存，避免重复解析
// 对于频繁使用的SQL，考虑使用<include>引用公共片段
```

2. **避免过度复杂的嵌套**
```xml
<!-- 推荐：逻辑清晰的嵌套 -->
<where>
    <if test="basicFilter != null">
        <if test="basicFilter.name != null">
            AND name = #{basicFilter.name}
        </if>
    </if>
</where>

<!-- 避免：过度深层嵌套 -->
<where>
    <if test="filters != null">
        <if test="filters.user != null">
            <if test="filters.user.profile != null">
                <if test="filters.user.profile.personal != null">
                    <if test="filters.user.profile.personal.name != null">
                        AND name = #{filters.user.profile.personal.name}
                    </if>
                </if>
            </if>
        </if>
    </if>
</where>
```

3. **参数预处理优化**
```java
// 在Java代码中预处理复杂逻辑，减少XML中的计算
Map<String, Object> params = new HashMap<>();
String processedKeyword = searchKeyword != null ? searchKeyword.trim().toLowerCase() : null;
params.put("processedKeyword", processedKeyword);
params.put("hasKeyword", processedKeyword != null && !processedKeyword.isEmpty());
```

4. **调试和监控**
```java
// 开启详细日志进行性能分析
DatabaseUtil31.LoggerCore.setDebugLogEnabled(true);
DatabaseUtil31.LoggerCore.setSqlDebugLogEnabled(true);

// 在生产环境中，可以通过日志分析SQL生成时间和执行性能
```

---

## 实战案例集合

### 18. 常见业务场景实战

#### 18.1 用户权限管理系统

**场景描述：** 实现基于角色的用户权限查询，支持多层级权限继承和动态权限过滤。

```xml
<select id="selectUsersWithPermissions" parameterType="map" resultType="map">
    <bind name="hasRoleFilter" value="roleIds != null and roleIds.size() > 0"/>
    <bind name="hasPermissionFilter" value="permissionCodes != null and permissionCodes.size() > 0"/>

    SELECT DISTINCT
        u.id, u.username, u.email, u.status,
        GROUP_CONCAT(DISTINCT r.name) as role_names,
        GROUP_CONCAT(DISTINCT p.code) as permission_codes
    FROM users u
    <if test="hasRoleFilter or hasPermissionFilter">
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
    </if>
    <if test="hasPermissionFilter">
        LEFT JOIN role_permissions rp ON r.id = rp.role_id
        LEFT JOIN permissions p ON rp.permission_id = p.id
    </if>
    <where>
        u.deleted = 0

        <!-- 角色过滤 -->
        <if test="hasRoleFilter">
            AND r.id IN
            <foreach collection="roleIds" item="roleId" open="(" close=")" separator=",">
                #{roleId}
            </foreach>
        </if>

        <!-- 权限过滤 -->
        <if test="hasPermissionFilter">
            AND p.code IN
            <foreach collection="permissionCodes" item="code" open="(" close=")" separator=",">
                #{code}
            </foreach>
        </if>

        <!-- 状态过滤 -->
        <if test="userStatus != null">
            AND u.status = #{userStatus}
        </if>

        <!-- 部门层级过滤 -->
        <if test="departmentPath != null and departmentPath != ''">
            AND EXISTS (
                SELECT 1 FROM departments d
                WHERE d.id = u.department_id
                AND d.path LIKE CONCAT(#{departmentPath}, '%')
            )
        </if>
    </where>
    GROUP BY u.id, u.username, u.email, u.status

    <!-- 权限数量过滤 -->
    <if test="minPermissionCount != null">
        HAVING COUNT(DISTINCT p.id) >= #{minPermissionCount}
    </if>

    ORDER BY u.username
</select>
```

**Java调用示例：**
```java
Map<String, Object> params = new HashMap<>();
params.put("roleIds", Arrays.asList(1L, 2L, 3L));
params.put("permissionCodes", Arrays.asList("USER_READ", "USER_WRITE", "ADMIN_MANAGE"));
params.put("userStatus", "ACTIVE");
params.put("departmentPath", "/company/IT/");
params.put("minPermissionCount", 2);

List<Map<String, Object>> users = DatabaseUtil31.selectListFromXmlAutoByMp("selectUsersWithPermissions", params);
```

#### 18.2 电商订单查询系统

**场景描述：** 复杂的订单查询，包含商品信息、用户信息、支付状态、物流信息等多表关联。

```xml
<select id="selectOrdersAdvanced" parameterType="map" resultType="map">
    <bind name="dateFormat" value="'%Y-%m-%d'"/>
    <bind name="hasDateRange" value="filters != null and filters.dateRange != null"/>
    <bind name="hasAmountRange" value="filters != null and filters.amountRange != null"/>

    SELECT
        o.id as order_id,
        o.order_no,
        o.total_amount,
        o.status as order_status,
        o.create_time,
        u.username,
        u.phone,
        COUNT(oi.id) as item_count,
        SUM(oi.quantity) as total_quantity,
        <if test="includePayment != null and includePayment == true">
            p.payment_method,
            p.payment_status,
            p.payment_time,
        </if>
        <if test="includeShipping != null and includeShipping == true">
            s.shipping_address,
            s.shipping_status,
            s.tracking_number,
        </if>
        GROUP_CONCAT(DISTINCT pr.name ORDER BY pr.name) as product_names
    FROM orders o
    INNER JOIN users u ON o.user_id = u.id
    INNER JOIN order_items oi ON o.id = oi.order_id
    INNER JOIN products pr ON oi.product_id = pr.id
    <if test="includePayment != null and includePayment == true">
        LEFT JOIN payments p ON o.id = p.order_id
    </if>
    <if test="includeShipping != null and includeShipping == true">
        LEFT JOIN shipping s ON o.id = s.order_id
    </if>
    <where>
        o.deleted = 0

        <!-- 日期范围过滤 -->
        <if test="hasDateRange">
            <if test="filters.dateRange.start != null">
                AND DATE(o.create_time) >= #{filters.dateRange.start}
            </if>
            <if test="filters.dateRange.end != null">
                AND DATE(o.create_time) &lt;= #{filters.dateRange.end}
            </if>
        </if>

        <!-- 金额范围过滤 -->
        <if test="hasAmountRange">
            <if test="filters.amountRange.min != null">
                AND o.total_amount >= #{filters.amountRange.min}
            </if>
            <if test="filters.amountRange.max != null">
                AND o.total_amount &lt;= #{filters.amountRange.max}
            </if>
        </if>

        <!-- 订单状态过滤 -->
        <if test="orderStatuses != null and orderStatuses.size() > 0">
            AND o.status IN
            <foreach collection="orderStatuses" item="status" open="(" close=")" separator=",">
                #{status}
            </foreach>
        </if>

        <!-- 用户类型过滤 -->
        <if test="userLevel != null">
            <choose>
                <when test="userLevel == 'VIP'">
                    AND u.vip_level > 0
                </when>
                <when test="userLevel == 'NORMAL'">
                    AND (u.vip_level = 0 OR u.vip_level IS NULL)
                </when>
            </choose>
        </if>

        <!-- 商品分类过滤 -->
        <if test="categoryIds != null and categoryIds.size() > 0">
            AND EXISTS (
                SELECT 1 FROM order_items oi2
                INNER JOIN products pr2 ON oi2.product_id = pr2.id
                WHERE oi2.order_id = o.id
                AND pr2.category_id IN
                <foreach collection="categoryIds" item="categoryId" open="(" close=")" separator=",">
                    #{categoryId}
                </foreach>
            )
        </if>

        <!-- 支付方式过滤 -->
        <if test="includePayment == true and paymentMethods != null and paymentMethods.size() > 0">
            AND p.payment_method IN
            <foreach collection="paymentMethods" item="method" open="(" close=")" separator=",">
                #{method}
            </foreach>
        </if>
    </where>
    GROUP BY o.id, o.order_no, o.total_amount, o.status, o.create_time, u.username, u.phone
    <if test="includePayment == true">
        , p.payment_method, p.payment_status, p.payment_time
    </if>
    <if test="includeShipping == true">
        , s.shipping_address, s.shipping_status, s.tracking_number
    </if>

    <!-- 订单项数量过滤 -->
    <if test="minItemCount != null">
        HAVING item_count >= #{minItemCount}
    </if>

    ORDER BY o.create_time DESC

    <!-- 分页 -->
    <if test="limit != null and limit > 0">
        LIMIT #{limit}
        <if test="offset != null and offset > 0">
            OFFSET #{offset}
        </if>
    </if>
</select>
```

#### 18.3 数据统计分析系统

**场景描述：** 灵活的数据统计查询，支持多维度分组、时间粒度选择、指标计算等。

```xml
<select id="selectDataStatistics" parameterType="map" resultType="map">
    <bind name="timeGroupField" value="timeGroupBy == 'day' ? 'DATE(create_time)' :
                                      (timeGroupBy == 'week' ? 'YEARWEEK(create_time)' :
                                      (timeGroupBy == 'month' ? 'DATE_FORMAT(create_time, \\'%Y-%m\\')' :
                                      'YEAR(create_time)'))"/>

    SELECT
        ${timeGroupField} as time_period,
        <if test="groupByFields != null and groupByFields.size() > 0">
            <foreach collection="groupByFields" item="field" separator=",">
                ${field},
            </foreach>
        </if>

        <!-- 基础统计指标 -->
        COUNT(*) as total_count,
        COUNT(DISTINCT user_id) as unique_users,

        <!-- 金额统计 -->
        <if test="includeAmount != null and includeAmount == true">
            SUM(amount) as total_amount,
            AVG(amount) as avg_amount,
            MIN(amount) as min_amount,
            MAX(amount) as max_amount,
        </if>

        <!-- 自定义指标计算 -->
        <if test="customMetrics != null">
            <foreach collection="customMetrics" item="metric">
                <choose>
                    <when test="metric.type == 'SUM'">
                        SUM(${metric.field}) as ${metric.alias},
                    </when>
                    <when test="metric.type == 'AVG'">
                        AVG(${metric.field}) as ${metric.alias},
                    </when>
                    <when test="metric.type == 'COUNT_DISTINCT'">
                        COUNT(DISTINCT ${metric.field}) as ${metric.alias},
                    </when>
                    <when test="metric.type == 'PERCENTAGE'">
                        ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM ${tableName}), 2) as ${metric.alias},
                    </when>
                </choose>
            </foreach>
        </if>

        <!-- 条件计数 -->
        <if test="conditionalCounts != null">
            <foreach collection="conditionalCounts" item="condition">
                SUM(CASE WHEN ${condition.expression} THEN 1 ELSE 0 END) as ${condition.alias},
            </foreach>
        </if>

        <!-- 环比、同比计算 -->
        <if test="includePeriodComparison != null and includePeriodComparison == true">
            LAG(COUNT(*), 1) OVER (ORDER BY ${timeGroupField}) as prev_period_count,
            ROUND((COUNT(*) - LAG(COUNT(*), 1) OVER (ORDER BY ${timeGroupField})) * 100.0 /
                  LAG(COUNT(*), 1) OVER (ORDER BY ${timeGroupField}), 2) as growth_rate
        </if>

    FROM ${tableName}
    <where>
        <!-- 时间范围过滤 -->
        <if test="dateRange != null">
            <if test="dateRange.start != null">
                AND create_time >= #{dateRange.start}
            </if>
            <if test="dateRange.end != null">
                AND create_time &lt;= #{dateRange.end}
            </if>
        </if>

        <!-- 动态过滤条件 -->
        <if test="filters != null">
            <foreach collection="filters" item="filter">
                <choose>
                    <when test="filter.operator == 'IN'">
                        AND ${filter.field} IN
                        <foreach collection="filter.values" item="value" open="(" close=")" separator=",">
                            #{value}
                        </foreach>
                    </when>
                    <when test="filter.operator == 'BETWEEN'">
                        AND ${filter.field} BETWEEN #{filter.values[0]} AND #{filter.values[1]}
                    </when>
                    <when test="filter.operator == 'LIKE'">
                        AND ${filter.field} LIKE CONCAT('%', #{filter.values[0]}, '%')
                    </when>
                    <otherwise>
                        AND ${filter.field} ${filter.operator} #{filter.values[0]}
                    </otherwise>
                </choose>
            </foreach>
        </if>
    </where>
    GROUP BY
        ${timeGroupField}
        <if test="groupByFields != null and groupByFields.size() > 0">
            <foreach collection="groupByFields" item="field">
                , ${field}
            </foreach>
        </if>

    <!-- HAVING子句 -->
    <if test="havingConditions != null">
        HAVING
        <foreach collection="havingConditions" item="condition" separator=" AND ">
            ${condition.expression}
        </foreach>
    </if>

    ORDER BY time_period
    <if test="orderByFields != null and orderByFields.size() > 0">
        <foreach collection="orderByFields" item="orderField">
            , ${orderField.field} ${orderField.direction}
        </foreach>
    </if>
</select>
```

### 19. 实际项目集成指南

#### 19.1 Spring Boot集成

**配置类示例：**
```java
@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseUtilConfig {

    @Autowired
    private DatabaseProperties properties;

    @PostConstruct
    public void initializeDatabaseUtil() {
        // 开启调试日志
        if (properties.isDebugEnabled()) {
            DatabaseUtil31.LoggerCore.setDebugLogEnabled(true);
            DatabaseUtil31.LoggerCore.setSqlDebugLogEnabled(true);
        }

        // 注册多数据源
        for (DatabaseProperties.DataSourceConfig config : properties.getDataSources()) {
            DatabaseUtil31.DataSourceCore.register(
                config.getName(),
                config.getUrl(),
                config.getUsername(),
                config.getPassword(),
                config.getDriverClassName()
            );
        }

        // 设置默认数据源
        if (properties.getDefaultDataSource() != null) {
            DatabaseUtil31.DataSourceCore.setDefaultDataSource(properties.getDefaultDataSource());
        }
    }
}
```

#### 19.2 Service层封装

**通用Service基类：**
```java
@Service
public abstract class BaseMyBatisService {

    protected <T> List<T> selectListByMp(String sqlId, Map<String, Object> params) {
        return (List<T>) DatabaseUtil31.selectListFromXmlAutoByMp(sqlId, params);
    }

    protected <T> T selectOneByMp(String sqlId, Map<String, Object> params) {
        return (T) DatabaseUtil31.selectOneFromXmlAutoByMp(sqlId, params);
    }

    protected int executeByMp(String sqlId, Map<String, Object> params) {
        return DatabaseUtil31.executeFromXmlAutoByMp(sqlId, params);
    }

    protected DatabaseAccessor transaction(String dataSourceName) {
        return DatabaseUtil31.accessor(dataSourceName).setTransaction();
    }

    protected Map<String, Object> buildParams(Object... keyValues) {
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            params.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return params;
    }
}
```

#### 19.3 错误处理和监控

**全局异常处理：**
```java
@ControllerAdvice
public class DatabaseExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(RuntimeException e) {
        if (e.getMessage().contains("SQL解析错误") || e.getMessage().contains("参数")) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("SQL_PARSE_ERROR", "动态SQL解析失败: " + e.getMessage()));
        }

        if (e.getMessage().contains("数据库未注册") || e.getMessage().contains("连接")) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("DATABASE_UNAVAILABLE", "数据库服务不可用: " + e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "系统内部错误"));
    }
}
```

### 20. 迁移指南和版本升级

#### 20.1 从MyBatis迁移

**步骤1：Mapper接口适配**
```java
// 原MyBatis方式
@Mapper
public interface UserMapper {
    List<User> selectByCondition(@Param("name") String name, @Param("status") String status);
}

// DatabaseUtil31方式
@Service
public class UserService extends BaseMyBatisService {
    public List<Map<String, Object>> selectByCondition(String name, String status) {
        return selectListByMp("selectUsersByCondition", buildParams("name", name, "status", status));
    }
}
```

**步骤2：XML文件复用**
```xml
<!-- 原MyBatis XML可以直接复用 -->
<select id="selectUsersByCondition" parameterType="map" resultType="map">
    SELECT * FROM users
    <where>
        <if test="name != null and name != ''">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
    </where>
</select>
```

#### 20.2 性能对比测试

**基准测试代码：**
```java
@Component
public class PerformanceBenchmark {

    public void compareExecutors() {
        Map<String, Object> params = buildTestParams();
        int iterations = 1000;

        // SimpleSqlExecutor测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            DatabaseUtil31.selectList("SELECT * FROM users WHERE name = #{name}", params);
        }
        long simpleTime = System.nanoTime() - startTime;

        // MyBatisSqlExecutor测试
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            DatabaseUtil31.selectListByMp("SELECT * FROM users <where><if test='name != null'>AND name = #{name}</if></where>", params);
        }
        long mybatisTime = System.nanoTime() - startTime;

        System.out.println("SimpleSqlExecutor: " + (simpleTime / 1_000_000) + "ms");
        System.out.println("MyBatisSqlExecutor: " + (mybatisTime / 1_000_000) + "ms");
        System.out.println("Performance Ratio: " + ((double) mybatisTime / simpleTime));
    }
}
```

### 21. 常见问题解答 (FAQ)

#### Q1: 如何处理SQL注入问题？
**A:** 使用 `#{}` 参数化查询，避免直接使用 `${}` 拼接用户输入。对于必须使用 `${}` 的场景（如动态表名），在业务层进行严格校验。

#### Q2: 大数据量查询如何优化？
**A:**
1. 使用分页查询避免一次性加载大量数据
2. 合理使用索引，确保WHERE条件包含索引列
3. 避免在循环中执行SQL，使用批量操作
4. 考虑使用数据库视图简化复杂查询

#### Q3: 如何实现读写分离？
**A:**
```java
// 写操作使用主库
DatabaseUtil31.DataSourceCore.setThreadDataSourceContext("master");
int result = DatabaseUtil31.executeByMp(sql, params);

// 读操作使用从库
DatabaseUtil31.DataSourceCore.setThreadDataSourceContext("slave");
List<Map<String, Object>> data = DatabaseUtil31.selectListByMp(sql, params);

// 清理线程上下文
DatabaseUtil31.DataSourceCore.clearThreadDataSourceContext();
```

#### Q4: 如何实现分布式事务？
**A:** DatabaseUtil31主要支持单数据源事务。对于分布式事务，建议：
1. 使用消息队列实现最终一致性
2. 采用Saga模式
3. 集成专业的分布式事务框架（如Seata）

---

## 附录

### A. 完整的标签参考表

| 标签 | 属性 | 说明 | 示例 |
|------|------|------|------|
| `<if>` | `test` | 条件表达式 | `<if test="name != null">` |
| `<where>` | 无 | 智能WHERE子句 | `<where>...</where>` |
| `<set>` | 无 | 智能SET子句 | `<set>...</set>` |
| `<choose>` | 无 | 选择容器 | `<choose>...</choose>` |
| `<when>` | `test` | 条件分支 | `<when test="status == 'active'">` |
| `<otherwise>` | 无 | 默认分支 | `<otherwise>...</otherwise>` |
| `<foreach>` | `collection`, `item`, `index`, `open`, `close`, `separator` | 循环遍历 | `<foreach collection="ids" item="id">` |
| `<trim>` | `prefix`, `suffix`, `prefixOverrides`, `suffixOverrides` | 字符串修剪 | `<trim prefix="WHERE">` |
| `<bind>` | `name`, `value` | 变量绑定 | `<bind name="pattern" value="'%' + keyword + '%'"/>` |
| `<sql>` | `id` | SQL片段定义 | `<sql id="userColumns">` |
| `<include>` | `refid` | 引用SQL片段 | `<include refid="userColumns"/>` |

### B. JEXL表达式参考

| 操作符 | 说明 | 示例 |
|--------|------|------|
| `==`, `!=` | 相等/不等 | `status == 'active'` |
| `>`, `>=`, `<`, `<=` | 比较 | `age > 18` |
| `and`, `or` | 逻辑运算 | `name != null and name != ''` |
| `+`, `-`, `*`, `/` | 数学运算 | `score * 0.8` |
| `.` | 属性访问 | `user.profile.age` |
| `[]` | 索引访问 | `items[0]` |
| `()` | 方法调用 | `name.toUpperCase()` |
| `? :` | 三元运算 | `age > 18 ? 'adult' : 'minor'` |

### C. 调试和日志配置

**日志级别说明：**
```java
// 基础调试日志：显示操作步骤和参数
DatabaseUtil31.LoggerCore.setDebugLogEnabled(true);

// SQL执行日志：显示完整SQL和执行结果
DatabaseUtil31.LoggerCore.setSqlDebugLogEnabled(true);

// 手动日志输出
DatabaseUtil31.LoggerCore.debugLog("自定义调试信息");
DatabaseUtil31.LoggerCore.infoLog("重要信息记录");
DatabaseUtil31.LoggerCore.warnLog("警告信息");
DatabaseUtil31.LoggerCore.debugErrorLog("错误信息");
```

这份完整的文档现在涵盖了从基础使用到高级实战的所有场景，为开发者提供了全面的技术指导和实践参考。
