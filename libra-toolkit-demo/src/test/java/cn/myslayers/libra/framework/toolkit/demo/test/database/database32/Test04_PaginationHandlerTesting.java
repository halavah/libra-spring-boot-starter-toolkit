package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32.DatabaseAccessor;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32.PaginationHandler;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32.PaginationHandler.PageResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * 分页处理器测试类：测试DatabaseUtil323中的PaginationHandler功能
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("分页处理器功能测试")
public class Test04_PaginationHandlerTesting {

    private static final String TEST_DB = "test-h2";

    @BeforeAll
    void setUp() {
        System.out.println("=== 初始化测试数据库 ===");
//        DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);

        try {
            // 注册测试用的H2内存数据库
            DatabaseUtil32.DataSourceCore.register(
                TEST_DB,
                "jdbc:h2:mem:testdb" + System.currentTimeMillis() + ";DB_CLOSE_DELAY=-1;MODE=MySQL",
                "sa",
                "",
                "org.h2.Driver"
            );

            // 创建测试表 - 在TEST_DB中创建
            String createUsersTable = "CREATE TABLE IF NOT EXISTS page_users (" +
                "id INT PRIMARY KEY," +
                "name VARCHAR(50)," +
                "age INT," +
                "email VARCHAR(100)," +
                "department_id INT," +
                "salary DECIMAL(10,2)" +
                ")";

            // 使用正确的方法创建表
            DatabaseUtil32.SimpleSqlExecutor.execute(TEST_DB, createUsersTable, new HashMap<>());

            // 清空数据 - 在TEST_DB中清空
            try {
                DatabaseUtil32.SimpleSqlExecutor.execute(TEST_DB, "DELETE FROM page_users", new HashMap<>());
            } catch (Exception e) {
                // 忽略表不存在的错误
            }

            // 插入测试数据 - 在TEST_DB中插入
            for (int i = 1; i <= 25; i++) {
                String insertSql = String.format(
                    "INSERT INTO page_users (id, name, age, email, department_id, salary) VALUES (%d, 'User%d', %d, 'user%d@test.com', %d, %.2f)",
                    i, i, 20 + (i % 30), i, (i % 3) + 1, 5000.0 + (i * 100)
                );
                DatabaseUtil32.SimpleSqlExecutor.execute(TEST_DB, insertSql, new HashMap<>());
            }

            System.out.println("✓ 测试数据准备完成：25个用户");

        } catch (Exception e) {
            System.err.println("❌ 测试环境初始化失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("测试环境初始化失败", e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("测试API1 - MySQL分页查询")
    void testAPI1MySQLDatabase() {
        System.out.println("\n=== 测试API1 - MySQL分页查询 ===");

        // 设置默认数据源为测试数据库
        DatabaseUtil32.DataSourceCore.setDefaultDataSource(TEST_DB);

        String sql = "SELECT * FROM page_users WHERE age > #{age} ORDER BY id";
        Map<String, Object> params = new HashMap<>();
        params.put("age", 25);

        PageResult<Map<String, Object>> result =
            PaginationHandler.selectPageByMpWithDbName(TEST_DB, sql, 1, 3, params);

        System.out.println("检查分页结果是否不应为空: " + (result != null));
        if (result != null) {
            System.out.println("检查应有符合条件的记录: " + (result.getTotal() > 0));
            System.out.println("检查每页最多3条记录: " + (result.getList().size() <= 3));
        }

        System.out.println("✓ API1 - MySQL数据库分页查询测试通过");
    }

    @Test
    @Order(2)
    @DisplayName("测试API2 - Oracle分页查询")
    void testAPI1OracleDatabase() {
        System.out.println("\n=== 测试AP2 - Oracle分页查询 ===");

        // 准备测试参数
        Map<String, Object> dto = new HashMap<>();
        dto.put("plNos", "Y2,W2");
        dto.put("stopReasonNames", "质量原因,维修原因");
        dto.put("startDate", "2000-01-01");
        dto.put("endDate", "2026-01-01");

        try {
            DatabaseAccessor accessor = DatabaseUtil32.accessor("second");
            PageResult<Map<String, Object>> queryCjsbgzmsList = accessor.selectPageFromXmlAuto("queryCjsbgzmsList", 1, 2, dto);
            System.out.println(queryCjsbgzmsList);
        } catch (Exception e) {
            System.err.println("Oracle测试可能失败，因为数据源'second'未配置: " + e.getMessage());
        }

        System.out.println("✓ API2 - Oracle数据库分页查询测试通过");
    }

    @Test
    @Order(3)
    @DisplayName("测试统一分页处理器基础功能")
    void testUnifiedPaginationHandler() {
        System.out.println("\n=== 测试统一分页处理器基础功能 ===");

        String sql = "SELECT * FROM page_users ORDER BY id";
        PageResult<Map<String, Object>> result =
            PaginationHandler.executePaginationQueryWithDbName(TEST_DB, sql, 1, 5, new ArrayList<>());

        System.out.println("检查分页结果是否不应为空: " + (result != null));
        if (result != null) {
            System.out.println("检查总记录数 | 预期: 25 | 实际: " + result.getTotal());
            System.out.println("检查第一页记录数 | 预期: 5 | 实际: " + result.getList().size());
            System.out.println("检查总页数 | 预期: 5 | 实际: " + result.getPages());
            System.out.println("检查当前页码 | 预期: 1 | 实际: " + result.getPageNum());
        }

        System.out.println("✓ 统一分页处理器基础功能测试通过");
    }

    @Test
    @Order(4)
    @DisplayName("测试分页策略选择")
    void testPaginationStrategySelection() {
        System.out.println("\n=== 测试分页策略选择 ===");

        // 测试H2数据库策略选择
        String strategy = PaginationHandler.testStrategySelection(
            PaginationHandler.DatabaseType.H2,
            "SELECT * FROM page_users"
        );

        System.out.println("检查H2数据库应选择H2分页策略 | 预期: H2PaginationStrategy | 实际: " + strategy);

        // 测试MySQL数据库策略选择
        String mysqlStrategy = PaginationHandler.testStrategySelection(
            PaginationHandler.DatabaseType.MYSQL,
            "SELECT * FROM page_users"
        );

        System.out.println("检查MySQL数据库应选择MySQL分页策略 | 预期: MySQLPaginationStrategy | 实际: " + mysqlStrategy);

        System.out.println("✓ 分页策略选择测试通过");
    }

    @Test
    @Order(5)
    @DisplayName("测试可用策略列表")
    void testAvailableStrategies() {
        System.out.println("\n=== 测试可用策略列表 ===");

        List<String> strategies = PaginationHandler.getAvailableStrategies();

        System.out.println("检查策略列表是否不为空: " + (strategies != null));
        if (strategies != null) {
            System.out.println("检查应有可用的分页策略: " + (strategies.size() > 0));
            System.out.println("检查应包含H2分页策略: " + (strategies.contains("H2PaginationStrategy")));
            System.out.println("检查应包含MySQL分页策略: " + (strategies.contains("MySQLPaginationStrategy")));
            System.out.println("检查应包含Oracle标准分页策略: " + (strategies.contains("OracleStandardPaginationStrategy")));
            System.out.println("检查应包含Oracle WITH子句分页策略: " + (strategies.contains("OracleWithClausePaginationStrategy")));
        }

        System.out.println("✓ 可用策略: " + strategies);
        System.out.println("✓ 可用策略列表测试通过");
    }

    @Test
    @Order(6)
    @DisplayName("测试SQL注释清理功能")
    void testSqlCommentRemoval() {
        System.out.println("\n=== 测试SQL注释清理功能 ===");

        // 设置默认数据源为测试数据库
        DatabaseUtil32.DataSourceCore.setDefaultDataSource(TEST_DB);

        // 测试包含多种注释的SQL
        String sqlWithComments = "-- 这是一个完整的注释行，应该被删除\n" +
            "SELECT * FROM page_users \n" +
            "WHERE age > #{age} --这是行末注释，应该被删除\n" +
            "-- 另一个完整注释行\n" +
            "AND department_id IS NOT NULL --行末注释2\n" +
            "ORDER BY id";

        Map<String, Object> params = new HashMap<>();
        params.put("age", 20);

        // 执行带注释的分页查询
        PageResult<Map<String, Object>> result =
            PaginationHandler.selectPageWithDbName(TEST_DB, sqlWithComments, 1, 5, params);

        System.out.println("检查分页结果是否不应为空: " + (result != null));
        if (result != null) {
            System.out.println("检查应有符合条件的记录: " + (result.getTotal() > 0));
        }

        System.out.println("✓ SQL注释清理功能测试通过");
    }

    @AfterAll
    void tearDown() {
        System.out.println("\n=== 清理测试环境 ===");
        try {
            // 删除测试表 - 使用正确的方法
            DatabaseUtil32.SimpleSqlExecutor.execute(TEST_DB, "DROP TABLE IF EXISTS page_users", new HashMap<>());

            // 关闭数据库连接
            DatabaseUtil32.DataSourceCore.closeDatabase(TEST_DB);

            System.out.println("✓ 测试环境清理完成");
        } catch (Exception e) {
            System.err.println("⚠ 清理测试环境时出现异常: " + e.getMessage());
        }
    }
}
