# DatabaseUtil 增强API (MyBatisSqlExecutorEnhanced) 使用指南

本文档专注于介绍 `DatabaseUtil` 中最强大、最现代的API体系——基于 `MyBatisSqlExecutorEnhanced` 的强类型对象API。这套API彻底告别了繁琐的 `Map` 转换，让您能以完全面向对象的方式进行数据库操作，极大提升开发效率和代码健壮性。

## 目录

- [1. 核心优势](#1-核心优势)
- [2. 顶层静态API](#2-顶层静态api)
  - [2.1 模式一：直接提供SQL](#21-模式一直接提供sql)
  - [2.2 模式二：从指定XML文件加载SQL](#22-模式二从指定xml文件加载sql)
  - [2.3 模式三：自动搜索XML文件加载SQL](#23-模式三自动搜索xml文件加载sql)
- [3. 链式访问器API (`accessor`)](#3-链式访问器api-accessor)
  - [3.1 核心概念与优势](#31-核心概念与优势)
  - [3.2 模式一：直接提供SQL](#32-模式一直接提供sql)
  - [3.3 模式二：从指定XML文件加载SQL](#33-模式二从指定xml文件加载sql)
  - [3.4 模式三：自动搜索XML文件加载SQL](#34-模式三自动搜索xml文件加载sql)
  - [3.5 事务管理](#35-事务管理)
- [4. 分页查询专题 (`selectPage...Enhanced`)](#4-分页查询专题-selectpageenhanced)
  - [4.1 `PageResult<R>` 结果对象详解](#41-pageresultr-结果对象详解)
  - [4.2 模式一：直接提供SQL的分页](#42-模式一直接提供sql的分页)
  - [4.3 模式二：从指定XML文件加载SQL的分页](#43-模式二从指定xml文件加载sql的分页)
  - [4.4 模式三：自动搜索XML文件加载SQL的分页](#44-模式三自动搜索xml文件加载sql的分页)

---

## 1. 核心优势

*   🚀 **原生对象支持**：入参和出参直接使用Java对象（DTO/VO），无需任何手动转换。
*   🔒 **编译期类型安全**：在编码阶段即可发现类型不匹配的错误，极大提升代码健壮性。
*   ✨ **代码极致简洁**：告别 `Map` 的创建和取值，业务逻辑更清晰，代码量显著减少。
*   ⚡ **功能全面强大**：完整支持MyBatis动态SQL、事务管理和自动分页。

---

## 2. 顶层静态API

这是最简单直接的使用方式，通过 `DatabaseUtil.静态方法(...)` 直接调用。适合于无事务、单次数据库操作的场景。

### 2.1 模式一：直接提供SQL

#### 2.1.1 `selectListByMpEnhanced`
此方法用于执行一个期望返回**对象列表**的查询。它是最常用的查询方法之一，适用于获取所有用户、查询符合条件的订单列表等场景。如果查询没有匹配到任何记录，它将安全地返回一个空的 `List`，避免了空指针异常。

**API 签名**
```java
public static <T, R> List<R> selectListByMpEnhanced(String sql, T inputParam, Class<R> outputType)
```

**参数详解**
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `sql` | `String` | 包含MyBatis动态标签的SQL语句。 |
| `inputParam` | `T` (泛型) | 输入参数。可以是简单类型（如 `Long`, `String`），也可以是复杂的DTO对象。 |
| `outputType` | `Class<R>` |期望返回的列表中对象的类型，通常是一个VO类的 `.class`。 |

**返回值**: `List<R>`

**完整示例**
```java
import java.util.List;
import cn.myslayers.app.utils.DatabaseUtil;

class UserQueryDTO {
    private String status;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
class UserVO {
    private Long id;
    private String name;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    @Override
    public String toString() { return "UserVO{id=" + id + ", name='" + name + "'}"; }
}

public class TestDirectSqlList {
    public static void main(String[] args) {
        UserQueryDTO query = new UserQueryDTO();
        query.setStatus("ACTIVE");
        String sql = "<select>SELECT id, name FROM users <if test='status != null'>WHERE status = #{status}</if></select>";
        List<UserVO> users = DatabaseUtil.selectListByMpEnhanced(sql, query, UserVO.class);
        users.forEach(System.out::println);
    }
}
```

#### 2.1.2 `selectOneByMpEnhanced`
此方法用于执行一个期望返回**单一结果**的查询。它非常适合于通过唯一ID获取实体、查询某个聚合值（如`COUNT(*)`）或任何只应匹配一条记录的场景。如果查询匹配到多条记录，MyBatis通常会抛出`TooManyResultsException`异常，这有助于保证数据的一致性。如果查询没有找到任何匹配的记录，此方法将返回`null`。

**API 签名**
```java
public static <T, R> R selectOneByMpEnhanced(String sql, T inputParam, Class<R> outputType)
```
**参数详解**
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `sql` | `String` | 包含MyBatis动态标签的SQL语句。 |
| `inputParam` | `T` (泛型) | 输入参数。 |
| `outputType` | `Class<R>` | 期望返回的单个对象的类型。 |

**返回值**: `R`

**完整示例**
```java
import cn.myslayers.app.utils.DatabaseUtil;
// UserVO class from above example

public class TestDirectSqlOne {
    public static void main(String[] args) {
        Long userId = 101L;
        String sql = "SELECT id, name FROM users WHERE id = #{value}";
        UserVO user = DatabaseUtil.selectOneByMpEnhanced(sql, userId, UserVO.class);
        System.out.println(user);
    }
}
```

#### 2.1.3 `executeByMpEnhanced`
此方法用于执行 `INSERT`, `UPDATE`, `DELETE` 等所有**写操作**。它返回一个整数，表示本次操作影响的数据库行数，这对于验证操作是否成功非常有用。例如，一个更新操作如果返回0，可能意味着没有找到匹配的记录。

**API 签名**
```java
public static <T> int executeByMpEnhanced(String sql, T inputParam)
```
**参数详解**
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `sql` | `String` | 包含MyBatis动态标签的SQL语句。 |
| `inputParam` | `T` (泛型) | 输入参数。 |

**返回值**: `int` (影响行数)

**完整示例**
```java
import cn.myslayers.app.utils.DatabaseUtil;

class UserUpdateDTO {
    private Long id;
    private String newEmail;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
}

public class TestDirectSqlExecute {
    public static void main(String[] args) {
        UserUpdateDTO updateData = new UserUpdateDTO();
        updateData.setId(101L);
        updateData.setNewEmail("new.email@example.com");
        String sql = "UPDATE users SET email = #{newEmail} WHERE id = #{id}";
        int affectedRows = DatabaseUtil.executeByMpEnhanced(sql, updateData);
        System.out.println("影响行数: " + affectedRows);
    }
}
```

### 2.2 模式二：从指定XML文件加载SQL
此模式将SQL与Java代码分离，提高了代码的可维护性，是企业级项目中的推荐实践。

#### 2.2.1 `selectListFromXmlByMpEnhanced`
通过指定XML文件的路径和SQL的唯一ID，执行一个返回列表的查询。

**API 签名**
```java
public static <T, R> List<R> selectListFromXmlByMpEnhanced(String xmlPath, String sqlId, T inputParam, Class<R> outputType)
```
**参数详解**
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `xmlPath` | `String` | XML文件的相对或绝对路径。 |
| `sqlId` | `String` | XML文件中SQL语句的唯一ID。 |
| `inputParam`| `T` | 输入参数对象。 |
| `outputType`| `Class<R>` | 期望返回的对象类型。 |

**完整示例**

**`src/main/resources/mapper/user_mapper.xml`**
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="user">
    <select id="findActiveUsers" resultType="cn.myslayers.app.vo.UserVO">
        SELECT id, name, email FROM users WHERE status = 'ACTIVE'
    </select>
</mapper>
```

**Java 代码**
```java
import java.util.List;
import cn.myslayers.app.utils.DatabaseUtil;
// UserVO class from above example

public class TestXmlList {
    public static void main(String[] args) {
        List<UserVO> users = DatabaseUtil.selectListFromXmlByMpEnhanced(
            "mapper/user_mapper.xml",
            "findActiveUsers",
            null, // 无参数
            UserVO.class
        );
        users.forEach(System.out::println);
    }
}
```

#### 2.2.2 `selectOneFromXmlByMpEnhanced`
通过指定XML文件的路径和SQL的唯一ID，执行一个返回单对象的查询。

**API 签名**
```java
public static <T, R> R selectOneFromXmlByMpEnhanced(String xmlPath, String sqlId, T inputParam, Class<R> outputType)
```
**参数详解**
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `xmlPath` | `String` | XML文件的相对或绝对路径。 |
| `sqlId` | `String` | XML文件中SQL语句的唯一ID。 |
| `inputParam`| `T` | 输入参数对象。 |
| `outputType`| `Class<R>` | 期望返回的对象类型。 |

**完整示例**

**`src/main/resources/mapper/user_mapper.xml`**
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="user">
    <select id="findUserById" resultType="cn.myslayers.app.vo.UserVO">
        SELECT id, name, email FROM users WHERE id = #{value}
    </select>
</mapper>
```

**Java 代码**
```java
import cn.myslayers.app.utils.DatabaseUtil;
// UserVO class from above example

public class TestXmlOne {
    public static void main(String[] args) {
        Long userIdToFind = 101L;
        UserVO user = DatabaseUtil.selectOneFromXmlByMpEnhanced(
            "mapper/user_mapper.xml",
            "findUserById",
            userIdToFind,
            UserVO.class
        );
        System.out.println(user);
    }
}
```

#### 2.2.3 `executeFromXmlByMpEnhanced`
通过指定XML文件的路径和SQL的唯一ID，执行一个写操作。

**API 签名**
```java
public static <T> int executeFromXmlByMpEnhanced(String xmlPath, String sqlId, T inputParam)
```
**参数详解**
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `xmlPath` | `String` | XML文件的相对或绝对路径。 |
| `sqlId` | `String` | XML文件中SQL语句的唯一ID。 |
| `inputParam`| `T` | 输入参数对象。 |

**完整示例**

**`src/main/resources/mapper/user_mapper.xml`**
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="user">
    <update id="updateUserEmail">
        UPDATE users SET email = #{email} WHERE id = #{id}
    </update>
</mapper>
```

**Java 代码**
```java
import cn.myslayers.app.utils.DatabaseUtil;

class UserEmailUpdateDTO {
    private Long id;
    private String email;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

public class TestXmlExecute {
    public static void main(String[] args) {
        UserEmailUpdateDTO dto = new UserEmailUpdateDTO();
        dto.setId(101L);
        dto.setEmail("updated.via.xml@example.com");
        int rows = DatabaseUtil.executeFromXmlByMpEnhanced(
            "mapper/user_mapper.xml",
            "updateUserEmail",
            dto
        );
        System.out.println("影响行数: " + rows);
    }
}
```

### 2.3 模式三：自动搜索XML文件加载SQL
这是最便捷的XML使用方式。`DatabaseUtil`会自动扫描预设的目录，您只需提供全局唯一的SQL ID即可，无需关心它具体在哪个文件里。

#### 2.3.1 `selectListFromXmlAutoByMpEnhanced`
自动扫描并根据SQL ID执行一个返回列表的查询。

**API 签名**
```java
public static <T, R> List<R> selectListFromXmlAutoByMpEnhanced(String sqlId, T inputParam, Class<R> outputType)
```
**参数详解**
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `sqlId` | `String` | 在所有预设目录的XML中唯一的SQL ID。 |
| `inputParam`| `T` | 输入参数对象。 |
| `outputType`| `Class<R>` | 期望返回的对象类型。 |

**完整示例**
(假设 `user_mapper.xml` 已放置在 `src/main/resources/mapper/` 目录下)
```java
import java.util.List;
import cn.myslayers.app.utils.DatabaseUtil;
// UserVO class from above example

public class TestXmlAutoList {
    public static void main(String[] args) {
        // 无需指定XML路径，直接通过ID调用
        List<UserVO> users = DatabaseUtil.selectListFromXmlAutoByMpEnhanced(
            "findActiveUsers",
            null,
            UserVO.class
        );
        users.forEach(System.out::println);
    }
}
```

#### 2.3.2 `selectOneFromXmlAutoByMpEnhanced`
自动扫描并根据SQL ID执行一个返回单对象的查询。

**API 签名**
```java
public static <T, R> R selectOneFromXmlAutoByMpEnhanced(String sqlId, T inputParam, Class<R> outputType)
```
**参数详解**
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `sqlId` | `String` | 在所有预设目录的XML中唯一的SQL ID。 |
| `inputParam`| `T` | 输入参数对象。 |
| `outputType`| `Class<R>` | 期望返回的对象类型。 |

**完整示例**
```java
import cn.myslayers.app.utils.DatabaseUtil;
// UserVO class from above example

public class TestXmlAutoOne {
    public static void main(String[] args) {
        Long userIdToFind = 101L;
        UserVO user = DatabaseUtil.selectOneFromXmlAutoByMpEnhanced(
            "findUserById",
            userIdToFind,
            UserVO.class
        );
        System.out.println(user);
    }
}
```

#### 2.3.3 `executeFromXmlAutoByMpEnhanced`
自动扫描并根据SQL ID执行一个写操作。

**API 签名**
```java
public static <T> int executeFromXmlAutoByMpEnhanced(String sqlId, T inputParam)
```
**参数详解**
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `sqlId` | `String` | 在所有预设目录的XML中唯一的SQL ID。 |
| `inputParam`| `T` | 输入参数对象。 |

**完整示例**
```java
import cn.myslayers.app.utils.DatabaseUtil;
// UserEmailUpdateDTO class from above example

public class TestXmlAutoExecute {
    public static void main(String[] args) {
        UserEmailUpdateDTO dto = new UserEmailUpdateDTO();
        dto.setId(101L);
        dto.setEmail("updated.via.auto.xml@example.com");
        int rows = DatabaseUtil.executeFromXmlAutoByMpEnhanced("updateUserEmail", dto);
        System.out.println("影响行数: " + rows);
    }
}
```

---

## 3. 链式访问器API (`accessor`)

通过 `DatabaseUtil.accessor()` 获取访问器实例，进行链式调用。这是**推荐的使用方式**，尤其适合需要事务管理或连续操作的复杂业务场景。

### 3.1 核心概念与优势
- **事务管理**: 轻松地开启、提交或回滚事务。
- **数据源绑定**: 创建访问器时可以绑定一个特定的数据源，后续所有操作都将针对该数据源。
- **状态保持**: 可获取事务中累计影响的总行数。

### 3.2 模式一：直接提供SQL
链式访问器提供与静态API同名的方法，用法完全一致，但操作属于访问器实例。

**API 列表**
- `accessor.selectListByMpEnhanced(String sql, T params, Class<R> outputType)`
- `accessor.selectOneByMpEnhanced(String sql, T params, Class<R> outputType)`
- `accessor.executeByMpEnhanced(String sql, T params)`

**完整示例**
```java
import cn.myslayers.app.utils.DatabaseUtil;
import cn.myslayers.app.utils.DatabaseUtil.DatabaseAccessor;
// UserVO, UserUpdateDTO classes from above examples

public class TestAccessorDirectSql {
    public static void main(String[] args) {
        // 获取默认数据源的访问器
        DatabaseAccessor accessor = DatabaseUtil.accessor();

        // 查询单个对象
        UserVO user = accessor.selectOneByMpEnhanced(
            "SELECT id, name FROM users WHERE id = #{value}",
            101L,
            UserVO.class
        );
        System.out.println("查询结果: " + user);

        // 执行更新
        UserUpdateDTO updateData = new UserUpdateDTO();
        updateData.setId(101L);
        updateData.setNewEmail("new.email.via.accessor@example.com");
        int rows = accessor.executeByMpEnhanced(
            "UPDATE users SET email = #{newEmail} WHERE id = #{id}",
            updateData
        );
        System.out.println("更新影响行数: " + rows);
    }
}
```

### 3.3 模式二：从指定XML文件加载SQL
通过访问器实例，调用 `FromXml` 系列方法。

**API 列表**
- `accessor.selectListFromXmlByMpEnhanced(String xmlPath, String sqlId, T params, Class<R> outputType)`
- `accessor.selectOneFromXmlByMpEnhanced(String xmlPath, String sqlId, T params, Class<R> outputType)`
- `accessor.executeFromXmlByMpEnhanced(String xmlPath, String sqlId, T params)`

**完整示例**
```java
import cn.myslayers.app.utils.DatabaseUtil;
import cn.myslayers.app.utils.DatabaseUtil.DatabaseAccessor;
// UserVO class from above example

public class TestAccessorXml {
    public static void main(String[] args) {
        DatabaseAccessor accessor = DatabaseUtil.accessor();
        UserVO user = accessor.selectOneFromXmlByMpEnhanced(
            "mapper/user_mapper.xml",
            "findUserById",
            101L,
            UserVO.class
        );
        System.out.println("从XML查询到的用户: " + user);
    }
}
```

### 3.4 模式三：自动搜索XML文件加载SQL
通过访问器实例，调用 `FromXmlAuto` 系列方法。

**API 列表**
- `accessor.selectListFromXmlAutoByMpEnhanced(String sqlId, T params, Class<R> outputType)`
- `accessor.selectOneFromXmlAutoByMpEnhanced(String sqlId, T params, Class<R> outputType)`
- `accessor.executeFromXmlAutoByMpEnhanced(String sqlId, T params)`

**完整示例**
```java
import cn.myslayers.app.utils.DatabaseUtil;
import cn.myslayers.app.utils.DatabaseUtil.DatabaseAccessor;
// UserEmailUpdateDTO class from above example

public class TestAccessorXmlAuto {
    public static void main(String[] args) {
        DatabaseAccessor accessor = DatabaseUtil.accessor();
        UserEmailUpdateDTO dto = new UserEmailUpdateDTO();
        dto.setId(102L);
        dto.setEmail("updated.via.accessor.auto.xml@example.com");
        int rows = accessor.executeFromXmlAutoByMpEnhanced("updateUserEmail", dto);
        System.out.println("通过Accessor自动搜索XML更新，影响行数: " + rows);
    }
}
```

### 3.5 事务管理
这是链式访问器最核心的应用场景。所有在 `setTransaction()` 和 `commit()`/`rollback()` 之间执行的写操作，都将被纳入同一个事务。

**API 签名**
-   `accessor.setTransaction()`: 开启事务。
-   `accessor.commit()`: 提交事务。
-   `accessor.rollback()`: 回滚事务。

**完整示例：银行转账**
```java
import cn.myslayers.app.utils.DatabaseUtil;
import cn.myslayers.app.utils.DatabaseUtil.DatabaseAccessor;
import java.math.BigDecimal;

class TransferDTO {
    private Long accountId;
    private BigDecimal amount;
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

public class TestTransaction {
    public static void main(String[] args) {
        DatabaseAccessor accessor = DatabaseUtil.accessor();
        try {
            // 1. 开启事务
            accessor.setTransaction();

            // 2. 准备参数
            TransferDTO debitInfo = new TransferDTO();
            debitInfo.setAccountId(101L);
            debitInfo.setAmount(new BigDecimal("100.00"));

            TransferDTO creditInfo = new TransferDTO();
            creditInfo.setAccountId(202L);
            creditInfo.setAmount(new BigDecimal("100.00"));

            // 3. 扣款 (使用直接SQL)
            accessor.executeByMpEnhanced("UPDATE accounts SET balance = balance - #{amount} WHERE id = #{accountId}", debitInfo);

            // 4. 入款 (使用自动搜索XML)
            accessor.executeFromXmlAutoByMpEnhanced("creditAccountBalance", creditInfo);

            // 5. 提交事务
            accessor.commit();
            System.out.println("转账成功，事务已提交！");

        } catch (Exception e) {
            // 6. 如果发生任何异常，执行回滚
            accessor.rollback();
            System.err.println("转账失败，事务已回滚。错误: " + e.getMessage());
        }
    }
}
```

---

## 4. 分页查询专题 (`selectPage...Enhanced`)

专为大数据量查询设计，自动处理物理分页逻辑，返回包含分页信息的 `PageResult` 对象。

### 4.1 `PageResult<R>` 结果对象详解
所有分页查询都返回 `DatabaseUtil.PaginationHandler.PageResult<R>` 对象，它封装了分页的所有核心信息。
```java
public class PageResult<R> {
    private final List<R> list;      // 当前页的数据列表
    private final long total;        // 总记录数
    private final int pageNum;       // 当前页码
    private final int pageSize;      // 每页大小
    private final int pages;         // 总页数

    // 所有字段都有公开的getter方法
    public List<R> getList() { return list; }
    public long getTotal() { return total; }
    public int getPageNum() { return pageNum; }
    public int getPageSize() { return pageSize; }
    public int getPages() { return pages; }
}
```

### 4.2 模式一：直接提供SQL的分页
直接在代码中提供用于分页的SQL字符串。

**API 签名 (静态与链式)**
```java
// 静态方法
public static <T, R> PageResult<R> selectPageByMpEnhanced(String sql, int pageNum, int pageSize, T inputParam, Class<R> outputType)

// 链式访问器
public <T, R> PageResult<R> selectPageByMpEnhanced(String sql, int pageNum, int pageSize, T inputParam, Class<R> outputType)
```
**参数详解**
| 参数名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `sql` | `String` | 包含MyBatis动态标签的SQL语句。**注意**：不要包含 `LIMIT` 子句。 |
| `pageNum` | `int` | 要查询的页码，从1开始。 |
| `pageSize` | `int` | 每页显示的记录数。 |
| `inputParam`| `T` | 输入参数对象。 |
| `outputType`| `Class<R>` | 期望返回的对象类型。 |

**完整示例**
```java
import cn.myslayers.app.utils.DatabaseUtil;
import cn.myslayers.app.utils.DatabaseUtil.PaginationHandler.PageResult;
import java.util.List;

class OrderQueryDTO {
    private String customerNameKeyword;
    private List<String> statusList;
    public String getCustomerNameKeyword() { return customerNameKeyword; }
    public void setCustomerNameKeyword(String keyword) { this.customerNameKeyword = keyword; }
    public List<String> getStatusList() { return statusList; }
    public void setStatusList(List<String> statuses) { this.statusList = statuses; }
}

class OrderListVO {
    private String orderNo;
    private java.math.BigDecimal amount;
    private String status;
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @Override
    public String toString() { return "OrderListVO{orderNo='" + orderNo + "', amount=" + amount + ", status='" + status + "'}"; }
}

public class TestPaginationDirectSql {
    public static void main(String[] args) {
        OrderQueryDTO query = new OrderQueryDTO();
        query.setCustomerNameKeyword("张");
        query.setStatusList(java.util.Arrays.asList("PAID", "SHIPPED"));

        String sql = "<select>" +
                     "SELECT order_no, amount, status FROM orders " +
                     "<where>" +
                     "  <if test='customerNameKeyword != null'>customer_name LIKE CONCAT('%', #{customerNameKeyword}, '%')</if>" +
                     "  <if test='statusList != null and !statusList.isEmpty()'>" +
                     "    AND status IN <foreach item='item' collection='statusList' open='(' separator=',' close=')'>#{item}</foreach>" +
                     "  </if>" +
                     "</where> ORDER BY created_at DESC" +
                     "</select>";

        PageResult<OrderListVO> pageResult = DatabaseUtil.selectPageByMpEnhanced(
            sql, 1, 10, query, OrderListVO.class
        );

        System.out.println("总记录数: " + pageResult.getTotal());
        System.out.println("总页数: " + pageResult.getPages());
        System.out.println("当前页数据:");
        pageResult.getList().forEach(order -> System.out.println("  " + order));
    }
}
```

### 4.3 模式二：从指定XML文件加载SQL的分页
从指定的XML文件中加载SQL并执行分页查询。

**API 签名 (静态与链式)**
```java
// 静态方法
public static <T, R> PageResult<R> selectPageFromXmlByMpEnhanced(String xmlPath, String sqlId, int pageNum, int pageSize, T inputParam, Class<R> outputType)

// 链式访问器
public <T, R> PageResult<R> selectPageFromXmlByMpEnhanced(String xmlPath, String sqlId, int pageNum, int pageSize, T inputParam, Class<R> outputType)
```

### 4.4 模式三：自动搜索XML文件加载SQL的分页
自动扫描XML目录，根据SQL ID执行分页查询。

**API 签名 (静态与链式)**
```java
// 静态方法
public static <T, R> PageResult<R> selectPageFromXmlAutoByMpEnhanced(String sqlId, int pageNum, int pageSize, T inputParam, Class<R> outputType)

// 链式访问器
public <T, R> PageResult<R> selectPageFromXmlAutoByMpEnhanced(String sqlId, int pageNum, int pageSize, T inputParam, Class<R> outputType)
```
**完整示例**
```java
import cn.myslayers.app.utils.DatabaseUtil;
import cn.myslayers.app.utils.DatabaseUtil.PaginationHandler.PageResult;
// OrderQueryDTO, OrderListVO classes from above example

public class TestPaginationXmlAuto {
    public static void main(String[] args) {
        OrderQueryDTO query = new OrderQueryDTO();
        query.setStatusList(java.util.Arrays.asList("COMPLETED"));

        // 假设ID为 "findOrders" 的SQL已定义在某个XML文件中
        PageResult<OrderListVO> pageResult = DatabaseUtil.selectPageFromXmlAutoByMpEnhanced(
            "findOrders", 2, 5, query, OrderListVO.class
        );

        System.out.println("--- 第2页，每页5条 ---");
        System.out.println("总记录数: " + pageResult.getTotal());
        System.out.println("当前页数据:");
        pageResult.getList().forEach(order -> System.out.println("  " + order));
    }
}
