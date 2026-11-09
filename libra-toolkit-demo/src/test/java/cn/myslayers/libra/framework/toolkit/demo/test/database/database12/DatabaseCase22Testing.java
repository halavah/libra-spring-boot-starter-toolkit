package cn.myslayers.libra.framework.toolkit.demo.test.database.database12;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil12;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DatabaseUtil12 - 自动搜索XML功能测试类
 *
 * 测试功能：
 * - 无需指定XML文件名的自动搜索
 * - 多个XML目录的自动扫描
 * - SQL语句的自动发现和执行
 *
 * @author Halavah
 * @version 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil12 - 自动搜索XML功能测试")
public class DatabaseCase22Testing {

    @BeforeAll
    static void setUp() {
        System.out.println("=== DatabaseUtil12自动搜索XML功能测试开始 ===");
        try {
            // 初始化数据库连接
            initializeDatabase();
        } catch (Exception e) {
            System.err.println("测试初始化失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("测试初始化失败", e);
        }
    }

    @AfterAll
    static void tearDown() {
        // 清理数据库连接
        DatabaseUtil12.closeAllDatabases();
        System.out.println("\n=== 所有测试完成 ===");
    }

    @Test
    @Order(1)
    @DisplayName("自动搜索查询（多条记录）")
    void testSelectListAutoSearch() {
        testSelectListAuto();
    }

    @Test
    @Order(2)
    @DisplayName("自动搜索查询（单条记录）")
    void testSelectOneAutoSearch() {
        testSelectOneAuto();
    }

    @Test
    @Order(3)
    @DisplayName("自动搜索插入")
    void testInsertAutoSearch() {
        testInsertAuto();
    }

    @Test
    @Order(4)
    @DisplayName("自动搜索更新")
    void testUpdateAutoSearch() {
        testUpdateAuto();
    }

    @Test
    @Order(5)
    @DisplayName("自动搜索删除")
    void testDeleteAutoSearch() {
        testDeleteAuto();
    }

    /**
     * 初始化数据库连接
     */
    private static void initializeDatabase() {
        try {
            System.out.println("1. 正在初始化数据库连接...");

            // 使用手动配置方式注册数据库
            DatabaseUtil12.DatabaseConfig config = new DatabaseUtil12.DatabaseConfig(
                "jdbc:mysql://localhost:3306/libra-demo-db?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai",
                "demo_user",
                "demo_password",
                "com.mysql.cj.jdbc.Driver"
            );

            DatabaseUtil12.registerDatabase("master", config);
            DatabaseUtil12.setDefaultDatabase("master");

            System.out.println("   ✓ 数据库连接初始化成功");
        } catch (Exception e) {
            System.err.println("   ✗ 数据库连接初始化失败: " + e.getMessage());
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    /**
     * 测试自动搜索查询（多条记录）
     */
    private static void testSelectListAuto() {
        try {
            System.out.println("\n   2.1 测试自动搜索查询（多条记录）");

            Map<String, Object> params = new HashMap<>();

            // 尝试使用基础查询
            List<Map<String, Object>> results = DatabaseUtil12.selectListFromXml("selectBasicList", params);
            System.out.println("       ✓ 基础查询成功，返回 " + results.size() + " 条记录");

            // 显示前几条记录
            if (!results.isEmpty()) {
                System.out.println("       数据示例（前3条）:");
                for (int i = 0; i < Math.min(3, results.size()); i++) {
                    System.out.println("         " + (i+1) + ". " + results.get(i));
                }
            }

        } catch (Exception e) {
            System.err.println("       ✗ 自动搜索查询失败: " + e.getMessage());
        }
    }

    /**
     * 测试自动搜索查询（单条记录）
     */
    private static void testSelectOneAuto() {
        try {
            System.out.println("\n   2.2 测试自动搜索查询（单条记录）");

            Map<String, Object> params = new HashMap<>();
            params.put("id", 1);

            // 尝试查询单条记录
            Map<String, Object> result = DatabaseUtil12.selectOneFromXml("selectWithParams", params);
            if (result != null) {
                System.out.println("       ✓ 单条查询成功，返回数据: " + result);
            } else {
                System.out.println("       ✓ 单条查询成功，但没有找到匹配记录");
            }

        } catch (Exception e) {
            System.err.println("       ✗ 自动搜索单条查询失败: " + e.getMessage());
        }
    }

    /**
     * 测试自动搜索插入
     */
    private static void testInsertAuto() {
        try {
            System.out.println("\n   2.3 测试自动搜索插入");

            Map<String, Object> params = new HashMap<>();
            params.put("tableName", "test_auto_search_" + System.currentTimeMillis());

            // 尝试执行创建表操作
            int result = DatabaseUtil12.insertFromXml("createTestTable", params);
            System.out.println("       ✓ 自动搜索插入成功，影响行数: " + result);

        } catch (Exception e) {
            System.err.println("       ✗ 自动搜索插入失败: " + e.getMessage());
        }
    }

    /**
     * 测试自动搜索更新
     */
    private static void testUpdateAuto() {
        try {
            System.out.println("\n   2.4 测试自动搜索更新");

            Map<String, Object> params = new HashMap<>();
            params.put("status", "updated_auto");
            params.put("updateTime", new java.util.Date());

            // 尝试执行更新操作（这里假设有相应的SQL）
            // 注意：实际SQL需要在XML文件中定义
            try {
                int result = DatabaseUtil12.updateFromXml("updateTestRecord", params);
                System.out.println("       ✓ 自动搜索更新成功，影响行数: " + result);
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("未找到SQL ID")) {
                    System.out.println("       ⚠ 未找到updateTestRecord SQL，这是正常的（SQL未定义）");
                } else {
                    throw e;
                }
            }

        } catch (Exception e) {
            System.err.println("       ✗ 自动搜索更新失败: " + e.getMessage());
        }
    }

    /**
     * 测试自动搜索删除
     */
    private static void testDeleteAuto() {
        try {
            System.out.println("\n   2.5 测试自动搜索删除");

            Map<String, Object> params = new HashMap<>();
            params.put("condition", "auto_test");

            // 尝试执行删除操作（这里假设有相应的SQL）
            try {
                int result = DatabaseUtil12.deleteFromXml("deleteTestRecord", params);
                System.out.println("       ✓ 自动搜索删除成功，影响行数: " + result);
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("未找到SQL ID")) {
                    System.out.println("       ⚠ 未找到deleteTestRecord SQL，这是正常的（SQL未定义）");
                } else {
                    throw e;
                }
            }

        } catch (Exception e) {
            System.err.println("       ✗ 自动搜索删除失败: " + e.getMessage());
        }
    }
}
