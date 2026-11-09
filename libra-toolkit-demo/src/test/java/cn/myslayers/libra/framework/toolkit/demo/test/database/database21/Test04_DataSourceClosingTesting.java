package cn.myslayers.libra.framework.toolkit.demo.test.database.database21;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil21;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

// 测试数据源的关闭和注销功能
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore - 关闭与注销测试")
class Test04_DataSourceClosingTesting {

    // 在每个测试方法开始前，重新设置环境
    @BeforeEach
    void setUp() {
        DatabaseUtil21.LoggerCore.setDebugLogEnabled(true);
        System.out.println("\n==================== 测试方法开始 ====================");
        // 清理并注册测试所需的数据源
        DatabaseUtil21.DataSourceCore.closeAllDatabases();
        // 手动注册三个H2数据库
        DatabaseUtil21.DataSourceCore.register("db_one", "jdbc:h2:mem:db_one_close;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
        DatabaseUtil21.DataSourceCore.register("db_two", "jdbc:h2:mem:db_two_close;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
        DatabaseUtil21.DataSourceCore.register("temp_db", "jdbc:h2:mem:temp_db_close;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
        // 确保 db_one 是默认
        DatabaseUtil21.DataSourceCore.setDefaultDataSource("db_one");
    }

    // 在每个测试方法结束后，清理环境
    @AfterEach
    void tearDown() {
        DatabaseUtil21.DataSourceCore.closeAllDatabases();
        System.out.println("==================== 测试方法结束 ====================\n");
    }

    // 测试关闭一个指定的数据源
    @Test
    @Order(1)
    @DisplayName("1. 测试关闭一个指定的数据源")
    void testCloseSingleDataSource() {
        System.out.println("--- 测试 1：关闭指定数据源 'db_two' ---");
        System.out.println("关闭前，已注册数据源: " + DatabaseUtil21.DataSourceCore.getRegisteredDatabases());

        // 执行关闭
        DatabaseUtil21.DataSourceCore.closeDatabase("db_two");
        System.out.println("执行关闭 'db_two' 操作后...");

        // 验证
        boolean isStillRegistered = DatabaseUtil21.DataSourceCore.isDatabaseRegistered("db_two");
        List<String> registeredDbs = DatabaseUtil21.DataSourceCore.getRegisteredDatabases();
        System.out.println("'db_two' 是否仍然注册: " + isStillRegistered);
        System.out.println("当前已注册数据源: " + registeredDbs);

        if (!isStillRegistered && registeredDbs.size() == 2 && registeredDbs.contains("db_one") && registeredDbs.contains("temp_db")) {
            System.out.println("成功：'db_two' 数据源已成功关闭并注销。");
        } else {
            System.out.println("失败：'db_two' 数据源未能正确关闭。");
        }
    }

    // 测试关闭默认数据源后的自动切换
    @Test
    @Order(2)
    @DisplayName("2. 测试关闭默认数据源后的自动切换")
    void testClosingDefaultDataSource() {
        System.out.println("--- 测试 2：关闭默认数据源 'db_one' ---");
        String originalDefault = "db_one";
        System.out.println("关闭前，默认数据源: " + DatabaseUtil21.DataSourceCore.getDefaultDatabaseName());
        System.out.println("关闭前，已注册数据源: " + DatabaseUtil21.DataSourceCore.getRegisteredDatabases());

        // 执行关闭
        DatabaseUtil21.DataSourceCore.closeDatabase(originalDefault);
        System.out.println("执行关闭 '" + originalDefault + "' 操作后...");

        // 验证
        String newDefault = DatabaseUtil21.DataSourceCore.getDefaultDatabaseName();
        boolean isOriginalDefaultRegistered = DatabaseUtil21.DataSourceCore.isDatabaseRegistered(originalDefault);
        System.out.println("新的默认数据源: " + newDefault);
        System.out.println("'" + originalDefault + "' 是否仍然注册: " + isOriginalDefaultRegistered);

        if (!isOriginalDefaultRegistered && newDefault != null && !newDefault.equals(originalDefault)) {
            System.out.println("成功：默认数据源已自动切换到 '" + newDefault + "'。");
        } else {
            System.out.println("失败：关闭默认数据源后，未能正确切换到新的默认数据源。");
        }
    }

    // 测试关闭所有数据源
    @Test
    @Order(3)
    @DisplayName("3. 测试关闭所有数据源")
    void testCloseAllDataSources() {
        System.out.println("--- 测试 3：关闭所有数据源 ---");
        System.out.println("关闭前，已注册数据源: " + DatabaseUtil21.DataSourceCore.getRegisteredDatabases());

        // 执行关闭所有
        DatabaseUtil21.DataSourceCore.closeAllDatabases();
        System.out.println("执行关闭所有数据源操作后...");

        // 验证
        List<String> registeredDbs = DatabaseUtil21.DataSourceCore.getRegisteredDatabases();
        String defaultDb = DatabaseUtil21.DataSourceCore.getDefaultDatabaseName();
        System.out.println("当前已注册数据源: " + registeredDbs);
        System.out.println("当前默认数据源: " + defaultDb);

        if (registeredDbs.isEmpty() && defaultDb == null) {
            System.out.println("成功：所有数据源均已关闭，列表为空，默认源为null。");
        } else {
            System.out.println("失败：关闭所有数据源操作未能完全清理。");
        }
    }
}
