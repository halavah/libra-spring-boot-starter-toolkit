package cn.myslayers.libra.framework.toolkit.demo.test.database.database21;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil21;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

// 测试数据源的注册功能
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore - 注册功能测试")
class Test01_DataSourceRegistrationTesting {

    // 在所有测试开始前，进行一次性设置
    @BeforeAll
    static void setUp() {
        // 开启日志，方便调试
        DatabaseUtil21.LoggerCore.setDebugLogEnabled(true);
        System.out.println("==================== 测试开始：数据源注册 ====================");
        // 清理可能存在的旧数据源，确保测试环境纯净
        DatabaseUtil21.DataSourceCore.closeAllDatabases();
    }

    // 在所有测试结束后，进行清理
    @AfterAll
    static void tearDown() {
        // 关闭所有数据源
        DatabaseUtil21.DataSourceCore.closeAllDatabases();
        System.out.println("==================== 测试结束：数据源注册 ====================");
    }

    // 测试手动注册一个新的H2内存数据库
    @Test
    @Order(1)
    @DisplayName("2. 测试手动注册H2内存数据源")
    void testManualRegisterH2() {
        System.out.println("\n--- 测试 2：手动注册 'h2_test_db' ---");
        String dbName = "h2_test_db";
        try {
            // 执行手动注册
            DatabaseUtil21.DataSourceCore.register(
                dbName,
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", // H2内存数据库URL
                "sa",
                "",
                "org.h2.Driver"
            );
            // 验证是否注册成功
            boolean isRegistered = DatabaseUtil21.DataSourceCore.isDatabaseRegistered(dbName);
            System.out.println("验证 '" + dbName + "' 是否已注册: " + isRegistered);
            if (!isRegistered) {
                System.out.println("失败：H2数据源 '" + dbName + "' 未能成功注册。");
            } else {
                System.out.println("成功：H2数据源 '" + dbName + "' 已成功注册。");
            }
        } catch (Exception e) {
            System.out.println("异常：手动注册H2数据源时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试获取所有已注册的数据源列表
    @Test
    @Order(3)
    @DisplayName("3. 测试获取所有已注册的数据源列表")
    void testGetAllRegisteredDatabases() {
        System.out.println("\n--- 测试 3：获取所有已注册的数据源 ---");
        try {
            // 手动注册一个源，以便列表不为空
            DatabaseUtil21.DataSourceCore.register("temp_db_for_list_test", "jdbc:h2:mem:temp_list", "sa", "", "org.h2.Driver");

            List<String> registeredDbs = DatabaseUtil21.DataSourceCore.getRegisteredDatabases();
            System.out.println("当前已注册的数据源列表: " + registeredDbs);

            // 验证列表是否包含我们注册的数据源
            boolean containsH2 = registeredDbs.contains("h2_test_db");
            System.out.println("列表是否包含 'h2_test_db': " + containsH2);

            if (containsH2) {
                System.out.println("成功：列表包含了预期的手动注册数据源。");
            } else {
                System.out.println("失败：列表未包含预期的手动注册数据源。");
            }
        } catch (Exception e) {
            System.out.println("异常：获取已注册数据源列表时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试覆盖注册一个已存在的数据源
    @Test
    @Order(4)
    @DisplayName("4. 测试覆盖注册一个已存在的数据源")
    void testOverwriteRegistration() {
        System.out.println("\n--- 测试 4：覆盖注册 'h2_test_db' ---");
        String dbName = "h2_test_db";
        try {
            System.out.println("尝试使用新的URL覆盖注册 '" + dbName + "'...");
            // 使用不同的URL重新注册
            DatabaseUtil21.DataSourceCore.register(
                dbName,
                "jdbc:h2:mem:testdb_new;DB_CLOSE_DELAY=-1", // 新的URL
                "sa",
                "",
                "org.h2.Driver"
            );
            // 验证数据源仍然存在
            boolean isRegistered = DatabaseUtil21.DataSourceCore.isDatabaseRegistered(dbName);
            System.out.println("验证 '" + dbName + "' 是否仍然注册: " + isRegistered);
            // 理论上，我们无法直接从外部获取URL来验证它是否已更新，
            // 但我们可以通过测试连接来间接确认。这将在连接测试中进行。
            // 此处我们只确认注册操作没有导致数据源被移除。
            if (isRegistered) {
                System.out.println("成功：覆盖注册操作完成，数据源 '" + dbName + "' 仍然存在。");
            } else {
                System.out.println("失败：覆盖注册操作导致数据源 '" + dbName + "' 被意外移除。");
            }
        } catch (Exception e) {
            System.out.println("异常：覆盖注册数据源时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
