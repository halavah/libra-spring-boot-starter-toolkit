package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.SimpleSqlParser.SimpleSqlParameterConverter;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.SimpleSqlParser.SimpleSqlWithParams;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil31 - SimpleSqlParser 直接SQL解析测试")
public class Test11_SimpleSqlParser_DirectSqlTesting {

    @BeforeAll
    static void setUpAll() {
//        DatabaseUtil31.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil31.LoggerCore.setSqlDebugLogEnabled(true);
        System.out.println("========== SimpleSqlParser 直接SQL执行测试开始 ==========");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("========== SimpleSqlParser 直接SQL执行测试结束 ==========");
    }

    private void printResult(String testName, SimpleSqlWithParams result) {
        System.out.println("--- " + testName + " ---");
        System.out.println("Parsed SQL: " + result.getSql());
        System.out.println("Parameters: " + java.util.Arrays.toString(result.getParams().toArray()));
        System.out.println();
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试基本的 `#{...}` 参数替换")
    void testBasicParameterReplacement() {
        String sql = "SELECT * FROM users WHERE id = #{userId} AND status = #{status}";
        Map<String, Object> params = new HashMap<>();
        params.put("userId", 101);
        params.put("status", "active");

        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertDirectSql(sql, params);
        printResult("基本参数替换", result);
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试 `${...}` 直接文本替换")
    void testDirectTextReplacement() {
        String sql = "SELECT * FROM ${tableName} ORDER BY ${columnName} DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "products");
        params.put("columnName", "create_time");

        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertDirectSql(sql, params);
        printResult("直接文本替换", result);
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试混合使用 `#{...}` 和 `${...}`")
    void testMixedPlaceholders() {
        String sql = "SELECT * FROM ${tableName} WHERE id = #{userId}";
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "users");
        params.put("userId", 123);

        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertDirectSql(sql, params);
        printResult("混合占位符", result);
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试 `<if>` 标签 - 条件为 true")
    void testIfTagConditionTrue() {
        String sql = "SELECT * FROM users WHERE 1=1 <if test='name != null'>AND name = #{name}</if>";
        Map<String, Object> params = new HashMap<>();
        params.put("name", "John Doe");

        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertDirectSql(sql, params);
        printResult("<if> 标签 - 条件为 true", result);
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试 `<if>` 标签 - 条件为 false")
    void testIfTagConditionFalse() {
        String sql = "SELECT * FROM users WHERE 1=1 <if test='name != null'>AND name = #{name}</if>";
        Map<String, Object> params = new HashMap<>();
        params.put("name", null);

        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertDirectSql(sql, params);
        printResult("<if> 标签 - 条件为 false", result);
    }

    @Test
    @Order(6)
    @DisplayName("6. 测试 `<if>` 标签 - 字符串比较条件")
    void testIfTagWithStringComparison() {
        String sql = "SELECT * FROM users WHERE 1=1 <if test='status == \"active\"'>AND is_active = 1</if>";
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");

        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertDirectSql(sql, params);
        printResult("<if> 标签 - 字符串比较", result);
    }

    @Test
    @Order(7)
    @DisplayName("7. 测试不含任何参数的SQL")
    void testSqlWithNoParameters() {
        String sql = "SELECT * FROM users";
        Map<String, Object> params = new HashMap<>();

        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertDirectSql(sql, params);
        printResult("不含任何参数的SQL", result);
    }

    @Test
    @Order(8)
    @DisplayName("8. 测试传入空的 params Map - 预期异常")
    void testWithEmptyParamsMap() {
        String sql = "SELECT * FROM users WHERE id = #{userId}";
        Map<String, Object> params = new HashMap<>();

        try {
            SimpleSqlParameterConverter.convertDirectSql(sql, params);
            // 如果没有抛出异常，则测试失败
            System.out.println("--- 传入空的 params Map ---");
            System.out.println("测试失败：未按预期抛出异常。");
        } catch (RuntimeException e) {
            // 捕获到预期的异常
            System.out.println("--- 传入空的 params Map ---");
            System.out.println("成功捕获到预期的异常: " + e.getCause().getMessage());
        }
        System.out.println();
    }

    @Test
    @Order(9)
    @DisplayName("9. 测试传入 null params Map")
    void testWithNullParamsMap() {
        String sql = "SELECT * FROM users WHERE 1=1 <if test='name != null'>AND name = #{name}</if>";

        // 在实际使用中，传入null可能导致空指针，但解析器应该能优雅处理
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertDirectSql(sql, null);
        printResult("传入 null params Map", result);
    }
}
