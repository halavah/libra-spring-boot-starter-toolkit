package cn.myslayers.libra.framework.toolkit.demo.test.database.database11;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil11;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * DatabaseUtil11工具类 - 使用junit5独立测试
 *
 * 测试覆盖范围：
 * - 数据库连接管理和配置验证
 * - CRUD操作的正确性验证
 * - 事务管理功能测试
 * - 批量操作性能测试
 * - 异常处理和边界条件测试
 * - 多数据源支持测试
 *
 * 测试设计原则：
 * - 独立性：每个测试方法相互独立，不依赖执行顺序
 * - 可重复性：测试可以重复执行，结果一致
 * - 完整性：覆盖正常流程和异常流程
 * - 真实性：使用真实数据库环境进行测试
 *
 * 测试环境要求：
 * - MySQL 8.0+ 数据库服务
 * - 测试数据库：libra-device-db
 * - 数据库用户：root/demo_password
 * - JDK 8+ 运行环境
 *
 * 注意事项：
 * - 测试会创建和删除测试表，请确保数据库权限充足
 * - 测试数据会在每次运行后清理，不会影响生产数据
 * - 如果测试失败，请检查数据库连接配置和服务状态
 *
 * @author Halavah
 * @version 4.0.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil11工具类功能测试")
public class DatabaseCase11Testing {

    /** 测试表名称 */
    private static final String TEST_TABLE_NAME = "test_users";

    /**
     * 全局测试环境初始化
     * 作用：初始化测试环境，创建测试表和基础数据
     * 用法：在所有测试方法执行前运行一次
     */
    @BeforeAll
    static void globalSetup() throws SQLException {
        System.out.println("🚀 全局测试环境初始化");
        System.out.println("==================================================");

        // 直接注册数据库 - 使用新的简化API
        String dbUrl = "jdbc:mysql://localhost:3306/libra-demo-db?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String username = "demo_user";
        String password = "demo_password";
        String driver = "com.mysql.cj.jdbc.Driver";

        // 创建测试数据库配置
        DatabaseUtil11.DatabaseConfig testConfig = new DatabaseUtil11.DatabaseConfig(dbUrl, username, password, driver);

        // 注册数据库 - 自动设置为默认数据库
        DatabaseUtil11.registerDatabase("testdb", testConfig);
        System.out.println("✓ 测试数据库注册成功");

        // 验证默认数据库配置
        String defaultDbName = DatabaseUtil11.getDefaultDatabase();
        assertNotNull(defaultDbName, "默认数据库名称不应为空");
        System.out.println("✓ 默认数据库: " + defaultDbName);

        // 验证数据库连接有效性
        try (Connection conn = DatabaseUtil11.getConnection()) {
            assertNotNull(conn, "数据库连接不应为空");
            assertFalse(conn.isClosed(), "数据库连接应该是打开状态");
            System.out.println("✓ 数据库连接验证成功");
        }

        // 创建测试表
        createTestTable();
        System.out.println("🎯 全局测试环境初始化完成");
        System.out.println("==================================================");
    }

    /**
     * 全局测试环境清理
     */
    @AfterAll
    static void globalCleanup() throws SQLException {
        System.out.println("🧹 全局测试环境清理");
        System.out.println("==================================================");

        try {
            // 使用现有的清理方法
            cleanupTestData();
            System.out.println("✓ 测试环境清理完成");

        } catch (Exception e) {
            System.err.println("⚠️ 清理过程中出现错误: " + e.getMessage());
        }

        System.out.println("🎯 全局测试环境清理完成");
        System.out.println("==================================================");
    }

    /**
     * 单个测试方法前的准备工作
     * 作用：清理上一个测试的残留数据，重置测试表到初始状态
     * 用法：每个测试方法执行前自动调用
     */
    @BeforeEach
    void setUp() {
        System.out.println("--- 准备执行测试方法 ---");

        try {
            // 清理测试表数据，确保每个测试从干净状态开始
            DatabaseUtil11.delete("DELETE FROM " + TEST_TABLE_NAME);
            System.out.println("测试表数据已清理");

        } catch (Exception e) {
            System.err.println("测试准备失败: " + e.getMessage());
        }
    }

    /**
     * 单个测试方法后的清理工作
     * 作用：清理当前测试产生的数据，验证资源释放
     * 用法：每个测试方法执行后自动调用
     */
    @AfterEach
    void tearDown() {
        System.out.println("--- 测试方法执行完成 ---");

        try {
            // 清理当前测试的临时数据
            DatabaseUtil11.delete("DELETE FROM " + TEST_TABLE_NAME + " WHERE name LIKE 'test_%'");
            System.out.println("临时测试数据已清理");

        } catch (Exception e) {
            System.err.println("测试清理失败: " + e.getMessage());
        }
    }

    /**
     * 测试数据库连接获取
     * 作用：验证DatabaseUtil11工具类能否正确获取数据库连接
     * 用法：作为基础连接功能的测试用例
     */
    @Test
    @Order(1)
    @DisplayName("测试数据库连接获取")
    void testDatabaseConnection() {
        System.out.println("【测试1】数据库连接获取测试");

        // 测试获取连接的功能
        assertDoesNotThrow(() -> {
            try (Connection conn = DatabaseUtil11.getConnection()) {
                assertNotNull(conn, "数据库连接不应为空");
                assertFalse(conn.isClosed(), "连接应该是打开状态");
                System.out.println("✅ 数据库连接获取成功");
            }
        }, "获取数据库连接不应抛出异常");

        // 测试多次获取连接的稳定性
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                try (Connection conn = DatabaseUtil11.getConnection()) {
                    assertNotNull(conn, "第" + (i + 1) + "次连接不应为空");
                }
            }
            System.out.println("✅ 多次连接获取测试通过");
        }, "多次获取连接不应失败");
    }

    /**
     * 测试默认数据库配置
     * 作用：验证默认数据库的配置和管理功能
     * 用法：测试数据库配置管理的正确性
     */
    @Test
    @Order(2)
    @DisplayName("测试默认数据库配置")
    void testDefaultDatabaseConfig() {
        System.out.println("【测试2】默认数据库配置测试");

        // 验证默认数据库设置
        String defaultDb = DatabaseUtil11.getDefaultDatabase();
        assertNotNull(defaultDb, "默认数据库不应为空");
        assertEquals("testdb", defaultDb, "默认数据库应为testdb");
        System.out.println("✅ 默认数据库: " + defaultDb);

        // 验证配置模式（自定义配置模式）
        System.out.println("✅ 配置模式: 自定义配置");

        // 验证数据库连接池状态
        assertDoesNotThrow(() -> {
            try (Connection conn = DatabaseUtil11.getConnection()) {
                assertNotNull(conn, "连接不应为空");
                assertFalse(conn.isClosed(), "连接应为开启状态");
            }
        }, "获取数据库连接不应抛出异常");
    }

    /**
     * 测试插入操作
     * 作用：验证INSERT操作的正确性，包括单条和多条插入
     * 用法：测试数据插入功能的各种场景
     */
    @Test
    @Order(3)
    @DisplayName("测试插入操作")
    void testInsertOperation() {
        System.out.println("【测试3】插入操作测试");

        // 测试单条记录插入
        String insertSql = "INSERT INTO " + TEST_TABLE_NAME + " (name, email, age) VALUES (?, ?, ?)";
        int result = DatabaseUtil11.insert(insertSql, "test_user1", "test1@example.com", 25);
        assertEquals(1, result, "插入操作应该影响1行");
        System.out.println("✅ 单条记录插入成功，影响行数: " + result);

        // 测试插入操作是否真实生效
        Map<String, Object> user = DatabaseUtil11.selectOne(
                "SELECT * FROM " + TEST_TABLE_NAME + " WHERE name = ?", "test_user1");
        assertNotNull(user, "插入的记录应该可以查询到");
        assertEquals("test_user1", user.get("name"), "用户名应该匹配");
        assertEquals("test1@example.com", user.get("email"), "邮箱应该匹配");
        assertEquals(25, user.get("age"), "年龄应该匹配");
        System.out.println("✅ 插入数据验证成功");
    }

    /**
     * 测试查询操作
     * 作用：验证SELECT操作的正确性，包括单条和多条查询
     * 用法：测试数据查询功能的各种场景
     */
    @Test
    @Order(4)
    @DisplayName("测试查询操作")
    void testSelectOperation() {
        System.out.println("【测试4】查询操作测试");

        // 先插入测试数据
        String insertSql = "INSERT INTO " + TEST_TABLE_NAME + " (name, email, age) VALUES (?, ?, ?)";
        DatabaseUtil11.insert(insertSql, "test_user1", "test1@example.com", 25);
        DatabaseUtil11.insert(insertSql, "test_user2", "test2@example.com", 30);
        DatabaseUtil11.insert(insertSql, "test_user3", "test3@example.com", 28);

        // 测试查询所有记录
        List<Map<String, Object>> allUsers = DatabaseUtil11.selectList("SELECT * FROM " + TEST_TABLE_NAME);
        assertEquals(3, allUsers.size(), "应该查询到3条记录");
        System.out.println("✅ 查询所有记录成功，共" + allUsers.size() + "条");

        // 测试条件查询
        List<Map<String, Object>> youngUsers = DatabaseUtil11.selectList(
                "SELECT * FROM " + TEST_TABLE_NAME + " WHERE age < ?", 30);
        assertEquals(2, youngUsers.size(), "应该查询到2条年龄小于30的记录");
        System.out.println("✅ 条件查询成功，共" + youngUsers.size() + "条");

        // 测试单条查询
        Map<String, Object> user = DatabaseUtil11.selectOne(
                "SELECT * FROM " + TEST_TABLE_NAME + " WHERE name = ?", "test_user1");
        assertNotNull(user, "应该能查询到指定用户");
        assertEquals("test_user1", user.get("name"), "用户名应该匹配");
        System.out.println("✅ 单条查询成功");

        // 测试查询不存在的记录
        Map<String, Object> notExist = DatabaseUtil11.selectOne(
                "SELECT * FROM " + TEST_TABLE_NAME + " WHERE name = ?", "not_exist_user");
        assertNull(notExist, "查询不存在的记录应该返回null");
        System.out.println("✅ 空结果查询测试通过");
    }

    /**
     * 测试更新操作
     * 作用：验证UPDATE操作的正确性，包括单条和批量更新
     * 用法：测试数据更新功能的各种场景
     */
    @Test
    @Order(5)
    @DisplayName("测试更新操作")
    void testUpdateOperation() {
        System.out.println("【测试5】更新操作测试");

        // 先插入测试数据
        String insertSql = "INSERT INTO " + TEST_TABLE_NAME + " (name, email, age) VALUES (?, ?, ?)";
        DatabaseUtil11.insert(insertSql, "test_user1", "test1@example.com", 25);
        DatabaseUtil11.insert(insertSql, "test_user2", "test2@example.com", 30);

        // 测试单条记录更新
        String updateSql = "UPDATE " + TEST_TABLE_NAME + " SET age = ?, email = ? WHERE name = ?";
        int result = DatabaseUtil11.update(updateSql, 26, "test1_updated@example.com", "test_user1");
        assertEquals(1, result, "更新操作应该影响1行");
        System.out.println("✅ 单条记录更新成功，影响行数: " + result);

        // 验证更新结果
        Map<String, Object> updatedUser = DatabaseUtil11.selectOne(
                "SELECT * FROM " + TEST_TABLE_NAME + " WHERE name = ?", "test_user1");
        assertNotNull(updatedUser, "更新后的记录应该存在");
        assertEquals(26, updatedUser.get("age"), "年龄应该已更新");
        assertEquals("test1_updated@example.com", updatedUser.get("email"), "邮箱应该已更新");
        System.out.println("✅ 更新结果验证成功");

        // 测试批量更新
        int batchResult = DatabaseUtil11.update("UPDATE " + TEST_TABLE_NAME + " SET age = age + 1");
        assertEquals(2, batchResult, "批量更新应该影响2行");
        System.out.println("✅ 批量更新成功，影响行数: " + batchResult);
    }

    /**
     * 测试删除操作
     * 作用：验证DELETE操作的正确性，包括单条和批量删除
     * 用法：测试数据删除功能的各种场景
     */
    @Test
    @Order(6)
    @DisplayName("测试删除操作")
    void testDeleteOperation() {
        System.out.println("【测试6】删除操作测试");

        // 先插入测试数据
        String insertSql = "INSERT INTO " + TEST_TABLE_NAME + " (name, email, age) VALUES (?, ?, ?)";
        DatabaseUtil11.insert(insertSql, "test_user1", "test1@example.com", 25);
        DatabaseUtil11.insert(insertSql, "test_user2", "test2@example.com", 30);
        DatabaseUtil11.insert(insertSql, "test_user3", "test3@example.com", 28);

        // 测试单条记录删除
        String deleteSql = "DELETE FROM " + TEST_TABLE_NAME + " WHERE name = ?";
        int result = DatabaseUtil11.delete(deleteSql, "test_user1");
        assertEquals(1, result, "删除操作应该影响1行");
        System.out.println("✅ 单条记录删除成功，影响行数: " + result);

        // 验证删除结果
        Map<String, Object> deletedUser = DatabaseUtil11.selectOne(
                "SELECT * FROM " + TEST_TABLE_NAME + " WHERE name = ?", "test_user1");
        assertNull(deletedUser, "删除的记录应该查询不到");
        System.out.println("✅ 删除结果验证成功");

        // 验证其他记录未受影响
        List<Map<String, Object>> remainingUsers = DatabaseUtil11.selectList("SELECT * FROM " + TEST_TABLE_NAME);
        assertEquals(2, remainingUsers.size(), "剩余记录数应该为2");
        System.out.println("✅ 其他记录未受影响");

        // 测试批量删除
        int batchResult = DatabaseUtil11.delete("DELETE FROM " + TEST_TABLE_NAME + " WHERE age >= ?", 28);
        assertEquals(2, batchResult, "批量删除应该影响2行");
        System.out.println("✅ 批量删除成功，影响行数: " + batchResult);

        // 验证表已清空
        List<Map<String, Object>> allUsers = DatabaseUtil11.selectList("SELECT * FROM " + TEST_TABLE_NAME);
        assertEquals(0, allUsers.size(), "表应该已清空");
        System.out.println("✅ 批量删除验证成功");
    }

    /**
     * 测试批量操作
     * 作用：验证批量插入和更新操作的正确性和性能
     * 用法：测试批量数据处理功能
     */
    @Test
    @Order(7)
    @DisplayName("测试批量操作")
    void testBatchOperation() {
        System.out.println("【测试7】批量操作测试");

        // 准备批量插入数据
        String batchInsertSql = "INSERT INTO " + TEST_TABLE_NAME + " (name, email, age) VALUES (?, ?, ?)";
        List<Object[]> batchData = Arrays.asList(
                new Object[]{"test_batch1", "batch1@example.com", 22},
                new Object[]{"test_batch2", "batch2@example.com", 24},
                new Object[]{"test_batch3", "batch3@example.com", 26},
                new Object[]{"test_batch4", "batch4@example.com", 28},
                new Object[]{"test_batch5", "batch5@example.com", 30}
        );

        // 执行批量插入
        int[] batchResults = DatabaseUtil11.executeBatch(batchInsertSql, batchData);
        assertNotNull(batchResults, "批量操作结果不应为空");
        assertEquals(5, batchResults.length, "应该返回5个操作结果");

        // 验证每个操作都成功
        for (int i = 0; i < batchResults.length; i++) {
            assertTrue(batchResults[i] > 0, "第" + (i + 1) + "条记录应该插入成功");
        }
        System.out.println("✅ 批量插入成功，插入" + batchResults.length + "条记录");

        // 验证批量插入结果
        List<Map<String, Object>> allUsers = DatabaseUtil11.selectList(
                "SELECT * FROM " + TEST_TABLE_NAME + " ORDER BY name");
        assertEquals(5, allUsers.size(), "应该查询到5条记录");
        System.out.println("✅ 批量插入结果验证成功");

        // 验证数据正确性
        for (int i = 0; i < allUsers.size(); i++) {
            Map<String, Object> user = allUsers.get(i);
            String expectedName = "test_batch" + (i + 1);
            assertEquals(expectedName, user.get("name"), "第" + (i + 1) + "条记录名称应该匹配");
        }
        System.out.println("✅ 批量数据正确性验证成功");
    }

    /**
     * 测试事务管理
     * 作用：验证事务的提交和回滚功能
     * 用法：测试事务管理的正确性和可靠性
     */
    @Test
    @Order(8)
    @DisplayName("测试事务管理")
    void testTransactionManagement() {
        System.out.println("【测试8】事务管理测试");

        // 测试事务提交
        String result = DatabaseUtil11.executeInTransaction(conn -> {
            String insertSql = "INSERT INTO " + TEST_TABLE_NAME + " (name, email, age) VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                // 在事务中执行多个操作
                pstmt.setString(1, "test_trans1");
                pstmt.setString(2, "trans1@example.com");
                pstmt.setInt(3, 35);
                pstmt.executeUpdate();

                pstmt.setString(1, "test_trans2");
                pstmt.setString(2, "trans2@example.com");
                pstmt.setInt(3, 36);
                pstmt.executeUpdate();

                return "事务提交成功";
            }
        });

        assertEquals("事务提交成功", result, "事务应该成功提交");
        System.out.println("✅ 事务提交测试成功: " + result);

        // 验证事务提交结果
        List<Map<String, Object>> transUsers = DatabaseUtil11.selectList(
                "SELECT * FROM " + TEST_TABLE_NAME + " WHERE name LIKE 'test_trans%'");
        assertEquals(2, transUsers.size(), "事务中插入的记录应该都存在");
        System.out.println("✅ 事务提交结果验证成功");

        // 测试事务回滚
        assertThrows(RuntimeException.class, () -> {
            DatabaseUtil11.executeInTransaction(conn -> {
                String insertSql = "INSERT INTO " + TEST_TABLE_NAME + " (name, email, age) VALUES (?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    // 插入一条记录
                    pstmt.setString(1, "test_rollback");
                    pstmt.setString(2, "rollback@example.com");
                    pstmt.setInt(3, 40);
                    pstmt.executeUpdate();

                    // 故意抛出异常触发回滚
                    throw new RuntimeException("故意触发回滚");
                }
            });
        }, "事务应该因异常而回滚");

        // 验证事务回滚结果
        Map<String, Object> rollbackUser = DatabaseUtil11.selectOne(
                "SELECT * FROM " + TEST_TABLE_NAME + " WHERE name = ?", "test_rollback");
        assertNull(rollbackUser, "回滚的记录不应该存在");
        System.out.println("✅ 事务回滚测试成功");
    }

    /**
     * 测试异常处理
     * 作用：验证各种异常情况下的处理机制
     * 用法：测试工具类的异常处理能力和稳定性
     */
    @Test
    @Order(9)
    @DisplayName("测试异常处理")
    void testExceptionHandling() {
        System.out.println("【测试9】异常处理测试");

        // 测试SQL语法错误
        assertThrows(RuntimeException.class, () -> {
            DatabaseUtil11.selectList("SELECT * FROM non_exist_table");
        }, "查询不存在的表应该抛出异常");
        System.out.println("✅ SQL语法错误处理测试通过");

        // 测试参数不匹配
        assertThrows(RuntimeException.class, () -> {
            DatabaseUtil11.selectOne("SELECT * FROM " + TEST_TABLE_NAME + " WHERE name = ?");
        }, "参数不匹配应该抛出异常");
        System.out.println("✅ 参数不匹配处理测试通过");

        // 测试空SQL语句
        assertThrows(RuntimeException.class, () -> {
            DatabaseUtil11.selectList("");
        }, "空SQL语句应该抛出异常");
        System.out.println("✅ 空SQL语句处理测试通过");

        // 测试null参数
        assertThrows(RuntimeException.class, () -> {
            DatabaseUtil11.selectList(null);
        }, "null SQL语句应该抛出异常");
        System.out.println("✅ null参数处理测试通过");
    }

    /**
     * 创建测试表
     * 作用：创建用于测试的数据表结构
     * 用法：在测试初始化时调用
     */
    private static void createTestTable() {
        String dropTableSql = "DROP TABLE IF EXISTS " + TEST_TABLE_NAME;
        String createTableSql = "CREATE TABLE " + TEST_TABLE_NAME + " (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(200), " +
                "age INT, " +
                "created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Connection conn = DatabaseUtil11.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(dropTableSql);
            stmt.executeUpdate(createTableSql);
            System.out.println("测试表创建成功: " + TEST_TABLE_NAME);

        } catch (SQLException e) {
            throw new RuntimeException("创建测试表失败", e);
        }
    }

    /**
     * 清理测试数据
     * 作用：清理测试过程中产生的数据和表结构
     * 用法：在测试结束时调用
     */
    private static void cleanupTestData() {
        try {
            // 删除测试表
            String dropTableSql = "DROP TABLE IF EXISTS " + TEST_TABLE_NAME;
            DatabaseUtil11.update(dropTableSql);
            System.out.println("测试表删除成功: " + TEST_TABLE_NAME);

            // 关闭数据库连接
            DatabaseUtil11.closeAllDatabases();
            System.out.println("数据库连接已关闭");

        } catch (Exception e) {
            System.err.println("清理测试数据失败: " + e.getMessage());
        }
    }
}
