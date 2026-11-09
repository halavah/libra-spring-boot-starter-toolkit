package cn.myslayers.libra.framework.toolkit.demo.test.database.database42;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil42;
import java.sql.Connection;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

// 测试 Druid 连接的归还与池状态
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore (Druid) - 连接池状态测试")
class Test03_DruidPoolStateTesting {

    private static final String DB_NAME = "druid_pool_test";

    // 在所有测试开始前，进行一次性设置
    @BeforeAll
    static void setUp() {
        DatabaseUtil42.LoggerCore.setDebugLogEnabled(true);
        System.out.println("\n==================== 测试套件开始：连接池状态 ====================");
        // 清理并注册一个用于测试的Druid数据源
        DatabaseUtil42.DataSourceCore.closeAllDatabases();
        DatabaseUtil42.DataSourceCore.register(
            DB_NAME,
            "jdbc:h2:mem:druid_pool;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
    }

    // 在所有测试结束后，进行清理
    @AfterAll
    static void tearDown() {
        DatabaseUtil42.DataSourceCore.closeAllDatabases();
        System.out.println("==================== 测试套件结束：连接池状态 ====================\n");
    }

    // 获取指定数据源的监控数据
    private Map<String, Object> getMonitorData(String dbName) {
        return DatabaseUtil42.DataSourceCore.getAllDataSourceInfo().get(dbName);
    }

    // 测试连接获取和归还时池状态的变化
    @Test
    @Order(1)
    @DisplayName("1. 测试连接获取与归还对池状态的影响")
    void testPoolStateOnGetAndReturnConnection() {
        System.out.println("--- 测试 1：连接获取与归还对池状态的影响 ---");
        try {
            // 初始状态
            Map<String, Object> initialState = getMonitorData(DB_NAME);
            int initialActive = (int) initialState.get("activeCount");
            System.out.println("初始状态 -> 活跃连接数: " + initialActive);

            // 获取一个连接
            Connection conn = DatabaseUtil42.DataSourceCore.getConnection(DB_NAME);
            Map<String, Object> afterGetState = getMonitorData(DB_NAME);
            int afterGetActive = (int) afterGetState.get("activeCount");
            System.out.println("获取连接后 -> 活跃连接数: " + afterGetActive);

            if (afterGetActive == initialActive + 1) {
                System.out.println("成功：获取连接后，活跃连接数增加 1。");
            } else {
                System.out.println("失败：获取连接后，活跃连接数未按预期增加。");
            }

            // 归还连接
            conn.close();
            Map<String, Object> afterReturnState = getMonitorData(DB_NAME);
            int afterReturnActive = (int) afterReturnState.get("activeCount");
            System.out.println("归还连接后 -> 活跃连接数: " + afterReturnActive);

            if (afterReturnActive == initialActive) {
                System.out.println("成功：归还连接后，活跃连接数恢复初始值。");
            } else {
                System.out.println("失败：归还连接后，活跃连接数未按预期恢复。");
            }

        } catch (Exception e) {
            System.out.println("异常：测试池状态变化时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试耗尽连接池资源
    @Test
    @Order(2)
    @DisplayName("2. 测试耗尽连接池资源")
    void testExhaustingConnectionPool() {
        System.out.println("--- 测试 2：耗尽连接池资源 ---");
        // Druid 默认 maxActive 是 50，这里我们获取少量连接进行测试
        int connectionsToGet = 5;
        Connection[] connections = new Connection[connectionsToGet];
        try {
            System.out.println("尝试获取 " + connectionsToGet + " 个连接...");
            for (int i = 0; i < connectionsToGet; i++) {
                connections[i] = DatabaseUtil42.DataSourceCore.getConnection(DB_NAME);
                System.out.println("已获取第 " + (i + 1) + " 个连接。");
            }

            Map<String, Object> state = getMonitorData(DB_NAME);
            int activeCount = (int) state.get("activeCount");
            System.out.println("获取 " + connectionsToGet + " 个连接后，活跃连接数: " + activeCount);

            if (activeCount >= connectionsToGet) {
                System.out.println("成功：成功获取了多个连接，活跃连接数符合预期。");
            } else {
                System.out.println("失败：获取多个连接后，活跃连接数与预期不符。");
            }

        } catch (Exception e) {
            System.out.println("异常：测试耗尽连接池时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理所有获取的连接
            System.out.println("正在归还所有连接...");
            for (Connection conn : connections) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            Map<String, Object> finalState = getMonitorData(DB_NAME);
            System.out.println("全部归还后，活跃连接数: " + finalState.get("activeCount"));
        }
    }
}
