package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser.MyBatisSqlParameterConverter;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试MyBatisSqlParser的基本SQL解析和参数处理功能
 */
@DisplayName("Test01: 基础语法和参数")
public class Test31_ParserBasicSyntaxTesting {

    private Map<String, Object> testParams;

    @BeforeEach
    void setUp() {
        testParams = new HashMap<>();
        testParams.put("name", "张三");
        testParams.put("age", 25);
        testParams.put("status", "ACTIVE");
        testParams.put("salary", 5000.0);
    }

    @Test
    @DisplayName("测试基本SQL解析")
    void testBasicSqlParsing() {
        String sql = "SELECT * FROM users WHERE name = #{name}";
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试基本SQL解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        System.out.println("解析后的SQL: " + (result != null ? result.getSql() : "null"));
        System.out.println("参数数量: " + (result != null ? result.getParams().size() : 0));
        System.out.println("参数内容: " + (result != null ? result.getParams() : "null"));
        System.out.println();
    }

    @Test
    @DisplayName("测试多参数SQL解析")
    void testMultipleParametersSql() {
        String sql = "SELECT * FROM users WHERE name = #{name} AND age = #{age} AND status = #{status}";
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试多参数SQL解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        System.out.println("解析后的SQL: " + (result != null ? result.getSql() : "null"));
        System.out.println("参数数量: " + (result != null ? result.getParams().size() : 0));
        System.out.println("参数内容: " + (result != null ? result.getParams() : "null"));
        System.out.println();
    }

    @Test
    @DisplayName("测试带参数的MyBatis SQL基础功能")
    void testMyBatisSqlWithParamsBasic() {
        String sql = "SELECT * FROM users WHERE name = #{name} AND age = #{age} AND status = #{status}";
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试带参数的MyBatis SQL基础功能 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含SELECT: " + result.getSql().contains("SELECT"));
            System.out.println("SQL包含WHERE: " + result.getSql().contains("WHERE"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试直接SQL参数转换")
    void testDirectSqlParameterConversion() {
        String sql = "SELECT * FROM users WHERE name = #{name} AND age = #{age}";
        MyBatisSqlWithParams result = MyBatisSqlParameterConverter.convertDirectSql(sql, testParams);

        System.out.println("=== 测试直接SQL参数转换 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试空参数处理")
    void testEmptyParameters() {
        String sql = "SELECT * FROM users WHERE name = #{name}";
        Map<String, Object> emptyParams = new HashMap<>();
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, emptyParams);

        System.out.println("=== 测试空参数处理 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试null参数处理")
    void testNullParameters() {
        String sql = "SELECT * FROM users WHERE name = #{name} AND age = #{age}";
        Map<String, Object> nullParams = new HashMap<>();
        nullParams.put("name", null);
        nullParams.put("age", null);
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, nullParams);

        System.out.println("=== 测试null参数处理 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试复杂SQL解析")
    void testComplexSqlParsing() {
        String sql = "SELECT u.id, u.name, u.age, u.status, u.salary " +
                    "FROM users u " +
                    "WHERE u.name = #{name} " +
                    "AND u.age > #{age} " +
                    "AND u.status = #{status} " +
                    "ORDER BY u.salary DESC";
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试复杂SQL解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含SELECT: " + result.getSql().contains("SELECT"));
            System.out.println("SQL包含ORDER BY: " + result.getSql().contains("ORDER BY"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试INSERT语句解析")
    void testInsertStatement() {
        String sql = "INSERT INTO users (name, age, status, salary) VALUES (#{name}, #{age}, #{status}, #{salary})";
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试INSERT语句解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含INSERT: " + result.getSql().contains("INSERT"));
            System.out.println("SQL包含VALUES: " + result.getSql().contains("VALUES"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试UPDATE语句解析")
    void testUpdateStatement() {
        String sql = "UPDATE users SET name = #{name}, age = #{age}, status = #{status} WHERE id = #{id}";
        testParams.put("id", 1);
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
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试DELETE语句解析")
    void testDeleteStatement() {
        String sql = "DELETE FROM users WHERE name = #{name} AND age = #{age}";
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试DELETE语句解析 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含DELETE: " + result.getSql().contains("DELETE"));
            System.out.println("SQL包含WHERE: " + result.getSql().contains("WHERE"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试特殊字符参数处理")
    void testSpecialCharacterParameters() {
        Map<String, Object> specialParams = new HashMap<>();
        specialParams.put("name", "张三'李四");
        specialParams.put("description", "这是一个\"测试\"描述");
        specialParams.put("path", "C:\\Users\\test\\file.txt");

        String sql = "SELECT * FROM users WHERE name = #{name} AND description = #{description} AND path = #{path}";
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, specialParams);

        System.out.println("=== 测试特殊字符参数处理 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试数字类型参数")
    void testNumericParameters() {
        Map<String, Object> numericParams = new HashMap<>();
        numericParams.put("intValue", 42);
        numericParams.put("longValue", 999999999L);
        numericParams.put("doubleValue", 3.14159);
        numericParams.put("floatValue", 2.71f);

        String sql = "SELECT * FROM data WHERE int_col = #{intValue} AND long_col = #{longValue} AND double_col = #{doubleValue} AND float_col = #{floatValue}";
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, numericParams);

        System.out.println("=== 测试数字类型参数 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
        }
        System.out.println();
    }
}
