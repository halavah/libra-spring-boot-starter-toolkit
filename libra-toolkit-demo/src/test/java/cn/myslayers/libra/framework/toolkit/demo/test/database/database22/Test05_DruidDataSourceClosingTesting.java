package cn.myslayers.libra.framework.toolkit.demo.test.database.database22;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil22;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

// 测试数据源的关闭与资源释放
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore (Druid) - 数据源关闭测试")
class Test05_DruidDataSourceClosingTesting {

    private static final String DB_TO_CLOSE = "db_to_close";
    private static final String DB_TO_KEEP = "db_to_keep";

    // 在每个测试方法执行前，重新注册数据源
    @BeforeEach
    void setUp() {
        DatabaseUtil22.LoggerCore.setDebugLogEnabled(true);
        System.out.println("\n==================== 测试方法开始 ====================");
        DatabaseUtil22.DataSourceCore.closeAllDatabases();
        // 注册两个数据源用于测试
        DatabaseUtil22.DataSourceCore.register(
            DB_TO_CLOSE,
            "jdbc:h2:mem:db_close;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
        DatabaseUtil22.DataSourceCore.register(
            DB_TO_KEEP,
            "jdbc:h2:mem:db_keep;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
    }

    // 测试关闭单个数据源
    @Test
    @Order(1)
    @DisplayName("1. 测试关闭单个数据源")
    void testClosingSingleDataSource() {
        System.out.println("--- 测试 1：关闭单个数据源 ---");
        System.out.println("关闭前，已注册的数据源: " + DatabaseUtil22.DataSourceCore.getRegisteredDatabases());

        // 关闭一个数据源
        System.out.println("正在关闭数据源: " + DB_TO_CLOSE);
        DatabaseUtil22.DataSourceCore.closeDatabase(DB_TO_CLOSE);

        List<String> registeredDbs = DatabaseUtil22.DataSourceCore.getRegisteredDatabases();
        System.out.println("关闭后，已注册的数据源: " + registeredDbs);

        boolean isClosedDbPresent = registeredDbs.contains(DB_TO_CLOSE);
        boolean isKeptDbPresent = registeredDbs.contains(DB_TO_KEEP);

        if (!isClosedDbPresent && isKeptDbPresent) {
            System.out.println("成功：指定的数据源已关闭，而其他数据源保持不变。");
        } else {
            System.out.println("失败：数据源关闭状态不符合预期。");
        }

        // 尝试从已关闭的数据源获取连接
        try {
            DatabaseUtil22.DataSourceCore.getConnection(DB_TO_CLOSE);
            System.out.println("失败：从已关闭的数据源获取连接时没有抛出异常。");
        } catch (Exception e) {
            System.out.println("成功：尝试从已关闭的数据源获取连接时，按预期抛出异常: " + e.getMessage());
        }
    }

    // 测试关闭所有数据源
    @Test
    @Order(2)
    @DisplayName("2. 测试关闭所有数据源")
    void testClosingAllDataSources() {
        System.out.println("--- 测试 2：关闭所有数据源 ---");
        System.out.println("关闭前，已注册的数据源: " + DatabaseUtil22.DataSourceCore.getRegisteredDatabases());

        // 关闭所有数据源
        System.out.println("正在关闭所有数据源...");
        DatabaseUtil22.DataSourceCore.closeAllDatabases();

        List<String> registeredDbs = DatabaseUtil22.DataSourceCore.getRegisteredDatabases();
        System.out.println("关闭后，已注册的数据源: " + registeredDbs);

        if (registeredDbs.isEmpty()) {
            System.out.println("成功：所有数据源均已成功关闭。");
        } else {
            System.out.println("失败：关闭所有数据源后，仍有数据源存在。");
        }

        // 验证默认数据源也被清空
        String defaultDb = DatabaseUtil22.DataSourceCore.getDefaultDatabaseName();
        if (defaultDb == null) {
            System.out.println("成功：默认数据源已被清空。");
        } else {
            System.out.println("失败：默认数据源未被清空。");
        }
    }
}
