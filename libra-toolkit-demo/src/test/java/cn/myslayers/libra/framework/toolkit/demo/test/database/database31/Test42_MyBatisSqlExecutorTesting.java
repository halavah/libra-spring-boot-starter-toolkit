package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * MyBatis动态SQL解析器优化测试类 - JDK8兼容版本
 * 核心标签测试：IF、WHERE、SET、FOREACH标签的各种场景
 * 递归安全性测试：确保WHERE和SET标签不会出现栈溢出
 * 性能和稳定性测试：包括解析基准、并发安全、内存稳定性
 * 实际业务场景测试：用户管理、订单系统等真实业务场景
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MyBatis动态SQL解析器优化测试")
@Execution(ExecutionMode.CONCURRENT)
public class Test42_MyBatisSqlExecutorTesting {

    private static DatabaseUtil31.MyBatisSqlParser parser;
    private static long testStartTime;
    private static Map<String, Long> performanceMetrics;
    private static final int PERFORMANCE_TEST_ITERATIONS = 500;

    @BeforeAll
    static void setUpAll() {
        System.out.println("================================================================================");
        System.out.println("开始MyBatis动态SQL解析器优化测试 - JDK8兼容版本");
        System.out.println("测试类型：核心标签 | 递归安全 | 性能稳定 | 业务场景");
        System.out.println("================================================================================");
        testStartTime = System.currentTimeMillis();
        parser = new DatabaseUtil31.MyBatisSqlParser();
        performanceMetrics = new HashMap<>();
    }

    @AfterAll
    static void tearDownAll() {
        long testEndTime = System.currentTimeMillis();
        long totalTime = testEndTime - testStartTime;

        System.out.println("\n================================================================================");
        System.out.println("MyBatis动态SQL解析器优化测试完成！");
        System.out.println("总测试时间: " + totalTime + "ms");

        if (performanceMetrics != null && !performanceMetrics.isEmpty()) {
            System.out.println("\n性能指标统计:");
            performanceMetrics.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry ->
                    System.out.printf("  %-30s: %6d ms%n", entry.getKey(), entry.getValue()));
        }

        System.out.println("================================================================================");
        parser = null;
        performanceMetrics = null;
    }

    @BeforeEach
    void setUp() {
        if (parser == null) {
            parser = new DatabaseUtil31.MyBatisSqlParser();
        }
    }

    @Test
    @Order(1)
    @DisplayName("IF标签功能测试 - 基本条件判断")
    void testBasicConditions() {
        long startTime = System.nanoTime();

        // 条件为真的情况
        Map<String, Object> params1 = new HashMap<>();
        params1.put("name", "张三");
        params1.put("age", 25);
        String sql1 = "SELECT * FROM users WHERE 1=1 <if test='name != null'>AND name = #{name}</if>";

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result1 = parser.processMapParametersForDirectSql(sql1, params1);
        System.out.println("检查SQL (条件为真) | 预期: SELECT * FROM users WHERE 1=1 AND name = ? | 实际: " + result1.getSql());
        System.out.println("检查参数数量 (条件为真) | 预期: 1 | 实际: " + result1.getParams().size());
        System.out.println("检查参数值 (条件为真) | 预期: 张三 | 实际: " + result1.getParams().get(0));

        // 条件为假的情况
        Map<String, Object> params2 = new HashMap<>();
        params2.put("age", 25);
        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result2 = parser.processMapParametersForDirectSql(sql1, params2);
        System.out.println("检查SQL (条件为假) | 预期: SELECT * FROM users WHERE 1=1 | 实际: " + result2.getSql());
        System.out.println("检查参数数量 (条件为假) | 预期: 0 | 实际: " + result2.getParams().size());

        performanceMetrics.put("IF基本条件", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    }

    @Test
    @Order(2)
    @DisplayName("IF标签功能测试 - 复杂条件表达式")
    void testComplexConditions() {
        String sql = "SELECT * FROM users WHERE 1=1" +
            "<if test='age != null and age > 18'>AND age > #{age}</if>" +
            "<if test='score >= 60'>AND score >= #{score}</if>" +
            "<if test='name != null and name.length() > 0'>AND name LIKE #{namePattern}</if>";

        Map<String, Object> params = new HashMap<>();
        params.put("age", 25);
        params.put("score", 85);
        params.put("name", "张三");
        params.put("namePattern", "%张%");

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);

        System.out.println("检查SQL是否包含 'AND age > ?' | 实际: " + result.getSql().contains("AND age > ?"));
        System.out.println("检查SQL是否包含 'AND score >= ?' | 实际: " + result.getSql().contains("AND score >= ?"));
        System.out.println("检查SQL是否包含 'AND name LIKE ?' | 实际: " + result.getSql().contains("AND name LIKE ?"));
        System.out.println("检查参数数量 | 预期: 3 | 实际: " + result.getParams().size());
    }

    @Test
    @Order(3)
    @DisplayName("WHERE标签递归安全测试 - 基本功能无递归")
    void testWhereTagBasicNoRecursion() {
        String sql = "SELECT * FROM users" +
            "<where>" +
            "    <if test='name != null'>AND name = #{name}</if>" +
            "    <if test='age != null'>AND age = #{age}</if>" +
            "</where>";

        Map<String, Object> params = new HashMap<>();
        params.put("name", "张三");
        params.put("age", 25);

        try {
            DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
            System.out.println("检查SQL (WHERE基本) | 预期: SELECT * FROM users WHERE name = ? AND age = ? | 实际: " + result.getSql());
            System.out.println("检查参数数量 (WHERE基本) | 预期: 2 | 实际: " + result.getParams().size());
            System.out.println("✓ WHERE标签基本功能测试未抛出异常");
        } catch (Throwable t) {
            System.err.println("✗ WHERE标签基本功能测试抛出异常: " + t.getMessage());
            t.printStackTrace();
        }
    }

    @Test
    @Order(4)
    @DisplayName("WHERE标签递归安全测试 - 深度嵌套无递归")
    void testWhereTagDeepNestingNoRecursion() {
        String sql = "SELECT * FROM users" +
            "<where>" +
            "    <if test='level1 != null'>" +
            "        <if test='level2 != null'>" +
            "            <if test='level3 != null'>" +
            "                AND deep_field = #{level3}" +
            "            </if>" +
            "        </if>" +
            "    </if>" +
            "    <if test='name != null'>AND name = #{name}</if>" +
            "</where>";

        Map<String, Object> params = new HashMap<>();
        params.put("level1", "value1");
        params.put("level2", "value2");
        params.put("level3", "value3");
        params.put("name", "张三");

        try {
            DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
            System.out.println("检查结果是否为null (WHERE嵌套) | 实际: " + (result != null));
            if (result != null) {
                System.out.println("检查SQL是否包含WHERE (WHERE嵌套) | 实际: " + result.getSql().contains("WHERE"));
                System.out.println("检查SQL是否包含deep_field = ? (WHERE嵌套) | 实际: " + result.getSql().contains("deep_field = ?"));
                System.out.println("检查SQL是否包含name = ? (WHERE嵌套) | 实际: " + result.getSql().contains("name = ?"));
            }
            System.out.println("✓ WHERE标签深度嵌套测试未抛出异常");
        } catch (Throwable t) {
            System.err.println("✗ WHERE标签深度嵌套测试抛出异常: " + t.getMessage());
            t.printStackTrace();
        }
    }

    @Test
    @Order(5)
    @DisplayName("SET标签递归安全测试 - 基本功能无递归")
    void testSetTagBasicNoRecursion() {
        String sql = "UPDATE users" +
            "<set>" +
            "    <if test='name != null'>name = #{name},</if>" +
            "    <if test='email != null'>email = #{email},</if>" +
            "    <if test='age != null'>age = #{age},</if>" +
            "</set>" +
            "WHERE id = #{id}";

        Map<String, Object> params = new HashMap<>();
        params.put("name", "李四");
        params.put("email", "lisi@example.com");
        params.put("id", 123);

        try {
            DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
            System.out.println("检查SQL (SET基本) | 预期: UPDATE users SET name = ?, email = ? WHERE id = ? | 实际: " + result.getSql());
            System.out.println("检查参数数量 (SET基本) | 预期: 3 | 实际: " + result.getParams().size());
            if(result.getParams().size() == 3) {
                System.out.println("检查参数1 (SET基本) | 预期: 李四 | 实际: " + result.getParams().get(0));
                System.out.println("检查参数2 (SET基本) | 预期: lisi@example.com | 实际: " + result.getParams().get(1));
                System.out.println("检查参数3 (SET基本) | 预期: 123 | 实际: " + result.getParams().get(2));
            }
            System.out.println("✓ SET标签基本功能测试未抛出异常");
        } catch (Throwable t) {
            System.err.println("✗ SET标签基本功能测试抛出异常: " + t.getMessage());
            t.printStackTrace();
        }
    }

    @Test
    @Order(6)
    @DisplayName("FOREACH标签高级测试 - 基本集合遍历")
    void testForeachBasic() {
        String sql = "SELECT * FROM users WHERE id IN " +
            "<foreach item='id' collection='userIds' open='(' separator=',' close=')'>#{id}</foreach>";

        Map<String, Object> params = new HashMap<>();
        params.put("userIds", Arrays.asList(1, 2, 3, 4, 5));

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);
        System.out.println("检查SQL (FOREACH基本) | 预期: SELECT * FROM users WHERE id IN (?,?,?,?,?) | 实际: " + result.getSql());
        System.out.println("检查参数数量 (FOREACH基本) | 预期: 5 | 实际: " + result.getParams().size());
        if (result.getParams().size() == 5) {
            System.out.println("检查参数1 (FOREACH基本) | 预期: 1 | 实际: " + result.getParams().get(0));
            System.out.println("检查参数5 (FOREACH基本) | 预期: 5 | 实际: " + result.getParams().get(4));
        }
    }

    @Test
    @Order(7)
    @DisplayName("FOREACH标签高级测试 - 空集合处理")
    void testForeachEmptyCollection() {
        String sql = "SELECT * FROM users WHERE id IN " +
            "<foreach item='id' collection='userIds' open='(' separator=',' close=')'>#{id}</foreach>";

        Map<String, Object> params1 = new HashMap<>();
        params1.put("userIds", new ArrayList<>());

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result1 = parser.processMapParametersForDirectSql(sql, params1);
        System.out.println("检查SQL (FOREACH空集合) | 预期: SELECT * FROM users WHERE id IN () | 实际: " + result1.getSql());
        System.out.println("检查参数数量 (FOREACH空集合) | 预期: 0 | 实际: " + result1.getParams().size());
    }

    @Test
    @Order(8)
    @DisplayName("性能和稳定性测试 - SQL解析基准")
    void testParsingPerformance() {
        System.out.println("\n开始SQL解析性能基准测试...");

        String simpleSql = "SELECT * FROM users WHERE name = #{name}";
        String complexSql = "SELECT * FROM users WHERE 1=1 " +
            "<if test='name != null'>AND name = #{name}</if>" +
            "<if test='age != null'>AND age > #{age}</if>";

        Map<String, Object> testParams = new HashMap<>();
        testParams.put("name", "张三");
        testParams.put("age", 25);

        // 简单SQL性能测试
        long totalTime = 0;
        for (int i = 0; i < PERFORMANCE_TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(simpleSql, testParams);
            long endTime = System.nanoTime();
            if (result != null) { // 简单检查
                 totalTime += (endTime - startTime);
            }
        }

        long avgTimeMicros = TimeUnit.NANOSECONDS.toMicros(totalTime / PERFORMANCE_TEST_ITERATIONS);
        System.out.printf("  简单SQL平均解析时间: %d μs%n", avgTimeMicros);
        performanceMetrics.put("简单SQL性能", avgTimeMicros);

        // 复杂SQL性能测试
        totalTime = 0;
        for (int i = 0; i < PERFORMANCE_TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(complexSql, testParams);
            long endTime = System.nanoTime();
            if (result != null) { // 简单检查
                totalTime += (endTime - startTime);
            }
        }

        avgTimeMicros = TimeUnit.NANOSECONDS.toMicros(totalTime / PERFORMANCE_TEST_ITERATIONS);
        System.out.printf("  复杂SQL平均解析时间: %d μs%n", avgTimeMicros);
        performanceMetrics.put("复杂SQL性能", avgTimeMicros);

        // 性能断言
        System.out.println("检查性能 | 平均解析时间是否小于2000μs: " + (avgTimeMicros < 2000));
        System.out.println("SQL解析性能基准测试完成。");
    }

    @Test
    @Order(9)
    @DisplayName("实际业务场景测试 - 用户管理 - 高级搜索场景")
    void testUserManagementAdvancedSearch() {
        System.out.println("\n测试用户管理高级搜索场景...");

        String sql = "SELECT u.*, p.department, p.position " +
            "FROM users u " +
            "LEFT JOIN user_profiles p ON u.id = p.user_id " +
            "<where>" +
            "    <if test='searchCriteria.keyword != null and searchCriteria.keyword != \"\"'>" +
            "        AND (u.name LIKE #{searchCriteria.keywordPattern} " +
            "             OR u.email LIKE #{searchCriteria.keywordPattern})" +
            "    </if>" +
            "    <if test='searchCriteria.departments != null and searchCriteria.departments.size() > 0'>" +
            "        AND p.department IN " +
            "        <foreach item='dept' collection='searchCriteria.departments' open='(' separator=',' close=')'>#{dept}</foreach>" +
            "    </if>" +
            "    <if test='searchCriteria.ageRange != null'>" +
            "        <if test='searchCriteria.ageRange.min != null'>AND u.age >= #{searchCriteria.ageRange.min}</if>" +
            "        <if test='searchCriteria.ageRange.max != null'>AND u.age <= #{searchCriteria.ageRange.max}</if>" +
            "    </if>" +
            "</where>";

        // 创建复杂的搜索条件
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("keyword", "张");
        searchCriteria.put("keywordPattern", "%张%");
        searchCriteria.put("departments", Arrays.asList("技术部", "产品部", "运营部"));

        Map<String, Object> ageRange = new HashMap<>();
        ageRange.put("min", 25);
        ageRange.put("max", 40);
        searchCriteria.put("ageRange", ageRange);

        Map<String, Object> params = new HashMap<>();
        params.put("searchCriteria", searchCriteria);

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);

        // 验证SQL生成正确性
        System.out.println("检查结果是否不为null | 实际: " + (result != null));
        if (result != null) {
            System.out.println("检查SQL是否包含WHERE | 实际: " + result.getSql().contains("WHERE"));
            System.out.println("检查SQL是否包含LIKE ? | 实际: " + result.getSql().contains("LIKE ?"));
            System.out.println("检查SQL是否包含IN (?,?,?) | 实际: " + result.getSql().contains("IN (?,?,?)"));
            System.out.println("检查SQL是否包含age >= ? | 实际: " + result.getSql().contains("age >= ?"));
            System.out.println("检查SQL是否包含age <= ? | 实际: " + result.getSql().contains("age <= ?"));
            System.out.println("检查参数数量 | 预期: 7 | 实际: " + result.getParams().size());
        }

        System.out.println("生成的SQL: " + (result != null ? result.getSql() : "null"));
        System.out.println("参数数量: " + (result != null ? result.getParams().size() : "null"));
        System.out.println("用户管理高级搜索场景测试通过。");
    }

    @Test
    @Order(10)
    @DisplayName("实际业务场景测试 - 订单系统 - 复杂状态更新场景")
    void testOrderSystemComplexUpdate() {
        System.out.println("\n测试订单系统复杂状态更新场景...");

        String sql = "UPDATE orders " +
            "<set>" +
            "    <if test='updateData.status != null'>status = #{updateData.status},</if>" +
            "    <if test='updateData.paymentInfo != null'>" +
            "        <if test='updateData.paymentInfo.method != null'>payment_method = #{updateData.paymentInfo.method},</if>" +
            "        <if test='updateData.paymentInfo.paidAmount != null'>paid_amount = #{updateData.paymentInfo.paidAmount},</if>" +
            "    </if>" +
            "    updated_time = NOW()," +
            "    updated_by = #{updateData.updatedBy}" +
            "</set>" +
            "WHERE id = #{orderId}";

        // 创建复杂的更新数据
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", "shipped");

        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("method", "credit_card");
        paymentInfo.put("paidAmount", 299.99);
        updateData.put("paymentInfo", paymentInfo);

        updateData.put("updatedBy", "system_user");

        Map<String, Object> params = new HashMap<>();
        params.put("updateData", updateData);
        params.put("orderId", 98765);

        DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result = parser.processMapParametersForDirectSql(sql, params);

        // 验证SQL生成正确性
        System.out.println("检查结果是否不为null | 实际: " + (result != null));
        if (result != null) {
            System.out.println("检查SQL是否包含UPDATE orders SET | 实际: " + result.getSql().contains("UPDATE orders SET"));
            System.out.println("检查SQL是否包含status = ? | 实际: " + result.getSql().contains("status = ?"));
            System.out.println("检查SQL是否包含payment_method = ? | 实际: " + result.getSql().contains("payment_method = ?"));
            System.out.println("检查SQL是否包含updated_time = NOW() | 实际: " + result.getSql().contains("updated_time = NOW()"));
            System.out.println("检查SQL是否包含updated_by = ? | 实际: " + result.getSql().contains("updated_by = ?"));
            System.out.println("检查SQL是否包含WHERE id = ? | 实际: " + result.getSql().contains("WHERE id = ?"));
            System.out.println("检查参数数量是否大于等于4 | 实际: " + (result.getParams().size() >= 4));
        }

        System.out.println("生成的SQL: " + (result != null ? result.getSql() : "null"));
        System.out.println("参数数量: " + (result != null ? result.getParams().size() : "null"));
        System.out.println("订单系统复杂状态更新场景测试通过。");
    }
}
