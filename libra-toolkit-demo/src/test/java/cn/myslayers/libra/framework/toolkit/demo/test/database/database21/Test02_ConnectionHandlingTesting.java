package cn.myslayers.libra.framework.toolkit.demo.test.database.database21;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil21;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Connection;
import java.sql.SQLException;

// 测试数据源的连接处理功能
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore - 连接处理测试")
class Test02_ConnectionHandlingTesting {

    private static final String H2_DB_NAME = "h2_conn_test";

    // 在所有测试开始前，进行一次性设置
    @BeforeAll
    static void setUp() {
        DatabaseUtil21.LoggerCore.setDebugLogEnabled(true);
        System.out.println("==================== 测试开始：连接处理 ====================");
        // 清理并注册测试所需的数据源
        DatabaseUtil21.DataSourceCore.closeAllDatabases();
        // 手动注册一个数据源，它将成为默认数据源
        DatabaseUtil21.DataSourceCore.register(
            "default_h2_db",
            "jdbc:h2:mem:default_conn;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
        DatabaseUtil21.DataSourceCore.register(
            H2_DB_NAME,
            "jdbc:h2:mem:conn_test;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
    }

    // 在所有测试结束后，进行清理
    @AfterAll
    static void tearDown() {
        DatabaseUtil21.DataSourceCore.closeAllDatabases();
        System.out.println("==================== 测试结束：连接处理 ====================");
    }

    // 测试从默认数据源获取连接
    @Test
    @Order(1)
    @DisplayName("1. 测试从默认数据源获取连接")
    void testGetConnectionFromDefault() {
        System.out.println("\n--- 测试 1：从默认数据源获取连接 ---");
        try {
            String defaultDbName = DatabaseUtil21.DataSourceCore.getDefaultDatabaseName();
            System.out.println("当前默认数据源是: " + defaultDbName);

            try (Connection conn = DatabaseUtil21.DataSourceCore.getConnection()) {
                boolean isValid = conn != null && !conn.isClosed();
                System.out.println("从默认数据源 '" + defaultDbName + "' 获取的连接是否有效: " + isValid);
                if (isValid) {
                    System.out.println("成功：已从默认数据源获取有效连接。");
                } else {
                    System.out.println("失败：未能从默认数据源获取有效连接。");
                }
            }
        } catch (Exception e) {
            System.out.println("异常：从默认数据源获取连接时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试从指定的数据源（H2）获取连接
    @Test
    @Order(2)
    @DisplayName("2. 测试从指定H2数据源获取连接")
    void testGetConnectionFromSpecified() {
        System.out.println("\n--- 测试 2：从指定数据源 ('" + H2_DB_NAME + "') 获取连接 ---");
        try (Connection conn = DatabaseUtil21.DataSourceCore.getConnection(H2_DB_NAME)) {
            boolean isValid = conn != null && !conn.isClosed();
            System.out.println("从 '" + H2_DB_NAME + "' 获取的连接是否有效: " + isValid);
            if (isValid) {
                System.out.println("成功：已从指定H2数据源获取有效连接。");
            } else {
                System.out.println("失败：未能从指定H2数据源获取有效连接。");
            }
        } catch (SQLException e) {
            System.out.println("异常：从指定H2数据源获取连接时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试获取一个未注册数据源的连接
    @Test
    @Order(3)
    @DisplayName("3. 测试获取未注册数据源的连接")
    void testGetConnectionFromUnregistered() {
        System.out.println("\n--- 测试 3：尝试获取未注册数据源 'non_existent_db' 的连接 ---");
        String unregisteredDbName = "non_existent_db";
        try {
            DatabaseUtil21.DataSourceCore.getConnection(unregisteredDbName);
            // 如果代码执行到这里，说明没有抛出预期的异常，测试失败
            System.out.println("失败：尝试获取未注册数据源的连接时，没有抛出预期的异常。");
        } catch (Exception e) {
            // 捕获到异常，这是预期的行为
            System.out.println("成功：捕获到预期异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            // 验证异常类型是否符合预期
            if (e instanceof RuntimeException && e.getMessage().contains("数据库未注册")) {
                System.out.println("验证成功：异常类型和消息内容符合预期。");
            } else {
                System.out.println("验证失败：异常类型或消息内容不符合预期。");
                e.printStackTrace();
            }
        }
    }

    // 测试连接的 testConnection 方法
    @Test
    @Order(4)
    @DisplayName("4. 测试 testConnection 方法")
    void testTestConnection() {
        System.out.println("\n--- 测试 4：测试 testConnection 方法 ---");
        // 测试已注册的H2数据源
        boolean h2TestResult = DatabaseUtil21.DataSourceCore.testConnection(H2_DB_NAME);
        System.out.println("测试 '" + H2_DB_NAME + "' 连接结果: " + h2TestResult);
        if (h2TestResult) {
            System.out.println("成功：testConnection 对有效数据源返回 true。");
        } else {
            System.out.println("失败：testConnection 对有效数据源返回 false。");
        }

        // 测试未注册的数据源
        boolean nonExistentTestResult = DatabaseUtil21.DataSourceCore.testConnection("non_existent_db_2");
        System.out.println("测试 'non_existent_db_2' 连接结果: " + nonExistentTestResult);
        if (!nonExistentTestResult) {
            System.out.println("成功：testConnection 对无效数据源返回 false。");
        } else {
            System.out.println("失败：testConnection 对无效数据源返回 true。");
        }
    }
}
