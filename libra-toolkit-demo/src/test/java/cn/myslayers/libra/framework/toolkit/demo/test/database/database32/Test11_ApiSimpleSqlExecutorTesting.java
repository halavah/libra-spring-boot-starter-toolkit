package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * 高层API集成测试 - 简单SQL执行
 *
 * ## 测试目标:
 * 统一验证通过链式调用 (Accessor) 和静态方法 (Static) 执行简单SQL的功能。
 *
 * ## 测试方式:
 * - 使用 @Nested 注解将两种调用方式的测试分组。
 * - 使用简单的、带 #{param} 占位符的SQL语句。
 * - 直接与H2内存数据库交互。
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Test11: 简单SQL执行器")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Test11_ApiSimpleSqlExecutorTesting {

    private DatabaseUtil32.DatabaseAccessor accessor;

    @BeforeAll
    void setupDatabase() {
        try {
            if (!DatabaseUtil32.DataSourceCore.isDatabaseRegistered("h2_test")) {
                DatabaseUtil32.DataSourceCore.register(
                    "h2_test",
                    "jdbc:h2:mem:testdb_simple_sql;DB_CLOSE_DELAY=-1;MODE=MySQL",
                    "sa",
                    "",
                    "org.h2.Driver"
                );
            }
            DatabaseUtil32.DataSourceCore.setDefaultDataSource("h2_test");
            // 开启两种日志
//            DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);
//            DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);

            String createTableSql = "CREATE TABLE IF NOT EXISTS simple_users (" +
                                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                                    "name VARCHAR(255), " +
                                    "age INT" +
                                    ");";
            DatabaseUtil32.execute(createTableSql, null);

            accessor = DatabaseUtil32.accessor();

        } catch (Exception e) {
            System.err.println("Database setup failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void resetData() {
        DatabaseUtil32.execute("DELETE FROM simple_users", null);
    }

    @Test
    @Order(1)
    @DisplayName("链式调用 (Accessor) - 简单SQL: 测试Insert")
    void testInsert() {
        System.out.println("\n--- Running Test: testInsert (AccessorSimple) ---");
        String sql = "INSERT INTO simple_users (name, age) VALUES (#{name}, #{age})";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "John Doe");
        params.put("age", 30);
        int result = accessor.execute(sql, params).result();
        System.out.println("Insert Result Check: Expected=1, Actual=" + result + " -> " + (result == 1 ? "PASS" : "FAIL"));
    }

    @Test
    @Order(2)
    @DisplayName("链式调用 (Accessor) - 简单SQL: 测试SelectList")
    void testSelectList() {
        System.out.println("\n--- Running Test: testSelectList (AccessorSimple) ---");
        accessor.execute("INSERT INTO simple_users (name, age) VALUES ('Jane Doe', 28)", null);
        String sql = "SELECT * FROM simple_users WHERE age < #{maxAge}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("maxAge", 30);
        List<Map<String, Object>> result = accessor.selectList(sql, params);
        System.out.println("SelectList Result Size Check: Expected=1, Actual=" + result.size() + " -> " + (result.size() == 1 ? "PASS" : "FAIL"));
        if (!result.isEmpty()) {
            System.out.println("SelectList Name Check: Expected='Jane Doe', Actual='" + result.get(0).get("NAME") + "' -> " + ("Jane Doe".equals(result.get(0).get("NAME")) ? "PASS" : "FAIL"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("链式调用 (Accessor) - 简单SQL: 测试SelectOne")
    void testSelectOne() {
        System.out.println("\n--- Running Test: testSelectOne (AccessorSimple) ---");
        accessor.execute("INSERT INTO simple_users (name, age) VALUES ('Unique User', 45)", null);
        String sql = "SELECT * FROM simple_users WHERE name = #{name}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Unique User");
        Map<String, Object> result = accessor.selectOne(sql, params);
        System.out.println("SelectOne Result Not Null Check: " + (result != null ? "PASS" : "FAIL"));
        if (result != null) {
            System.out.println("SelectOne Age Check: Expected=45, Actual=" + result.get("AGE") + " -> " + (Integer.valueOf(45).equals(result.get("AGE")) ? "PASS" : "FAIL"));
        }
    }

    @Test
    @Order(4)
    @DisplayName("链式调用 (Accessor) - 简单SQL: 测试Update")
    void testUpdate() {
        System.out.println("\n--- Running Test: testUpdate (AccessorSimple) ---");
        accessor.execute("INSERT INTO simple_users (name, age) VALUES ('To Be Updated', 50)", null);
        String sql = "UPDATE simple_users SET age = #{newAge} WHERE name = #{name}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("newAge", 55);
        params.put("name", "To Be Updated");
        int result = accessor.execute(sql, params).result();
        System.out.println("Update Result Check: Expected=1, Actual=" + result + " -> " + (result == 1 ? "PASS" : "FAIL"));
        Map<String, Object> updatedUser = accessor.selectOne("SELECT age FROM simple_users WHERE name = 'To Be Updated'", null);
        System.out.println("Updated Age Check: Expected=55, Actual=" + updatedUser.get("AGE") + " -> " + (Integer.valueOf(55).equals(updatedUser.get("AGE")) ? "PASS" : "FAIL"));
    }

    @Test
    @Order(5)
    @DisplayName("链式调用 (Accessor) - 简单SQL: 测试Delete")
    void testDelete() {
        System.out.println("\n--- Running Test: testDelete (AccessorSimple) ---");
        accessor.execute("INSERT INTO simple_users (name, age) VALUES ('To Be Deleted', 99)", null);
        String sql = "DELETE FROM simple_users WHERE name = #{name}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "To Be Deleted");
        int result = accessor.execute(sql, params).result();
        System.out.println("Delete Result Check: Expected=1, Actual=" + result + " -> " + (result == 1 ? "PASS" : "FAIL"));
        Map<String, Object> deletedUser = accessor.selectOne("SELECT * FROM simple_users WHERE name = 'To Be Deleted'", null);
        System.out.println("Deleted User Null Check: " + (deletedUser == null ? "PASS" : "FAIL"));
    }

    @Test
    @Order(6)
    @DisplayName("静态方法 (Static) - 简单SQL: 测试Insert")
    void testStaticInsert() {
        System.out.println("\n--- Running Test: testStaticInsert (StaticSimple) ---");
        String sql = "INSERT INTO simple_users (name, age) VALUES (#{name}, #{age})";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Static John");
        params.put("age", 31);
        int result = DatabaseUtil32.execute(sql, params);
        System.out.println("Static Insert Result Check: Expected=1, Actual=" + result + " -> " + (result == 1 ? "PASS" : "FAIL"));
    }

    @Test
    @Order(7)
    @DisplayName("静态方法 (Static) - 简单SQL: 测试SelectList")
    void testStaticSelectList() {
        System.out.println("\n--- Running Test: testStaticSelectList (StaticSimple) ---");
        DatabaseUtil32.execute("INSERT INTO simple_users (name, age) VALUES ('Static Jane', 29)", null);
        String sql = "SELECT * FROM simple_users WHERE age < #{maxAge}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("maxAge", 30);
        List<Map<String, Object>> result = DatabaseUtil32.selectList(sql, params);
        System.out.println("Static SelectList Size Check: Expected=1, Actual=" + result.size() + " -> " + (result.size() == 1 ? "PASS" : "FAIL"));
        if (!result.isEmpty()) {
            System.out.println("Static SelectList Name Check: Expected='Static Jane', Actual='" + result.get(0).get("NAME") + "' -> " + ("Static Jane".equals(result.get(0).get("NAME")) ? "PASS" : "FAIL"));
        }
    }

    @Test
    @Order(8)
    @DisplayName("静态方法 (Static) - 简单SQL: 测试SelectOne")
    void testStaticSelectOne() {
        System.out.println("\n--- Running Test: testStaticSelectOne (StaticSimple) ---");
        DatabaseUtil32.execute("INSERT INTO simple_users (name, age) VALUES ('Static Unique', 46)", null);
        String sql = "SELECT * FROM simple_users WHERE name = #{name}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Static Unique");
        Map<String, Object> result = DatabaseUtil32.selectOne(sql, params);
        System.out.println("Static SelectOne Not Null Check: " + (result != null ? "PASS" : "FAIL"));
        if (result != null) {
            System.out.println("Static SelectOne Age Check: Expected=46, Actual=" + result.get("AGE") + " -> " + (Integer.valueOf(46).equals(result.get("AGE")) ? "PASS" : "FAIL"));
        }
    }

    @Test
    @Order(9)
    @DisplayName("静态方法 (Static) - 简单SQL: 测试Update")
    void testStaticUpdate() {
        System.out.println("\n--- Running Test: testStaticUpdate (StaticSimple) ---");
        DatabaseUtil32.execute("INSERT INTO simple_users (name, age) VALUES ('Static To Be Updated', 51)", null);
        String sql = "UPDATE simple_users SET age = #{newAge} WHERE name = #{name}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("newAge", 56);
        params.put("name", "Static To Be Updated");
        int result = DatabaseUtil32.execute(sql, params);
        System.out.println("Static Update Result Check: Expected=1, Actual=" + result + " -> " + (result == 1 ? "PASS" : "FAIL"));
        Map<String, Object> updatedUser = DatabaseUtil32.selectOne("SELECT age FROM simple_users WHERE name = 'Static To Be Updated'", null);
        System.out.println("Static Updated Age Check: Expected=56, Actual=" + updatedUser.get("AGE") + " -> " + (Integer.valueOf(56).equals(updatedUser.get("AGE")) ? "PASS" : "FAIL"));
    }

    @Test
    @Order(10)
    @DisplayName("静态方法 (Static) - 简单SQL: 测试Delete")
    void testStaticDelete() {
        System.out.println("\n--- Running Test: testStaticDelete (StaticSimple) ---");
        DatabaseUtil32.execute("INSERT INTO simple_users (name, age) VALUES ('Static To Be Deleted', 100)", null);
        String sql = "DELETE FROM simple_users WHERE name = #{name}";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Static To Be Deleted");
        int result = DatabaseUtil32.execute(sql, params);
        System.out.println("Static Delete Result Check: Expected=1, Actual=" + result + " -> " + (result == 1 ? "PASS" : "FAIL"));
        Map<String, Object> deletedUser = DatabaseUtil32.selectOne("SELECT * FROM simple_users WHERE name = 'Static To Be Deleted'", null);
        System.out.println("Static Deleted User Null Check: " + (deletedUser == null ? "PASS" : "FAIL"));
    }
}
