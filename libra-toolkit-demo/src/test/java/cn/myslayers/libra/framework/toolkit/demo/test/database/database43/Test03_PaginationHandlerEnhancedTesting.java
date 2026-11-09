package cn.myslayers.libra.framework.toolkit.demo.test.database.database43;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43.PaginationHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by halavah on 2025-07-19 11:10
 */
// 使用Junit5进行测试，并按Order注解排序
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PaginationHandler 增强版分页功能测试")
public class Test03_PaginationHandlerEnhancedTesting {

    private static final String TEST_DB = "default_mysql1";

    // ==================== 全局设置 ====================

    @BeforeAll
    static void setUp() {
        System.out.println("=== PaginationHandler 增强版分页功能测试 ===");
        // 开启调试日志查看详细执行过程
        DatabaseUtil43.LoggerCore.setDebugLogEnabled(true);
        DatabaseUtil43.LoggerCore.setSqlDebugLogEnabled(true);
    }

    @AfterAll
    static void tearDown() {
        System.out.println("\n✅ 所有分页增强版测试完成！");
    }

    // ==================== 测试数据类 ====================

    public static class UserQuery {
        private int minAge;
        private String namePattern;

        public UserQuery() {}

        public UserQuery(int minAge) {
            this.minAge = minAge;
        }

        public UserQuery(int minAge, String namePattern) {
            this.minAge = minAge;
            this.namePattern = namePattern;
        }

        public int getMinAge() { return minAge; }
        public void setMinAge(int minAge) { this.minAge = minAge; }
        public String getNamePattern() { return namePattern; }
        public void setNamePattern(String namePattern) { this.namePattern = namePattern; }

        @Override
        public String toString() {
            return "UserQuery{minAge=" + minAge + ", namePattern='" + namePattern + "'}";
        }
    }

    public static class UserResult {
        private Integer id;
        private String name;
        private Integer age;
        private String email;

        public UserResult() {}

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public String toString() {
            return "UserResult{id=" + id + ", name='" + name + "', age=" + age + ", email='" + email + "'}";
        }
    }

    // ==================== API1：直接SQL分页测试 ====================

    @Test
    @Order(1)
    @DisplayName("测试直接SQL分页（强类型对象参数）- dbName版本")
    void testDirectSqlPaginationWithDbName() {
        System.out.println("\n--- 测试1：直接SQL分页（强类型对象参数）- dbName版本 ---");
        try {
            String sql = "SELECT 1 as id, 'Alice' as name, 28 as age, 'alice@test.com' as email " +
                        "UNION ALL SELECT 2, 'Bob', 32, 'bob@test.com' " +
                        "UNION ALL SELECT 3, 'Charlie', 25, 'charlie@test.com' " +
                        "UNION ALL SELECT 4, 'Diana', 30, 'diana@test.com' " +
                        "UNION ALL SELECT 5, 'Eve', 27, 'eve@test.com'";

            UserQuery query = new UserQuery(25, "test");

            // 测试dbName版本
            PaginationHandler.PageResult<UserResult> result =
                PaginationHandler.selectPageByMpEnhancedWithDbName(TEST_DB, sql, 1, 3, query, UserResult.class);

            System.out.println("分页结果：");
            System.out.println("  总记录数: " + result.getTotal());
            System.out.println("  当前页码: " + result.getPageNum());
            System.out.println("  每页大小: " + result.getPageSize());
            System.out.println("  总页数: " + result.getPages());
            System.out.println("  数据列表:");
            result.getList().forEach(user -> System.out.println("    " + user));

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试直接SQL分页（强类型对象参数）- 静态方法")
    void testDirectSqlPaginationStatic() {
        System.out.println("\n--- 测试2：直接SQL分页（强类型对象参数）- 静态方法 ---");
        try {
            String sql = "SELECT 1 as id, 'Test User 1' as name, 25 as age, 'user1@test.com' as email " +
                        "UNION ALL SELECT 2, 'Test User 2', 30, 'user2@test.com' " +
                        "UNION ALL SELECT 3, 'Test User 3', 35, 'user3@test.com'";

            UserQuery query = new UserQuery(20);

            // 测试静态方法
            PaginationHandler.PageResult<UserResult> result =
                DatabaseUtil43.selectPageByMpEnhanced(sql, 1, 2, query, UserResult.class);

            System.out.println("静态方法分页结果：");
            System.out.println("  总记录数: " + result.getTotal());
            System.out.println("  数据列表:");
            result.getList().forEach(user -> System.out.println("    " + user));

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @Order(3)
    @DisplayName("测试直接SQL分页（强类型对象参数）- 链式访问器")
    void testDirectSqlPaginationAccessor() {
        System.out.println("\n--- 测试3：直接SQL分页（强类型对象参数）- 链式访问器 ---");
        try {
            String sql = "SELECT 1 as id, 'Chain User 1' as name, 28 as age, 'chain1@test.com' as email " +
                        "UNION ALL SELECT 2, 'Chain User 2', 32, 'chain2@test.com'";

            UserQuery query = new UserQuery(25);

            // 测试链式访问器
            PaginationHandler.PageResult<UserResult> result =
                DatabaseUtil43.accessor().selectPageByMpEnhanced(sql, 1, 2, query, UserResult.class);

            System.out.println("链式访问器分页结果：");
            System.out.println("  总记录数: " + result.getTotal());
            System.out.println("  数据列表:");
            result.getList().forEach(user -> System.out.println("    " + user));

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== API2：XML文件分页测试 ====================

    @Test
    @Order(4)
    @DisplayName("测试XML文件分页（强类型对象参数）")
    void testXmlFilePagination() {
        System.out.println("\n--- 测试4：XML文件分页（强类型对象参数）---");
        try {
            UserQuery query = new UserQuery(25, "test");

            // 测试XML文件分页（会因为文件不存在而失败，但可以验证方法调用）
            try {
                PaginationHandler.PageResult<UserResult> result =
                    PaginationHandler.selectPageFromXmlByMpEnhancedWithDbName(
                        TEST_DB, "UserMapper.xml", "findUsers", 1, 3, query, UserResult.class);

                System.out.println("XML文件分页成功（如果XML文件存在）");
                System.out.println("  总记录数: " + result.getTotal());

            } catch (RuntimeException e) {
                System.out.println("预期错误（XML文件不存在）: " + e.getMessage());
            }

            // 测试静态方法版本
            try {
                PaginationHandler.PageResult<UserResult> result =
                    DatabaseUtil43.selectPageFromXmlByMpEnhanced("UserMapper.xml", "findUsers", 1, 3, query, UserResult.class);
                System.out.println("XML静态方法分页成功");
            } catch (RuntimeException e) {
                System.out.println("预期错误（XML文件不存在）: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== API3：自动搜索XML分页测试 ====================

    @Test
    @Order(5)
    @DisplayName("测试自动搜索XML分页（强类型对象参数）")
    void testXmlAutoPagination() {
        System.out.println("\n--- 测试5：自动搜索XML分页（强类型对象参数）---");
        try {
            UserQuery query = new UserQuery(30);

            // 测试自动搜索XML分页
            try {
                PaginationHandler.PageResult<UserResult> result =
                    PaginationHandler.selectPageFromXmlAutoByMpEnhancedWithDbName(
                        TEST_DB, "findUsersByAge", 1, 2, query, UserResult.class);

                System.out.println("自动搜索XML分页成功");
                System.out.println("  总记录数: " + result.getTotal());

            } catch (RuntimeException e) {
                System.out.println("预期错误（SQL ID不存在）: " + e.getMessage());
            }

            // 测试静态方法版本
            try {
                PaginationHandler.PageResult<UserResult> result =
                    DatabaseUtil43.selectPageFromXmlAutoByMpEnhanced("findUsersByAge", 1, 2, query, UserResult.class);
                System.out.println("自动搜索XML静态方法分页成功");
            } catch (RuntimeException e) {
                System.out.println("预期错误（SQL ID不存在）: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 事务测试 ====================

    @Test
    @Order(6)
    @DisplayName("测试事务模式下的分页（强类型对象参数）")
    void testTransactionPagination() {
        System.out.println("\n--- 测试6：事务模式下的分页（强类型对象参数）---");
        try {
            String sql = "SELECT 1 as id, 'TX User 1' as name, 25 as age, 'tx1@test.com' as email " +
                        "UNION ALL SELECT 2, 'TX User 2', 30, 'tx2@test.com'";

            UserQuery query = new UserQuery(20);

            // 测试事务模式
            DatabaseUtil43.accessor()
                .setTransaction()
                .executeByMpEnhanced("SELECT 1", query)  // 执行一个简单操作
                .selectPageByMpEnhanced(sql, 1, 2, query, UserResult.class);  // 在事务中分页

            System.out.println("事务模式分页测试完成（无具体验证，主要测试调用链）");

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 边界测试 ====================

    @Test
    @Order(7)
    @DisplayName("测试边界情况和错误处理")
    void testEdgeCasesAndErrorHandling() {
        System.out.println("\n--- 测试7：边界情况和错误处理 ---");

        // 测试空结果
        try {
            String emptyResultSql = "SELECT 1 as id, 'Test' as name, 25 as age, 'test@test.com' as email WHERE 1=0";
            UserQuery query = new UserQuery(25);

            PaginationHandler.PageResult<UserResult> result =
                PaginationHandler.selectPageByMpEnhancedWithDbName(TEST_DB, emptyResultSql, 1, 10, query, UserResult.class);

            System.out.println("空结果测试：");
            System.out.println("  总记录数: " + result.getTotal());
            System.out.println("  数据列表大小: " + result.getList().size());

        } catch (Exception e) {
            System.err.println("空结果测试失败: " + e.getMessage());
        }

        // 测试无效页码
        try {
            String sql = "SELECT 1 as id, 'Test' as name, 25 as age, 'test@test.com' as email";
            UserQuery query = new UserQuery(25);

            PaginationHandler.PageResult<UserResult> result =
                PaginationHandler.selectPageByMpEnhancedWithDbName(TEST_DB, sql, 0, -1, query, UserResult.class);

            System.out.println("无效页码测试（会被自动修正）：");
            System.out.println("  实际页码: " + result.getPageNum());
            System.out.println("  实际页大小: " + result.getPageSize());

        } catch (Exception e) {
            System.err.println("无效页码测试失败: " + e.getMessage());
        }

        // 测试null参数
        try {
            String sql = "SELECT 1 as id, 'Test' as name, 25 as age, 'test@test.com' as email";

            PaginationHandler.PageResult<UserResult> result =
                PaginationHandler.selectPageByMpEnhancedWithDbName(TEST_DB, sql, 1, 10, null, UserResult.class);

            System.out.println("null参数测试：");
            System.out.println("  总记录数: " + result.getTotal());

        } catch (Exception e) {
            System.err.println("null参数测试失败: " + e.getMessage());
        }
    }

    // ==================== 性能对比测试 ====================

    @Test
    @Order(8)
    @DisplayName("性能对比测试：Map参数 vs 强类型对象参数")
    void testPerformanceComparison() {
        System.out.println("\n--- 测试8：性能对比测试 ---");
        try {
            String sql = "SELECT 1 as id, 'Perf User' as name, 28 as age, 'perf@test.com' as email";

            // Map参数版本
            long startTime1 = System.currentTimeMillis();
            Map<String, Object> mapParams = new HashMap<>();
            mapParams.put("minAge", 25);
            mapParams.put("namePattern", "test");

            PaginationHandler.PageResult<Map<String, Object>> mapResult =
                PaginationHandler.selectPageByMpWithDbName(TEST_DB, sql, 1, 10, mapParams);
            long endTime1 = System.currentTimeMillis();

            // 强类型对象参数版本
            long startTime2 = System.currentTimeMillis();
            UserQuery objectParams = new UserQuery(25, "test");

            PaginationHandler.PageResult<UserResult> objectResult =
                PaginationHandler.selectPageByMpEnhancedWithDbName(TEST_DB, sql, 1, 10, objectParams, UserResult.class);
            long endTime2 = System.currentTimeMillis();

            System.out.println("性能对比结果：");
            System.out.println("  Map参数版本耗时: " + (endTime1 - startTime1) + "ms");
            System.out.println("  强类型对象版本耗时: " + (endTime2 - startTime2) + "ms");
            System.out.println("  Map版本结果: " + mapResult.getTotal() + " 条记录");
            System.out.println("  对象版本结果: " + objectResult.getTotal() + " 条记录");

        } catch (Exception e) {
            System.err.println("性能对比测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}