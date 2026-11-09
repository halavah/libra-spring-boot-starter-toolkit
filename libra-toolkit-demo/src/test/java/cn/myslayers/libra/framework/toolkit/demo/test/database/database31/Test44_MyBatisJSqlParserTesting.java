package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试MyBatisSqlParser与JSqlParser集成的SQL解析功能
 */
@DisplayName("MyBatis参数测试类 - JSqlParser")
public class Test44_MyBatisJSqlParserTesting {

    private Map<String, Object> testParams;

    @BeforeEach
    void setUp() {
        testParams = new HashMap<>();
        testParams.put("id", 1);
        testParams.put("name", "张三");
        testParams.put("age", 25);
        testParams.put("email", "zhangsan@example.com");
        testParams.put("status", "ACTIVE");
        testParams.put("active", true);
        testParams.put("salary", 5000.0);
        testParams.put("department", "IT");
    }

    @Test
    @DisplayName("测试基础SELECT语句解析")
    void testBasicSelectParsing() {
        String sql = "SELECT * FROM users WHERE id = #{id}";
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试基础SELECT语句解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含SELECT: " + result.getSql().contains("SELECT"));
            System.out.println("SQL包含FROM: " + result.getSql().contains("FROM"));
            System.out.println("SQL包含WHERE: " + result.getSql().contains("WHERE"));
            System.out.println("SQL包含参数占位符: " + result.getSql().contains("?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试复杂SELECT语句解析")
    void testComplexSelectParsing() {
        String sql = "SELECT u.id, u.name, u.age, u.email, d.name as dept_name " +
                    "FROM users u " +
                    "LEFT JOIN departments d ON u.department_id = d.id " +
                    "WHERE u.age > #{age} AND u.status = #{status} " +
                    "ORDER BY u.create_time DESC " +
                    "LIMIT 10";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试复杂SELECT语句解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含LEFT JOIN: " + result.getSql().contains("LEFT JOIN"));
            System.out.println("SQL包含ORDER BY: " + result.getSql().contains("ORDER BY"));
            System.out.println("SQL包含LIMIT: " + result.getSql().contains("LIMIT"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试INSERT语句解析")
    void testInsertParsing() {
        String sql = "INSERT INTO users (name, age, email, status, active, salary, department) " +
                    "VALUES (#{name}, #{age}, #{email}, #{status}, #{active}, #{salary}, #{department})";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试INSERT语句解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含INSERT INTO: " + result.getSql().contains("INSERT INTO"));
            System.out.println("SQL包含VALUES: " + result.getSql().contains("VALUES"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 7));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试UPDATE语句解析")
    void testUpdateParsing() {
        String sql = "UPDATE users SET name = #{name}, age = #{age}, email = #{email}, " +
                    "status = #{status}, active = #{active}, salary = #{salary} " +
                    "WHERE id = #{id}";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试UPDATE语句解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含UPDATE: " + result.getSql().contains("UPDATE"));
            System.out.println("SQL包含SET: " + result.getSql().contains("SET"));
            System.out.println("SQL包含WHERE: " + result.getSql().contains("WHERE"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 7));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试DELETE语句解析")
    void testDeleteParsing() {
        String sql = "DELETE FROM users WHERE id = #{id} AND status = #{status}";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试DELETE语句解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含DELETE FROM: " + result.getSql().contains("DELETE FROM"));
            System.out.println("SQL包含WHERE: " + result.getSql().contains("WHERE"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 2));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试子查询解析")
    void testSubqueryParsing() {
        String sql = "SELECT * FROM users WHERE id IN " +
                    "(SELECT user_id FROM user_roles WHERE role_id = #{roleId}) " +
                    "AND status = #{status}";

        Map<String, Object> params = new HashMap<>();
        params.put("roleId", 1);
        params.put("status", "ACTIVE");

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试子查询解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含子查询: " + result.getSql().contains("SELECT user_id FROM user_roles"));
            System.out.println("SQL包含IN子句: " + result.getSql().contains("IN"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 2));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试UNION查询解析")
    void testUnionQueryParsing() {
        String sql = "SELECT name, age FROM users WHERE status = #{status} " +
                    "UNION ALL " +
                    "SELECT name, age FROM archived_users WHERE status = #{status}";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试UNION查询解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含UNION ALL: " + result.getSql().contains("UNION ALL"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 2));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试聚合函数解析")
    void testAggregateFunctionParsing() {
        String sql = "SELECT COUNT(*) as total, " +
                    "AVG(age) as avg_age, " +
                    "MAX(salary) as max_salary, " +
                    "MIN(salary) as min_salary, " +
                    "SUM(salary) as total_salary " +
                    "FROM users " +
                    "WHERE department = #{department} " +
                    "GROUP BY department " +
                    "HAVING COUNT(*) > #{minCount}";

        Map<String, Object> params = new HashMap<>();
        params.put("department", "IT");
        params.put("minCount", 5);

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试聚合函数解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含聚合函数: " + (result.getSql().contains("COUNT(*)") && result.getSql().contains("AVG(age)")));
            System.out.println("SQL包含GROUP BY: " + result.getSql().contains("GROUP BY"));
            System.out.println("SQL包含HAVING: " + result.getSql().contains("HAVING"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 2));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试窗口函数解析")
    void testWindowFunctionParsing() {
        String sql = "SELECT id, name, salary, " +
                    "ROW_NUMBER() OVER (ORDER BY salary DESC) as rank, " +
                    "RANK() OVER (PARTITION BY department ORDER BY salary DESC) as dept_rank " +
                    "FROM users " +
                    "WHERE status = #{status}";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试窗口函数解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含ROW_NUMBER: " + result.getSql().contains("ROW_NUMBER()"));
            System.out.println("SQL包含OVER子句: " + result.getSql().contains("OVER"));
            System.out.println("SQL包含PARTITION BY: " + result.getSql().contains("PARTITION BY"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 1));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试CTE查询解析")
    void testCTEQueryParsing() {
        String sql = "WITH dept_stats AS (" +
                    "SELECT department, COUNT(*) as emp_count, AVG(salary) as avg_salary " +
                    "FROM users " +
                    "WHERE status = #{status} " +
                    "GROUP BY department" +
                    ") " +
                    "SELECT d.department, d.emp_count, d.avg_salary " +
                    "FROM dept_stats d " +
                    "WHERE d.emp_count > #{minCount}";

        Map<String, Object> params = new HashMap<>();
        params.put("status", "ACTIVE");
        params.put("minCount", 3);

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试CTE查询解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含WITH子句: " + result.getSql().contains("WITH"));
            System.out.println("SQL包含CTE名称: " + result.getSql().contains("dept_stats"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 2));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试多表连接解析")
    void testMultiTableJoinParsing() {
        String sql = "SELECT u.id, u.name, d.name as dept_name, r.name as role_name " +
                    "FROM users u " +
                    "INNER JOIN departments d ON u.department_id = d.id " +
                    "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
                    "LEFT JOIN roles r ON ur.role_id = r.id " +
                    "WHERE u.status = #{status} AND d.active = #{active}";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试多表连接解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含INNER JOIN: " + result.getSql().contains("INNER JOIN"));
            System.out.println("SQL包含LEFT JOIN: " + result.getSql().contains("LEFT JOIN"));
            System.out.println("SQL包含多个表别名: " + (result.getSql().contains("u.") && result.getSql().contains("d.") && result.getSql().contains("r.")));
            System.out.println("参数数量正确: " + (result.getParams().size() == 2));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试存储过程调用解析")
    void testStoredProcedureCallParsing() {
        String sql = "CALL GetUserStatistics(#{startDate}, #{endDate}, #{department})";

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2023-01-01");
        params.put("endDate", "2023-12-31");
        params.put("department", "IT");

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试存储过程调用解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含CALL: " + result.getSql().contains("CALL"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 3));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试SQL语法错误处理")
    void testSqlSyntaxErrorHandling() {
        String invalidSql = "SELECT * FROM users WHERE id = #{id} AND"; // 语法错误

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(invalidSql, testParams);

        System.out.println("=== 测试SQL语法错误处理 ===");
        System.out.println("无效SQL: " + invalidSql);
        System.out.println("解析结果处理: " + (result != null ? "成功处理" : "返回null"));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试复杂嵌套查询解析")
    void testComplexNestedQueryParsing() {
        String sql = "SELECT u.id, u.name, " +
                    "(SELECT COUNT(*) FROM orders o WHERE o.user_id = u.id AND o.status = #{orderStatus}) as order_count, " +
                    "(SELECT MAX(o.create_time) FROM orders o WHERE o.user_id = u.id) as last_order_time " +
                    "FROM users u " +
                    "WHERE u.status = #{status} " +
                    "AND EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.amount > #{minAmount})";

        Map<String, Object> params = new HashMap<>();
        params.put("orderStatus", "COMPLETED");
        params.put("status", "ACTIVE");
        params.put("minAmount", 100.0);

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试复杂嵌套查询解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含子查询: " + result.getSql().contains("SELECT COUNT(*)"));
            System.out.println("SQL包含EXISTS: " + result.getSql().contains("EXISTS"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 3));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试数据库特定函数解析")
    void testDatabaseSpecificFunctionParsing() {
        String sql = "SELECT id, name, " +
                    "CONCAT(name, ' - ', email) as display_name, " +
                    "DATE_FORMAT(create_time, '%Y-%m-%d') as create_date, " +
                    "IFNULL(phone, 'N/A') as phone_display " +
                    "FROM users " +
                    "WHERE create_time >= STR_TO_DATE(#{startDate}, '%Y-%m-%d') " +
                    "AND status = #{status}";

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2023-01-01");
        params.put("status", "ACTIVE");

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试数据库特定函数解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含CONCAT函数: " + result.getSql().contains("CONCAT"));
            System.out.println("SQL包含DATE_FORMAT函数: " + result.getSql().contains("DATE_FORMAT"));
            System.out.println("SQL包含IFNULL函数: " + result.getSql().contains("IFNULL"));
            System.out.println("SQL包含STR_TO_DATE函数: " + result.getSql().contains("STR_TO_DATE"));
            System.out.println("参数数量正确: " + (result.getParams().size() == 2));
        }
        System.out.println();
    }
}
