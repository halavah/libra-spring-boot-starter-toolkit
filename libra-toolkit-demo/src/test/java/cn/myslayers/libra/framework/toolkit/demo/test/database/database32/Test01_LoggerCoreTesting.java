package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * 日志核心功能测试 (LoggerCore)
 *
 * <p>
 * 该测试类专注于验证 {@link DatabaseUtil32.LoggerCore} 的所有功能。
 * 它使用H2内存数据库来测试SQL日志的输出，确保测试环境的独立与稳定。
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("日志核心功能测试 (LoggerCore)")
public class Test01_LoggerCoreTesting {

    private static final String H2_DB_1 = "h2_log_test_db";

    /**
     * 在所有测试开始前执行，初始化H2内存数据库和测试表。
     */
    @BeforeAll
    void setupAll() {
        System.out.println("==================== 🎬 环境初始化开始 (LoggerCore) ====================");
        // 注册H2内存数据库
        System.out.println("  注册数据源: " + H2_DB_1);
        DatabaseUtil32.DataSourceCore.register(H2_DB_1, "jdbc:h2:mem:log_test;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "", "org.h2.Driver");
        DatabaseUtil32.DataSourceCore.setDefaultDataSource(H2_DB_1);
        System.out.println("  设置默认数据源为: " + H2_DB_1);

        // 创建测试表
        String createTableSql = "CREATE TABLE test_log (id INT PRIMARY KEY, message VARCHAR(255))";
        try {
            DatabaseUtil32.executeDDL(createTableSql);
            System.out.println("  ✓ 测试表 'test_log' 创建成功");
        } catch (Exception e) {
            System.err.println("  ❌ 创建测试表失败: " + e.getMessage());
            throw new RuntimeException("测试环境初始化失败", e);
        }
        System.out.println("==================== ✅ 环境初始化完成 (LoggerCore) ====================\n");
    }

    /**
     * 在所有测试结束后执行，清理数据库环境。
     */
    @AfterAll
    void tearDownAll() {
        System.out.println("\n==================== 🎬 环境清理开始 (LoggerCore) ====================");
        try {
            DatabaseUtil32.executeDDL("DROP TABLE test_log");
            System.out.println("  ✓ 测试表 'test_log' 已删除");
        } catch (Exception e) {
            System.err.println("  ❌ 删除测试表失败: " + e.getMessage());
        }
        DatabaseUtil32.DataSourceCore.closeAllDatabases();
        System.out.println("  ✓ 所有H2数据源已关闭");
        System.out.println("==================== ✅ 环境清理完成 (LoggerCore) ====================");
    }

    @Test
    @Order(1)
    @DisplayName("测试日志开关功能")
    void testToggleLogs() {
        System.out.println("\n【测试1】: 验证日志开关功能");
        System.out.println("  - 开启普通调试日志和SQL日志...");
        DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);
        DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);
        System.out.println("    当前DebugLog状态: " + DatabaseUtil32.LoggerCore.isDebugLogEnabled());
        System.out.println("    当前SqlDebugLog状态: " + DatabaseUtil32.LoggerCore.isSqlDebugLogEnabled());

        System.out.println("\n  - 执行一个简单查询以触发SQL日志:");
        DatabaseUtil32.selectOneByMp("SELECT * FROM test_log WHERE id = 1", new HashMap<>());

        System.out.println("\n  - 关闭所有日志...");
        DatabaseUtil32.LoggerCore.setDebugLogEnabled(false);
        DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(false);
        System.out.println("    当前DebugLog状态: " + DatabaseUtil32.LoggerCore.isDebugLogEnabled());
        System.out.println("    当前SqlDebugLog状态: " + DatabaseUtil32.LoggerCore.isSqlDebugLogEnabled());
        System.out.println("  - ✅ 日志开关功能验证完毕");
    }

    @Test
    @Order(2)
    @DisplayName("测试手动日志输出")
    void testManualLogging() {
        System.out.println("\n【测试2】: 验证手动日志输出API");
        DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);

        System.out.println("  - 调用 debugLog:");
        DatabaseUtil32.LoggerCore.debugLog("这是一条手动的普通调试信息。");

        System.out.println("  - 调用 debugErrorLog:");
        DatabaseUtil32.LoggerCore.debugErrorLog("这是一条手动的高亮错误信息。");

        System.out.println("  - 调用 logException:");
        try {
            int x = 1 / 0;
        } catch (Exception e) {
            DatabaseUtil32.LoggerCore.logException("计算过程中发生预期异常", e);
        }
        System.out.println("  - ✅ 手动日志输出验证完毕");
        DatabaseUtil32.LoggerCore.setDebugLogEnabled(false);
    }

    @Test
    @Order(3)
    @DisplayName("测试SQL执行日志")
    void testSqlExecutionLogging() {
        System.out.println("\n【测试3】: 验证数据库操作的SQL日志输出");
        System.out.println("  - 开启SQL日志以观察后续操作...");
        DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);

        // Insert
        System.out.println("\n  1. 测试INSERT日志:");
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("id", 100);
        insertParams.put("message", "Hello, SQL Log!");
        DatabaseUtil32.executeByMp("INSERT INTO test_log (id, message) VALUES (#{id}, #{message})", insertParams);

        // Select
        System.out.println("\n  2. 测试SELECT日志:");
        Map<String, Object> selectParams = new HashMap<>();
        selectParams.put("id", 100);
        DatabaseUtil32.selectOneByMp("SELECT * FROM test_log WHERE id = #{id}", selectParams);

        // Update
        System.out.println("\n  3. 测试UPDATE日志:");
        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("id", 100);
        updateParams.put("message", "Log Updated!");
        DatabaseUtil32.executeByMp("UPDATE test_log SET message = #{message} WHERE id = #{id}", updateParams);

        // Delete
        System.out.println("\n  4. 测试DELETE日志:");
        Map<String, Object> deleteParams = new HashMap<>();
        deleteParams.put("id", 100);
        DatabaseUtil32.executeByMp("DELETE FROM test_log WHERE id = #{id}", deleteParams);

        System.out.println("\n  - ✅ SQL执行日志验证完毕");
        DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(false);
    }
}
