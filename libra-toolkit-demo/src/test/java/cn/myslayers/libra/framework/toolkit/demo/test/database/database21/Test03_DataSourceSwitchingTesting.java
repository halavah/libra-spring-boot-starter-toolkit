package cn.myslayers.libra.framework.toolkit.demo.test.database.database21;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil21;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

// 测试数据源的动态切换功能
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore - 动态切换测试")
class Test03_DataSourceSwitchingTesting {

    private static final String DB_ONE = "h2_db_one";
    private static final String DB_TWO = "h2_db_two";

    // 在所有测试开始前，进行一次性设置
    @BeforeAll
    static void setUp() {
        DatabaseUtil21.LoggerCore.setDebugLogEnabled(true);
        System.out.println("==================== 测试开始：动态切换 ====================");
        // 清理并注册测试所需的数据源
        DatabaseUtil21.DataSourceCore.closeAllDatabases();
        // 手动注册两个H2数据库用于测试切换
        DatabaseUtil21.DataSourceCore.register(DB_ONE, "jdbc:h2:mem:db_one;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
        DatabaseUtil21.DataSourceCore.register(DB_TWO, "jdbc:h2:mem:db_two;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
    }

    // 在所有测试结束后，进行清理
    @AfterAll
    static void tearDown() {
        DatabaseUtil21.DataSourceCore.closeAllDatabases();
        System.out.println("==================== 测试结束：动态切换 ====================");
    }

    // 测试获取和设置全局默认数据源
    @Test
    @Order(1)
    @DisplayName("1. 测试设置和获取全局默认数据源")
    void testGetAndSetDefaultDataSource() {
        System.out.println("\n--- 测试 1：设置和获取全局默认数据源 ---");
        // 初始默认应为第一个注册的 DB_ONE
        String initialDefault = DatabaseUtil21.DataSourceCore.getDefaultDatabaseName();
        System.out.println("初始默认数据源: " + initialDefault);
        if (!DB_ONE.equals(initialDefault)) {
            System.out.println("失败：初始默认数据源不是 '" + DB_ONE + "'。");
            return;
        }

        // 切换默认数据源到 DB_TWO
        System.out.println("切换默认数据源到 '" + DB_TWO + "'...");
        boolean setResult = DatabaseUtil21.DataSourceCore.setDefaultDataSource(DB_TWO);
        System.out.println("设置结果: " + setResult);

        String newDefault = DatabaseUtil21.DataSourceCore.getDefaultDatabaseName();
        System.out.println("新的默认数据源: " + newDefault);

        if (setResult && DB_TWO.equals(newDefault)) {
            System.out.println("成功：默认数据源已成功切换到 '" + DB_TWO + "'。");
        } else {
            System.out.println("失败：未能成功切换默认数据源。");
        }

        // 恢复默认数据源为 DB_ONE 以免影响其他测试
        DatabaseUtil21.DataSourceCore.setDefaultDataSource(DB_ONE);
    }

    // 测试线程级数据源的设置和优先级
    @Test
    @Order(2)
    @DisplayName("2. 测试线程级数据源的设置和优先级")
    void testThreadLocalDataSource() {
        System.out.println("\n--- 测试 2：线程级数据源 ---");
        // 确保当前默认是 DB_ONE
        DatabaseUtil21.DataSourceCore.setDefaultDataSource(DB_ONE);
        System.out.println("当前全局默认数据源: " + DatabaseUtil21.DataSourceCore.getDefaultDatabaseName());

        try {
            // 在当前线程设置数据源为 DB_TWO
            System.out.println("为当前线程设置数据源为 '" + DB_TWO + "'...");
            DatabaseUtil21.DataSourceCore.setThreadDataSourceContext(DB_TWO);

            // 验证当前生效的数据源是否为 DB_TWO
            String currentDb = DatabaseUtil21.DataSourceCore.getCurrentDataSourceName();
            System.out.println("设置后，当前线程生效的数据源: " + currentDb);

            if (DB_TWO.equals(currentDb)) {
                System.out.println("成功：线程级数据源设置成功，并具有更高优先级。");
            } else {
                System.out.println("失败：线程级数据源未能正确生效。");
            }
        } finally {
            // 必须在 finally 块中清理线程上下文
            System.out.println("清理线程上下文...");
            DatabaseUtil21.DataSourceCore.clearThreadDataSourceContext();
        }
    }

    // 测试清理线程级数据源后的回退
    @Test
    @Order(3)
    @DisplayName("3. 测试清理线程级数据源后的回退")
    void testClearThreadLocalDataSource() {
        System.out.println("\n--- 测试 3：清理线程级数据源 ---");
        DatabaseUtil21.DataSourceCore.setDefaultDataSource(DB_ONE);

        try {
            DatabaseUtil21.DataSourceCore.setThreadDataSourceContext(DB_TWO);
            String beforeClear = DatabaseUtil21.DataSourceCore.getCurrentDataSourceName();
            System.out.println("清理前，当前线程生效的数据源: " + beforeClear);

            // 清理
            DatabaseUtil21.DataSourceCore.clearThreadDataSourceContext();
            String afterClear = DatabaseUtil21.DataSourceCore.getCurrentDataSourceName();
            System.out.println("清理后，当前线程生效的数据源: " + afterClear);

            if (DB_ONE.equals(afterClear)) {
                System.out.println("成功：清理线程上下文后，生效数据源已正确回退到全局默认值。");
            } else {
                System.out.println("失败：清理线程上下文后，未能回退到全局默认数据源。");
            }
        } catch (Exception e) {
            // 确保即使发生异常也清理
            DatabaseUtil21.DataSourceCore.clearThreadDataSourceContext();
            System.out.println("异常：" + e.getMessage());
        }
    }

    // 测试设置一个不存在的数据源为线程上下文
    @Test
    @Order(4)
    @DisplayName("4. 测试为线程设置不存在的数据源")
    void testSetNonExistentThreadDataSource() {
        System.out.println("\n--- 测试 4：为线程设置不存在的数据源 ---");
        String nonExistentDb = "non_existent_db";
        try {
            DatabaseUtil21.DataSourceCore.setThreadDataSourceContext(nonExistentDb);
            System.out.println("失败：为线程设置不存在的数据源时，没有抛出预期的异常。");
        } catch (Exception e) {
            System.out.println("成功：捕获到预期异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            if (e instanceof IllegalArgumentException && e.getMessage().contains("数据源不存在")) {
                System.out.println("验证成功：异常类型和消息内容符合预期。");
            } else {
                System.out.println("验证失败：异常类型或消息内容不符合预期。");
                e.printStackTrace();
            }
        }
    }
}
