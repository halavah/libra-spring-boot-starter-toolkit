package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试MyBatisSqlParser的各种MyBatis动态标签解析功能
 */
@DisplayName("Test03: 迭代及其他标签 (foreach/trim/set/where)")
public class Test33_ParserIterativeTagsTesting {

    private Map<String, Object> testParams;

    @BeforeEach
    void setUp() {
        testParams = new HashMap<>();
        testParams.put("name", "张三");
        testParams.put("age", 25);
        testParams.put("status", "ACTIVE");
        testParams.put("salary", 5000.0);
        testParams.put("keywords", Arrays.asList("Java", "Spring", "MyBatis"));
    }

    @Test
    @DisplayName("测试if标签 - 条件为真")
    void testIfTagTrue() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"name != null\"> AND name = #{name} </if>" +
                    "<if test=\"age != null\"> AND age = #{age} </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试if标签 - 条件为真 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含name条件: " + result.getSql().contains("name = ?"));
            System.out.println("SQL包含age条件: " + result.getSql().contains("age = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试if标签 - 条件为假")
    void testIfTagFalse() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", null);
        params.put("age", 0);

        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"name != null and name != ''\"> AND name = #{name} </if>" +
                    "<if test=\"age > 15\"> AND age = #{age} </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试if标签 - 条件为假 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL不包含name条件: " + !result.getSql().contains("name = ?"));
            System.out.println("SQL不包含age条件: " + !result.getSql().contains("age = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试where标签")
    void testWhereTag() {
        String sql = "SELECT * FROM users " +
                    "<where>" +
                    "<if test=\"name != null\"> AND name = #{name} </if>" +
                    "<if test=\"age != null\"> AND age = #{age} </if>" +
                    "<if test=\"status != null\"> AND status = #{status} </if>" +
                    "</where>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试where标签 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含WHERE: " + result.getSql().contains("WHERE"));
            System.out.println("SQL不以AND开头: " + !result.getSql().contains("WHERE AND"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试where标签 - 空条件")
    void testWhereTagEmpty() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", null);
        params.put("age", null);

        String sql = "SELECT * FROM users " +
                    "<where>" +
                    "<if test=\"name != null\"> AND name = #{name} </if>" +
                    "<if test=\"age != null\"> AND age = #{age} </if>" +
                    "</where>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试where标签 - 空条件 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            // 当没有条件时，where标签应该被移除
            System.out.println("SQL不包含WHERE: " + !result.getSql().contains("WHERE"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试set标签")
    void testSetTag() {
        String sql = "UPDATE users " +
                    "<set>" +
                    "<if test=\"name != null\"> name = #{name}, </if>" +
                    "<if test=\"age != null\"> age = #{age}, </if>" +
                    "<if test=\"status != null\"> status = #{status}, </if>" +
                    "</set>" +
                    "WHERE id = #{id}";

        testParams.put("id", 1);
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试set标签 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含SET: " + result.getSql().contains("SET"));
            System.out.println("SQL不以逗号结尾: " + !result.getSql().contains(", WHERE"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试choose/when/otherwise标签")
    void testChooseWhenOtherwise() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<choose>" +
                    "<when test=\"name != null\"> AND name = #{name} </when>" +
                    "<when test=\"age != null\"> AND age = #{age} </when>" +
                    "<otherwise> AND status = 'DEFAULT' </otherwise>" +
                    "</choose>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试choose/when/otherwise标签 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含name条件: " + result.getSql().contains("name = ?"));
            System.out.println("SQL不包含age条件: " + !result.getSql().contains("age = ?"));
            System.out.println("SQL不包含DEFAULT: " + !result.getSql().contains("DEFAULT"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试choose/when/otherwise标签 - 默认情况")
    void testChooseWhenOtherwiseDefault() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", null);
        params.put("age", null);

        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<choose>" +
                    "<when test=\"name != null\"> AND name = #{name} </when>" +
                    "<when test=\"age != null\"> AND age = #{age} </when>" +
                    "<otherwise> AND status = 'DEFAULT' </otherwise>" +
                    "</choose>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试choose/when/otherwise标签 - 默认情况 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL不包含name条件: " + !result.getSql().contains("name = ?"));
            System.out.println("SQL不包含age条件: " + !result.getSql().contains("age = ?"));
            System.out.println("SQL包含DEFAULT: " + result.getSql().contains("DEFAULT"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试trim标签")
    void testTrimTag() {
        String sql = "SELECT * FROM users " +
                    "<trim prefix=\"WHERE\" prefixOverrides=\"AND |OR \">" +
                    "<if test=\"name != null\"> AND name = #{name} </if>" +
                    "<if test=\"age != null\"> AND age = #{age} </if>" +
                    "</trim>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试trim标签 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含WHERE: " + result.getSql().contains("WHERE"));
            System.out.println("SQL不以AND开头: " + !result.getSql().contains("WHERE AND"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试foreach标签 - 集合")
    void testForeachTag() {
        String sql = "SELECT * FROM users WHERE id IN " +
                    "<foreach collection=\"keywords\" item=\"keyword\" open=\"(\" separator=\",\" close=\")\">" +
                    "#{keyword}" +
                    "</foreach>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试foreach标签 - 集合 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含IN: " + result.getSql().contains("IN"));
            System.out.println("SQL包含括号: " + (result.getSql().contains("(") && result.getSql().contains(")")));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试foreach标签 - 数组")
    void testForeachTagArray() {
        Map<String, Object> params = new HashMap<>();
        List<Integer> ids = Arrays.asList(1, 2, 3, 4, 5);
        params.put("ids", ids);

        String sql = "DELETE FROM users WHERE id IN " +
                    "<foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">" +
                    "#{id}" +
                    "</foreach>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试foreach标签 - 数组 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含DELETE: " + result.getSql().contains("DELETE"));
            System.out.println("SQL包含IN: " + result.getSql().contains("IN"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试bind标签")
    void testBindTag() {
        String sql = "<bind name=\"pattern\" value=\"'%' + name + '%'\" />" +
                    "SELECT * FROM users WHERE name LIKE #{pattern}";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试bind标签 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含LIKE: " + result.getSql().contains("LIKE"));
            System.out.println("参数包含%: " + (result.getParams().size() > 0 && result.getParams().get(0) != null && result.getParams().get(0).toString().contains("%")));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试复杂嵌套动态标签")
    void testComplexNestedTags() {
        String sql = "SELECT * FROM users " +
                    "<where>" +
                    "<choose>" +
                    "<when test=\"name != null\">" +
                    "AND name = #{name}" +
                    "<if test=\"age != null\"> AND age = #{age} </if>" +
                    "</when>" +
                    "<otherwise>" +
                    "<if test=\"status != null\"> AND status = #{status} </if>" +
                    "</otherwise>" +
                    "</choose>" +
                    "<if test=\"department != null\"> AND department = #{department} </if>" +
                    "</where>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试复杂嵌套动态标签 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含WHERE: " + result.getSql().contains("WHERE"));
            System.out.println("SQL包含name条件: " + result.getSql().contains("name = ?"));
            System.out.println("SQL包含age条件: " + result.getSql().contains("age = ?"));
            System.out.println("SQL包含department条件: " + result.getSql().contains("department = ?"));
            // 由于name不为null，应该不包含status条件
            System.out.println("SQL不包含status条件: " + !result.getSql().contains("status = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试动态更新语句")
    void testDynamicUpdateStatement() {
        String sql = "UPDATE users " +
                    "<set>" +
                    "<if test=\"name != null and name != ''\"> name = #{name}, </if>" +
                    "<if test=\"age != null\"> age = #{age}, </if>" +
                    "<if test=\"email != null and email != ''\"> email = #{email}, </if>" +
                    "<if test=\"status != null\"> status = #{status}, </if>" +
                    "</set>" +
                    "WHERE id = #{id}";

        testParams.put("id", 1);
        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试动态更新语句 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含UPDATE: " + result.getSql().contains("UPDATE"));
            System.out.println("SQL包含SET: " + result.getSql().contains("SET"));
            System.out.println("SQL包含WHERE: " + result.getSql().contains("WHERE"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试动态插入语句")
    void testDynamicInsertStatement() {
        String sql = "INSERT INTO users " +
                    "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">" +
                    "<if test=\"name != null\"> name, </if>" +
                    "<if test=\"age != null\"> age, </if>" +
                    "<if test=\"email != null\"> email, </if>" +
                    "<if test=\"status != null\"> status, </if>" +
                    "</trim>" +
                    "VALUES " +
                    "<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">" +
                    "<if test=\"name != null\"> #{name}, </if>" +
                    "<if test=\"age != null\"> #{age}, </if>" +
                    "<if test=\"email != null\"> #{email}, </if>" +
                    "<if test=\"status != null\"> #{status}, </if>" +
                    "</trim>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试动态插入语句 ===");
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
    @DisplayName("测试复杂条件表达式")
    void testComplexConditionExpressions() {
        String sql = "SELECT * FROM users " +
                    "<where>" +
                    "<if test=\"name != null and name != '' and name.length() > 2\">" +
                    "AND name = #{name}" +
                    "</if>" +
                    "<if test=\"age != null and age > 0 and age < 100\">" +
                    "AND age = #{age}" +
                    "</if>" +
                    "<if test=\"status != null and (status == 'ACTIVE' or status == 'INACTIVE')\">" +
                    "AND status = #{status}" +
                    "</if>" +
                    "</where>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试复杂条件表达式 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含WHERE: " + result.getSql().contains("WHERE"));
        }
        System.out.println();
    }
}
