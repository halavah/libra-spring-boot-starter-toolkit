package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * MyBatis参数测试类 - 专注于MyBatis动态SQL参数处理
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil31 - MyBatis参数处理测试")
public class Test43_MyBatisParametersTesting {

    @AfterAll
    static void tearDown() {
        System.out.println("\nMyBatis参数处理测试完成");
    }

    // 测试基本的MyBatis参数转换
    @Test
    @Order(1)
    @DisplayName("测试基本MyBatis参数转换")
    public void testBasicMyBatisParameterConversion() {
        System.out.println("=== 测试基本MyBatis参数转换 ===");

        try {
            String sql = "SELECT * FROM users WHERE name = #{name} AND age = #{age}";
            Map<String, Object> params = new HashMap<>();
            params.put("name", "张三");
            params.put("age", 25);

            DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result =
                DatabaseUtil31.MyBatisSqlParser.parse(sql, params);

            System.out.println("原始SQL: " + sql);
            System.out.println("解析后SQL: " + result.getSql());
            System.out.println("参数列表: " + result.getParams());

            if (result.getSql().contains("?") && result.getParams().size() == 2) {
                System.out.println("✓ 基本MyBatis参数转换测试通过");
            } else {
                System.out.println("✗ 基本MyBatis参数转换测试失败");
            }

        } catch (Exception e) {
            System.out.println("✗ 基本MyBatis参数转换测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试条件参数处理
    @Test
    @Order(2)
    @DisplayName("测试条件参数处理")
    public void testConditionalParameters() {
        System.out.println("\n=== 测试条件参数处理 ===");

        try {
            String sql = "SELECT * FROM users WHERE 1=1 " +
                        "<if test='name != null'>AND name = #{name}</if> " +
                        "<if test='age != null'>AND age > #{age}</if>";

            Map<String, Object> params = new HashMap<>();
            params.put("name", "李四");
            params.put("age", 30);

            DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result =
                DatabaseUtil31.MyBatisSqlParser.parse(sql, params);

            System.out.println("条件参数处理结果:");
            System.out.println("SQL: " + result.getSql());
            System.out.println("参数: " + result.getParams());

            if (result.getParams().size() == 2) {
                System.out.println("✓ 条件参数处理测试通过");
            } else {
                System.out.println("✗ 条件参数处理测试失败");
            }

        } catch (Exception e) {
            System.out.println("✗ 条件参数处理测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 测试集合参数处理
    @Test
    @Order(3)
    @DisplayName("测试集合参数处理")
    public void testCollectionParameters() {
        System.out.println("\n=== 测试集合参数处理 ===");

        try {
            String sql = "SELECT * FROM users WHERE id IN " +
                        "<foreach collection='userIds' item='id' open='(' close=')' separator=','>" +
                        "#{id}" +
                        "</foreach>";

            Map<String, Object> params = new HashMap<>();
            params.put("userIds", Arrays.asList(1, 2, 3));

            DatabaseUtil31.MyBatisSqlParser.MyBatisSqlWithParams result =
                DatabaseUtil31.MyBatisSqlParser.parse(sql, params);

            System.out.println("集合参数处理结果:");
            System.out.println("SQL: " + result.getSql());
            System.out.println("参数: " + result.getParams());

            if (result.getParams().size() == 3) {
                System.out.println("✓ 集合参数处理测试通过");
            } else {
                System.out.println("✗ 集合参数处理测试失败");
            }

        } catch (Exception e) {
            System.out.println("✗ 集合参数处理测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
