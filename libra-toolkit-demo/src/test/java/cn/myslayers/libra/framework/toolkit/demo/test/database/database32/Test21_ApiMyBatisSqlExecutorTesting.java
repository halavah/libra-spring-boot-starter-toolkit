package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

/**
 * 高层API集成测试 - MyBatis动态SQL执行
 *
 * ## 测试目标:
 * 统一验证通过链式调用 (Accessor) 和静态方法 (Static) 执行MyBatis动态SQL的功能。
 *
 * ## 测试方式:
 * - 使用 @Nested 注解将两种调用方式的测试分组。
 * - SQL可以是包含动态标签的片段，也可以是从XML加载。
 * - 直接与H2内存数据库交互。
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Test12: MyBatis SQL执行器")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Test21_ApiMyBatisSqlExecutorTesting {

    private DatabaseUtil32.DatabaseAccessor accessor;

    @BeforeAll
    void setupDatabase() {
        try {
            if (!DatabaseUtil32.DataSourceCore.isDatabaseRegistered("h2_test")) {
                DatabaseUtil32.DataSourceCore.register(
                    "h2_test",
                    "jdbc:h2:mem:testdb_mybatis_sql;DB_CLOSE_DELAY=-1;MODE=MySQL",
                    "sa",
                    "",
                    "org.h2.Driver"
                );
            }
            DatabaseUtil32.DataSourceCore.setDefaultDataSource("h2_test");
            // 开启两种日志
            DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);
            DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);

            String createTableSql = "CREATE TABLE IF NOT EXISTS mybatis_users (" +
                                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                                    "name VARCHAR(255), " +
                                    "status VARCHAR(50), " +
                                    "age INT, " +
                                    "email VARCHAR(255)" +
                                    ");";
            DatabaseUtil32.executeDDL(createTableSql);

            accessor = DatabaseUtil32.accessor();

        } catch (Exception e) {
            System.err.println("Database setup failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("链式调用 (Accessor) - MyBatis动态SQL: 测试 selectListByMp")
    void testSelectListByMp() {
        // Data setup from original @BeforeEach
        accessor.executeByMp("DELETE FROM mybatis_users", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (1, 'user_active_1', 'active')", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (2, 'user_active_2', 'active')", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (3, 'user_inactive_1', 'inactive')", null);

        System.out.println("\n--- Running Test: testSelectListByMp (AccessorMyBatis) ---");
        String sql = "SELECT * FROM mybatis_users <where> <if test=\"status != null\"> AND status = #{status} </if> </where>";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", "active");
        List<Map<String, Object>> result = accessor.selectListByMp(sql, params);
        System.out.println("SelectListByMp Size Check: Expected=2, Actual=" + result.size() + " -> " + (result.size() == 2 ? "PASS" : "FAIL"));
    }

    @Test
    @Order(2)
    @DisplayName("链式调用 (Accessor) - MyBatis动态SQL: 测试 selectOneByMp")
    void testSelectOneByMp() {
        // Data setup from original @BeforeEach
        accessor.executeByMp("DELETE FROM mybatis_users", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (1, 'user_active_1', 'active')", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (2, 'user_active_2', 'active')", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (3, 'user_inactive_1', 'inactive')", null);

        System.out.println("\n--- Running Test: testSelectOneByMp (AccessorMyBatis) ---");
        String sql = "SELECT * FROM mybatis_users WHERE name = #{name}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "user_inactive_1");
        Map<String, Object> result = accessor.selectOneByMp(sql, params);
        System.out.println("SelectOneByMp Not Null Check: " + (result != null ? "PASS" : "FAIL"));
        if (result != null) {
            System.out.println("SelectOneByMp Status Check: Expected='inactive', Actual='" + result.get("STATUS") + "' -> " + ("inactive".equals(result.get("STATUS")) ? "PASS" : "FAIL"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("链式调用 (Accessor) - MyBatis动态SQL: 测试 updateByMp")
    void testUpdateByMp() {
        // Data setup from original @BeforeEach
        accessor.executeByMp("DELETE FROM mybatis_users", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (1, 'user_active_1', 'active')", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (2, 'user_active_2', 'active')", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (3, 'user_inactive_1', 'inactive')", null);

        System.out.println("\n--- Running Test: testUpdateByMp (AccessorMyBatis) ---");
        String sql = "UPDATE mybatis_users <set> <if test=\"status != null\">status = #{status},</if> </set> WHERE name = #{name}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", "archived");
        params.put("name", "user_inactive_1");
        accessor.executeByMp(sql, params);
        Map<String, Object> updatedUser = accessor.selectOneByMp("SELECT status FROM mybatis_users WHERE name = 'user_inactive_1'", null);
        System.out.println("Updated Status Check: Expected='archived', Actual='" + updatedUser.get("STATUS") + "' -> " + ("archived".equals(updatedUser.get("STATUS")) ? "PASS" : "FAIL"));
    }

    @Test
    @Order(4)
    @DisplayName("链式调用 (Accessor) - MyBatis动态SQL: 测试 deleteByMp")
    void testDeleteByMp() {
        // Data setup from original @BeforeEach
        accessor.executeByMp("DELETE FROM mybatis_users", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (1, 'user_active_1', 'active')", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (2, 'user_active_2', 'active')", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (3, 'user_inactive_1', 'inactive')", null);

        System.out.println("\n--- Running Test: testDeleteByMp (AccessorMyBatis) ---");
        String sql = "DELETE FROM mybatis_users WHERE id IN <foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">#{id}</foreach>";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", Arrays.asList(1, 2));
        accessor.executeByMp(sql, params);
        long count = (long) accessor.selectOneByMp("SELECT COUNT(*) as count FROM mybatis_users", null).get("COUNT");
        System.out.println("Count After Delete Check: Expected=1, Actual=" + count + " -> " + (count == 1 ? "PASS" : "FAIL"));
    }

    @Test
    @Order(5)
    @DisplayName("链式调用 (Accessor) - MyBatis动态SQL: 测试 insertByMp")
    void testInsertByMp() {
        // Data setup from original @BeforeEach
        accessor.executeByMp("DELETE FROM mybatis_users", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (1, 'user_active_1', 'active')", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (2, 'user_active_2', 'active')", null);
        accessor.executeByMp("INSERT INTO mybatis_users (id, name, status) VALUES (3, 'user_inactive_1', 'inactive')", null);

        System.out.println("\n--- Running Test: testInsertByMp (AccessorMyBatis) ---");
        String sql = "DatabaseUtil32<foreach collection=\"users\" item=\"user\" separator=\",\">(#{user.name}, #{user.status})</foreach>";
        List<Map<String, Object>> newUsers = new ArrayList<Map<String, Object>>();
        Map<String, Object> user1 = new HashMap<String, Object>();
        user1.put("name", "new_user_1");
        user1.put("status", "pending");
        newUsers.add(user1);
        Map<String, Object> user2 = new HashMap<String, Object>();
        user2.put("name", "new_user_2");
        user2.put("status", "pending");
        newUsers.add(user2);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("users", newUsers);
        accessor.executeByMp(sql, params);
        long count = (long) accessor.selectOneByMp("SELECT COUNT(*) as count FROM mybatis_users WHERE status = 'pending'", null).get("COUNT");
        System.out.println("Count After Insert Check: Expected=2, Actual=" + count + " -> " + (count == 2 ? "PASS" : "FAIL"));
    }

    @Test
    @Order(6)
    @DisplayName("静态方法 (Static) - MyBatis动态SQL: 测试 selectListByMp")
    void testStaticSelectListByMp() {
        // Data setup from original @BeforeEach
        DatabaseUtil32.executeByMp("DELETE FROM mybatis_users", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (1, 'static_active_1', 'active', 25, 'test1@example.com')", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (2, 'static_active_2', 'active', 30, 'test2@example.com')", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (3, 'static_inactive_1', 'inactive', 35, 'another@example.com')", null);

        System.out.println("\n--- Running Test: testStaticSelectListByMp (StaticMyBatis) ---");
        String sql = "SELECT * FROM mybatis_users <where> <if test=\"status != null\"> AND status = #{status} </if> </where>";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", "active");
        List<Map<String, Object>> result = DatabaseUtil32.selectListByMp(sql, params);
        System.out.println("Static SelectListByMp Size Check: Expected=2, Actual=" + result.size() + " -> " + (result.size() == 2 ? "PASS" : "FAIL"));
    }

    @Test
    @Order(7)
    @DisplayName("静态方法 (Static) - MyBatis动态SQL: 测试 selectOneByMp")
    void testStaticSelectOneByMp() {
        // Data setup from original @BeforeEach
        DatabaseUtil32.executeByMp("DELETE FROM mybatis_users", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (1, 'static_active_1', 'active', 25, 'test1@example.com')", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (2, 'static_active_2', 'active', 30, 'test2@example.com')", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (3, 'static_inactive_1', 'inactive', 35, 'another@example.com')", null);

        System.out.println("\n--- Running Test: testStaticSelectOneByMp (StaticMyBatis) ---");
        String sql = "SELECT * FROM mybatis_users WHERE name = #{name}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "static_inactive_1");
        Map<String, Object> result = DatabaseUtil32.selectOneByMp(sql, params);
        System.out.println("Static SelectOneByMp Not Null Check: " + (result != null ? "PASS" : "FAIL"));
        if (result != null) {
            System.out.println("Static SelectOneByMp Status Check: Expected='inactive', Actual='" + result.get("STATUS") + "' -> " + ("inactive".equals(result.get("STATUS")) ? "PASS" : "FAIL"));
        }
    }

    @Test
    @Order(8)
    @DisplayName("静态方法 (Static) - MyBatis动态SQL: 测试 updateByMp")
    void testStaticUpdateByMp() {
        // Data setup from original @BeforeEach
        DatabaseUtil32.executeByMp("DELETE FROM mybatis_users", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (1, 'static_active_1', 'active', 25, 'test1@example.com')", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (2, 'static_active_2', 'active', 30, 'test2@example.com')", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (3, 'static_inactive_1', 'inactive', 35, 'another@example.com')", null);

        System.out.println("\n--- Running Test: testStaticUpdateByMp (StaticMyBatis) ---");
        String sql = "UPDATE mybatis_users <set><if test=\"status != null\">status = #{status},</if></set> WHERE name = #{name}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", "archived");
        params.put("name", "static_inactive_1");
        int result = DatabaseUtil32.executeByMp(sql, params);
        System.out.println("Static UpdateByMp Result Check: Expected=1, Actual=" + result + " -> " + (result == 1 ? "PASS" : "FAIL"));
        Map<String, Object> updatedUser = DatabaseUtil32.selectOneByMp("SELECT status FROM mybatis_users WHERE name = 'static_inactive_1'", null);
        System.out.println("Static Updated Status Check: Expected='archived', Actual='" + updatedUser.get("STATUS") + "' -> " + ("archived".equals(updatedUser.get("STATUS")) ? "PASS" : "FAIL"));
    }

    @Test
    @Order(9)
    @DisplayName("静态方法 (Static) - MyBatis动态SQL: 测试 deleteByMp")
    void testStaticDeleteByMp() {
        // Data setup from original @BeforeEach
        DatabaseUtil32.executeByMp("DELETE FROM mybatis_users", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (1, 'static_active_1', 'active', 25, 'test1@example.com')", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (2, 'static_active_2', 'active', 30, 'test2@example.com')", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (3, 'static_inactive_1', 'inactive', 35, 'another@example.com')", null);

        System.out.println("\n--- Running Test: testStaticDeleteByMp (StaticMyBatis) ---");
        String sql = "DELETE FROM mybatis_users WHERE id IN <foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">#{id}</foreach>";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", Arrays.asList(1, 2));
        int result = DatabaseUtil32.executeByMp(sql, params);
        System.out.println("Static DeleteByMp Result Check: Expected=2, Actual=" + result + " -> " + (result == 2 ? "PASS" : "FAIL"));
        long count = (long) DatabaseUtil32.selectOneByMp("SELECT COUNT(*) as count FROM mybatis_users", null).get("COUNT");
        System.out.println("Static Count After Delete Check: Expected=1, Actual=" + count + " -> " + (count == 1 ? "PASS" : "FAIL"));
    }

    @Test
    @Order(10)
    @DisplayName("静态方法 (Static) - MyBatis动态SQL: 测试 insertByMp")
    void testStaticInsertByMp() {
        // Data setup from original @BeforeEach
        DatabaseUtil32.executeByMp("DELETE FROM mybatis_users", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (1, 'static_active_1', 'active', 25, 'test1@example.com')", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (2, 'static_active_2', 'active', 30, 'test2@example.com')", null);
        DatabaseUtil32.executeByMp("INSERT INTO mybatis_users (id, name, status, age, email) VALUES (3, 'static_inactive_1', 'inactive', 35, 'another@example.com')", null);

        System.out.println("\n--- Running Test: testStaticInsertByMp (StaticMyBatis) ---");
        String sql = "DatabaseUtil32<foreach collection=\"users\" item=\"user\" separator=\",\">(#{user.name}, #{user.status})</foreach>";
        List<Map<String, Object>> newUsers = new ArrayList<Map<String, Object>>();
        Map<String, Object> user1 = new HashMap<String, Object>();
        user1.put("name", "static_new_1");
        user1.put("status", "pending");
        newUsers.add(user1);
        Map<String, Object> user2 = new HashMap<String, Object>();
        user2.put("name", "static_new_2");
        user2.put("status", "pending");
        newUsers.add(user2);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("users", newUsers);
        DatabaseUtil32.executeByMp(sql, params);
        long count = (long) DatabaseUtil32.selectOneByMp("SELECT COUNT(*) as count FROM mybatis_users WHERE status = 'pending'", null).get("COUNT");
        System.out.println("Static Count After Insert Check: Expected=2, Actual=" + count + " -> " + (count == 2 ? "PASS" : "FAIL"));
    }
}
