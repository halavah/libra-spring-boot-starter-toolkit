package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * DatabaseUtil4 - Demo执行
 *
 * ## 测试目标:
 * 验证 `DatabaseUtil4` 提供的静态方法（如 `selectListFromXmlByMp`, `updateFromXmlByMp` 等）
 * 能够正确解析并执行XML中定义的MyBatis动态SQL，并与数据库进行实际交互。
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
@DisplayName("MyBatisSqlExecutor - Demo.xml 使用")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Test23_ApiStaticMyBatisExecutorTesting {

    // 定义操作类型，用于辅助方法分发
    private enum OperationType {
        SELECT_LIST,
        SELECT_ONE,
        INSERT,
        UPDATE,
        DELETE
    }

    /**
     * 核心测试辅助方法
     *
     * @param testName      测试用例的名称。
     * @param mapperFile    Mapper XML文件名。
     * @param sqlId         SQL语句的ID。
     * @param params        传入的参数。
     * @param operationType 要执行的操作类型 (SELECT_LIST, UPDATE, etc.)。
     */
    private void executeAndVerify(String testName, String mapperFile, String sqlId, Map<String, Object> params, OperationType operationType) {
        System.out.println("\n--- Running Integration Test: " + testName + " ---");
        System.out.println("Mapper: " + mapperFile + ", SQL ID: " + sqlId);
        System.out.println("Input Params: " + params);
        System.out.println("Operation: " + operationType);

        try {
            Object result;
            switch (operationType) {
                case SELECT_LIST:
                    result = DatabaseUtil32.selectListFromXmlByMp(mapperFile, sqlId, params);
                    break;
                case SELECT_ONE:
                    result = DatabaseUtil32.selectOneFromXmlByMp(mapperFile, sqlId, params);
                    break;
                case INSERT:
                    result = DatabaseUtil32.executeFromXmlByMp(mapperFile, sqlId, params);
                    break;
                case UPDATE:
                    result = DatabaseUtil32.executeFromXmlByMp(mapperFile, sqlId, params);
                    break;
                case DELETE:
                    result = DatabaseUtil32.executeFromXmlByMp(mapperFile, sqlId, params);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported operation type: " + operationType);
            }
            System.out.println("✅ " + testName + ": PASSED");
            System.out.println("   - Result: " + result);
        } catch (Exception e) {
            System.out.println("❌ " + testName + ": FAILED");
            System.out.println("   - Exception: " + e.getMessage());
            // e.printStackTrace(); // 可选：打印完整堆栈跟踪
        }
    }

    @BeforeAll
    void setup() {
        System.out.println("====================================================");
        System.out.println("  Starting Static MyBatis Executor Integration Tests");
        System.out.println("====================================================");
        // 可以在此处执行一些全局设置，例如创建测试表、插入初始数据等
    }

    @Test
    @Order(1)
    @DisplayName("Demo1Mapper.xml - 常用标签测试 - 测试 <if> 标签")
    void testSelectUsersByCondition() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "test");
        params.put("age", 30);
        executeAndVerify("testSelectUsersByCondition", "Demo1Mapper.xml", "selectUsersByCondition", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(2)
    @DisplayName("Demo1Mapper.xml - 常用标签测试 - 测试 <set> 标签")
    void testUpdateUserSelective() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1L);
        params.put("name", "new name updated");
        params.put("email", "new_updated@email.com");
        executeAndVerify("testUpdateUserSelective", "Demo1Mapper.xml", "updateUserSelective", params, OperationType.UPDATE);
    }

    @Test
    @Order(3)
    @DisplayName("Demo1Mapper.xml - 常用标签测试 - 测试 <foreach> 标签")
    void testSelectUsersByIds() {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList(1, 2, 3));
        executeAndVerify("testSelectUsersByIds", "Demo1Mapper.xml", "selectUsersByIds", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(4)
    @DisplayName("Demo1Mapper.xml - 常用标签测试 - 测试 <include> 标签")
    void testSelectUsersWithInclude() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "test");
        executeAndVerify("testSelectUsersWithInclude", "Demo1Mapper.xml", "selectUsersWithInclude", params, OperationType.SELECT_LIST);
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

        executeAndVerify("testSelectUsersAdvancedSearch", "Demo2Mapper.xml", "selectUsersAdvancedSearch", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(6)
    @DisplayName("Demo2Mapper.xml - 高级复杂场景测试 - 动态插入")
    void testInsertUserDynamic() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "dynamic_user_integration");
        params.put("email", "dynamic_integration@user.com");
        executeAndVerify("testInsertUserDynamic", "Demo2Mapper.xml", "insertUserDynamic", params, OperationType.INSERT);
    }

    @Test
    @Order(7)
    @DisplayName("Demo3Mapper.xml - <include> 高级用法测试 - 测试 <property> 子元素")
    void testFindWithProperties() {
        Map<String, Object> params = new HashMap<>();
        executeAndVerify("testFindWithProperties", "Demo3Mapper.xml", "findWithProperties", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(8)
    @DisplayName("Demo3Mapper.xml - <include> 高级用法测试 - 测试嵌套 <include>")
    void testFindWithNestedInclude() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        params.put("orderByColumn", "name");
        params.put("orderDirection", "DESC");
        executeAndVerify("testFindWithNestedInclude", "Demo3Mapper.xml", "findWithNestedInclude", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(9)
    @DisplayName("Demo4Mapper.xml - 边缘案例与特殊语法测试 - 测试XML实体字符")
    void testFindWithXmlEntitiesInTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("priority", 6);
        params.put("name", "special & co");
        executeAndVerify("testFindWithXmlEntitiesInTest", "Demo4Mapper.xml", "findWithXmlEntitiesInTest", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(10)
    @DisplayName("Demo4Mapper.xml - 边缘案例与特殊语法测试 - 简单布尔值判断")
    void testFindByUserMethod() {
        Map<String, Object> params = new HashMap<>();
        params.put("isAdmin", true);
        params.put("canViewSensitiveData", true);
        executeAndVerify("testFindByUserMethod", "Demo4Mapper.xml", "findByUserMethod", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(11)
    @DisplayName("Demo4Mapper.xml - 边缘案例与特殊语法测试 - 测试 _parameter 内置变量")
    void testFindBySimpleParameter() {
        String paramValue = "test_user";
        executeAndVerify("testFindBySimpleParameter", "Demo4Mapper.xml", "findBySimpleParameter", Collections.singletonMap("_parameter", paramValue), OperationType.SELECT_LIST);
    }
}
