package cn.myslayers.libra.framework.toolkit.demo.test.database.database22;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil22;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;

// 测试 Druid 数据源的注册与配置验证
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DataSourceCore (Druid) - 注册与配置测试")
class Test01_DruidRegistrationAndConfigTesting {

    // 在每个测试方法开始前，重新设置环境
    @BeforeEach
    void setUp() {
        DatabaseUtil22.LoggerCore.setDebugLogEnabled(true);
        System.out.println("\n==================== 测试方法开始 ====================");
        // 清理并确保使用Druid
        DatabaseUtil22.DataSourceCore.closeAllDatabases();
        DatabaseUtil22.DataSourceCore.clearThreadDruidEnabled(); // 确保线程设置为默认
    }

    // 在每个测试方法结束后，清理环境
    @AfterEach
    void tearDown() {
        DatabaseUtil22.DataSourceCore.closeAllDatabases();
        System.out.println("==================== 测试方法结束 ====================\n");
    }

    // 测试默认情况下（全局开启Druid）注册的数据源是否为Druid类型
    @Test
    @Order(1)
    @DisplayName("1. 测试默认注册为Druid数据源")
    void testDefaultRegistrationIsDruid() {
        System.out.println("--- 测试 1：默认注册为Druid数据源 ---");
        String dbName = "druid_test_db";
        try {
            // 注册一个新的H2数据源
            DatabaseUtil22.DataSourceCore.register(
                dbName,
                "jdbc:h2:mem:druid_reg_test;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                "org.h2.Driver"
            );

            // 获取数据源类型进行验证
            String dsType = DatabaseUtil22.DataSourceCore.getDataSourceType(dbName);
            System.out.println("数据源 '" + dbName + "' 的类型是: " + dsType);

            if ("Druid连接池".equals(dsType)) {
                System.out.println("成功：默认情况下成功注册了Druid数据源。");
            } else {
                System.out.println("失败：默认情况下注册的数据源类型不正确，期望是'Druid连接池'，实际是'" + dsType + "'。");
            }
        } catch (Exception e) {
            System.out.println("异常：测试默认注册时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试获取Druid数据源的监控信息
    @Test
    @Order(2)
    @DisplayName("2. 测试获取Druid监控信息")
    void testGetDruidMonitorData() {
        System.out.println("--- 测试 2：获取Druid监控信息 ---");
        String dbName = "druid_monitor_test";
        try {
            DatabaseUtil22.DataSourceCore.register(
                dbName,
                "jdbc:h2:mem:druid_monitor;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                "org.h2.Driver"
            );

            // 获取所有数据源信息
            Map<String, Map<String, Object>> allInfo = DatabaseUtil22.DataSourceCore.getAllDataSourceInfo();
            Map<String, Object> monitorData = allInfo.get(dbName);

            System.out.println("获取到的监控数据: " + monitorData);

            if (monitorData != null && "Druid".equals(monitorData.get("type"))) {
                // Druid的initialSize默认为5，poolingCount应该是5
                Object poolingCount = monitorData.get("poolingCount");
                System.out.println("连接池中的连接数 (poolingCount): " + poolingCount);
                if (poolingCount != null && (Integer)poolingCount >= 1) { // initialSize 默认为 5，这里检查大于等于1即可
                    System.out.println("成功：成功获取到Druid监控数据，并且连接池已初始化。");
                } else {
                    System.out.println("失败：Druid连接池的初始连接数不符合预期。");
                }
            } else {
                System.out.println("失败：未能获取到 '" + dbName + "' 的Druid监控数据。");
            }
        } catch (Exception e) {
            System.out.println("异常：测试获取监控数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试通过线程级开关临时禁用Druid
    @Test
    @Order(3)
    @DisplayName("3. 测试线程级开关禁用Druid")
    void testThreadLocalDisableDruid() {
        System.out.println("--- 测试 3：线程级开关禁用Druid ---");
        String dbName = "simple_ds_test";
        try {
            // 在当前线程禁用Druid
            System.out.println("为当前线程临时禁用Druid...");
            DatabaseUtil22.DataSourceCore.setThreadDruidEnabled(false);

            // 注册一个新的数据源
            DatabaseUtil22.DataSourceCore.register(
                dbName,
                "jdbc:h2:mem:simple_ds;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                "org.h2.Driver"
            );

            // 验证数据源类型
            String dsType = DatabaseUtil22.DataSourceCore.getDataSourceType(dbName);
            System.out.println("禁用Druid后，数据源 '" + dbName + "' 的类型是: " + dsType);

            if ("简单数据源".equals(dsType)) {
                System.out.println("成功：通过线程级开关成功注册了简单数据源。");
            } else {
                System.out.println("失败：线程级开关未能成功禁用Druid。");
            }

        } catch (Exception e) {
            System.out.println("异常：测试线程级开关时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 必须清理线程设置
            DatabaseUtil22.DataSourceCore.clearThreadDruidEnabled();
        }
    }
}
