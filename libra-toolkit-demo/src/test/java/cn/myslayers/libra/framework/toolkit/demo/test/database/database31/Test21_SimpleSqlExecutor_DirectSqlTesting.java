package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31;
import java.util.HashMap;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil31 - SimpleSqlExecutor 直接SQL执行测试")
public class Test21_SimpleSqlExecutor_DirectSqlTesting {

    private static final String DB_NAME = "default_mysql1";
    private static final String TABLE_NAME = "test_users_executor";

    // 辅助方法：打印查询结果
    private void printQueryResult(String testName, List<Map<String, Object>> result) {
        System.out.println("--- " + testName + " ---");
        if (result == null) {
            System.out.println("Result: null");
        } else {
            System.out.println("Result (" + result.size() + " rows):");
            result.forEach(row -> System.out.println("  " + row));
        }
        System.out.println();
    }

    // 辅助方法：打印单条查询结果
    private void printSingleQueryResult(String testName, Map<String, Object> result) {
        System.out.println("--- " + testName + " ---");
        if (result == null) {
            System.out.println("Result: null");
        } else {
            System.out.println("Result: " + result);
        }
        System.out.println();
    }

    // 辅助方法：打印执行结果
    private void printExecuteResult(String testName, int affectedRows) {
        System.out.println("--- " + testName + " ---");
        System.out.println("Affected Rows: " + affectedRows);
        System.out.println();
    }

    @BeforeAll
    static void setUpAll() {
//        DatabaseUtil31.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil31.LoggerCore.setSqlDebugLogEnabled(true);
        System.out.println("========== SimpleSqlExecutor 直接SQL执行测试开始 ==========");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("========== SimpleSqlExecutor 直接SQL执行测试结束 ==========");
    }

    @BeforeEach
    void setUp() {
        // 创建测试表
        String createTableSql = "CREATE TABLE " + TABLE_NAME + " (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100), " +
            "email VARCHAR(100), " +
            "age INT, " +
            "status VARCHAR(20))";
        Map<String, Object> params = new HashMap<>();
        DatabaseUtil31.execute(createTableSql, params);

        // 插入初始数据
        String insertSql = "INSERT INTO " + TABLE_NAME + " (name, email, age, status) VALUES " +
            "('Alice', 'alice@example.com', 30, 'active'), " +
            "('Bob', 'bob@example.com', 25, 'inactive'), " +
            "('Charlie', 'charlie@example.com', 35, 'active')";
        DatabaseUtil31.execute(insertSql, params);
    }

    @AfterEach
    void tearDown() {
        // 删除测试表
        String dropTableSql = "DROP TABLE " + TABLE_NAME;
        Map<String, Object> params = new HashMap<>();
        DatabaseUtil31.execute(dropTableSql, params);
    }

    @Test
    @Order(1)
    @DisplayName("1. selectList - 基本查询")
    void testSelectListBasic() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status = #{status}";
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");

        List<Map<String, Object>> result = DatabaseUtil31.SimpleSqlExecutor.selectList(DB_NAME, sql, params);
        printQueryResult("selectList - 基本查询", result);
    }

    @Test
    @Order(2)
    @DisplayName("2. selectOne - 查询单条记录")
    void testSelectOne() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE name = #{name}";
        Map<String, Object> params = new HashMap<>();
        params.put("name", "Alice");

        Map<String, Object> result = DatabaseUtil31.SimpleSqlExecutor.selectOne(DB_NAME, sql, params);
        printSingleQueryResult("selectOne - 查询单条记录", result);
    }

    @Test
    @Order(3)
    @DisplayName("3. execute - INSERT 操作")
    void testExecuteInsert() {
        String sql = "INSERT INTO " + TABLE_NAME + " (name, email, age, status) VALUES (#{name}, #{email}, #{age}, #{status})";
        Map<String, Object> params = new HashMap<>();
        params.put("name", "David");
        params.put("email", "david@example.com");
        params.put("age", 40);
        params.put("status", "active");

        int affectedRows = DatabaseUtil31.SimpleSqlExecutor.execute(DB_NAME, sql, params);
        printExecuteResult("execute - INSERT 操作", affectedRows);

        // 验证插入
        List<Map<String, Object>> result = DatabaseUtil31.SimpleSqlExecutor.selectList(DB_NAME, "SELECT * FROM " + TABLE_NAME, new HashMap<>());
        printQueryResult("INSERT后全表数据", result);
    }

    @Test
    @Order(4)
    @DisplayName("4. execute - UPDATE 操作")
    void testExecuteUpdate() {
        String sql = "UPDATE " + TABLE_NAME + " SET age = #{newAge} WHERE name = #{name}";
        Map<String, Object> params = new HashMap<>();
        params.put("newAge", 31);
        params.put("name", "Alice");

        int affectedRows = DatabaseUtil31.SimpleSqlExecutor.execute(DB_NAME, sql, params);
        printExecuteResult("execute - UPDATE 操作", affectedRows);

        // 验证更新
        Map<String, Object> result = DatabaseUtil31.SimpleSqlExecutor.selectOne(DB_NAME, "SELECT * FROM " + TABLE_NAME + " WHERE name = 'Alice'", new HashMap<>());
        printSingleQueryResult("UPDATE后查询Alice", result);
    }

    @Test
    @Order(5)
    @DisplayName("5. execute - DELETE 操作")
    void testExecuteDelete() {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE name = #{name}";
        Map<String, Object> params = new HashMap<>();
        params.put("name", "Bob");

        int affectedRows = DatabaseUtil31.SimpleSqlExecutor.execute(DB_NAME, sql, params);
        printExecuteResult("execute - DELETE 操作", affectedRows);

        // 验证删除
        List<Map<String, Object>> result = DatabaseUtil31.SimpleSqlExecutor.selectList(DB_NAME, "SELECT * FROM " + TABLE_NAME, new HashMap<>());
        printQueryResult("DELETE后全表数据", result);
    }

    @Test
    @Order(6)
    @DisplayName("6. 动态SQL - <if> 标签 - 条件为 true")
    void testIfTagConditionTrue() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE 1=1 <if test='status != null'>AND status = #{status}</if>";
        Map<String, Object> params = new HashMap<>();
        params.put("status", "inactive");

        List<Map<String, Object>> result = DatabaseUtil31.SimpleSqlExecutor.selectList(DB_NAME, sql, params);
        printQueryResult("动态SQL - <if> 标签 - 条件为 true", result);
    }

    @Test
    @Order(7)
    @DisplayName("7. 动态SQL - <if> 标签 - 条件为 false")
    void testIfTagConditionFalse() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE 1=1 <if test='status != null'>AND status = #{status}</if>";
        Map<String, Object> params = new HashMap<>();
        params.put("status", null);

        List<Map<String, Object>> result = DatabaseUtil31.SimpleSqlExecutor.selectList(DB_NAME, sql, params);
        printQueryResult("动态SQL - <if> 标签 - 条件为 false", result);
    }

    @Test
    @Order(8)
    @DisplayName("8. 动态SQL - ${...} 替换表名和列名")
    void testDirectTextReplacement() {
        String sql = "SELECT name, age FROM ${tableName} ORDER BY ${columnName} DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", TABLE_NAME);
        params.put("columnName", "age");

        List<Map<String, Object>> result = DatabaseUtil31.SimpleSqlExecutor.selectList(DB_NAME, sql, params);
        printQueryResult("动态SQL - ${...} 替换", result);
    }

    @Test
    @Order(9)
    @DisplayName("9. 错误处理 - SQL语法错误")
    void testSqlSyntaxError() {
        String sql = "SELECTTT * FROM " + TABLE_NAME;
        Map<String, Object> params = new HashMap<>();

        try {
            DatabaseUtil31.SimpleSqlExecutor.selectList(DB_NAME, sql, params);
        } catch (Exception e) {
            System.out.println("--- 错误处理 - SQL语法错误 ---");
            System.out.println("成功捕获到预期的异常: " + e.getMessage());
        }
        System.out.println();
    }
}
