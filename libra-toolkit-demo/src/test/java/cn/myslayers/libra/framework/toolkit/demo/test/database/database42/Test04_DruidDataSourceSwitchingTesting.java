package cn.myslayers.libra.framework.toolkit.demo.test.database.database42;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil42;
import java.sql.Connection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

// 测试数据源的动态切换与隔离性
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore (Druid) - 数据源切换测试")
class Test04_DruidDataSourceSwitchingTesting {

    private static final String DB_H2_FIRST = "h2_db_1";
    private static final String DB_H2_SECOND = "h2_db_2";

    // 在所有测试开始前，进行一次性设置
    @BeforeAll
    static void setUp() {
        DatabaseUtil42.LoggerCore.setDebugLogEnabled(true);
        System.out.println("\n==================== 测试套件开始：数据源切换 ====================");
        DatabaseUtil42.DataSourceCore.closeAllDatabases();
        // 注册两个不同的H2 Druid数据源
        DatabaseUtil42.DataSourceCore.register(
            DB_H2_FIRST,
            "jdbc:h2:mem:h2_switch_1;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
        DatabaseUtil42.DataSourceCore.register(
            DB_H2_SECOND,
            "jdbc:h2:mem:h2_switch_2;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
    }

    // 在所有测试结束后，进行清理
    @AfterAll
    static void tearDown() {
        DatabaseUtil42.DataSourceCore.closeAllDatabases();
        System.out.println("==================== 测试套件结束：数据源切换 ====================\n");
    }

    // 测试全局默认数据源的切换
    @Test
    @Order(1)
    @DisplayName("1. 测试全局默认数据源切换")
    void testGlobalDataSourceSwitching() {
        System.out.println("--- 测试 1：全局默认数据源切换 ---");
        try {
            // 初始默认应为第一个注册的
            String initialDefault = DatabaseUtil42.DataSourceCore.getDefaultDatabaseName();
            System.out.println("初始默认数据源: " + initialDefault);

            // 切换默认数据源到第二个H2数据源
            System.out.println("切换默认数据源到: " + DB_H2_SECOND);
            DatabaseUtil42.DataSourceCore.setDefaultDataSource(DB_H2_SECOND);

            String newDefault = DatabaseUtil42.DataSourceCore.getDefaultDatabaseName();
            System.out.println("切换后默认数据源: " + newDefault);

            if (DB_H2_SECOND.equals(newDefault)) {
                System.out.println("成功：全局默认数据源已成功切换。");
            } else {
                System.out.println("失败：全局默认数据源未能成功切换。");
            }

            // 验证从默认源获取的连接是否正确
            try (Connection conn = DatabaseUtil42.DataSourceCore.getConnection()) {
                String connectionUrl = conn.getMetaData().getURL();
                System.out.println("从默认源获取的连接URL: " + connectionUrl);
                if (connectionUrl.contains("h2_switch_2")) {
                    System.out.println("成功：从切换后的默认源获取到了正确的连接。");
                } else {
                    System.out.println("失败：从切换后的默认源获取的连接不正确。");
                }
            }

        } catch (Exception e) {
            System.out.println("异常：测试全局切换时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 恢复默认设置
            DatabaseUtil42.DataSourceCore.setDefaultDataSource(DB_H2_FIRST);
        }
    }

    // 测试线程级数据源上下文切换
    @Test
    @Order(2)
    @DisplayName("2. 测试线程级数据源上下文")
    void testThreadLocalDataSourceSwitching() throws InterruptedException {
        System.out.println("--- 测试 2：线程级数据源上下文 ---");

        // 主线程的默认数据源
        String mainThreadDefault = DatabaseUtil42.DataSourceCore.getCurrentDataSourceName();
        System.out.println("主线程的默认数据源是: " + mainThreadDefault);

        Thread thread = new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + " -> 开始执行...");
                // 在新线程中设置上下文为第二个H2数据源
                System.out.println(Thread.currentThread().getName() + " -> 设置线程级数据源为: " + DB_H2_SECOND);
                DatabaseUtil42.DataSourceCore.setThreadDataSourceContext(DB_H2_SECOND);

                String threadCurrentDb = DatabaseUtil42.DataSourceCore.getCurrentDataSourceName();
                System.out.println(Thread.currentThread().getName() + " -> 当前数据源: " + threadCurrentDb);

                if (DB_H2_SECOND.equals(threadCurrentDb)) {
                    System.out.println(Thread.currentThread().getName() + " -> 成功：线程级数据源设置成功。");
                } else {
                    System.out.println(Thread.currentThread().getName() + " -> 失败：线程级数据源设置失败。");
                }

                // 验证连接
                try (Connection conn = DatabaseUtil42.DataSourceCore.getConnection()) {
                    String connectionUrl = conn.getMetaData().getURL();
                    System.out.println(Thread.currentThread().getName() + " -> 获取到的连接URL: " + connectionUrl);
                    if (connectionUrl.contains("h2_switch_2")) {
                        System.out.println(Thread.currentThread().getName() + " -> 成功：获取到了正确的线程级连接。");
                    } else {
                        System.out.println(Thread.currentThread().getName() + " -> 失败：获取的连接不属于线程级数据源。");
                    }
                }

            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + " -> 异常: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // 清理线程上下文
                DatabaseUtil42.DataSourceCore.clearThreadDataSourceContext();
                System.out.println(Thread.currentThread().getName() + " -> 清理线程上下文。");
            }
        });

        thread.start();
        thread.join(); // 等待子线程执行完毕

        // 验证主线程的默认数据源未受影响
        String mainThreadDefaultAfter = DatabaseUtil42.DataSourceCore.getCurrentDataSourceName();
        System.out.println("子线程执行后，主线程的默认数据源是: " + mainThreadDefaultAfter);
        if (mainThreadDefault.equals(mainThreadDefaultAfter)) {
            System.out.println("成功：主线程的默认数据源未受子线程影响，隔离性良好。");
        } else {
            System.out.println("失败：主线程的默认数据源被子线程影响。");
        }
    }
}
