package cn.myslayers.libra.framework.toolkit.demo.test.database.database22;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil22;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Connection;
import java.sql.SQLException;

// 测试 Druid 连接的获取与有效性
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore (Druid) - 连接处理测试")
class Test02_DruidConnectionHandlingTesting {

    private static final String DB_NAME = "druid_conn_test";

    // 在所有测试开始前，进行一次性设置
    @BeforeAll
    static void setUp() {
        DatabaseUtil22.LoggerCore.setDebugLogEnabled(true);
        System.out.println("\n==================== 测试套件开始：连接处理 ====================");
        // 清理并注册一个用于测试的Druid数据源
        DatabaseUtil22.DataSourceCore.closeAllDatabases();
        DatabaseUtil22.DataSourceCore.register(
            DB_NAME,
            "jdbc:h2:mem:druid_conn;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
    }

    // 在所有测试结束后，进行清理
    @AfterAll
    static void tearDown() {
        DatabaseUtil22.DataSourceCore.closeAllDatabases();
        System.out.println("==================== 测试套件结束：连接处理 ====================\n");
    }

    // 测试从Druid连接池成功获取一个连接
    @Test
    @Order(1)
    @DisplayName("1. 测试成功获取Druid连接")
    void testGetConnectionSuccessfully() {
        System.out.println("--- 测试 1：成功获取Druid连接 ---");
        Connection conn = null;
        try {
            conn = DatabaseUtil22.DataSourceCore.getConnection(DB_NAME);
            System.out.println("获取到的连接对象: " + (conn != null ? conn.getClass().getName() : "null"));

            if (conn != null && !conn.isClosed()) {
                System.out.println("成功：从Druid连接池获取到有效的数据库连接。");
            } else {
                System.out.println("失败：获取到的连接为null或已关闭。");
            }
        } catch (SQLException e) {
            System.out.println("异常：获取连接时发生SQL错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close(); // 将连接归还给池
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 验证获取的连接是Druid代理的连接
    @Test
    @Order(2)
    @DisplayName("2. 验证连接为Druid代理类型")
    void testConnectionIsDruidProxy() {
        System.out.println("--- 测试 2：验证连接为Druid代理类型 ---");
        try (Connection conn = DatabaseUtil22.DataSourceCore.getConnection(DB_NAME)) {
            String connectionClassName = conn.getClass().getName();
            System.out.println("连接实例的类名是: " + connectionClassName);

            if (connectionClassName.toLowerCase().contains("druid")) {
                System.out.println("成功：获取的连接是Druid代理连接，符合预期。");
            } else {
                System.out.println("失败：获取的连接不是Druid代理连接。");
            }
        } catch (Exception e) {
            System.out.println("异常：验证连接类型时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试从未注册的数据源获取连接
    @Test
    @Order(3)
    @DisplayName("3. 测试从不存在的数据源获取连接")
    void testGetConnectionFromNonExistentDataSource() {
        System.out.println("--- 测试 3：从不存在的数据源获取连接 ---");
        String nonExistentDbName = "non_existent_db";
        try {
            DatabaseUtil22.DataSourceCore.getConnection(nonExistentDbName);
            System.out.println("失败：从一个不存在的数据源获取连接时，没有按预期抛出异常。");
        } catch (Exception e) {
            System.out.println("捕获到异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            if (e instanceof RuntimeException && e.getMessage().contains("数据库未注册")) {
                System.out.println("成功：当数据源不存在时，系统按预期抛出了运行时异常。");
            } else {
                System.out.println("失败：抛出的异常类型或消息不符合预期。");
                e.printStackTrace();
            }
        }
    }
}
