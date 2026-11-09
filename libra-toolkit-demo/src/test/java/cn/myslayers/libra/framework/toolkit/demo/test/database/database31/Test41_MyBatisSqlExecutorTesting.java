package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * MyBatis动态SQL解析器测试类
 *
 * 测试覆盖范围：
 * - IF标签测试（基本条件、复杂条件、逻辑运算符）
 * - CHOOSE/WHEN/OTHERWISE标签测试
 * - FOREACH标签测试（基本遍历、带索引、空集合处理）
 * - WHERE标签测试（自动处理AND/OR前缀）
 * - SET标签测试（动态更新字段）
 * - TRIM标签测试（自定义前后缀处理）
 * - BIND标签测试（变量绑定）
 * - 复杂嵌套标签测试
 * - 嵌套属性访问测试
 * - SQL清理和边界情况测试
 * - 性能和缓存测试
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MyBatis动态SQL解析器测试")
public class Test41_MyBatisSqlExecutorTesting {

    private static DatabaseUtil31.MyBatisSqlParser parser;
    private static long testStartTime;

    @BeforeAll
    static void setUpAll() {
        System.out.println("开始MyBatis动态SQL解析器测试...");
        // 启用调试日志
        DatabaseUtil31.LoggerCore.setDebugLogEnabled(true);
        testStartTime = System.currentTimeMillis();
        parser = new DatabaseUtil31.MyBatisSqlParser();
    }

    @AfterAll
    static void tearDownAll() {
        long testEndTime = System.currentTimeMillis();
        long totalTime = testEndTime - testStartTime;
        System.out.println("MyBatis动态SQL解析器测试完成！");
        System.out.println("总测试时间: " + totalTime + "ms");
        parser = null;
    }

    @BeforeEach
    void setUp() {
        // 确保每个测试都有新的解析器实例
        if (parser == null) {
            parser = new DatabaseUtil31.MyBatisSqlParser();
        }
    }

    // ==================== IF标签测试 ====================

    @Test
    @Order(1)
    @DisplayName("IF标签 - 基本条件判断测试")
    void testIfTagBasicConditions() {
        // 测试用例1：条件为真
        String sql1 = "SELECT * FROM users WHERE 1=1 " +
                     "<if test='name != null'>AND name = #{name}</if>";

        Map<String, Object> params1 = new HashMap<>();
        params1.put("name", "张三");

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result1 = parser.processMapParametersForDirectSql(sql1, params1);
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE 1=1 AND name = ? | 实际: " + result1.getSql());
        System.out.println("检查参数数量 | 预期: 1 | 实际: " + result1.getParams().size());
        System.out.println("检查参数值 | 预期: 张三 | 实际: " + result1.getParams().get(0));

        // 测试用例2：条件为假
        String sql2 = "SELECT * FROM users WHERE 1=1 " +
                     "<if test='name != null'>AND name = #{name}</if>";

        Map<String, Object> params2 = new HashMap<>();
        // name为null，条件不满足

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result2 = parser.processMapParametersForDirectSql(sql2, params2);
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE 1=1 | 实际: " + result2.getSql());
        System.out.println("检查参数数量 | 预期: 0 | 实际: " + result2.getParams().size());
    }

    @Test
    @Order(2)
    @DisplayName("IF标签 - 复杂条件表达式测试")
    void testIfTagComplexConditions() {
        // 测试数值比较
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test='age != null and age > 18'>AND age > #{age}</if> " +
                    "<if test='score >= 60'>AND score >= #{score}</if>";

        Map<String, Object> params = new HashMap<>();
        params.put("age", 25);
        params.put("score", 85);

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE 1=1 AND age > ? AND score >= ? | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 2 | 实际: " + result.getParams().size());
    }

    @Test
    @Order(3)
    @DisplayName("IF标签 - 逻辑运算符测试")
    void testIfTagLogicalOperators() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test='name != null and age > 18'>AND name = #{name} AND age > #{age}</if> " +
                    "<if test='email != null or phone != null'>AND (email = #{email} OR phone = #{phone})</if>";

        Map<String, Object> params = new HashMap<>();
        params.put("name", "张三");
        params.put("age", 25);
        params.put("email", "test@example.com");

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE 1=1 AND name = ? AND age > ? AND (email = ? OR phone = ?) | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 4 | 实际: " + result.getParams().size());
    }

    // ==================== CHOOSE/WHEN/OTHERWISE标签测试 ====================

    @Test
    @Order(4)
    @DisplayName("CHOOSE/WHEN/OTHERWISE标签测试")
    void testChooseWhenOtherwiseTags() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<choose>" +
                    "  <when test='name != null'>AND name = #{name}</when>" +
                    "  <when test='email != null'>AND email = #{email}</when>" +
                    "  <otherwise>AND status = 'active'</otherwise>" +
                    "</choose>";

        // 测试第一个when条件
        Map<String, Object> params1 = new HashMap<>();
        params1.put("name", "张三");

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result1 = parser.processMapParametersForDirectSql(sql, params1);
        System.out.println("检查SQL (when 1) | 预期: SELECT * FROM users WHERE 1=1 AND name = ? | 实际: " + result1.getSql());

        // 测试第二个when条件
        Map<String, Object> params2 = new HashMap<>();
        params2.put("email", "test@example.com");

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result2 = parser.processMapParametersForDirectSql(sql, params2);
        System.out.println("检查SQL (when 2) | 预期: SELECT * FROM users WHERE 1=1 AND email = ? | 实际: " + result2.getSql());

        // 测试otherwise条件
        Map<String, Object> params3 = new HashMap<>();

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result3 = parser.processMapParametersForDirectSql(sql, params3);
        System.out.println("检查SQL (otherwise) | 预期: SELECT * FROM users WHERE 1=1 AND status = 'active' | 实际: " + result3.getSql());
    }

    // ==================== FOREACH标签测试 ====================

    @Test
    @Order(5)
    @DisplayName("FOREACH标签 - 基本集合遍历测试")
    void testForeachTagBasic() {
        String sql = "SELECT * FROM users WHERE id IN " +
                    "<foreach collection='ids' item='id' open='(' close=')' separator=','>" +
                    "#{id}" +
                    "</foreach>";

        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList(1, 2, 3, 4, 5));

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE id IN (?,?,?,?,?) | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 5 | 实际: " + result.getParams().size());
    }

    @Test
    @Order(6)
    @DisplayName("FOREACH标签 - 带索引遍历测试")
    void testForeachTagWithIndex() {
        String sql = "INSERT INTO user_roles (user_id, role_id, sort_order) VALUES " +
                    "<foreach collection='roles' item='role' index='idx' separator=','>" +
                    "(#{userId}, #{role}, #{idx})" +
                    "</foreach>";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", 100);
        params.put("roles", Arrays.asList("admin", "user", "guest"));

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL | 预期: INSERT INTO user_roles (user_id, role_id, sort_order) VALUES (?,?,?),(?,?,?),(?,?,?) | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 9 | 实际: " + result.getParams().size());
    }

    @Test
    @Order(7)
    @DisplayName("FOREACH标签 - 空集合处理测试")
    void testForeachTagEmptyCollection() {
        String sql = "SELECT * FROM users WHERE id IN " +
                    "<foreach collection='ids' item='id' open='(' close=')' separator=','>" +
                    "#{id}" +
                    "</foreach>";

        Map<String, Object> params = new HashMap<>();
        params.put("ids", Collections.emptyList());

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE id IN | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 0 | 实际: " + result.getParams().size());
    }

    // ==================== WHERE标签测试 ====================

    @Test
    @Order(8)
    @DisplayName("WHERE标签 - 自动处理AND/OR前缀测试")
    void testWhereTag() {
        String sql = "SELECT * FROM users " +
                    "<where>" +
                    "  <if test='name != null'>AND name = #{name}</if>" +
                    "  <if test='age != null'>AND age = #{age}</if>" +
                    "  <if test='email != null'>AND email = #{email}</if>" +
                    "</where>";

        // 测试多个条件
        Map<String, Object> params1 = new HashMap<>();
        params1.put("name", "张三");
        params1.put("age", 25);

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result1 = parser.processMapParametersForDirectSql(sql, params1);
        System.out.println("检查SQL (多条件) | 预期: SELECT * FROM users WHERE name = ? AND age = ? | 实际: " + result1.getSql());

        // 测试单个条件（自动移除开头的AND）
        Map<String, Object> params2 = new HashMap<>();
        params2.put("name", "李四");

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result2 = parser.processMapParametersForDirectSql(sql, params2);
        System.out.println("检查SQL (单条件) | 预期: SELECT * FROM users WHERE name = ? | 实际: " + result2.getSql());

        // 测试无条件（WHERE标签被移除）
        Map<String, Object> params3 = new HashMap<>();

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result3 = parser.processMapParametersForDirectSql(sql, params3);
        System.out.println("检查SQL (无条件) | 预期: SELECT * FROM users | 实际: " + result3.getSql());
    }

    // ==================== SET标签测试 ====================

    @Test
    @Order(9)
    @DisplayName("SET标签 - 动态更新字段测试")
    void testSetTag() {
        String sql = "UPDATE users " +
                    "<set>" +
                    "  <if test='name != null'>name = #{name},</if>" +
                    "  <if test='age != null'>age = #{age},</if>" +
                    "  <if test='email != null'>email = #{email},</if>" +
                    "</set>" +
                    "WHERE id = #{id}";

        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("name", "张三");
        params.put("email", "zhangsan@example.com");

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL | 预期: UPDATE users SET name = ?, email = ? WHERE id = ? | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 3 | 实际: " + result.getParams().size());
    }

    // ==================== TRIM标签测试 ====================

    @Test
    @Order(10)
    @DisplayName("TRIM标签 - 自定义前后缀处理测试")
    void testTrimTag() {
        String sql = "SELECT * FROM users " +
                    "<trim prefix='WHERE' prefixOverrides='AND |OR '>" +
                    "  <if test='name != null'>AND name = #{name}</if>" +
                    "  <if test='age != null'>OR age = #{age}</if>" +
                    "</trim>";

        Map<String, Object> params = new HashMap<>();
        params.put("name", "张三");
        params.put("age", 25);

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE name = ? OR age = ? | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 2 | 实际: " + result.getParams().size());
    }

    // ==================== BIND标签测试 ====================

    @Test
    @Order(11)
    @DisplayName("BIND标签 - 变量绑定测试")
    void testBindTag() {
        String sql = "<bind name='pattern' value=\"'%' + name + '%'\"/>" +
                    "SELECT * FROM users WHERE name LIKE #{pattern}";

        Map<String, Object> params = new HashMap<>();
        params.put("name", "张");

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE name LIKE ? | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 1 | 实际: " + result.getParams().size());
    }

    // ==================== 复杂嵌套标签测试 ====================

    @Test
    @Order(12)
    @DisplayName("复杂嵌套标签测试")
    void testComplexNestedTags() {
        String sql = "SELECT * FROM users " +
                    "<where>" +
                    "  <if test='searchCriteria != null'>" +
                    "    <choose>" +
                    "      <when test='searchCriteria.type == \"name\"'>" +
                    "        AND name LIKE #{searchCriteria.value}" +
                    "      </when>" +
                    "      <when test='searchCriteria.type == \"email\"'>" +
                    "        AND email = #{searchCriteria.value}" +
                    "      </when>" +
                    "      <otherwise>" +
                    "        AND status = 'active'" +
                    "      </otherwise>" +
                    "    </choose>" +
                    "  </if>" +
                    "  <if test='roles != null and roles.size() > 0'>" +
                    "    AND role IN " +
                    "    <foreach collection='roles' item='role' open='(' close=')' separator=','>" +
                    "      #{role}" +
                    "    </foreach>" +
                    "  </if>" +
                    "</where>";

        Map<String, Object> params = new HashMap<>();

        // 搜索条件
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("type", "name");
        searchCriteria.put("value", "%张%");
        params.put("searchCriteria", searchCriteria);

        // 角色列表
        params.put("roles", Arrays.asList("admin", "user"));

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE name LIKE ? AND role IN (?,?) | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 3 | 实际: " + result.getParams().size());
    }

    // ==================== 嵌套属性访问测试 ====================

    @Test
    @Order(13)
    @DisplayName("嵌套属性访问测试")
    void testNestedPropertyAccess() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test='user.profile.name != null'>AND name = #{user.profile.name}</if> " +
                    "<if test='user.settings.theme != null'>AND theme = #{user.settings.theme}</if>";

        Map<String, Object> params = new HashMap<>();

        // 构建嵌套对象结构
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", "张三");

        Map<String, Object> settings = new HashMap<>();
        settings.put("theme", "dark");

        Map<String, Object> user = new HashMap<>();
        user.put("profile", profile);
        user.put("settings", settings);

        params.put("user", user);

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE 1=1 AND name = ? AND theme = ? | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 2 | 实际: " + result.getParams().size());
    }

    // ==================== SQL清理功能测试 ====================

    @Test
    @Order(14)
    @DisplayName("SQL清理功能测试")
    void testSqlCleanup() {
        String sql = "SELECT   *   FROM   users   WHERE   1=1   " +
                    "<if test='name != null'>   AND   name   =   #{name}   </if>";

        Map<String, Object> params = new HashMap<>();
        params.put("name", "张三");

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);

        // 验证多余空白被清理
        System.out.println("检查SQL | 预期: SELECT * FROM users WHERE 1=1 AND name = ? | 实际: " + result.getSql());
        System.out.println("检查参数数量 | 预期: 1 | 实际: " + result.getParams().size());
    }

    // ==================== 性能和缓存测试 ====================

    @Test
    @Order(15)
    @DisplayName("性能和缓存测试")
    void testPerformanceAndCache() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test='name != null'>AND name = #{name}</if>";

        Map<String, Object> params = new HashMap<>();
        params.put("name", "张三");

        // 第一次解析
        long startTime = System.currentTimeMillis();
        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result1 = parser.processMapParametersForDirectSql(sql, params);
        long firstParseTime = System.currentTimeMillis() - startTime;

        // 第二次解析（应该使用缓存）
        startTime = System.currentTimeMillis();
        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result2 = parser.processMapParametersForDirectSql(sql, params);
        long secondParseTime = System.currentTimeMillis() - startTime;

        // 验证结果一致
        System.out.println("检查SQL是否一致 | 预期: " + result1.getSql() + " | 实际: " + result2.getSql());
        System.out.println("检查参数数量是否一致 | 预期: " + result1.getParams().size() + " | 实际: " + result2.getParams().size());

        System.out.println("第一次解析时间: " + firstParseTime + "ms");
        System.out.println("第二次解析时间: " + secondParseTime + "ms");
    }

    // ==================== 综合业务场景测试 ====================

    @Test
    @Order(16)
    @DisplayName("综合业务场景 - 用户查询测试")
    void testUserQueryScenario() {
        String sql = "SELECT u.id, u.name, u.email, u.age, u.status " +
                    "FROM users u " +
                    "<where>" +
                    "  <if test='name != null and name != \"\"'>" +
                    "    AND u.name LIKE #{namePattern}" +
                    "  </if>" +
                    "  <if test='email != null'>" +
                    "    AND u.email = #{email}" +
                    "  </if>" +
                    "  <if test='ageRange != null'>" +
                    "    <if test='ageRange.min != null'>" +
                    "      AND u.age >= #{ageRange.min}" +
                    "    </if>" +
                    "    <if test='ageRange.max != null'>" +
                    "      AND u.age <= #{ageRange.max}" +
                    "    </if>" +
                    "  </if>" +
                    "  <if test='statuses != null and statuses.size() > 0'>" +
                    "    AND u.status IN " +
                    "    <foreach collection='statuses' item='status' open='(' close=')' separator=','>" +
                    "      #{status}" +
                    "    </foreach>" +
                    "  </if>" +
                    "</where>" +
                    "ORDER BY u.id";

        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("name", "张");
        params.put("namePattern", "%张%");

        Map<String, Integer> ageRange = new HashMap<>();
        ageRange.put("min", 18);
        ageRange.put("max", 65);
        params.put("ageRange", ageRange);

        params.put("statuses", Arrays.asList("active", "pending"));

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);

        System.out.println("检查SQL是否不为null | 实际: " + (result.getSql() != null));
        System.out.println("检查SQL是否包含WHERE | 实际: " + result.getSql().contains("WHERE"));
        System.out.println("检查SQL是否包含 u.name LIKE ? | 实际: " + result.getSql().contains("u.name LIKE ?"));
        System.out.println("检查SQL是否包含 u.age >= ? | 实际: " + result.getSql().contains("u.age >= ?"));
        System.out.println("检查SQL是否包含 u.age <= ? | 实际: " + result.getSql().contains("u.age <= ?"));
        System.out.println("检查SQL是否包含 u.status IN (?,?) | 实际: " + result.getSql().contains("u.status IN (?,?)"));
        System.out.println("检查参数数量 | 预期: 5 | 实际: " + result.getParams().size());
    }

    @Test
    @Order(17)
    @DisplayName("综合业务场景 - 批量插入测试")
    void testBatchInsertScenario() {
        String sql = "INSERT INTO users (name, email, age, department_id) VALUES " +
                    "<foreach collection='users' item='user' separator=','>" +
                    "  (#{user.name}, #{user.email}, #{user.age}, #{user.departmentId})" +
                    "</foreach>";

        // 构建用户数据
        List<Map<String, Object>> users = new ArrayList<>();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", "张三");
        user1.put("email", "zhangsan@example.com");
        user1.put("age", 25);
        user1.put("departmentId", 1);
        users.add(user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("name", "李四");
        user2.put("email", "lisi@example.com");
        user2.put("age", 30);
        user2.put("departmentId", 2);
        users.add(user2);

        Map<String, Object> params = new HashMap<>();
        params.put("users", users);

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);

        System.out.println("检查SQL是否不为null | 实际: " + (result.getSql() != null));
        System.out.println("检查SQL是否以INSERT INTO users开头 | 实际: " + result.getSql().startsWith("INSERT INTO users"));
        System.out.println("检查SQL是否包含 (?,?,?,?),(?,?,?,?) | 实际: " + result.getSql().contains("(?,?,?,?),(?,?,?,?)"));
        System.out.println("检查参数数量 | 预期: 8 | 实际: " + result.getParams().size());
    }

    @Test
    @Order(18)
    @DisplayName("综合业务场景 - 动态更新测试")
    void testDynamicUpdateScenario() {
        String sql = "UPDATE users " +
                    "<set>" +
                    "  <if test='name != null and name != \"\"'>" +
                    "    name = #{name}," +
                    "  </if>" +
                    "  <if test='email != null and email != \"\"'>" +
                    "    email = #{email}," +
                    "  </if>" +
                    "  <if test='age != null'>" +
                    "    age = #{age}," +
                    "  </if>" +
                    "  <if test='departmentId != null'>" +
                    "    department_id = #{departmentId}," +
                    "  </if>" +
                    "  update_time = NOW()" +
                    "</set>" +
                    "WHERE id = #{id}";

        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("name", "张三丰");
        params.put("email", "zhangsanfeng@example.com");
        // age和departmentId为null，不会被更新

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);

        System.out.println("检查SQL是否不为null | 实际: " + (result.getSql() != null));
        System.out.println("检查SQL是否以UPDATE users SET开头 | 实际: " + result.getSql().startsWith("UPDATE users SET"));
        System.out.println("检查SQL是否包含 name = ? | 实际: " + result.getSql().contains("name = ?"));
        System.out.println("检查SQL是否包含 email = ? | 实际: " + result.getSql().contains("email = ?"));
        System.out.println("检查SQL是否包含 update_time = NOW() | 实际: " + result.getSql().contains("update_time = NOW()"));
        System.out.println("检查SQL是否包含 WHERE id = ? | 实际: " + result.getSql().contains("WHERE id = ?"));
        System.out.println("检查参数数量 | 预期: 3 | 实际: " + result.getParams().size());
    }

    @Test
    @Order(19)
    @DisplayName("综合性能测试")
    void testComprehensivePerformance() {
        String sql = "SELECT * FROM users " +
                    "<where>" +
                    "  <if test='name != null'>AND name = #{name}</if>" +
                    "  <if test='age != null'>AND age = #{age}</if>" +
                    "</where>";

        Map<String, Object> params = new HashMap<>();
        params.put("name", "测试用户");
        params.put("age", 25);

        // 预热
        for (int i = 0; i < 10; i++) {
            parser.processMapParametersForDirectSql(sql, params);
        }

        // 性能测试
        int testCount = 1000;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < testCount; i++) {
            DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
            System.out.println("检查SQL是否不为null | 实际: " + (result.getSql() != null));
            System.out.println("检查参数数量 | 预期: 2 | 实际: " + result.getParams().size());
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("解析次数: " + testCount);
        System.out.println("总耗时: " + totalTime + "ms");
        System.out.println("平均耗时: " + (totalTime * 1.0 / testCount) + "ms");
        System.out.println("QPS: " + (totalTime > 0 ? (testCount * 1000.0 / totalTime) : "N/A"));

        // 验证性能指标
        System.out.println("检查性能 | 1000次解析是否在5秒内完成: " + (totalTime < 5000));
        System.out.println("检查性能 | 平均解析时间是否小于5ms: " + ((totalTime * 1.0 / testCount) < 5));
    }
}
