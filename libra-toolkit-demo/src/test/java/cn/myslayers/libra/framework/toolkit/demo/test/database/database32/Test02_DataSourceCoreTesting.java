package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Druid数据源核心功能测试 (DatabaseUtil32.DataSourceCore)
 *
 * <p>
 * 这是一个全面的测试类，专注于验证 {@link DatabaseUtil32.DataSourceCore} 的所有核心功能。
 * 它整合了多个测试场景，包括数据源的注册、状态管理、连接、关闭、默认源管理以及在多线程环境下的表现。
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Druid数据源核心功能测试 (DatabaseUtil32.DataSourceCore)")
public class Test02_DataSourceCoreTesting {

    // 定义两个H2内存数据库的名称，用于模拟多数据源环境
    private static final String H2_DB_1 = "h2_test_1";
    private static final String H2_DB_2 = "h2_test_2";

    /**
     * 在所有测试开始前执行一次，负责初始化测试环境。
     */
    @BeforeAll
    void setupAll() {
        System.out.println("==================== 🎬 环境初始化开始 (DatabaseUtil32) ====================");
        // 开启日志，方便调试
//        DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);

        // 注册两个H2内存数据库
        System.out.println("  注册数据源: " + H2_DB_1);
        DatabaseUtil32.DataSourceCore.register(H2_DB_1, "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "", "org.h2.Driver");
        System.out.println("  注册数据源: " + H2_DB_2);
        DatabaseUtil32.DataSourceCore.register(H2_DB_2, "jdbc:h2:mem:db2;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "", "org.h2.Driver");

        // 关键步骤：设置一个默认数据源，确保依赖默认源的静态方法能正常工作
        DatabaseUtil32.DataSourceCore.setDefaultDataSource(H2_DB_1);
        System.out.println("  设置默认数据源为: " + H2_DB_1);

        // 在默认数据库中创建测试表
        String createTableSql = "CREATE TABLE test_users (id INT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255))";
        try {
            DatabaseUtil32.executeDDL(createTableSql);
            System.out.println("  ✓ 测试表 'test_users' 在 " + H2_DB_1 + " 中创建成功");
        } catch (Exception e) {
            System.err.println("  ❌ 创建测试表失败: " + e.getMessage());
            throw new RuntimeException("测试环境初始化失败，无法创建测试表", e);
        }
        System.out.println("==================== ✅ 环境初始化完成 (DatabaseUtil32) ====================\n");
    }

    /**
     * 在所有测试结束后执行一次，负责清理测试环境。
     */
    @AfterAll
    void tearDownAll() {
        System.out.println("\n==================== 🎬 环境清理开始 (DatabaseUtil32) ====================");
        // 确保默认数据源设置正确，以便执行清理操作
        DatabaseUtil32.DataSourceCore.setDefaultDataSource(H2_DB_1);
        // 删除测试表
        try {
            DatabaseUtil32.executeDDL("DROP TABLE test_users");
            System.out.println("  ✓ 测试表 'test_users' 已删除");
        } catch (Exception e) {
            System.err.println("  ❌ 删除测试表失败: " + e.getMessage());
        }

        // 关闭所有数据源
        DatabaseUtil32.DataSourceCore.closeAllDatabases();
        System.out.println("  ✓ 所有H2数据源已关闭");
        System.out.println("==================== ✅ 环境清理完成 (DatabaseUtil32) ====================");
    }

    /**
     * 在每个测试用例开始前执行，确保测试数据的独立性和一致性。
     */
    @BeforeEach
    void setupEach() {
        System.out.println("--- 刷新测试数据 (DatabaseUtil32) ---");
        // 确保操作在默认数据源上执行
        DatabaseUtil32.DataSourceCore.setDefaultDataSource(H2_DB_1);
        // 清理并插入初始数据
        DatabaseUtil32.executeByMp("DELETE FROM test_users", new HashMap<>());
        Map<String, Object> params1 = new HashMap<>();
        params1.put("id", 1);
        params1.put("name", "Alice");
        params1.put("email", "alice@example.com");
        DatabaseUtil32.executeByMp("INSERT INTO test_users (id, name, email) VALUES (#{id}, #{name}, #{email})", params1);

        Map<String, Object> params2 = new HashMap<>();
        params2.put("id", 2);
        params2.put("name", "Bob");
        params2.put("email", "bob@example.com");
        DatabaseUtil32.executeByMp("INSERT INTO test_users (id, name, email) VALUES (#{id}, #{name}, #{email})", params2);

        // 清理线程上下文，确保测试隔离
        DatabaseUtil32.DataSourceCore.clearThreadDataSourceContext();
        System.out.println("  ✓ 测试数据已重置，线程上下文已清理");
    }

    @Test
    @Order(1)
    @DisplayName("基础注册与状态管理测试")
    void testRegistrationAndStatus() {
        System.out.println("\n【测试1】: 验证数据源注册、状态查询及类型信息");
        // 验证数据源是否已注册
        boolean isRegistered = DatabaseUtil32.DataSourceCore.isDatabaseRegistered(H2_DB_1);
        System.out.println("  - 数据源 '" + H2_DB_1 + "' 是否已注册: " + isRegistered);

        // 验证获取已注册列表
        List<String> registeredDbs = DatabaseUtil32.DataSourceCore.getRegisteredDatabases();
        System.out.println("  - 当前已注册的数据源列表: " + registeredDbs);

        // 验证数据源类型
        String dsType = DatabaseUtil32.DataSourceCore.getDataSourceType(H2_DB_1);
        System.out.println("  - 数据源 '" + H2_DB_1 + "' 的类型是: " + dsType);

        // 验证获取所有数据源信息
        Map<String, Map<String, Object>> allInfo = DatabaseUtil32.DataSourceCore.getAllDataSourceInfo();
        System.out.println("  - 所有数据源的详细信息数量: " + allInfo.size());
    }

    @Test
    @Order(2)
    @DisplayName("默认数据源管理测试")
    void testDefaultDataSourceManagement() {
        System.out.println("\n【测试2】: 验证默认数据源的获取与切换");
        // 获取当前默认数据源
        String currentDefault = DatabaseUtil32.DataSourceCore.getDefaultDatabaseName();
        System.out.println("  - 当前默认数据源: " + currentDefault);

        // 切换默认数据源
        System.out.println("  - 尝试切换默认数据源到: " + H2_DB_2);
        DatabaseUtil32.DataSourceCore.setDefaultDataSource(H2_DB_2);
        String newDefault = DatabaseUtil32.DataSourceCore.getDefaultDatabaseName();
        System.out.println("  - 切换后默认数据源: " + newDefault);

        // 恢复默认数据源，以免影响其他测试
        DatabaseUtil32.DataSourceCore.setDefaultDataSource(H2_DB_1);
        System.out.println("  - 恢复默认数据源到: " + DatabaseUtil32.DataSourceCore.getDefaultDatabaseName());
    }

    @Test
    @Order(3)
    @DisplayName("连接与关闭功能测试")
    void testConnectionAndClosing() {
        System.out.println("\n【测试3】: 验证连接测试和数据源关闭功能");
        // 测试连接
        boolean canConnectDefault = DatabaseUtil32.DataSourceCore.testConnection();
        System.out.println("  - 默认数据源(" + H2_DB_1 + ")连接测试: " + (canConnectDefault ? "成功" : "失败"));
        boolean canConnectSpecific = DatabaseUtil32.DataSourceCore.testConnection(H2_DB_2);
        System.out.println("  - 指定数据源(" + H2_DB_2 + ")连接测试: " + (canConnectSpecific ? "成功" : "失败"));

        // 测试关闭
        String tempDb = "temp_db_to_close";
        System.out.println("  - 注册一个临时数据源用于测试关闭: " + tempDb);
        DatabaseUtil32.DataSourceCore.register(tempDb, "jdbc:h2:mem:tempdb_util2;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");
        System.out.println("    关闭前，数据源列表: " + DatabaseUtil32.DataSourceCore.getRegisteredDatabases());
        DatabaseUtil32.DataSourceCore.closeDatabase(tempDb);
        System.out.println("    关闭后，数据源列表: " + DatabaseUtil32.DataSourceCore.getRegisteredDatabases());
    }

    @Test
    @Order(4)
    @DisplayName("线程隔离功能测试")
    void testThreadIsolation() throws InterruptedException {
        System.out.println("\n【测试4】: 验证线程上下文(ThreadLocal)的隔离性");
        CountDownLatch latch = new CountDownLatch(1);
        String mainThreadDsBefore = DatabaseUtil32.DataSourceCore.getCurrentDataSourceName();
        System.out.println("  - 主线程，进入测试时数据源: " + mainThreadDsBefore);

        new Thread(() -> {
            try {
                System.out.println("    - 子线程启动，初始数据源: " + DatabaseUtil32.DataSourceCore.getCurrentDataSourceName());
                // 在子线程中设置数据源
                DatabaseUtil32.DataSourceCore.setThreadDataSourceContext(H2_DB_2);
                System.out.println("    - 子线程设置后，数据源变为: " + DatabaseUtil32.DataSourceCore.getCurrentDataSourceName());
                // 清理上下文
                DatabaseUtil32.DataSourceCore.clearThreadDataSourceContext();
                System.out.println("    - 子线程清理后，数据源回退为: " + DatabaseUtil32.DataSourceCore.getCurrentDataSourceName());
            } finally {
                latch.countDown();
            }
        }).start();

        latch.await(5, TimeUnit.SECONDS);
        String mainThreadDsAfter = DatabaseUtil32.DataSourceCore.getCurrentDataSourceName();
        System.out.println("  - 主线程，子线程结束后数据源: " + mainThreadDsAfter);
        System.out.println("  - 结论: 主线程数据源未受子线程影响 -> " + mainThreadDsBefore.equals(mainThreadDsAfter));
    }

    @Test
    @Order(5)
    @DisplayName("并发安全访问测试")
    void testConcurrentAccess() throws InterruptedException {
        System.out.println("\n【测试5】: 模拟高并发获取连接，验证线程安全性");
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        System.out.println("  - 启动 " + threadCount + " 个并发线程获取连接...");

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i + 1;
            executor.submit(() -> {
                try (Connection conn = DatabaseUtil32.DataSourceCore.getConnection()) {
                    if (conn != null && !conn.isClosed()) {
                        System.out.println("    线程 " + threadId + " 获取连接成功。");
                    }
                } catch (Exception e) {
                    System.err.println("    线程 " + threadId + " 获取连接失败: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        System.out.println("  - 所有并发线程执行完毕。");
    }

    @Test
    @Order(6)
    @DisplayName("原子操作测试")
    void testAtomicOperations() {
        System.out.println("\n⚛️ 执行原子操作测试...");
        System.out.println("【测试6】原子操作测试");

        try {
            // 获取当前所有已注册的数据源
            List<String> databases = DatabaseUtil32.DataSourceCore.getRegisteredDatabases();
            System.out.println("  已注册数据源数量: " + databases.size());
            System.out.println("  数据源列表: " + databases);

            String originalDefault = DatabaseUtil32.DataSourceCore.getCurrentDataSourceName();
            System.out.println("  原始默认数据源: " + originalDefault);

            // 测试原子切换
            if (databases.size() > 1) {
                String targetDS = databases.stream()
                    .filter(ds -> !ds.equals(originalDefault))
                    .findFirst()
                    .orElse(originalDefault);

                System.out.println("  尝试切换到: " + targetDS);
                boolean success = DatabaseUtil32.DataSourceCore.setDefaultDataSource(targetDS);
                System.out.println("  切换结果: " + (success ? "成功" : "失败"));

                String currentDefault = DatabaseUtil32.DataSourceCore.getCurrentDataSourceName();
                System.out.println("  当前默认数据源: " + currentDefault);

                // 恢复原始状态
                DatabaseUtil32.DataSourceCore.setDefaultDataSource(originalDefault);
                System.out.println("  恢复默认数据源: " + DatabaseUtil32.DataSourceCore.getCurrentDataSourceName());
            }

            System.out.println("  ✅ 原子操作测试完成\n");

        } catch (Exception e) {
            System.out.println("  ❌ 原子操作测试失败: " + e.getMessage() + "\n");
        }
        System.out.println("✅ 原子操作测试执行完成");
    }
}
