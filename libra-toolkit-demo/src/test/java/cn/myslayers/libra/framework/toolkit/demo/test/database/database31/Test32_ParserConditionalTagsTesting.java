package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 测试MyBatisSqlParser的各种条件表达式解析和求值功能
 */
@DisplayName("Test02: 条件标签 (if/choose)")
public class Test32_ParserConditionalTagsTesting {

    private Map<String, Object> testParams;

    @BeforeEach
    void setUp() {
        testParams = new HashMap<>();
        testParams.put("name", "张三");
        testParams.put("age", 25);
        testParams.put("salary", 5000.0);
        testParams.put("active", true);
        testParams.put("department", "IT");
    }

    @Test
    @DisplayName("测试null条件判断")
    void testNullConditions() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"name != null\"> AND name = #{name} </if>" +
                    "<if test=\"email != null\"> AND email = #{email} </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试null条件判断 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含name条件: " + result.getSql().contains("name = ?"));
            System.out.println("SQL不包含email条件: " + !result.getSql().contains("email = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试字符串比较")
    void testStringComparisons() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"name == '张三'\"> AND name = #{name} </if>" +
                    "<if test=\"name != '李四'\"> AND status = 'ACTIVE' </if>" +
                    "<if test=\"department == 'IT'\"> AND department = #{department} </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试字符串比较 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含name条件: " + result.getSql().contains("name = ?"));
            System.out.println("SQL包含status条件: " + result.getSql().contains("status = 'ACTIVE'"));
            System.out.println("SQL包含department条件: " + result.getSql().contains("department = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试数值比较")
    void testNumericComparisons() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"age > 18\"> AND age = #{age} </if>" +
                    "<if test=\"age >= 25\"> AND status = 'ADULT' </if>" +
                    "<if test=\"age < 60\"> AND category = 'YOUNG' </if>" +
                    "<if test=\"age <= 30\"> AND group_type = 'JUNIOR' </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试数值比较 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含age条件: " + result.getSql().contains("age = ?"));
            System.out.println("SQL包含ADULT状态: " + result.getSql().contains("status = 'ADULT'"));
            System.out.println("SQL包含YOUNG分类: " + result.getSql().contains("category = 'YOUNG'"));
            System.out.println("SQL包含JUNIOR组别: " + result.getSql().contains("group_type = 'JUNIOR'"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试浮点数比较")
    void testFloatComparisons() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"salary > 3000.0\"> AND salary = #{salary} </if>" +
                    "<if test=\"salary >= 5000.0\"> AND level = 'HIGH' </if>" +
                    "<if test=\"salary < 10000.0\"> AND level != 'EXECUTIVE' </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试浮点数比较 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含salary条件: " + result.getSql().contains("salary = ?"));
            System.out.println("SQL包含HIGH级别: " + result.getSql().contains("level = 'HIGH'"));
            System.out.println("SQL包含非EXECUTIVE: " + result.getSql().contains("level != 'EXECUTIVE'"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试布尔条件")
    void testBooleanConditions() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"active == true\"> AND active = #{active} </if>" +
                    "<if test=\"active\"> AND status = 'ENABLED' </if>" +
                    "<if test=\"!active\"> AND status = 'DISABLED' </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试布尔条件 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含active条件: " + result.getSql().contains("active = ?"));
            System.out.println("SQL包含ENABLED状态: " + result.getSql().contains("status = 'ENABLED'"));
            System.out.println("SQL不包含DISABLED状态: " + !result.getSql().contains("status = 'DISABLED'"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试逻辑AND操作符")
    void testLogicalAndOperator() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"name != null and age > 18\"> AND name = #{name} AND age = #{age} </if>" +
                    "<if test=\"salary > 3000 and active == true\"> AND salary = #{salary} </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试逻辑AND操作符 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含name和age条件: " + (result.getSql().contains("name = ?") && result.getSql().contains("age = ?")));
            System.out.println("SQL包含salary条件: " + result.getSql().contains("salary = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试逻辑OR操作符")
    void testLogicalOrOperator() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", null);
        params.put("age", 25);
        params.put("active", true);

        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"name != null or age > 18\"> AND condition1 = 'TRUE' </if>" +
                    "<if test=\"name == null or active == false\"> AND condition2 = 'TRUE' </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试逻辑OR操作符 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含condition1: " + result.getSql().contains("condition1 = 'TRUE'"));
            System.out.println("SQL包含condition2: " + result.getSql().contains("condition2 = 'TRUE'"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试复杂嵌套条件")
    void testComplexNestedConditions() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"(name != null and age > 18) or (salary > 5000 and active == true)\"> " +
                    "AND complex_condition = 'TRUE' " +
                    "</if>" +
                    "<if test=\"name != null and (age > 18 or salary > 3000)\"> " +
                    "AND another_condition = 'TRUE' " +
                    "</if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试复杂嵌套条件 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含complex_condition: " + result.getSql().contains("complex_condition = 'TRUE'"));
            System.out.println("SQL包含another_condition: " + result.getSql().contains("another_condition = 'TRUE'"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试字符串方法调用")
    void testStringMethodCalls() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"name != null and name.length() > 2\"> AND name = #{name} </if>" +
                    "<if test=\"department != null and department.startsWith('I')\"> AND department = #{department} </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试字符串方法调用 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含name条件: " + result.getSql().contains("name = ?"));
            System.out.println("SQL包含department条件: " + result.getSql().contains("department = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试条件表达式中的运算")
    void testArithmeticInConditions() {
        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"age + 5 > 30\"> AND age_group = 'SENIOR' </if>" +
                    "<if test=\"salary * 12 > 60000\"> AND annual_salary = #{salary} </if>" +
                    "<if test=\"age - 18 >= 7\"> AND experience_level = 'EXPERIENCED' </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, testParams);

        System.out.println("=== 测试条件表达式中的运算 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL不包含SENIOR年龄组: " + !result.getSql().contains("age_group = 'SENIOR'"));
            System.out.println("SQL不包含年薪条件: " + !result.getSql().contains("annual_salary = ?"));
            System.out.println("SQL包含经验级别: " + result.getSql().contains("experience_level = 'EXPERIENCED'"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试空字符串条件")
    void testEmptyStringConditions() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "");
        params.put("description", "   ");
        params.put("code", "ABC123");

        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"name != null and name != ''\"> AND name = #{name} </if>" +
                    "<if test=\"description != null and description.trim() != ''\"> AND description = #{description} </if>" +
                    "<if test=\"code != null and code != ''\"> AND code = #{code} </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试空字符串条件 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL不包含name条件: " + !result.getSql().contains("name = ?"));
            System.out.println("SQL不包含description条件: " + !result.getSql().contains("description = ?"));
            System.out.println("SQL包含code条件: " + result.getSql().contains("code = ?"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试集合条件判断")
    void testCollectionConditions() {
        Map<String, Object> params = new HashMap<>();
        params.put("keywords", java.util.Arrays.asList("Java", "Spring"));
        params.put("emptyList", java.util.Collections.emptyList());
        params.put("nullList", null);

        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"keywords != null and keywords.size() > 0\"> AND has_keywords = 'TRUE' </if>" +
                    "<if test=\"emptyList != null and emptyList.size() > 0\"> AND has_empty_list = 'TRUE' </if>" +
                    "<if test=\"nullList != null and nullList.size() > 0\"> AND has_null_list = 'TRUE' </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试集合条件判断 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含keywords条件: " + result.getSql().contains("has_keywords = 'TRUE'"));
            System.out.println("SQL不包含empty_list条件: " + !result.getSql().contains("has_empty_list = 'TRUE'"));
            System.out.println("SQL不包含null_list条件: " + !result.getSql().contains("has_null_list = 'TRUE'"));
        }
        System.out.println();
    }

    @Test
    @DisplayName("测试条件表达式求值错误处理")
    void testConditionEvaluationErrorHandling() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "张三");
        params.put("nullValue", null);

        String sql = "SELECT * FROM users WHERE 1=1 " +
                    "<if test=\"name != null\"> AND name = #{name} </if>" +
                    "<if test=\"nullValue != null and nullValue.length() > 0\"> AND null_value = #{nullValue} </if>" +
                    "<if test=\"undefinedParam != null\"> AND undefined_param = #{undefinedParam} </if>";

        MyBatisSqlWithParams result = MyBatisSqlParser.parse(sql, params);

        System.out.println("=== 测试条件表达式求值错误处理 ===");
        System.out.println("原始SQL: " + sql);
        System.out.println("解析结果不为空: " + (result != null));
        if (result != null) {
            System.out.println("解析后的SQL: " + result.getSql());
            System.out.println("参数数量: " + result.getParams().size());
            System.out.println("参数内容: " + result.getParams());
            System.out.println("SQL包含name条件: " + result.getSql().contains("name = ?"));
            System.out.println("SQL不包含null_value条件: " + !result.getSql().contains("null_value = ?"));
            System.out.println("SQL不包含undefined_param条件: " + !result.getSql().contains("undefined_param = ?"));
        }
        System.out.println();
    }
}
