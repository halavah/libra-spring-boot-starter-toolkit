package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32.MyBatisSqlParser.MyBatisSqlWithParams;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * DatabaseUtil4 - Demo解析
 *
 * ## 测试目标:
 * 专注于验证 `MyBatisSqlParser` 从外部XML文件（Demo1, Demo2, Demo3, Demo4）中
 * 解析复杂、高级、甚至边缘情况的动态SQL的逻辑正确性。
 *
 * ## 测试方式:
 * - **结构化**：使用 JUnit 5 的 `@Nested` 将测试按源XML文件分组，逻辑清晰。
 * - **自动化验证**：通过一个强大的辅助方法 `executeAndVerify`，实现对SQL和参数的自动比较和结果输出，
 *   严格遵守“不使用断言”的要求，同时避免了人工比对的繁琐和易错。
 * - **不涉及数据库交互**，是纯粹的逻辑单元测试。
 *
 * ## 覆盖场景:
 * - **Demo1**: 覆盖所有MyBatis常用标签的基础和高级用法。
 * - **Demo2**: 覆盖真实世界中极其复杂的业务场景和高级SQL特性。
 * - **Demo3**: 专门测试 `<include>` 标签的各种高级和嵌套用法。
 * - **Demo4**: 专门测试各种边缘案例、特殊语法和复杂OGNL表达式。
 */
@DisplayName("MyBatisSqlParser - Demo.xml 解析")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Test22_ApiStaticMyBatisParserTesting {

    /**
     * 核心测试辅助方法
     *
     * @param testName      测试用例的名称，用于打印。
     * @param mapperFile    要测试的Mapper XML文件名。
     * @param sqlId         要测试的SQL语句的ID。
     * @param params        传入的参数。
     * @param expectedSql   期望生成的SQL语句。
     */
    private void executeAndVerify(String testName, String mapperFile, String sqlId, Map<String, Object> params, String expectedSql, List<Object> expectedParams) {
        System.out.println("\n--- Running Test: " + testName + " ---");
        System.out.println("Mapper: " + mapperFile + ", SQL ID: " + sqlId);
        System.out.println("Input Params: " + params);

        // 执行解析
        MyBatisSqlWithParams result = DatabaseUtil32.MyBatisSqlParser.getSqlWithParamsFromXml(mapperFile, sqlId, params);
        String actualSql = normalizeSql(result.getSql());
        List<Object> actualParams = result.getParams();
        String normalizedExpectedSql = normalizeSql(expectedSql);

        // 验证SQL和参数
        boolean sqlMatch = Objects.equals(actualSql, normalizedExpectedSql);
        boolean paramsMatch = Objects.equals(actualParams, expectedParams);

        if (sqlMatch && paramsMatch) {
            System.out.println("✅ " + testName + ": PASSED");
            System.out.println("   - Generated SQL: " + actualSql);
            System.out.println("   - Generated Params: " + actualParams);
        } else {
            System.out.println("❌ " + testName + ": FAILED");
            if (!sqlMatch) {
                System.out.println("  [SQL Mismatch]");
                System.out.println("  - Expected: " + normalizedExpectedSql);
                System.out.println("  - Actual  : " + actualSql);
            }
            if (!paramsMatch) {
                System.out.println("  [Params Mismatch]");
                System.out.println("  - Expected: " + expectedParams);
                System.out.println("  - Actual  : " + actualParams);
            }
        }
    }

    // 标准化SQL字符串，移除多余的空白、换行和制表符，使比较更可靠
    private String normalizeSql(String sql) {
        if (sql == null) {
            return null;
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    @BeforeAll
    void setup() {
        System.out.println("====================================================");
        System.out.println("  Starting Advanced XML Parsing Tests");
        System.out.println("====================================================");
    }

    @Test
    @Order(1)
    @DisplayName("Demo1Mapper.xml - 常用标签测试 - 测试 <if> 标签")
    void testSelectUsersByCondition() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "test");
        params.put("age", 30);
        String expectedSql = "SELECT * FROM users WHERE 1=1 AND name LIKE CONCAT('%', ?, '%') AND age = ?";
        List<Object> expectedParams = Arrays.asList("test", 30);
        executeAndVerify("testSelectUsersByCondition", "Demo1Mapper.xml", "selectUsersByCondition", params, expectedSql, expectedParams);
    }

    @Test
    @Order(2)
    @DisplayName("Demo1Mapper.xml - 常用标签测试 - 测试 <set> 标签")
    void testUpdateUserSelective() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1L);
        params.put("name", "new name");
        params.put("email", "new@email.com");
        String expectedSql = "UPDATE users SET name = ?, email = ?, update_time = NOW() WHERE id = ?";
        List<Object> expectedParams = Arrays.asList("new name", "new@email.com", 1L);
        executeAndVerify("testUpdateUserSelective", "Demo1Mapper.xml", "updateUserSelective", params, expectedSql, expectedParams);
    }

    @Test
    @Order(3)
    @DisplayName("Demo1Mapper.xml - 常用标签测试 - 测试 <foreach> 标签")
    void testSelectUsersByIds() {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList(1, 2, 3));
        String expectedSql = "SELECT * FROM users WHERE id IN ( ?,?,? )";
        List<Object> expectedParams = Arrays.asList(1, 2, 3);
        executeAndVerify("testSelectUsersByIds", "Demo1Mapper.xml", "selectUsersByIds", params, expectedSql, expectedParams);
    }

    @Test
    @Order(4)
    @DisplayName("Demo1Mapper.xml - 常用标签测试 - 测试 <include> 标签")
    void testSelectUsersWithInclude() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "test");
        String expectedSql = "SELECT id , name, age, email, phone, status, create_time, update_time FROM users WHERE name LIKE CONCAT('%', ?, '%') ORDER BY create_time DESC";
        List<Object> expectedParams = Collections.singletonList("test");
        executeAndVerify("testSelectUsersWithInclude", "Demo1Mapper.xml", "selectUsersWithInclude", params, expectedSql, expectedParams);
    }

    @Test
    @Order(5)
    @DisplayName("Demo2Mapper.xml - 高级复杂场景测试 - 复杂嵌套参数查询")
    void testSelectUsersAdvancedSearch() {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> basicSearch = new HashMap<>();
        basicSearch.put("keyword", "keyword");
        params.put("basicSearch", basicSearch);

        Map<String, Object> filters = new HashMap<>();
        Map<String, Object> ageRange = new HashMap<>();
        ageRange.put("min", 20);
        filters.put("ageRange", ageRange);
        filters.put("statuses", Arrays.asList("active", "pending"));
        params.put("filters", filters);

        String expectedSql = "SELECT * FROM users WHERE ( name LIKE CONCAT('%', ?, '%') OR email LIKE CONCAT('%', ?, '%') OR phone LIKE CONCAT('%', ?, '%') ) AND age >= ? AND status IN ( ?, ? )";
        List<Object> expectedParams = Arrays.asList("keyword", "keyword", "keyword", 20, "active", "pending");
        executeAndVerify("testSelectUsersAdvancedSearch", "Demo2Mapper.xml", "selectUsersAdvancedSearch", params, expectedSql, expectedParams);
    }

    @Test
    @Order(6)
    @DisplayName("Demo2Mapper.xml - 高级复杂场景测试 - 动态插入")
    void testInsertUserDynamic() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "dynamic_user");
        params.put("email", "dynamic@user.com");
        String expectedSql = "INSERT INTO users ( name, email ) VALUES ( ?, ? )";
        List<Object> expectedParams = Arrays.asList("dynamic_user", "dynamic@user.com");
        executeAndVerify("testInsertUserDynamic", "Demo2Mapper.xml", "insertUserDynamic", params, expectedSql, expectedParams);
    }

    @Test
    @Order(7)
    @DisplayName("Demo3Mapper.xml - <include> 高级用法测试 - 测试 <property> 子元素")
    void testFindWithProperties() {
        Map<String, Object> params = new HashMap<>();
        String expectedSql = "SELECT id, name, email FROM users WHERE status = 'active'";
        List<Object> expectedParams = Collections.emptyList();
        executeAndVerify("testFindWithProperties", "Demo3Mapper.xml", "findWithProperties", params, expectedSql, expectedParams);
    }

    @Test
    @Order(8)
    @DisplayName("Demo3Mapper.xml - <include> 高级用法测试 - 测试嵌套 <include>")
    void testFindWithNestedInclude() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        params.put("orderByColumn", "name");
        params.put("orderDirection", "DESC");
        String expectedSql = "SELECT id, name, email, status, created_at FROM users WHERE status = ? ORDER BY name DESC";
        List<Object> expectedParams = Collections.singletonList("active");
        executeAndVerify("testFindWithNestedInclude", "Demo3Mapper.xml", "findWithNestedInclude", params, expectedSql, expectedParams);
    }

    @Test
    @Order(9)
    @DisplayName("Demo4Mapper.xml - 边缘案例与特殊语法测试 - 测试XML实体字符")
    void testFindWithXmlEntitiesInTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("priority", 6);
        params.put("name", "special & co");
        String expectedSql = "SELECT * FROM records WHERE priority_level = 'high' AND name = ?";
        List<Object> expectedParams = Collections.singletonList("special & co");
        executeAndVerify("testFindWithXmlEntitiesInTest", "Demo4Mapper.xml", "findWithXmlEntitiesInTest", params, expectedSql, expectedParams);
    }

    @Test
    @Order(10)
    @DisplayName("Demo4Mapper.xml - 边缘案例与特殊语法测试 - 简单布尔值判断")
    void testFindByUserMethod() {
        Map<String, Object> params = new HashMap<>();
        params.put("isAdmin", true);
        params.put("canViewSensitiveData", true);

        String expectedSql = "SELECT * FROM users WHERE role = 'admin' AND can_view_sensitive = 1";
        List<Object> expectedParams = Collections.emptyList();
        executeAndVerify("testFindByUserMethod", "Demo4Mapper.xml", "findByUserMethod", params, expectedSql, expectedParams);
    }

    @Test
    @Order(11)
    @DisplayName("Demo4Mapper.xml - 边缘案例与特殊语法测试 - 测试 _parameter 内置变量")
    void testFindBySimpleParameter() {
        // MyBatis中，当只有一个简单类型参数时，会将其放入一个特殊的Map中，
        // key为"_parameter"或"0"，value为参数本身。我们的工具需要模拟这种行为。
        String paramValue = "test_user";
        String expectedSql = "SELECT * FROM users WHERE name = ?";
        List<Object> expectedParams = Collections.singletonList(paramValue);
        executeAndVerify("testFindBySimpleParameter", "Demo4Mapp" +
            "er.xml", "findBySimpleParameter", Collections.singletonMap("_parameter", paramValue), expectedSql, expectedParams);
    }
}
